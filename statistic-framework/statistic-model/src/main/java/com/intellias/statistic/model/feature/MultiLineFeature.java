/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intellias.statistic.model.geometry.MultiLineGeometry;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("Feature")
public class MultiLineFeature implements StatisticFeature<MultiLineGeometry> {
  private String featureId;
  private MultiLineGeometry geometry;
  private LineFeatureProperties properties;
}
