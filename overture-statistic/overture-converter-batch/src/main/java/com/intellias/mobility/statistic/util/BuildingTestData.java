/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.util;

import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.MultiPolygonGeometry;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.geometry.PolygonGeometry;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class to generate test data that is guaranteed to be valid for storage and processing.
 *
 */
public final class BuildingTestData {

  private BuildingTestData() {
    // Utility class
  }

  public static MultiPolygonFeature createFeatureAt(String featureId, double lon, double lat) {
    List<List<Double>> coordinates = List.of(
        List.of(lon - 0.0001, lat - 0.0001),
        List.of(lon + 0.0001, lat - 0.0001),
        List.of(lon + 0.0001, lat + 0.0001),
        List.of(lon - 0.0001, lat + 0.0001),
        List.of(lon - 0.0001, lat - 0.0001));
    return createMultiPolygonFeature(featureId, coordinates);
  }

  private static MultiPolygonFeature createMultiPolygonFeature(
      String featureId, List<List<Double>> coordinates) {
    LineGeometry outerRing = new LineGeometry(coordinates.stream()
        .map(coords -> new PointGeometry(coords.getFirst(), coords.get(1)))
        .collect(Collectors.toList()));

    PolygonGeometry polygon = new PolygonGeometry(outerRing, Collections.emptyList());
    MultiPolygonGeometry multiPolygon = new MultiPolygonGeometry(List.of(polygon));

    return MultiPolygonFeature.builder()
        .featureId(featureId)
        .geometry(multiPolygon)
        .properties(PolygonFeatureProperties.builder()
            .version("v1")
            .featureType("BUILDING")
            .timestamp("2024-11-13T11:53:31.000+0000")
            .featureProperties(List.of(new FeatureProperty("IS_UNDERGROUND", List.of("false"))))
            .build())
        .build();
  }
}
