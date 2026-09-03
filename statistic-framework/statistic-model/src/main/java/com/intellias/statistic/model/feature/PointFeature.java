/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intellias.statistic.model.geometry.PointGeometry;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("Feature")
public class PointFeature implements StatisticFeature<PointGeometry> {
  private String featureId;
  private PointGeometry geometry;
  private PointFeatureProperties properties;
}
