/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intellias.statistic.model.geometry.MultiPointGeometry;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("Feature")
public class MultiPointFeature implements StatisticFeature<MultiPointGeometry> {
  private String featureId;
  private MultiPointGeometry geometry;
  private PointFeatureProperties properties;
}
