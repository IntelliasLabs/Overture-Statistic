/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractOvertureProcessorTest {

  private AbstractOvertureProcessor<?> processor;

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneOffset.UTC);

  @BeforeEach
  void setUp() {
    processor = new AbstractOvertureProcessor<>() {
      @Override
      protected Set<String> getKeysToOmit() {
        return Set.of();
      }

      private static final String PROCESSOR_NAME = "test";

      @Override
      public StatisticFeature<?> process(OvertureItem item) throws Exception {
        return null;
      }

      @Override
      public String getProcessorName() {
        return PROCESSOR_NAME;
      }
    };
  }

  @Test
  void testGetTimestampWithValidTimestampString() {
    Map<String, Object> sources = new HashMap<>();
    sources.put("update_time", "2025-08-08T12:34:56.789Z");

    Map<String, Object> props = Map.of("sources", sources);

    String ts = processor.getTimestamp(props);

    assertThat(ts).isEqualTo("2025-08-08T12:34:56.789+0000");
  }

  @Test
  void testGetTimestampWithNullOrMissingUpdateTimeReturnsNow() {
    Map<String, Object> props1 = Map.of("sources", Map.of());
    String ts1 = processor.getTimestamp(props1);
    assertNotNull(ts1);

    Map<String, Object> props2 = Map.of();
    String ts2 = processor.getTimestamp(props2);
    assertNotNull(ts2);

    // Should parse date or fallback, so both non-null and parseable or current time string
    Instant parsed1 = Instant.from(FORMATTER.parse(ts1));
    Instant parsed2 = Instant.from(FORMATTER.parse(ts2));
    assertNotNull(parsed1);
    assertNotNull(parsed2);
  }

  @Test
  void testGetTimestampWithInvalidTimestampStringLogsAndReturnsNow() {
    Map<String, Object> sources = new HashMap<>();
    sources.put("update_time", "invalid-timestamp");

    Map<String, Object> props = Map.of("sources", sources);

    String ts = processor.getTimestamp(props);

    // Fallback to current time, so result should be parseable instant (current-ish time)
    Instant parsed = Instant.from(FORMATTER.parse(ts));
    assertNotNull(parsed);
  }

  @Test
  void testGetAsMapFromMap() {
    Map<String, Object> input = Map.of("key1", "value1", "key2", 123);
    Map<String, Object> result = processor.getAsMap(input);

    assertEquals(2, result.size());
    assertEquals("value1", result.get("key1"));
    assertEquals(123, result.get("key2"));
  }

  @Test
  void testGetAsMapFromGenericRecord() {
    Schema schema = Schema.createRecord("Test", null, null, false);
    schema.setFields(List.of(
        new Schema.Field("field1", Schema.create(Schema.Type.STRING), null, null),
        new Schema.Field("field2", Schema.create(Schema.Type.INT), null, null)));
    GenericRecord record = new GenericData.Record(schema);
    record.put("field1", "value1");
    record.put("field2", 123);

    Map<String, Object> map = processor.getAsMap(record);
    assertEquals("value1", map.get("field1"));
    assertEquals(123, map.get("field2"));
  }

  @Test
  void testExtractFeaturePropertiesNestedListAndMap() {
    Map<String, Object> nestedMap = Map.of("innerKey", "innerValue");
    List<Object> list = List.of("listValue1", nestedMap);
    Map<String, Object> obj = Map.of("listField", list);

    List<FeatureProperty> props = processor.extractFeatureProperties(obj);
    assertThat(props).extracting(FeatureProperty::getKey).contains("listField.innerKey");
    assertThat(props)
        .extracting(FeatureProperty::getValues)
        .anyMatch(values -> values.contains("innerValue"));
  }
}
