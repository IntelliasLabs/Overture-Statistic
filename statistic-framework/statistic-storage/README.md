# [Statistic Storage](../statistic-storage)

This module is responsible for persisting geospatial features and their properties in Elasticsearch. 
It provides utilities for managing index templates, exposes a simple storage API and contains helpers for Kibana
integration.

## Key points

- Mapping Templates are created **automatically**
  by [`IndexTemplateCreator`](src/main/java/com/intellias/mobility/statistic/framework/templates/IndexTemplateCreator.java)
  for the geometry field(`geo_point` - for PointFeature, `geo_shape` - Line,
  Polygon etc.)
- When the main feature is saved, all its properties are saved in the other appropriate index **at the same time**, in
  order to have a separate 1-to-1 relationship between them (**key-single value**)
- Storage can also persist auxiliary derivative documents during the same save operation, which is
  how ingress now writes range-attribute documents alongside the source feature

## Index Template Architecture

Index templates are created on application startup by
[`IndexTemplateService`](src/main/java/com/intellias/mobility/statistic/framework/templates/IndexTemplateService.java).
Every bean that implements
[`IndexTemplateCreator`](src/main/java/com/intellias/mobility/statistic/framework/templates/IndexTemplateCreator.java)
is executed and can register templates via the
[`IndexTemplateManager`](src/main/java/com/intellias/mobility/statistic/framework/templates/IndexTemplateManager.java).

The module provides
[`DefaultIndexTemplateCreator`](src/main/java/com/intellias/mobility/statistic/framework/templates/DefaultIndexTemplateCreator.java)
which creates basic templates for all supported geometry types. To add your own templates simply declare another `IndexTemplateCreator` bean:

```java
@Component
class CustomTemplateCreator implements IndexTemplateCreator {
  @Override
  public void createTemplates(IndexTemplateManager manager) {
    manager.createIndexTemplate("my-template", "{...json...}");
  }
}
```

## Feature Properties

### [AbstractFeatureProperty](src/main/java/com/intellias/mobility/statistic/framework/property/model/AbstractFeatureProperty.java)

`AbstractFeatureProperty` is a **base model** representing a key-value property associated with a geospatial feature.  
It encapsulates metadata such as versioning, timestamps, and feature context, and is intended to be extended by more
geometry
specific property models.

#### 🧱 Fields

| Field          | Type     | Description                                               |
|----------------|----------|-----------------------------------------------------------|
| `key`          | `String` | The property key.                                         |
| `value`        | `String` | The property value.                                       |
| `version`      | `String` | Version of the feature this property is associated with.  |
| `timestamp`    | `Date`   | Timestamp when the property was recorded.                 |
| `featureDocId` | `String` | ID of the document (feature) this property belongs to.    |
| `featureType`  | `String` | Type of the feature (e.g., POI, ROAD, LANE, ADMIN, etc.). |

| 🔄 Supported Subtypes                                                     |
|---------------------------------------------------------------------------|
| [`PointFeatureProperty`](#PointFeatureProperty)                           |
| [`MultiPointFeatureProperty`](#MultiPointFeatureProperty)                 |
| [`LineFeatureProperty`](#LineFeatureProperty)                             |
| [`MultiLineFeatureProperty`](#MultiLineFeatureProperty)                   |
| [`PolygonFeatureProperty`](#PolygonFeatureProperty)                       |
| [`MultiPolygonFeatureProperty`](#MultiPolygonFeatureProperty)             |
| [`GeometryCollectionFeatureProperty`](#GeometryCollectionFeatureProperty) |

---

### [PointFeatureProperty](src/main/java/com/intellias/mobility/statistic/framework/property/model/PointFeatureProperty.java)

`PointFeatureProperty` extends [`AbstractFeatureProperty`](#AbstractFeatureProperty)  
to associate a **specific point geometry** with the feature property.

#### 🧱 Fields

| Field      | Type            | Description                    |
|------------|-----------------|--------------------------------|
| `geometry` | `PointGeometry` | The associated point geometry. |

---

### [MultiPointFeatureProperty](src/main/java/com/intellias/mobility/statistic/framework/property/model/MultiPointFeatureProperty.java)

`MultiPointFeatureProperty` extends [`AbstractFeatureProperty`](#AbstractFeatureProperty)  
to associate a **multi-point geometry** with the feature property.

#### 🧱 Fields

| Field      | Type                 | Description                          |
|------------|----------------------|--------------------------------------|
| `geometry` | `MultiPointGeometry` | The associated multi-point geometry. |

---

### [LineFeatureProperty](src/main/java/com/intellias/mobility/statistic/framework/property/model/LineFeatureProperty.java)

`LineFeatureProperty` extends [`AbstractFeatureProperty`](#AbstractFeatureProperty)  
to associate a **line geometry** with the feature property.

#### 🧱 Fields

| Field      | Type           | Description                   |
|------------|----------------|-------------------------------|
| `geometry` | `LineGeometry` | The associated line geometry. |

---

### [MultiLineFeatureProperty](src/main/java/com/intellias/mobility/statistic/framework/property/model/MultiLineFeatureProperty.java)

`MultiLineFeatureProperty` extends [`AbstractFeatureProperty`](#AbstractFeatureProperty)  
to associate a **multi-line geometry** with the feature property.

#### 🧱 Fields

| Field      | Type                | Description                         |
|------------|---------------------|-------------------------------------|
| `geometry` | `MultiLineGeometry` | The associated multi-line geometry. |

---

### [PolygonFeatureProperty](src/main/java/com/intellias/mobility/statistic/framework/property/model/PolygonFeatureProperty.java)

`PolygonFeatureProperty` extends [`AbstractFeatureProperty`](#AbstractFeatureProperty)  
to associate a **polygon geometry** with the feature property.

#### 🧱 Fields

| Field      | Type              | Description                      |
|------------|-------------------|----------------------------------|
| `geometry` | `PolygonGeometry` | The associated polygon geometry. |

---

### [MultiPolygonFeatureProperty](src/main/java/com/intellias/mobility/statistic/framework/property/model/MultiPolygonFeatureProperty.java)

`MultiPolygonFeatureProperty` extends [`AbstractFeatureProperty`](#AbstractFeatureProperty)  
to associate a **multi-polygon geometry** with the feature property.

#### 🧱 Fields

| Field      | Type                   | Description                            |
|------------|------------------------|----------------------------------------|
| `geometry` | `MultiPolygonGeometry` | The associated multi-polygon geometry. |

---

### [GeometryCollectionFeatureProperty](src/main/java/com/intellias/mobility/statistic/framework/property/model/GeometryCollectionFeatureProperty.java)

`GeometryCollectionFeatureProperty` extends [`AbstractFeatureProperty`](#AbstractFeatureProperty)  
to associate a **geometry collection** with the feature property.

#### 🧱 Fields

| Field      | Type                          | Description                         |
|------------|-------------------------------|-------------------------------------|
| `geometry` | `StatisticGeometryCollection` | The associated geometry collection. |

---

### 🧪 Example JSON

```json
{
  "key": "property_key_example",
  "value": "property_value_example",
  "version": "1.0",
  "timestamp": "2025-04-08T14:30:00.000Z",
  "featureDocId": "feature_12345",
  "featureType": "POI",
  "geometry": {
    "type": "Point",
    "coordinates": [
      24.0199,
      49.8429
    ]
  }
}
```


## StorageService

The `StorageService` interface exposes methods to store and read
[`StatisticFeature`](../statistic-model/src/main/java/com/intellias/statistic/model/feature/StatisticFeature.java)
objects.

```java
public interface StorageService {
  void save(StatisticFeature feature, String indexNameSuffix);
  void saveAll(List<StatisticFeature> features, String indexNameSuffix);
  List<StatisticFeature> read(String indexName);
  List<StatisticFeature> read(String indexName, String version);
}
```

The interface also exposes overloads that accept
[`AuxiliaryDocumentWrite`](src/main/java/com/intellias/mobility/statistic/framework/storage/AuxiliaryDocumentWrite.java)
entries when a caller needs to persist derivative documents to additional indices during the same
save flow.

`StorageServiceImpl` handles the actual Elasticsearch interaction and is
auto-configured by
[`StorageConfiguration`](src/main/java/com/intellias/mobility/statistic/framework/storage/StorageConfiguration.java).
In a Spring application you can simply inject `StorageService` and call `save`:

```java
@RequiredArgsConstructor
class FeatureImporter {
  private final StorageService storageService;

  public void importFeature(StatisticFeature feature) {
    storageService.save(feature, "road");
  }
}
```

Ingress uses this same mechanism to persist
[`RangeDocument`](src/main/java/com/intellias/mobility/statistic/framework/range/RangeDocument.java)
instances into `rangeattribute-*` indices immediately after preprocessing line features.

## KibanaManager

`KibanaManager` is a lightweight helper used by the analytics module to create
or update Kibana saved objects (dashboards, data views, etc.). It wraps a REST
client and provides convenient methods for sending JSON payloads to Kibana's
content management API. While optional for simple storage operations, it is
required when generating dashboards programmatically.




