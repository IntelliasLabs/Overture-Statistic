/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.property;

import com.intellias.mobility.statistic.framework.property.model.*;
import com.intellias.statistic.model.feature.StatisticFeature;
import com.intellias.statistic.model.feature.StatisticFeatureProperties;
import com.intellias.statistic.model.geometry.*;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;

public class FeaturePropertyDocumentsBuilderImpl implements FeaturePropertyDocumentsBuilder {

  @Override
  public List<AbstractFeatureProperty> buildFeaturePropertyDocuments(
      StatisticFeature<?> statisticFeature) {
    var featureProperties = Optional.ofNullable(
            statisticFeature.getProperties().getFeatureProperties())
        .orElse(List.of());
    return featureProperties.stream()
        .flatMap(featureProperty -> featureProperty.getValues().stream()
            .map(value -> buildFeatureProperty(statisticFeature, featureProperty.getKey(), value)))
        .toList();
  }

  @SneakyThrows
  private AbstractFeatureProperty buildFeatureProperty(
      StatisticFeature<?> feature, String key, String value) {
    var geometry = feature.getGeometry();
    var version = feature.getProperties().getVersion();
    var timestampString = feature.getProperties().getTimestamp();
    var timestamp = Date.from(StatisticFeatureProperties.FORMATTER
        .parse(timestampString, ZonedDateTime::from)
        .toInstant());
    var id = feature.getFeatureId();
    var featureType = feature.getProperties().getFeatureType();

    return switch (geometry) {
      case PointGeometry pointGeometry -> new PointFeatureProperty(
          key, value, version, timestamp, id, featureType, pointGeometry);
      case LineGeometry lineGeometry -> new LineFeatureProperty(
          key, value, version, timestamp, id, featureType, lineGeometry);
      case PolygonGeometry polygonGeometry -> new PolygonFeatureProperty(
          key, value, version, timestamp, id, featureType, polygonGeometry);
      case MultiPointGeometry multiPointGeometry -> new MultiPointFeatureProperty(
          key, value, version, timestamp, id, featureType, multiPointGeometry);
      case MultiLineGeometry multiLineGeometry -> new MultiLineFeatureProperty(
          key, value, version, timestamp, id, featureType, multiLineGeometry);
      case MultiPolygonGeometry multiPolygonGeometry -> new MultiPolygonFeatureProperty(
          key, value, version, timestamp, id, featureType, multiPolygonGeometry);
      case StatisticGeometryCollection geometryCollection -> new GeometryCollectionFeatureProperty(
          key, value, version, timestamp, id, featureType, geometryCollection);
      default -> throw new IllegalArgumentException("Unknown geometry type");
    };
  }
}
