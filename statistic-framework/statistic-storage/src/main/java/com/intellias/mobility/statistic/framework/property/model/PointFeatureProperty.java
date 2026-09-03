/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.property.model;

import com.intellias.statistic.model.geometry.PointGeometry;
import java.util.Date;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointFeatureProperty extends AbstractFeatureProperty {
  private PointGeometry geometry;

  public PointFeatureProperty(
      String key,
      String value,
      String version,
      Date timestamp,
      String featureDocId,
      String featureType,
      PointGeometry geometry) {
    super(key, value, version, timestamp, featureDocId, featureType);
    this.geometry = geometry;
  }
}
