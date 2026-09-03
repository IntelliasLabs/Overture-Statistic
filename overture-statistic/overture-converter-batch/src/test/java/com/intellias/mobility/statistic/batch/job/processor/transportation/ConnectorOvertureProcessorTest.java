/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.transportation;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.feature.PointFeature;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

/**
 * Unit tests for {@link ConnectorOvertureProcessor}.
 */
class ConnectorOvertureProcessorTest {

  private GeometryFactory geometryFactory;
  private ConnectorOvertureProcessor processor;

  @BeforeEach
  void setUp() {
    processor = new ConnectorOvertureProcessor();
    geometryFactory = new GeometryFactory();
  }

  @Test
  @DisplayName("Should return PointFeature when geometry is Point")
  void shouldReturnPointFeatureWhenGeometryIsPoint() {
    // GIVEN
    Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(21.0, 52.0));
    Map<String, Object> sources = Map.of("update_time", "2024-01-01T12:00:00Z");

    OvertureItem item = new OvertureItem();
    item.setId("conn-1");
    item.setGeometry(point);
    item.setVersion("1");
    item.setProperties(Map.of("sources", sources));

    // WHEN
    PointFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNotNull();
    assertThat(feature.getFeatureId()).isEqualTo("conn-1");
    assertThat(feature.getGeometry().getLat()).isEqualTo(52.0);
    assertThat(feature.getGeometry().getLon()).isEqualTo(21.0);

    var propsOut = feature.getProperties();
    assertThat(propsOut.getVersion()).isEqualTo("1");
    assertThat(propsOut.getFeatureType()).isEqualTo("Connector");
    assertThat(propsOut.getTimestamp()).isEqualTo("2024-01-01T12:00:00.000+0000");
  }

  @Test
  @DisplayName("Should fallback to current timestamp if one is missing")
  void shouldFallbackToCurrentTimestampIfMissing() {
    // GIVEN
    Point point = geometryFactory.createPoint(new org.locationtech.jts.geom.Coordinate(21.0, 52.0));

    OvertureItem item = new OvertureItem();
    item.setId("conn-2");
    item.setGeometry(point);
    item.setVersion("1");
    item.setProperties(Map.of());

    // WHEN
    PointFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNotNull();
    assertThat(feature.getProperties().getTimestamp())
        .startsWith(Instant.now().toString().substring(0, 10));
  }

  @Test
  @DisplayName("Should return null when geometry is not a Point")
  void shouldReturnNullWhenGeometryIsNotPoint() {
    // GIVEN
    LineString line = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(21.0, 52.0), new Coordinate(21.1, 52.1)});
    OvertureItem item = new OvertureItem();
    item.setId("conn-3");
    item.setGeometry(line);
    item.setVersion("1");
    item.setProperties(Map.of());

    // WHEN
    PointFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNull();
  }

  @Test
  @DisplayName("Should fallback to now if update_time is unparsable")
  void shouldFallbackIfUpdateTimeUnparsable() {
    // GIVEN
    Point point = geometryFactory.createPoint(new Coordinate(21.0, 52.0));
    Map<String, Object> sources = Map.of("update_time", "BAD_DATE");
    OvertureItem item = new OvertureItem();
    item.setId("conn-4");
    item.setGeometry(point);
    item.setVersion("1");
    item.setProperties(Map.of("sources", sources));

    // WHEN
    PointFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNotNull();
    assertThat(feature.getProperties().getTimestamp())
        .startsWith(Instant.now().toString().substring(0, 10));
  }

  @Test
  @DisplayName("Should handle empty properties map gracefully")
  void shouldHandleEmptyProperties() {
    // GIVEN
    Point point = geometryFactory.createPoint(new Coordinate(21.0, 52.0));
    OvertureItem item = new OvertureItem();
    item.setId("conn-5");
    item.setGeometry(point);
    item.setVersion("1");
    item.setProperties(Collections.emptyMap());

    // WHEN
    PointFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNotNull();
    assertThat(feature.getProperties().getFeatureProperties()).isEmpty();
  }
}
