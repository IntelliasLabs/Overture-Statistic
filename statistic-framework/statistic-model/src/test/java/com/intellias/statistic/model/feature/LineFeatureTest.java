/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.statistic.model.attribute.Range;
import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.attribute.RangeAttributeValue;
import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.LonLat;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.util.SerializerUtils;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LineFeatureTest {

  @DisplayName("Verify LineFeature serialization to/from json")
  @SneakyThrows
  @Test
  public void testLineStringSerialization() {
    var coordinates = List.of(
        new LonLat(6.134128158472095, 49.593476516900296),
        new LonLat(6.134940920303421, 49.593091623674354),
        new LonLat(6.1350546227353675, 49.593091623674354),
        new LonLat(6.135260971593681, 49.59312711041093));

    LineGeometry line =
        new LineGeometry(coordinates.stream().map(PointGeometry::new).toList());

    LineFeature feature = new LineFeature();
    feature.setGeometry(line);
    feature.setFeatureId("id1");
    feature.setProperties(new LineFeatureProperties(
        "v1", "LineFeature", new Date(), List.of(new FeatureProperty("key", List.of("value")))));

    var json = SerializerUtils.toJson(feature);
    var deserialized = SerializerUtils.fromJson(json, LineFeature.class);

    assertEquals(feature, deserialized);
  }

  @DisplayName("Verify LineFeature serialization to/from json with range attributes")
  @SneakyThrows
  @Test
  public void testLineStringSerializationRangeAttr() {
    var coordinates = List.of(
        new LonLat(6.134128158472095, 49.593476516900296),
        new LonLat(6.134940920303421, 49.593091623674354),
        new LonLat(6.1350546227353675, 49.593091623674354),
        new LonLat(6.135260971593681, 49.59312711041093));

    LineGeometry line =
        new LineGeometry(coordinates.stream().map(PointGeometry::new).toList());

    var range = new RangeAttribute(
        "speed-limit",
        List.of(
            new RangeAttributeValue("50km", List.of(new Range(0.0, 0.2), new Range(0.2, 0.5))),
            new RangeAttributeValue("40km", List.of(new Range(0.5, 1.0)))));

    LineFeature feature = new LineFeature();
    feature.setGeometry(line);
    feature.setFeatureId("id1");
    feature.setProperties(new LineFeatureProperties(
        "v1",
        "LineFeature",
        new Date(),
        List.of(new FeatureProperty("key", List.of("value"))),
        List.of(range)));

    var json = SerializerUtils.toJson(feature);
    var deserialized = SerializerUtils.fromJson(json, LineFeature.class);

    assertEquals(feature, deserialized);
  }
}
