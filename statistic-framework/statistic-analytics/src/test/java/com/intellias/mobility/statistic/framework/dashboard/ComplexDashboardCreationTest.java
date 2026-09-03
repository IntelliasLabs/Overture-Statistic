/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.storage.StorageService;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import com.intellias.statistic.model.geometry.LonLat;
import com.intellias.statistic.model.geometry.PointGeometry;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest()
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComplexDashboardCreationTest extends TestcontainersConfiguration {

  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Autowired
  private StorageService storageService;

  @Autowired
  private DashboardController dashboardController;

  @Autowired
  private ElasticManageClient elasticManageClient;

  @AfterAll
  void cleanUp() {
    deleteAllIndexes(elasticManageClient);
  }

  @DisplayName("Should create simple dashboard fro POI correctly")
  @SneakyThrows
  @Test
  void savePointTest() {
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
            "v1", "Point", new Date(), List.of(new FeatureProperty("PARK", List.of("FORESTRY")))));

    storageService.save(feature1, "point");
    storageService.save(feature2, "point");
    storageService.save(feature3, "point");
    refreshIndex("statistic-point-point", elasticsearchOperations);

    String jsonContent =
        "{\"statistic-point-point\":{\"geometry\":[\"map\"],\"properties.additionalProperties.SHOP.keyword\":[\"countHistogram\",\"count\"]},\"name\":\"Dashboard"
            + " for POI\"}";
    var json = new MockMultipartFile(
        "file",
        "data.json",
        MediaType.APPLICATION_JSON_VALUE,
        jsonContent.getBytes(StandardCharsets.UTF_8));

    var dashboardAnswers = dashboardController.createDashboard(json);
    ObjectMapper mapper = new ObjectMapper();
    ArrayNode jsonNode = (ArrayNode) mapper.readTree(dashboardAnswers).get("answers");

    Assertions.assertTrue(
        jsonNode.get(0).toString().contains("\"contentTypeId\":\"index-pattern\""));
    Assertions.assertTrue(jsonNode.get(1).toString().contains("\"contentTypeId\":\"dashboard\""));
    Assertions.assertTrue(jsonNode.get(2).toString().contains("\"contentTypeId\":\"dashboard\""));
  }
}
