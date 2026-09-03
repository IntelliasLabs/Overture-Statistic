/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.building;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.AbstractOvertureProcessor;
import com.intellias.mobility.statistic.batch.job.processor.util.FeatureComputationUtils;
import com.intellias.mobility.statistic.batch.job.processor.util.StatisticGeometryExtractor;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import com.intellias.statistic.model.geometry.MultiPolygonGeometry;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Component;

/**
 * Processes Overture data items to create features for buildings.
 * <p>
 * This class transforms an {@link OvertureItem} with a polygon geometry
 * into a standard {@link MultiPolygonFeature}. It handles both 'building'
 * and 'building_part' types from the source data.
 * <p>
 * A key part of the logic is how the final feature ID is determined for data connectivity:
 * If an item contains a {@code building_id} property (indicating it is a building part),
 * that ID is used as the {@code featureId} for the final feature to link it to the main building.
 * Otherwise, the item's own ID is used.
 * <p>
 * It also processes the item's properties via the shared recursive extractor,
 * while a predefined set of common properties (e.g., theme, type) is skipped.
 */
@Component("BuildingFeatureProcessor")
@Slf4j
public class BuildingFeatureProcessor extends AbstractOvertureProcessor<MultiPolygonFeature> {

  public static final String PROCESSOR_NAME = "Building";
  public static final String BUILDING_ID = "building_id";

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
  public MultiPolygonFeature process(final @NotNull OvertureItem item) {
    try {
      Geometry inputGeometry = item.getGeometry();
      if (!(inputGeometry instanceof Polygon) && !(inputGeometry instanceof MultiPolygon)) {
        log.warn(
            "Skipping record with id: {} because its geometry is not a Polygon or MultiPolygon, but a {}.",
            item.getId(),
            inputGeometry.getGeometryType());
        return null;
      }

      MultiPolygonGeometry multiPolygonGeometry;
      if (inputGeometry instanceof MultiPolygon mp) {
        multiPolygonGeometry = StatisticGeometryExtractor.getMultiPolygonGeometry(mp);
      } else {
        Polygon poly = (Polygon) inputGeometry;
        multiPolygonGeometry =
            new MultiPolygonGeometry(List.of(StatisticGeometryExtractor.getPolygonGeometry(poly)));
      }

      Map<String, Object> properties = item.getProperties();
      String formattedTimestamp = getTimestamp(properties);
      List<FeatureProperty> featurePropertiesList = extractFeatureProperties(properties);

      String finalFeatureId = Optional.ofNullable(properties.get(BUILDING_ID))
          .map(Object::toString)
          .filter(id -> !id.isBlank())
          .orElse(item.getId());

      double areaInMeters = getAreaInMeters(inputGeometry);

      PolygonFeatureProperties featureProperties = PolygonFeatureProperties.builder()
          .version(item.getVersion())
          .featureType(PROCESSOR_NAME)
          .timestamp(formattedTimestamp)
          .featureProperties(featurePropertiesList)
          .area(areaInMeters)
          .globalSourceId(finalFeatureId)
          .build();

      return MultiPolygonFeature.builder()
          .featureId(finalFeatureId)
          .geometry(multiPolygonGeometry)
          .properties(featureProperties)
          .build();

    } catch (Exception e) {
      log.error(
          "Failed to process Building record with id: {}. Reason: {}",
          item.getId(),
          e.getMessage(),
          e);
      return null;
    }
  }

  private double getAreaInMeters(Geometry geometry) {
    if (geometry instanceof Polygon poly) {
      return FeatureComputationUtils.getAreaInSquareMeters(poly);
    }

    if (geometry instanceof MultiPolygon mp) {
      double sum = 0.0;
      for (int i = 0; i < mp.getNumGeometries(); i++) {
        Geometry g = mp.getGeometryN(i);
        if (g instanceof Polygon p) {
          sum += FeatureComputationUtils.getAreaInSquareMeters(p);
        }
      }
      return sum;
    }
    return 0.0;
  }
}
