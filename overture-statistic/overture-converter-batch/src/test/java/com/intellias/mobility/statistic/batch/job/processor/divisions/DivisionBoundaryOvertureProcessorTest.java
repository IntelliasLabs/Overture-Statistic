/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.divisions;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.LineFeature;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

class DivisionBoundaryOvertureProcessorTest {

  private static final GeometryFactory GF = new GeometryFactory();

  private static final String FIXED_ISO_TS = "2024-05-06T12:30:00Z";
  private static final String FIXED_FORMATTED_TS = "2024-05-06T12:30:00.000+0000";

  @Test
  @DisplayName("LineString -> LineFeature, props normalized, empty rangeAttributes")
  void lineProducesLineFeature() {
    DivisionBoundaryOvertureProcessor proc = new DivisionBoundaryOvertureProcessor();

    LineString ls =
        GF.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
    OvertureItem item = new OvertureItem();
    item.setId("div-boundary-line-1");
    item.setVersion("v3");
    item.setGeometry(ls);
    item.setProperties(Map.of(
        "meta", Map.of("x", 1),
        "theme", "divisions",
        "bbox", "omit-me",
        "names", Map.of("primary", "Kyiv Boundary"),
        "sources", Map.of("update_time", FIXED_ISO_TS)));

    var out = proc.process(item);
    LineFeature lf = assertInstanceOf(LineFeature.class, out);

    assertEquals("DivisionBoundary", lf.getProperties().getFeatureType());
    assertEquals("v3", lf.getProperties().getVersion());
    assertEquals(FIXED_FORMATTED_TS, lf.getProperties().getTimestamp());

    // rangeAttributes explicitly initialized as empty list
    assertNotNull(lf.getProperties().getRangeAttributes());
    assertTrue(lf.getProperties().getRangeAttributes().isEmpty());

    List<FeatureProperty> fp = lf.getProperties().getFeatureProperties();
    // Local-omit keys must be absent
    assertFalse(containsKey(fp, "THEME"));
    assertFalse(containsKey(fp, "BBOX"));
    assertFalse(containsKey(fp, "SOURCES"));
    assertFalse(containsKey(fp, "NAMES"));
  }

  @Test
  @DisplayName("Non-LineString geometries -> null")
  void unsupportedGeometryReturnsNull() {
    DivisionBoundaryOvertureProcessor proc = new DivisionBoundaryOvertureProcessor();

    // Point
    OvertureItem item = new OvertureItem();
    item.setId("div-boundary-pt");
    Point pt = GF.createPoint(new Coordinate(10, 20));
    item.setGeometry(pt);
    item.setProperties(Map.of("sources", Map.of("update_time", FIXED_ISO_TS)));
    assertNull(proc.process(item));

    // Polygon
    Polygon poly = GF.createPolygon(new Coordinate[] {
      new Coordinate(0, 0),
      new Coordinate(1, 0),
      new Coordinate(1, 1),
      new Coordinate(0, 1),
      new Coordinate(0, 0)
    });
    item.setGeometry(poly);
    assertNull(proc.process(item));
  }

  // --- helpers for FeatureProperty list checks ---
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
