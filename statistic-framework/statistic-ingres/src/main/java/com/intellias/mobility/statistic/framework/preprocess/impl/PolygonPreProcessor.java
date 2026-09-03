/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess.impl;

import com.intellias.mobility.statistic.framework.preprocess.PreProcessor;
import com.intellias.statistic.model.feature.PolygonFeature;
import com.intellias.statistic.model.feature.StatisticFeature;
import com.intellias.statistic.model.util.GeoSystemProjector;
import com.intellias.statistic.model.util.JtsGeometryConverter;
import org.locationtech.jts.geom.Polygon;

/**
 * Calculates polygon area if it is missing.
 */
public class PolygonPreProcessor implements PreProcessor {
  @Override
  public boolean isApplicable(StatisticFeature<?> feature, String indexName) {
    return feature instanceof PolygonFeature;
  }

  @Override
  public StatisticFeature<?> process(StatisticFeature<?> feature) {
    return switch (feature) {
      case PolygonFeature polygonFeature -> enhance(polygonFeature);
      default -> throw new IllegalStateException("Unexpected feature: " + feature);
    };
  }

  /**
   * Computes and sets the polygon area if it hasn't been calculated yet.
   */
  static PolygonFeature enhance(PolygonFeature polygonFeature) {
    if (polygonFeature.getProperties().getArea() <= 0.0) {
      var jtsPolygon = JtsGeometryConverter.toJtsPolygon(polygonFeature.getGeometry());
      var jtsPolygonInMetricSystem = (Polygon) GeoSystemProjector.projectWgsToMetric(jtsPolygon);

      polygonFeature.getProperties().setArea(jtsPolygonInMetricSystem.getArea());
    }
    return polygonFeature;
  }
}
