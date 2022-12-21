package com.demo.consumer;

public class Environment {

	// Data Plane API's endpoints for PHX
	
	public static String _DP_ENDPOINT = "https://cell-1.queue.messaging.us-phoenix-1.oci.oraclecloud.com";

	// values below are required if you are using user private key 
	//not suggested to use it when running from OKE
	
	public static String _TENANT_ID = "<Your tenancy ocid>";
	public static String _USER_ID = "<user ocid>";
	public static String _FINGER_PRINT = "<key fingerprint>";
	public static String _PVT_KEY_FILE_PATH = "<absolute path of private key>";

	// Get the _QUEUE_ID after creating the Queue
	public static String _QUEUE_ID = "<Your Queue OCID>";
	

	
}

