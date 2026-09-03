/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.mergejob.elastic;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.aggregations.AggregationBuilders;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.CompositeAggregationSource;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MergeElasticsearchRepository {

  private final ElasticsearchOperations elasticsearchOperations;

  private static final String VERSION_FIELD = "properties.version.keyword";
  private static final String SOURCE_ID_FIELD = "properties.globalSourceId.keyword";
  private static final String AGGREGATION_NAME = "distinct_featureIds";
  private static final String VERSION_AGGREGATION = "version";
  private static final String SOURCE_ID_AGGREGATION = "sourceId";
  private static final int BATCH_SIZE = 10000;

  public List<Pair<String, String>> findDistinctFeatureGlobalSourceIds(
      String indexName, Optional<Map<String, String>> afterKeyOpt, Set<String> versionsFilter) {
    var searchQuery = NativeQuery.builder()
        .withAggregation(AGGREGATION_NAME, AggregationBuilders.composite(composite -> {
          composite.sources(
              Map.of(
                  VERSION_AGGREGATION,
                  CompositeAggregationSource.of(cas -> cas.terms(t -> t.field(VERSION_FIELD)))),
              Map.of(
                  SOURCE_ID_AGGREGATION,
                  CompositeAggregationSource.of(cas -> cas.terms(t -> t.field(SOURCE_ID_FIELD)))));

          composite.size(BATCH_SIZE);

          afterKeyOpt.ifPresent(afterValue -> {
            Map<String, FieldValue> afterMap = new HashMap<>();
            afterValue.forEach((k, v) -> afterMap.put(k, FieldValue.of(v)));
            composite.after(afterMap);
          });

          return composite;
        }));

    if (!versionsFilter.isEmpty()) {
      List<FieldValue> fieldValues =
          versionsFilter.stream().map(FieldValue::of).collect(Collectors.toList());

      searchQuery.withQuery(Query.of(
          q -> q.terms(t -> t.field(VERSION_FIELD).terms(terms -> terms.value(fieldValues)))));
    }

    var searchHits = elasticsearchOperations.search(
        searchQuery.build(), Object.class, IndexCoordinates.of(indexName));

    var aggregationsContainer = searchHits.getAggregations();
    if (aggregationsContainer != null) {
      var aggregations = (List<ElasticsearchAggregation>) aggregationsContainer.aggregations();
      var compositeAgg = (CompositeAggregate)
          aggregations.getFirst().aggregation().getAggregate()._get();

      return compositeAgg.buckets().array().stream()
          .map(bucket -> {
            String version = bucket.key().get(VERSION_AGGREGATION).stringValue();
            String sourceId = bucket.key().get(SOURCE_ID_AGGREGATION).stringValue();
            return Pair.of(version, sourceId);
          })
          .toList();
    } else {
      return List.of();
    }
  }
}
