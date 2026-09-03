/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Point feature properties.
 */
@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointFeatureProperties extends StatisticFeatureProperties {

  public PointFeatureProperties(
      String version, String featureType, Date timestamp, List<FeatureProperty> featureProperties) {
    super(version, featureType, timestamp, featureProperties);
  }

  public PointFeatureProperties(String version, String featureType, Date timestamp) {
    super(version, featureType, timestamp);
  }
}
