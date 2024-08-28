# Crypto Service

## Overview

This service helps to encrypt and decrypt sensitive data like credentials, etc.

## Things you need to know about this repository

### How to contribute

Contributions are most welcomed! Just commit your changes, send for review to `HEAD:refs/for/master` and send the review to all EVNFM teams

```
git push origin HEAD:refs/for/master
```

This will kick off a gerrit code review job in jenkins, please see the next section for more details on the CI Flows. The link to the jenkins jobs is
available in the top level pom in this repo, checkout the `<ciManagement>` tag.

### CI Flow defined in code

**Note:** If you don't plan on building the repository locally then you can probably skip this section. We want to leverage DevOps best practices
where possible. One such example is to define the CI flow for a project with the following requirements:

* Self generating
* Self managing pipelines
* Reduced setup time when creating new microservices
* Centrally controlled pipelines that can be reconfigured and automatically updated for all flows for each microservice

To achieve this, we have created common CI flows which are code based and use both Jenkins pipelines and job-dsl plugins. This allows us to
pragmatically create Jenkins jobs and pipelines to any configuration we want. This common code is managed in a separate git repository and added as a
git sub-module to each microservice repo. This allows the CI flows to be controlled centrally and common updates can be rolled out to all the 
microservices CI pipelines without having to make changes in each of these repositories.

## Design

![](design/crypto.html)

1. Service details
    - Crypto service is as k8s StatefulSet with 2 PersistentVolumes
    - **keystore** PersistentVolume contains /vnfm.jks file which stores keystore used by the service
    - **secure** PersistentVolume contains /seed file which contains passwords used for the keystore
    - The service supports backup-and-restore functionality, for more details look in the
      [B&R docs](https://adp.ericsson.se/marketplace/backup-and-restore-orchestrator/documentation/development/dpi/service-deployment-guide)

2. Project structure
    - **eric-eo-evnfm-crypto-api** module contains the api.yaml specification for the service and generates model classes defined in the file
      Include this module as a dependency in case you want to generate client stubs for the Crypto service
    - **eric-eo-evnfm-crypto-server** module contains implementation of the functionality for the service
    - **eric-eo-evnfm-crypto-utils** contains utility applications used to generate encrypted constants and test keys files

3. Extending the functionality

3.1 Changing encryption method of the sensitive data

- Choose the next version for you new encryption method (currently, it is 2)
- Implement the *MessageCipher* interface and name the implementation according to the chosen version (currently, it is MessageCipher1v1)
- Put an instance of the new cipher class in the *versionToCipherMap* field of the *MessageCipherVersionUtil* class
- Increment the *latestEncryptionDecryptionVersion* property and generate new */constants* file content using
  *CreateEncryptedConstantsFile* class in *eric-eo-evnfm-crypto-utils module*

3.2 Changing obscurity method for the passwords file

- Choose the next version in a hexadecimal format for the new obscurity method (currently, it is 0x0103 = 259)
- Implement the *PasswordsCipher* interface and name the implementation according to the chosen version (currently, it is PasswordsCipher1v3)
- Put an instance of the new cipher class in the *versionToCipherMap* field of the *PasswordsCipherVersionUtil* class
- Increment the *latestPasswordsObscurityVersion* property and generate new */constants* file content using
  *CreateEncryptedConstantsFile* class in *eric-eo-evnfm-crypto-utils module* 