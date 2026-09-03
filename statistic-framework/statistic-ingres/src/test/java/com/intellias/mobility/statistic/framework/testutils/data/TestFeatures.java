/**
 Copyright ©2024-2025 Intellias
 */
package com.intellias.mobility.statistic.framework.testutils.data;

import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import com.intellias.statistic.model.geometry.LonLat;
import com.intellias.statistic.model.geometry.PointGeometry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TestFeatures {
  public static PointFeature createPOI() {
    var coordinates = new LonLat(6.134128158472095, 49.593476516900296);

    PointGeometry point = new PointGeometry(coordinates);
    var featureProperties = new ArrayList<FeatureProperty>();
    featureProperties.add(new FeatureProperty("key1", List.of("value1")));

    return new PointFeature(
        "id1", point, new PointFeatureProperties("v1", "POI", new Date(), featureProperties));
  }
}
