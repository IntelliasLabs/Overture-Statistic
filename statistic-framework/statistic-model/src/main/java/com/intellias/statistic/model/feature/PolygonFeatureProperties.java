/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import java.util.Date;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PolygonFeatureProperties extends StatisticFeatureProperties {
  private double area;

  public PolygonFeatureProperties(String version, String featureType, Date timestamp) {
    super(version, featureType, timestamp);
  }

  public PolygonFeatureProperties(String version, String featureType, Date timestamp, double area) {
    super(version, featureType, timestamp);
    this.area = area;
  }

  public PolygonFeatureProperties(
      String version, String featureType, Date timestamp, List<FeatureProperty> featureProperties) {
    super(version, featureType, timestamp, featureProperties);
  }

  public PolygonFeatureProperties(
      String version,
      String featureType,
      Date timestamp,
      List<FeatureProperty> featureProperties,
      double area) {
    super(version, featureType, timestamp, featureProperties);
    this.area = area;
  }
}
