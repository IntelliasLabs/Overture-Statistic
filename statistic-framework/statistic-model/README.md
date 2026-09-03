# [Statistic Model](../statistic-model)

This module contains all feature classes that can be used for statistic.

**Note**: The framework is designed to be embedded into other projects. When
included as a dependency you are free to extend existing feature models or even
introduce new ones. Other modules of the framework are able to handle such
extensions automatically when persisting data to Elasticsearch.

## [Geometry](src/main/java/com/intellias/statistic/model/geometry)

This package contains all geometry classes for models.

---

### [StatisticGeometry](src/main/java/com/intellias/statistic/model/geometry/StatisticGeometry.java)

`StatisticGeometry` is a **common geometry interface** designed to support polymorphic JSON serialization of various
geospatial geometry types, following the [GeoJSON](https://geojson.org/) specification.

It is the base interface for all geometry types like `Point`, `LineString`, `Polygon`, and their multi-variants.

#### 🔄 Supported Subtypes

| Type                   | Java Class                                                    |
|------------------------|---------------------------------------------------------------|
| `"Point"`              | [`PointGeometry`](#PointGeometry)                             |
| `"LineString"`         | [`LineGeometry`](#LineGeometry)                               |
| `"Polygon"`            | [`PolygonGeometry`](#PolygonGeometry)                         |
| `"MultiPoint"`         | [`MultiPointGeometry`](#MultiPointGeometry)                   |
| `"MultiLineString"`    | [`MultiLineGeometry`](#MultiLineGeometry)                     |
| `"MultiPolygon"`       | [`MultiPolygonGeometry`](#MultiPolygonGeometry)               |
| `"GeometryCollection"` | [`StatisticGeometryCollection`](#StatisticGeometryCollection) |

---

### [LonLat](src/main/java/com/intellias/statistic/model/geometry/LonLat.java)

`LonLat` is a simple data container representing a geographic coordinate pair: [**longitude**,**latitude**].

It is commonly used to construct or extract coordinates from geometry types like `PointGeometry`.

#### 📌 Fields

| Name  | Type     | Description     |
|-------|----------|-----------------|
| `lon` | `double` | Longitude value |
| `lat` | `double` | Latitude value  |

---

### [PointGeometry](src/main/java/com/intellias/statistic/model/geometry/PointGeometry.java)

`PointGeometry` represents a single geographic point defined by longitude and latitude.  
It implements the [`StatisticGeometry`](#StatisticGeometry)
interface and maps
to the `"Point"` type in the GeoJSON specification.

#### 📌 Fields

| Field         | Type           | Description                      |
|---------------|----------------|----------------------------------|
| `type`        | `String`       | Always `"Point"`                 |
| `coordinates` | `List<Double>` | Contains `[longitude, latitude]` |

#### 🧪 Example JSON

```json
{
  "type": "Point",
  "coordinates": [
    30.5238,
    50.4547
  ]
}
```

---

### [MultiPointGeometry](src/main/java/com/intellias/statistic/model/geometry/MultiPointGeometry.java)

`MultiPointGeometry` represents a **multi-point geometry**, where multiple geographic points are stored as an array of
coordinates.  
It implements the [`StatisticGeometry`](#StatisticGeometry)
interface and corresponds to the `"MultiPoint"` type in the GeoJSON specification.

#### 🧱 Fields

| Field         | Type                 | Description                                                                                  |
|---------------|----------------------|----------------------------------------------------------------------------------------------|
| `type`        | `String`             | Always `"MultiPoint"`                                                                        |
| `coordinates` | `List<List<Double>>` | A list of lists where each inner list contains `[longitude, latitude]` pairs for each point. |

#### 🧪 Example JSON

```json
{
  "type": "MultiPoint",
  "coordinates": [
    [
      30.5238,
      50.4547
    ],
    [
      24.0316,
      49.8409
    ]
  ]
}
```

---

### [LineGeometry](src/main/java/com/intellias/statistic/model/geometry/LineGeometry.java)

`LineGeometry` represents a **line geometry**, where multiple geographic points form a path (line).  
It implements the [`StatisticGeometry`](#StatisticGeometry)
interface and corresponds to the `"LineString"` type in the GeoJSON specification.

#### 🧱 Fields

| Field         | Type                 | Description                                                                                  |
|---------------|----------------------|----------------------------------------------------------------------------------------------|
| `type`        | `String`             | Always `"LineString"`                                                                        |
| `coordinates` | `List<List<Double>>` | A list of lists where each inner list contains `[longitude, latitude]` pairs for each point. |

#### 🧪 Example JSON

```json
{
  "type": "LineString",
  "coordinates": [
    [
      30.5238,
      50.4547
    ],
    [
      24.0316,
      49.8409
    ]
  ]
}

```

---

### [MultiLineGeometry](src/main/java/com/intellias/statistic/model/geometry/MultiLineGeometry.java)

`MultiLineGeometry` represents a **multi-line geometry**, where multiple line geometries are stored as an array of
coordinates.  
It implements the [`StatisticGeometry`](#StatisticGeometry)
interface and corresponds to the `"MultiLineString"` type in the GeoJSON specification.

#### 🧱 Fields

| Field         | Type                       | Description                                                                                                                                                                                                                                                |
|---------------|----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `type`        | `String`                   | Always `"MultiLineString"`                                                                                                                                                                                                                                 |
| `coordinates` | `List<List<List<Double>>>` | A list of lines, where each line is represented by a list of points. Each point is a pair of `[longitude, latitude]`. In total, this field contains multiple lines, each consisting of multiple points (each point defined by its longitude and latitude). |

#### 🧪 Example JSON

```json
{
  "type": "MultiLineString",
  "coordinates": [
    [
      [
        30.5238,
        50.4547
      ],
      [
        24.0316,
        49.8409
      ]
    ],
    [
      [
        24.1540,
        49.0215
      ],
      [
        30.7142,
        49.6130
      ]
    ]
  ]
}
```

---

### [PolygonGeometry](src/main/java/com/intellias/statistic/model/geometry/PolygonGeometry.java)

`PolygonGeometry` represents a **polygon geometry**, where a polygon is defined by an **outer ring** (the boundary of
the polygon) and **optional inner rings** (holes inside the polygon).  
It implements the [`StatisticGeometry`](#StatisticGeometry)
interface and corresponds to the `"Polygon"` type in the GeoJSON specification.

#### 🧱 Fields

| Field         | Type                       | Description                                                                                                                                                                                                                            |
|---------------|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `type`        | `String`                   | Always `"Polygon"`                                                                                                                                                                                                                     |
| `coordinates` | `List<List<List<Double>>>` | A list of rings, where the first list contains the outer ring (boundary) of the polygon and the subsequent lists represent inner rings (holes) of the polygon. Each ring is represented by a list of points (`[longitude, latitude]`). |

#### 🧪 Example JSON

```json
{
  "type": "Polygon",
  "coordinates": [
    [
      [
        26.7993,
        50.4971
      ],
      [
        26.7993,
        49.4436
      ],
      [
        30.9658,
        49.4436
      ],
      [
        30.9658,
        50.4971
      ],
      [
        26.7993,
        50.4971
      ]
    ],
    [
      [
        27.6021,
        50.1363
      ],
      [
        27.6021,
        49.8280
      ],
      [
        29.9889,
        49.8280
      ],
      [
        29.9889,
        50.1363
      ],
      [
        27.6021,
        50.1363
      ]
    ]
  ]
}
```

---

### [MultiPolygonGeometry](src/main/java/com/intellias/statistic/model/geometry/MultiPolygonGeometry.java)

`MultiPolygonGeometry` represents a **multi-polygon geometry**, where multiple polygons are stored as an array of
polygon coordinates.  
It implements the [`StatisticGeometry`](#StatisticGeometry)
interface and corresponds to the `"MultiPolygon"` type in the GeoJSON specification.

#### 🧱 Fields

| Field         | Type                             | Description                                                                                                                                                                                                                             |
|---------------|----------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `type`        | `String`                         | Always `"MultiPolygon"`                                                                                                                                                                                                                 |
| `coordinates` | `List<List<List<List<Double>>>>` | A list of polygons, where each polygon is represented by a list of rings. Each ring is a list of points (`[longitude, latitude]`). The first list is the outer ring, and subsequent lists represent inner rings (holes) in the polygon. |

#### 🧪 Example JSON

```json
{
  "type": "MultiPolygon",
  "coordinates": [
    [
      [
        [
          26.7993,
          50.4971
        ],
        [
          26.7993,
          49.4436
        ],
        [
          30.9658,
          49.4436
        ],
        [
          30.9658,
          50.4971
        ],
        [
          26.7993,
          50.4971
        ]
      ],
      [
        [
          27.6021,
          50.1363
        ],
        [
          27.6021,
          49.8280
        ],
        [
          29.9889,
          49.8280
        ],
        [
          29.9889,
          50.1363
        ],
        [
          27.6021,
          50.1363
        ]
      ]
    ],
    [
      [
        [
          26.8355,
          49.0402
        ],
        [
          26.8355,
          48.0913
        ],
        [
          30.9546,
          48.0913
        ],
        [
          30.9546,
          49.0402
        ],
        [
          26.8355,
          49.0402
        ]
      ]
    ]
  ]
}
```

---

### [StatisticGeometryCollection](src/main/java/com/intellias/statistic/model/geometry/StatisticGeometryCollection.java)

`StatisticGeometryCollection` represents a **collection of geometries** of different types, adhering to the
GeoJSON `"GeometryCollection"` type.  
It implements the [`StatisticGeometry`](#StatisticGeometry)
interface and maps to the `"GeometryCollection"` type in the GeoJSON specification.

#### 🧱 Fields

| Field        | Type                      | Description                                                                                         |
|--------------|---------------------------|-----------------------------------------------------------------------------------------------------|
| `type`       | `String`                  | Always `"GeometryCollection"`                                                                       |
| `geometries` | `List<StatisticGeometry>` | A list of geometries that can be of different types, such as `Point`, `LineString`, `Polygon`, etc. |

#### 🧪 Example JSON

```json
{
  "type": "GeometryCollection",
  "geometries": [
    {
      "type": "Point",
      "coordinates": [
        30.5238,
        50.4547
      ]
    },
    {
      "type": "LineString",
      "coordinates": [
        [
          30.5238,
          50.4547
        ],
        [
          24.0316,
          49.8409
        ]
      ]
    }
  ]
}
```

---

## [Attribute](src/main/java/com/intellias/statistic/model/attribute)

This package contains classes for representing range attribute with length and corresponding geometry.

---

### [Range](src/main/java/com/intellias/statistic/model/attribute/Range.java)

`Range` represents a **range of a geometry feature** within a specified interval, typically normalized to a range
between `0.0` and `1.0`.  
It is used to define a **segment of the original feature geometry**, along with its corresponding **length** and the
actual geometry representation of the segment.

#### 🧱 Fields

| Field          | Type                            | Description                                                                                           |
|----------------|---------------------------------|-------------------------------------------------------------------------------------------------------|
| `start`        | `double`                        | The start of the range, typically in the normalized range `[0.0, 1.0]`                                |
| `end`          | `double`                        | The end of the range, typically in the normalized range `[0.0, 1.0]`                                  |
| `lengthMeters` | `double`                        | The length of the segment in meters (optional, calculated based on the segment)                       |
| `geometry`     | [`LineGeometry`](#LineGeometry) | A `LineGeometry` object representing the segment of the original geometry corresponding to this range |

---

### [RangeAttributeValue](src/main/java/com/intellias/statistic/model/attribute/RangeAttributeValue.java)

`RangeAttributeValue` represents an **attribute value** associated with a **range** of geometries, which are typically
segments of a feature geometry.  
It contains a list of ranges, the total length of the geometry, and the `MultiLineGeometry` representing the range
lines.

#### 🧱 Fields

| Field          | Type                                      | Description                                                                                                           |
|----------------|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `value`        | `String`                                  | The attribute value associated with the range (For example: "Kyivska Street")                                         |
| `ranges`       | `List<Range>`                             | A list of [`Range`](#Range) objects that define the specific segments of the original geometry                        |
| `lengthMeters` | `Double`                                  | The total length of the `MultiLineGeometry` in meters, which represents the combined length of the range segments     |
| `geometry`     | [`MultiLineGeometry`](#MultiLineGeometry) | A `MultiLineGeometry` object representing the geometry of the range lines (multiple segments of the feature geometry) |

---

### [RangeAttribute](src/main/java/com/intellias/statistic/model/attribute/RangeAttribute.java)

`RangeAttribute` represents an **attributes** with a **key** and associated **range values**. It stores the geometries
for all the range lines and the total length of the geometries.  
This class is typically used for modeling attribute with multiple values and they in turn with multiple ranges and their
corresponding geometries for the same value.

#### 🧱 Fields

| Field          | Type                        | Description                                                                                                                   |
|----------------|-----------------------------|-------------------------------------------------------------------------------------------------------------------------------|
| `key`          | `String`                    | The key or identifier associated with the range attribute (e.g., a name or label for the attribute. For example: "ROAD_NAME") |
| `values`       | `List<RangeAttributeValue>` | A list of [`RangeAttributeValue`](#RangeAttributeValue) objects                                                               |
| `lengthMeters` | `Double`                    | The total length of the `MultiLineGeometry` in meters, which represents the combined length of the range geometries           |
| `geometry`     | `MultiLineGeometry`         | A `MultiLineGeometry` representing all the range lines for the range attribute                                                |

#### 🧪 Example JSON

```json
{
  "key": "STREET_NAME",
  "values": [
    {
      "value": "Horodotska Street",
      "ranges": [
        {
          "start": 0.0,
          "end": 0.4,
          "lengthMeters": 67.0,
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [
                24.0208,
                49.8430
              ],
              [
                24.0199,
                49.8429
              ]
            ]
          }
        },
        {
          "start": 0.5,
          "end": 1.0,
          "lengthMeters": 75.0,
          "geometry": {
            "type": "LineString",
            "coordinates": [
              [
                24.0197,
                49.8429
              ],
              [
                24.0187,
                49.8428
              ]
            ]
          }
        }
      ],
      "lengthMeters": 142.0,
      "geometry": {
        "type": "MultiLineString",
        "coordinates": [
          [
            [
              24.0208,
              49.8430
            ],
            [
              24.0199,
              49.8429
            ]
          ],
          [
            [
              24.0197,
              49.8429
            ],
            [
              24.0187,
              49.8428
            ]
          ]
        ]
      }
    }
  ],
  "lengthMeters": 142.0,
  "geometry": {
    "type": "MultiLineString",
    "coordinates": [
      [
        [
          24.0208,
          49.8430
        ],
        [
          24.0199,
          49.8429
        ]
      ],
      [
        [
          24.0197,
          49.8429
        ],
        [
          24.0187,
          49.8428
        ]
      ]
    ]
  }
}
```

## [Feature](src/main/java/com/intellias/statistic/model/feature)

This package contains all feature and their properties classes.

---

### [FeatureProperty](src/main/java/com/intellias/statistic/model/feature/FeatureProperty.java)

`FeatureProperty` represents a **single property** associated with a feature, containing a `key` and a list of
associated `values`.  
It is used to store additional information about a feature in the context of the feature's attributes.

#### 🧱 Fields

| Field    | Type           | Description                                                                                             |
|----------|----------------|---------------------------------------------------------------------------------------------------------|
| `key`    | `String`       | The key or name of the property.                                                                        |
| `values` | `List<String>` | A list of values associated with the given `key`. This can store multiple values for a single property. |

---

### [StatisticFeatureProperties](src/main/java/com/intellias/statistic/model/feature/StatisticFeatureProperties.java)

`StatisticFeatureProperties` is an abstract class that encapsulates the metadata and extra properties for features.  
It holds the essential details about the feature's version, type, timestamp, and additional properties.

#### 🧱 Fields

| Field               | Type                    | Description                                                                                       |
|---------------------|-------------------------|---------------------------------------------------------------------------------------------------|
| `version`           | `String`                | The version of the feature delivery.                                                              |
| `featureType`       | `String`                | The type of the feature (e.g., POI, ROAD, LANE, ADMIN).                                           |
| `timestamp`         | `String`                | The timestamp when the feature was delivered, in conjunction with the `version` field.            |
| `featureProperties` | `List<FeatureProperty>` | A list of [`FeatureProperty`](#FeatureProperty) related to the feature, which can vary in format. |

| 🔄 Supported Subtypes                                                         |
|-------------------------------------------------------------------------------|
| [`PointFeatureProperties`](#PointFeatureProperties)                           |
| [`LineFeatureProperties`](#LineFeatureProperties)                             |
| [`PolygonFeatureProperties`](#PolygonFeatureProperties)                       |
| [`GeometryCollectionFeatureProperties`](#GeometryCollectionFeatureProperties) |

#### 📌 Why Subtypes of `StatisticFeatureProperties`?

The `StatisticFeatureProperties` class is designed to encapsulate **common metadata** for geospatial features—such as
version, feature type, timestamp, and a list of additional attributes.

However, **different types of geometries**—such as points, lines, polygons, and geometry collections—often require
**additional, feature-specific attributes** to represent their spatial or analytical characteristics accurately.

To handle this variation in a clean and type-safe way, we introduce **dedicated subtypes**
of `StatisticFeatureProperties`. Each subtype captures the **unique attributes relevant to that specific geometry type
**:

| Subtype                               | Geometry Type          | Additional Attributes Captured        |
|---------------------------------------|------------------------|---------------------------------------|
| `PointFeatureProperties`              | Point                  | — _(inherits only common properties)_ |
| `LineFeatureProperties`               | Line / MultiLine       | `lengthMeters`, `rangeAttributes`     |
| `PolygonFeatureProperties`            | Polygon / MultiPolygon | `area`                                |
| `GeometryCollectionFeatureProperties` | GeometryCollection     | — _(inherits only common properties)_ |

---

### [PointFeatureProperties](src/main/java/com/intellias/statistic/model/feature/PointFeatureProperties.java)

`PointFeatureProperties` represents the **properties** associated with a **Point Feature**.  
It extends the [`StatisticFeatureProperties`](#StatisticFeatureProperties) class, inheriting common properties
like `version`, `featureType`, `timestamp`, and `featureProperties`. This class is specifically used for point features
in the context of statistical geometry.  
There are **no additional fields** beyond those inherited
from [`StatisticFeatureProperties`](#StatisticFeatureProperties).

---

### [LineFeatureProperties](src/main/java/com/intellias/statistic/model/feature/LineFeatureProperties.java)

`LineFeatureProperties` represents the **properties** associated with a **Line Feature**.  
It extends the [`StatisticFeatureProperties`](#StatisticFeatureProperties) class, inheriting common properties
like `version`, `featureType`, `timestamp`, and `featureProperties`. Additionally, it includes specific fields for line
features, such as `lengthMeters` and `rangeAttributes`.

#### 🧱 Fields

| Field             | Type                   | Description                                                                     |
|-------------------|------------------------|---------------------------------------------------------------------------------|
| `lengthMeters`    | `double`               | The length of the line feature in meters.                                       |
| `rangeAttributes` | `List<RangeAttribute>` | A list of [`RangeAttribute`](#RangeAttribute) associated with the line feature. |

---

### [PolygonFeatureProperties](src/main/java/com/intellias/statistic/model/feature/PolygonFeatureProperties.java)

`PolygonFeatureProperties` represents the **properties** associated with a **Polygon Feature**.  
It extends the [`StatisticFeatureProperties`](#StatisticFeatureProperties) class, inheriting common properties
like `version`, `featureType`, `timestamp`, and `featureProperties`. Additionally, it includes a specific field for
polygons, namely the `area` of the polygon.

#### 🧱 Fields

| Field  | Type     | Description                                       |
|--------|----------|---------------------------------------------------|
| `area` | `double` | The area of the polygon feature in square meters. |

---

### [GeometryCollectionFeatureProperties](src/main/java/com/intellias/statistic/model/feature/GeometryCollectionFeatureProperties.java)

`GeometryCollectionFeatureProperties` represents the **properties** associated with a **GeometryCollection Feature**.  
It extends the `StatisticFeatureProperties` class, inheriting common properties
like `version`, `featureType`, `timestamp`, and `featureProperties`.
There are **no additional fields** beyond those inherited
from [`StatisticFeatureProperties`](#StatisticFeatureProperties).

---

### [StatisticFeature](src/main/java/com/intellias/statistic/model/feature/StatisticFeature.java)

`StatisticFeature` is a **base feature interface** that defines a geospatial feature with associated geometry and
properties. It provides a foundation for various feature types that can be serialized and deserialized in JSON format,
following the GeoJSON format. The feature includes a unique identifier (`featureId`), geometry (`geometry`), and
properties (`properties`), with support for polymorphic serialization of the `geometry` and `properties`.

#### 🔄 Supported Subtypes

| Type        | Java Class                                                |
|-------------|-----------------------------------------------------------|
| `"Feature"` | [`PointFeature`](#PointFeature)                           |
| `"Feature"` | [`LineFeature`](#LineFeature)                             |
| `"Feature"` | [`PolygonFeature`](#PolygonFeature)                       |
| `"Feature"` | [`MultiPointFeature`](#MultiPointFeature)                 |
| `"Feature"` | [`MultiLineFeature`](#MultiLineFeature)                   |
| `"Feature"` | [`MultiPolygonFeature`](#MultiPolygonFeature)             |
| `"Feature"` | [`GeometryCollectionFeature`](#GeometryCollectionFeature) |

---

### [PointFeature](src/main/java/com/intellias/statistic/model/feature/PointFeature.java)

`PointFeature` represents a **geospatial feature** with **point geometry**.  
It implements the [`StatisticFeature`](#StatisticFeature) interface and maps to the `"Feature"` type in the GeoJSON
specification, specifically for point features.

#### 🧱 Fields

| Field        | Type                                                | Description                                                                                                  |
|--------------|-----------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| `featureId`  | `String`                                            | The unique identifier for the feature.                                                                       |
| `geometry`   | [`PointGeometry`](#PointGeometry)                   | The geometry of the feature, represented by a `PointGeometry` object.                                        |
| `properties` | [`PointFeatureProperties`](#PointFeatureProperties) | The properties associated with the point feature. This includes information like version, feature type, etc. |

#### 🧪 Example JSON

```json
{
  "featureId": "point-1",
  "type": "Feature",
  "geometry": {
    "type": "Point",
    "coordinates": [
      24.0199,
      49.8429
    ]
  },
  "properties": {
    "version": "1.0",
    "featureType": "PointFeature",
    "timestamp": "2025-04-06T12:30:00.000+0000",
    "featureProperties": [
      {
        "key": "property1",
        "values": [
          "value1"
        ]
      }
    ]
  }
}
```

---

### [MultiPointFeature](src/main/java/com/intellias/statistic/model/feature/MultiPointFeature.java)

`MultiPointFeature` represents a **geospatial feature** with **multi-point geometry**.  
It implements the [`StatisticFeature`](#StatisticFeature) interface and maps to the `"Feature"` type in the GeoJSON
specification, specifically for multi-point features.

#### 🧱 Fields

| Field        | Type                                                | Description                                                                                                        |
|--------------|-----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| `featureId`  | `String`                                            | The unique identifier for the feature.                                                                             |
| `geometry`   | [`MultiPointGeometry`](#MultiPointGeometry)         | The geometry of the feature, represented by a `MultiPointGeometry` object.                                         |
| `properties` | [`PointFeatureProperties`](#PointFeatureProperties) | The properties associated with the multi-point feature. This includes information like version, feature type, etc. |

#### 🧪 Example JSON

```json
{
  "featureId": "multi-point-1",
  "type": "Feature",
  "geometry": {
    "type": "MultiPoint",
    "coordinates": [
      [
        24.0199,
        49.8429
      ],
      [
        24.0199,
        49.8429
      ]
    ]
  },
  "properties": {
    "version": "1.0",
    "featureType": "MultiPointFeature",
    "timestamp": "2025-04-06T12:30:00.000+0000",
    "featureProperties": [
      {
        "key": "property1",
        "values": [
          "value1"
        ]
      }
    ]
  }
}
```

---

### [LineFeature](src/main/java/com/intellias/statistic/model/feature/LineFeature.java)

`LineFeature` represents a **geospatial feature** with **line geometry**.  
It implements the [`StatisticFeature`](#StatisticFeature) interface and maps to the `"Feature"` type in the GeoJSON
specification, specifically for line features.

#### 🧱 Fields

| Field        | Type                                              | Description                                                                                                 |
|--------------|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| `featureId`  | `String`                                          | The unique identifier for the feature.                                                                      |
| `geometry`   | [`LineGeometry`](#LineGeometry)                   | The geometry of the feature, represented by a `LineGeometry` object.                                        |
| `properties` | [`LineFeatureProperties`](#LineFeatureProperties) | The properties associated with the line feature. This includes information like version, feature type, etc. |

#### 🧪 Example JSON

```json
{
  "featureId": "line-1",
  "type": "Feature",
  "geometry": {
    "type": "LineString",
    "coordinates": [
      [
        24.0202,
        49.8429
      ],
      [
        24.0187,
        49.8428
      ]
    ]
  },
  "properties": {
    "version": "1.0",
    "featureType": "LineFeature",
    "timestamp": "2025-04-06T12:30:00.000+0000",
    "featureProperties": [
      {
        "key": "property1",
        "values": [
          "value1"
        ]
      }
    ],
    "lengthMeters": 112.5,
    "rangeAttributes": [
      {
        "key": "STREET_NAME",
        "values": [
          {
            "value": "Horodotska Street",
            "ranges": [
              {
                "start": 0.0,
                "end": 0.4,
                "lengthMeters": 40.0,
                "geometry": {
                  "type": "LineString",
                  "coordinates": [
                    [
                      24.0208,
                      49.8430
                    ],
                    [
                      24.0199,
                      49.8429
                    ]
                  ]
                }
              },
              {
                "start": 0.5,
                "end": 1.0,
                "lengthMeters": 60.0,
                "geometry": {
                  "type": "LineString",
                  "coordinates": [
                    [
                      24.0197,
                      49.8429
                    ],
                    [
                      24.0187,
                      49.8428
                    ]
                  ]
                }
              }
            ],
            "lengthMeters": 100.0,
            "geometry": {
              "type": "MultiLineString",
              "coordinates": [
                [
                  [
                    24.0208,
                    49.8430
                  ],
                  [
                    24.0199,
                    49.8429
                  ]
                ],
                [
                  [
                    24.0197,
                    49.8429
                  ],
                  [
                    24.0187,
                    49.8428
                  ]
                ]
              ]
            }
          }
        ],
        "lengthMeters": 100.0,
        "geometry": {
          "type": "MultiLineString",
          "coordinates": [
            [
              [
                24.0208,
                49.8430
              ],
              [
                24.0199,
                49.8429
              ]
            ],
            [
              [
                24.0197,
                49.8429
              ],
              [
                24.0187,
                49.8428
              ]
            ]
          ]
        }
      }
    ]
  }
}
```

---

### [MultiLineFeature](src/main/java/com/intellias/statistic/model/feature/MultiLineFeature.java)

`MultiLineFeature` represents a **geospatial feature** with **multi-line geometry**.  
It implements the [`StatisticFeature`](#StatisticFeature) interface and maps to the `"Feature"` type in the GeoJSON
specification, specifically for multi-line features.

#### 🧱 Fields

| Field        | Type                                              | Description                                                                                                       |
|--------------|---------------------------------------------------|-------------------------------------------------------------------------------------------------------------------|
| `featureId`  | `String`                                          | The unique identifier for the feature.                                                                            |
| `geometry`   | [`MultiLineGeometry`](#MultiLineGeometry)         | The geometry of the feature, represented by a `MultiLineGeometry` object.                                         |
| `properties` | [`LineFeatureProperties`](#LineFeatureProperties) | The properties associated with the multi-line feature. This includes information like version, feature type, etc. |

#### 🧪 Example JSON

```json
{
  "featureId": "multi-line-1",
  "geometry": {
    "type": "MultiLineString",
    "coordinates": [
      [
        [
          30.5238,
          50.4547
        ],
        [
          24.0316,
          49.8409
        ]
      ],
      [
        [
          24.1540,
          49.0215
        ],
        [
          30.7142,
          49.6130
        ]
      ]
    ]
  },
  "properties": {
    "version": "1.0",
    "featureType": "MultiLineFeature",
    "timestamp": "2025-04-06T12:30:00.000+0000",
    "featureProperties": [
      {
        "key": "property1",
        "values": [
          "value1"
        ]
      }
    ]
  },
  "lengthMeters": 170.5,
  "rangeAttributes": [
    {
      "key": "STREET_NAME",
      "values": [
        {
          "value": "Horodotska Street",
          "ranges": [
            {
              "start": 0.0,
              "end": 0.4,
              "lengthMeters": 40.0,
              "geometry": {
                "type": "LineString",
                "coordinates": [
                  [
                    24.0208,
                    49.8430
                  ],
                  [
                    24.0199,
                    49.8429
                  ]
                ]
              }
            },
            {
              "start": 0.5,
              "end": 1.0,
              "lengthMeters": 60.0,
              "geometry": {
                "type": "LineString",
                "coordinates": [
                  [
                    24.0197,
                    49.8429
                  ],
                  [
                    24.0187,
                    49.8428
                  ]
                ]
              }
            }
          ],
          "lengthMeters": 100.0,
          "geometry": {
            "type": "MultiLineString",
            "coordinates": [
              [
                [
                  24.0208,
                  49.8430
                ],
                [
                  24.0199,
                  49.8429
                ]
              ],
              [
                [
                  24.0197,
                  49.8429
                ],
                [
                  24.0187,
                  49.8428
                ]
              ]
            ]
          }
        }
      ],
      "lengthMeters": 100.0,
      "geometry": {
        "type": "MultiLineString",
        "coordinates": [
          [
            [
              24.0208,
              49.8430
            ],
            [
              24.0199,
              49.8429
            ]
          ],
          [
            [
              24.0197,
              49.8429
            ],
            [
              24.0187,
              49.8428
            ]
          ]
        ]
      }
    }
  ]
}
```

---

### [PolygonFeature](src/main/java/com/intellias/statistic/model/feature/PolygonFeature.java)

`PolygonFeature` represents a **geospatial feature** with **polygon geometry**.  
It implements the [`StatisticFeature`](#StatisticFeature) interface and maps to the `"Feature"` type in the GeoJSON
specification, specifically for polygon features.

#### 🧱 Fields

| Field        | Type                                                    | Description                                                                                                    |
|--------------|---------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `featureId`  | `String`                                                | The unique identifier for the feature.                                                                         |
| `geometry`   | [`PolygonGeometry`](#PolygonGeometry)                   | The geometry of the feature, represented by a `PolygonGeometry` object.                                        |
| `properties` | [`PolygonFeatureProperties`](#PolygonFeatureProperties) | The properties associated with the polygon feature. This includes information like version, feature type, etc. |

#### 🧪 Example JSON

```json
{
  "featureId": "polygon-1",
  "type": "Feature",
  "geometry": {
    "type": "Polygon",
    "coordinates": [
      [
        [
          26.7993,
          50.4971
        ],
        [
          26.7993,
          49.4436
        ],
        [
          30.9658,
          49.4436
        ],
        [
          30.9658,
          50.4971
        ],
        [
          26.7993,
          50.4971
        ]
      ],
      [
        [
          27.6021,
          50.1363
        ],
        [
          27.6021,
          49.8280
        ],
        [
          29.9889,
          49.8280
        ],
        [
          29.9889,
          50.1363
        ],
        [
          27.6021,
          50.1363
        ]
      ]
    ]
  },
  "properties": {
    "version": "1.0",
    "featureType": "PolygonFeature",
    "timestamp": "2025-04-06T12:30:00.000+0000",
    "featureProperties": [
      {
        "key": "property1",
        "values": [
          "value1"
        ]
      }
    ],
    "area": 29121097872.68
  }
}
```

---

### [MultiPolygonFeature](src/main/java/com/intellias/statistic/model/feature/MultiPolygonFeature.java)

`MultiPolygonFeature` represents a **geospatial feature** with **multi-polygon geometry**.  
It implements the [`StatisticFeature`](#StatisticFeature) interface and maps to the `"Feature"` type in the GeoJSON
specification, specifically for multi-polygon features.

#### 🧱 Fields

| Field        | Type                                                    | Description                                                                                     |
|--------------|---------------------------------------------------------|-------------------------------------------------------------------------------------------------|
| `featureId`  | `String`                                                | The unique identifier for the feature.                                                          |
| `geometry`   | [`MultiPolygonGeometry`](#MultiPolygonGeometry)         | The geometry of the feature, represented by a `MultiPolygonGeometry` object.                    |
| `properties` | [`PolygonFeatureProperties`](#PolygonFeatureProperties) | The properties associated with the multi-polygon feature, including version, feature type, etc. |

#### 🧪 Example JSON

```json
{
  "featureId": "multipolygon-1",
  "type": "Feature",
  "geometry": {
    "type": "MultiPolygon",
    "coordinates": [
      [
        [
          [
            26.7993,
            50.4971
          ],
          [
            26.7993,
            49.4436
          ],
          [
            30.9658,
            49.4436
          ],
          [
            30.9658,
            50.4971
          ],
          [
            26.7993,
            50.4971
          ]
        ],
        [
          [
            27.6021,
            50.1363
          ],
          [
            27.6021,
            49.8280
          ],
          [
            29.9889,
            49.8280
          ],
          [
            29.9889,
            50.1363
          ],
          [
            27.6021,
            50.1363
          ]
        ]
      ],
      [
        [
          [
            26.8355,
            49.0402
          ],
          [
            26.8355,
            48.0913
          ],
          [
            30.9546,
            48.0913
          ],
          [
            30.9546,
            49.0402
          ],
          [
            26.8355,
            49.0402
          ]
        ]
      ]
    ]
  },
  "properties": {
    "version": "1.0",
    "featureType": "MultiPolygonFeature",
    "timestamp": "2025-04-06T12:30:00.000+0000",
    "featureProperties": [
      {
        "key": "property1",
        "values": [
          "value1"
        ]
      }
    ],
    "area": 61173519645.3
  }
}
```

---

### [GeometryCollectionFeature](src/main/java/com/intellias/statistic/model/feature/GeometryCollectionFeature.java)

`GeometryCollectionFeature` represents a **geospatial feature** with **geometry collection** geometry.  
It implements the [`StatisticFeature`](#StatisticFeature) interface and maps to the `"Feature"` type in the GeoJSON
specification, specifically for geometry collections.

#### 🧱 Fields

| Field        | Type                                                                          | Description                                                                         |
|--------------|-------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| `featureId`  | `String`                                                                      | The unique identifier for the feature.                                              |
| `geometry`   | [`StatisticGeometryCollection`](#StatisticGeometryCollection)                 | The geometry of the feature, represented by a `StatisticGeometryCollection` object. |
| `properties` | [`GeometryCollectionFeatureProperties`](#GeometryCollectionFeatureProperties) | The properties associated with the geometry collection feature.                     |

#### 🧪 Example JSON

```json
{
  "featureId": "geometryCollection-1",
  "type": "Feature",
  "geometry": {
    "type": "GeometryCollection",
    "geometries": [
      {
        "type": "Point",
        "coordinates": [
          24.0199,
          49.8429
        ]
      },
      {
        "type": "LineString",
        "coordinates": [
          [
            24.0202,
            49.8429
          ],
          [
            24.0187,
            49.8428
          ]
        ]
      }
    ]
  },
  "properties": {
    "version": "1.0",
    "featureType": "GeometryCollectionFeature",
    "timestamp": "2025-04-06T12:30:00.000+0000",
    "featureProperties": [
      {
        "key": "property1",
        "values": [
          "value1"
        ]
      }
    ]
  }
}
```

---

### [StatisticFeatureCollection](src/main/java/com/intellias/statistic/model/feature/StatisticFeatureCollection.java)

`StatisticFeatureCollection` represents a **collection of features** that are grouped together, implementing the *
*GeoJSON `FeatureCollection`** type.  
It allows multiple features to be stored and provides methods to add individual or multiple features to the collection.

#### 🧱 Fields

| Field      | Type                     | Description                                                                                                                               |
|------------|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `type`     | `String`                 | Always `"FeatureCollection"`                                                                                                              |
| `features` | `List<StatisticFeature>` | A list of features that can be of any type extending [`StatisticFeature`](#StatisticFeature), such as `PointFeature`, `LineFeature`, etc. |

## 🧩 Extending a Feature

Each feature class (e.g., `PointFeature`, `LineFeature`, `PolygonFeature`, etc.) is designed to be **extensible**.  
This enables developers to introduce **custom fields**, **domain-specific logic**, or additional annotations depending
on their project requirements, without modifying the base infrastructure.

For example, you might want to include the source of data or quality score.

### 🔧 How to Extend a Feature

To create a custom feature, simply **extend** one of the existing feature types and add your own fields or behavior.

```java
public class CustomPointFeature extends PointFeature {
  private String sourceSystem;
  private double confidenceScore;
}
```

You can also do this for example with `PointFeatureProperties` and add there you own fields








