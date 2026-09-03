# [Statistic Ingres](../statistic-analytics)
Statistic Ingres hosts the ingress REST controller and a feature grabber used to receive data for the statistic framework. Its main job is to accept input data and store it in the appropriate index.


This module receives features in the **Framework Inter Model** format and stores
them into the proper indices. It exposes the REST API described in
`IngresControllerOpenApi` and can also be used programmatically via
`IngresService`.

Before any feature is stored, it is passed through a set of preprocessors. The
default ones cover all existing geometry types, but the architecture allows you
to register new preprocessors with ease. After preprocessing, ingress can also
materialize derivative documents that should be saved together with the source
feature.

## Usage

### Via REST API

Use the `/ingress/save` and `/ingress/save-all` endpoints (see`IngresControllerOpenApi`) to submit single features or collections.

### Via `IngresService`

When this module is added as a dependency you may inject
`IngresService` directly. It provides two main methods:

- `processAndStore(StatisticFeature feature, String indexName)` – preprocesses a single feature and saves it.
- `processAndStoreAll(List<? extends StatisticFeature<?>> features, String indexName)` – preprocesses and saves multiple features.

## PreProcessors

Each `PreProcessor` decides if it is applicable to a feature (`isApplicable`) and
then returns a processed version via `process`. `PreProcessService` runs all
registered preprocessors in sequence and then invokes any matching derivative
document materializers.

Line feature ingress now also creates `RangeDocument` entries in
`rangeattribute-*` indices during the same save flow. The
`RangeAttributeIndexTemplateCreator` bean remains part of the shared
`IndexTemplateCreator` startup path, so range indices still receive their
template automatically before first write.

Operational note: environments with historical line features ingested before
this change need a one-time backfill into `rangeattribute-*` indices. New
ingress traffic is populated automatically; old data must be reingested or
migrated separately.

Predefined implementations include:
- [`LinePreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/LinePreProcessor.java)
- calculate length of `LineFeature`
- extract geometry according to the range attribute and calculate its length
- collect all ranges' geometries into one `MultiLineFeature` and merge lines that share points
- [`MultiLinePreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/MultiLinePreProcessor.java)
  - calculate total length of all lines
- [`PointPreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/PointPreProcessor.java)
  - currently returns the same feature
- [`PolygonPreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/PolygonPreProcessor.java)
  - calculate area of polygon
- [`MultiPolygonPreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/MultiPolygonPreProcessor.java)
  - calculate total area of all polygons



The module that contains the ingress endpoint(see OpenAPI ingres-controller for detailed info) and grabber.
The main purpose of this module is to save features to the appropriate index. Before store each feature goes through its
corresponding preprocessor if it exists.

For example:

- [`LinePreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/LinePreProcessor.java)
  - calculate length of LineFeature
  - extract geometry according to the range attribute and calculate its length
  - collect all ranges' geometries into one MultiLineFeature also merge some lines if they have common points
- [`MultiLinePreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/MultiLinePreProcessor.java)
  - calculate total length of all lines
- [`PointPreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/PointPreProcessor.java)
  - currently return the same feature
- [`PolygonPreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/PolygonPreProcessor.java)
  - calculate area of polygon
- [`MultiPolygonPreProcessor`](src/main/java/com/intellias/mobility/statistic/framework/preprocess/impl/MultiPolygonPreProcessor.java)
  - calculate total area of all polygons
