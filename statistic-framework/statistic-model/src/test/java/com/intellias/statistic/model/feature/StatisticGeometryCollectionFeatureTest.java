/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.statistic.model.geometry.*;
import com.intellias.statistic.model.util.SerializerUtils;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatisticGeometryCollectionFeatureTest {

  @DisplayName("Verify GeometryCollectionFeature serialization to/from JSON")
  @SneakyThrows
  @Test
  public void testGeometryCollectionFeatureSerialization() {
    var point = new PointGeometry(new LonLat(6.131935, 49.611673));

    var lineString = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.129673, 49.611004)),
        new PointGeometry(new LonLat(6.133527, 49.613243))));

    var polygon = new PolygonGeometry(
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.1305, 49.6100)),
            new PointGeometry(new LonLat(6.1320, 49.6105)),
            new PointGeometry(new LonLat(6.1310, 49.6115)),
            new PointGeometry(new LonLat(6.1305, 49.6100)))),
        List.of());

    var geometryCollection = new StatisticGeometryCollection(List.of(point, lineString, polygon));

    GeometryCollectionFeature feature = new GeometryCollectionFeature();
    feature.setGeometry(geometryCollection);
    feature.setFeatureId("id1");
    feature.setProperties(new GeometryCollectionFeatureProperties(
        "v1",
        "GeometryCollectionFeature",
        new Date(),
        List.of(new FeatureProperty("key", List.of("value")))));

    var json = SerializerUtils.toJson(feature);

    var deserialized = SerializerUtils.fromJson(json, GeometryCollectionFeature.class);

    assertEquals(feature, deserialized);
  }
}
