provider "azurerm" {
  features {}
}

locals {
  subscription_name = "hmc-to-fis-subscription-${var.env}"
}

data "azurerm_servicebus_namespace" "fis_servicebus_namespace" {
  name                = join("-", ["hmc-servicebus", var.env])
  resource_group_name = join("-", ["hmc-shared", var.env])
}

module "servicebus_topic_subscription" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription"
  name                  = local.subscription_name
  namespace_name        = data.azurerm_servicebus_namespace.fis_servicebus_namespace.name
  resource_group_name   = data.azurerm_servicebus_namespace.fis_servicebus_namespace.resource_group_name
  topic_name            = join("-", ["hmc-to-cft", var.env])
}






