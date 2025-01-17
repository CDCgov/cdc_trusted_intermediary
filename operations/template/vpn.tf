resource "azurerm_public_ip" "vpn_ip" {
  name                = "vpn-ip"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  allocation_method = "Static"
  sku               = "Standard"
  #   below tags are managed by CDC
  lifecycle {
    ignore_changes = [
      tags["business_steward"],
      tags["center"],
      tags["environment"],
      tags["escid"],
      tags["funding_source"],
      tags["pii_data"],
      tags["security_compliance"],
      tags["security_steward"],
      tags["support_group"],
      tags["system"],
      tags["technical_steward"],
      tags["zone"]
    ]
  }
}

resource "azurerm_virtual_network_gateway" "vpn" {
  name                = "${var.environment}-vpn"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  type     = "Vpn"
  vpn_type = "RouteBased"

  active_active = false
  enable_bgp    = false
  generation    = "Generation2"
  sku           = "VpnGw2"

  ip_configuration {
    public_ip_address_id          = azurerm_public_ip.vpn_ip.id
    private_ip_address_allocation = "Dynamic"
    subnet_id                     = azurerm_subnet.vpn.id
  }

  dynamic "vpn_client_configuration" {
    for_each = var.vpn_root_certificate != null ? [1] : []
    content {
      address_space        = ["192.168.0.0/16"]
      vpn_auth_types       = ["Certificate"]
      vpn_client_protocols = ["OpenVPN"]

      root_certificate {
        name             = "vpn-cert"
        public_cert_data = var.vpn_root_certificate
      }
    }
  }

  depends_on = [azurerm_subnet.app, azurerm_subnet.database, azurerm_subnet.resolver_inbound, azurerm_subnet.resolver_outbound, azurerm_subnet_network_security_group_association.app_security_group, azurerm_subnet_network_security_group_association.database_security_group, azurerm_subnet_route_table_association.database_database] # the VPN "locks" the subnets, so the VPN should wait until the subnet edits are done

  #   below tags are managed by CDC
  lifecycle {
    ignore_changes = [
      tags["business_steward"],
      tags["center"],
      tags["environment"],
      tags["escid"],
      tags["funding_source"],
      tags["pii_data"],
      tags["security_compliance"],
      tags["security_steward"],
      tags["support_group"],
      tags["system"],
      tags["technical_steward"],
      tags["zone"]
    ]
  }
}

resource "azurerm_private_dns_resolver" "private_zone_resolver" {
  name                = "private-resolve-${var.environment}"
  resource_group_name = data.azurerm_resource_group.group.name
  location            = data.azurerm_resource_group.group.location
  virtual_network_id  = data.azurerm_virtual_network.app.id

  #   below tags are managed by CDC
  lifecycle {
    ignore_changes = [
      tags["business_steward"],
      tags["center"],
      tags["environment"],
      tags["escid"],
      tags["funding_source"],
      tags["pii_data"],
      tags["security_compliance"],
      tags["security_steward"],
      tags["support_group"],
      tags["system"],
      tags["technical_steward"],
      tags["zone"]
    ]
  }
}


resource "azurerm_private_dns_resolver_inbound_endpoint" "resolver_inbound_endpoint" {
  name                    = "endpoint-inbound-${var.environment}"
  private_dns_resolver_id = azurerm_private_dns_resolver.private_zone_resolver.id
  location                = azurerm_private_dns_resolver.private_zone_resolver.location

  ip_configurations {
    private_ip_allocation_method = "Dynamic"
    subnet_id                    = azurerm_subnet.resolver_inbound.id
  }

  #   below tags are managed by CDC
  lifecycle {
    ignore_changes = [
      tags["business_steward"],
      tags["center"],
      tags["environment"],
      tags["escid"],
      tags["funding_source"],
      tags["pii_data"],
      tags["security_compliance"],
      tags["security_steward"],
      tags["support_group"],
      tags["system"],
      tags["technical_steward"],
      tags["zone"]
    ]
  }
}

resource "azurerm_private_dns_resolver_outbound_endpoint" "resolver_outbound_endpoint" {
  name                    = "endpoint-outbound-${var.environment}"
  private_dns_resolver_id = azurerm_private_dns_resolver.private_zone_resolver.id
  location                = azurerm_private_dns_resolver.private_zone_resolver.location
  subnet_id               = azurerm_subnet.resolver_outbound.id

  #   below tags are managed by CDC
  lifecycle {
    ignore_changes = [
      tags["business_steward"],
      tags["center"],
      tags["environment"],
      tags["escid"],
      tags["funding_source"],
      tags["pii_data"],
      tags["security_compliance"],
      tags["security_steward"],
      tags["support_group"],
      tags["system"],
      tags["technical_steward"],
      tags["zone"]
    ]
  }
}
