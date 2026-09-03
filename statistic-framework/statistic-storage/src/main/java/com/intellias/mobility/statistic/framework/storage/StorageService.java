/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.storage;

import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.List;

/**
 * Save Features into index.
 * Just keep in mind all indices will always have prefix configured in "statistic-app.storage" property.
 */
public interface StorageService {

  /**
   * Stores a feature and its derived documents using the configured feature index naming contract.
   */
  default void save(StatisticFeature feature, String indexNameSuffix) {
    save(feature, indexNameSuffix, List.of());
  }

  /**
   * Stores a feature together with optional auxiliary documents destined for other indices.
   *
   * <p>This is used by ingress when a single input feature should atomically fan out into multiple
   * persisted document streams such as the source feature, feature-property documents, and derived
   * range-attribute documents.</p>
   */
  void save(
      StatisticFeature feature,
      String indexNameSuffix,
      List<AuxiliaryDocumentWrite> auxiliaryDocumentWrites);

  /**
   * Stores a batch of features without auxiliary documents.
   */
  default void saveAll(List<StatisticFeature> features, String indexNameSuffix) {
    saveAll(features, indexNameSuffix, List.of());
  }

  /**
   * Stores a batch of features together with optional auxiliary documents emitted during
   * preprocessing.
   */
  void saveAll(
      List<StatisticFeature> features,
      String indexNameSuffix,
      List<AuxiliaryDocumentWrite> auxiliaryDocumentWrites);

  List<StatisticFeature> read(String indexName);

  List<StatisticFeature> read(String indexName, String version);
}
