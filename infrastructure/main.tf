provider "azurerm" {
  features {}
}

locals {
  subscription_name = "fis-hmc-api-subscription"
}

data "azurerm_resource_group" "rg" {
  name     = join("-", [var.product, var.env])
}

data "azurerm_key_vault" "fis_key_vault" {
  name = join("-", [var.product, "kv", var.env])
  resource_group_name = join("-", [var.product, var.env])
}

data "azurerm_servicebus_namespace" "fis_servicebus_namespace" {
  name                = join("-", ["hmc-servicebus", var.env])
  resource_group_name = join("-", ["hmc-shared", var.env])
}

data "azurerm_servicebus_topic" "fis_servicebus_topic"  {
  source              = "git@github.com:hmcts/terraform-module-servicebus-topic"
  name                = join("-", ["hmc-to-cft", var.env])
  connection_string   = data.azurerm_servicebus_namespace.fis_servicebus_namespace.default_primary_connection_string
  resource_group_name = data.azurerm_servicebus_namespace.fis_servicebus_namespace.resource_group_name
}

# primary connection string for send and listen operations
output "connection_string" {
  value     = data.azurerm_servicebus_topic.fis_servicebus_topic.connection_string
  sensitive = true
}

module "servicebus_topic_subscription" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription"
  name                  = local.subscription_name
  namespace_name        = data.azurerm_servicebus_namespace.fis_servicebus_namespace.name
  resource_group_name   = data.azurerm_servicebus_namespace.fis_servicebus_namespace.resource_group_name
  topic_name            = join("-", ["hmc-to-cft", var.env])
}
