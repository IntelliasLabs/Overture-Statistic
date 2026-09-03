/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.divisions;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.OvertureFeatureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils;
import com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import com.intellias.statistic.model.geometry.MultiPolygonGeometry;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

/**
 * Converts Overture divisions/division_area records to MultiPolygonFeature.
 */
@Slf4j
@Component
public class DivisionAreaOvertureProcessor extends AbstractOvertureProcessor<MultiPolygonFeature>
    implements OvertureFeatureProcessor<MultiPolygonFeature> {

  private static final Set<String> IGNORED_PROPERTIES =
      Set.of("theme", "type", "version", "sources", "bbox");
  public static final String PROCESSOR_NAME = "DivisionArea";

  @Override
  protected Set<String> getKeysToOmit() {
    return IGNORED_PROPERTIES;
  }

  @Override
  public String getProcessorName() {
    return PROCESSOR_NAME;
  }

  @Override
  public MultiPolygonFeature process(@NotNull OvertureItem item) {
    try {
      Geometry geometry = item.getGeometry();
      if (!(geometry instanceof Polygon) && !(geometry instanceof MultiPolygon)) {
        log.warn("Skipping division_area id={} due to unsupported geometry", item.getId());
        return null;
      }

      MultiPolygonGeometry mpGeometry = (geometry instanceof MultiPolygon mp)
          ? StatisticGeometryExtractor.getMultiPolygonGeometry(mp)
          : new MultiPolygonGeometry(
              List.of(StatisticGeometryExtractor.getPolygonGeometry((Polygon) geometry)));

      PolygonFeatureProperties props = PolygonFeatureProperties.builder()
          .version(item.getVersion())
          .featureType(PROCESSOR_NAME)
          .timestamp(getTimestamp(item.getProperties()))
          .featureProperties(extractFeatureProperties(item.getProperties()))
          .area(getAreaInMeters(geometry))
          .globalSourceId(item.getId())
          .build();

      return MultiPolygonFeature.builder()
          .featureId(item.getId())
          .geometry(mpGeometry)
          .properties(props)
          .build();
    } catch (Exception e) {
      log.error(
          "Failed to process DivisionArea record with id: {}, Reason: {}",
          item.getId(),
          e.getMessage(),
          e);
      return null;
    }
  }

  private double getAreaInMeters(Geometry geometry) {
    if (geometry instanceof Polygon p) {
      return FeatureComputationUtils.getAreaInSquareMeters(p);
    }
    if (geometry instanceof MultiPolygon mp) {
      double sum = 0.0;
      for (int i = 0; i < mp.getNumGeometries(); i++) {
        Geometry g = mp.getGeometryN(i);
        if (g instanceof Polygon pi) {
          sum += FeatureComputationUtils.getAreaInSquareMeters(pi);
        }
      }
      return sum;
    }
    return 0.0;
  }
}
