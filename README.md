# oci-queue-fn-oke-demo

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_oci-queue-fn-oke-demo)](https://sonarcloud.io/dashboard?id=oracle-devrel_oci-queue-fn-oke-demo)

## Introduction

This is an example project that will enable the consumption of queue messages and will POST the messages to an OCI function provided by the user.

Resources created by this deployment are:
- OCI DevOps project with CI/CD pipelines for queue-fn-automation application deployment.
- OKE cluster (if required).
- OCIR repository for the queue-fn-automation container image.
- OKE deployment for the queue-fn-automation application.
- Policies to enable queue-fn-automation application interaction with queues and functions (if required).

## Getting Started

### Prerequisites

Please ensure required policies are configured:

- DevOps service required policies to containerize an application as part of the build stage, deliver a container image to OCIR and deploy resources on the OKE cluster. (policies can be created by the stack and the deploying user must have administrative privileges)
- OKE worker nodes are part of a dynamic group with the proper policies attached to allow the interaction of the application with queues and functions. (policies can be created by the stack and the deploying user must have administrative privileges)

#### Create OCI Dynamic Group

To interact with OCI resources: queues & functions, the application will authenticate as a resource principal.
If you are using an **existing OKE cluster** please make sure the required policies are configured.

**Create tag namespace**: authorization
**Create a tag key**: instance_principal

**Name**: queue_automation_dg
**match_rule**:
`All {instance.compartment.id='<kubernetes worker nodes compartment_ocid>',`
`tag.authorization.instance_principal.value='yes'}`

Add to Kubernetes worker nodes below the defined tag:
authorization.instance_principal = 'yes'

#### Create OCI policies

 **Name** : queue_automation_policies  

**Policies** : 

`allow dynamic-group queue_automation_dg to use queues in compartment <queue_parent_compartment><br/>allow dynamic-group queue_automation_dg to use fn-invocation in compartment <function_parent_compartment>` 

For explicit access is possible to target queue.id and function.id

**Policies** : 

`allow dynamic-group queue_automation_dg to use queues in compartment <queue_parent_compartment> where target.queue.id='<queue_OCID>'<br/>allow dynamic-group queue_automation_dg to use fn-invocation in compartment <function_parent_compartment> where target.function.id = '<function_OCID>'` 

### Automated deployment

Create a stack in ORM, load the project files, fill in all required values and click `Apply`.

[![Deploy to OCI](https://docs.oracle.com/en-us/iaas/Content/Resources/Images/deploy-to-oracle-cloud.svg)](https://cloud.oracle.com/resourcemanager/stacks/create?zipUrl=https://github.com/robo-cap/oci-arch-queue-oke-demo/archive/refs/tags/v1.0.zip)

### Manual deployment

If you intend to manually deploy the application to an existing OKE cluster please follow the below steps.

#### Build docker image

To build the container image you can refer to `/application` directory and run the below command:

`docker build -f Dockerfile -t <OCIR_container_image_url>` 

#### Push docker image to OCIR

https://docs.oracle.com/en-us/iaas/Content/Registry/Tasks/registrypushingimagesusingthedockercli.htm

`docker push <OCIR_container_image_url>`

#### OKE deployment 

Update the missing values in `application/queue-automation.yaml` file and run the below command:

`kubectl apply -f queue-automation.yaml`

## Notes/Issues
* None

## URLs
* Nothing at this time

## Contributing
This project is open source. Please submit your contributions by forking this repository and submitting a pull request! Oracle appreciates any contributions that are made by the open-source community.

## License
Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 
