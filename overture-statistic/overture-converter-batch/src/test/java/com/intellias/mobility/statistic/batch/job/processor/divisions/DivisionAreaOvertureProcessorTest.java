/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.divisions;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

class DivisionAreaOvertureProcessorTest {

  private static final GeometryFactory GF = new GeometryFactory();

  // Small square polygon to keep area computation stable and quick
  private static final Polygon SMALL_SQUARE = GF.createPolygon(new Coordinate[] {
    new Coordinate(0.0, 0.0),
    new Coordinate(0.1, 0.0),
    new Coordinate(0.1, 0.1),
    new Coordinate(0.0, 0.1),
    new Coordinate(0.0, 0.0)
  });

  private static final String FIXED_ISO_TS = "2024-05-06T12:30:00Z";
  private static final String FIXED_FORMATTED_TS = "2024-05-06T12:30:00.000+0000";

  @Test
  @DisplayName("Polygon -> MultiPolygonFeature, area filled, props normalized")
  void polygonProducesMultiPolygonFeature() {
    DivisionAreaOvertureProcessor proc = new DivisionAreaOvertureProcessor();

    OvertureItem item = new OvertureItem();
    item.setId("div-area-poly-1");
    item.setVersion("v1");
    item.setGeometry(SMALL_SQUARE);
    item.setProperties(Map.of(
        "classCode", "ADM1",
        "theme", "divisions",
        "type", "division_area",
        "bbox", "omit-me",
        "names", Map.of("primary", "Kyiv City"),
        "sources", Map.of("update_time", FIXED_ISO_TS)));

    var out = proc.process(item);
    MultiPolygonFeature mf = assertInstanceOf(MultiPolygonFeature.class, out);

    PolygonFeatureProperties props = mf.getProperties();
    assertEquals("v1", props.getVersion());
    assertEquals("DivisionArea", props.getFeatureType());
    assertEquals(FIXED_FORMATTED_TS, props.getTimestamp());

    double expectedArea = FeatureComputationUtils.getAreaInSquareMeters(SMALL_SQUARE);
    assertEquals(expectedArea, props.getArea(), expectedArea * 0.001);

    // Geometry normalized to MultiPolygonGeometry with single polygon
    assertEquals(1, mf.getGeometry().getCoordinates().size());

    List<FeatureProperty> fp = props.getFeatureProperties();
    // Local-omit keys must be absent
    assertFalse(containsKey(fp, "THEME"));
    assertFalse(containsKey(fp, "BBOX"));
    assertFalse(containsKey(fp, "SOURCES"));
    assertFalse(containsKey(fp, "NAMES"));

    // UPPER_SNAKE_CASE + names.primary -> NAME
    assertTrue(containsKV(fp, "classCode", "ADM1"));
  }

  @Test
  @DisplayName("MultiPolygon -> MultiPolygonFeature with summed area")
  void multiPolygonProducesMultiPolygonFeature() {
    DivisionAreaOvertureProcessor proc = new DivisionAreaOvertureProcessor();

    Polygon p2 = GF.createPolygon(new Coordinate[] {
      new Coordinate(0.2, 0.2),
      new Coordinate(0.3, 0.2),
      new Coordinate(0.3, 0.3),
      new Coordinate(0.2, 0.3),
      new Coordinate(0.2, 0.2)
    });
    MultiPolygon mp = GF.createMultiPolygon(new Polygon[] {SMALL_SQUARE, p2});

    OvertureItem item = new OvertureItem();
    item.setId("div-area-mp-1");
    item.setVersion("v2");
    item.setGeometry(mp);
    item.setProperties(Map.of("sources", Map.of("update_time", FIXED_ISO_TS)));

    var out = proc.process(item);
    MultiPolygonFeature mf = assertInstanceOf(MultiPolygonFeature.class, out);

    assertEquals("DivisionArea", mf.getProperties().getFeatureType());
    assertEquals("v2", mf.getProperties().getVersion());
    assertEquals(FIXED_FORMATTED_TS, mf.getProperties().getTimestamp());

    double expected = FeatureComputationUtils.getAreaInSquareMeters(SMALL_SQUARE)
        + FeatureComputationUtils.getAreaInSquareMeters(p2);
    assertEquals(expected, mf.getProperties().getArea(), expected * 0.001);

    // Should contain two polygon parts
    assertEquals(2, mf.getGeometry().getCoordinates().size());
  }

  @Test
  @DisplayName("Unsupported geometry -> null")
  void unsupportedGeometryReturnsNull() {
    DivisionAreaOvertureProcessor proc = new DivisionAreaOvertureProcessor();

    // Point
    OvertureItem item = new OvertureItem();
    item.setId("div-area-pt");
    item.setGeometry(GF.createPoint(new Coordinate(1, 2)));
    item.setProperties(Map.of("sources", Map.of("update_time", FIXED_ISO_TS)));
    assertNull(proc.process(item));

    // LineString
    LineString ls =
        GF.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
    item.setGeometry(ls);
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
