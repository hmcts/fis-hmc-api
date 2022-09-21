variable "product" {
  type        = string
  default     = "fis-hmc-api"
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
