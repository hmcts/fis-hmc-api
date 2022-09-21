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

module "servicebus_topic" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-topic"
  name                = join("-", ["hmc-to-cft", var.env])
  namespace_name        = data.azurerm_servicebus_namespace.fis_servicebus_namespace.name
  resource_group_name   = data.azurerm_resource_group.rg.name
}

module "servicebus_topic_subscription" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription"
  name                  = local.subscription_name
  namespace_name        = data.azurerm_servicebus_namespace.fis_servicebus_namespace.name
  resource_group_name   = data.azurerm_resource_group.rg.name
  topic_name            = module.servicebus_topic.name
}

resource "azurerm_key_vault_secret" "fis_servicebus_topic_shared_access_key" {
  name         = "ccpay-service-request-cpo-update-topic-shared-access-key"
  value        = module.servicebus_topic.primary_send_and_listen_shared_access_key
  key_vault_id = data.azurerm_key_vault.fis_key_vault.id
}
