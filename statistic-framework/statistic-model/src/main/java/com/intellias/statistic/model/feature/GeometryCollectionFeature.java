/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intellias.statistic.model.geometry.StatisticGeometryCollection;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("Feature")
public class GeometryCollectionFeature implements StatisticFeature<StatisticGeometryCollection> {
  private String featureId;
  private StatisticGeometryCollection geometry;
  private GeometryCollectionFeatureProperties properties;
}
