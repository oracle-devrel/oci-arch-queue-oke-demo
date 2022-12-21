version: 0.1
component: build
timeoutInSeconds: 6000
runAs: root
shell: bash
env:
  # these are local variables to the build config
  variables:
    key: "value"

  # the value of a vaultVariable is the secret-id (in OCI ID format) stored in the OCI Vault service
  # you can then access the value of that secret in your build_spec.yaml commands
  vaultVariables:
  #  EXAMPLE_SECRET: "YOUR-SECRET-OCID"
  
  # exportedVariables are made available to use as parameters in sucessor Build Pipeline stages
  # For this Build to run, the Build Pipeline needs to have a BUILDRUN_HASH parameter set
  exportedVariables:
    - BUILDRUN_HASH
    - image_version

steps:
  - type: Command
    name: "Define unique image tag"
    timeoutInSeconds: 40
    command: |
      export BUILDRUN_HASH=`echo $OCI_BUILD_RUN_ID | rev | cut -c 1-7`
      echo "BUILDRUN_HASH: " $BUILDRUN_HASH

  - type: Command
    timeoutInSeconds: 1200
    name: "Build container image"
    command: |
      cd $OCI_WORKSPACE_DIR/${source_name}
      docker build --pull --rm -t queue-fn-automation:$BUILDRUN_HASH .
#      export image_version="${image_version}"

outputArtifacts:
  - name: output01
    type: DOCKER_IMAGE
    # this location tag doesn't effect the tag used to deliver the container image
    # to the Container Registry
    location: queue-fn-automation:$${BUILDRUN_HASH}