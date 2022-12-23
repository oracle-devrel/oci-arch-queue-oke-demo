# oci-arch-queue-oke-demo (provider app)

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_oci-arch-queue-oke-demo)](https://sonarcloud.io/dashboard?id=oracle-devrel_oci-arch-queue-oke-demo)

## Introduction

The following document describes how to create the provider application in the demo. This application generates a pushes the content into the queue. It is designed to be run anywhere (local desktop or VM, for example).

## Getting Started

The folder includes two scripts (provided in Windows (.bat) and Linux bash (.sh) formats. As a recommended precaution, it is worth running the [dos2unix](https://docs.oracle.com/cd/E26502_01/html/E29030/dos2unix-1.html) and [unix2dos](https://docs.oracle.com/cd/E36784_01/html/E36870/unix2dos-1.html) scripts on these files just in case a version is pushed that hasn't been corrected.

- **getStats.[bat|sh]** - this script sets the environment variable to tell the application that the configuration file with the properties necessary is in the same directory as where you run the tool from.  It then uses Maven to create and execute the tool, which will then send to the console details of the test queue.
- **producer.[bat|sh]** - this script works in the same way as the getStats script by setting the environment variable and then using Maven to compile and execute the utility. The difference being this will then start generating and putting messages in the Queue.

As the Java utility needs to communicate with OCI, it requires the user details to authenticate the user, as mentioned in the overall [readme](../README.md) and explained in the [ SDK documentation](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/apisigningkey.htm). You may wish to use separate users for the provider and consumer.

### Prerequisites

- [Java JDK 8 or later](https://www.oracle.com/java/technologies/downloads/)
- [Oracle OCI SDK](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm)
- [Maven](https://maven.apache.org/download.cgi)
- User with the privileges to interact with the OCI Queue - we describe a simple solution to this in the parent [readme](../README.md).

Maven and Java are assumed to be visible using the PATH environment variable.

#### Making the queue identifiable and accessible

Within the code is a class called *Environment* (*src/main/java/com/demo/samples/basic/Environment.java*) which declares several constants that capture the OCI Queue OCID, the URL for the OCI Data Plane endpoint, and the attributes necessary for authenticating and authorization to use the service.

These values need to have their defaults replaced with the appropriate values established during the OCI Queue setup.

If the Queue is not configured in the Phoenix region, then the region part of the name needs to be modified to reflect the region being used.

### Dynamic Group and Policies
  

We are going to use instance principle so we have to create a dynamic group , e.g. dyanmic group name is *queue_dg*
>ALL {instance.compartment.id='<OKE Cluster Compartment id>'}
please use below policies 
> allow dynamic-group queue_dg to use queues in compartment <queue_parent_compartment>

> allow dynamic-group queue_dg to use fn-invocation in compartment <function_parent_compartment>

Above policies and dynamic group are needed when using instance principal , if you are planning to run it locally then you will have to generate API keys and ensure you have access to queue service using a group ,e.g. 
> Allow group \<user group name\> to manage queues in compartment \<compartment name of queue parent compartment\>

### Making the queue identifiable and accessible

Within the code is a class called *Environment* (*src/main/java/com/demo/samples/basic/Environment.java*) which declares several constants that capture the OCI Queue OCID, the URL for the OCI Data Plane endpoint, and the attributes necessary for authenticating and authorization to use the service.

These values need to have their defaults replaced with the appropriate values established during the OCI Queue setup.



## Notes/Issues

***<u>TODO</u>:***

* <u>**fix SDK in POM**</u>
* **<u>check/correct bat and sh file</u>** 

## URLs

* These will be unique to the deployment

## Contributing

This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open-source community.

## License

Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 
