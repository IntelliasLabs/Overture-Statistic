/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob;

import com.intellias.mobility.statistic.framework.analytics.diffjob.common.CustomFeatureIdIterator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;

@Slf4j
@RequiredArgsConstructor
public class FeatureIdReader extends AbstractItemStreamItemReader<String> {

  private final CustomFeatureIdIterator customIterator;
  private boolean firstReadLogged;

  @Override
  public String read() {
    if (!firstReadLogged) {
      log.info("FeatureIdReader started reading feature ids");
      firstReadLogged = true;
    }
    return customIterator.getNextValue().orElse(null);
  }
}
