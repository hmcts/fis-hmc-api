#HMC to Hearings API Azure service bus Topic
module "servicebus-namespace" {
  source              = "git@github.com:hmcts/terraform-module-servicebus-namespace?ref=master"
  name                = "${var.product}-servicebus-${var.env}"
  location            = var.location
  env                 = var.env
  common_tags         = local.tags
  resource_group_name = local.resource_group_name
}

data "azurerm_key_vault" "fis-key-vault" {
  name                = "fis-kv-${var.env}"
  resource_group_name = "fis-${var.env}"
}

data "azurerm_key_vault_secret" "fis-servicebus-connection-string" {
  key_vault_id = data.azurerm_key_vault.fis_key_vault.id
  name         = "hmc-servicebus-connection-string"
}

resource "azurerm_key_vault_secret" "servicebus_primary_connection_string" {
  name         = "hmc-servicebus-connection-string"
  value        = module.servicebus-namespace.primary_send_and_listen_connection_string
  key_vault_id = data.azurerm_key_vault.fis_key_vault.id
}
