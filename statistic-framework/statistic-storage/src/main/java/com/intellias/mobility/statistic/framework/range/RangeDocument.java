/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.range;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.intellias.mobility.statistic.framework.property.model.AbstractFeatureProperty;
import com.intellias.statistic.model.geometry.MultiLineGeometry;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Derivative document stored in {@code rangeattribute-*} indices.
 *
 * <p>The same document shape is consumed by analytics dashboards and index discovery, so ingress
 * now writes this shared model directly instead of relying on a later analytics batch job.</p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "class")
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RangeDocument extends AbstractFeatureProperty {
  private double lengthMeters;
  private MultiLineGeometry geometry;

  public RangeDocument(
      String key,
      String value,
      String version,
      String timestamp,
      double lengthMeters,
      String featureDocId,
      String featureType,
      MultiLineGeometry geometry) {
    super(key, value, version, timestamp, featureDocId, featureType);
    this.lengthMeters = lengthMeters;
    this.geometry = geometry;
  }

  public RangeDocument(
      String key,
      String version,
      String timestamp,
      double lengthMeters,
      String featureDocId,
      String featureType,
      MultiLineGeometry geometry) {
    this(key, null, version, timestamp, lengthMeters, featureDocId, featureType, geometry);
  }
}
