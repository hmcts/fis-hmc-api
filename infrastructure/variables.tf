variable "product" {
  type        = string
  default     = "fis"
  description = "The name of your application"
}

variable "env" {
type = string
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "common_tags" {
type = map(string)
}

variable "aks_subscription_id" {}
