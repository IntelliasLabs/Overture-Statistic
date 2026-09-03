/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intellias.statistic.model.geometry.MultiPolygonGeometry;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("Feature")
public class MultiPolygonFeature implements StatisticFeature<MultiPolygonGeometry> {
  private String featureId;
  private MultiPolygonGeometry geometry;
  private PolygonFeatureProperties properties;
}
