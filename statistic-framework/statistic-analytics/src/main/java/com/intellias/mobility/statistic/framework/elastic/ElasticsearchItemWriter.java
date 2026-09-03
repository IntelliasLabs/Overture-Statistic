/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.elastic;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

public class ElasticsearchItemWriter<T> implements ItemWriter<T> {

  private final ElasticsearchOperations elasticsearchOperations;
  private final String indexName;

  public ElasticsearchItemWriter(
      ElasticsearchOperations elasticsearchOperations, String indexName) {
    this.elasticsearchOperations = elasticsearchOperations;
    this.indexName = indexName;
  }

  @Override
  public void write(Chunk<? extends T> items) {
    elasticsearchOperations.save(items.getItems(), IndexCoordinates.of(indexName));
  }
}
