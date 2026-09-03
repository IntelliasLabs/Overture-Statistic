/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.util.BuildingTestData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

/**
 * Unit tests for {@link MockOvertureProcessor}.
 * These tests verify the processor's logic for creating dynamic test data.
 */
class MockOvertureProcessorTest {

  private final GeometryFactory geometryFactory = new GeometryFactory();

  @Test
  @DisplayName("Should create a new feature using coordinates from the input item")
  void shouldCreateFeatureFromInputCoordinates() {
    // Given
    var processor = new MockOvertureProcessor();
    var inputItem = mock(OvertureItem.class);

    String id = "test-id-123";
    double lon = 10.0;
    double lat = 20.0;
    Point mockPoint = geometryFactory.createPoint(new Coordinate(lon, lat));

    when(inputItem.getId()).thenReturn(id);
    when(inputItem.getGeometry()).thenReturn(mockPoint);

    var expectedFeature = BuildingTestData.createFeatureAt(id, lon, lat);

    // When
    var actualFeature = processor.process(inputItem);

    // Then
    assertThat(actualFeature).isEqualTo(expectedFeature);
  }

  @Test
  @DisplayName("Should return null when input item is null")
  void shouldReturnNullWhenInputIsNull() {
    // Given
    var processor = new MockOvertureProcessor();

    // When
    var result = processor.process(null);

    // Then
    assertThat(result).isNull();
  }

  @Test
  @DisplayName("Should return null when input geometry is null")
  void shouldReturnNullForMissingGeometry() {
    // Given
    var processor = new MockOvertureProcessor();
    var itemWithNullGeometry = mock(OvertureItem.class);
    when(itemWithNullGeometry.getGeometry()).thenReturn(null);

    // When
    var result = processor.process(itemWithNullGeometry);

    // Then
    assertThat(result).isNull();
  }
}
