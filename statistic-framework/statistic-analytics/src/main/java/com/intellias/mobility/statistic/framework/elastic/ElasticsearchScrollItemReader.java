/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.elastic;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public class ElasticsearchScrollItemReader<T> extends AbstractItemStreamItemReader<T> {

  private final ElasticsearchOperations elasticsearchOperations;
  private final Query query;
  private final Class<T> clazz;
  private final String indexName;

  private SearchHitsIterator<T> searchHitsIterator;

  @Override
  public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
    searchHitsIterator =
        elasticsearchOperations.searchForStream(query, clazz, IndexCoordinates.of(indexName));
  }

  @Nullable @Override
  public T read() {
    if (searchHitsIterator == null || !searchHitsIterator.hasNext()) {
      return null;
    }
    return searchHitsIterator.next().getContent();
  }

  @Override
  public void close() {
    if (searchHitsIterator != null) {
      searchHitsIterator.close();
    }
  }
}
