provider "azurerm" {
  features {}
}

locals {
  subscription_name = "hmc-fis-subscription-${var.env}"
  subscription_name_new = "hmc-fis-subs-prl-${var.env}"
  s2s_rg_prefix               = "rpe-service-auth-provider"
  s2s_key_vault_name          = var.env == "preview" || var.env == "spreview" ? join("-", ["s2s", "aat"]) : join("-", ["s2s", var.env])
  s2s_vault_resource_group    = var.env == "preview" || var.env == "spreview" ? join("-", [local.s2s_rg_prefix, "aat"]) : join("-", [local.s2s_rg_prefix, var.env])
}

data "azurerm_servicebus_namespace" "fis_servicebus_namespace" {
  name                = join("-", ["hmc-servicebus", var.env])
  resource_group_name = join("-", ["hmc-shared", var.env])
}

data "azurerm_key_vault" "fis_kv_key_vault" {
  name = join("-", ["fis-kv", var.env])
  resource_group_name = join("-", [var.product, var.env])
}

module "servicebus_topic_subscription" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=4.x"
  name                  = local.subscription_name
  namespace_id        = data.azurerm_servicebus_namespace.fis_servicebus_namespace.id
  topic_name            = join("-", ["hmc-to-cft", var.env])
}

resource "azurerm_servicebus_subscription_rule" "hmctsServiceCode" {
  name            = "hmc_to_fis_subscription_rule"
  subscription_id = module.servicebus_topic_subscription.id
  filter_type     = "SqlFilter"
  sql_filter      = "hmctsServiceCode = 'BBA3'"
}

module "servicebus_topic_subscription_new" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=4.x"
  name                  = local.subscription_name_new
  namespace_id        = data.azurerm_servicebus_namespace.fis_servicebus_namespace.id
  topic_name            = join("-", ["hmc-to-cft", var.env])
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = local.s2s_key_vault_name
  resource_group_name = local.s2s_vault_resource_group
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name          = "microservicekey-fis-hmc-api"
  key_vault_id  = data.azurerm_key_vault.s2s_key_vault.id
}

resource "azurerm_key_vault_secret" "fis-hmc-api-s2s-secret" {
  name          = "fis-hmc-api-s2s-secret"
  value         = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id  = data.azurerm_key_vault.fis_kv_key_vault.id
}
