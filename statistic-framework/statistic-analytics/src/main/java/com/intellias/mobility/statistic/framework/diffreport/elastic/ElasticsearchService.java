/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.elastic;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeature;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeatureType;
import com.intellias.mobility.statistic.framework.config.AnalyticsProperties;
import com.intellias.mobility.statistic.framework.diffreport.model.DiffReportRequest;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;

@RequiredArgsConstructor
public class ElasticsearchService {

  private final ElasticsearchOperations elasticsearchOperations;
  private final StorageProperties storageProperties;
  private final AnalyticsProperties analyticsProperties;

  public SearchHitsIterator<DifferencePerFeature> getDifferencePerFeatureIterator(
      DiffReportRequest diffReportRequest) {
    var query = buildQuery(diffReportRequest);
    return elasticsearchOperations.searchForStream(
        query,
        DifferencePerFeature.class,
        IndexCoordinates.of(storageProperties.indexPrefix()
            + analyticsProperties.diffPerFeatureResultIndexPrefix()));
  }

  public SearchHitsIterator<DifferencePerFeatureType> getDifferencePerFeatureTypeIterator(
      DiffReportRequest diffReportRequest) {
    var query = buildQuery(diffReportRequest);
    return elasticsearchOperations.searchForStream(
        query,
        DifferencePerFeatureType.class,
        IndexCoordinates.of(storageProperties.indexPrefix()
            + analyticsProperties.diffPerFeatureTypeResultIndexPrefix()));
  }

  private Query buildQuery(DiffReportRequest diffReportRequest) {
    var boolBuilder = new BoolQuery.Builder();
    boolBuilder.must(m -> m.match(match ->
        match.field("metadata.sourceIndex.keyword").query(diffReportRequest.getIndexName())));
    boolBuilder.must(m -> m.match(match ->
        match.field("metadata.sourceVersion.keyword").query(diffReportRequest.getSourceVersion())));
    boolBuilder.must(m -> m.match(match ->
        match.field("metadata.targetVersion.keyword").query(diffReportRequest.getTargetVersion())));

    return NativeQuery.builder().withQuery(q -> q.bool(boolBuilder.build())).build();
  }
}
