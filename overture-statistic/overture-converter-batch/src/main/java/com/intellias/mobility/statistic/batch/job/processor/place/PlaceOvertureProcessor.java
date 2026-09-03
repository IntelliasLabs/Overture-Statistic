/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.place;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlaceOvertureProcessor extends AbstractOvertureProcessor<PointFeature> {
  private static final String PROCESSOR_NAME = "Place";

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
  public PointFeature process(OvertureItem item) {
    if (!(item.getGeometry() instanceof Point point)) {
      log.error("Invalid geometry for item with id: {}", item.getId());
      return null;
    }
    try {
      var pointGeometry = StatisticGeometryExtractor.getPointGeometry(point);

      Map<String, Object> itemProperties = getAsMap(item.getProperties());

      String formattedTimestamp = getTimestamp(itemProperties);

      List<FeatureProperty> featureProperties = extractFeatureProperties(itemProperties);

      PointFeatureProperties pointFeatrueProperties = PointFeatureProperties.builder()
          .featureType(PROCESSOR_NAME)
          .version(item.getVersion())
          .timestamp(formattedTimestamp)
          .featureProperties(featureProperties)
          .globalSourceId(item.getId())
          .build();

      return PointFeature.builder()
          .featureId(item.getId())
          .geometry(pointGeometry)
          .properties(pointFeatrueProperties)
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
}
