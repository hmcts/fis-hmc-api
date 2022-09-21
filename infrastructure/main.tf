provider "azurerm" {
  features {}
  skip_provider_registration = true
  alias                      = "private_endpoint"
  subscription_id            = var.aks_subscription_id
}

locals {
  topic_name                        = "hmc-to-cft-${var.env}"
  subscription_name                 = "${var.product}-case-events-sub-${var.env}"
  servicebus_namespace_name         = "hmc-servicebus-${var.env}"
  resource_group_name               = "hmc-shared-${var.env}"
  ccd_case_events_subscription_name = "fis-hmc-api-subscription-${var.env}"
  fis_key_vault = join("-", ["fis-kv", var.env])
  fis_resource_group_name = join("-", ["fis", var.env])
  tags = var.common_tags

}

data "azurerm_key_vault" "fis_key_vault" {
name                = local.fis_key_vault
resource_group_name = local.fis_resource_group_name
}


