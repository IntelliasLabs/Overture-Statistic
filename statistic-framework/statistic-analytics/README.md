# [Statistic Analytics](../statistic-analytics)

The **Statistic Analytics** module is the main component responsible for running
analytics jobs and preparing information for visualization dashboards. It
provides batch processes that scan feature indices, create difference reports,
merge data and query the derivative indices populated by ingress.

## API Documentation

This module provides comprehensive API documentation through **Swagger UI**. When the application is running, you can access the interactive API documentation at:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs/openapi.json`

The API documentation includes detailed information about all available endpoints, request/response formats, and example usage.

## Analytics Features

1) There is several additional job which you can run manually(for more detail see OpenAPI analytics-job-controller).
    - **Difference Job** - Compares two versions of the same data and highlights how features evolved. It groups features by their identifiers, detects added and deleted entries and marks modified ones so that dashboards can clearly show the changes between versions.
    - **Merge Job** - This job applies to all indexes where features are stored **(Currently TODO add)**. The main goal is to merge geometry and all properties of features with the same globalSourceId.

2) **Difference Dashboard**. There are some endpoints to create different visualization panels to show difference between two versions (see OpenAPI diff-dashboard-controller)
    - Feature Count Diff
    - Feature Property Types Count Diff
    - Feature Property Unique Values Count Diff
    - Range Attribute Types Count Diff
    - Range Attribute Unique Values Count Diff
    - Length(meters) Diff
    - Area Diff
    - Feature Count Diff Per Feature Property Type
    - Feature Count Diff Per Range Attribute Type
    - Changed Feature Count (You need to run a Difference Job before)
    - Added Feature Count (You need to run a Difference Job before)
    - Deleted Feature Count (You need to run a Difference Job before)
    - **All described above**

3) **Dashboard Creation**. There are some templates for visualization panels on Kibana and you can create each of them for your data. For example:
    - Count
    - Count and MultiCount Histogram
    - Geo and MultiGeo Map
    - MultiLength Histogram
4) **Statistic Dashboards**. There are some endpoints to create different types of dashboards, each displaying various visualizations of features. (see OpenAPI statistic-dashboard-controller) The available dashboards are:

    1. **Common Dashboard** – General statistics across all features.
    2. **Lines Dashboard** – Statistics specifically for line features.
    3. **Points Dashboard** – Statistics specifically for point features.
    4. **Polygons Dashboard** – Statistics specifically for polygon features.
    5. **Range Attributes** - Statistics specifically for range attributes.

   Available Visualizations:
    - **Feature Map** – Displays features on the map.
    - **Total Count** – Shows the total number of features.
    - **Pie by Feature Type** – Distribution of features by `featureType`.
    - **Table by Feature Type** – Shows the count of features per `featureType`.
    - **Sum of Length/Area (Sum)** – Displays the total sum of length or area.
    - **Maximum Length/Area (Max)** – Displays the maximum length or area.
    - **Minimum Length/Area (Min)** – Displays the minimum length or area.
    - **Average Length/Area (Average)** – Displays the average length or area.
    - **Pie by Attribute** – Distribution of attribute values for features.
    - **Table by Attribute** – Count of features by attribute.
    - **Table by Attribute for Feature Type** – Attributes distribution by feature type.
    - **Table by Attribute for Values** – Attributes distribution by unique values.

5) **Difference reports**. There are two endpoints to generate json diff between two versions report and save it into storage(for more detail see OpenAPI diff-report-controller)
   - Per Feature diff report
   - Per Feature Type diff report

**Don't forget to set** 
```properties
statistic-app.analytics.diff-report.diff-report-out-folder-path=/path/to/diff/reports
```

Supported output targets:
```properties
# Local filesystem
statistic-app.analytics.diff-report.diff-report-out-folder-path=/path/to/diff/reports

# S3
statistic-app.analytics.diff-report.diff-report-out-folder-path=s3://my-diff-report-bucket/reports
```

The diff-report export now selects the writer implementation from the configured path:
- paths starting with `s3://` use the S3 writer
- all other paths use the local filesystem writer

The writer abstraction is intentionally narrow so future backends can be added without changing `DiffReportService`. Candidate extensions include S3-compatible object stores such as MinIO, Google Cloud Storage, Azure Blob Storage, or shared mounted storage exposed as a local path.

## Analytics Job Definition and Execution

Jobs in this module are declared through the `AnalyticsJobDefinitionProvider` interface.
Each provider supplies the job name, description, a Spring Batch `Job` bean and
a template for job parameters. Providers are registered automatically when the
application starts.

Range attribute indices are no longer populated by an analytics job. Ingress
creates and stores the `rangeattribute-*` documents during line-feature saves,
and analytics dashboards continue to read those indices without query changes.

The `AnalyticsJobExecutor` keeps a registry of these providers. It exposes the
available jobs through REST endpoints, verifies that a job is not already
running with the same parameters and then delegates execution to Spring Batch.
This design allows new analytics jobs to be plugged in easily while ensuring
consistent execution and status reporting.
