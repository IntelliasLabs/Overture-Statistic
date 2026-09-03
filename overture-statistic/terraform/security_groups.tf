resource "aws_security_group" "ecs_tasks" {
  name        = "overture-statistic-ecs-tasks-sg"
  description = "Allow inbound traffic for ECS tasks"
  vpc_id      = aws_vpc.main.id

  ingress {
    protocol    = "tcp"
    from_port   = 8080
    to_port     = 8080
    cidr_blocks = var.admin_ips # Whitelisted IP access list
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "overture-statistic-ecs-tasks-sg"
  }
}

resource "aws_security_group" "ssm_endpoints" {
  name        = "overture-statistic-ssm-endpoints-sg"
  description = "Allow ECS tasks to reach Systems Manager interface endpoints"
  vpc_id      = aws_vpc.main.id

  ingress {
    protocol        = "tcp"
    from_port       = 443
    to_port         = 443
    security_groups = [aws_security_group.ecs_tasks.id, aws_security_group.elasticsearch.id]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "overture-statistic-ssm-endpoints-sg"
  }
}

resource "aws_security_group" "elasticsearch" {
  name        = "overture-statistic-elasticsearch-sg"
  description = "Allow inbound traffic for Elasticsearch and Kibana"
  vpc_id      = aws_vpc.main.id

  ingress {
    protocol        = "tcp"
    from_port       = 9200
    to_port         = 9200
    security_groups = [aws_security_group.ecs_tasks.id]
  }

  ingress {
    protocol        = "tcp"
    from_port       = 9200
    to_port         = 9200
    security_groups = [aws_security_group.elasticsearch_proxy.id]
  }

  ingress {
    protocol        = "tcp"
    from_port       = 5601
    to_port         = 5601
    security_groups = [aws_security_group.ecs_tasks.id]
  }

  ingress {
    protocol        = "tcp"
    from_port       = 5601
    to_port         = 5601
    security_groups = [aws_security_group.kibana_alb.id]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "overture-statistic-elasticsearch-sg"
  }
}

resource "aws_security_group" "elasticsearch_proxy" {
  name        = "overture-statistic-elasticsearch-proxy-sg"
  description = "Allow inbound traffic for the Elasticsearch proxy"
  vpc_id      = aws_vpc.main.id

  ingress {
    protocol        = "tcp"
    from_port       = 8081
    to_port         = 8081
    security_groups = [aws_security_group.kibana_alb.id]
  }

  ingress {
    protocol    = "tcp"
    from_port   = 8081
    to_port     = 8081
    cidr_blocks = var.admin_ips
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "overture-statistic-elasticsearch-proxy-sg"
  }
}

resource "aws_security_group" "efs" {
  name        = "overture-statistic-efs-sg"
  description = "Allow inbound EFS traffic from ECS"
  vpc_id      = aws_vpc.main.id

  ingress {
    protocol        = "tcp"
    from_port       = 2049
    to_port         = 2049
    security_groups = [aws_security_group.elasticsearch.id, aws_security_group.ecs_tasks.id]
  }

  egress {
    protocol    = "-1"
    from_port   = 0
    to_port     = 0
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "overture-statistic-efs-sg"
  }
}
