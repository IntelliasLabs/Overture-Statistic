/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.mergejob;

import static com.intellias.mobility.statistic.framework.analytics.mergejob.MergeFeaturesUtil.*;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import com.intellias.statistic.model.feature.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.util.Pair;

@Slf4j
public class MergeFeaturesProcessor<T extends StatisticFeature<?>>
    implements ItemProcessor<Pair<String, String>, StatisticFeature<?>> {

  private final ElasticsearchOperations elasticsearchOperations;
  private final String indexName;
  private final Class<T> featureClass;

  public MergeFeaturesProcessor(ElasticsearchOperations elasticsearchOperations, String indexName) {
    this.elasticsearchOperations = elasticsearchOperations;
    this.indexName = indexName;
    this.featureClass = identifyFeatureClass(indexName);
  }

  @Override
  public StatisticFeature<?> process(Pair<String, String> versionWithSourceId) throws Exception {
    var version = versionWithSourceId.getFirst();
    var sourceId = versionWithSourceId.getSecond();
    try (var searchHitsIterator = elasticsearchOperations.searchForStream(
        getTermQuery(version, sourceId), featureClass, IndexCoordinates.of(indexName))) {

      log.info(
          "Processing {} sourceId: {} version: {} index: {}",
          featureClass,
          sourceId,
          version,
          indexName);
      StatisticFeature<?> mergedFeature = null;

      while (searchHitsIterator.hasNext()) {
        var nextFeature = searchHitsIterator.next().getContent();

        if (mergedFeature == null) {
          mergedFeature = nextFeature;
        } else {
          mergedFeature = mergeFeatures(mergedFeature, nextFeature);
        }
      }

      return mergedFeature;
    } catch (Exception e) {
      log.error(
          "Error while processing sourceId {} version {}: {}",
          sourceId,
          version,
          e.getMessage(),
          e);
      throw new ItemStreamException(
          String.format("Error processing sourceId %s version %s", sourceId, version));
    }
  }

  private StatisticFeature<?> mergeFeatures(
      StatisticFeature<?> mergedFeature, StatisticFeature<?> nextFeature) {
    if (mergedFeature instanceof MultiPolygonFeature mergedMultiPolygonFeature
        && nextFeature instanceof MultiPolygonFeature nextMultiPolygonFeature) {

      return mergeMultiPolygonFeatures(mergedMultiPolygonFeature, nextMultiPolygonFeature);

    } else if (mergedFeature instanceof PolygonFeature mergedPolygonFeature
        && nextFeature instanceof PolygonFeature nextPolygonFeature) {

      return mergePolygonFeatures(mergedPolygonFeature, nextPolygonFeature);

    } else if (mergedFeature instanceof LineFeature mergedLineFeature
        && nextFeature instanceof LineFeature nextLineFeature) {
      return mergeLineFeatures(mergedLineFeature, nextLineFeature);
    } else if (mergedFeature instanceof MultiLineFeature mergedMultiLineFeature
        && nextFeature instanceof MultiLineFeature nextMultiLineFeature) {
      return mergeMultiLineFeatures(mergedMultiLineFeature, nextMultiLineFeature);
    } else if (mergedFeature instanceof MultiLineFeature mergedMultiLineFeature
        && nextFeature instanceof LineFeature nextLineFeature) {
      return mergeMultiLineWithLineFeature(mergedMultiLineFeature, nextLineFeature);
    } else if (mergedFeature instanceof MultiPolygonFeature mergedMultiPolygonFeature
        && nextFeature instanceof PolygonFeature nextPolygonFeature) {
      return mergeMultiPolygonWithPolygonFeatures(mergedMultiPolygonFeature, nextPolygonFeature);
    } else {
      throw new IllegalArgumentException("Incompatible feature types for merging.");
    }
  }

  @SuppressWarnings("unchecked")
  private Class<T> identifyFeatureClass(String indexName) {
    String lowerCaseIndexName = indexName.toLowerCase();
    if (lowerCaseIndexName.contains("multilinestring")) {
      return (Class<T>) MultiLineFeature.class;
    } else if (lowerCaseIndexName.contains("linestring")) {
      return (Class<T>) LineFeature.class;
    } else if (lowerCaseIndexName.contains("multipolygon")) {
      return (Class<T>) MultiPolygonFeature.class;
    } else if (lowerCaseIndexName.contains("polygon")) {
      return (Class<T>) PolygonFeature.class;
    } else {
      throw new IllegalStateException("Unexpected value: " + lowerCaseIndexName);
    }
  }

  private NativeQuery getTermQuery(String version, String sourceId) {
    return NativeQuery.builder()
        .withQuery(Query.of(q -> q.bool(b -> b.must(
            Query.of(q1 -> q1.term(
                TermQuery.of(tq -> tq.field("properties.globalSourceId.keyword").value(sourceId)))),
            Query.of(q2 -> q2.term(
                TermQuery.of(tq -> tq.field("properties.version.keyword").value(version))))))))
        .build();
  }
}
