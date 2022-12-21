# oci-arch-queue-oke-demo (queue length Function)

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_oci-arch-queue-oke-demo)](https://sonarcloud.io/dashboard?id=oracle-devrel_oci-arch-queue-oke-demo)

## Introduction

The following document describes creating and deploying the serverless Function that reports the queue depth. [KEDA](https://keda.sh/) then utilizes this to control the auto-scaling.

## Getting Started

MISSING

### Prerequisites

- [Java JDK 8 or later](https://www.oracle.com/java/technologies/downloads/)
- [Oracle OCI SDK](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm)
- [Maven](https://maven.apache.org/download.cgi)
- [OCI CLI](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/cliconcepts.htm)
- [FN Development environment](https://docs.oracle.com/en-us/iaas/Content/Functions/Tasks/functionsconfiguringclient.htm) (includes Docker, Fn tools, etc.)  with [quickstart guide](https://docs.oracle.com/en-us/iaas/Content/Functions/Tasks/functionsquickstartlocalhost.htm).



#### Building the Function

TBD

#### Deploying the Function

TBD

#### Making the queue identifiable and accessible

As the Function runs within the OCI environment, we don't need to provide authentication details. However, the data plane URL is still required along with the OCID of the queue. The function code takes these two values from environment variables called:

- DP_ENDPOINT
- QUEUE_ID

These values can be set once the Function is deployed with the following command:

```
fn config function <app-name> <function-name> DP_ENDPOINT <Your deployment URL for the Queue>
fn config function <app-name> <function-name> QUEUE_ID <Your Queue's OCID>
```

You can read more about this in the Functions documentation about [configuring functions](https://docs.oracle.com/en-us/iaas/Content/Functions/Tasks/functionspassingconfigparams.htm).

If the Queue is not configured in the Phoenix region, then the region part of the name needs to be modified to reflect the region being used.

#### Exposing the function via the API Gateway

TBD

## Notes/Issues

***<u>TODO:</u>***

* ***<u>Complete the API setup steps above</u>***

## URLs

* These will be unique to the deployment

## Contributing

This project is open source.  Please submit your contributions by forking this repository and submitting a pull request!  Oracle appreciates any contributions that are made by the open-source community.

## License

Copyright (c) 2022 Oracle and/or its affiliates.

Licensed under the Universal Permissive License (UPL), Version 1.0.

See [LICENSE](LICENSE) for more details.

ORACLE AND ITS AFFILIATES DO NOT PROVIDE ANY WARRANTY WHATSOEVER, EXPRESS OR IMPLIED, FOR ANY SOFTWARE, MATERIAL OR CONTENT OF ANY KIND CONTAINED OR PRODUCED WITHIN THIS REPOSITORY, AND IN PARTICULAR SPECIFICALLY DISCLAIM ANY AND ALL IMPLIED WARRANTIES OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE.  FURTHERMORE, ORACLE AND ITS AFFILIATES DO NOT REPRESENT THAT ANY CUSTOMARY SECURITY REVIEW HAS BEEN PERFORMED WITH RESPECT TO ANY SOFTWARE, MATERIAL OR CONTENT CONTAINED OR PRODUCED WITHIN THIS REPOSITORY. IN ADDITION, AND WITHOUT LIMITING THE FOREGOING, THIRD PARTIES MAY HAVE POSTED SOFTWARE, MATERIAL OR CONTENT TO THIS REPOSITORY WITHOUT ANY REVIEW. USE AT YOUR OWN RISK. 