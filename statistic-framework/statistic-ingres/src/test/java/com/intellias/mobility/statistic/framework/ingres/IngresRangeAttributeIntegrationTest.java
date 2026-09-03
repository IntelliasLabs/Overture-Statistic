/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.ingres;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.elastic.model.IndexTemplatesResponse;
import com.intellias.mobility.statistic.framework.range.RangeDocument;
import com.intellias.mobility.statistic.framework.templates.IndexTemplateManager;
import com.intellias.statistic.model.attribute.Range;
import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.attribute.RangeAttributeValue;
import com.intellias.statistic.model.feature.LineFeature;
import com.intellias.statistic.model.feature.LineFeatureProperties;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.LonLat;
import com.intellias.statistic.model.geometry.PointGeometry;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IngresRangeAttributeIntegrationTest extends TestcontainersConfiguration {
  @Autowired
  private IngresService ingresService;

  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Autowired
  private ElasticManageClient elasticManageClient;

  @Autowired
  private IndexTemplateManager indexTemplateManager;

  @AfterAll
  void cleanUp() {
    deleteAllIndexes(elasticManageClient);
  }

  @Test
  void lineFeatureSaveCreatesRangeDocumentsAndTemplate() {
    var indexSuffix = "road-ingres-test";
    var lineFeature = new LineFeature(
        "feature-1",
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.137156353662704, 49.60200863275489)),
            new PointGeometry(new LonLat(6.13764026954567, 49.60122805471545)),
            new PointGeometry(new LonLat(6.14197400512117, 49.602768290455515)))),
        LineFeatureProperties.withRangeAttributes(
            "v1",
            "road",
            new Date(),
            List.of(
                new RangeAttribute(
                    "speed",
                    List.of(new RangeAttributeValue("50", List.of(new Range(0.0, 0.5)))),
                    null,
                    null),
                new RangeAttribute(
                    "name",
                    List.of(
                        new RangeAttributeValue("first", List.of(new Range(0.0, 0.5))),
                        new RangeAttributeValue("second", List.of(new Range(0.5, 1.0)))),
                    null,
                    null))));

    ingresService.processAndStore(lineFeature, indexSuffix);

    refreshIndex("statistic-rangeattribute-linestring-road-ingres-test", elasticsearchOperations);

    var rangeDocuments = elasticsearchOperations.search(
        Query.findAll(),
        RangeDocument.class,
        IndexCoordinates.of("statistic-rangeattribute-linestring-road-ingres-test"));

    assertEquals(5, rangeDocuments.getTotalHits());

    IndexTemplatesResponse response =
        indexTemplateManager.getIndexTemplate("statistic-rangeattribute");
    assertFalse(response.index_templates().isEmpty());
  }

  @Test
  void nonLineFeatureSaveDoesNotCreateRangeDocuments() {
    var rangeIndexName = "statistic-rangeattribute-point-poi-no-range";
    var pointFeature = new PointFeature(
        "point-1",
        new PointGeometry(new LonLat(6.134128158472095, 49.593476516900296)),
        new PointFeatureProperties("v1", "poi", new Date()));

    ingresService.processAndStore(pointFeature, "poi-no-range");

    assertFalse(
        elasticsearchOperations.indexOps(IndexCoordinates.of(rangeIndexName)).exists());
  }
}
