/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess;

import static com.intellias.mobility.statistic.framework.testutils.data.TestFeatures.createPOI;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.mobility.statistic.framework.preprocess.impl.PointPreProcessor;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PreProcessServiceTest {
  String indexName = "TestPoi";

  @DisplayName("Should modify feature")
  @Test
  void process() {
    var preProfessor = new PointPreProcessor();
    var service = new PreProcessService(List.of(preProfessor, new TestPointPreProcessor()));

    var processedFeature = service.preprocess(createPOI(), indexName);

    assertEquals(
        List.of("value2"),
        processedFeature.feature().getProperties().getFeatureProperties().stream()
            .filter(featureProperty -> featureProperty.getKey().equals("key1"))
            .findFirst()
            .get()
            .getValues());
  }

  @DisplayName("Should not modify feature")
  @Test
  void processNotModify() {
    var preProcessor = new PointPreProcessor();
    var service = new PreProcessService(List.of(preProcessor));

    var processedFeature = service.preprocess(createPOI(), indexName);

    assertEquals(
        List.of("value1"),
        processedFeature.feature().getProperties().getFeatureProperties().stream()
            .filter(featureProperty -> featureProperty.getKey().equals("key1"))
            .findFirst()
            .get()
            .getValues());
  }

  private static class TestPointPreProcessor implements PreProcessor {
    @Override
    public boolean isApplicable(StatisticFeature feature, String indexName) {
      return feature instanceof PointFeature
          && feature.getProperties().getFeatureType().equals("POI");
    }

    @Override
    public StatisticFeature process(StatisticFeature feature) {
      PointFeature pointFeature = (PointFeature) feature;
      pointFeature.getProperties().getFeatureProperties().removeIf(prop -> prop.getKey()
          .equals("key1"));
      pointFeature
          .getProperties()
          .getFeatureProperties()
          .add(new FeatureProperty("key1", List.of("value2")));
      return pointFeature;
    }
  }
}
