/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess.impl;

import com.intellias.mobility.statistic.framework.preprocess.PreProcessor;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.StatisticFeature;

/**
 * Pre-processor for {@link PointFeature} that currently performs no modifications but acts as a
 * hook for future enhancements.
 */
public class PointPreProcessor implements PreProcessor {

  /** {@inheritDoc} */
  @Override
  public boolean isApplicable(StatisticFeature<?> feature, String indexName) {
    return feature instanceof PointFeature;
  }

  /** {@inheritDoc} */
  @Override
  public StatisticFeature<?> process(StatisticFeature<?> feature) {
    return feature;
  }
}
