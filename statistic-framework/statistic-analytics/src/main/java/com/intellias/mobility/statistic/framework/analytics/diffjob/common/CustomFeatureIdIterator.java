/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob.common;

import com.intellias.mobility.statistic.framework.analytics.diffjob.elastic.ElasticsearchRepository;
import com.intellias.mobility.statistic.framework.analytics.iterator.BatchIterator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomFeatureIdIterator extends BatchIterator<String, String> {

  private final ElasticsearchRepository elasticsearchRepository;
  private final String indexName;

  @Override
  protected List<String> fetchBatch(Optional<String> lastKey) {
    log.info(
        "Fetching distinct feature ids for index='{}' with lastKey={}",
        indexName,
        lastKey.orElse("<start>"));
    var batch = elasticsearchRepository.findDistinctFeatureSourceIds(indexName, lastKey);
    log.info(
        "Fetched {} distinct feature ids for index='{}' with lastKey={}, nextLastKey={}",
        batch.size(),
        indexName,
        lastKey.orElse("<start>"),
        batch.isEmpty() ? "<none>" : batch.getLast());
    return batch;
  }

  @Override
  protected Optional<String> extractLastKey(List<String> batch) {
    return Optional.of(batch.getLast());
  }
}
