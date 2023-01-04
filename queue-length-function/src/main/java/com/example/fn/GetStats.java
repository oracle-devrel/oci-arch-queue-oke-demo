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
