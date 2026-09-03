/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils;
import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.*;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReproductionTest {

  private AbstractOvertureProcessor<?> processor;

  @BeforeEach
  void setUp() {
    processor = new AbstractOvertureProcessor<>() {
      @Override
      protected Set<String> getKeysToOmit() {
        return Set.of();
      }

      @Override
      public StatisticFeature<?> process(OvertureItem item) {
        return null;
      }

      @Override
      public String getProcessorName() {
        return "repro";
      }
    };
  }

  @Test
  void reproduceArrayExtractionBug() {
    // Scenario: An array of complex objects (GenericRecords)
    Schema innerSchema = Schema.createRecord("Inner", null, null, false);
    innerSchema.setFields(
        List.of(new Schema.Field("value", Schema.create(Schema.Type.STRING), null, null)));

    GenericRecord rec0 = new GenericData.Record(innerSchema);
    rec0.put("value", "A");
    GenericRecord rec1 = new GenericData.Record(innerSchema);
    rec1.put("value", "B");

    Map<String, Object> props = Map.of("my_array", List.of(rec0, rec1));

    List<FeatureProperty> result = processor.extractFeatureProperties(props);

    // CURRENT behavior: produces "my_array.value" with values ["A", "B"]
    assertThat(result).extracting(FeatureProperty::getKey).containsOnly("my_array.value");

    FeatureProperty fp = result.stream()
        .filter(p -> p.getKey().equals("my_array.value"))
        .findFirst()
        .orElseThrow();

    assertThat(fp.getValues()).containsExactlyInAnyOrder("A", "B");
  }

  @Test
  void reproduceRangeParsingCorrectness() {
    // Scenario: speed_limits with ranges provided by the user.

    Map<String, Object> limit1 = new HashMap<>();
    limit1.put("min_speed", null);
    limit1.put("max_speed", Map.of("value", 50, "unit", "km/h"));
    limit1.put("is_max_speed_variable", null);
    limit1.put("when", null);
    limit1.put("between", List.of(0.0, 0.074907137));

    Map<String, Object> limit2 = new HashMap<>();
    limit2.put("min_speed", null);
    limit2.put("max_speed", Map.of("value", 70, "unit", "km/h"));
    limit2.put("is_max_speed_variable", null);
    limit2.put("when", null);
    limit2.put("between", List.of(0.074907137, 0.138038736));

    Map<String, Object> limit3 = new HashMap<>();
    limit3.put("min_speed", null);
    limit3.put("max_speed", Map.of("value", 100, "unit", "km/h"));
    limit3.put("is_max_speed_variable", null);
    limit3.put("when", null);
    limit3.put("between", List.of(0.138038736, 1.0));

    OvertureItem item = new OvertureItem();
    item.setProperties(Map.of("speed_limits", List.of(limit1, limit2, limit3)));

    List<RangeAttribute> result = FeatureComputationUtils.getRangeAttributes(item);

    // Expected: 2 RangeAttributes: "max_speed.value" and "max_speed.unit"
    assertThat(result).hasSize(2);

    RangeAttribute valueAttr = result.stream()
        .filter(ra -> ra.getKey().equals("speed_limits.max_speed.value"))
        .findFirst()
        .orElseThrow();
    assertThat(valueAttr.getValues()).hasSize(3); // 50, 70, 100

    RangeAttribute unitAttr = result.stream()
        .filter(ra -> ra.getKey().equals("speed_limits.max_speed.unit"))
        .findFirst()
        .orElseThrow();
    assertThat(unitAttr.getValues()).hasSize(1); // "km/h"
    assertThat(unitAttr.getValues().getFirst().getRanges()).hasSize(3);
  }
}
