/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.property.model;

import com.intellias.statistic.model.geometry.MultiPointGeometry;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MultiPointFeatureProperty extends AbstractFeatureProperty {
  private MultiPointGeometry geometry;

  public MultiPointFeatureProperty(
      String key,
      String value,
      String version,
      Date timestamp,
      String featureDocId,
      String featureType,
      MultiPointGeometry geometry) {
    super(key, value, version, timestamp, featureDocId, featureType);
    this.geometry = geometry;
  }
}
