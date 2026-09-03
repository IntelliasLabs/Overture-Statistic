/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.base;

import static com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils.getRangeAttributes;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils;
import com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor;
import com.intellias.statistic.model.feature.*;
import com.intellias.statistic.model.geometry.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.*;

@Slf4j
public abstract class BaseThemeOvertureProcessor
    extends AbstractOvertureProcessor<StatisticFeature<?>> {

  /** Keys to omit from feature properties for this theme. */
  private static final Set<String> IGNORED_PROPERTIES =
      Set.of("theme", "type", "version", "sources", "bbox");

  @Override
  protected Set<String> getKeysToOmit() {
    return IGNORED_PROPERTIES;
  }

  /**
   * Whether this processor should run geometry filtering for tile rectangles.
   * Default: off. Override in LandOvertureProcessor).
   */
  protected boolean validateGeometry(@NotNull OvertureItem item) {
    return false;
  }

  /**
   * Processes a single Overture item.
   *
   * @param item source record with geometry and attributes
   * @return a feature ready for indexing, or {@code null} if geometry is filtered or unsupported
   */
  @Override
  public StatisticFeature<?> process(@NotNull OvertureItem item) {
    try {
      if (validateGeometry(item)) {
        if (isLandMaskByProperty(item)) {
          return null;
        }
      }

      Geometry geometry = item.getGeometry();

      if (geometry instanceof MultiPolygon mp) {
        PolygonFeatureProperties props = polygonProps(item, getProcessorName() + "_multiPolygon");
        props.setArea(getAreaInMeters(mp));
        MultiPolygonGeometry geo = StatisticGeometryExtractor.getMultiPolygonGeometry(mp);
        return new MultiPolygonFeature(item.getId(), geo, props);
      }

      if (geometry instanceof Polygon p) {
        PolygonFeatureProperties props = polygonProps(item, getProcessorName() + "_multiPolygon");
        props.setArea(getAreaInMeters(p));
        MultiPolygonGeometry geo =
            new MultiPolygonGeometry(List.of(StatisticGeometryExtractor.getPolygonGeometry(p)));
        return new MultiPolygonFeature(item.getId(), geo, props);
      }

      if (geometry instanceof LineString line) {
        LineGeometry geo = StatisticGeometryExtractor.getLineGeometry(line);
        LineFeatureProperties props = LineFeatureProperties.builder()
            .version(item.getVersion())
            .featureType(getProcessorName() + "_lineString")
            .timestamp(getTimestamp(item.getProperties()))
            .featureProperties(extractFeatureProperties(item.getProperties()))
            .rangeAttributes(getRangeAttributes(item))
            .globalSourceId(item.getId())
            .build();
        return new LineFeature(item.getId(), geo, props);
      }

      if (geometry instanceof Point point) {
        PointGeometry geo = new PointGeometry(point.getX(), point.getY());
        PointFeatureProperties props = PointFeatureProperties.builder()
            .version(item.getVersion())
            .featureType(getProcessorName() + "_point")
            .timestamp(getTimestamp(item.getProperties()))
            .featureProperties(extractFeatureProperties(item.getProperties()))
            .globalSourceId(item.getId())
            .build();
        return new PointFeature(item.getId(), geo, props);
      }

      log.warn("Unsupported geometry type {} for id={}", geometry.getGeometryType(), item.getId());
      return null;

    } catch (Exception e) {
      log.error(
          "Failed to process {} item {}: {}", getProcessorName(), item.getId(), e.getMessage(), e);
      return null;
    }
  }

  /**
   * Builds polygon feature properties for the given feature type.
   */
  private PolygonFeatureProperties polygonProps(OvertureItem item, String featureType) {
    return PolygonFeatureProperties.builder()
        .version(item.getVersion())
        .featureType(featureType)
        .timestamp(getTimestamp(item.getProperties()))
        .featureProperties(extractFeatureProperties(item.getProperties()))
        .globalSourceId(item.getId())
        .build();
  }

  /**
   * Computes geodesic area in square meters for Polygon and MultiPolygon.
   */
  protected double getAreaInMeters(Geometry geometry) {
    if (geometry instanceof Polygon poly) {
      return FeatureComputationUtils.getAreaInSquareMeters(poly);
    }
    if (geometry instanceof MultiPolygon mp) {
      double sum = 0.0;
      for (int i = 0; i < mp.getNumGeometries(); i++) {
        Geometry g = mp.getGeometryN(i);
        if (g instanceof Polygon p) sum += FeatureComputationUtils.getAreaInSquareMeters(p);
      }
      return sum;
    }
    return 0.0;
  }

  /**
   * Fast property-based check to skip background land-mask features.
   * Treats an item as a land mask when {@code theme=base} and {@code type=land},
   * and {@code class=land} or {@code subtype=land} (case-insensitive).
   *
   * @param item the Overture item (properties may be {@code null} or empty)
   * @return {@code true} if the item should be skipped as a land mask; {@code false} otherwise
   */
  protected boolean isLandMaskByProperty(@NotNull OvertureItem item) {
    Map<String, Object> p = item.getProperties();
    if (p == null || p.isEmpty()) return false;

    String theme = String.valueOf(p.getOrDefault("theme", ""));
    String type = String.valueOf(p.getOrDefault("type", ""));
    if (!"base".equalsIgnoreCase(theme) || !"land".equalsIgnoreCase(type)) return false;

    String clazz = String.valueOf(p.getOrDefault("class", ""));
    String subtype = String.valueOf(p.getOrDefault("subtype", ""));
    return "land".equalsIgnoreCase(clazz) || "land".equalsIgnoreCase(subtype);
  }
}
