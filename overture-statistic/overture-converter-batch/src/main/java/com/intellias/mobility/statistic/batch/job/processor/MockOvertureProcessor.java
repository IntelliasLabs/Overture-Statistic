/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.util.BuildingTestData;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

/**
 * A mock processor
 * It ignores the input record and returns a predefined,
 * valid StatisticFeature from the test data in turn.
 */
@Component
public class MockOvertureProcessor implements OvertureFeatureProcessor<MultiPolygonFeature> {

  private static final String PROCESSOR_NAME = "mockOverture";

  @Override
  public MultiPolygonFeature process(OvertureItem item) {
    if (item == null || item.getGeometry() == null || item.getGeometry().isEmpty()) {
      return null;
    }

    Geometry inputGeometry = item.getGeometry();

    Point centroid = inputGeometry.getCentroid();
    double lon = centroid.getX();
    double lat = centroid.getY();

    MultiPolygonFeature feature = BuildingTestData.createFeatureAt(item.getId(), lon, lat);

    return feature;
  }

  @Override
  public String getProcessorName() {
    return PROCESSOR_NAME;
  }
}
