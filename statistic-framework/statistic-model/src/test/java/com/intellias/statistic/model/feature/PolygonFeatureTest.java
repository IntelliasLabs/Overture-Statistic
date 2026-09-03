/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.statistic.model.geometry.*;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.geometry.PolygonGeometry;
import com.intellias.statistic.model.util.SerializerUtils;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PolygonFeatureTest {

  @DisplayName("Verify PolygonFeature serialization to/from json")
  @SneakyThrows
  @Test
  void testPolygonFeatureSerialization() {
    // Outer ring
    var outerRing = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.1, 49.5)),
        new PointGeometry(new LonLat(6.2, 49.6)),
        new PointGeometry(new LonLat(6.3, 49.5)),
        new PointGeometry(new LonLat(6.1, 49.5)) // close the polygon
        ));
    var polygonGeometry = new PolygonGeometry(outerRing, List.of());

    PolygonFeature feature = new PolygonFeature();
    feature.setGeometry(polygonGeometry);
    feature.setFeatureId("polygonId");
    feature.setProperties(new PolygonFeatureProperties(
        "v1", "PolygonFeature", new Date(), List.of(new FeatureProperty("key", List.of("value")))));

    var json = SerializerUtils.toJson(feature);
    var deserialized = SerializerUtils.fromJson(json, PolygonFeature.class);

    assertEquals(feature, deserialized);
  }
}
