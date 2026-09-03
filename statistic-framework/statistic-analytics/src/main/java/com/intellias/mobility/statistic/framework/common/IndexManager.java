/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.common;

import static com.intellias.mobility.statistic.framework.analytics.mergejob.MergeJobDefinitionProvider.MERGE_NAME;
import static com.intellias.mobility.statistic.framework.range.RangeAttributeIndexSupport.RANGE_ATTR_INDEX_PREFIX;

import com.intellias.mobility.statistic.framework.config.AnalyticsProperties;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IndexManager {

  private final ElasticsearchOperations elasticsearchOperations;
  private final ElasticManageClient elasticManageClient;
  private final StorageProperties storageProperties;
  private final AnalyticsProperties analyticsProperties;

  public Map<String, Object> getIndexMapping(String indexName) {
    IndexOperations indexOperations =
        elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
    return indexOperations.getMapping();
  }

  public List<String> getIndexList(String indexPattern) {
    var str =
        elasticManageClient.performString("GET", "/_cat/indices?h=index&index=" + indexPattern);
    return Arrays.stream(str.split("\n"))
        .map(str2 -> str2.replace("\r", ""))
        .filter(indexName -> !indexName.isEmpty())
        .toList();
  }

  public Map<String, Map<String, Object>> getIndicesMapping(String indexPattern) {
    return StreamEx.of(getIndexList(indexPattern))
        .mapToEntry(this::getIndexMapping)
        .toMap();
  }

  /**
   * The difference between this and getIndexList is that this adds an index prefix from {@link StorageProperties}.
   */
  public List<String> getIndexListWithSystemPrefix(String indexSuffixPattern) {
    var str = elasticManageClient.performString(
        "GET",
        String.format(
            "/_cat/indices?h=index&index=%s%s",
            storageProperties.indexPrefix(), indexSuffixPattern));
    return Arrays.stream(str.split("\n"))
        .map(str2 -> str2.replace("\r", ""))
        .filter(indexName -> !indexName.isEmpty())
        .toList();
  }

  public Set<String> getIndexListForRangeJob() {
    return getFilteredIndexList("-linestring*", Optional.empty());
  }

  public Set<String> getAllFeatureIndexList() {
    return getFilteredIndexList("*", Optional.empty());
  }

  // line and multiline
  public Set<String> getLineFeatureIndexList() {
    return getFilteredIndexList("*", Optional.of("linestring"));
  }

  // polygon and multipolygon
  public Set<String> getPolygonFeatureIndexList() {
    return getFilteredIndexList("*", Optional.of("polygon"));
  }

  public Set<String> getFeatureIndexForMergingList() {
    // currently merging works for: "multipolygon", "polygon", "line", "multiline"
    var polyIndexes = getPolygonFeatureIndexList();
    var lineIndexes = getLineFeatureIndexList();

    return Stream.concat(polyIndexes.stream(), lineIndexes.stream())
        .filter(indexName -> !indexName.contains(MERGE_NAME))
        .collect(Collectors.toSet());
  }

  // point and multipoint
  public Set<String> getPointFeatureIndexList() {
    return getFilteredIndexList("*", Optional.of("point"));
  }

  public Set<String> getAllFeaturePropertiesIndexList() {
    return new HashSet<>(
        getIndexListWithSystemPrefix("*" + storageProperties.featurePropertiesIndexSuffix()));
  }

  public Set<String> getPointFeaturePropertiesIndexList() {
    return getFilteredFeaturePropertiesIndexList("point");
  }

  public Set<String> getLineFeaturePropertiesIndexList() {
    return getFilteredFeaturePropertiesIndexList("linestring");
  }

  public Set<String> getPolygonFeaturePropertiesIndexList() {
    return getFilteredFeaturePropertiesIndexList("polygon");
  }

  public Set<String> getAllRangeAttributesIndexList() {
    return new HashSet<>(getIndexListWithSystemPrefix("-" + RANGE_ATTR_INDEX_PREFIX + "*"));
  }

  public Set<String> getDifferencePerFeatureIndex() {
    return Set.of(
        storageProperties.indexPrefix() + analyticsProperties.diffPerFeatureResultIndexPrefix());
  }

  public Set<String> getDifferencePerFeatureTypeIndex() {
    return Set.of(storageProperties.indexPrefix()
        + analyticsProperties.diffPerFeatureTypeResultIndexPrefix());
  }

  private Set<String> getFilteredIndexList(
      String indexSuffixPattern, Optional<String> additionalContext) {
    return getIndexListWithSystemPrefix(indexSuffixPattern).stream()
        .filter(indexName -> !indexName.contains(storageProperties.featurePropertiesIndexSuffix()))
        .filter(indexName ->
            !indexName.contains(analyticsProperties.diffPerFeatureTypeResultIndexPrefix()))
        .filter(
            indexName -> !indexName.contains(analyticsProperties.diffPerFeatureResultIndexPrefix()))
        .filter(indexName -> !indexName.contains(RANGE_ATTR_INDEX_PREFIX))
        .filter(indexName -> additionalContext.map(indexName::contains).orElse(true))
        .collect(Collectors.toSet());
  }

  private Set<String> getFilteredFeaturePropertiesIndexList(String additionalContext) {
    return getIndexListWithSystemPrefix("*" + storageProperties.featurePropertiesIndexSuffix())
        .stream()
        .filter(indexName -> indexName.contains(additionalContext))
        .collect(Collectors.toSet());
  }
}
