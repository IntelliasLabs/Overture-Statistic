/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.base;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.LineFeature;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.locationtech.jts.geom.*;

class BaseThemeOvertureProcessorsTest {

  private static final GeometryFactory GF = new GeometryFactory();

  // Below 0.2° to bypass Land tile-rectangle filter
  private static final Polygon SMALL_SQUARE = GF.createPolygon(new Coordinate[] {
    new Coordinate(0.0, 0.0),
    new Coordinate(0.1, 0.0),
    new Coordinate(0.1, 0.1),
    new Coordinate(0.0, 0.1),
    new Coordinate(0.0, 0.0)
  });

  private static final String FIXED_ISO_TS = "2024-05-06T12:30:00Z";
  private static final String FIXED_FORMATTED_TS = "2024-05-06T12:30:00.000+0000";

  private static Stream<Arguments> processors() {
    return Stream.of(
        Arguments.of(new WaterOvertureProcessor(), "Water"),
        Arguments.of(new LandOvertureProcessor(), "Land"),
        Arguments.of(new LandUseOvertureProcessor(), "LandUse"),
        Arguments.of(new InfrastructureOvertureProcessor(), "Infrastructure"));
  }

  @ParameterizedTest(name = "{1}: polygon -> MultiPolygonFeature")
  @MethodSource("processors")
  @DisplayName("Polygon path")
  void polygonProducesMultiPolygonFeature(BaseThemeOvertureProcessor proc, String name) {
    OvertureItem item = new OvertureItem();
    item.setId("poly-" + name);
    item.setVersion("v1");
    item.setGeometry(SMALL_SQUARE);
    item.setProperties(Map.of(
        "theme", name.toLowerCase(),
        "bbox", "must-omit",
        "names", Map.of("primary", "Central Park"),
        "attrs", Map.of("class", "sample", "tags", List.of("a", "b")),
        "sources", Map.of("update_time", FIXED_ISO_TS)));

    var out = proc.process(item);
    MultiPolygonFeature mf = assertInstanceOf(MultiPolygonFeature.class, out);

    PolygonFeatureProperties props = mf.getProperties();
    assertEquals("v1", props.getVersion());
    assertEquals(name + "_multiPolygon", props.getFeatureType());
    assertEquals(FIXED_FORMATTED_TS, props.getTimestamp());

    double expectedArea = FeatureComputationUtils.getAreaInSquareMeters(SMALL_SQUARE);
    assertEquals(expectedArea, props.getArea(), expectedArea * 0.001);

    // Standard recursive extraction and local omit
    List<FeatureProperty> fp = props.getFeatureProperties();
    assertFalse(containsKey(fp, "theme"));
    assertFalse(containsKey(fp, "bbox"));
    assertFalse(containsKey(fp, "sources"));
    assertFalse(containsKey(fp, "names"));
    assertFalse(containsKey(fp, "NAME"));
    assertTrue(containsKV(fp, "attrs.class", "sample"));
    assertTrue(containsKV(fp, "attrs.tags", "a"));
    assertTrue(containsKV(fp, "attrs.tags", "b"));
  }

  @ParameterizedTest(name = "{1}: multipolygon -> MultiPolygonFeature")
  @MethodSource("processors")
  @DisplayName("MultiPolygon path")
  void multipolygonProducesMultiPolygonFeature(BaseThemeOvertureProcessor proc, String name) {
    Polygon p2 = GF.createPolygon(new Coordinate[] {
      new Coordinate(0.2, 0.2),
      new Coordinate(0.3, 0.2),
      new Coordinate(0.3, 0.3),
      new Coordinate(0.2, 0.3),
      new Coordinate(0.2, 0.2)
    });
    MultiPolygon mp = GF.createMultiPolygon(new Polygon[] {SMALL_SQUARE, p2});

    OvertureItem item = new OvertureItem();
    item.setId("mp-" + name);
    item.setVersion("v2");
    item.setGeometry(mp);
    item.setProperties(Map.of("sources", Map.of("update_time", FIXED_ISO_TS)));

    var out = proc.process(item);
    MultiPolygonFeature mf = assertInstanceOf(MultiPolygonFeature.class, out);

    var props = mf.getProperties();
    assertEquals("v2", props.getVersion());
    assertEquals(name + "_multiPolygon", props.getFeatureType());
    assertEquals(FIXED_FORMATTED_TS, props.getTimestamp());

    double expected = FeatureComputationUtils.getAreaInSquareMeters(SMALL_SQUARE)
        + FeatureComputationUtils.getAreaInSquareMeters(p2);
    assertEquals(expected, props.getArea(), expected * 0.001);
  }

  @ParameterizedTest(name = "{1}: line -> LineFeature")
  @MethodSource("processors")
  @DisplayName("LineString path")
  void lineProducesLineFeature(BaseThemeOvertureProcessor proc, String name) {
    LineString ls =
        GF.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});

    OvertureItem item = new OvertureItem();
    item.setId("line-" + name);
    item.setVersion("v3");
    item.setGeometry(ls);
    item.setProperties(Map.of(
        "meta", Map.of("x", 1),
        "sources", Map.of("update_time", FIXED_ISO_TS)));

    var out = proc.process(item);
    LineFeature lf = assertInstanceOf(LineFeature.class, out);

    assertEquals(name + "_lineString", lf.getProperties().getFeatureType());
    assertEquals("v3", lf.getProperties().getVersion());
    assertEquals(FIXED_FORMATTED_TS, lf.getProperties().getTimestamp());
    assertNotNull(lf.getProperties().getRangeAttributes());
    assertTrue(lf.getProperties().getRangeAttributes().isEmpty());

    String metaVal =
        getFirstValue(lf.getProperties().getFeatureProperties(), "meta.x").orElse("");
    assertEquals("1", metaVal);
  }

  @ParameterizedTest(name = "{1}: point -> PointFeature (building-like props)")
  @MethodSource("processors")
  @DisplayName("Point path")
  void pointProducesPointFeature(BaseThemeOvertureProcessor proc, String name) {
    Point pt = GF.createPoint(new Coordinate(10, 20));

    OvertureItem item = new OvertureItem();
    item.setId("pt-" + name);
    item.setVersion("v4");
    item.setGeometry(pt);
    item.setProperties(Map.of("label", "here", "sources", Map.of("update_time", FIXED_ISO_TS)));

    var out = proc.process(item);
    PointFeature pf = assertInstanceOf(PointFeature.class, out);

    assertEquals(name + "_point", pf.getProperties().getFeatureType());
    assertEquals("v4", pf.getProperties().getVersion());
    assertEquals(FIXED_FORMATTED_TS, pf.getProperties().getTimestamp());

    assertTrue(containsKV(pf.getProperties().getFeatureProperties(), "label", "here"));
  }

  @ParameterizedTest(name = "{1}: GeometryCollection -> null (unsupported)")
  @MethodSource("processors")
  @DisplayName("GeometryCollection returns null")
  void geometryCollectionReturnsNull(BaseThemeOvertureProcessor proc, String name) {
    LineString ls =
        GF.createLineString(new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 0)});
    GeometryCollection gc1 = GF.createGeometryCollection(new Geometry[] {SMALL_SQUARE, ls});
    GeometryCollection gc2 =
        GF.createGeometryCollection(new Geometry[] {SMALL_SQUARE, SMALL_SQUARE});
    GeometryCollection gc3 = GF.createGeometryCollection(new Geometry[] {ls});

    OvertureItem item = baseItem("gc-" + name);
    item.setGeometry(gc1);
    assertNull(proc.process(item));

    item.setGeometry(gc2);
    assertNull(proc.process(item));

    item.setGeometry(gc3);
    assertNull(proc.process(item));
  }

  private static OvertureItem baseItem(String id) {
    OvertureItem item = new OvertureItem();
    item.setId(id);
    item.setVersion("vX");
    item.setProperties(Map.of("sources", Map.of("update_time", FIXED_ISO_TS)));
    return item;
  }

  // --- helpers for FeatureProperty lists ---
  private static boolean containsKey(List<FeatureProperty> list, String key) {
    return list.stream().anyMatch(p -> p.getKey().equals(key));
  }

  private static boolean containsKV(List<FeatureProperty> list, String key, String value) {
    return list.stream().anyMatch(p -> p.getKey().equals(key) && p.getValues().contains(value));
  }

  private static Optional<String> getFirstValue(List<FeatureProperty> list, String key) {
    return list.stream()
        .filter(p -> p.getKey().equals(key))
        .findFirst()
        .flatMap(p -> p.getValues().isEmpty()
            ? Optional.empty()
            : Optional.of(p.getValues().get(0)));
  }

  // Ensures Land processor drops land-mask features based on properties.
  @Test
  @DisplayName("Land: property-based land mask is filtered out")
  void landMaskFilteredByProperty() {
    OvertureItem item = new OvertureItem();
    item.setId("land-mask");
    item.setVersion("vL");
    item.setGeometry(SMALL_SQUARE);
    item.setProperties(Map.of(
        "theme", "base",
        "type", "land",
        "class", "land",
        "sources", Map.of("update_time", FIXED_ISO_TS)));

    LandOvertureProcessor proc = new LandOvertureProcessor();
    assertNull(proc.process(item));
  }
}
