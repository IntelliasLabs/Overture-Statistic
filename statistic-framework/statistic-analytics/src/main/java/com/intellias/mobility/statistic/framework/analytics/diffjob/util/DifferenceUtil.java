/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob.util;

import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeature;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeatureMetadata;
import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.feature.*;
import com.intellias.statistic.model.util.GeoTools;
import com.intellias.statistic.model.util.JtsGeometryConverter;
import java.util.*;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DifferenceUtil {

  public static final String ADDED_KEY = "added";
  public static final String DELETED_KEY = "deleted";

  public static Map<String, Map<String, Set<String>>> compareMaps(
      Map<String, Set<String>> source, Map<String, Set<String>> target) {

    Map<String, Set<String>> deleted = source.entrySet().stream()
        .filter(entry -> !target.containsKey(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));

    Map<String, Set<String>> added = target.entrySet().stream()
        .filter(entry -> !source.containsKey(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, e -> new HashSet<>(e.getValue())));

    source.keySet().stream().filter(target::containsKey).forEach(key -> {
      Set<String> sourceValues = source.get(key);
      Set<String> targetValues = target.get(key);

      Set<String> removedValues = sourceValues.stream()
          .filter(val -> !targetValues.contains(val))
          .collect(Collectors.toSet());

      Set<String> newValues = targetValues.stream()
          .filter(val -> !sourceValues.contains(val))
          .collect(Collectors.toSet());

      if (!removedValues.isEmpty()) {
        deleted.put(key, removedValues);
      }
      if (!newValues.isEmpty()) {
        added.put(key, newValues);
      }
    });

    Map<String, Map<String, Set<String>>> result = new HashMap<>();
    if (!added.isEmpty()) {
      result.put(ADDED_KEY, added);
    }
    if (!deleted.isEmpty()) {
      result.put(DELETED_KEY, deleted);
    }
    return result;
  }

  public DifferencePerFeature compareFeatures(
      StatisticFeature<?> source,
      StatisticFeature<?> target,
      DifferencePerFeatureMetadata differencePerFeatureMetadata) {
    var geometryDifference = getGeometryDiff(source, target);

    var sourceFeatureProperties =
        toFeaturePropertiesMap(source.getProperties().getFeatureProperties());
    var targetFeatureProperties =
        toFeaturePropertiesMap(target.getProperties().getFeatureProperties());
    var featurePropertiesDiff = compareMaps(sourceFeatureProperties, targetFeatureProperties);

    var rangeAttributesDiff = getRangeAttributesDiff(source, target);

    return new DifferencePerFeature(
        differencePerFeatureMetadata,
        geometryDifference == 0.0 ? null : geometryDifference,
        featurePropertiesDiff.get(ADDED_KEY),
        featurePropertiesDiff.get(DELETED_KEY),
        rangeAttributesDiff.get(ADDED_KEY),
        rangeAttributesDiff.get(DELETED_KEY));
  }

  private Map<String, Map<String, Set<String>>> getRangeAttributesDiff(
      StatisticFeature<?> source, StatisticFeature<?> target) {
    if (source instanceof LineFeature lineSource && target instanceof LineFeature lineTarget) {
      var sourceRangeAttributesMap =
          toRangeAttributesMap(lineSource.getProperties().getRangeAttributes());
      var targetRangeAttributesMap =
          toRangeAttributesMap(lineTarget.getProperties().getRangeAttributes());
      return compareMaps(sourceRangeAttributesMap, targetRangeAttributesMap);
    } else if (source instanceof MultiLineFeature sourceMultiLineFeature
        && target instanceof MultiLineFeature targetMultiLineFeature) {
      var sourceRangeAttributesMap =
          toRangeAttributesMap(sourceMultiLineFeature.getProperties().getRangeAttributes());
      var targetRangeAttributesMap =
          toRangeAttributesMap(targetMultiLineFeature.getProperties().getRangeAttributes());
      return compareMaps(sourceRangeAttributesMap, targetRangeAttributesMap);
    } else {
      return Map.of();
    }
  }

  private Double getGeometryDiff(StatisticFeature<?> source, StatisticFeature<?> target) {
    if (source instanceof PointFeature pointSource && target instanceof PointFeature pointTarget) {
      return calculatePointDistance(pointSource, pointTarget);
    } else if (source instanceof LineFeature lineSource
        && target instanceof LineFeature lineTarget) {
      return calculateLineLengthDifference(lineSource.getProperties(), lineTarget.getProperties());
    } else if (source instanceof PolygonFeature polygonSource
        && target instanceof PolygonFeature polygonTarget) {
      return calculatePolygonAreaDifference(
          polygonSource.getProperties(), polygonTarget.getProperties());
    } else if (source instanceof MultiLineFeature multiLineSource
        && target instanceof MultiLineFeature multiLineTarget) {
      return calculateLineLengthDifference(
          multiLineSource.getProperties(), multiLineTarget.getProperties());
    } else if (source instanceof MultiPolygonFeature multiPolygonSource
        && target instanceof MultiPolygonFeature multiPolygonTarget) {
      return calculatePolygonAreaDifference(
          multiPolygonSource.getProperties(), multiPolygonTarget.getProperties());
    } else {
      return null;
    }
  }

  private Double calculatePointDistance(PointFeature source, PointFeature target) {
    return GeoTools.calculateDistance(
        JtsGeometryConverter.toJtsPoint(source.getGeometry()).getCoordinate(),
        JtsGeometryConverter.toJtsPoint(target.getGeometry()).getCoordinate());
  }

  private Double calculateLineLengthDifference(
      LineFeatureProperties sourceProperties, LineFeatureProperties targetProperties) {
    return sourceProperties.getLengthMeters() - targetProperties.getLengthMeters();
  }

  private Double calculatePolygonAreaDifference(
      PolygonFeatureProperties sourceProperties, PolygonFeatureProperties targetProperties) {
    return sourceProperties.getArea() - targetProperties.getArea();
  }

  private Map<String, Set<String>> toFeaturePropertiesMap(List<FeatureProperty> featureProperties) {
    return featureProperties.stream()
        .collect(Collectors.toMap(
            FeatureProperty::getKey,
            featureProperty -> new HashSet<>(featureProperty.getValues())));
  }

  private Map<String, Set<String>> toRangeAttributesMap(List<RangeAttribute> rangeAttributes) {
    return rangeAttributes.stream()
        .flatMap(rangeAttribute -> {
          var key = rangeAttribute.getKey();
          return rangeAttribute.getValues().stream()
              .flatMap(rangeAttributeValue -> rangeAttributeValue.getRanges().stream()
                  .map(range -> new AbstractMap.SimpleEntry<>(
                      key,
                      String.format(
                          "value{%s} range{%.2f - %.2f} length{%.5f}",
                          rangeAttributeValue.getValue(),
                          range.getStart(),
                          range.getEnd(),
                          range.getLengthMeters()))));
        })
        .collect(Collectors.groupingBy(
            Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toSet())));
  }
}
