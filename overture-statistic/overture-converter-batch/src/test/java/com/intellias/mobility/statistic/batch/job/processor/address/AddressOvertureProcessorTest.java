/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.address;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;

class AddressOvertureProcessorTest {

  private AddressOvertureProcessor processor;
  private GeometryFactory geometryFactory;

  @BeforeEach
  void setUp() {
    processor = new AddressOvertureProcessor();
    geometryFactory = new GeometryFactory();
  }

  @Test
  void shouldReturnNull_whenGeometryIsNotPoint() {
    LineString line = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1)});

    OvertureItem item = new OvertureItem();
    item.setGeometry(line);

    assertThat(processor.process(item)).isNull();
  }

  @Test
  void shouldMapToPointFeature_whenValidPointAndProperties() {
    // given
    Point point = geometryFactory.createPoint(new Coordinate(10, 20));

    Map<String, Object> sources =
        Map.of("update_time", Instant.parse("2024-01-01T12:00:00Z").toString());

    Map<String, Object> props = new LinkedHashMap<>();
    props.put("sources", sources);
    props.put("customProp", "test");
    props.put("bbox", "shouldBeOmitted");

    OvertureItem item = new OvertureItem();
    item.setId("test-id");
    item.setVersion("2025-08-12");
    item.setGeometry(point);
    item.setProperties(props);

    // when
    PointFeature result = processor.process(item);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getFeatureId()).isEqualTo("test-id");
    assertThat(result.getGeometry()).isNotNull();

    PointFeatureProperties properties = result.getProperties();
    assertThat(properties.getVersion()).isEqualTo("2025-08-12");
    assertThat(properties.getFeatureType()).isEqualTo("address");
    assertThat(properties.getTimestamp()).startsWith("2024-01-01T12:00:00");
    assertThat(properties.getFeatureProperties())
        .extracting("key")
        .contains("customProp")
        .doesNotContain("bbox");
  }

  @Test
  void shouldReturnProcessorName() {
    assertThat(processor.getProcessorName()).isEqualTo("address");
  }
}
