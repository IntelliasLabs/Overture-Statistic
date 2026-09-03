# Statistic Framework App

## Overview

The **Statistic Framework App** is a standalone Spring Boot application that runs all modules of the Statistic Framework as a single application. It provides an easy way to deploy the entire framework as a standalone service without having to integrate it into another application.

## Features

- Runs all Statistic Framework modules in a single application
- Provides REST endpoints for data ingestion and analytics
- Configurable through application properties
- Can be deployed as a JAR or Docker container

## Getting Started

### Prerequisites

- Java 21+
- Docker (optional)
- Elasticsearch/Kibana
  - Install it directly
  - Or use the preconfigured docker-compose: [compose.yaml](../compose.yaml)

### Configuration

The application can be configured through [application.properties](src/main/resources/application.properties). Key configuration properties include:

```properties
# Elasticsearch configuration
statistic-app.elastic.host=localhost
statistic-app.elastic.port=9201
statistic-app.elastic.token=YOUR_API_KEY

# Storage configuration
statistic-app.storage.index-prefix=statistic

# Analytics configuration
statistic-app.analytics.diff-report.diff-report-out-folder-path=/path/to/diff/reports

# Or write diff reports to S3
statistic-app.analytics.diff-report.diff-report-out-folder-path=s3://my-diff-report-bucket/reports
```

### Running the Application

#### Option 1: Using Java

```bash
# Build the project
mvn clean package

# Run the application
java -jar target/statistic-framework-app-0.1.jar
```

#### Option 2: Using Docker

```bash
# Build the Docker image
docker build -f Dockerfile -t statistic-framework .

# Run the Docker container
docker run -p 8080:8080 statistic-framework
```

## API Endpoints

The application exposes various REST endpoints for data ingestion and analytics. These endpoints are provided by the statistic-ingres and statistic-analytics modules. Refer to their respective documentation for details:

- [Statistic Ingres API](../statistic-ingres/README.md)
- [Statistic Analytics API](../statistic-analytics/README.md)

## Architecture

The Statistic Framework App integrates all the modules of the Statistic Framework:

- **statistic-model**: Core data models
- **statistic-ingres**: Data ingestion
- **statistic-storage**: Elasticsearch communication
- **statistic-analytics**: Analytics capabilities
- **statistic-framework**: Aggregator module

This integration allows the application to provide a complete solution for geospatial data processing and analysis.
