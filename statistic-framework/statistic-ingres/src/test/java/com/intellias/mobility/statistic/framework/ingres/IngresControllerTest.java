/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.ingres;

import static com.intellias.mobility.statistic.framework.testutils.data.TestFeatures.createPOI;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.testutils.IntegrationControllerTestSpec;
import com.intellias.statistic.model.feature.PointFeature;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IngresControllerTest extends IntegrationControllerTestSpec {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private ElasticsearchOperations elasticsearchOperations;

  @Autowired
  private ElasticManageClient elasticManageClient;

  @AfterAll
  void cleanUp() {
    deleteAllIndexes(elasticManageClient);
  }

  @SneakyThrows
  @Test
  void ingress() {
    var json = objectMapper.writeValueAsString(createPOI());

    var res = restTemplate.exchange(
        buildUrl("ingress/save"),
        HttpMethod.POST,
        new HttpEntity<>(json, buildHeaders()),
        Boolean.class);

    refreshIndex("statistic-point-poi", elasticsearchOperations);

    assertEquals(true, res.getBody());

    SearchHits<PointFeature> search = elasticsearchOperations.search(
        Query.findAll(), PointFeature.class, IndexCoordinates.of("statistic-point-poi"));

    assertFalse(search.getSearchHits().isEmpty());
  }
}
