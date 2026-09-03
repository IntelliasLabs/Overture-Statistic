/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.elastic;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

@RequiredArgsConstructor
public class ElasticsearchItemListWriter<T> implements ItemWriter<List<T>> {

  private final ElasticsearchOperations elasticsearchOperations;
  private final String indexName;
  private final int batchSize;

  @Override
  public void write(Chunk<? extends List<T>> chunk) {
    List<T> items = chunk.getItems().stream().flatMap(Collection::stream).toList();
    if (!items.isEmpty()) {
      saveInBatches(items, batchSize, this::saveBatch);
    }
  }

  public void saveBatch(List<T> features) {
    if (features.isEmpty()) return;
    elasticsearchOperations.save(features, IndexCoordinates.of(indexName));
  }

  // TODO: move to utils, and replace in StorageServiceImpl
  private void saveInBatches(List<T> items, int batchSize, Consumer<List<T>> batchProcessor) {
    if (batchSize <= 0 || items.size() <= batchSize) {
      batchProcessor.accept(items);
    } else {
      IntStream.iterate(0, i -> i < items.size(), i -> i + batchSize)
          .mapToObj(i -> items.subList(i, Math.min(items.size(), i + batchSize)))
          .forEach(batchProcessor);
    }
  }
}
