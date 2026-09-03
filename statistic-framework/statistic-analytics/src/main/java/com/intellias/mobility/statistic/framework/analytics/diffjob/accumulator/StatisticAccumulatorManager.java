/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob.accumulator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class StatisticAccumulatorManager {

  private final Map<String, StatisticAccumulator> accumulatorMap;

  public StatisticAccumulatorManager() {
    this.accumulatorMap = new ConcurrentHashMap<>();
  }

  public StatisticAccumulator getAccumulator(String jobId) {
    return accumulatorMap.computeIfAbsent(jobId, key -> new StatisticAccumulator());
  }

  public void removeAccumulator(String jobId) {
    accumulatorMap.remove(jobId);
  }
}
