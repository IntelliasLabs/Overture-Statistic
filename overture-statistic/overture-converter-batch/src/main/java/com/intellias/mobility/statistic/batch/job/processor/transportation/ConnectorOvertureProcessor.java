/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.transportation;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import com.intellias.statistic.model.geometry.PointGeometry;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConnectorOvertureProcessor extends AbstractOvertureProcessor<PointFeature> {
  private static final String PROCESSOR_NAME = "Connector";

  private static final Set<String> IGNORED_PROPERTIES =
      Set.of("theme", "type", "version", "sources", "bbox");

  @Override
  protected Set<String> getKeysToOmit() {
    return IGNORED_PROPERTIES;
  }

  @Override
  public PointFeature process(OvertureItem item) {
    if (!(item.getGeometry() instanceof Point point)) {
      log.warn("Invalid or missing geometry in item with ID: {}", item.getId());
      return null;
    }

    try {
      return PointFeature.builder()
          .featureId(item.getId())
          .geometry(new PointGeometry(point.getX(), point.getY()))
          .properties(PointFeatureProperties.builder()
              .version(item.getVersion())
              .featureType(PROCESSOR_NAME)
              .timestamp(getTimestamp(item.getProperties()))
              .featureProperties(extractFeatureProperties(item.getProperties()))
              .globalSourceId(item.getId())
              .build())
          .build();
    } catch (Exception e) {
      log.error(
          "Failed to process Connector record with id: {}. Reason: {}",
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
