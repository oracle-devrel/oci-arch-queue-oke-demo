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
