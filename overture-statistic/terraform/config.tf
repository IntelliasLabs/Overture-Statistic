# Sensitive Configuration (SSM Parameter Store)
resource "aws_ssm_parameter" "elastic_password" {
  name        = "/overture/prod/elastic-password"
  description = "Password for the elastic user"
  type        = "SecureString"
  value       = "change-me-in-aws-console" # Placeholder

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "kibana_password" {
  name        = "/overture/prod/kibana-password"
  description = "Password for the kibana_system user"
  type        = "SecureString"
  value       = "change-me-in-aws-console" # Placeholder

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "elastic_token" {
  name        = "/overture/prod/elastic-token"
  description = "Authentication token for Elasticsearch"
  type        = "SecureString"
  value       = "change-me-in-aws-console" # Placeholder

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "s3_access_key" {
  name        = "/overture/prod/s3-access-key"
  description = "AWS Access Key for S3"
  type        = "SecureString"
  value       = "change-me-in-aws-console" # Placeholder

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "s3_secret_key" {
  name        = "/overture/prod/s3-secret-key"
  description = "AWS Secret Key for S3"
  type        = "SecureString"
  value       = "change-me-in-aws-console" # Placeholder

  lifecycle {
    ignore_changes = [value]
  }
}

# General Configuration
resource "aws_ssm_parameter" "log_level" {
  name  = "/overture/prod/log-level"
  type  = "String"
  value = "INFO"
}
