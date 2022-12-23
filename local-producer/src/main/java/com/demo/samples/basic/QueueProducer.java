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
