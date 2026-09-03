/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import com.intellias.mobility.statistic.framework.analytics.AnalyticsJobLauncherTestConfiguration;
import com.intellias.mobility.statistic.framework.analytics.diffjob.DifferenceJobConfiguration;
import com.intellias.mobility.statistic.framework.analytics.mergejob.MergeJobConfiguration;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.ingres.IngresService;
import com.intellias.mobility.statistic.framework.range.RangeDocument;
import com.intellias.statistic.model.feature.LineFeature;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
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
class AnalyticsJobConfigurationTest extends TestcontainersConfiguration {

  @Autowired
  private AnalyticsJobExecutor analyticsJobExecutor;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private IngresService ingresService;

  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Autowired
  private ElasticManageClient elasticManageClient;

  @AfterAll
  void cleanUp() {
    deleteAllIndexes(elasticManageClient);
  }

  @Test
  void testJobLoading() {
    var jobs = analyticsJobExecutor.registeredJobList();

    assertFalse(jobs.isEmpty());
    Assertions.assertEquals(MergeJobConfiguration.JOB_NAME, jobs.get(0).name());
    Assertions.assertEquals(DifferenceJobConfiguration.JOB_NAME, jobs.get(1).name());
    assertEquals(2, jobs.size());
  }

  @SneakyThrows
  @Test
  void testRangeDocumentsAreCreatedDuringIngress() {

    var featureFile = Files.readAllBytes(Path.of("src/test/resources/data/line-feature-road.json"));
    var feature2File =
        Files.readAllBytes(Path.of("src/test/resources/data/line-feature-road2.json"));
    var feature = objectMapper.readValue(featureFile, LineFeature.class);
    var feature2 = objectMapper.readValue(feature2File, LineFeature.class);

    ingresService.processAndStore(
        feature, feature.getProperties().getFeatureType().toLowerCase());
    ingresService.processAndStore(
        feature2, feature2.getProperties().getFeatureType().toLowerCase());

    refreshIndex("statistic-linestring-road", elasticsearchOperations);
    refreshIndex("statistic-linestring-road2", elasticsearchOperations);

    refreshIndex("statistic-rangeattribute-linestring-road", elasticsearchOperations);
    refreshIndex("statistic-rangeattribute-linestring-road2", elasticsearchOperations);

    var rangeDocList = elasticsearchOperations.search(
        elasticsearchOperations.matchAllQuery(),
        RangeDocument.class,
        IndexCoordinates.of("statistic-rangeattribute*"));

    assertFalse(rangeDocList.getSearchHits().isEmpty());
    assertEquals(12, rangeDocList.getTotalHits());
  }
}
