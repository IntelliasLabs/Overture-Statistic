/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import com.intellias.mobility.statistic.framework.dashboards.controller.statistic.StatisticDashboardController;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.storage.StorageService;
import com.intellias.statistic.model.feature.*;
import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.LonLat;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.geometry.PolygonGeometry;
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
public class StatisticDashboardCreationTest extends TestcontainersConfiguration {

  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Autowired
  private StorageService storageService;

  @Autowired
  private ElasticManageClient elasticManageClient;

  @Autowired
  private StatisticDashboardController statisticDashboardController;

  @BeforeAll
  void setUp() {
    PointFeature pointFeature = new PointFeature(
        "poi1",
        new PointGeometry(new LonLat(6.134128158472095, 49.593476516900296)),
        new PointFeatureProperties(
            "v1", "Point", new Date(), List.of(new FeatureProperty("SHOP", List.of("FOOD")))));

    LineFeature lineFeature = new LineFeature(
        "line1",
        new LineGeometry(List.of(
            new PointGeometry(6.134128158472095, 49.593476516900296),
            new PointGeometry(6.134128158472032, 49.593476516900227))),
        new LineFeatureProperties(
            "v1",
            "Line",
            new Date(),
            List.of(new FeatureProperty("ROAD_NAME", List.of("Chornovola 1715")))));

    PolygonFeature polygonFeature = new PolygonFeature(
        "polygon1",
        new PolygonGeometry(
            new LineGeometry(List.of(
                new PointGeometry(6.134128158472095, 49.593476516900296),
                new PointGeometry(6.134128158472032, 49.593476516900227),
                new PointGeometry(6.134128158472052, 49.593476516900267),
                new PointGeometry(6.134128158472095, 49.593476516900296))),
            List.of()),
        new PolygonFeatureProperties(
            "v1", "Line", new Date(), List.of(new FeatureProperty("AREA_NAME", List.of("City")))));

    storageService.save(pointFeature, "point");
    storageService.save(lineFeature, "line");
    storageService.save(polygonFeature, "polygon");
    refreshIndex("statistic-point-point", elasticsearchOperations);
    refreshIndex("statistic-linestring-line", elasticsearchOperations);
    refreshIndex("statistic-polygon-polygon", elasticsearchOperations);
  }

  @AfterAll
  void cleanUp() {
    deleteAllIndexes(elasticManageClient);
  }

  @DisplayName("Should create common dashboard with visualization correctly")
  @Test
  void createCommonDashboardTest() {
    ResponseEntity<String> response = statisticDashboardController.createCommonDashboard();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertThat(response.getBody())
        .containsPattern(
            "Common dashboard: .* was successfully created! With controller and visualisations: \\[.*\\]");
  }

  @DisplayName("Should create lienes dashboard with visualization correctly")
  @Test
  void createLinesDashboardTest() {
    ResponseEntity<String> response = statisticDashboardController.createLinesDashboard();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertThat(response.getBody())
        .containsPattern(
            "Lines dashboard: .* was successfully created! With controller and visualisations: \\[.*\\]");
  }

  @DisplayName("Should create polygons dashboard with visualization correctly")
  @Test
  void createPolygonsDashboardTest() {
    ResponseEntity<String> response = statisticDashboardController.createPolygonsDashboard();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertThat(response.getBody())
        .containsPattern(
            "Polygons dashboard: .* was successfully created! With controller and visualisations: \\[.*\\]");
  }

  @DisplayName("Should create points dashboard with visualization correctly")
  @Test
  void createPointsDashboardTest() {
    ResponseEntity<String> response = statisticDashboardController.createPointsDashboard();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertThat(response.getBody())
        .containsPattern(
            "Points dashboard: .* was successfully created! With controller and visualisations: \\[.*\\]");
  }
}
