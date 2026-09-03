/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intellias.statistic.model.geometry.StatisticGeometry;

/**
 * Base Feature interface.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
@JsonTypeName("Feature")
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface StatisticFeature<T extends StatisticGeometry> {

  @JsonProperty("type")
  default String obtainType() {
    return "Feature";
  }

  @JsonProperty("type")
  default void retrieveType(String type) {}

  @JsonProperty("featureId")
  String getFeatureId();

  @JsonProperty("geometry")
  T getGeometry();

  @JsonProperty("properties")
  StatisticFeatureProperties getProperties();
}
