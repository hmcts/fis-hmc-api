variable "product" {
  type        = string
  default     = "fis-hmc-api"
  description = "The name of your application"
}

variable "env" {
  type        = string
  description = "The deployment environment (sandbox, aat, prod etc..)"
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "component" {}

variable "subscription" {}

variable "deployment_namespace" {}

variable "common_tags" {
type = map(string)
}
