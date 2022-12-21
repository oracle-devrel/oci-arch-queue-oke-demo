## Copyright (c) 2022, Oracle and/or its affiliates.
## All rights reserved. The Universal Permissive License (UPL), Version 1.0 as shown at http://oss.oracle.com/licenses/upl


resource "oci_identity_dynamic_group" "oke_nodes_dg" {
  name           = "oke-cluster-dg-${random_string.deploy_id.result}"
  description    = "Cluster Dynamic Group"
  compartment_id = var.tenancy_ocid
  matching_rule  = "ANY {ALL {instance.compartment.id = '${local.oke_compartment_id}'},ALL {resource.type = 'cluster', resource.compartment.id = '${local.oke_compartment_id}'}}"

  provider = oci.home_region

  count = var.create_dynamic_group_for_nodes_in_compartment ? 1 : 0
}
resource "oci_identity_policy" "oke_compartment_policies" {
  name           = "oke-cluster-compartment-policies-${random_string.deploy_id.result}"
  description    = "OKE Cluster Compartment Policies"
  compartment_id = local.oke_compartment_id
  statements     = local.oke_compartment_statements

  depends_on = [oci_identity_dynamic_group.oke_nodes_dg]

  provider = oci.home_region

  count = var.create_compartment_policies ? 1 : 0
}
resource "oci_identity_policy" "kms_compartment_policies" {
  name           = "kms-compartment-policies-${random_string.deploy_id.result}"
  description    = "KMS Compartment Policies"
  compartment_id = local.oke_compartment_id
  statements     = local.kms_compartment_statements

  depends_on = [oci_identity_dynamic_group.oke_nodes_dg]

  provider = oci.home_region

  count = (var.create_compartment_policies && var.create_vault_policies_for_group) ? 1 : 0
}

resource "oci_identity_policy" "oke_tenancy_policies" {
  name           = "oke-cluster-tenancy-policies-${random_string.deploy_id.result}"
  description    = "OKE Cluster Tenancy Policies"
  compartment_id = var.tenancy_ocid
  statements     = local.oke_tenancy_statements

  depends_on = [oci_identity_dynamic_group.oke_nodes_dg]

  provider = oci.home_region

  count = var.create_tenancy_policies ? 1 : 0
}

locals {
  oke_tenancy_statements = concat(
    local.oci_grafana_metrics_statements,
    local.allow_oke_use_queues_statements,
    local.allow_oke_invoke_fn_statements
  )
  oke_compartment_statements = concat(
    local.oci_grafana_logs_statements,
    var.use_encryption_from_oci_vault ? local.allow_oke_use_oci_vault_keys_statements : []
    #var.cluster_autoscaler_enabled ? local.cluster_autoscaler_statements : []
  )
  kms_compartment_statements = concat(
    local.allow_group_manage_vault_keys_statements
  )
}

locals {
  oke_nodes_dg     = var.create_dynamic_group_for_nodes_in_compartment ? oci_identity_dynamic_group.oke_nodes_dg.0.name : "void"
  oci_vault_key_id = "void"
  #var.use_encryption_from_oci_vault ? (var.create_new_encryption_key ? oci_kms_key.mushop_key[0].id : var.existent_encryption_key_id) : "void"
  oci_grafana_metrics_statements = [
    "Allow dynamic-group ${local.oke_nodes_dg} to read metrics in tenancy",
    "Allow dynamic-group ${local.oke_nodes_dg} to read compartments in tenancy"
  ]
  oci_grafana_logs_statements = [
    "Allow dynamic-group ${local.oke_nodes_dg} to read log-groups in compartment id ${local.oke_compartment_id}",
    "Allow dynamic-group ${local.oke_nodes_dg} to read log-content in compartment id ${local.oke_compartment_id}"
  ]
  # cluster_autoscaler_statements = [
  #   "Allow dynamic-group ${local.oke_nodes_dg} to manage cluster-node-pools in compartment id ${local.oke_compartment_id}",
  #   "Allow dynamic-group ${local.oke_nodes_dg} to manage instance-family in compartment id ${local.oke_compartment_id}",
  #   "Allow dynamic-group ${local.oke_nodes_dg} to use subnets in compartment id ${local.oke_compartment_id}",
  #   "Allow dynamic-group ${local.oke_nodes_dg} to read virtual-network-family in compartment id ${local.oke_compartment_id}",
  #   "Allow dynamic-group ${local.oke_nodes_dg} to use vnics in compartment id ${local.oke_compartment_id}",
  #   "Allow dynamic-group ${local.oke_nodes_dg} to inspect compartments in compartment id ${local.oke_compartment_id}"
  # ]
  allow_oke_use_queues_statements = [
    "Allow dynamic-group ${local.oke_nodes_dg} to use queues in compartment id ${var.queue_compartment_ocid == "" ? local.oke_compartment_id : var.queue_compartment_ocid}"
  ]
  allow_oke_invoke_fn_statements = [
    "Allow dynamic-group ${local.oke_nodes_dg} to use fn-invocation in compartment id ${var.function_compartment_ocid == "" ? local.oke_compartment_id : var.function_compartment_ocid}"
  ]
  allow_oke_use_oci_vault_keys_statements = [
    "Allow service oke to use vaults in compartment id ${local.oke_compartment_id}",
    "Allow service oke to use keys in compartment id ${local.oke_compartment_id} where target.key.id = '${local.oci_vault_key_id}'",
    "Allow dynamic-group ${local.oke_nodes_dg} to use keys in compartment id ${local.oke_compartment_id} where target.key.id = '${local.oci_vault_key_id}'"
  ]
  allow_group_manage_vault_keys_statements = [
    "Allow group ${var.user_admin_group_for_vault_policy} to manage vaults in compartment id ${local.oke_compartment_id}",
    "Allow group ${var.user_admin_group_for_vault_policy} to manage keys in compartment id ${local.oke_compartment_id}",
    "Allow group ${var.user_admin_group_for_vault_policy} to use key-delegate in compartment id ${local.oke_compartment_id}"
  ]
}