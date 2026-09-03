/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.divisions;

import static com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils.getRangeAttributes;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.OvertureFeatureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.LineFeature;
import com.intellias.statistic.model.feature.LineFeatureProperties;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.LineString;
import org.springframework.stereotype.Component;

/**
 * Converts divisions/division_boundary to LineFeature.
 */
@Slf4j
@Component
public class DivisionBoundaryOvertureProcessor extends AbstractOvertureProcessor<LineFeature>
    implements OvertureFeatureProcessor<LineFeature> {

  public static final String PROCESSOR_NAME = "DivisionBoundary";
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
  public LineFeature process(@NotNull OvertureItem item) {
    try {
      if (!(item.getGeometry() instanceof LineString line)) {
        log.warn("Skipping division_area id={} due to unsupported geometry", item.getId());
        return null;
      }

      List<FeatureProperty> featureProps = extractFeatureProperties(item.getProperties());

      LineFeatureProperties props = LineFeatureProperties.builder()
          .version(item.getVersion())
          .featureType(PROCESSOR_NAME)
          .timestamp(getTimestamp(item.getProperties()))
          .featureProperties(featureProps)
          .rangeAttributes(getRangeAttributes(item))
          .globalSourceId(item.getId())
          .build();

      return LineFeature.builder()
          .featureId(item.getId())
          .geometry(StatisticGeometryExtractor.getLineGeometry(line))
          .properties(props)
          .build();

    } catch (Exception e) {
      log.error(
          "Failed to process DivisionBoundary record with id: {}, Reason: {}",
          item.getId(),
          e.getMessage(),
          e);
      return null;
    }
  }
}
