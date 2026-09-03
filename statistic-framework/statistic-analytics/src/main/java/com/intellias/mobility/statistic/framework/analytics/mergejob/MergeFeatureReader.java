/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.mergejob;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.data.util.Pair;

@RequiredArgsConstructor
public class MergeFeatureReader extends AbstractItemStreamItemReader<Pair<String, String>> {

  private final MergeFeatureIdIterator customIterator;

  @Override
  public Pair<String, String> read() {
    return customIterator.getNextValue().orElse(null);
  }
}
