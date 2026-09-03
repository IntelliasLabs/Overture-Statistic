/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import com.intellias.mobility.statistic.framework.dashboards.controller.diff.DiffDashboardController;
import com.intellias.mobility.statistic.framework.dashboards.dto.DifferenceRequest;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.storage.StorageService;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import com.intellias.statistic.model.geometry.LonLat;
import com.intellias.statistic.model.geometry.PointGeometry;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest()
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DiffDashboardCreationTest extends TestcontainersConfiguration {

  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Autowired
  private StorageService storageService;

  @Autowired
  private ElasticManageClient elasticManageClient;

  @Autowired
  private DiffDashboardController diffDashboardController;

  @BeforeAll
  void setUp() {
    PointFeature feature1 = new PointFeature(
        "poi1",
        new PointGeometry(new LonLat(6.134128158472095, 49.593476516900296)),
        new PointFeatureProperties(
            "v1", "Point", new Date(), List.of(new FeatureProperty("SHOP", List.of("FOOD")))));
    PointFeature feature2 = new PointFeature(
        "poi2",
        new PointGeometry(new LonLat(6.134128158471065, 49.593476516900398)),
        new PointFeatureProperties(
            "v1", "Point", new Date(), List.of(new FeatureProperty("SHOP", List.of("BOOK")))));
    PointFeature feature3 = new PointFeature(
        "poi3",
        new PointGeometry(new LonLat(6.134128158443065, 49.593476518200408)),
        new PointFeatureProperties(
            "v2", "Point", new Date(), List.of(new FeatureProperty("PARK", List.of("FORESTRY")))));

    storageService.save(feature1, "point");
    storageService.save(feature2, "point");
    storageService.save(feature3, "point");
    refreshIndex("statistic-point-point", elasticsearchOperations);
  }

  @AfterAll
  void cleanUp() {
    deleteAllIndexes(elasticManageClient);
  }

  @DisplayName("Should create difference dashboard with visualization correctly")
  @Test
  void diffVisualizationCreationTest() {
    var differenceRequest = new DifferenceRequest("v1", "v2");

    ResponseEntity<String> response =
        diffDashboardController.createFeatureCountDiff(differenceRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertThat(response.getBody()).containsPattern("Visualization: .* was successfully created!");
  }

  @DisplayName(
      "Should create difference dashboard with visualization correctly when there are not needed indexes")
  @Test
  void diffVisualizationCreationTestWithoutNeededIndexes() {
    var differenceRequest = new DifferenceRequest("v1", "v2");

    ResponseEntity<String> response = diffDashboardController.createLengthDiff(differenceRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        "There are no indexes for Statistic lines features. Visualization was not created!",
        response.getBody());
  }
}
