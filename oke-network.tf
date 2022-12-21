## Copyright (c) 2022, Oracle and/or its affiliates.
## All rights reserved. The Universal Permissive License (UPL), Version 1.0 as shown at http://oss.oracle.com/licenses/upl


resource "oci_core_virtual_network" "oke_vcn" {
  cidr_block     = lookup(var.network_cidrs, "VCN-CIDR")
  compartment_id = local.oke_compartment_id
  display_name   = "OKE-VCN-${random_string.deploy_id.result}"
  dns_label      = "oke${random_string.deploy_id.result}"
  defined_tags   = { "${oci_identity_tag_namespace.ArchitectureCenterTagNamespace.name}.${oci_identity_tag.ArchitectureCenterTag.name}" = var.release }

  count = var.create_new_oke_cluster ? 1 : 0
}

resource "oci_core_subnet" "oke_k8s_endpoint_subnet" {
  cidr_block                 = lookup(var.network_cidrs, "ENDPOINT-SUBNET-REGIONAL-CIDR")
  compartment_id             = local.oke_compartment_id
  display_name               = "oke-k8s-endpoint-subnet-${random_string.deploy_id.result}"
  dns_label                  = "okek8sn${random_string.deploy_id.result}"
  vcn_id                     = oci_core_virtual_network.oke_vcn[0].id
  prohibit_public_ip_on_vnic = (var.cluster_endpoint_visibility == "Private") ? true : false
  route_table_id             = (var.cluster_endpoint_visibility == "Private") ? oci_core_route_table.oke_private_route_table[0].id : oci_core_route_table.oke_public_route_table[0].id
  dhcp_options_id            = oci_core_virtual_network.oke_vcn[0].default_dhcp_options_id
  security_list_ids          = [oci_core_security_list.oke_endpoint_security_list[0].id]
  defined_tags               = { "${oci_identity_tag_namespace.ArchitectureCenterTagNamespace.name}.${oci_identity_tag.ArchitectureCenterTag.name}" = var.release }


  count = var.create_new_oke_cluster ? 1 : 0
}
resource "oci_core_subnet" "oke_nodes_subnet" {
  cidr_block                 = lookup(var.network_cidrs, "SUBNET-REGIONAL-CIDR")
  compartment_id             = local.oke_compartment_id
  display_name               = "oke-nodes-subnet-${random_string.deploy_id.result}"
  dns_label                  = "okenodesn${random_string.deploy_id.result}"
  vcn_id                     = oci_core_virtual_network.oke_vcn[0].id
  prohibit_public_ip_on_vnic = (var.cluster_workers_visibility == "Private") ? true : false
  route_table_id             = (var.cluster_workers_visibility == "Private") ? oci_core_route_table.oke_private_route_table[0].id : oci_core_route_table.oke_public_route_table[0].id
  dhcp_options_id            = oci_core_virtual_network.oke_vcn[0].default_dhcp_options_id
  security_list_ids          = [oci_core_security_list.oke_nodes_security_list[0].id]
  defined_tags               = { "${oci_identity_tag_namespace.ArchitectureCenterTagNamespace.name}.${oci_identity_tag.ArchitectureCenterTag.name}" = var.release }

  count = var.create_new_oke_cluster ? 1 : 0
}

resource "oci_core_subnet" "oke_lb_subnet" {
  cidr_block                 = lookup(var.network_cidrs, "LB-SUBNET-REGIONAL-CIDR")
  compartment_id             = local.oke_compartment_id
  display_name               = "oke-lb-subnet-${random_string.deploy_id.result}"
  dns_label                  = "okelbsn${random_string.deploy_id.result}"
  vcn_id                     = oci_core_virtual_network.oke_vcn[0].id
  prohibit_public_ip_on_vnic = false
  route_table_id             = oci_core_route_table.oke_public_route_table[0].id
  dhcp_options_id            = oci_core_virtual_network.oke_vcn[0].default_dhcp_options_id
  security_list_ids          = [oci_core_security_list.oke_lb_security_list[0].id]
  defined_tags               = { "${oci_identity_tag_namespace.ArchitectureCenterTagNamespace.name}.${oci_identity_tag.ArchitectureCenterTag.name}" = var.release }


  count = var.create_new_oke_cluster ? 1 : 0
}

resource "oci_core_route_table" "oke_private_route_table" {
  compartment_id = local.oke_compartment_id
  vcn_id         = oci_core_virtual_network.oke_vcn[0].id
  display_name   = "oke-private-route-table-${random_string.deploy_id.result}"

  route_rules {
    description       = "Traffic to the internet"
    destination       = lookup(var.network_cidrs, "ALL-CIDR")
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_nat_gateway.oke_nat_gateway[0].id
  }
  route_rules {
    description       = "Traffic to OCI services"
    destination       = lookup(data.oci_core_services.all_services.services[0], "cidr_block")
    destination_type  = "SERVICE_CIDR_BLOCK"
    network_entity_id = oci_core_service_gateway.oke_service_gateway[0].id
  }
  defined_tags = { "${oci_identity_tag_namespace.ArchitectureCenterTagNamespace.name}.${oci_identity_tag.ArchitectureCenterTag.name}" = var.release }

  count = var.create_new_oke_cluster ? 1 : 0
}

resource "oci_core_route_table" "oke_public_route_table" {
  compartment_id = local.oke_compartment_id
  vcn_id         = oci_core_virtual_network.oke_vcn[0].id
  display_name   = "oke-public-route-table-${random_string.deploy_id.result}"

  route_rules {
    description       = "Traffic to/from internet"
    destination       = lookup(var.network_cidrs, "ALL-CIDR")
    destination_type  = "CIDR_BLOCK"
    network_entity_id = oci_core_internet_gateway.oke_internet_gateway[0].id
  }
  defined_tags = { "${oci_identity_tag_namespace.ArchitectureCenterTagNamespace.name}.${oci_identity_tag.ArchitectureCenterTag.name}" = var.release }

  count = var.create_new_oke_cluster ? 1 : 0
}

resource "oci_core_nat_gateway" "oke_nat_gateway" {
  block_traffic  = "false"
  compartment_id = local.oke_compartment_id
  display_name   = "oke-nat-gateway-${random_string.deploy_id.result}"
  vcn_id         = oci_core_virtual_network.oke_vcn[0].id
  defined_tags   = { "${oci_identity_tag_namespace.ArchitectureCenterTagNamespace.name}.${oci_identity_tag.ArchitectureCenterTag.name}" = var.release }

  count = var.create_new_oke_cluster ? 1 : 0
}

resource "oci_core_internet_gateway" "oke_internet_gateway" {
  compartment_id = local.oke_compartment_id
  display_name   = "oke-internet-gateway-${random_string.deploy_id.result}"
  enabled        = true
  vcn_id         = oci_core_virtual_network.oke_vcn[0].id
  defined_tags   = { "${oci_identity_tag_namespace.ArchitectureCenterTagNamespace.name}.${oci_identity_tag.ArchitectureCenterTag.name}" = var.release }

  count = var.create_new_oke_cluster ? 1 : 0
}

resource "oci_core_service_gateway" "oke_service_gateway" {
  compartment_id = local.oke_compartment_id
  display_name   = "oke-service-gateway-${random_string.deploy_id.result}"
  vcn_id         = oci_core_virtual_network.oke_vcn[0].id
  services {
    service_id = lookup(data.oci_core_services.all_services.services[0], "id")
  }
  defined_tags = { "${oci_identity_tag_namespace.ArchitectureCenterTagNamespace.name}.${oci_identity_tag.ArchitectureCenterTag.name}" = var.release }

  count = var.create_new_oke_cluster ? 1 : 0
}
