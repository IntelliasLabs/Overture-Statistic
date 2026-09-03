resource "aws_security_group" "kibana_alb" {
  name        = "overture-statistic-kibana-alb-sg"
  description = "Allow public access to Kibana ALB"
  vpc_id      = aws_vpc.main.id

  ingress {
    protocol    = "tcp"
    from_port   = 80
    to_port     = 80
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    protocol    = "tcp"
    from_port   = 443
    to_port     = 443
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "overture-statistic-kibana-alb-sg"
  }
}

resource "aws_lb" "kibana" {
  name               = "overture-kibana-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.kibana_alb.id]
  subnets            = aws_subnet.public[*].id

  tags = {
    Name = "overture-kibana-alb"
  }
}

resource "aws_lb_target_group" "kibana" {
  name        = "overture-kibana-tg"
  port        = 5601
  protocol    = "HTTP"
  target_type = "ip"
  vpc_id      = aws_vpc.main.id

  health_check {
    path                = "/"
    protocol            = "HTTP"
    matcher             = "200-399"
    interval            = 30
    timeout             = 5
    healthy_threshold   = 2
    unhealthy_threshold = 3
  }
}

resource "aws_lb_listener" "kibana_http" {
  load_balancer_arn = aws_lb.kibana.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type = "redirect"

    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

resource "tls_private_key" "kibana_self_signed" {
  algorithm = "RSA"
  rsa_bits  = 2048
}

resource "tls_self_signed_cert" "kibana_self_signed" {
  private_key_pem = tls_private_key.kibana_self_signed.private_key_pem

  subject {
    common_name  = aws_lb.kibana.dns_name
    organization = "overture-statistic"
  }

  validity_period_hours = 8760
  allowed_uses = [
    "key_encipherment",
    "digital_signature",
    "server_auth",
  ]
}

resource "aws_acm_certificate" "kibana_self_signed" {
  private_key      = tls_private_key.kibana_self_signed.private_key_pem
  certificate_body = tls_self_signed_cert.kibana_self_signed.cert_pem
}

resource "aws_lb_listener" "kibana_https" {
  load_balancer_arn = aws_lb.kibana.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS13-1-2-2021-06"
  certificate_arn   = aws_acm_certificate.kibana_self_signed.arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.kibana.arn
  }
}
