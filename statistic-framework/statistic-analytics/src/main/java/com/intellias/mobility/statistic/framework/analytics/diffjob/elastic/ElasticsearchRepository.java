/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob.elastic;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregationSource;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ElasticsearchRepository {

  private final ElasticsearchOperations elasticsearchOperations;

  private static final String AGGREGATION_KEY = "featureId";
  private static final int BATCH_SIZE = 10000;

  public List<String> findDistinctFeatureSourceIds(
      String indexName, Optional<String> afterValueOp) {

    var searchQuery = NativeQuery.builder()
        .withAggregation("distinct_featureIds", AggregationBuilders.composite(composite -> {
          composite.sources(Map.of(
              AGGREGATION_KEY,
              CompositeAggregationSource.of(cas -> cas.terms(t -> t.field("featureId.keyword")))));
          composite.size(BATCH_SIZE);
          afterValueOp.ifPresent(
              afterValue -> composite.after(Map.of(AGGREGATION_KEY, FieldValue.of(afterValue))));

          return composite;
        }))
        .build();

    long startedAt = System.nanoTime();
    try {
      var searchHits =
          elasticsearchOperations.search(searchQuery, Object.class, IndexCoordinates.of(indexName));
      log.info(
          "Distinct feature id search returned for index='{}', afterKey={}, took {} ms before aggregation parsing",
          indexName,
          afterValueOp.orElse("<start>"),
          (System.nanoTime() - startedAt) / 1_000_000);

      var aggregationsContainer = searchHits.getAggregations();
      log.info(
          "Distinct feature id aggregations extracted for index='{}', afterKey={}, took {} ms before bucket parsing",
          indexName,
          afterValueOp.orElse("<start>"),
          (System.nanoTime() - startedAt) / 1_000_000);
      var elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
      if (Objects.nonNull(aggregationsContainer)) {
        var aggregations = (List<ElasticsearchAggregation>) aggregationsContainer.aggregations();
        var stringTermsAggregate = (CompositeAggregate)
            aggregations.getFirst().aggregation().getAggregate()._get();
        var result = stringTermsAggregate.buckets().array().stream()
            .map(bucket -> bucket.key().get(AGGREGATION_KEY).stringValue())
            .toList();
        log.info(
            "Distinct feature id aggregation finished for index='{}' in {} ms, afterKey={}, resultCount={}",
            indexName,
            elapsedMillis,
            afterValueOp.orElse("<start>"),
            result.size());
        return result;
      } else {
        log.info(
            "Distinct feature id aggregation finished for index='{}' in {} ms, afterKey={}, resultCount=0 (no aggregations)",
            indexName,
            elapsedMillis,
            afterValueOp.orElse("<start>"));
        return List.of();
      }
    } catch (RuntimeException ex) {
      var elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
      log.error(
          "Distinct feature id aggregation failed for index='{}' in {} ms, afterKey={}",
          indexName,
          elapsedMillis,
          afterValueOp.orElse("<start>"),
          ex);
      throw ex;
    }
  }

  public <T extends StatisticFeature<?>> Optional<T> findFeature(
      String featureSourceId, String version, String indexName, Class<T> clazz) {
    BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

    boolBuilder.must(m -> m.term(t -> t.field("featureId.keyword").value(featureSourceId)));
    boolBuilder.must(m -> m.term(t -> t.field("properties.version.keyword").value(version)));

    Query query = Query.of(q -> q.bool(boolBuilder.build()));

    return Optional.ofNullable(elasticsearchOperations.searchOne(
            NativeQuery.builder().withQuery(query).build(), clazz, IndexCoordinates.of(indexName)))
        .map(SearchHit::getContent);
  }
}
