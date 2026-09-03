#!/bin/bash
# Mirror images from CICD Container Registry to Amazon ECR

# Load environment variables from .env if it exists
if [ -f "$(dirname "$0")/../.env" ]; then
    export $(grep -v '^#' "$(dirname "$0")/../.env" | xargs)
fi

# Exit on error
set -e

DOCKER_REGISTRY="registry.CICD.com/overture-statistic"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION="eu-west-1"
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
APP_SOURCE_TAG="${APP_SOURCE_TAG:-latest}"
APP_TARGET_TAG="${APP_TARGET_TAG:-$(date +%Y%m%d)-$(git -C "$(dirname "$0")/.." rev-parse --short HEAD)}"
ELASTICSEARCH_TAG="${ELASTICSEARCH_TAG:-8.17.0}"

# Login to DockerHub
if [ -z "$DOCKER_USER" ] || [ -z "$DOCKER_TOKEN" ]; then
    echo "Error: DOCKER_USER and DOCKER_TOKEN environment variables must be set for private registry access."
    exit 1
fi
echo "$DOCKER_TOKEN" | docker login registry.CICD.com -u "$DOCKER_USER" --password-stdin

# Login to ECR
aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}

mirror_image() {
    local source_repo=$1
    local dest_repo=$2
    local source_tag=$3
    local dest_tag=${4:-$3}

    echo "Mirroring ${source_repo}:${source_tag} to ${dest_repo}:${dest_tag}..."
    docker pull "${source_repo}:${source_tag}"
    docker tag "${source_repo}:${source_tag}" "${dest_repo}:${dest_tag}"
    docker push "${dest_repo}:${dest_tag}"
}

# Mirror Overture Converter Batch
mirror_image \
    "${DOCKER_REGISTRY}/overture-converter-batch" \
    "${ECR_REGISTRY}/overture-converter-batch" \
    "${APP_SOURCE_TAG}" \
    "${APP_TARGET_TAG}"

# Mirror Overture Framework Extension App
mirror_image \
    "${DOCKER_REGISTRY}/overture-framework-extension-app" \
    "${ECR_REGISTRY}/overture-framework-extension-app" \
    "${APP_SOURCE_TAG}" \
    "${APP_TARGET_TAG}"

# Mirror Elasticsearch (from official to our ECR for reliability)
mirror_image \
    "docker.elastic.co/elasticsearch/elasticsearch" \
    "${ECR_REGISTRY}/elasticsearch-custom" \
    "${ELASTICSEARCH_TAG}"
