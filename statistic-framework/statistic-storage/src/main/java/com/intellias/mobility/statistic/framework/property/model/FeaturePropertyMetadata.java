/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.property.model;

import com.intellias.statistic.model.feature.StatisticFeatureProperties;
import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FeaturePropertyMetadata {
  private String key;
  private String value;
  private String version;
  private String timestamp;
  private String featureType;

  public FeaturePropertyMetadata(
      String key, String value, String version, Date timestamp, String featureType) {
    this(key, value, version, formatTimestamp(timestamp), featureType);
  }

  static String formatTimestamp(Date timestamp) {
    return StatisticFeatureProperties.FORMATTER.format(
        timestamp.toInstant().atZone(java.time.ZoneOffset.UTC));
  }
}
