# Overture Statistic Project: Developer and User Guide

## Table of Contents

- [1. Introduction](#1-introduction)
    - [1.1. Project Goal](#11-project-goal)
    - [1.2. Key Features](#12-key-features)
- [2. User Guide](#2-user-guide)
    - [2.1. System Requirements](#21-system-requirements)
    - [2.2. Getting Started: Running the Application](#22-getting-started-running-the-application)
    - [2.3. How to Use](#23-how-to-use)
- [3. Developer Guide](#3-developer-guide)
    - [3.1. Architecture Overview](#31-architecture-overview)
    - [3.2. Project Setup for Development](#32-project-setup-for-development)
    - [3.3. Core Components Deep Dive](#33-core-components-deep-dive)
    - [3.4. How to Add a New Converter Job](#34-how-to-add-a-new-converter-job)
    - [3.5. CI/CD Pipeline](#35-cicd-pipeline)
    - [3.6. Statistic Framework API](#36-statistic-framework-api)

---

## 1. Introduction

### 1.1. Project Goal

The main goal of the Overture-Statistic project is to read geographic data from the Overture Maps Foundation, convert it into a standard internal format, and save it for later use. The application is designed as a powerful batch processing system that can handle very large datasets efficiently.

It acts as a bridge, transforming raw Overture data into a structured model that is ready for storage, analysis, or integration with other systems like the `statistic-framework`.

### 1.2. Key Features

- **High-Performance Batch Processing**: The application uses a parallel, multi-threaded architecture to process large volumes of data quickly and reliably.
- **Extensible Converters**: A modular design makes it easy to add support for new Overture feature types (e.g., Buildings, Administrative Boundaries, Places, Roads). It provides a simple way to convert different data schemas into one unified model.
- **Flexible Data Sources**: It can read Overture data (in Parquet format) from both the local file system and cloud storage (AWS S3).
- **Robust and Resilient**: The system is built for stability. It includes features like configurable connection pools, timeouts, and an automatic retry mechanism with exponential backoff to handle temporary failures gracefully.
- **Developer-Friendly**: The codebase is organized with clear, reusable components. An abstract base class (`AbstractOvertureProcessor`) and a set of utility classes simplify the process of creating new data converters and performing common tasks.
- **Automated CI/CD**: A full Continuous Integration and Continuous Deployment (CI/CD) pipeline is set up to automatically build, test, and deploy the application, ensuring code quality and fast delivery.

---

## 2. User Guide

This section is for users who want to run the application to process Overture data.

### 2.1. System Requirements

To run the Overture-Statistic application, your system should meet the following requirements.

**Software:**
- **Docker Engine**: The application is packaged as a set of Docker containers. You will need Docker installed to run it.
- **Docker Compose**: Used to orchestrate the multi-container application environment. It is typically included with Docker Desktop.

**Hardware (Recommended):**
- **RAM**: A minimum of 8 GB of RAM is recommended, as this is crucial for the Elasticsearch container and the overall Docker environment. For processing very large datasets, 16 GB or more is advised to ensure smooth performance.
- **CPU**: To take full advantage of the parallel processing capabilities, it is recommended to allocate at least 4 CPU cores to your Docker environment.
- **Disk Space**: Ensure you have sufficient disk space to store the Overture Parquet files and the output data. The required space will depend on the size of your datasets.

### 2.2. Getting Started: Running the Application

The application and its dependencies (Elasticsearch, Kibana, MinIO) are managed using Docker Compose. Follow these steps to get everything running.

#### 2.2.1. Using Docker Compose

1.  **Configure Environment**: Before launching, make sure you have a `.env` file in the root of the project. You can copy the provided `.env.example` if it exists, or create one from scratch. See the section below for details on the required variables.
2.  **Start the Services**: Open a terminal in the project's root directory and run the following command:
    ```bash
    docker-compose up -d
    ```
    This command will download the necessary Docker images and start all the services in the background.
3.  **Monitor the Startup**: The initial setup, especially for Elasticsearch, can take a few minutes. You can check the logs to see the progress:
    ```bash
    docker-compose logs -f setup
    ```
    Once the `setup` container finishes successfully, the other services like Elasticsearch and Kibana will be available.
4.  **Stopping the Services**: To stop all running containers, use the command:
    ```bash
    docker-compose down
    ```

#### 2.2.2. Required Configuration (Environment Variables)

All configuration is managed through a `.env` file in the project root. Below is a list of the variables you can set.

| Environment Variable   | Description                                                                          | Default Value                |
|------------------------|--------------------------------------------------------------------------------------|------------------------------|
| `COMPOSE_PROJECT_NAME` | A namespace for Docker Compose to prevent conflicts.                                 | `overture-intelli-map-lab-2` |
| `ELASTIC_PASSWORD`     | Sets the password for the `elastic` superuser.                                       | `elastic`                    |
| `KIBANA_PASSWORD`      | Sets the password for the `kibana_system` user.                                      | `elastic`                    |
| `ELASTIC_TOKEN`        | A pre-generated Base64 API token for applications to communicate with Elasticsearch. | (long token string)          |
| `STACK_VERSION`        | The version of the Elastic Stack (Elasticsearch, Kibana) to use.                     | `8.17.0`                     |
| `CLUSTER_NAME`         | The name for the Elasticsearch cluster.                                              | `overture-intelli-map-lab-2` |
| `LICENSE`              | The type of Elastic license to use (`basic` or `trial`).                             | `basic`                      |
| `ES_PORT`              | The external port on your local machine to access Elasticsearch.                     | `9200`                       |
| `KIBANA_PORT`          | The external port on your local machine to access Kibana.                            | `5601`                       |
| `MEM_LIMIT`            | The memory limit for the Elasticsearch and Kibana containers.                        | `1073741824` (1 GB)          |

### 2.3. How to Use

This section describes how to prepare your data and run the conversion process.

#### 2.3.1. Preparing Input Data

The application can process Overture data from two sources: a local directory or Amazon S3. The system automatically detects the source from the path provided.

**Important Rule**: A single job can only process one type of Overture feature at a time. Therefore, the input folder (whether local or on S3) must contain `.parquet` files belonging to only **one feature theme**. For example, a folder should contain only `places` files or only `buildings` files, but not both.

*   **Exception**: The `BuildingJob` can process both `building` and `building_part` feature types together, as their structure is very similar.

**Option 1: Local Filesystem**

1.  Create a folder anywhere on your local machine.
2.  Place your Overture `.parquet` files inside this folder.
3.  Get the full path to the folder or a specific file (e.g., `C:/data/overture/buildings/`).

**Option 2: Amazon S3**

1.  Ensure your `.parquet` files are uploaded to an S3 bucket.
2.  The application uses credentials from the default AWS chain (e.g., environment variables, EC2 instance profile). Make sure the environment where the application runs has the necessary permissions to read from the bucket.
3.  Get the S3 URI for the folder or a specific file. **Note**: The S3 path must specify the feature type (e.g., `s3://overturemaps-us-west-2/release/2025-07-23.0/theme=places/type=place/`).

#### 2.3.2. Running a Conversion Job

Conversion jobs are configured and launched via the `application.properties` file.

1.  **Open `application.properties`**: Locate this file in the `overture-converter-batch/src/main/resources` directory.
2.  **Enable the Job**: Find the `batch.jobs.enabledJobs` property and set it to the name of the job you want to run (e.g., `BuildingJob`).

    ```properties
    # Example: Enable the job for processing buildings
    batch.jobs.enabledJobs=BuildingJob
    ```

3.  **Set Job Parameters**: For the job you enabled, you must provide the `inputPath` and the data `version`.

    -   `inputPath`: The full path to your data source (local or S3). It can be a path to a folder or a single `.parquet` file.
    -   `version`: A version string (e.g., `v2025-07-23.0`) that will be assigned to the converted data.

    ```properties
    # Example: Configure parameters for BuildingJob with a local path
    batch.jobs.parameters.BuildingJob.inputPath=file:///C:/data/overture/buildings/
    batch.jobs.parameters.BuildingJob.version=v2025-07-23.0

    # Example: Configure parameters for a different job with an S3 path
    # batch.jobs.parameters.PlacesJob.inputPath=s3://overturemaps-us-west-2/release/2025-07-23.0/theme=places/type=place/
    # batch.jobs.parameters.PlacesJob.version=v2025-07-23.0
    ```

4.  **Run the Application**: With the Docker services already running (`docker-compose up -d`), you can trigger the job by restarting the batch application container:

    ```bash
    docker-compose restart overture-converter-batch
    ```

The application will start, execute the configured job, and then shut down gracefully.

---

## 3. Developer Guide

This guide provides technical details for developers who need to extend, maintain, or contribute to the `overture-statistic` project.

### 3.1. Architecture Overview

The application is a Spring Batch-based system designed for high-performance, parallel processing of large-scale geospatial data from Overture Maps Foundation.

#### 3.1.1. Core Technologies

-   **Spring Boot**: Provides the core framework for building the standalone application.
-   **Spring Batch**: The backbone for robust, scalable batch processing. It manages jobs, steps, partitioning, and fault tolerance.
-   **Apache Parquet & Avro**: Used for reading Overture data, which is stored in the efficient columnar Parquet format with Avro schemas.
-   **JTS (Java Topology Suite)**: The standard library for handling and processing geospatial geometries in Java.
-   **AWS S3 SDK v2**: Enables seamless integration with Amazon S3 for reading data from cloud storage.
-   **Elasticsearch**: Serves as the data sink where converted and processed `StatisticFeature` objects are stored for analysis and visualization.

#### 3.1.2. High-Level Architecture

The data processing pipeline follows a classic ETL (Extract, Transform, Load) pattern, orchestrated by Spring Batch.

1.  **Job Triggering**: The application starts and checks the `application.properties` file for the `batch.jobs.enabledJobs` property. If a job name is specified (e.g., `BuildingJob`), the system prepares to launch it.

2.  **Dynamic Job Registration**: At startup, the `DynamicJobRegistrar` scans the application context for all beans that implement the `OvertureFeatureProcessor` interface. For each processor found, it dynamically creates and registers a complete Spring Batch `Job` instance. This makes the system highly extensible.

3.  **Partitioning (Manager Step)**: The job begins with a manager step orchestrated by `MultiResourcePartitioner`. This partitioner:
    -   Scans the `inputPath` (local or S3) for all `.parquet` files.
    -   Reads the metadata of each file to get the total row count without loading the data.
    -   Splits the total rows of each file into smaller chunks based on `batch.jobs.partition-size.rows`.
    -   Creates an `ExecutionContext` for each chunk, containing the file path, start row, and end row. This becomes a work unit.

4.  **Parallel Processing (Worker Steps)**: The manager step distributes these partitions to multiple worker steps, which run in parallel. The number of concurrent workers is controlled by `batch.jobs.grid-size`.

5.  **Data Reading (Extract)**: Each worker step uses a `GeoParquetItemReader` to read its assigned data slice. The reader opens the specified Parquet file, seeks to the `startRow`, and reads records sequentially until it reaches the `endRow`.

6.  **Data Processing (Transform)**: The records (`OvertureItem`) are passed to the corresponding `OvertureFeatureProcessor` (e.g., `BuildingItemProcessor`). This processor converts the raw Overture data model into the internal `StatisticFeature` model.

7.  **Data Writing (Load)**: The processed `StatisticFeature` objects are collected into chunks (sized by `batch.jobs.chunk-size`) and written to Elasticsearch by a shared `ItemWriter`.

#### 3.1.3. Parallel Processing Model (Partitioning)

The key to the application's performance is its partitioning strategy, implemented in `MultiResourcePartitioner`. This approach avoids the common bottleneck of having one worker process one large file.

-   **Row-Based Splitting**: Instead of splitting by file, the partitioner splits work by row count. This ensures that even a single, massive Parquet file can be processed by multiple threads simultaneously.
-   **Efficient Metadata Scan**: It gets the row count from the Parquet file's footer metadata, which is a very fast operation and avoids reading the entire file.
-   **Uniform Work Distribution**: This model ensures that all available threads in the pool (`grid-size`) are kept busy, maximizing resource utilization whether processing many small files or a few large ones.

#### 3.1.4. Dynamic Job Registration

The system is designed to be easily extensible without requiring manual Spring Batch configuration for new data types. This is achieved through two components:

-   `DynamicJobRegistrar`: A `BeanFactoryPostProcessor` that discovers all `OvertureFeatureProcessor` beans at startup.
-   `JobCreationFactory`: A factory that builds the necessary `Job` and `Step` beans based on definitions provided by the registrar.

To add support for a new Overture feature (e.g., "admins"), a developer only needs to:
1.  Create a new class `AdminsItemProcessor` that implements `OvertureFeatureProcessor`.
2.  Annotate it with `@Component`.

The registrar will automatically create and configure `AdminsJob`, `AdminsManagerStep`, and `AdminsWorkerStep`, wiring them together with the standard reader, writer, and partitioner.

#### 3.1.5. Fault Tolerance and Recovery

The application includes robust mechanisms to handle failures and ensure data integrity, configured in `JobCreationFactory` and implemented in the reader.

-   **Retry Logic**: Worker steps are configured to automatically retry operations on transient errors like network timeouts (`SocketTimeoutException`) or database connection issues (`DataAccessResourceFailureException`). It uses an exponential backoff policy to wait longer between retries, preventing system overload.
-   **Skip Logic**: If a record is malformed and causes a non-recoverable error during processing (e.g., `ParseException`), the framework will skip that record and continue with the next one, up to a defined `skip-limit`.
-   **Restartability**: The `GeoParquetItemReader` and Spring Batch framework track the progress of each partition. If a job is interrupted and restarted, the reader will resume from the last successfully processed row (`currentRow`) within its assigned partition, preventing data loss or duplication.

### 3.2. Project Setup for Development

This section outlines the steps required to set up the project for local development and testing.

#### 3.2.1. Prerequisites

-   **Java JDK**: Version 17 or higher.
-   **Apache Maven**: For dependency management and building the project.
-   **Docker and Docker Compose**: For running the required services (Elasticsearch, Kibana).
-   **Git**: For cloning the repository.

#### 3.2.2. Building from Source

1.  Clone the repository from your source control system.
2.  Navigate to the project's root directory.
3.  Run the following Maven command to build the project and download all dependencies:

    ```bash
    mvn clean install
    ```

This will compile the code, run tests, and create the application JAR file in the `target` directory of the `overture-converter-batch` module.

#### 3.2.3. Running Tests

To run the unit and integration tests, execute the following command from the root directory:

```bash
mvn test
```

### 3.3. Core Components Deep Dive

This section provides a closer look at the key classes and interfaces that form the core of the application's logic.

#### 3.3.1. Overture Feature Processors (Converters)

The transformation logic is encapsulated within processors that implement the `OvertureFeatureProcessor` interface. This design makes the system modular and easy to extend.

-   **`OvertureFeatureProcessor<T>`**: A generic interface that defines the contract for all processors. It requires two methods:
    -   `process(OvertureItem item)`: The main transformation method.
    -   `getProcessorName()`: A unique name for the processor, used for dynamic job registration (e.g., `"Building"`).

-   **`AbstractOvertureProcessor<T>`**: An abstract class that provides common functionality for all processors, such as logging, error handling, and basic property mapping. Concrete processors should extend this class to reduce boilerplate code.

-   **`BuildingFeatureProcessor`**: A concrete implementation that handles the conversion of Overture's `building` and `building_part` features into the internal `StatisticFeature` model. It extracts relevant properties, computes statistics, and prepares the data for storage in Elasticsearch.

#### 3.3.2. Key Utility Classes

Several utility classes support the processors by encapsulating complex calculations and data manipulations.

-   **`FeatureComputationUtils`**: This class contains static methods for performing various calculations required for the statistic features. This may include calculating areas, validating properties, or deriving new attributes from the source data. Centralizing this logic here makes it reusable across different processors and easier to test.

-   **`StatisticGeometryExtractor`**: This utility is responsible for handling and converting geospatial data. It extracts JTS `Geometry` objects from the Overture data, performs necessary transformations (e.g., ensuring validity), and prepares them for the `StatisticFeature` model. It abstracts away the complexities of working with different geometry types.

### 3.4. How to Add a New Converter Job

This project uses **dynamic Spring Batch job registration**. This means you **do not need** to manually create configuration files for each new job. The entire infrastructure (`Job`, `Step`) is created automatically.

To add a new converter for any Overture feature type (e.g., `places`, `transportation`, etc.), you only need to perform one step: **create a new processor class**.

#### Step 1: Create a Processor Class

1.  Create a new Java class in the `com.intellias.mobility.statistic.batch.job.processor` package.
2.  This class must implement the `OvertureFeatureProcessor<O extends StatisticFeature>` interface, where:
    *   `O` is the feature type from your internal model you are converting to (e.g., `PointFeature`, `MultiPolygonFeature`).
3.  Implement two methods:
    *   `getProcessorName()`: Returns a unique name for your processor. A job will be created based on this name with the pattern `[processorName]Job` (e.g., `addressJob`).
    *   `process()`: Contains your core logic for converting a single `OvertureItem` record into your target feature.
4.  Annotate your class with `@Component` so Spring can discover it.

##### Example: Creating a Processor for the "Address" Feature

```java
package com.intellias.mobility.statistic.batch.job.processor;

// ... imports

@Component
@Slf4j
public class AddressItemProcessor implements OvertureFeatureProcessor<PointFeature> {

    public static final String PROCESSOR_NAME = "address";

    @Override
    public String getProcessorName() {
        return PROCESSOR_NAME;
    }

    @Override
    public PointFeature process(final OvertureItem item) {
        // ... conversion logic here ...
        return new PointFeature(...);
    }
}
```

#### Step 2: Configure and Enable the Job

After creating the processor, you must configure the new job in the `application.properties` file.

1.  **Enable the Job:** Add the name of your new job (e.g., `addressJob`) to the comma-separated list in `batch.jobs.enabledJobs`.
2.  **Set Job Parameters:** Specify the input file path for your new job using the pattern `batch.jobs.parameters.[JobName].inputPath` and version `batch.jobs.parameters.[JobName].version`

##### Example:

To run the `mockOvertureJob` and our new `addressJob`, the properties would look like this:

```properties
# Enable the jobs you want to run
batch.jobs.enabledJobs=addressJob

# Provide the input file path for each enabled job
batch.jobs.parameters.mockOvertureJob.path=s3://overture/kyiv_address.parquet
batch.jobs.parameters.addressJob.path=s3://overture/path/to/addresses.parquet
```

### 3.5. CI/CD Pipeline
The project uses CI/CD to automate building, testing, and publishing Docker images to CICD Container Registry.

#### Pipeline Stages:
1. Build & Test (build) – Compiles code, runs tests, and produces JAR artifacts.

2. Publish Docker Images (publish-image) – Builds Docker images for:
    - ```overture-converter-batch```
    - ```overture-framework-extension-app```

   and pushes them to the Container Registry.

#### Artifact Details
- Built JARs:
    - ```overture-converter-batch/target/overture-converter-batch-0.01.jar```
    - ```overture-framework-extension-app/target/overture-framework-extension-app-0.01.jar```

- Artifacts are stored for 7 days.

#### Manual Docker Image Publishing
Docker image publishing is manual for the main branch. Trigger  CI/CD Job via UI.

#### Running Docker Images from Container Registry

### 3.6. Statistic Framework API

The `overture-framework-extension-app` provides a REST API for advanced operations, such as running internal analytics jobs and managing Kibana dashboards.

All available endpoints are documented and can be tested via Swagger UI, which is accessible at:

[http://localhost:8101/swagger-ui/index.html](http://localhost:8101/swagger-ui/index.html)

This interface allows you to:
-   Execute internal analytics jobs like `mergeJob` and `featureDifferenceJob`.
-   Create and manage Kibana dashboards programmatically.
