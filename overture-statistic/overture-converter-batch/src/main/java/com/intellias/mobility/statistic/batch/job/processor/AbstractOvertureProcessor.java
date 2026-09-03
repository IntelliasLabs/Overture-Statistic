/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor;

import com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

/**
 * A base class for all Overture feature processors.
 * It contains common helper methods for processing data.
 */
@Slf4j
public abstract class AbstractOvertureProcessor<T extends StatisticFeature<?>>
    implements OvertureFeatureProcessor<T> {
  protected abstract Set<String> getKeysToOmit();

  protected static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneOffset.UTC);

  /**
   * Extracts and formats the timestamp from the "sources" property.
   * If the timestamp is missing, empty, or invalid, it defaults to the current time.
   */
  protected String getTimestamp(Map<String, Object> properties) {
    return Optional.ofNullable(properties.get("sources"))
        .map(this::getAsMap)
        .map(m -> m.get("update_time"))
        .map(Object::toString)
        .flatMap(this::parseTimestampSafe)
        .orElseGet(Instant::now)
        .atZone(ZoneOffset.UTC)
        .format(TIMESTAMP_FORMATTER);
  }

  private Optional<Instant> parseTimestampSafe(String ts) {
    if (ts == null || ts.isBlank() || "null".equalsIgnoreCase(ts)) return Optional.empty();
    try {
      return Optional.of(Instant.parse(ts));
    } catch (DateTimeParseException e) {
      log.debug("Failed to parse timestamp: '{}'. Using current time.", ts);
      return Optional.empty();
    }
  }

  /**
   * Safely converts an object that is either a Map or a GenericRecord into a Map.
   */
  protected Map<String, Object> getAsMap(Object obj) {
    if (obj instanceof Map<?, ?> rawMap) {
      Map<String, Object> result = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
        Object rawKey = entry.getKey();
        if (rawKey == null) continue;
        String key = rawKey.toString();
        result.put(key, entry.getValue());
      }
      return result;
    }
    if (obj instanceof GenericRecord record) {
      try {
        return record.getSchema().getFields().stream()
            .filter(field -> record.get(field.name()) != null)
            .collect(Collectors.toMap(
                Schema.Field::name,
                field -> record.get(field.name()),
                (u, v) -> {
                  throw new IllegalStateException("Duplicate key: " + u);
                },
                LinkedHashMap::new));
      } catch (IllegalStateException e) {
        log.warn("Duplicate keys detected in GenericRecord", e);
        return Map.of();
      }
    }
    return Map.of();
  }

  /**
   * Extracts feature properties from the given object by recursively traversing its structure.
   * Returns a list of key–value mappings, skipping keys defined in KEYS_TO_OMIT.
   */
  protected List<FeatureProperty> extractFeatureProperties(Map<String, Object> properties) {
    Map<String, List<String>> featurePropertiesMap = new LinkedHashMap<>();
    extractFeaturePropertiesRecursive(properties, new StringBuilder(), featurePropertiesMap);

    return featurePropertiesMap.entrySet().stream()
        .map(e -> new FeatureProperty(e.getKey(), e.getValue()))
        .collect(Collectors.toList());
  }

  /**
   * Recursively traverses the given object to collect feature property keys and values.
   * Supports Map, GenericRecord, and List structures, skipping any keys listed in KEYS_TO_OMIT.
   */
  private void extractFeaturePropertiesRecursive(
      Object obj, StringBuilder keyBuilder, Map<String, List<String>> result) {
    if (obj instanceof Map || obj instanceof GenericRecord) {
      Map<String, Object> map = getAsMap(obj);
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        if (FeatureComputationUtils.RANGE_FIELDS.contains(key)) continue;

        Object value = entry.getValue();
        if (value == null) continue;

        int originalLength = keyBuilder.length();
        if (originalLength > 0) keyBuilder.append(".");
        keyBuilder.append(key);
        String fullKey = keyBuilder.toString();

        if (getKeysToOmit().contains(fullKey)) {
          keyBuilder.setLength(originalLength);
          continue;
        }
        extractFeaturePropertiesRecursive(value, keyBuilder, result);
        keyBuilder.setLength(originalLength);
      }

    } else if (obj instanceof List<?> list) {
      for (Object listItem : list) {
        extractFeaturePropertiesRecursive(listItem, keyBuilder, result);
      }
    } else {
      String valueStr = obj.toString();
      if (valueStr.isBlank()) return;
      result.computeIfAbsent(keyBuilder.toString(), k -> new ArrayList<>()).add(valueStr);
    }
  }
}
