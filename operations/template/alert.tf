resource "azurerm_monitor_action_group" "notify_slack_email" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti${var.environment}-actiongroup"
  resource_group_name = data.azurerm_resource_group.group.name
  short_name          = "cdcti-alerts"

  email_receiver {
    name          = "cdcti-flexion-slack-email-receiver"
    email_address = var.alert_slack_email
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

resource "azurerm_monitor_activity_log_alert" "azure_service_health_alert" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti-${var.environment}-azure-status-alert"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name
  scopes              = [azurerm_container_registry.registry.id]

  criteria {
    category = "ServiceHealth"
    levels   = ["Error"]
    service_health {
      locations = var.service_health_locations
      events    = ["Incident"]
      services  = ["*"]
    }
  }

  action {
    action_group_id = azurerm_monitor_action_group.notify_slack_email[count.index].id
  }

  description = "Alert service(s) appear to be down"
  enabled     = true

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

resource "azurerm_monitor_scheduled_query_rules_alert" "database_token_expired_alert" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti-${var.environment}-api-log-token-alert"
  location            = data.azurerm_resource_group.group.location
  resource_group_name = data.azurerm_resource_group.group.name

  action {
    action_group  = [azurerm_monitor_action_group.notify_slack_email[count.index].id]
    email_subject = "FATAL: The access token has expired!"
  }

  data_source_id = azurerm_linux_web_app.api.id
  description    = "Alert when total results cross threshold"
  enabled        = true

  query = <<-QUERY
      AppServiceConsoleLogs
      | where ResultDescription has "FATAL: The access token has expired."
      and TimeGenerated >= ago(30m)
      and TimeGenerated <= now()
      | summarize count()
    QUERY

  severity                = 3
  frequency               = 10
  time_window             = 30
  auto_mitigation_enabled = true

  trigger {
    operator  = "GreaterThan"
    threshold = 1
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

resource "azurerm_monitor_metric_alert" "azure_4XX_alert" {
  count               = local.non_pr_environment ? 1 : 0
  name                = "cdcti-${var.environment}-azure-http-4XX-alert"
  resource_group_name = data.azurerm_resource_group.group.name
  scopes              = [azurerm_storage_account.storage.id]
  description         = "Action will be triggered when Transactions count is greater than 50."

  criteria {
    metric_namespace = "Microsoft.Storage/storageAccounts"
    metric_name      = "Transactions"
    aggregation      = "Total"
    operator         = "GreaterThan"
    threshold        = 50

    dimension {
      name     = "ApiName"
      operator = "Include"
      values   = ["*"]
    }
  }

  action {
    action_group_id = azurerm_monitor_action_group.main.id
  }
}
