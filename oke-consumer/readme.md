# oci-arch-queue-oke-demo (consumer service)

[![License: UPL](https://img.shields.io/badge/license-UPL-green)](https://img.shields.io/badge/license-UPL-green) [![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=oracle-devrel_oci-arch-queue-oke-demo)](https://sonarcloud.io/dashboard?id=oracle-devrel_oci-arch-queue-oke-demo)

## Introduction

The following document describes how to create the consumer microservice and deploy it ready to use.

### Prerequisites

- [Java JDK 8 or later](https://www.oracle.com/java/technologies/downloads/)
- [Oracle OCI SDK](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm)
- [Maven](https://maven.apache.org/download.cgi)
- KubeCTL



#### Making the queue identifiable and accessible

Within the code is a class called *Environment* (*src/main/java/com/demo/consumer/Environment.java*) which declares several constants that capture the OCI Queue OCID, the URL for the OCI Data Plane endpoint, and the attributes necessary for authenticating and authorization to use the service.

These values need to have their defaults replaced with the appropriate values established during the OCI Queue setup.

If the Queue is not configured in the Phoenix region, then the region part of the name needs to be modified to reflect the region being used.

#### Packaging and deploying the consumer

##### Creating the JAR file

blah

##### Creating the container image

blah

##### Pushing the image to the repository

blah

##### Deploying KEDA

blah

##### deploying the container

blah

#### Observing the Consumer running

blah

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