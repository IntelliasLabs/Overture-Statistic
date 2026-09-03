/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.divisions;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.OvertureFeatureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

/**
 * Converts Overture divisions/division records to PointFeature.
 */
@Slf4j
@Component
public class DivisionOvertureProcessor extends AbstractOvertureProcessor<PointFeature>
    implements OvertureFeatureProcessor<PointFeature> {

  public static final String PROCESSOR_NAME = "Division";
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
  public PointFeature process(@NotNull OvertureItem item) {
    try {
      if (!(item.getGeometry() instanceof Point point)) {
        log.warn("Skipping division_area id={} due to unsupported geometry", item.getId());
        return null;
      }

      PointFeatureProperties props = PointFeatureProperties.builder()
          .version(item.getVersion())
          .featureType(PROCESSOR_NAME)
          .timestamp(getTimestamp(item.getProperties()))
          .featureProperties(extractFeatureProperties(item.getProperties()))
          .globalSourceId(item.getId())
          .build();

      return PointFeature.builder()
          .featureId(item.getId())
          .geometry(StatisticGeometryExtractor.getPointGeometry(point))
          .properties(props)
          .build();
    } catch (Exception e) {
      log.error(
          "Failed to process Division record with id: {}, Reason: {}",
          item.getId(),
          e.getMessage(),
          e);
      return null;
    }
  }
}
