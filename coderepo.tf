## Copyright (c) 2022, Oracle and/or its affiliates. 
## All rights reserved. The Universal Permissive License (UPL), Version 1.0 as shown at http://oss.oracle.com/licenses/upl

locals {
  encode_user = urlencode(local.devops_username)
  auth_token  = urlencode(var.oci_user_authtoken)
}


resource "oci_devops_repository" "test_repository" {
  #Required
  name       = var.repository_name
  project_id = oci_devops_project.test_project.id

  #Optional
  default_branch = var.repository_default_branch
  description    = var.repository_description

  repository_type = var.repository_repository_type
}

data "template_file" "build_spec" {
  template = "${file("${path.module}/templates/build_spec.yaml.tpl")}"
  vars = {
    source_name   = "${var.build_pipeline_stage_build_source_collection_items_name}"
    image_version = "${local.ocir_docker_repository}/${local.ocir_namespace}/${oci_artifacts_container_repository.test_container_repository.display_name}:$BUILDRUN_HASH"
  }
}

resource "local_file" "build_spec" {
  content  = data.template_file.build_spec.rendered
  filename = "${path.module}/application/build_spec.yaml"
}

resource "null_resource" "clonerepo" {

  depends_on = [local_file.build_spec, oci_devops_project.test_project, oci_devops_repository.test_repository]

  provisioner "local-exec" {
    command = "echo '(1) Cleaning local repo: '; rm -rf ${oci_devops_repository.test_repository.name}"
  }

  provisioner "local-exec" {
    command = "echo '(2) Repo to clone: https://devops.scmservice.${var.region}.oci.oraclecloud.com/namespaces/${local.ocir_namespace}/projects/${oci_devops_project.test_project.name}/repositories/${oci_devops_repository.test_repository.name}'"
  }

  provisioner "local-exec" {
    command = "echo '(3) Starting git clone command... '; echo 'Username: Before' ${local.devops_username}; echo 'Username: After' ${local.encode_user}; echo 'auth_token' ${local.auth_token}; git clone https://${local.encode_user}:${local.auth_token}@devops.scmservice.${var.region}.oci.oraclecloud.com/namespaces/${local.ocir_namespace}/projects/${oci_devops_project.test_project.name}/repositories/${oci_devops_repository.test_repository.name};"
  }

  provisioner "local-exec" {
    command = "echo '(4) Finishing git clone command: '; ls -latr ${oci_devops_repository.test_repository.name}"
  }
}


resource "null_resource" "copyfiles" {

  depends_on = [null_resource.clonerepo]

  provisioner "local-exec" {
    command = "cp -pr ${path.module}/application/* ${oci_devops_repository.test_repository.name}/; cd .."
  }
}


resource "null_resource" "pushcode" {

  depends_on = [null_resource.copyfiles]

  provisioner "local-exec" {
    command = "cd ./${oci_devops_repository.test_repository.name}; git config --local user.email 'test@example.com'; git config --local user.name '${local.devops_username}';git add .; git commit -m 'added latest files'; git push origin '${var.repository_default_branch}'"
  }
}


