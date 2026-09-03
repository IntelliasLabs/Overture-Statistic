/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.base;

import static com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils.getAreaInSquareMeters;
import static com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor.getPolygonGeometry;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.statistic.model.feature.PolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LandCoverProcessor extends AbstractOvertureProcessor<PolygonFeature> {
  private static final String PROCESSOR_NAME = "landCover";

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
  public PolygonFeature process(OvertureItem item) {
    try {
      if (!(item.getGeometry() instanceof Polygon polygon)) {
        log.warn("Invalid geometry in item with ID: {}", item.getId());
        return null;
      }

      return new PolygonFeature(
          item.getId(),
          getPolygonGeometry(polygon),
          PolygonFeatureProperties.builder()
              .version(item.getVersion())
              .featureType(PROCESSOR_NAME)
              .timestamp(getTimestamp(item.getProperties()))
              .featureProperties(extractFeatureProperties(item.getProperties()))
              .globalSourceId(item.getId())
              .area(getAreaInSquareMeters(polygon))
              .build());
    } catch (Exception e) {
      log.error(
          "Failed to process Land cover record with id: {}. Reason: {}",
          item.getId(),
          e.getMessage(),
          e);
      return null;
    }
  }
}
