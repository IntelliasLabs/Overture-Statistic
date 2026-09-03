/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.building;

import static com.intellias.mobility.statistic.batch.job.processor.building.BuildingFeatureProcessor.PROCESSOR_NAME;
import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

class BuildingFeatureProcessorTest {

  private BuildingFeatureProcessor processor;
  private GeometryFactory geometryFactory;

  private static final GeometryFactory GEO_FACTORY = new GeometryFactory();
  private static final Polygon UNIT_SQUARE = GEO_FACTORY.createPolygon(new Coordinate[] {
    new Coordinate(0, 0),
    new Coordinate(1, 0),
    new Coordinate(1, 1),
    new Coordinate(0, 1),
    new Coordinate(0, 0)
  });

  @BeforeEach
  void setUp() {
    processor = new BuildingFeatureProcessor();
    geometryFactory = new GeometryFactory();
  }

  @Test
  @DisplayName("Process polygon geometry")
  void shouldProcessPolygonGeometry() {
    // Given
    OvertureItem item = itemWithPolygon("test-polygon-id");
    item.setVersion("1.0");

    item.setProperties(Map.of(
        "height",
        10.5,
        "names",
        Map.of("primary", "Test Building"),
        "theme",
        "building",
        "type",
        "building"));

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNotNull(feature);
    assertEquals("test-polygon-id", feature.getFeatureId());
    assertNotNull(feature.getGeometry());
    assertEquals(1, feature.getGeometry().getCoordinates().size());
    assertNotNull(feature.getProperties());
    assertEquals("1.0", feature.getProperties().getVersion());
    assertEquals(PROCESSOR_NAME, feature.getProperties().getFeatureType());

    List<FeatureProperty> properties = feature.getProperties().getFeatureProperties();
    assertTrue(properties.stream().noneMatch(prop -> prop.getKey().equals("theme")));
    assertTrue(properties.stream().noneMatch(prop -> prop.getKey().equals("type")));

    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("height")));
    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("names.primary")));
  }

  @Test
  @DisplayName("Process multipolygon geometry")
  void shouldProcessMultiPolygonGeometry() {
    // Given
    OvertureItem item = new OvertureItem();
    item.setId("test-multipolygon-id");
    item.setVersion("2.0");

    Polygon polygon2 = geometryFactory.createPolygon(new Coordinate[] {
      new Coordinate(2, 2),
      new Coordinate(3, 2),
      new Coordinate(3, 3),
      new Coordinate(2, 3),
      new Coordinate(2, 2)
    });
    MultiPolygon multiPolygon =
        geometryFactory.createMultiPolygon(new Polygon[] {UNIT_SQUARE, polygon2});
    item.setGeometry(multiPolygon);

    item.setProperties(Map.of("height", 15.0, "names", Map.of("primary", "Test Multi Building")));

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNotNull(feature);
    assertEquals("test-multipolygon-id", feature.getFeatureId());
    assertNotNull(feature.getGeometry());
    assertEquals(2, feature.getGeometry().getCoordinates().size());
    assertNotNull(feature.getProperties());
    assertEquals("2.0", feature.getProperties().getVersion());
    assertEquals(PROCESSOR_NAME, feature.getProperties().getFeatureType());

    List<FeatureProperty> properties = feature.getProperties().getFeatureProperties();
    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("height")));
    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("names.primary")));
  }

  @Test
  @DisplayName("Return null for null geometry")
  void shouldReturnNullForNullGeometry() {
    // Given
    OvertureItem item = new OvertureItem();
    item.setId("test-null-geometry-id");
    item.setGeometry(null);

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNull(feature);
  }

  @Test
  @DisplayName("Return null for empty geometry")
  void shouldReturnNullForEmptyGeometry() {
    // Given
    OvertureItem item = new OvertureItem();
    item.setId("test-empty-geometry-id");
    item.setGeometry(geometryFactory.createPolygon());

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNull(feature);
  }

  @Test
  @DisplayName("Return null for invalid geometry type")
  void shouldReturnNullForInvalidGeometryType() {
    // Given
    OvertureItem item = new OvertureItem();
    item.setId("test-invalid-geometry-id");

    Geometry point = geometryFactory.createPoint(new Coordinate(0, 0));
    item.setGeometry(point);

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNull(feature);
  }

  @Test
  @DisplayName("Process various properties")
  void shouldProcessVariousProperties() {
    // Given
    OvertureItem item = itemWithPolygon("test-properties-id");
    item.setVersion("1.5");

    item.setProperties(Map.of(
        "height",
        20.0,
        "area",
        100.5,
        "theme",
        "building",
        "type",
        "building",
        "version",
        "1.0",
        "sources",
        List.of("source1", "source2"),
        "bbox",
        "bbox-data",
        "names",
        Map.of("primary", "Property Test Building")));

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNotNull(feature);
    List<FeatureProperty> properties = feature.getProperties().getFeatureProperties();

    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("height")));
    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("area")));
    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("names.primary")));

    assertFalse(properties.stream().anyMatch(prop -> prop.getKey().equals("theme")));
    assertFalse(properties.stream().anyMatch(prop -> prop.getKey().equals("type")));
    assertFalse(properties.stream().anyMatch(prop -> prop.getKey().equals("version")));
    assertFalse(properties.stream().anyMatch(prop -> prop.getKey().equals("sources")));
    assertFalse(properties.stream().anyMatch(prop -> prop.getKey().equals("bbox")));
  }

  @Test
  @DisplayName("Handle names property scenarios")
  void shouldHandleNamesProperty() {
    // Given
    OvertureItem item = itemWithPolygon("test-names-id");

    item.setProperties(Map.of("names", Map.of("primary", "Primary Name Test")));
    // When
    MultiPolygonFeature feature1 = processor.process(item);

    // Then
    assertNotNull(feature1);
    assertTrue(feature1.getProperties().getFeatureProperties().stream()
        .anyMatch(prop -> prop.getKey().equals("names.primary")
            && prop.getValues().contains("Primary Name Test")));

    item.setProperties(Map.of("names", Map.of("secondary", "Secondary Name")));
    // When
    MultiPolygonFeature feature2 = processor.process(item);

    // Then
    assertNotNull(feature2);
    assertTrue(feature2.getProperties().getFeatureProperties().stream()
        .noneMatch(prop -> prop.getKey().equals("names.primary")));

    Map<String, Object> namesNullPrimary = new java.util.HashMap<>();
    namesNullPrimary.put("primary", null);
    item.setProperties(Map.of("names", namesNullPrimary));
    // When
    MultiPolygonFeature feature3 = processor.process(item);

    // Then
    assertNotNull(feature3);
    assertTrue(feature3.getProperties().getFeatureProperties().stream()
        .noneMatch(prop -> prop.getKey().equals("names.primary")));

    item.setProperties(Map.of("names", Map.of("primary", "")));
    // When
    MultiPolygonFeature feature4 = processor.process(item);

    // Then
    assertNotNull(feature4);
    assertTrue(feature4.getProperties().getFeatureProperties().stream()
        .noneMatch(prop -> prop.getKey().equals("names.primary")));
  }

  @Test
  @DisplayName("Handle processing exception")
  void shouldHandleProcessingException() {
    // Given
    OvertureItem item = itemWithPolygon("test-exception-id");

    item.setProperties(Map.of("problematicProperty", new Object() {
      @Override
      public String toString() {
        throw new RuntimeException("Test exception");
      }
    }));

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNull(feature);
  }

  @Test
  @DisplayName("Calculate area for polygon")
  void shouldCalculateAreaForPolygon() {
    // Given

    OvertureItem item = new OvertureItem();
    item.setId("test-area-polygon-id");
    item.setGeometry(UNIT_SQUARE);
    item.setProperties(Map.of());

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNotNull(feature);
    double expectedArea = FeatureComputationUtils.getAreaInSquareMeters(UNIT_SQUARE);
    assertEquals(expectedArea, feature.getProperties().getArea(), expectedArea * 0.001);
  }

  @Test
  @DisplayName("Calculate area for multipolygon")
  void shouldCalculateAreaForMultiPolygon() {
    // Given
    Polygon polygon2 = geometryFactory.createPolygon(new Coordinate[] {
      new Coordinate(2, 2),
      new Coordinate(3, 2),
      new Coordinate(3, 3),
      new Coordinate(2, 3),
      new Coordinate(2, 2)
    });
    MultiPolygon multiPolygon =
        geometryFactory.createMultiPolygon(new Polygon[] {UNIT_SQUARE, polygon2});

    OvertureItem item = new OvertureItem();
    item.setId("test-area-multipolygon-id");
    item.setGeometry(multiPolygon);
    item.setProperties(Map.of());

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNotNull(feature);
    double expectedArea1 = FeatureComputationUtils.getAreaInSquareMeters(UNIT_SQUARE);
    double expectedArea2 = FeatureComputationUtils.getAreaInSquareMeters(polygon2);
    double expectedTotal = expectedArea1 + expectedArea2;
    assertEquals(expectedTotal, feature.getProperties().getArea(), expectedTotal * 0.001);
  }

  @Test
  @DisplayName("Preserve shared recursive property key format")
  void shouldPreserveSharedRecursivePropertyKeyFormat() {
    // Given
    OvertureItem item = itemWithPolygon("test-snake-case-id");

    item.setProperties(Map.of(
        "buildingHeight", 25.0,
        "buildingArea", 150.0,
        "names", Map.of("primary", "Snake Case Test Building")));

    // When
    MultiPolygonFeature feature = processor.process(item);

    // Then
    assertNotNull(feature);
    List<FeatureProperty> properties = feature.getProperties().getFeatureProperties();

    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("buildingHeight")));
    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("buildingArea")));
    assertTrue(properties.stream().anyMatch(prop -> prop.getKey().equals("names.primary")));
  }

  private OvertureItem itemWithPolygon(String id) {
    OvertureItem it = new OvertureItem();
    it.setId(id);
    it.setGeometry(UNIT_SQUARE);
    return it;
  }
}
