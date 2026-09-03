/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellias.statistic.model.attribute.RangeAttribute;
import java.util.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;

@SuppressWarnings("unused")
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class LineFeatureProperties extends StatisticFeatureProperties {

  @JsonProperty(required = true)
  private double lengthMeters;

  /**
   * Map[ATTRIBUTE_KEY, AttribAttributeRangeProperties]
   */
  @JsonProperty(required = true)
  private List<RangeAttribute> rangeAttributes;

  public LineFeatureProperties(String version, String featureType, Date timestamp) {
    super(version, featureType, timestamp);
    this.rangeAttributes = new ArrayList<>();
  }

  public LineFeatureProperties(
      String version, String featureType, Date timestamp, List<FeatureProperty> featureProperties) {
    super(version, featureType, timestamp, featureProperties);
    this.rangeAttributes = new ArrayList<>();
  }

  public LineFeatureProperties(
      String version,
      String featureType,
      Date timestamp,
      List<FeatureProperty> featureProperties,
      List<RangeAttribute> rangeAttributes) {
    super(version, featureType, timestamp, featureProperties);
    this.rangeAttributes = rangeAttributes;
  }

  public LineFeatureProperties(
      String version, String featureType, Date timestamp, double lengthMeters) {
    super(version, featureType, timestamp);
    this.rangeAttributes = new ArrayList<>();
    this.lengthMeters = lengthMeters;
  }

  public LineFeatureProperties(
      String version,
      String featureType,
      Date timestamp,
      double lengthMeters,
      List<FeatureProperty> featureProperties) {
    super(version, featureType, timestamp, featureProperties);
    this.rangeAttributes = new ArrayList<>();
    this.lengthMeters = lengthMeters;
  }

  @SneakyThrows
  public void setTimestamp(String timestamp) {
    var date = FORMATTER.parse(timestamp);
    this.timestamp = timestamp;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setFeatureType(String featureType) {
    this.featureType = featureType;
  }

  public void setFeatureProperties(List<FeatureProperty> featureProperties) {
    this.featureProperties = featureProperties;
  }

  public static LineFeatureProperties withRangeAttributes(
      String version, String featureType, Date timestamp, List<RangeAttribute> rangeAttributes) {
    LineFeatureProperties lineFeatureProperties =
        new LineFeatureProperties(version, featureType, timestamp, new ArrayList<>());
    lineFeatureProperties.setRangeAttributes(rangeAttributes);
    return lineFeatureProperties;
  }
}
