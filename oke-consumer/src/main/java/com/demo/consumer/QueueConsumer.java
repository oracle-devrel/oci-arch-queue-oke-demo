/*Copyright (c) 2021 Oracle and/or its affiliates.
The Universal Permissive License (UPL), Version 1.0
Subject to the condition set forth below, permission is hereby granted to any
person obtaining a copy of this software, associated documentation and/or data
(collectively the "Software"), free of charge and under any and all copyright
rights in the Software, and any and all patent rights owned or freely
licensable by each licensor hereunder covering either (i) the unmodified
Software as contributed to or provided by such licensor, or (ii) the Larger
Works (as defined below), to deal in both
(a) the Software, and
(b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
one is included with the Software (each a "Larger Work" to which the Software
is contributed by such licensors),
without restriction, including without limitation the rights to copy, create
derivative works of, display, perform, and distribute the Software and make,
use, sell, offer for sale, import, export, have made, and have sold the
Software and the Larger Work(s), and to sublicense the foregoing rights on
either these or other terms.
This license is subject to the following condition:
The above copyright notice and either this complete permission notice or at
a minimum a reference to the UPL must be included in all copies or
substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
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