/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess.impl;

import com.intellias.mobility.statistic.framework.preprocess.PreProcessor;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import com.intellias.statistic.model.feature.StatisticFeature;
import com.intellias.statistic.model.util.GeoSystemProjector;
import com.intellias.statistic.model.util.JtsGeometryConverter;
import lombok.SneakyThrows;

/**
 * Calculates area for {@link MultiPolygonFeature} if not present.
 */
public class MultiPolygonPreProcessor implements PreProcessor {
  @Override
  public boolean isApplicable(StatisticFeature<?> feature, String indexName) {
    return feature instanceof MultiPolygonFeature;
  }

  @Override
  public StatisticFeature<?> process(StatisticFeature<?> feature) {
    return switch (feature) {
      case MultiPolygonFeature multiPolygonFeature -> enhance(multiPolygonFeature);
      default -> throw new IllegalStateException("Unexpected feature: " + feature);
    };
  }

  /**
   * Computes and sets the area of the multipolygon if missing.
   */
  @SneakyThrows
  static MultiPolygonFeature enhance(MultiPolygonFeature multiPolygonFeature) {
    if (multiPolygonFeature.getProperties().getArea() <= 0.0) {
      multiPolygonFeature
          .getProperties()
          .setArea(multiPolygonFeature.getGeometry().getPolygons().stream()
              .map(JtsGeometryConverter::toJtsPolygon)
              .map(jtsPolygon ->
                  GeoSystemProjector.projectWgsToMetric(jtsPolygon).getArea())
              .reduce(0.0, Double::sum));
    }
    return multiPolygonFeature;
  }
}
