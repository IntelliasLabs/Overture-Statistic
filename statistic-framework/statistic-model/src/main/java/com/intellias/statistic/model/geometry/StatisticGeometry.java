/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.geometry;

import com.fasterxml.jackson.annotation.*;

/** Common geometry interface. */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = PointGeometry.class, name = "Point"),
  @JsonSubTypes.Type(value = LineGeometry.class, name = "LineString"),
  @JsonSubTypes.Type(value = PolygonGeometry.class, name = "Polygon"),
  @JsonSubTypes.Type(value = MultiPointGeometry.class, name = "MultiPoint"),
  @JsonSubTypes.Type(value = MultiLineGeometry.class, name = "MultiLineString"),
  @JsonSubTypes.Type(value = MultiPolygonGeometry.class, name = "MultiPolygon"),
  @JsonSubTypes.Type(value = StatisticGeometryCollection.class, name = "GeometryCollection")
})
public interface StatisticGeometry {
  String getType();
}
