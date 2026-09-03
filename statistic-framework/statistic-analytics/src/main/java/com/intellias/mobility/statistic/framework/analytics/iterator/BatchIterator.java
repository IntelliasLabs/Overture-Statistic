/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.iterator;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BatchIterator<T, K> {
  private final Queue<T> queue = new LinkedList<>();
  private Optional<K> lastKey = Optional.empty();

  public Optional<T> getNextValue() {
    if (queue.isEmpty() && !fetchNextBatch()) {
      return Optional.empty();
    }
    return Optional.ofNullable(queue.poll());
  }

  private boolean fetchNextBatch() {
    try {
      List<T> batch = fetchBatch(lastKey);
      if (batch.isEmpty()) {
        lastKey = Optional.empty();
        return false;
      }

      queue.addAll(batch);
      lastKey = extractLastKey(batch);
      return true;
    } catch (RuntimeException ex) {
      log.error(
          "Failed to fetch next batch in {} with lastKey={}",
          getClass().getSimpleName(),
          lastKey.orElse(null),
          ex);
      throw ex;
    }
  }

  protected abstract List<T> fetchBatch(Optional<K> lastKey);

  protected abstract Optional<K> extractLastKey(List<T> batch);
}
