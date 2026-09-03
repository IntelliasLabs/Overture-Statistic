/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.mergejob;

import com.intellias.mobility.statistic.framework.analytics.iterator.BatchIterator;
import com.intellias.mobility.statistic.framework.analytics.mergejob.elastic.MergeElasticsearchRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;

@RequiredArgsConstructor
public class MergeFeatureIdIterator
    extends BatchIterator<Pair<String, String>, Map<String, String>> {

  private final MergeElasticsearchRepository mergeElasticsearchRepository;
  private final String indexName;
  private final Set<String> versionsFilter;

  @Override
  protected List<Pair<String, String>> fetchBatch(Optional<Map<String, String>> lastKey) {
    return mergeElasticsearchRepository.findDistinctFeatureGlobalSourceIds(
        indexName, lastKey, versionsFilter);
  }

  @Override
  protected Optional<Map<String, String>> extractLastKey(List<Pair<String, String>> batch) {
    var last = batch.getLast();
    return Optional.of(Map.of("version", last.getFirst(), "sourceId", last.getSecond()));
  }
}
