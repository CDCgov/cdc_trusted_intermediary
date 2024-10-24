data "azuread_service_principal" "principal" {
  object_id = data.azurerm_client_config.current.object_id
}

resource "azurerm_postgresql_flexible_server" "database" {
  name                  = "cdcti-${var.environment}-database"
  resource_group_name   = data.azurerm_resource_group.group.name
  location              = data.azurerm_resource_group.group.location
  sku_name              = local.higher_environment_level ? "B_Standard_B2ms" : "B_Standard_B1ms"
  version               = "16"
  storage_mb            = "32768"
  auto_grow_enabled     = true
  backup_retention_days = "14"

  public_network_access_enabled = !local.cdc_domain_environment
  delegated_subnet_id           = local.cdc_domain_environment ? azurerm_subnet.database.id : null
  private_dns_zone_id           = local.cdc_domain_environment ? azurerm_private_dns_zone.dns_zone.id : null

  authentication {
    password_auth_enabled         = "false"
    active_directory_auth_enabled = "true"
    tenant_id                     = data.azurerm_client_config.current.tenant_id
  }

  maintenance_window { # Sunday at 12:00 UTC which is 7:00 AM EST or 8:00 AM EDT (around the time of our SLA's maintenance window)
    day_of_week  = 0
    start_hour   = 12
    start_minute = 0
  }

  depends_on = [azurerm_private_dns_zone_virtual_network_link.db_network_link]

  lifecycle {
    ignore_changes = [
      zone,
      high_availability.0.standby_availability_zone,
      #   below tags are managed by CDC
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

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "admin_for_deployer" {
  server_name         = azurerm_postgresql_flexible_server.database.name
  resource_group_name = data.azurerm_resource_group.group.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = var.deployer_id
  principal_name      = "cdcti-github"
  principal_type      = "ServicePrincipal"
}

resource "azurerm_postgresql_flexible_server_active_directory_administrator" "admin_for_app" {
  server_name         = azurerm_postgresql_flexible_server.database.name
  resource_group_name = data.azurerm_resource_group.group.name
  tenant_id           = data.azurerm_client_config.current.tenant_id
  object_id           = azurerm_linux_web_app.api.identity.0.principal_id
  principal_name      = azurerm_linux_web_app.api.name
  principal_type      = "ServicePrincipal"
}

resource "azurerm_postgresql_flexible_server_firewall_rule" "db_firewall_5" {
  count            = local.cdc_domain_environment ? 0 : 1
  name             = "AllowAzure"
  server_id        = azurerm_postgresql_flexible_server.database.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}
