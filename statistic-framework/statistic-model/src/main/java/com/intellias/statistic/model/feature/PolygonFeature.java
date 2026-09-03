/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intellias.statistic.model.geometry.PolygonGeometry;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("Feature")
public class PolygonFeature implements StatisticFeature<PolygonGeometry> {
  private String featureId;
  private PolygonGeometry geometry;
  private PolygonFeatureProperties properties;
}
