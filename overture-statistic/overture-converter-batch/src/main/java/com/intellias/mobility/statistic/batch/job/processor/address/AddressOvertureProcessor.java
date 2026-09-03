/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.address;

import static com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor.getPointGeometry;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AddressOvertureProcessor extends AbstractOvertureProcessor<PointFeature> {

  private static final String PROCESSOR_NAME = "address";

  private static final Set<String> IGNORED_PROPERTIES =
      Set.of("theme", "type", "version", "sources", "bbox");

  @Override
  protected Set<String> getKeysToOmit() {
    return IGNORED_PROPERTIES;
  }

  @Override
  public PointFeature process(OvertureItem item) {
    try {
      if (!(item.getGeometry() instanceof Point point)) {
        log.warn("Invalid geometry in item with ID: {}", item.getId());
        return null;
      }

      return PointFeature.builder()
          .featureId(item.getId())
          .geometry(getPointGeometry(point))
          .properties(PointFeatureProperties.builder()
              .version(item.getVersion())
              .featureType(PROCESSOR_NAME)
              .timestamp(getTimestamp(item.getProperties()))
              .featureProperties(extractFeatureProperties(item.getProperties()))
              .build())
          .build();
    } catch (Exception e) {
      log.error(
          "Failed to process Place record with id: {}, Reason: {}",
          item.getId(),
          e.getMessage(),
          e);
      return null;
    }
  }

  @Override
  public String getProcessorName() {
    return PROCESSOR_NAME;
  }
}
