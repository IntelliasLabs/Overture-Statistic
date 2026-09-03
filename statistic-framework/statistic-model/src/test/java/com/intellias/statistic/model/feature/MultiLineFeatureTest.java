/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.statistic.model.geometry.*;
import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.MultiLineGeometry;
import com.intellias.statistic.model.util.SerializerUtils;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MultiLineFeatureTest {

  @DisplayName("Verify MultiLineStringFeature serialization to/from json")
  @SneakyThrows
  @Test
  void testMultiLineStringFeatureSerialization() {
    var line1 = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.0, 49.0)), new PointGeometry(new LonLat(6.1, 49.1))));
    var line2 = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.2, 49.2)), new PointGeometry(new LonLat(6.3, 49.3))));

    var multiLineStringGeometry = new MultiLineGeometry(List.of(line1, line2));

    MultiLineFeature feature = new MultiLineFeature();
    feature.setGeometry(multiLineStringGeometry);
    feature.setFeatureId("multiLineId");
    feature.setProperties(new LineFeatureProperties(
        "v1",
        "MultiLineFeature",
        new Date(),
        List.of(new FeatureProperty("key", List.of("value")))));

    var json = SerializerUtils.toJson(feature);
    var deserialized = SerializerUtils.fromJson(json, MultiLineFeature.class);

    assertEquals(feature, deserialized);
  }
}
