/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.ingres;

import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.List;

/**
 * Service responsible for preprocessing features and storing them in the underlying storage.
 */
public interface IngresService {

  /**
   * Processes a single feature and stores it under the provided index.
   */
  void processAndStore(StatisticFeature feature, String indexName);

  /**
   * Processes a list of features and stores them under the provided index.
   */
  void processAndStoreAll(List<? extends StatisticFeature<?>> features, String indexName);
}
