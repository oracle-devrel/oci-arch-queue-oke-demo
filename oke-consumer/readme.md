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

In order to test locally , Within the code is a class called *Environment* (*src/main/java/com/demo/consumer/Environment.java*) which declares several constants that capture the OCI Queue OCID, the URL for the OCI Data Plane endpoint, and the attributes necessary for authenticating and authorization to use the service. This can be used to test in standalone fashion queue service as well as from your IDE.(e.g. writing some queue specific test)

These values need to have their defaults replaced with the appropriate values established during the OCI Queue setup.

If the Queue is not configured in the Phoenix region, then the region part of the name(for the data plane URL) needs to be modified to reflect the region being used.

### Dynamic Group and Policies
  

We are going to use instance principle while deploying it on OKE cluster so we have to create a dynamic group , e.g. dyanmic group name is *queue_dg*
>ALL {instance.compartment.id='\<OKE Cluster Compartment id\>'}

please use below policies 
> allow dynamic-group queue_dg to use queues in compartment <queue_parent_compartment>

> allow dynamic-group queue_dg to use fn-invocation in compartment <function_parent_compartment>
### <u>Packaging and deploying the consumer</u>

#### <u>Creating the JAR file</u>

This is maven based project we can use standard maven command like below 
> *mvn clean package*

This will produce a jar file in *target* folder.
#### Creating the container image

The next step is to create  container image which can be done in following way , you have to be in root directory of the consumer code.
> *docker build -t queueoke .*

This will create container image in your local .

#### Pushing the image to the repository

The next step is to push it to remote docker repo , following is the command for that 
> *docker push \<region-name\>.ocir.io/\<tenancy name\>/queueoke:latest*

Please check following link for creating and configuring OCI container registry.
[OCI Container Repo](https://docs.oracle.com/en-us/iaas/Content/Registry/Concepts/registryoverview.htm)

#### Deploying KEDA

KEDA stands for Kubernetes Event-driven Autoscaling , please check following link to know more about KEDA and how to deploy it in Kubernetes cluster.

[KEDA](https://keda.sh/)

#### Deploying the Consumer

To deploy your consumer image in OKE cluster , you will have to execute following command . ( More Details here [Create a Secret](https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/#create-a-secret-by-providing-credentials-on-the-command-line))
> *kubectl create secret docker-registry queueoke-secret --docker-server='\<repo name\>' --docker-username='\<user name\>' --docker-password='\<password\>' --docker-email=\<email id\>*

The secret will be used by OKE to pull image from container registry.

Next step is to create Scaled object which can be created using _so-object.yaml_ file . We can use following command .
>  _kubectl apply -f so-object.yaml_

As we have created Secret and Scaled object now we have to create our deployment which can be created using following command below
> _kubectl apply -f queue-oke.yaml_

Please ensure you have provided value of *DP_ENDPOINT* and *QUEUE_ID* in *queue-oke.yaml* before running above command. This will ensure our consumer is up and running.
#### <u>Observing the Consumer running</u>

In order to check if all resources are created , you can use following command (assuming it is created in default namespace)
> *k get all -n default*

However we will not be able to see any pods as there are not any visible messages in the queue , in order to view consumer pods you can send test messages from queue console or you can run producer component as well, please check link below for sending test message from queue console(Check Using the Console section on given hyperlink).
[Send Test Message to Queue](https://docs.oracle.com/en-us/iaas/Content/queue/publish-messages.htm#example-manage)

## Notes/Issues

**<u>*TODO:*</u>**

* <u>**fix SDK in POM**</u>
* **<u>check/correct bat and sh file</u>** 
* **KubeCTL hyperlinked link**
* **<u>Add to introduction brief explination of</u>**
  * **<u> how the container is setup e.g. how app starts</u>**
  * ***What KEDA is - 1 or 2 sentences***
* <u>**Complete the subsections for *Packaging and deploying the consumer* and *Observing the Consumer running* above includes the command line steps**</u>

## URLs

* N/A

## Contributing

This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open-source community.

## License

Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 
