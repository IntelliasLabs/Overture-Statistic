/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess.impl;

import com.intellias.statistic.model.feature.PolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.geometry.PolygonGeometry;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for PolygonPreProcessor.
 */
public class PolygonPreProcessorTest {

  @Test
  void polygonPreProcessorTest() {
    var polygonGeometry = new PolygonGeometry(
        new LineGeometry(List.of(
            new PointGeometry(24.030537314987214, 49.84222465108192),
            new PointGeometry(24.03101337857086, 49.84124943031978),
            new PointGeometry(24.032721606726653, 49.841642229937264),
            new PointGeometry(24.032224540337978, 49.842581324132624),
            new PointGeometry(24.030537314987214, 49.84222465108192))),
        List.of());

    var polygonFeature = new PolygonFeature(
        "1", polygonGeometry, new PolygonFeatureProperties("v1", "polygon", new Date()));

    var processedPolygonFeature =
        (PolygonFeature) new PolygonPreProcessor().process(polygonFeature);

    Assertions.assertEquals(14464.205, processedPolygonFeature.getProperties().getArea(), 0.001);
  }
}
