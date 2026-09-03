/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.mergejob;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import com.intellias.mobility.statistic.framework.analytics.AnalyticsJobLauncherTestConfiguration;
import com.intellias.mobility.statistic.framework.analytics.job.AnalyticsJobExecutor;
import com.intellias.mobility.statistic.framework.analytics.job.AnalyticsJobLauncherConfiguration;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.storage.StorageService;
import com.intellias.statistic.model.feature.FeatureProperty;
import com.intellias.statistic.model.feature.MultiPolygonFeature;
import com.intellias.statistic.model.feature.PolygonFeatureProperties;
import com.intellias.statistic.model.feature.StatisticFeatureCollection;
import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.MultiPolygonGeometry;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.geometry.PolygonGeometry;
import com.intellias.statistic.model.util.JtsGeometryConverter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
@EnableAutoConfiguration(exclude = {AnalyticsJobLauncherConfiguration.class})
@Import(AnalyticsJobLauncherTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MergeJobTest extends TestcontainersConfiguration {
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private StorageService storageService;

  @Autowired
  private AnalyticsJobExecutor analyticsJobExecutor;

  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Autowired
  private ElasticManageClient elasticManageClient;

  @BeforeAll
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public void setUp() {
    var jsonPoiContent =
        Files.readAllBytes(Paths.get("src/test/resources/data/multipolygon-feature-area.json"));
    var featureCollection =
        objectMapper.readValue(jsonPoiContent, StatisticFeatureCollection.class);
    storageService.saveAll(featureCollection.getFeatures(), "area");
    refreshIndex("statistic-multipolygon-area", elasticsearchOperations);
  }

  @AfterAll
  void cleanUp() {
    deleteAllIndexes(elasticManageClient);
  }

  @Test
  void mergeJobTest() {
    Map<String, Object> runtimeParameters =
        Map.of("sourceIndexes", List.of("statistic-multipolygon-area"));
    analyticsJobExecutor.execute(MergeJobConfiguration.JOB_NAME, runtimeParameters);
    refreshIndex("statistic-multipolygon-area", elasticsearchOperations);
    refreshIndex("statistic-multipolygon-area-merged", elasticsearchOperations);

    var mergedFeature = elasticsearchOperations
        .search(
            elasticsearchOperations.matchAllQuery(),
            MultiPolygonFeature.class,
            IndexCoordinates.of("statistic-multipolygon-area-merged"))
        .getSearchHits()
        .getFirst()
        .getContent();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    ZonedDateTime zonedDateTime = ZonedDateTime.parse("2025-04-03T12:30:53.006+0300", formatter);

    var expectedPolygonProperties = new PolygonFeatureProperties(
        "v1", "ADMIN-DISPLAY-AREA-SET", Date.from(zonedDateTime.toInstant()));

    expectedPolygonProperties.setGlobalSourceId("123-LUX");

    var expectedFeatureProperties = List.of(
        new FeatureProperty(
            "ADMINISTRATIVE_HIERARCHY",
            List.of(
                "COUNTRY - Luxembourg",
                "COUNTRY - Lëtzebuerg",
                "SUB_COUNTRY - Canton Remich",
                "MUNICIPALITY - Bous-Waldbredimus",
                "COUNTRY - Großherzogtum Luxemburg",
                "COUNTRY - Люксембург")),
        new FeatureProperty(
            "ADMINISTRATIVE_AREA_NAME",
            List.of(
                "Luxemburg",
                "Lëtzebuerg",
                "Bous-Waldbredimus",
                "Remich",
                "Grand Duchy of Luxembourg")),
        new FeatureProperty("TYPE", List.of("AREA_COUNTRY")));

    expectedPolygonProperties.setFeatureProperties(expectedFeatureProperties);

    LineGeometry coords = new LineGeometry(List.of(
        new PointGeometry(6.328125, 49.5263671875),
        new PointGeometry(6.328125, 49.54833984375),
        new PointGeometry(6.35009765625, 49.54833984375),
        new PointGeometry(6.35009765625, 49.5263671875),
        new PointGeometry(6.35009765625, 49.50439453125),
        new PointGeometry(6.328125, 49.50439453125),
        new PointGeometry(6.328125, 49.5263671875)));

    var expectedGeometry =
        new MultiPolygonGeometry(List.of(new PolygonGeometry(coords, List.of())));

    var expectedArea = MergeFeaturesUtil.calculateGeometryArea(
        JtsGeometryConverter.toJtsMultiPolygon(expectedGeometry));
    expectedPolygonProperties.setArea(expectedArea);

    var expectedMultiPolygonFeature = MultiPolygonFeature.builder()
        .featureId("id1-1")
        .properties(expectedPolygonProperties)
        .geometry(expectedGeometry)
        .build();

    Assertions.assertEquals(expectedMultiPolygonFeature, mergedFeature);
  }
}
