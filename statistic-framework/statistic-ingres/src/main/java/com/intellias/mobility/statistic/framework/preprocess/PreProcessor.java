/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess;

import com.intellias.statistic.model.feature.StatisticFeature;

/**
 * Component that performs additional enrichment or validation of {@link StatisticFeature}
 * objects before they are stored.
 */
public interface PreProcessor {

  /**
   * Checks whether this processor should handle the given feature for the specified index.
   *
   * @param feature the feature to check
   * @param indexName name of the target index
   * @return {@code true} if this processor can process the feature
   */
  boolean isApplicable(StatisticFeature<?> feature, String indexName);

  /**
   * Processes and potentially modifies the supplied feature.
   *
   * @param feature the feature to process
   * @return the processed feature
   */
  StatisticFeature<?> process(StatisticFeature<?> feature);
}
