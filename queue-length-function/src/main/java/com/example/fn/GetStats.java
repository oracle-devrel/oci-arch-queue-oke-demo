package com.example.fn;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.queue.QueueClient;
import com.oracle.bmc.queue.model.GetMessage;
import com.oracle.bmc.queue.model.QueueStats;
import com.oracle.bmc.queue.requests.GetStatsRequest;
import com.oracle.bmc.queue.responses.GetStatsResponse;

public class GetStats {

	static QueueClient dpClient;
	static String _DP_ENDPOINT = System.getenv("DP_ENDPOINT");// "https://cell-1.queue.messaging.us-phoenix-1.oci.oraclecloud.com";

	public static String getStats() {
		final ResourcePrincipalAuthenticationDetailsProvider provider = ResourcePrincipalAuthenticationDetailsProvider
				.builder().build();
		System.err.println("in HandleMessages batchDeleteMessages()");
		dpClient = new QueueClient(provider);
		dpClient.setEndpoint(_DP_ENDPOINT);
		GetStatsRequest getStatsRequest = GetStatsRequest.builder().queueId(System.getenv("QUEUE_ID")).build();
		GetStatsResponse getStatsResponse = dpClient.getStats(getStatsRequest);
		return toJson(getStatsResponse.getQueueStats());

	}
	
	private static String toJson(Object obj) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type listType = new TypeToken<QueueStats>() {
		}.getType();
		String jsonOutput = gson.toJson(obj, listType);
		System.err.println("jsonOutput "+jsonOutput);
		return jsonOutput;

	}

}
