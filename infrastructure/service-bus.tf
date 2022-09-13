#HMC to Hearings API
module "servicebus-subscription" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=master"
  name                  = "fis-hmc-api-subscription-${var.env}"
  namespace_name        = "hmc-servicebus-${var.env}"
  topic_name            = "hmc-to-cft-${var.env}"
  resource_group_name   = "hmc-shared-${var.env}"
}

data "azurerm_key_vault" "fis-key-vault" {
  name                = "fis-kv-${var.env}"
  resource_group_name = "fis-${var.env}"
}

data "azurerm_key_vault_secret" "fis-servicebus-connection-string" {
  key_vault_id = data.azurerm_key_vault.fis_key_vault.id
  name         = "hmc-servicebus-connection-string"
}

resource "azurerm_key_vault_secret" "fis-servicebus-connection-string" {
  name         = "hmc-servicebus-connection-string"
  value        = data.azurerm_key_vault_secret.fis-servicebus-connection-string.value
  key_vault_id = data.azurerm_key_vault.fis_key_vault.id
}

data "azurerm_key_vault_secret" "hmc-servicebus-shared-access-key" {
  key_vault_id = data.azurerm_key_vault.fis_key_vault.id
  name         = "hmc-servicebus-shared-access-key"
}

resource "azurerm_key_vault_secret" "sscs-hmc-servicebus-hared-access-key" {
  name         = "hmc-servicebus-shared-access-key"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-shared-access-key.value
  key_vault_id = data.azurerm_key_vault.fis_key_vault.id
}
