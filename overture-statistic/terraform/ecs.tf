resource "aws_ecs_cluster" "main" {
  name = "overture-statistic-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = {
    Name = "overture-statistic-cluster"
  }
}

resource "aws_cloudwatch_log_group" "ecs_logs" {
  name              = "/ecs/overture-statistic"
  retention_in_days = 30
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name = "overture-statistic-task-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role" "ecs_task_role" {
  name = "overture-statistic-task-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Allow ecs exec/ssm traffic from the task role
resource "aws_iam_role_policy" "ecs_task_ssm_exec_policy" {
  name = "overture-statistic-ssm-exec"
  role = aws_iam_role.ecs_task_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssmmessages:CreateControlChannel",
          "ssmmessages:CreateDataChannel",
          "ssmmessages:OpenControlChannel",
          "ssmmessages:OpenDataChannel",
        ]
        Resource = "*"
      }
    ]
  })
}

# Add ECR access policy
resource "aws_iam_role_policy" "ecs_ecr_policy" {
  name = "overture-statistic-ecr-policy"
  role = aws_iam_role.ecs_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Effect   = "Allow"
        Resource = "*"
      }
    ]
  })
}

# Add SSM access policy for secrets and config updates
resource "aws_iam_role_policy" "ecs_ssm_policy" {
  name = "overture-statistic-ssm-policy"
  role = aws_iam_role.ecs_task_execution_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action   = ["ssm:GetParameters", "ssm:PutParameter", "kms:Decrypt"]
        Effect   = "Allow"
        Resource = "*"
      }
    ]
  })
}

# Task Definition for Elasticsearch/Kibana
resource "aws_ecs_task_definition" "elasticsearch" {
  family                   = "elasticsearch"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "2048"
  memory                   = "4096"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_execution_role.arn # Need this to run SSM commands from within container

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "ARM64"
  }

  volume {
    name = "es_data"
    efs_volume_configuration {
      file_system_id     = aws_efs_file_system.elasticsearch_data.id
      transit_encryption = "ENABLED"
      authorization_config {
        access_point_id = aws_efs_access_point.elasticsearch.id
        iam             = "ENABLED"
      }
    }
  }

  container_definitions = jsonencode([
    {
      name  = "elasticsearch"
      image = "${aws_ecr_repository.elasticsearch.repository_url}:8.17.0"
      environment = [
        { name = "discovery.type", value = "single-node" },
        { name = "xpack.security.enabled", value = "true" },
        { name = "ES_JAVA_OPTS", value = "-Xms1g -Xmx1g" }
      ]
      secrets = [
        { name = "ELASTIC_PASSWORD", valueFrom = aws_ssm_parameter.elastic_password.arn }
      ]
      portMappings = [
        { containerPort = 9200, hostPort = 9200 }
      ]
      mountPoints = [
        { sourceVolume = "es_data", containerPath = "/usr/share/elasticsearch/data" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "elasticsearch"
        }
      }
    },
    {
      name  = "kibana"
      image = "docker.elastic.co/kibana/kibana:8.17.0"
      environment = [
        { name = "ELASTICSEARCH_HOSTS", value = "http://localhost:9200" },
        { name = "ELASTICSEARCH_USERNAME", value = "kibana_system" }
      ]
      secrets = [
        { name = "ELASTICSEARCH_PASSWORD", valueFrom = aws_ssm_parameter.kibana_password.arn }
      ]
      portMappings = [
        { containerPort = 5601, hostPort = 5601 }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "kibana"
        }
      }
    },
    {
      name      = "setup"
      image     = "alpine:latest"
      essential = false
      command = [
        "sh", "-c",
        "apk add --no-cache curl aws-cli; until curl -s -u elastic:$ELASTIC_PASSWORD http://localhost:9200 | grep -q \"tagline\"; do sleep 5; done; curl -s -X POST -u elastic:$ELASTIC_PASSWORD -H \"Content-Type: application/json\" http://localhost:9200/_security/user/kibana_system/_password -d \"{\\\"password\\\":\\\"$KIBANA_PASSWORD\\\"}\"; TOKEN=$(curl -s -X POST -u elastic:$ELASTIC_PASSWORD -H \"Content-Type: application/json\" http://localhost:9200/_security/api_key -d \"{\\\"name\\\":\\\"overture-apps\\\"}\" | grep -o '\"api_key\":\"[^\"]*' | cut -d'\"' -f4); if [ ! -z \"$TOKEN\" ]; then aws ssm put-parameter --name \"/overture/prod/elastic-token\" --value \"$TOKEN\" --type \"SecureString\" --overwrite --region eu-west-1; fi;"
      ]
      secrets = [
        { name = "ELASTIC_PASSWORD", valueFrom = aws_ssm_parameter.elastic_password.arn },
        { name = "KIBANA_PASSWORD", valueFrom = aws_ssm_parameter.kibana_password.arn }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "setup"
        }
      }
    }
  ])
}

# Task Definition for Overture Converter Batch (Standalone Task)
resource "aws_ecs_task_definition" "overture_batch" {
  family                   = "overture-converter-batch"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "2048"
  memory                   = "4096"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn            = aws_iam_role.ecs_task_role.arn
  ephemeral_storage {
    size_in_gib = 150
  }

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "ARM64"
  }

  container_definitions = jsonencode([
    {
      name  = "overture-converter-batch"
      image = "${aws_ecr_repository.overture_batch.repository_url}:latest"
      environment = [
        { name = "DEBUG", value = var.debug_enabled },
        { name = "SERVER_PORT", value = var.server_port },
        { name = "ELASTIC_BASE_URL", value = "elasticsearch.overture.local:9200" },
        { name = "KIBANA_URL", value = "kibana.overture.local:5601" },
        { name = "ELASTIC_CONNECT_TIMEOUT", value = var.elastic_connect_timeout },
        { name = "ELASTIC_SOCKET_TIMEOUT", value = var.elastic_socket_timeout },
        { name = "ELASTIC_REQUEST_TIMEOUT", value = var.elastic_request_timeout },
        { name = "HIKARI_MAX_POOL_SIZE", value = var.hikari_max_pool_size },
        { name = "S3_REGION", value = var.aws_region },
        { name = "BATCH_ENABLED_JOBS", value = var.batch_enabled_jobs },
        { name = "INPUT_BASE_PATH", value = var.input_base_path },
        { name = "DATA_VERSION", value = var.data_version },
        { name = "BATCH_CHUNK_SIZE", value = var.batch_chunk_size },
        { name = "BATCH_GRID_SIZE", value = var.batch_grid_size },
        { name = "BATCH_PARTITION_SIZE_ROWS", value = var.batch_partition_size_rows },
        { name = "JAVA_TOOL_OPTIONS", value = "-Djava.security.manager=allow" }
      ]
      secrets = [
        { name = "ELASTIC_TOKEN", valueFrom = aws_ssm_parameter.elastic_token.arn },
        { name = "LOG_LEVEL", valueFrom = aws_ssm_parameter.log_level.arn },
        { name = "S3_ACCESS_KEY", valueFrom = aws_ssm_parameter.s3_access_key.arn },
        { name = "S3_SECRET_KEY", valueFrom = aws_ssm_parameter.s3_secret_key.arn }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "overture-batch"
        }
      }
    }
  ])
}

# Service Discovery (Cloud Map)
resource "aws_service_discovery_private_dns_namespace" "main" {
  name        = "overture.local"
  description = "Service discovery for overture components"
  vpc         = aws_vpc.main.id
}

resource "aws_service_discovery_service" "elasticsearch" {
  name = "elasticsearch"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.main.id

    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

resource "aws_ecs_task_definition" "elasticsearch_proxy" {
  family                   = "elasticsearch-proxy"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "256"
  memory                   = "512"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "ARM64"
  }

  container_definitions = jsonencode([
    {
      name  = "nginx"
      image = "nginx:1.27-alpine"
      command = [
        "sh",
        "-c",
        "set -eu; cat <<'EOF' >/etc/nginx/conf.d/default.conf\nserver {\n  listen 8081;\n  client_max_body_size 100m;\n\n  location = /healthz {\n    access_log off;\n    default_type text/plain;\n    return 200 'ok';\n  }\n\n  location / {\n    proxy_pass http://elasticsearch.overture.local:9200;\n    proxy_http_version 1.1;\n    proxy_set_header Host elasticsearch.overture.local;\n    proxy_set_header Connection \"\";\n    proxy_set_header X-Real-IP $remote_addr;\n    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n    proxy_set_header X-Forwarded-Proto $scheme;\n    proxy_connect_timeout 30s;\n    proxy_send_timeout 60s;\n    proxy_read_timeout 60s;\n  }\n}\nEOF\nnginx -g 'daemon off;'"
      ]
      portMappings = [
        { containerPort = 8081, hostPort = 8081 }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "elasticsearch-proxy"
        }
      }
    }
  ])
}

# Task Definition for Overture Framework Extension App
resource "aws_ecs_task_definition" "overture_extension" {
  family                   = "overture-framework-extension-app"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "2048"
  memory                   = "4096"
  execution_role_arn       = aws_iam_role.ecs_task_execution_role.arn

  runtime_platform {
    operating_system_family = "LINUX"
    cpu_architecture        = "ARM64"
  }

  container_definitions = jsonencode([
    {
      name  = "overture-framework-extension-app"
      image = "${aws_ecr_repository.overture_extension.repository_url}:20260404-09fc740"
      command = [
        "sh", "-c",
        "until curl -s http://elasticsearch.overture.local:9200 > /dev/null; do echo 'Waiting for ES...'; sleep 5; done; java -jar app.jar"
      ]
      environment = [
        { name = "DEBUG", value = var.debug_enabled },
        { name = "SERVER_PORT", value = "8080" },
        { name = "ELASTIC_BASE_URL", value = "elasticsearch.overture.local:9200" },
        { name = "KIBANA_URL", value = "elasticsearch.overture.local:5601" },
        { name = "LOG_LEVEL", value = "INFO" }
      ]
      secrets = [
        { name = "ELASTIC_TOKEN", valueFrom = aws_ssm_parameter.elastic_token.arn }
      ]
      portMappings = [
        { containerPort = 8080, hostPort = 8080 }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.ecs_logs.name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "overture-extension"
        }
      }
    }
  ])
}

# ECS Services
resource "aws_ecs_service" "elasticsearch" {
  name                               = "elasticsearch-service"
  cluster                            = aws_ecs_cluster.main.id
  task_definition                    = aws_ecs_task_definition.elasticsearch.arn
  desired_count                      = 1
  launch_type                        = "FARGATE"
  deployment_minimum_healthy_percent = 0
  deployment_maximum_percent         = 100

  network_configuration {
    security_groups  = [aws_security_group.elasticsearch.id]
    subnets          = aws_subnet.public[*].id
    assign_public_ip = true
  }

  service_registries {
    registry_arn = aws_service_discovery_service.elasticsearch.arn
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.kibana.arn
    container_name   = "kibana"
    container_port   = 5601
  }

  depends_on = [aws_lb_listener.kibana_https]
}

# resource "aws_ecs_service" "overture_batch" {
#   name            = "overture-batch-service"
#   cluster         = aws_ecs_cluster.main.id
#   task_definition = aws_ecs_task_definition.overture_batch.arn
#   desired_count   = 0
#   launch_type     = "FARGATE"
#
#   network_configuration {
#     security_groups  = [aws_security_group.ecs_tasks.id]
#     subnets          = aws_subnet.public[*].id
#     assign_public_ip = true
#   }
#
#   lifecycle {
#     ignore_changes = [desired_count]
#   }
# }

resource "aws_ecs_service" "overture_extension" {
  name            = "overture-extension-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.overture_extension.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    security_groups  = [aws_security_group.ecs_tasks.id]
    subnets          = aws_subnet.public[*].id
    assign_public_ip = true
  }

  lifecycle {
    ignore_changes = [desired_count]
  }
}

resource "aws_ecs_service" "elasticsearch_proxy" {
  name            = "elasticsearch-proxy-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.elasticsearch_proxy.arn
  desired_count   = 0
  launch_type     = "FARGATE"

  network_configuration {
    security_groups  = [aws_security_group.elasticsearch_proxy.id]
    subnets          = aws_subnet.public[*].id
    assign_public_ip = true
  }
}
