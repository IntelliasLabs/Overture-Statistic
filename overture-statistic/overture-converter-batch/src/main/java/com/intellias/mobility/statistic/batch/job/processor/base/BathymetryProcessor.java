/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.base;

import static com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils.getAreaInSquareMeters;
import static com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor.getMultiPolygonGeometry;
import static com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor.getPolygonGeometry;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BathymetryProcessor extends AbstractOvertureProcessor<StatisticFeature<?>> {
  private static final String PROCESSOR_NAME = "bathymetry";

  private static final Set<String> IGNORED_PROPERTIES =
      Set.of("theme", "type", "version", "sources", "bbox");

  @Override
  protected Set<String> getKeysToOmit() {
    return IGNORED_PROPERTIES;
  }

  @Override
  public String getProcessorName() {
    return PROCESSOR_NAME;
  }

  @Override
  public StatisticFeature<?> process(OvertureItem item) {
    try {
      Geometry geometry = item.getGeometry();
      if (!(geometry instanceof Polygon) && !(geometry instanceof MultiPolygon)) {
        log.warn("Invalid geometry in item with ID: {}", item.getId());
        return null;
      }

      PolygonFeatureProperties featureProperties = PolygonFeatureProperties.builder()
          .version(item.getVersion())
          .featureType(PROCESSOR_NAME)
          .timestamp(getTimestamp(item.getProperties()))
          .featureProperties(extractFeatureProperties(item.getProperties()))
          .area(getArea(geometry))
          .globalSourceId(item.getId())
          .build();

      if (geometry instanceof MultiPolygon multiPolygon) {
        return new MultiPolygonFeature(
            item.getId(), getMultiPolygonGeometry(multiPolygon), featureProperties);
      }

      return new PolygonFeature(
          item.getId(), getPolygonGeometry((Polygon) geometry), featureProperties);
    } catch (Exception e) {
      log.error(
          "Failed to process Bathymetry record with id: {}. Reason: {}",
          item.getId(),
          e.getMessage(),
          e);
      return null;
    }
  }

  private double getArea(Geometry geometry) {
    if (geometry instanceof MultiPolygon multiPolygon) {
      return IntStream.range(0, multiPolygon.getNumGeometries())
          .mapToObj(multiPolygon::getGeometryN)
          .map(Polygon.class::cast)
          .mapToDouble(FeatureComputationUtils::getAreaInSquareMeters)
          .sum();
    }

    return getAreaInSquareMeters((Polygon) geometry);
  }
}
