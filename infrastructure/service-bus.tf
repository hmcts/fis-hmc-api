#HMC to Hearings API Azure service bus Topic
module "topic-subscription" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=master"
  name                  = local.ccd_case_events_subscription_name
  namespace_name        = local.servicebus_namespace_name
  topic_name            = local.topic_name
  resource_group_name   = local.resource_group_name
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
