variable "product" {
  type    = string
  default = "fis"
}

variable "component" {
  type    = string
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "env" {
  type = string
}

variable "subscription" {
type = string
}

variable "deployment_namespace" {}

variable "common_tags" {
  type = map(string)
}
