locals {
  devops_username = join("/", [lower(data.oci_identity_tenancy.tenant_details.name), var.oci_username])
  ocir_docker_repository = join("", [lower(lookup(data.oci_identity_regions.current_region.regions[0], "key")), ".ocir.io"])
  ocir_namespace = lookup(data.oci_objectstorage_namespace.ns, "namespace")
  ocir_username = join("/", [local.ocir_namespace, var.oci_username])
  oci_queues = [for queue in split(",", var.queue_ocid): trimspace(queue) if trimspace(queue) != ""]
  oci_functions = [for function in split(",", var.function_ocid): trimspace(function) if trimspace(function) != ""]
  queue_function_map = { for index in range(length(local.oci_queues)):
    local.oci_queues[index] => local.oci_functions[index]
  }
}
