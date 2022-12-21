from oci.functions import FunctionsInvokeClient, FunctionsManagementClient
from oci.retry import DEFAULT_RETRY_STRATEGY
from oci.auth.signers import InstancePrincipalsSecurityTokenSigner
from oci.signer import Signer
from oci.config import from_file
import logging
import logging.config

import json
import os
import queue
import random
import signal
import sys
import uuid

from concurrent.futures import ThreadPoolExecutor, _base

import requests
from requests.adapters import HTTPAdapter, Retry

from threading import Thread, Event, BoundedSemaphore
from time import sleep
from time import time as timestamp

from ratemate import RateLimit

# Disable OCI SDK service import for faster module import
os.environ["OCI_PYTHON_SDK_NO_SERVICE_IMPORTS"] = "1"


# Setup Queues which will be used for message handling
messages_queue = queue.Queue()
message_receipts_queue = queue.Queue()

# Queues to count failures number
queue_long_polling_failures = queue.Queue()
function_invocation_failures = queue.Queue()
MAX_consecutive_queue_long_polling_failures = 3
MAX_consecutive_function_invocation_failures = 10


# Environmnet variables setup
QUEUE_OCI_REGION = os.environ.get("QUEUE_OCI_REGION", "us-ashburn-1")
FUNCTION_OCI_REGION = os.environ.get("FUNCTION_OCI_REGION", "eu-frankfurt-1")
QUEUE_OCID = os.environ.get(
    "QUEUE_OCID", "ocid1.queue.oc1.iad.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
FUNCTION_OCID = os.environ.get("FUNCTION_OCID",
                               "ocid1.fnfunc.oc1.eu-frankfurt-1.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")

STATUS_FILE_PATH = r"/tmp/healthy"
FUNCTION_INVOKE_BODY_BATCH_SIZE = 100
FUNCTION_INVOKE_RETURN_BATCH_FAILURE = True
MESSAGE_DELETE_BATCH_SIZE = 20
QUEUE_API_MAX_RETRIES = 8  # Retries will be used only for delete attempts
QUEUE_FAST_POLLING_RPS = 400
QUEUE_DELETE_RPS = 400
QUEUE_FAST_POLLING_RAISE_INTERVAL = 3
QUEUE_FAST_POLLING_MAX_FUTURES = 1000
MAX_MESSAGES_IN_FUNCTION_INVOKE_QUEUE = 10000
MAX_FUTURES = 500  # maximum number of futures for fast polling

fast_polling_rate_limit = RateLimit(max_count=QUEUE_FAST_POLLING_RPS, per=1)
delete_rate_limit = RateLimit(max_count=QUEUE_DELETE_RPS, per=1)
####################################################################################################

# Logging setup
logging.config.fileConfig('logging.conf')
logger = logging.getLogger('queueEventsLogger')


class Queue:
    _path = "20210201"
    _N = 1

    def __init__(self, oci_region, queue_ocid, signer, get_visibilityInSeconds=600, get_timeoutInSeconds=20, get_fast_timeoutInSeconds=3, get_limit=20, delete_timeout=5):
        self.queue_ocid = queue_ocid
        self.signer = signer
        self.get_visibilityInSeconds = get_visibilityInSeconds
        self.get_timeoutInSeconds = get_timeoutInSeconds
        self.get_fast_timeoutInSeconds = get_fast_timeoutInSeconds
        self.get_limit = get_limit
        self.delete_message_timeout = delete_timeout
        self.cp_endpoint = f"https://messaging.{oci_region}.oci.oraclecloud.com/{self._path}"
        self.dp_endpoint = f"https://cell-{self._N}.queue.messaging.{oci_region}.oci.oraclecloud.com/{self._path}"


class Function:
    def __init__(self, function_ocid, function_endpoint, signer, config, timeout_connect=10, timeout_read=330):
        self.function_ocid = function_ocid
        self.function_endpoint = function_endpoint
        self.signer = signer
        self.config = config
        self.timeout_connect = timeout_connect
        self.timeout_read = timeout_read


def delete_message_from_queue_callback(future):
    global message_delete_th_pool
    try:
        exc = future.exception()
        if exc is not None:
            logger.error(
                f'An exception occurred during attempt to delete message from the queue: {exc}', exc_info=True)
        else:
            data, attempts = future.result()
            if attempts < QUEUE_API_MAX_RETRIES:
                if data.status_code == 200:
                    logger.info(f'Delete request response: {data.text}')
                elif data.status_code == 429:
                    sleep(random.randint(1000, 3000)/1000)
                    logger.info(
                        f'Too many message delete requests. Retrying deletion from queue.')
                    request_body = json.loads(data.request.body)
                    attempt += 1
                    future = message_delete_th_pool.submit(
                        delete_message_from_queue, request_body, attempts)
                    future.add_done_callback(
                        delete_message_from_queue_callback)
                else:
                    logger.debug(
                        f'Unexpected response when attempting to remove messages from queue: {data.status_code} | response_body: {data.text} | request_body: {data.request.body}')
            else:
                logger.error(
                    f'Failed to delete receipts from the queue in {attempts} attempts.')
    except Exception as exc:
        logger.error(
            f'An exception occurred during execution of delete_message_from_queue_callback function: {exc}', exc_info=True)


def delete_message_from_queue(body, attempt=1):
    try:
        delete_rate_limit.wait()
        logger.debug(
            f'Attempt {attempt} to delete messages from the queue using body: {body}')
        with requests.Session() as session:
            request_response = session.post(f'{OCI_QUEUE.dp_endpoint}/queues/{OCI_QUEUE.queue_ocid}/messages/actions/deleteMessages',
                                            auth=OCI_QUEUE.signer, data=body, timeout=OCI_QUEUE.delete_message_timeout)
            return request_response, attempt
    except Exception as exc:
        logger.error(
            f'An exception occurred during call of message deletion from queue: {exc}', exc_info=True)
        raise exc


def message_delete_daemon(message_delete_th_pool, callback_function=None):
    global MESSAGE_DELETE_BATCH_SIZE, message_receipts_queue, queue_long_polling_th_pool_shutdown_done, queue_fast_polling_th_pool_shutdown_done, function_invoke_th_pool_shutdown_done, message_delete_th_pool_shutdown_done, shutdown
    while True:
        if not message_receipts_queue.empty():
            logger.info(
                f'There are {message_receipts_queue.qsize()} receipts in the message receipts queue.')
            current_size = 0
            receipts = []
            while current_size < MESSAGE_DELETE_BATCH_SIZE:
                try:
                    message = message_receipts_queue.get_nowait()
                    receipts.append(message)
                    current_size += 1
                except queue.Empty:
                    break
                except Exception as exc:
                    logger.error(
                        f'An exception occurred during extraction of message from the message receipts queue {exc}', exc_info=True)
            entries = []
            for receipt_id in receipts:
                entries.append({"receipt": receipt_id})
            try:
                future = message_delete_th_pool.submit(
                    delete_message_from_queue, json.dumps({"entries": entries}))
                if callback_function != None:
                    future.add_done_callback(callback_function)
            except Exception as exc:
                logger.error(
                    f'An error occurred during creation of delete_message_from_queue future. Returning message receipts to the queue: {exc}.')
                for receipt_id in receipts:
                    message_receipts_queue.put_nowait(receipt_id)
        else:
            sleep(1)
            if shutdown.is_set() and queue_long_polling_th_pool_shutdown_done.is_set() and queue_fast_polling_th_pool_shutdown_done.is_set() and function_invoke_th_pool_shutdown_done.is_set():
                logger.info('Exiting message_delete_daemon_thread ... ')
                message_delete_th_pool.shutdown(wait=True)
                message_delete_th_pool_shutdown_done.set()
                return


def function_invoke_callback(future):
    global FUNCTION_INVOKE_RETURN_BATCH_FAILURE, message_receipts_queue
    try:
        exc = future.exception()
        if exc is not None:
            logger.error(
                f'An exception occurred during function invocation: {exc}', exc_info=True)
            queue_long_polling_failures.put_nowait(f'{exc}')
        else:
            invoke_response, start_execution_timestamp = future.result()
            logger.info(
                f'Function call result: {invoke_response.data.status_code} | {invoke_response.data.text}')
            if invoke_response.data.status_code == 200:
                # Clear invocation failures
                while not queue_long_polling_failures.empty():
                    queue_long_polling_failures.get_nowait()
                end_execution_timestamp = timestamp()
                logger.info(
                    f'Function successfully executed in {end_execution_timestamp-start_execution_timestamp} seconds')
                # Process batchItemFailures

                request_json = json.loads(invoke_response.request.body)
                request_messages_receipts = [message.get(
                    'receipt') for message in request_json]
                if FUNCTION_INVOKE_RETURN_BATCH_FAILURE:
                    try:
                        # Determine failed requests
                        response_json = invoke_response.data.json()
                        failed_messages_receipts = [entry.get(
                            "receipt_id", None) for entry in response_json.get("batchItemFailures", [])]
                    except Exception as exc:
                        logger.error(
                            f'Invalid response returned by function: {exc}', exc_info=True)
                        failed_messages_receipts = []
                    for failed_message in failed_messages_receipts:
                        if failed_message in request_messages_receipts:
                            request_messages_receipts.remove(failed_message)
                # Push receipts of successfully processed messages to message_receipts_queue
                for receipt_id in request_messages_receipts:
                    message_receipts_queue.put(receipt_id)
    except Exception as exc:
        logger.error(
            f"An exception occurred during execution of function callback: {exc}", exc_info=True)


def function_invoke(payload, connect_timeout=10, read_timeout=330):
    global OCI_FUNCTION, messages_queue
    try:
        logger.debug(
            f'Function invoke request received with arguments: payload={payload}')
        functions_client = FunctionsInvokeClient(config=OCI_FUNCTION.config, signer=OCI_FUNCTION.signer,
                                                 service_endpoint=OCI_FUNCTION.function_endpoint, timeout=(connect_timeout, read_timeout))
        start_execution_timestamp = timestamp()
        invoke_function_response = functions_client.invoke_function(
            function_id=OCI_FUNCTION.function_ocid,
            invoke_function_body=payload,
            fn_intent="cloudevent",
            fn_invoke_type="sync",
            retry_strategy=DEFAULT_RETRY_STRATEGY)
        logger.info(
            f"Function invoke response: {invoke_function_response.data.status_code} | {invoke_function_response.data.text}")
        return invoke_function_response, start_execution_timestamp
    except Exception as exc:
        logger.error(
            f'An exception occurred during function invocation: {exc}', exc_info=True)
        # Return to message queue unsuccessfully processed messages in case of function invocation exception
        messages = json.loads(payload)
        for message in messages:
            messages_queue.put(message)
        raise exc


def function_invoke_daemon(function_invoke_th_pool, callback_function=None):
    global OCI_FUNCTION, FUNCTION_INVOKE_BODY_BATCH_SIZE, messages_queue, message_delete_th_pool_shutdown_done, queue_long_polling_th_pool_shutdown_done, queue_fast_polling_th_pool_shutdown_done, function_invoke_th_pool_shutdown_done, shutdown
    while True:
        if not messages_queue.empty():
            logger.info(
                f'There are {messages_queue.qsize()} messages in the function invocation queue.')
            # Attempt to pack FUNCTION_INVOKE_BODY_BATCH_SIZE messages in function invocation body
            current_size = 0
            payload = []
            while current_size < FUNCTION_INVOKE_BODY_BATCH_SIZE:
                try:
                    message = messages_queue.get_nowait()
                    payload.append(message)
                    current_size += 1
                except queue.Empty:
                    break
                except Exception as exc:
                    logger.error(
                        f'An exception occurred during extraction of message from the message invocation queue: {exc}', exc_info=True)

            future_function_invoke = function_invoke_th_pool.submit(function_invoke, json.dumps(
                payload), OCI_FUNCTION.timeout_connect, OCI_FUNCTION.timeout_read)
            future_function_invoke.add_done_callback(function_invoke_callback)
        else:
            sleep(1)
            if shutdown.is_set() and queue_long_polling_th_pool_shutdown_done.is_set() and queue_fast_polling_th_pool_shutdown_done.is_set():
                logger.info('Exiting function_invoke_daemon_thread ... ')
                function_invoke_th_pool.shutdown(wait=True)
                function_invoke_th_pool_shutdown_done.set()
                return


def fast_polling_request_rate():
    # Use fibonacci series up to MAX_FUTURES value to determine rate in which new fast message polling futures are created
    a, b = 0, 1
    while True:
        if b < MAX_FUTURES:
            a, b = b, a + b
            yield a
        else:
            a = MAX_FUTURES
            yield MAX_FUTURES


def queue_fast_polling_callback(future):
    global execute_fast_polling, messages_queue
    try:
        exc = future.exception()
        if exc is not None:
            logger.error(
                f'An exception occured during execution of fast polling call: {exc}', exc_info=True)
        else:
            data = future.result()
            if data != None:
                if data.status_code == 200:
                    logger.info(
                        f"Data received from queue get message call: {data.json()}")
                    try:
                        messages = data.json().get('messages')
                    except Exception as exc:
                        messages = None
                        logger.debug(
                            f'Get queue messages API Call payload: {data.json()}')
                        logger.error(
                            f'An exception occurred during attempt to get Queue API response "messages" attribute: {exc}', exc_info=True)
                    # If response time is lower than long polling interval, start using also short polling thread
                    if messages != None and len(messages) == 0:
                        logger.info(f'Clearing execute_fast_polling flag...')
                        execute_fast_polling.clear()
                    else:
                        try:
                            logger.debug(
                                f'Adding {len(messages)} to the message queue from queue_fast_polling_callback function')
                            for message in messages:
                                messages_queue.put(message)
                        except Exception as exc:
                            logger.error(
                                f"An error occurred when to push messages to messages queue {messages}: {exc}", exc_info=True)
                else:
                    logger.debug(
                        f"Queue API returned an unexpected response code when attempting to read data: {data.status_code}, {data.json()}")
    except _base.CancelledError as exc:
        pass
    except Exception as exc:
        logger.error(
            f'An exception occurred during execution of fast polling callback function: {exc}', exc_info=True)


def queue_fast_polling_request():
    global OCI_QUEUE, fast_polling_rate_limit
    try:
        fast_polling_rate_limit.wait()
        session = requests.Session()
        if execute_fast_polling.is_set():
            query_params = f"?visibilityInSeconds={OCI_QUEUE.get_visibilityInSeconds}&timeoutInSeconds={OCI_QUEUE.get_fast_timeoutInSeconds}&limit={OCI_QUEUE.get_limit}"
            request_response = session.get(f'{OCI_QUEUE.dp_endpoint}/queues/{OCI_QUEUE.queue_ocid}/messages{query_params}',
                                           auth=OCI_QUEUE.signer, timeout=OCI_QUEUE.get_fast_timeoutInSeconds+2)
            return request_response
        else:
            return None
    except Exception as exc:
        logger.error(
            f'An error occurred during queue long polling request: {exc}', exc_info=True)
        raise exc


def queue_fast_polling(queue_fast_polling_th_pool, callback_function=None):
    global QUEUE_FAST_POLLING_MAX_FUTURES, QUEUE_FAST_POLLING_RAISE_INTERVAL, execute_fast_polling, fast_polling_request_multiply_factor, queue_long_polling_th_pool_shutdown_done, queue_fast_polling_th_pool_shutdown_done, shutdown
    fast_polling_futures = {}
    while True:
        if execute_fast_polling.is_set():
            if len(fast_polling_futures) < QUEUE_FAST_POLLING_MAX_FUTURES and messages_queue.qsize() < MAX_MESSAGES_IN_FUNCTION_INVOKE_QUEUE:
                try:
                    for _ in range(rate_generator.__next__()):
                        future = queue_fast_polling_th_pool.submit(
                            queue_fast_polling_request)
                        if callback_function != None:
                            future.add_done_callback(callback_function)
                        fast_polling_futures[str(uuid.uuid4())] = future
                except Exception as exc:
                    logger.error(
                        f'An unexpected error occurred during execution of queue_fast_polling: {exc}', exc_info=True)
            else:
                for key in tuple(fast_polling_futures.keys()):
                    if fast_polling_futures[key].done() or fast_polling_futures[key].cancelled():
                        fast_polling_futures.pop(key)
                logger.info(
                    f'There are {len(fast_polling_futures.keys())} futures pending ...')
            sleep(QUEUE_FAST_POLLING_RAISE_INTERVAL)
            logger.debug(
                f'{len(fast_polling_futures)} futures in dictionary... ')
        else:
            rate_generator = fast_polling_request_rate()
            try:
                if fast_polling_futures:
                    for key in tuple(fast_polling_futures.keys()):
                        if fast_polling_futures[key]._state == "PENDING":
                            fast_polling_futures[key].cancel()
                        if fast_polling_futures[key].done() or fast_polling_futures[key].cancelled():
                            fast_polling_futures.pop(key)
                    logger.info(
                        f'There are {len(fast_polling_futures.keys())} queue fast polling futures pending ...')
            except Exception as exc:
                logger.error(
                    f'An unexpected error occurred during cleanup of fast_polling_futures: {exc}', exc_info=True)
            finally:
                sleep(1)
        if shutdown.is_set() and queue_long_polling_th_pool_shutdown_done.is_set():
            logger.info('Exiting queue_fast_polling_thread ... ')
            try:
                if fast_polling_futures:
                    for key in tuple(fast_polling_futures.keys()):
                        if fast_polling_futures[key].done() or fast_polling_futures[key].cancelled():
                            fast_polling_futures.pop(key)
                    logger.debug(
                        f'There are {len(fast_polling_futures.keys())} queue fast polling futures pending ...')
            except Exception as exc:
                logger.error(
                    f'An unexpected error occurred during cleanup of fast_polling_futures: {exc}', exc_info=True)
            queue_fast_polling_th_pool.shutdown(wait=True)
            queue_fast_polling_th_pool_shutdown_done.set()
            return


def queue_long_polling_callback(future):
    global OCI_QUEUE, execute_fast_polling, messages_queue
    try:
        exc = future.exception()
        if exc is not None:
            logger.error(
                f'An exception occurred during execution of long polling call: {exc}', exc_info=True)
            queue_long_polling_failures.put(f"{exc}")
        else:
            data = future.result()
            if data.status_code == 200:
                # clear long polling failures
                while not queue_long_polling_failures.empty():
                    queue_long_polling_failures.get_nowait()
                logger.debug(
                    f"Data received from queue get message call: {data.json()}")
                try:
                    messages = data.json().get('messages')
                except Exception as exc:
                    messages = []
                    logger.debug(
                        f'Get queue messages API Call payload: {data.json()}')
                    logger.error(
                        f'An exception occurred during attempt to get Queue API response "messages" attribute: {exc}', exc_info=True)
                # If response time is lower than long polling interval, start using also short polling thread
                if len(messages) == OCI_QUEUE.get_limit and data.elapsed.seconds < OCI_QUEUE.get_timeoutInSeconds:
                    logger.info(
                        f'Received {len(messages)} in {data.elapsed.seconds} seconds. Enabling fast message')
                    execute_fast_polling.set()
                try:
                    if messages:
                        logger.debug(
                            f'Adding {len(messages)} to the message queue from queue_long_polling_callback function')
                        for message in messages:
                            messages_queue.put(message)
                    else:
                        execute_fast_polling.clear()
                except Exception as exc:
                    logger.error(
                        f"An error occurred when to push messages to messages queue {messages}: {exc}", exc_info=True)
            else:
                logger.debug(
                    f"Queue API returned an unexpected response code when attempting to read data: {data.status_code}, {data.json()}")
    except Exception as exc:
        logger.error(
            f'An exception occurred during execution of regular long polling callback function: {exc}', exc_info=True)


def queue_long_polling_request():
    global OCI_QUEUE
    try:
        session = requests.Session()
        query_params = f"?visibilityInSeconds={OCI_QUEUE.get_visibilityInSeconds}&timeoutInSeconds={OCI_QUEUE.get_timeoutInSeconds}&limit={OCI_QUEUE.get_limit}"
        request_response = session.get(f'{OCI_QUEUE.dp_endpoint}/queues/{OCI_QUEUE.queue_ocid}/messages{query_params}',
                                       auth=OCI_QUEUE.signer, timeout=OCI_QUEUE.get_timeoutInSeconds + 3)
        return request_response
    except Exception as exc:
        logger.error(
            f'An error occurred during queue long polling request: {exc}', exc_info=True)
        raise exc


def queue_long_polling(queue_long_polling_th_pool, callback_function=None):
    global OCI_QUEUE, queue_long_polling_th_pool_shutdown_done, shutdown
    while True:
        try:
            if shutdown.is_set():
                logger.info('Exiting queue_long_polling_thread ... ')
                queue_long_polling_th_pool.shutdown(wait=True)
                queue_long_polling_th_pool_shutdown_done.set()
                return
            future = queue_long_polling_th_pool.submit(
                queue_long_polling_request)
            if callback_function != None:
                future.add_done_callback(callback_function)
        except Exception as exc:
            logger.error(
                f'An unexpected error occurred during execution of queue_long_polling: {exc}', exc_info=True)
        finally:
            logger.debug(
                f'Sleeping for {OCI_QUEUE.get_timeoutInSeconds} seconds until next regular queue long message polling request.')
            sleep(OCI_QUEUE.get_timeoutInSeconds)


def create_health_file(path):
    try:
        with open(path, "w"):
            pass
        logger.debug(
            f'File to indicate application is healthy was created at: {path}')
    except Exception as exc:
        logger.error(
            f'An unexpected error occurred during creation of health file {path}: {exc}', exc_info=True)


def delete_health_file(path):
    if os.path.exists(path):
        try:
            os.remove(path)
            logger.debug(
                f'File to indicate application is healthy was removed.')
        except Exception as exc:
            logger.error(
                f'An unexpected error occurred during deletion of health file {path}: {exc}', exc_info=True)
    else:
        logger.info(
            f"File to indicate application is healthy doesn't exist at {path}")


if __name__ == "__main__":
    # Setup authentication to OCI services begin
    # config = from_file('./config', "DEFAULT")
    config = {"region": FUNCTION_OCI_REGION}
    # signer = Signer(
    #     tenancy=config['tenancy'],
    #     user=config['user'],
    #     fingerprint=config['fingerprint'],
    #     private_key_file_location=config['key_file'],
    #     pass_phrase=config['pass_phrase']
    # )
    signer = InstancePrincipalsSecurityTokenSigner()
    ###############################################

    # Setup OCI_FUNCTION variable begin
    try:
        OCI_FUNCTION = None
        functions_client = FunctionsManagementClient(
            config=config, signer=signer)
        get_function_response = functions_client.get_function(
            function_id=FUNCTION_OCID)

        FUNCTION_ENDPOINT = get_function_response.data.invoke_endpoint
        OCI_FUNCTION = Function(
            FUNCTION_OCID, FUNCTION_ENDPOINT, signer, config, 10, 330)

    except Exception as exc:
        logger.error(
            f"Could not determine function endpoint: {exc}", exc_info=True)
        sys.exit(1)
    ###############################################

    # Setup OCI_QUEUE variable begin
    OCI_QUEUE = Queue(QUEUE_OCI_REGION, QUEUE_OCID, signer)
    ###############################################

    # Thread Pool setup
    # Thread pool used for regular calls to queue to get messages
    queue_long_polling_th_pool = ThreadPoolExecutor(max_workers=3)

    # Thread pool used for bursty, fast calls to queue to get messages
    queue_fast_polling_th_pool = ThreadPoolExecutor(max_workers=200)

    # Thread pool used for function invocation
    function_invoke_th_pool = ThreadPoolExecutor(max_workers=100)

    # Thread pool used for calls to remove successfully processed messages from the queue
    message_delete_th_pool = ThreadPoolExecutor(max_workers=200)

    # Setup events which control program flow and safe shutdown process
    execute_fast_polling = Event()
    shutdown = Event()
    queue_long_polling_th_pool_shutdown_done = Event()
    queue_fast_polling_th_pool_shutdown_done = Event()
    function_invoke_th_pool_shutdown_done = Event()
    message_delete_th_pool_shutdown_done = Event()

    def handler_stop_signals(signum, frame):
        global shutdown
        shutdown.set()

    def safe_shutdown():
        global shutdown
        shutdown.set()
        while True:
            if queue_long_polling_th_pool_shutdown_done.is_set() and \
                    queue_fast_polling_th_pool_shutdown_done.is_set() and \
                    function_invoke_th_pool_shutdown_done.is_set() and \
                    message_delete_th_pool_shutdown_done.is_set():
                logger.info('Exiting...')
                os._exit(1)
            else:
                logger.info('Waiting for threadpools to shutdown...')
                sleep(1)

    # Setup handlers for SIGINT and SIGTERM
    signal.signal(signal.SIGINT, handler_stop_signals)
    signal.signal(signal.SIGTERM, handler_stop_signals)

    # Start daemon thread used for regular get messages requests
    try:
        queue_long_polling_thread = Thread(name="queue_long_polling_thread",
                                           target=queue_long_polling,
                                           args=(queue_long_polling_th_pool,),
                                           kwargs={
                                               "callback_function": queue_long_polling_callback}
                                           )

        queue_long_polling_thread.daemon = True
        queue_long_polling_thread.start()
        logger.info(f'Queue regular long polling thread started.')
    except Exception as exc:
        logger.error(
            f'An unexpected error occurred during setup of queue long polling thread: {exc}', exc_info=True)

    # Start daemon thread used for bursty fast get messages requests
    try:
        queue_fast_polling_thread = Thread(name="queue_fast_polling_thread",
                                           target=queue_fast_polling,
                                           args=(queue_fast_polling_th_pool,),
                                           kwargs={
                                               "callback_function": queue_fast_polling_callback}
                                           )
        queue_fast_polling_thread.daemon = True
        queue_fast_polling_thread.start()
        logger.info(f'Queue fast polling thread started.')
    except Exception as e:
        logger.error(
            f'An unexpected error occurred during setup of queue fast polling thread: {exc}', exc_info=True)

    # Start daemon thread used for function invocation
    try:
        function_invoke_daemon_thread = Thread(name="function_invoke_daemon_thread",
                                               target=function_invoke_daemon,
                                               args=(function_invoke_th_pool,),
                                               kwargs={
                                                   "callback_function": function_invoke_callback}
                                               )

        function_invoke_daemon_thread.daemon = True
        function_invoke_daemon_thread.start()
        logger.info(f'Function invoke daemon thread started.')
    except Exception as e:
        logger.error(
            f'An unexpected error occurred during setup of function invoke daemon thread: {exc}', exc_info=True)

    # Start daemon thread used for deletion of successfully processed messages from the queue
    try:
        message_deletion_daemon_thread = Thread(name="message_deletion_daemon_thread",
                                                target=message_delete_daemon,
                                                args=(message_delete_th_pool,),
                                                kwargs={
                                                    "callback_function": delete_message_from_queue_callback}
                                                )

        message_deletion_daemon_thread.daemon = True
        message_deletion_daemon_thread.start()
        logger.info(f'Message delete daemon thread started.')
    except Exception as exc:
        logger.error(
            f'An unexpected error occurred during setup of message delete daemon thread: {exc}', exc_info=True)

    # Assume application health is good
    create_health_file(STATUS_FILE_PATH)

    logging.info(f'Queue monitor running...')
    # preventing daemon threads from exiting
    while True:
        try:
            if shutdown.is_set():
                safe_shutdown()
            if queue_long_polling_failures.qsize() > MAX_consecutive_queue_long_polling_failures or \
                    function_invocation_failures.qsize() > MAX_consecutive_function_invocation_failures:
                shutdown.set()
                delete_health_file(STATUS_FILE_PATH)
            sleep(2)
        except KeyboardInterrupt:
            logger.info('CTRL+C pressed...')
            safe_shutdown()
