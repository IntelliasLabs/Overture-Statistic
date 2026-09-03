/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.property.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Date;
import lombok.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
public abstract class AbstractFeatureProperty {
  private String key;
  private String value;
  private String version;
  private String timestamp;
  private String featureDocId;
  private String featureType;
  private FeaturePropertyMetadata properties;

  protected AbstractFeatureProperty(
      String key,
      String value,
      String version,
      Date timestamp,
      String featureDocId,
      String featureType) {
    this(
        key,
        value,
        version,
        FeaturePropertyMetadata.formatTimestamp(timestamp),
        featureDocId,
        featureType);
  }

  protected AbstractFeatureProperty(
      String key,
      String value,
      String version,
      String timestamp,
      String featureDocId,
      String featureType) {
    this.key = key;
    this.value = value;
    this.version = version;
    this.timestamp = timestamp;
    this.featureDocId = featureDocId;
    this.featureType = featureType;
    this.properties = new FeaturePropertyMetadata(key, value, version, timestamp, featureType);
  }
}
