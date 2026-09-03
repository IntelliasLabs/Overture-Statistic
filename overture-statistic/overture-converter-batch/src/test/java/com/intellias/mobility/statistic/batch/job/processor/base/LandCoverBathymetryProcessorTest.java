/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.base;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.PolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

class LandCoverBathymetryProcessorTest {

  private static final GeometryFactory GF = new GeometryFactory();
  private static final BathymetryProcessor BATHYMETRY_PROCESSOR = new BathymetryProcessor();
  private static final LandCoverProcessor LANDcOVER_PROCESSOR = new LandCoverProcessor();
  private static final OvertureItem ITEM = new OvertureItem();

  @BeforeEach
  void init() {
    ITEM.setId("37fd931a-a811-5a61-9e78-fa4af83118c3");
    ITEM.setVersion("v1.11.0");

    Map<String, Object> properties = new HashMap<>();

    Map<String, Object> sources = new HashMap<>();
    sources.put("property", "");
    sources.put("dataset", "ETOPO/GLOBathy");
    sources.put("record_id", "2024-12-09T00:00:00.000Z");
    sources.put("update_time", null);
    sources.put("confidence", null);
    sources.put("between", null);

    Map<String, Object> bbox = new HashMap<>();
    bbox.put("xmin", -92.12344);
    bbox.put("xmax", -91.553925);
    bbox.put("ymin", -40.209866);
    bbox.put("ymax", -38.97252);

    Map<String, Object> cartography = new HashMap<>();
    cartography.put("prominence", null);
    cartography.put("min_zoom", null);
    cartography.put("max_zoom", null);
    cartography.put("sort_key", 17);

    properties.put("depth", 3000);
    properties.put("sources", sources);
    properties.put("bbox", bbox);
    properties.put("cartography", cartography);
    ITEM.setProperties(properties);
  }

  @Test
  void process_Polygon() {
    // Create a simple 10x10 square polygon (area = 100)
    Polygon polygon = createSquarePolygon(0, 0, 10);

    ITEM.setGeometry(polygon);

    StatisticFeature<?> feature = BATHYMETRY_PROCESSOR.process(ITEM);

    // core props
    PolygonFeatureProperties props = (PolygonFeatureProperties) feature.getProperties();
    assertNotNull(props);
    assertEquals("v1.11.0", props.getVersion());
    assertEquals("bathymetry", props.getFeatureType());
    assertNotNull(props.getTimestamp());

    assertEquals(1.2278771916096267E12, props.getArea(), 1e-3);
    List<FeatureProperty> fp = props.getFeatureProperties();
    assertNotNull(fp);
    assertEquals(2, fp.size());

    Function<String, Optional<FeatureProperty>> byKey =
        k -> fp.stream().filter(p -> k.equals(p.getKey())).findFirst();

    assertTrue(byKey.apply("depth").isPresent(), "depth key missing");
    assertEquals(List.of("3000"), byKey.apply("depth").get().getValues());

    assertTrue(byKey.apply("cartography.sort_key").isPresent(), "cartography.sort_key missing");
    assertEquals(List.of("17"), byKey.apply("cartography.sort_key").get().getValues());
  }

  @Test
  void process_MultiPolygon() {
    // Two squares: 10x10 = 100, and 5x5 = 25, total = 125
    Polygon square10 = createSquarePolygon(0, 0, 10);
    Polygon square5 = createSquarePolygon(20, 20, 5);
    MultiPolygon mp = GF.createMultiPolygon(new Polygon[] {square10, square5});

    ITEM.setGeometry(mp);

    StatisticFeature<?> feature = BATHYMETRY_PROCESSOR.process(ITEM);

    // core props
    PolygonFeatureProperties props = (PolygonFeatureProperties) feature.getProperties();
    assertNotNull(props);
    assertEquals("v1.11.0", props.getVersion());
    assertEquals("bathymetry", props.getFeatureType());
    assertNotNull(props.getTimestamp());

    assertEquals(1.5127506581945127E12, props.getArea(), 1e-3);
    List<FeatureProperty> fp = props.getFeatureProperties();
    assertNotNull(fp);
    assertEquals(2, fp.size());

    Function<String, Optional<FeatureProperty>> byKey =
        k -> fp.stream().filter(p -> k.equals(p.getKey())).findFirst();

    assertTrue(byKey.apply("depth").isPresent(), "depth key missing");
    assertEquals(List.of("3000"), byKey.apply("depth").get().getValues());

    assertTrue(byKey.apply("cartography.sort_key").isPresent(), "cartography.sort_key missing");
    assertEquals(List.of("17"), byKey.apply("cartography.sort_key").get().getValues());
  }

  @Test
  void process_LandCover_Polygon() {
    // Create a simple 10x10 square polygon (area = 100)
    Polygon polygon = createSquarePolygon(0, 0, 10);

    ITEM.setId("bda01124-4c8d-5433-9bc4-4068c00c4347");
    ITEM.setVersion("v1.11.0");

    Map<String, Object> properties = new HashMap<>();

    Map<String, Object> sources = new HashMap<>();
    sources.put("property", "");
    sources.put("dataset", "ESA WorldCover");
    sources.put("record_id", null);
    sources.put("update_time", "2024-11-07T00:00:00.000Z");
    sources.put("confidence", null);
    sources.put("between", null);

    Map<String, Object> bbox = new HashMap<>();
    bbox.put("xmin", -92.12344);
    bbox.put("xmax", -91.553925);
    bbox.put("ymin", -40.209866);
    bbox.put("ymax", -38.97252);

    Map<String, Object> cartography = new HashMap<>();
    cartography.put("prominence", null);
    cartography.put("min_zoom", 8);
    cartography.put("max_zoom", 15);
    cartography.put("sort_key", 4);

    properties.put("subtype", "shrub");
    properties.put("sources", sources);
    properties.put("bbox", bbox);
    properties.put("cartography", cartography);

    ITEM.setProperties(properties);

    ITEM.setGeometry(polygon);

    StatisticFeature<?> feature = LANDcOVER_PROCESSOR.process(ITEM);

    // core props
    PolygonFeatureProperties props = (PolygonFeatureProperties) feature.getProperties();
    assertNotNull(props);
    assertEquals("v1.11.0", props.getVersion());
    assertEquals("landCover", props.getFeatureType());
    assertNotNull(props.getTimestamp());

    // Area from your computation (degrees -> meters²)
    assertEquals(1.2278771916096267E12, props.getArea(), 1e-3);

    // featureProperties assertions
    List<FeatureProperty> fp = props.getFeatureProperties();
    assertNotNull(fp);
    assertEquals(4, fp.size(), "Only subtype and cartography.sort_key expected");

    Function<String, Optional<FeatureProperty>> byKey =
        k -> fp.stream().filter(p -> k.equals(p.getKey())).findFirst();

    // subtype present
    assertTrue(byKey.apply("subtype").isPresent(), "subtype key missing");
    assertEquals(List.of("shrub"), byKey.apply("subtype").get().getValues());

    // cartography.sort_key present
    assertTrue(byKey.apply("cartography.sort_key").isPresent(), "cartography.sort_key missing");
    assertEquals(List.of("4"), byKey.apply("cartography.sort_key").get().getValues());
  }

  @Test
  void process_withInvalidGeometry_returnsNullAndLogsError() {
    // given: a MultiPolygon instead of a Polygon to trigger ClassCastException
    Polygon poly1 = GF.createPolygon(new Coordinate[] {
      new Coordinate(0, 0),
      new Coordinate(1, 0),
      new Coordinate(1, 1),
      new Coordinate(0, 1),
      new Coordinate(0, 0)
    });
    MultiPolygon badGeometry = GF.createMultiPolygon(new Polygon[] {poly1});

    OvertureItem item = new OvertureItem();
    item.setId("test-id");
    item.setVersion("v1.0");
    item.setGeometry(badGeometry);
    item.setProperties(Map.of());

    LandCoverProcessor processor = new LandCoverProcessor(); // replace with actual class name

    // when
    PolygonFeature result = processor.process(item);

    // then
    assertNull(result, "Expected null when geometry is invalid");

    // If you want to verify log output, you can hook in a test logger here.
  }

  @Test
  void process_withInvalidGeometryType_returnsNull() {
    // given: a Point geometry to trigger ClassCastException
    Point badGeometry = GF.createPoint(new Coordinate(0, 0));

    OvertureItem item = new OvertureItem();
    item.setId("test-id");
    item.setVersion("v1.0");
    item.setGeometry(badGeometry);
    item.setProperties(Map.of());

    BathymetryProcessor processor =
        new BathymetryProcessor(); // replace with actual processor class

    // when
    StatisticFeature<?> result = processor.process(item);

    // then
    assertNull(result, "Expected null when geometry type is unsupported");
    // Optionally, verify logging if you capture logs in your tests.
  }

  private static Polygon createSquarePolygon(double x, double y, double size) {
    Coordinate[] coords = new Coordinate[] {
      new Coordinate(x, y),
      new Coordinate(x + size, y),
      new Coordinate(x + size, y + size),
      new Coordinate(x, y + size),
      new Coordinate(x, y) // close ring
    };
    LinearRing shell = GF.createLinearRing(coords);
    return GF.createPolygon(shell);
  }
}
