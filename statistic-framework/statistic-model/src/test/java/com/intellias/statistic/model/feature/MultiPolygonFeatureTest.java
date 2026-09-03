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

class MultiPolygonFeatureTest {

  @DisplayName("Verify MultiPolygonFeature serialization to/from json")
  @SneakyThrows
  @Test
  void testMultiPolygonFeatureSerialization() {
    var outerRing1 = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.0, 49.0)),
        new PointGeometry(new LonLat(6.1, 49.0)),
        new PointGeometry(new LonLat(6.1, 49.1)),
        new PointGeometry(new LonLat(6.0, 49.0)) // close
        ));
    var polygon1 = new PolygonGeometry(outerRing1, List.of());

    var outerRing2 = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.2, 49.2)),
        new PointGeometry(new LonLat(6.3, 49.2)),
        new PointGeometry(new LonLat(6.3, 49.3)),
        new PointGeometry(new LonLat(6.2, 49.2)) // close
        ));

    var polygon2 = new PolygonGeometry(outerRing2, List.of());

    var multiPolygonGeometry = new MultiPolygonGeometry(List.of(polygon1, polygon2));

    MultiPolygonFeature feature = new MultiPolygonFeature();
    feature.setGeometry(multiPolygonGeometry);
    feature.setFeatureId("multiPolygonId");
    feature.setProperties(new PolygonFeatureProperties(
        "v1",
        "MultiPolygonFeature",
        new Date(),
        List.of(new FeatureProperty("key", List.of("value")))));

    var json = SerializerUtils.toJson(feature);
    var deserialized = SerializerUtils.fromJson(json, MultiPolygonFeature.class);

    assertEquals(feature, deserialized);
  }
}
