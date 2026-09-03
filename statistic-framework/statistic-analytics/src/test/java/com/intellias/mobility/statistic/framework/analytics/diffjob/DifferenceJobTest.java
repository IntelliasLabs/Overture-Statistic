/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob;

import static com.intellias.mobility.statistic.framework.analytics.diffjob.DifferenceJobDefinitionProvider.SOURCE_VERSION_KEY;
import static com.intellias.mobility.statistic.framework.analytics.diffjob.DifferenceJobDefinitionProvider.TARGET_VERSION_KEY;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import com.intellias.mobility.statistic.framework.analytics.AnalyticsJobLauncherTestConfiguration;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferenceMetadata;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeature;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeatureMetadata;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeatureType;
import com.intellias.mobility.statistic.framework.analytics.job.AnalyticsJobExecutor;
import com.intellias.mobility.statistic.framework.analytics.job.AnalyticsJobLauncherConfiguration;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.storage.StorageService;
import com.intellias.statistic.model.feature.StatisticFeatureCollection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
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
public class DifferenceJobTest extends TestcontainersConfiguration {

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
        Files.readAllBytes(Paths.get("src/test/resources/data/point-poi-collection.json"));
    var featureCollection =
        objectMapper.readValue(jsonPoiContent, StatisticFeatureCollection.class);
    storageService.saveAll(featureCollection.getFeatures(), "poi");
    refreshIndex("statistic-point-poi", elasticsearchOperations);
  }

  @AfterAll
  void cleanUp() {
    deleteAllIndexes(elasticManageClient);
  }

  @Test
  void differenceJobTest() {
    Map<String, Object> runtimeParameters = Map.of(
        SOURCE_VERSION_KEY, "1",
        TARGET_VERSION_KEY, "2");
    analyticsJobExecutor.execute(DifferenceJobConfiguration.JOB_NAME, runtimeParameters);
    refreshIndex("statistic-difference-per-feature-type-result", elasticsearchOperations);
    refreshIndex("statistic-difference-per-feature-result", elasticsearchOperations);

    // diff per feature type
    var diffPerFeatureTypeResult = elasticsearchOperations
        .search(
            elasticsearchOperations.matchAllQuery(),
            DifferencePerFeatureType.class,
            IndexCoordinates.of("statistic-difference-per-feature-type-result"))
        .getSearchHits()
        .getFirst()
        .getContent();

    var expectedPerFeatureTypeMetadata = new DifferenceMetadata("statistic-point-poi", "1", "2");
    Assertions.assertEquals(expectedPerFeatureTypeMetadata, diffPerFeatureTypeResult.getMetadata());

    var expectedPerFeatureTypeAddedFeatureProperties = Map.of(
        "HOUSE_NUMBER", Set.of("8"),
        "ISO_DETAILS", Set.of("LUX LU-EC"),
        "ADMINISTRATIVE_HIERARCHY",
            Set.of(
                "COUNTRY - Luxembourg", "SUB_COUNTRY - Canton Echternach", "MUNICIPALITY - Bech"),
        "CATEGORY", Set.of("Shop"),
        "ROAD_NAME", Set.of("Haupeschbierg"),
        "NAME", Set.of("Hondsschoul Heureka"));
    Assertions.assertEquals(
        expectedPerFeatureTypeAddedFeatureProperties,
        diffPerFeatureTypeResult.getAddedFeatureProperties());

    var expectedPerFeatureTypeDeletedFeatureProperties = Map.of(
        "ADMINISTRATIVE_HIERARCHY", Set.of("COUNTRY - Luxembourg from first version"),
        "CATEGORY", Set.of("POICAT_ACCESS_POINT"));
    Assertions.assertEquals(
        expectedPerFeatureTypeDeletedFeatureProperties,
        diffPerFeatureTypeResult.getDeletedFeatureProperties());

    var expectedPerFeatureTypeAddedFeatureIds = Set.of("545366907-2");
    Assertions.assertEquals(
        expectedPerFeatureTypeAddedFeatureIds, diffPerFeatureTypeResult.getAddedFeatureIds());

    // diff per feature
    var diffPerFeatureResult = elasticsearchOperations
        .search(
            elasticsearchOperations.matchAllQuery(),
            DifferencePerFeature.class,
            IndexCoordinates.of("statistic-difference-per-feature-result"))
        .getSearchHits()
        .getFirst()
        .getContent();

    var expectedPerFeatureMetadata =
        new DifferencePerFeatureMetadata("statistic-point-poi", "1", "2", "545366907-1");
    Assertions.assertEquals(expectedPerFeatureMetadata, diffPerFeatureResult.getMetadata());

    Assertions.assertNull(diffPerFeatureResult.getGeometryDifference()); // there aren't any changes

    var expectedPerFeatureAddedFeatureProperties =
        Map.of("ADMINISTRATIVE_HIERARCHY", Set.of("COUNTRY - Luxembourg"));
    Assertions.assertEquals(
        expectedPerFeatureAddedFeatureProperties, diffPerFeatureResult.getAddedFeatureProperties());

    var expectedPerFeatureDeletedFeatureProperties = Map.of(
        "ADMINISTRATIVE_HIERARCHY", Set.of("COUNTRY - Luxembourg from first version"),
        "CATEGORY", Set.of("POICAT_ACCESS_POINT"));
    Assertions.assertEquals(
        expectedPerFeatureDeletedFeatureProperties,
        diffPerFeatureResult.getDeletedFeatureProperties());
  }
}
