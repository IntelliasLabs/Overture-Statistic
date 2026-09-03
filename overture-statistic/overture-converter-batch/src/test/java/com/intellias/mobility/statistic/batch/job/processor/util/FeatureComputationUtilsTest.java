/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.attribute.RangeAttribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

class FeatureComputationUtilsTest {

  private static final GeometryFactory GF = new GeometryFactory();

  @Test
  void getRangeAttributes_shouldExtractNestedProperties() {
    // GIVEN
    Map<String, Object> limit1 = new HashMap<>();
    limit1.put("max_speed", Map.of("value", 50, "unit", "km/h"));
    limit1.put("between", List.of(0.0, 0.5));

    Map<String, Object> limit2 = new HashMap<>();
    limit2.put("max_speed", Map.of("value", 70, "unit", "km/h"));
    limit2.put("between", List.of(0.5, 1.0));

    OvertureItem item = new OvertureItem();
    item.setProperties(Map.of("speed_limits", List.of(limit1, limit2)));

    // WHEN
    List<RangeAttribute> result = FeatureComputationUtils.getRangeAttributes(item);

    // THEN
    assertThat(result).hasSize(2);

    RangeAttribute valueAttr = result.stream()
        .filter(ra -> ra.getKey().equals("speed_limits.max_speed.value"))
        .findFirst()
        .orElseThrow();
    assertThat(valueAttr.getValues()).hasSize(2);
    assertThat(valueAttr.getValues()).anyMatch(v -> v.getValue().equals("50"));
    assertThat(valueAttr.getValues()).anyMatch(v -> v.getValue().equals("70"));

    RangeAttribute unitAttr = result.stream()
        .filter(ra -> ra.getKey().equals("speed_limits.max_speed.unit"))
        .findFirst()
        .orElseThrow();
    assertThat(unitAttr.getValues()).hasSize(1);
    assertThat(unitAttr.getValues().getFirst().getValue()).isEqualTo("km/h");
    assertThat(unitAttr.getValues().getFirst().getRanges()).hasSize(2);
  }

  @Test
  void testGetAreaInSquareMeters_simpleTriangle() {
    Coordinate[] coords = new Coordinate[] {
      new Coordinate(0.0, 0.0),
      new Coordinate(0.0, 0.1),
      new Coordinate(0.1, 0.0),
      new Coordinate(0.0, 0.0)
    };

    LinearRing shell = GF.createLinearRing(coords);
    Polygon polygon = GF.createPolygon(shell);

    double area = FeatureComputationUtils.getAreaInSquareMeters(polygon);

    double expected = 61_545_392.271015316;
    assertEquals(expected, area, 1e-3, "Area in square meters should match expected value");
  }

  @Test
  void testGetAreaInSquareMeters_withHole() {
    Coordinate[] outerCoords = new Coordinate[] {
      new Coordinate(0.0, 0.0),
      new Coordinate(0.0, 1.0),
      new Coordinate(1.0, 1.0),
      new Coordinate(1.0, 0.0),
      new Coordinate(0.0, 0.0)
    };

    Coordinate[] holeCoords = new Coordinate[] {
      new Coordinate(0.2, 0.2),
      new Coordinate(0.2, 0.8),
      new Coordinate(0.8, 0.8),
      new Coordinate(0.8, 0.2),
      new Coordinate(0.2, 0.2)
    };

    LinearRing outer = GF.createLinearRing(outerCoords);
    LinearRing hole = GF.createLinearRing(holeCoords);

    Polygon polygonWithHole = GF.createPolygon(outer, new LinearRing[] {hole});

    double area = FeatureComputationUtils.getAreaInSquareMeters(polygonWithHole);

    double areaWithoutHole = FeatureComputationUtils.getAreaInSquareMeters(GF.createPolygon(outer));

    assertEquals(
        areaWithoutHole - FeatureComputationUtils.getAreaInSquareMeters(GF.createPolygon(hole)),
        area,
        0.0001);
  }
}
