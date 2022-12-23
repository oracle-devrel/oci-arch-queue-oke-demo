# oci-arch-queue-oke-demo (consumer service)

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_oci-arch-queue-oke-demo)](https://sonarcloud.io/dashboard?id=oracle-devrel_oci-arch-queue-oke-demo)

## Introduction

The following document describes how to create the consumer microservice and deploy it ready to use.

### Prerequisites

- [Java JDK 8 or later](https://www.oracle.com/java/technologies/downloads/)
- [Oracle OCI SDK](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm)
- [Maven](https://maven.apache.org/download.cgi)
- [KubeCTL](https://kubernetes.io/docs/tasks/tools/)
- [Docker](https://docs.docker.com/get-docker/)
- [OKE Cluster](https://docs.oracle.com/en-us/iaas/Content/ContEng/home.htm), please check here to setup OKE Cluster [OKE Cluster Setup](https://docs.oracle.com/en-us/iaas/Content/ContEng/Tasks/contengcreatingclusterusingoke_topic-Using_the_Console_to_create_a_Quick_Cluster_with_Default_Settings.htm)
- [OCI Container Registry](https://docs.oracle.com/en-us/iaas/Content/Registry/home.htm) , please check here about setting up OCI Container registry. [OCIR Setup](https://docs.oracle.com/en-us/iaas/Content/Registry/Tasks/registrycreatingarepository.htm#Creating_a_Repository)



### <u>Making the queue identifiable and accessible</u>

Please ensure you are providing Queue OCID and Queue Data plane url in *queue-oke.yaml* , these are defined as environment variable names *QUEUE_ID* and *DP_ENDPOINT*.

In order to test locally , Within the code is a class called *Environment* (*src/main/java/com/demo/consumer/Environment.java*) which declares several constants that capture the OCI Queue OCID, the URL for the OCI Data Plane endpoint, and the attributes necessary for authenticating and authorization to use the service. This can be used to test in standalone fashion queue service as well as from your IDE.(e.g., writing some queue specific test).

These values need to have their defaults replaced with the appropriate values established during the OCI Queue setup.

If the Queue is not configured in the Phoenix region, then the region part of the name(for the data plane URL) needs to be modified to reflect the region being used.

### Dynamic Group and Policies


We are going to use an instance principle while deploying to the OKE cluster, so we have to create a dynamic group , e.g. dyanmic group name is *queue_dg*

`ALL {instance.compartment.id='\<OKE Cluster Compartment id\>'}`

please also need the following policies:

`allow dynamic-group queue_dg to use queues in compartment <queue_parent_compartment>`

`allow dynamic-group queue_dg to use fn-invocation in compartment <function_parent_compartment>`

Don't forget to substitute <values> with your real compartment names etc.

### <u>Packaging and deploying the consumer</u>

#### <u>Creating the JAR file</u>

This is maven based project we can use standard maven commands like this:

`mvn clean package`

This will produce a jar file in the *target* folder.
#### Creating the container image

The next step is to create a container image which can be done in the following way, you have to be in the root directory of the consumer code.

`docker build -t queueoke .`

This will create a container image in your local filesystem.

#### Pushing the image to the repository

The next step is to push it to the remote docker repository, following command will do that 

`docker push \<region-name\>.ocir.io/\<tenancy name\>/queueoke:latest`

Please check the following link for creating and configuring OCI container registry.
[OCI Container Repo](https://docs.oracle.com/en-us/iaas/Content/Registry/Concepts/registryoverview.htm)

#### Deploying KEDA

KEDA stands for Kubernetes Event-driven Autoscaling , please check the following link to learn more about KEDA and how to deploy it in the Kubernetes cluster.

[KEDA](https://keda.sh/)

#### Deploying the Consumer

To deploy your consumer image in the OKE cluster, you will have to execute the following command . ( More Details here [Create a Secret](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-secret-by-providing-credentials-on-the-command-line))

`kubectl create secret docker-registry queueoke-secret --docker-server='\<repo name\>' --docker-username='\<user name\>' --docker-password='\<password\>' --docker-email=\<email id\>`

The secret will be used by OKE to pull the image from the container registry.

The next step is to create a Scaled object which can be created using the _so-object.yaml_ file . We can use the following command to do this.

`kubectl apply -f so-object.yaml`

As we have created the Secret and Scaled object, nowe can nowreate our deployment, which can be done using the following command:

`kubectl apply -f queue-oke.yaml`

Please ensure you have provided the value of *DP_ENDPOINT* and *QUEUE_ID* in *queue-oke.yaml* before running the commandabove. This will ensure our consumer is up and running.
#### <u>Observing the Consumer running</u>

In order to check if all resources are created, you can use the following command (assuming it is created in the default namespace)

`kubectl get all -n default`

However, we will not be able to see any pods as there are not any visible messages in the queue, in order to view consumer pods, you can send test messages from the queue console, or you can run the producer component as well, please check the link below for sending a test message from queue console (Check Using the Console section on the given hyperlink).
[Send Test Message to Queue](https://docs.oracle.com/en-us/iaas/Content/queue/publish-messages.htm#example-manage)

## Notes/Issues

None

## URLs

* N/A

## Contributing

This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open-source community.

## License

Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 
