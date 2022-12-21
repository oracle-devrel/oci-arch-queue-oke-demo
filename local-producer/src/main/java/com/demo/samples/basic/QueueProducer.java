package com.demo.samples.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.model.PutMessagesDetails;
import com.oracle.bmc.queue.model.PutMessagesDetailsEntry;
import com.oracle.bmc.queue.requests.PutMessagesRequest;
import com.oracle.bmc.queue.responses.PutMessagesResponse;

public class QueueProducer {

	static QueueClient dpClient;

	public static void main(String[] args) {

		dpClient = new QueueClient(GetProvider.getProvider(System.getenv("queue_profile")));
		dpClient.setEndpoint(Environment._DP_ENDPOINT);
		List<PutMessagesDetailsEntry> messages = new ArrayList<>();
		int totalMessageCount = 0;
		while (true) {

			messages.clear();
			PutMessagesRequest putMessageRequest = prepareMessage(messages);

			totalMessageCount = totalMessageCount + 20;
			long startTime = System.currentTimeMillis();

			sendMessage(putMessageRequest);
			long estimatedTime = System.currentTimeMillis() - startTime;
			System.out.println("Send Latency " + estimatedTime + " totalMessageCount: " + totalMessageCount);

		}

	}

	/**
	 * prepare PutMessageReuqest
	 * 
	 * @param messages
	 * @return
	 */
	private static PutMessagesRequest prepareMessage(List<PutMessagesDetailsEntry> messages) {

		for (int i = 0; i < 20; i++) {
			messages.add(PutMessagesDetailsEntry.builder()
					.content("New messages from Java Client Producer " + UUID.randomUUID().toString()).build());

		}
//		messages.add(PutMessagesDetailsEntry.builder()
//				.content("New messages from Java Client Producer " + UUID.randomUUID().toString()).build());
		PutMessagesRequest putMessageRequest = PutMessagesRequest.builder().queueId(Environment._QUEUE_ID)
				.putMessagesDetails(PutMessagesDetails.builder().messages(messages).build()).build();
		return putMessageRequest;

	}

	/**
	 * send message to queue using queue client
	 * 
	 * @param request
	 */
	private static void sendMessage(PutMessagesRequest request) {

		PutMessagesResponse response = dpClient.putMessages(request);

	}

}
