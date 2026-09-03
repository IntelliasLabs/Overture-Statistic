/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess.impl;

import com.intellias.mobility.statistic.framework.preprocess.PreProcessor;
import com.intellias.statistic.model.feature.MultiLineFeature;
import com.intellias.statistic.model.feature.StatisticFeature;
import com.intellias.statistic.model.util.GeoTools;
import com.intellias.statistic.model.util.JtsGeometryConverter;

/**
 * Ensures length of {@link MultiLineFeature} is calculated.
 */
public class MultiLinePreProcessor implements PreProcessor {
  @Override
  public boolean isApplicable(StatisticFeature<?> feature, String indexName) {
    return feature instanceof MultiLineFeature;
  }

  @Override
  public StatisticFeature<?> process(StatisticFeature<?> feature) {
    return switch (feature) {
      case MultiLineFeature multiLine -> enhance(multiLine);
      default -> throw new IllegalStateException("Unexpected feature: " + feature);
    };
  }

  /**
   * Calculates and sets length in meters when not provided.
   */
  static MultiLineFeature enhance(MultiLineFeature multiLine) {
    if (multiLine.getProperties().getLengthMeters() <= 0.0) {
      multiLine
          .getProperties()
          .setLengthMeters(multiLine.getGeometry().retrieveLines().stream()
              .map(JtsGeometryConverter::toJtsLineString)
              .map(lineString -> GeoTools.calculateLength(lineString.getCoordinates()))
              .reduce(0.0, Double::sum));
    }
    return multiLine;
  }
}
