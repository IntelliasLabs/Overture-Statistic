variable "aws_region" {
  description = "The AWS region to deploy to"
  type        = string
  default     = "eu-west-1"
}

variable "vpc_cidr" {
  description = "The CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnets" {
  description = "The CIDR blocks for the public subnets"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "availability_zones" {
  description = "The availability zones to deploy to"
  type        = list(string)
  default     = ["eu-west-1a", "eu-west-1b"]
}

variable "admin_ips" {
  description = "A list of CIDR blocks to allow administrative access to Kibana and the extension app."
  type        = list(string)
  default     = ["???"] # Whitelisted admin access
}

# Application Config Variables
variable "debug_enabled" {
  type    = string
  default = "false"
}

variable "server_port" {
  type    = string
  default = "8102"
}

variable "hikari_max_pool_size" {
  type    = string
  default = "25"
}

variable "batch_enabled_jobs" {
  type    = string
  default = "BuildingJob,addressJob,DivisionJob,DivisionAreaJob,DivisionBoundaryJob,WaterJob,InfrastructureJob,LandJob,LandUseJob,ConnectorJob,PlaceJob,landCoverJob,SegmentJob,bathymetryJob"
}

variable "input_base_path" {
  type    = string
  default = "s3://???"
}

variable "data_version" {
  type    = string
  default = "v2026-03-18.2"
}

variable "batch_chunk_size" {
  type    = string
  default = "1000"
}

variable "batch_grid_size" {
  type    = string
  default = "10"
}

variable "batch_partition_size_rows" {
  type    = string
  default = "100000"
}

variable "elastic_connect_timeout" {
  type    = string
  default = "60s"
}

variable "elastic_socket_timeout" {
  type    = string
  default = "60s"
}

variable "elastic_request_timeout" {
  type    = string
  default = "60s"
}
