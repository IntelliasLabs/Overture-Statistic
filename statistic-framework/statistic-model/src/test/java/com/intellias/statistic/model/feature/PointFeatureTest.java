/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.statistic.model.geometry.LonLat;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.util.SerializerUtils;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PointFeatureTest {

  @DisplayName("Verify PointFeature serialization to/from json")
  @SneakyThrows
  @Test
  public void testLineStringSerialization() {
    var coordinates = new LonLat(6.134128158472095, 49.593476516900296);

    PointGeometry point = new PointGeometry(coordinates);

    PointFeature feature = new PointFeature();
    feature.setGeometry(point);
    feature.setFeatureId("id1");
    feature.setProperties(new PointFeatureProperties(
        "v1", "Point", new Date(), List.of(new FeatureProperty("key", List.of("value")))));

    var json = SerializerUtils.toJson(feature);
    var deserialized = SerializerUtils.fromJson(json, PointFeature.class);

    assertEquals(feature, deserialized);
  }
}
