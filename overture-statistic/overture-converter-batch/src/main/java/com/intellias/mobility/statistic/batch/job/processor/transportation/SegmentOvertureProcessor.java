/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.transportation;

import static com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils.getRangeAttributes;
import static com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor.getLineGeometry;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.LineFeature;
import com.intellias.statistic.model.feature.LineFeatureProperties;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SegmentOvertureProcessor extends AbstractOvertureProcessor<LineFeature> {
  private static final String PROCESSOR_NAME = "Segment";

  private static final Set<String> IGNORED_PROPERTIES =
      Set.of("theme", "type", "version", "sources", "bbox");

  @Override
  protected Set<String> getKeysToOmit() {
    return IGNORED_PROPERTIES;
  }

  @Override
  public LineFeature process(OvertureItem item) {
    if (!(item.getGeometry() instanceof LineString line)) {
      log.warn("Invalid or missing geometry in item with ID: {}", item.getId());
      return null;
    }

    try {
      List<FeatureProperty> featureProperties = extractFeatureProperties(item.getProperties());

      List<RangeAttribute> rangeAttributes = getRangeAttributes(item);

      return LineFeature.builder()
          .featureId(item.getId())
          .geometry(getLineGeometry(line))
          .properties(LineFeatureProperties.builder()
              .version(item.getVersion())
              .featureType(PROCESSOR_NAME)
              .timestamp(getTimestamp(item.getProperties()))
              .featureProperties(featureProperties)
              .rangeAttributes(rangeAttributes)
              .globalSourceId(item.getId())
              .build())
          .build();
    } catch (Exception e) {
      log.error(
          "Failed to process Segment record with id: {}. Reason: {}",
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
