# oci-arch-queue-oke-demo

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_oci-arch-queue-oke-demo)](https://sonarcloud.io/dashboard?id=oracle-devrel_oci-arch-queue-oke-demo)

## Introduction
This repository contains the code and instructions to create and run an OCI Queue demo that runs a consumer that can autoscale on Oracle Kubernetes Engine (OKE), a demo producer that you can run anywhere(Local Desktop/Virtual Machine), and an OCI Function that provides queue depth for the autoscaling of queue consumers with KEDA.

An overview video and demo video is available [here](https://youtu.be/4RMA_EMjyfo) and [here](https://www.youtube.com/watch?v=wC3h9LmKgGY).

![](images/demo-architecture.png)

This document walks through the build and deployment necessary to configure the demo. Each part of the demo has a readme file explaining how to build and deploy the components in the demo. Which can be found at:

- <a href="https://github.com/oracle-devrel/terraform-oci-arch-queue" target="_blank">Terraforn Repo</a> that can be used to setup the complete solution in your tenancy. As Producer can be executed from your local laptop/ VM instance etc. so it can be done manually after capturing output parameters for Queue OCID and DP endpoint from Resource manager output.
- [Producer](./local-producer/readme.md)


## Getting Started


### Prerequisites

- Permission to `manage` the following types of resources in your Oracle Cloud Infrastructure tenancy: `vcns`, `internet-gateways`, `route-tables`, `network-security-groups`, `subnets`, `OKE`, `functions` , `api gateway` and `Queue`.
- Quota to create all of these services  


### Deployment

- <a href="https://github.com/oracle-devrel/terraform-oci-arch-queue">Terraforn Repo</a> that can be used to setup the complete solution in your tenancy. As Producer can be executed from your local laptop/ VM instance etc. so it can be done manually after capturing output parameters for Queue OCID and DP endpoint from Resource manager output.
- [Producer](./local-producer/readme.md)

##### Execution

With the services deployed and configured along with our [Consumer](./oke-consumer/readme.md) and [Queue Depth function](./queue-length-function/readme.md). Execution is simply a case of running one of the scripts provided by the [Producer](./local-producer/readme.md).

#### Observing queue production and consumption

As observing how the different elements execute is unique to that element, the readme documentation for those elements describes how you can see them work. Please check the demo links [here](https://youtu.be/4RMA_EMjyfo) and [here](https://www.youtube.com/watch?v=wC3h9LmKgGY) to get more detail.

## Notes/Issues

None

## URLs
* These will be unique to the deployment

## Contributing
This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open-source community.

## License
Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 
