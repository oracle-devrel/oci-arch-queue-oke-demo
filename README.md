# oci-arch-queue-oke-demo

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_oci-arch-queue-oke-demo)](https://sonarcloud.io/dashboard?id=oracle-devrel_oci-arch-queue-oke-demo)

## Introduction
This repository contains the code and instructions to create and run an OCI Queue demo that runs a consumer that can autoscale on Oracle Kubernetes Engine (OKE), a demo provider which you can run anywhere, and an OCI Function that controls the autoscaling with KEDA

An overview video and demo video is available [here](https://youtu.be/4RMA_EMjyfo).

![](images/demo-architecture.png)

This document walks through the build and deployment necessary to configure the demo. Each part of the demo has a readme file explaining how to build and deploy the components in the demo. Which can be found at:

- [Consumer](./oke-consumer/readme.md)
- [Producer](./local-producer/readme.md)
- [Queue Depth function](./queue-length-function/readme.md)

The core of this document focuses on creating the environment and then running the demo.

## Getting Started

This section walks through the process of setting up the necessary environment ready to be used.  To implement this, we will take advantage of a preexisting One-click deployment which will use the Resource Manager to run a Terraform configuration file. Then we'll add to that using the OCI Console to add the remaining elements, such as the Queue, manually so that you get to see the simplicity of configuration.  While working within the OCI Console, we will retrieve a number of details that will need to be added to the configuration of the different resources.  Such as the Queue OCID.

### Prerequisites

- OCI Cloud account
- OCI Compartment with privileges to manage:
  - OCI Queue
  - Kubernetes Engine
  - Network
  - Internet gateway
  - Container Registry
  - Resource Manager

### Build

##### Foundation

1. Within OCI we need to have a compartment to work with. The guidance for creating a compartment can be found [here](https://docs.oracle.com/en/cloud/paas/integration-cloud/oracle-integration-oci/creating-oci-compartment.html).
2. Get the user token, fingerprint, and related attributes needed by the OCI API to enable the application to communicate using the SDK. The details on how to do this can be found [here](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/apisigningkey.htm). You may wish to use different users for the provider and consumer utilities.
3. Note which region we're going to deploy to. If it is not Phoenix, then some additional changes will be needed.
4. Follow the instructions for the architecture [terraform-oci-arch-microservices-oke](https://github.com/oracle-devrel/terraform-oci-arch-microservice-oke) (ideally using the 1-click deploy button). Once this has been completed, the URL for the Repository (OCIR) and OKE will be needed.

##### Creating the Queue

1. Within the compartment, we need to create the queue and ensure that the applications know about it through its OCID. The steps for creating a queue can be followed in the OCI Queue documentation.
2. With the queue established, you may wish to try using the OCI Console's Queue UI to send and receive. The steps to do this can be found in the OCI Queue UI documentation.
3. The OCID for the Queue needs to be made available to our applications before they are deployed. Each of the subsidiary readme documents has a section called *setting the queue OCID,* which details how to set the Queue OCID.



#### Making the queue identifiable and accessible

Within the code is a class called *Environment* (*src/main/java/com/demo/samples/basic/Environment.java*) which declares several constants that capture the OCI Queue OCID, the URL for the OCI Data Plane endpoint, and the attributes necessary for authenticating and authorization to use the service.

These values need to have their defaults replaced with the appropriate values established during the OCI Queue setup.

If the Queue is not configured in the Phoenix region, then the region part of the name needs to be modified to reflect the region being used.

##### Deployment

With the infrastructure ready, the different components can be deployed and executed. Each of the component readme documents will describe the configuration and deployment steps.

##### Execution

With the services deployed and configured along with our [Consumer](./oke-consumer/readme.md) and [Queue Depth function](./queue-length-function/readme.md). Execution is simply a case of running one of the scripts provided by the [Producer](./local-producer/readme.md).

## Notes/Issues

***TODO:***

* ***extend the execution section above to describe how to observe the scaling and logs most simply.***
* ***Add links to any tools needed***

## URLs
* These will be unique to the deployment

## Contributing
This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open-source community.

## License
Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 