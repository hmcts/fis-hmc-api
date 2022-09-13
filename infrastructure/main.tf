provider "azurerm" {
  features {}
}

data "azurerm_key_vault" "fis_key_vault" {
  name                = "${local.azureVaultName}"
  resource_group_name = local.azureVaultName
}

locals {
  azureVaultName = "fis-kv-${var.env}"
}
