package com.demo.consumer;

import java.util.ArrayList;
import java.util.List;

import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.model.DeleteMessagesDetails;
import com.oracle.bmc.queue.model.DeleteMessagesDetailsEntry;
import com.oracle.bmc.queue.model.GetMessage;
import com.oracle.bmc.queue.requests.DeleteMessagesRequest;
import com.oracle.bmc.queue.requests.GetMessagesRequest;
import com.oracle.bmc.queue.responses.DeleteMessagesResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueueConsumer {

	static QueueClient dpClient;

	public static void main(String[] args) {

		dpClient = new QueueClient(GetProvider.getProvider(System.getenv("queue_profile")));
		String QUEUE_DP_ENDPOINT=System.getenv().getOrDefault("DP_ENDPOINT", Environment._DP_ENDPOINT);
		log.info("QUEUE_DP_ENDPOINT "+QUEUE_DP_ENDPOINT);
		log.info("QUEUE_ID "+System.getenv().getOrDefault("QUEUE_ID", Environment._QUEUE_ID));
		dpClient.setEndpoint(QUEUE_DP_ENDPOINT);
		
		// use timeOut as 0 for short polling and any other value upto 30 for long
		// polling
		int totalMessageCount = 0;
		while (true) {
			GetMessagesRequest getMessagesRequest = prepareGetMessageRequest();
			long startTime = System.currentTimeMillis();
			List<GetMessage> messages = getMessage(getMessagesRequest);
			long messageReceiveTime = System.currentTimeMillis() - startTime;
			long processStartTime = System.currentTimeMillis();
			List<DeleteMessagesDetailsEntry> entries = new ArrayList<>();
			if (messages != null && !messages.isEmpty()) {

				for (GetMessage msg : messages) {
					processMessage(msg);
					entries.add(DeleteMessagesDetailsEntry.builder().receipt(msg.getReceipt()).build());
					totalMessageCount++;

				}
				batchDeleteMessages(entries);
				long processEstimatedTime = System.currentTimeMillis() - processStartTime;
				log.info("Total Message Processed " + totalMessageCount + " messageReceiveTime " + messageReceiveTime
						+ " processEstimatedTime " + processEstimatedTime);
			}

		}

	}

	/**
	 * waits for certain milliseconds to replicate process message
	 * 
	 * @param msg
	 */
	private static void processMessage(GetMessage msg) {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * prepare a GetMessageRequest
	 * 
	 * @return
	 */
	private static GetMessagesRequest prepareGetMessageRequest() {
		int timeOut = 20;
		GetMessagesRequest getMessagesRequest = GetMessagesRequest.builder()
				.queueId(System.getenv().getOrDefault("QUEUE_ID", Environment._QUEUE_ID)).timeoutInSeconds(timeOut)
				.limit(20).build();

		return getMessagesRequest;
	}

	/**
	 * Get the Message from queues
	 * 
	 * @param getMessagesRequest
	 * @return
	 */
	private static List<GetMessage> getMessage(GetMessagesRequest getMessagesRequest) {

		List<GetMessage> messages = dpClient.getMessages(getMessagesRequest).getGetMessages().getMessages();
		return messages;
	}

	

	private static void batchDeleteMessages(List<DeleteMessagesDetailsEntry> entries) {

		log.info("Batch deleting the messages");
		DeleteMessagesResponse batchResponse = dpClient
				.deleteMessages(DeleteMessagesRequest.builder().queueId(System.getenv().getOrDefault("QUEUE_ID", Environment._QUEUE_ID))
						.deleteMessagesDetails(DeleteMessagesDetails.builder().entries(entries).build()).build());
		log.info("batch delete response " + batchResponse.getDeleteMessagesResult().getServerFailures());

	}

}