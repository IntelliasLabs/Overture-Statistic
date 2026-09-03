/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.property.model;

import com.intellias.statistic.model.geometry.PolygonGeometry;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolygonFeatureProperty extends AbstractFeatureProperty {
  private PolygonGeometry geometry;

  public PolygonFeatureProperty(
      String key,
      String value,
      String version,
      Date timestamp,
      String featureDocId,
      String featureType,
      PolygonGeometry geometry) {
    super(key, value, version, timestamp, featureDocId, featureType);
    this.geometry = geometry;
  }
}
