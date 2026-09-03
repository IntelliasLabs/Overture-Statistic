/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.place;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import com.intellias.statistic.model.geometry.PointGeometry;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.*;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceOvertureProcessorTest {

  private PlaceOvertureProcessor processor;
  private GeometryFactory geometryFactory;

  @BeforeEach
  void setUp() {
    processor = new PlaceOvertureProcessor();
    geometryFactory = new GeometryFactory();
  }

  @Test
  void getProcessorName_ShouldReturnPlace() {
    // When
    String result = processor.getProcessorName();

    // Then
    assertEquals("Place", result);
  }

  @Test
  void process_WithInvalidGeometry_ShouldLogErrorAndReturnNull() {
    // Given
    LineString lineGeometry = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});
    OvertureItem item = mock(OvertureItem.class);
    when(item.getId()).thenReturn("test-id");
    when(item.getGeometry()).thenReturn(lineGeometry);

    // When
    PointFeature result = processor.process(item);

    // Then
    assertNull(result);
  }

  @Test
  void process_WithValidGeometry_ShouldReturnPointFeature() {
    // Given
    Point pointGeometry = geometryFactory.createPoint(new Coordinate(0, 0));
    Map<String, Object> properties = createTestProperties();

    OvertureItem item = createOvertureItem("test-id", pointGeometry, "1.0", properties);

    PointGeometry expectedPointGeometry = new PointGeometry(0, 0);

    try (MockedStatic<StatisticGeometryExtractor> mockedExtractor =
        mockStatic(StatisticGeometryExtractor.class)) {
      mockedExtractor
          .when(() -> StatisticGeometryExtractor.getPointGeometry(pointGeometry))
          .thenReturn(expectedPointGeometry);

      PlaceOvertureProcessor spyProcessor = spy(processor);

      // When
      PointFeature result = spyProcessor.process(item);

      // Then
      assertNotNull(result);
      assertEquals("test-id", result.getFeatureId());
      assertEquals(expectedPointGeometry, result.getGeometry());

      PointFeatureProperties resultProperties = result.getProperties();
      assertEquals("Place", resultProperties.getFeatureType());
      assertEquals("1.0", resultProperties.getVersion());
      List<String> actualPropertyKeys = resultProperties.getFeatureProperties().stream()
          .map(FeatureProperty::getKey)
          .toList();
      assertTrue(actualPropertyKeys.contains("name"));
      assertTrue(actualPropertyKeys.contains("name.secondary"));
      assertTrue(actualPropertyKeys.contains("category"));

      FeatureProperty nameProperty = resultProperties.getFeatureProperties().stream()
          .filter(p -> p.getKey().equals("name"))
          .findFirst()
          .orElse(null);
      assertNotNull(nameProperty);
      assertEquals(List.of("Test Place"), nameProperty.getValues());
    }
  }

  @Test
  void process_WithExceptionDuringProcessing_ShouldLogErrorAndReturnNull() {
    // Given
    Point pointGeometry = geometryFactory.createPoint(new Coordinate(10.0, 20.0));
    OvertureItem item = mock(OvertureItem.class);
    when(item.getId()).thenReturn("test-id");
    when(item.getGeometry()).thenReturn(pointGeometry);

    try (MockedStatic<StatisticGeometryExtractor> mockedExtractor =
        mockStatic(StatisticGeometryExtractor.class)) {
      mockedExtractor
          .when(() -> StatisticGeometryExtractor.getPointGeometry(pointGeometry))
          .thenThrow(new RuntimeException("Test exception"));

      // When
      PointFeature result = processor.process(item);

      // Then
      assertNull(result);
    }
  }

  @Test
  void process_WithEmptyProperties_ShouldHandleGracefully() {
    // Given
    Point pointGeometry = geometryFactory.createPoint(new Coordinate(0.0, 0.0));
    Map<String, Object> emptyProperties = new HashMap<>();
    OvertureItem item = mock(OvertureItem.class);
    when(item.getId()).thenReturn("empty-item");
    when(item.getGeometry()).thenReturn(pointGeometry);
    when(item.getVersion()).thenReturn("1.0");
    when(item.getProperties()).thenReturn(emptyProperties);

    PointGeometry expectedGeometry = new PointGeometry(0.0, 0.0);

    try (MockedStatic<StatisticGeometryExtractor> mockedExtractor =
        mockStatic(StatisticGeometryExtractor.class)) {
      mockedExtractor
          .when(() -> StatisticGeometryExtractor.getPointGeometry(pointGeometry))
          .thenReturn(expectedGeometry);

      PlaceOvertureProcessor spyProcessor = spy(processor);

      // When
      PointFeature result = spyProcessor.process(item);

      // Then
      assertNotNull(result);
      assertEquals("empty-item", result.getFeatureId());
      assertEquals(expectedGeometry, result.getGeometry());
      assertEquals("Place", result.getProperties().getFeatureType());
      assertEquals("1.0", result.getProperties().getVersion());
      assertTrue(result.getProperties().getFeatureProperties().isEmpty());
    }
  }

  private OvertureItem createOvertureItem(
      String id, Geometry geometry, String version, Map<String, Object> properties) {
    OvertureItem item = mock(OvertureItem.class);
    lenient().when(item.getId()).thenReturn(id);
    lenient().when(item.getGeometry()).thenReturn(geometry);
    lenient().when(item.getVersion()).thenReturn(version);
    lenient().when(item.getProperties()).thenReturn(properties);
    return item;
  }

  private Map<String, Object> createTestProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("name", List.of("Test Place", Map.of("secondary", "Test Place v2")));
    properties.put("category", "restaurant");
    properties.put("address", "456 Test Ave");
    return properties;
  }
}
