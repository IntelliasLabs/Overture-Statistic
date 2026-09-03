output "kibana_alb_dns_name" {
  description = "Public ALB DNS name for Kibana"
  value       = aws_lb.kibana.dns_name
}

output "kibana_url" {
  description = "Public HTTPS URL for Kibana via ALB"
  value       = "https://${aws_lb.kibana.dns_name}"
}
