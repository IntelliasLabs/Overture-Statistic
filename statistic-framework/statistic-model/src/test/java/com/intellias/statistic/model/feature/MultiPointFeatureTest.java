/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.statistic.model.geometry.LonLat;
import com.intellias.statistic.model.geometry.MultiPointGeometry;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.util.SerializerUtils;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MultiPointFeatureTest {

  @DisplayName("Verify MultiPointFeature serialization to/from json")
  @SneakyThrows
  @Test
  void testMultiPointFeatureSerialization() {

    var point1 = new PointGeometry(new LonLat(6.0, 49.0));
    var point2 = new PointGeometry(new LonLat(6.1, 49.1));
    var multiPointGeometry = new MultiPointGeometry(List.of(point1, point2));

    MultiPointFeature feature = new MultiPointFeature();
    feature.setGeometry(multiPointGeometry);
    feature.setFeatureId("multiPointId");
    feature.setProperties(new PointFeatureProperties(
        "v1",
        "MultiPointFeature",
        new Date(),
        List.of(new FeatureProperty("key", List.of("value")))));

    var json = SerializerUtils.toJson(feature);
    var deserialized = SerializerUtils.fromJson(json, MultiPointFeature.class);

    assertEquals(feature, deserialized);
  }
}
