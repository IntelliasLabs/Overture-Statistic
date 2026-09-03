/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Properties with predefined fields and a map for extra custom fields.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class StatisticFeatureProperties {

  /**
   * Feature delivery version.
   */
  @JsonProperty(required = true)
  protected String version;

  /**
   * Feature type, (POI, ROAD, LANE, ADMIN)
   */
  @JsonProperty(required = true)
  protected String featureType;

  /**
   * Feature delivery timestamp. this field should be in conjunction with @{version}.
   */
  @JsonProperty(required = true)
  protected String timestamp;

  /**
   * Extra fields, free format.
   */
  @JsonProperty(required = true)
  protected List<FeatureProperty> featureProperties;

  /**
   * Global Source ID (optional).
   */
  @JsonProperty(required = false)
  protected String globalSourceId;

  @JsonIgnore
  public static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  public StatisticFeatureProperties(String version, String featureType, Date timestamp) {
    this.version = version;
    this.featureType = featureType;
    this.timestamp = FORMATTER.format(timestamp.toInstant().atZone(ZoneOffset.UTC));
  }

  public StatisticFeatureProperties(String version, String featureType, String timestamp) {
    this.version = version;
    this.featureType = featureType;
    this.timestamp = timestamp;
  }

  public StatisticFeatureProperties(
      String version, String featureType, Date timestamp, List<FeatureProperty> featureProperties) {
    this.version = version;
    this.featureType = featureType;
    this.timestamp = FORMATTER.format(timestamp.toInstant().atZone(ZoneOffset.UTC));
    this.featureProperties = featureProperties;
  }

  public StatisticFeatureProperties(
      String version,
      String featureType,
      String timestamp,
      List<FeatureProperty> featureProperties) {
    this.version = version;
    this.featureType = featureType;
    this.timestamp = timestamp;
    this.featureProperties = featureProperties;
    this.globalSourceId = null;
  }
}
