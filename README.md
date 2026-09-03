# Overture Statistics

This repository contains two related Java/Spring projects for processing, storing, and analyzing geospatial statistics.

## Projects

### [statistic-framework](statistic-framework)

`statistic-framework` is a reusable geospatial statistics platform. It provides the core model, ingestion, storage, analytics, and standalone application modules needed to work with GeoJSON-compatible spatial features.

It supports point, line, polygon, multi-geometry, and geometry collection features; stores data in Elasticsearch; preprocesses geometry metrics such as length and area; and provides analytics jobs and dashboard-ready outputs for comparing versions and tracking spatial data changes.

See [statistic-framework/README.md](statistic-framework/README.md) for architecture, modules, configuration, and usage details.

### [overture-statistic](overture-statistic)

`overture-statistic` is an Overture Maps data conversion and batch processing project. It reads Overture Maps Foundation data from local files or Amazon S3, converts Parquet-based source records into the internal statistic model, and writes the processed features for storage and analysis.

It is built around Spring Batch, supports parallel processing of large datasets, and is designed to be extended with new converters for additional Overture feature types such as buildings, places, roads, and administrative boundaries.

See [overture-statistic/README.md](overture-statistic/README.md) for setup, runtime configuration, conversion jobs, and developer guidance.

## How They Fit Together

`statistic-framework` provides the common geospatial model and statistics infrastructure. `overture-statistic` acts as a data pipeline that transforms raw Overture Maps data into that model so it can be stored, analyzed, and visualized through the framework.

## Quick Navigation

- [Statistic Framework guide](statistic-framework/README.md)
- [Overture Statistic guide](overture-statistic/README.md)
- [Statistic Framework application](statistic-framework/statistic-framework-app/README.md)
- [Overture Terraform resources](overture-statistic/terraform/README.md)
