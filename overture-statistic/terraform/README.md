# Overture Statistic - AWS Infrastructure

This directory contains the Infrastructure as Code (Terraform) for deploying the Overture Maps statistic framework on AWS.

## Architecture Overview

The infrastructure leverages Amazon ECS (Fargate) for serverless container orchestration, providing a secure and scalable environment for the following components:

- **Elasticsearch & Kibana**: Deployed as a single ECS task (sidecar pattern).
- **Overture Converter Batch**: A Spring Batch application for processing geographic data.
- **Overture Framework Extension App**: A web service for interacting with the processed data.

### Key Infrastructure Components:
- **Networking**: A dedicated VPC with public subnets, an internet gateway, and route tables.
- **Service Discovery**: AWS Cloud Map (`overture.local`) for internal communication between apps and Elasticsearch.
- **Container Registry**: Amazon ECR repositories for storing mirrored Docker images.
- **Configuration & Secrets**: AWS SSM Parameter Store for managing environment-specific settings and tokens.
- **Security**: Strict Security Groups with IP whitelisting for administrative access.

---

## What runs after deployment?

Once `terraform apply` is complete and the containers start:

1.  **Elasticsearch Service**: Running on port `9200` (internal access only).
2.  **Kibana Dashboard**: Running on port `5601`.
3.  **Overture Batch Service**: Deployed with **Desired Count = 0**. (See "Running the Batch Job" below).
4.  **Extension App Service**: Running on port `8080`, accessible via its Public IP.

---

## Running the Batch Job

The Overture Batch application is a one-time job that processes data and then exits. In AWS ECS, if a service has a desired count of 1, it will automatically restart the task whenever it finishes, creating an infinite loop.

To avoid this, the service is kept at **0** by default. When you want to run a processing job, you must trigger a manual task execution.

### Trigger via AWS CLI
Run the following command to start a single execution of the batch job:

```bash
aws ecs run-task \
       --region eu-west-1 \
       --cluster overture-statistic-cluster \
       --task-definition overture-converter-batch:19 \
       --launch-type FARGATE \
       --enable-execute-command \
       --network-configuration '{
         "awsvpcConfiguration": {
           "subnets": ["subnet-00954a2675c601874", "subnet-09975ff11cd877dbb"],
           "securityGroups": ["sg-0f5b05d8ae1b9e4f0"],
          "assignPublicIp": "ENABLED"
        }
      }'
```

*Note: Ensure the subnet and security group IDs match your current deployment.*

---

## How to Deploy

### Prerequisites
- AWS CLI configured with appropriate credentials.
- Terraform CLI installed.
- Docker running (for the initial image mirroring).

### 1. Mirror Docker Images
Before deploying, you must push the images from Docker Registry to ECR using the provided script:
```bash
# Ensure .env contains DOCKER_USER and DOCKER_TOKEN
bash terraform/mirror_images.sh
```

By default, the script:
- pulls `latest` for `overture-converter-batch` and `overture-framework-extension-app` from Docker Registry
- pushes both images to ECR with `<YYYYMMDD>-<short git hash>` as the tag
- mirrors Elasticsearch with its fixed version tag

To override the app source tag or target tag:
```bash
APP_SOURCE_TAG="latest" \
APP_TARGET_TAG="$(date +%Y%m%d)-$(git rev-parse --short HEAD)" \
ELASTICSEARCH_TAG="8.17.0" \
bash terraform/mirror_images.sh
```

### 2. Initialize Terraform
```bash
export $(grep -v '^#' .env | xargs)
cd terraform
terraform init \
  -backend-config="address=https://CICD.com/aws-infra-state" \
  -backend-config="username=${CICD_USER}" \
  -backend-config="password=${CICD_TOKEN}"
```

### 3. Deploy
```bash
terraform apply -var="admin_ips=[\"YOUR_IP/32\"]"
```

---

## How to Access Resources

### Administrative Access (Web)
To access Kibana or the Extension App, you must first whitelist your IP in the AWS Security Group or provide it during `terraform apply`.

1.  **Find Public IPs**:
    - Go to the **Amazon ECS Console**.
    - Select the `overture-statistic-cluster`.
    - Go to the **Tasks** tab.
    - Click on the task ID for `elasticsearch` (for Kibana) or `overture-framework-extension-app`.
    - Find the **Public IP** under the "Configuration" or "Networking" section.

2.  **Access Kibana**:
    - URL: `http://<PUBLIC_IP>:5601`
    - *Note: Default user is `kibana_system`.*

3.  **Access Extension App**:
    - URL: `http://<PUBLIC_IP>:8080`

### Updating Configuration
You can update passwords, tokens, or log levels without redeploying code by:
1.  Going to **AWS Systems Manager > Parameter Store**.
2.  Editing the values under `/overture/prod/`.
3.  Restarting the ECS tasks to pick up the new values.
