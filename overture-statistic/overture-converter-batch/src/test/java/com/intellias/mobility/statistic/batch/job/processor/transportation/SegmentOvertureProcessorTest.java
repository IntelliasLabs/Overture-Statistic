/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.transportation;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.attribute.Range;
import com.intellias.statistic.model.feature.LineFeature;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

/**
 * Unit tests for {@link SegmentOvertureProcessor}.
 */
class SegmentOvertureProcessorTest {

  private GeometryFactory geometryFactory;
  private SegmentOvertureProcessor processor;

  @BeforeEach
  void setUp() {
    processor = new SegmentOvertureProcessor();
    geometryFactory = new GeometryFactory();
  }

  @Test
  @DisplayName("Should return LineFeature when geometry is LineString")
  void shouldReturnLineFeatureWhenGeometryIsLineString() {
    // GIVEN
    LineString line = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(21.0, 52.0), new Coordinate(21.1, 52.1)});
    Map<String, Object> sources = Map.of("update_time", "2024-01-01T12:00:00Z");
    Map<String, Object> properties = new HashMap<>();

    Map<String, Object> accessRestrictions = new HashMap<>();
    accessRestrictions.put("access_type", "allowed");

    Map<String, Object> when = new HashMap<>();
    when.put("recognized", "as_private");
    accessRestrictions.put("when", when);

    List<String> betweenValues = List.of("0.0", "0.747572001");
    accessRestrictions.put("between", betweenValues);

    List<Map<String, Object>> connectors = List.of(
        Map.of(
            "connector_id", "c5cb0241-000a-4ab3-a61d-255c28592b3d",
            "at", "0.0"),
        Map.of(
            "connector_id", "56dd65cb-b62b-473e-a268-c5ac85603a3f",
            "at", "1.0"));

    properties.put("connectors", connectors);
    properties.put("access_restrictions", accessRestrictions);
    properties.put("subtype", "road");
    properties.put("sources", sources);

    OvertureItem item = new OvertureItem();
    item.setId("seg-1");
    item.setGeometry(line);
    item.setVersion("2");
    item.setProperties(properties);

    // WHEN
    LineFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNotNull();
    assertThat(feature.getFeatureId()).isEqualTo("seg-1");
    assertThat(feature.getGeometry().getCoordinates()).hasSize(2);

    var propsOut = feature.getProperties();
    assertThat(propsOut.getVersion()).isEqualTo("2");
    assertThat(propsOut.getFeatureType()).isEqualTo("Segment");
    assertThat(propsOut.getTimestamp()).isEqualTo("2024-01-01T12:00:00.000+0000");

    assertThat(propsOut.getFeatureProperties())
        .anyMatch(p -> p.getKey().equals("subtype") && p.getValues().contains("road"));

    assertThat(propsOut.getRangeAttributes())
        .anyMatch(p -> p.getKey().equals("access_restrictions.access_type")
            && p.getValues().getFirst().getValue().equals("allowed")
            && p.getValues().getFirst().getRanges().getFirst().equals(new Range(0, 0.747572001)));

    assertThat(propsOut.getRangeAttributes())
        .anyMatch(p -> p.getKey().equals("access_restrictions.when.recognized")
            && p.getValues().getFirst().getValue().contains("as_private"));

    assertThat(propsOut.getFeatureProperties())
        .anyMatch(p -> p.getKey().equals("connectors.connector_id")
            && p.getValues().contains("c5cb0241-000a-4ab3-a61d-255c28592b3d"));

    assertThat(propsOut.getFeatureProperties())
        .anyMatch(p -> p.getKey().equals("connectors.at") && p.getValues().contains("0.0"));

    assertThat(propsOut.getFeatureProperties())
        .anyMatch(p -> p.getKey().equals("connectors.connector_id")
            && p.getValues().contains("56dd65cb-b62b-473e-a268-c5ac85603a3f"));

    assertThat(propsOut.getFeatureProperties())
        .anyMatch(p -> p.getKey().equals("connectors.at") && p.getValues().contains("1.0"));

    assertThat(propsOut.getRangeAttributes()).isNotEmpty();
  }

  @Test
  @DisplayName("Should return null when geometry is not a LineString")
  void shouldReturnNullWhenGeometryIsNotLineString() {
    // GIVEN
    Point point = geometryFactory.createPoint(new Coordinate(21.0, 52.0));
    OvertureItem item = new OvertureItem();
    item.setId("seg-2");
    item.setGeometry(point);
    item.setVersion("1");
    item.setProperties(Map.of());

    // WHEN
    LineFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNull();
  }

  @Test
  @DisplayName("Should fallback to current timestamp if missing")
  void shouldFallbackToCurrentTimestampIfMissing() {
    // GIVEN
    LineString line = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(21.0, 52.0), new Coordinate(21.1, 52.1)});

    OvertureItem item = new OvertureItem();
    item.setId("seg-3");
    item.setGeometry(line);
    item.setVersion("1");
    item.setProperties(Map.of());

    // WHEN
    LineFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNotNull();
    assertThat(feature.getProperties().getTimestamp())
        .startsWith(Instant.now().toString().substring(0, 10));
  }

  @Test
  @DisplayName("Should fallback to now if update_time is unparsable")
  void shouldFallbackIfUpdateTimeUnparsable() {
    // GIVEN
    LineString line = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(21.0, 52.0), new Coordinate(21.1, 52.1)});
    Map<String, Object> sources = Map.of("update_time", "BAD_DATE");

    OvertureItem item = new OvertureItem();
    item.setId("seg-4");
    item.setGeometry(line);
    item.setVersion("1");
    item.setProperties(Map.of("sources", sources));

    // WHEN
    LineFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNotNull();
    assertThat(feature.getProperties().getTimestamp())
        .startsWith(Instant.now().toString().substring(0, 10));
  }

  @Test
  @DisplayName("Should handle empty properties map gracefully")
  void shouldHandleEmptyProperties() {
    // GIVEN
    LineString line = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(21.0, 52.0), new Coordinate(21.1, 52.1)});
    OvertureItem item = new OvertureItem();
    item.setId("seg-5");
    item.setGeometry(line);
    item.setVersion("1");
    item.setProperties(Collections.emptyMap());

    // WHEN
    LineFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNotNull();
    assertThat(feature.getProperties().getFeatureProperties()).isEmpty();
    assertThat(feature.getProperties().getRangeAttributes()).isNotNull();
  }

  @Test
  @DisplayName("rangeAttribute should be [0, 1] when no 'between' field present")
  void shouldReturnEmptyRangeAttributesIfNoBetween() {
    // GIVEN
    LineString line = geometryFactory.createLineString(
        new Coordinate[] {new Coordinate(21.0, 52.0), new Coordinate(21.1, 52.1)});

    Map<String, Object> properties = new HashMap<>();
    properties.put(
        "access_restrictions",
        Map.of("access_type", "allowed", "when", Map.of("recognized", "as_private")));
    properties.put("sources", Map.of("update_time", "2024-01-01T12:00:00Z"));

    OvertureItem item = new OvertureItem();
    item.setId("seg-6");
    item.setGeometry(line);
    item.setVersion("2");
    item.setProperties(properties);

    // WHEN
    LineFeature feature = processor.process(item);

    // THEN
    assertThat(feature).isNotNull();
    assertThat(feature
            .getProperties()
            .getRangeAttributes()
            .getFirst()
            .getValues()
            .getFirst()
            .getRanges())
        .isEqualTo(List.of(new Range(0, 1)));
  }
}
