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


import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.requests.GetStatsRequest;
import com.oracle.bmc.queue.responses.GetStatsResponse;

public class GetStats {

	static QueueClient dpClient;

	public static void main(String[] args) {

		dpClient = new QueueClient(GetProvider.getProvider(System.getenv("queue_profile")));
		dpClient.setEndpoint(Environment._DP_ENDPOINT);
		while (true) {
			getStats();
			sleepForAWhile();
		}

	}

	/**just sleep for a few milliseconds
	 * 
	 */
	private static void sleepForAWhile() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/** Call Get stat API of OQS and get various stats
	 * 
	 */
	private static void getStats() {
		GetStatsRequest getStatsRequest = GetStatsRequest.builder().queueId(Environment._QUEUE_ID).build();
		GetStatsResponse getStatsResponse = dpClient.getStats(getStatsRequest);
		long visibleMessages = getStatsResponse.getQueueStats().getQueue().getVisibleMessages();
		long inFlightMessages = getStatsResponse.getQueueStats().getQueue().getInFlightMessages();
		long dlqvisibleMessages = getStatsResponse.getQueueStats().getDlq().getVisibleMessages();
		long dlqinFlightMessages = getStatsResponse.getQueueStats().getDlq().getInFlightMessages();
		System.out.println("visibleMessages " + visibleMessages + " inFlightMessages " + inFlightMessages
				+ " dlqvisibleMessages " + dlqvisibleMessages + " dlqinFlightMessages " + dlqinFlightMessages);
	}
	
	public static long getVisibleMessage()
	{
		
		dpClient = new QueueClient(GetProvider.getProvider(System.getenv("queue_profile")));
		dpClient.setEndpoint(Environment._DP_ENDPOINT);
		GetStatsRequest getStatsRequest = GetStatsRequest.builder().queueId(Environment._QUEUE_ID).build();
		return dpClient.getStats(getStatsRequest).getQueueStats().getQueue().getVisibleMessages();
	}

}
