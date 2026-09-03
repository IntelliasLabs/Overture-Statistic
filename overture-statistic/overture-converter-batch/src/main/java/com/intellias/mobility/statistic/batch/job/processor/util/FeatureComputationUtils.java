/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.util;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.attribute.Range;
import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.attribute.RangeAttributeValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.PolygonArea;
import net.sf.geographiclib.PolygonResult;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

@UtilityClass
public final class FeatureComputationUtils {
  private static final String BETWEEN_FIELD = "between";
  public static final Set<String> RANGE_FIELDS = Set.of(
      "routes",
      "prohibited_transitions",
      "road_surface",
      "road_flags",
      "speed_limits",
      "width_rules",
      "subclass_rules",
      "access_restrictions",
      "level_rules",
      "rail_flags",
      "names.rules");

  public static double getAreaInSquareMeters(Polygon polygon) {
    double area = ringArea(polygon.getExteriorRing().getCoordinates());

    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      area -= ringArea(polygon.getInteriorRingN(i).getCoordinates());
    }

    return area;
  }

  public static List<RangeAttribute> getRangeAttributes(OvertureItem overtureItem) {
    Map<String, Object> properties = overtureItem.getProperties();
    if (properties == null || properties.isEmpty()) {
      return Collections.emptyList();
    }

    Map<String, Map<String, List<Range>>> collectedRangeProps = new LinkedHashMap<>();

    RANGE_FIELDS.forEach(fieldPath -> {
      Object rangeFieldObj = resolveNestedField(properties, fieldPath);
      if (rangeFieldObj == null) return;
      processRangeField(rangeFieldObj, collectedRangeProps, fieldPath);
    });

    return collectedRangeProps.entrySet().stream()
        .map(entry -> {
          String key = entry.getKey();
          List<RangeAttributeValue> values = entry.getValue().entrySet().stream()
              .map(vEntry -> new RangeAttributeValue(vEntry.getKey(), vEntry.getValue()))
              .toList();
          return new RangeAttribute(key, values);
        })
        .toList();
  }

  private static Object resolveNestedField(Map<String, Object> properties, String fieldPath) {
    if (fieldPath == null || fieldPath.isEmpty()) return null;

    String[] pathParts = fieldPath.split("\\.");
    Object current = properties;

    for (String part : pathParts) {
      if (current == null) return null;
      Map<String, Object> currentMap = getAsMap(current);
      current = currentMap.get(part);
    }

    return current;
  }

  private static void processRangeField(
      Object rangeFieldObj,
      Map<String, Map<String, List<Range>>> collectedRangeProps,
      String fieldPath) {
    if (rangeFieldObj instanceof List<?> list) {
      list.forEach(item -> processRangeItem(item, collectedRangeProps, fieldPath));
    } else {
      processRangeItem(rangeFieldObj, collectedRangeProps, fieldPath);
    }
  }

  private static void processRangeItem(
      Object item, Map<String, Map<String, List<Range>>> collectedRangeProps, String prefix) {
    Map<String, Object> map = getAsMap(item);
    Object betweenObj = map.get(BETWEEN_FIELD);
    if (!(betweenObj instanceof List<?> between) || between.size() < 2) betweenObj = List.of(0, 1);

    Range range = new Range(
        Double.parseDouble(((List<?>) betweenObj).get(0).toString()),
        Double.parseDouble(((List<?>) betweenObj).get(1).toString()));

    extractRangePropertiesRecursive(map, range, prefix, collectedRangeProps);
  }

  private static void extractRangePropertiesRecursive(
      Object obj, Range range, String prefix, Map<String, Map<String, List<Range>>> results) {
    if (obj instanceof Map<?, ?> map) {
      map.forEach((k, v) -> {
        String key = k.toString();
        if (BETWEEN_FIELD.equals(key)) return;
        String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
        extractRangePropertiesRecursive(v, range, fullKey, results);
      });
    } else if (obj instanceof GenericRecord record) {
      record.getSchema().getFields().stream()
          .filter(f -> record.get(f.name()) != null)
          .forEach(f -> {
            String key = f.name();
            if (BETWEEN_FIELD.equals(key)) return;
            Object v = record.get(key);
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            extractRangePropertiesRecursive(v, range, fullKey, results);
          });
    } else if (obj != null) {
      String valueStr = obj.toString();
      results
          .computeIfAbsent(prefix, k -> new LinkedHashMap<>())
          .computeIfAbsent(valueStr, k -> new ArrayList<>())
          .add(range);
    }
  }

  private static Map<String, Object> getAsMap(Object obj) {
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
      return record.getSchema().getFields().stream()
          .filter(field -> record.get(field.name()) != null)
          .collect(Collectors.toMap(
              Schema.Field::name,
              field -> record.get(field.name()),
              (u, v) -> u,
              LinkedHashMap::new));
    }
    return Map.of();
  }

  private static double ringArea(Coordinate[] coordinates) {
    PolygonArea polygonArea = new PolygonArea(Geodesic.WGS84, false);
    for (Coordinate coordinate : coordinates) {
      polygonArea.AddPoint(coordinate.y, coordinate.x); // lat, lon
    }
    PolygonResult result = polygonArea.Compute(false, true);
    return Math.abs(result.area);
  }
}
