/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.divisions;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.PointFeature;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

class DivisionOvertureProcessorTest {

  private static final GeometryFactory GF = new GeometryFactory();

  // Fixed timestamp for deterministic formatter check
  private static final String FIXED_ISO_TS = "2024-05-06T12:30:00Z";
  private static final String FIXED_FORMATTED_TS = "2024-05-06T12:30:00.000+0000";

  @Test
  @DisplayName(
      "Point -> PointFeature with building-like props (UPPER_SNAKE_CASE, NAME promotion, local omit)")
  void pointProducesPointFeature() {
    DivisionOvertureProcessor proc = new DivisionOvertureProcessor();

    Point pt = GF.createPoint(new Coordinate(10, 20));
    OvertureItem item = new OvertureItem();
    item.setId("div-pt-1");
    item.setVersion("v1");
    item.setGeometry(pt);
    item.setProperties(Map.of(
        "label", "here",
        "populationCount", 42,
        "theme", "divisions",
        "type", "division",
        "bbox", "omit-me",
        "names", Map.of("primary", "Central District"),
        "sources", Map.of("update_time", FIXED_ISO_TS)));

    var out = proc.process(item);
    PointFeature pf = assertInstanceOf(PointFeature.class, out);

    assertEquals("Division", pf.getProperties().getFeatureType());
    assertEquals("v1", pf.getProperties().getVersion());
    assertEquals(FIXED_FORMATTED_TS, pf.getProperties().getTimestamp());

    List<FeatureProperty> props = pf.getProperties().getFeatureProperties();
    // Local-omit keys must be absent
    assertFalse(containsKey(props, "THEME"));
    assertFalse(containsKey(props, "TYPE"));
    assertFalse(containsKey(props, "BBOX"));
    assertFalse(containsKey(props, "SOURCES"));
    assertFalse(containsKey(props, "NAMES"));

    // UPPER_SNAKE_CASE + value stringification
    assertTrue(containsKV(props, "label", "here"));
    assertTrue(containsKV(props, "populationCount", "42"));
  }

  @Test
  @DisplayName("Non-Point geometry returns null (unsupported)")
  void nonPointReturnsNull() {
    DivisionOvertureProcessor proc = new DivisionOvertureProcessor();

    // LineString
    LineString ls =
        GF.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
    OvertureItem item = new OvertureItem();
    item.setId("div-pt-2");
    item.setVersion("v2");
    item.setGeometry(ls);
    item.setProperties(Map.of("sources", Map.of("update_time", FIXED_ISO_TS)));
    assertNull(proc.process(item));

    // Null geometry also results in null
    item.setGeometry(null);
    assertNull(proc.process(item));
  }

  // --- small helpers for FeatureProperty list checks ---
  private static boolean containsKey(List<FeatureProperty> list, String key) {
    return list.stream().anyMatch(p -> p.getKey().equals(key));
  }

  private static boolean containsKV(List<FeatureProperty> list, String key, String value) {
    return list.stream().anyMatch(p -> p.getKey().equals(key) && p.getValues().contains(value));
  }

  @SuppressWarnings("unused")
  private static Optional<String> firstVal(List<FeatureProperty> list, String key) {
    return list.stream()
        .filter(p -> p.getKey().equals(key))
        .findFirst()
        .flatMap(p -> p.getValues().isEmpty()
            ? Optional.empty()
            : Optional.of(p.getValues().get(0)));
  }
}
