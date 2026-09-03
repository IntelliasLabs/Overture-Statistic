/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess;

import com.intellias.mobility.statistic.framework.storage.AuxiliaryDocumentWrite;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.List;

/**
 * Produces auxiliary Elasticsearch writes derived from a preprocessed feature.
 *
 * <p>Materializers are evaluated after the regular {@link PreProcessor} chain has finished so they
 * can rely on enrichment such as calculated lengths, areas, or derived geometries already being
 * present on the feature.</p>
 */
public interface DerivedDocumentMaterializer {

  /**
   * Determines whether this materializer should emit auxiliary documents for the provided feature.
   *
   * @param feature the already preprocessed feature
   * @param indexName the logical destination suffix used for the source feature
   * @return {@code true} when the feature should produce derivative documents
   */
  boolean isApplicable(StatisticFeature<?> feature, String indexName);

  /**
   * Builds the auxiliary document writes that should be stored together with the source feature.
   *
   * @param feature the already preprocessed feature
   * @param indexName the logical destination suffix used for the source feature
   * @return auxiliary writes grouped later by storage by index and document type
   */
  List<AuxiliaryDocumentWrite> materialize(StatisticFeature<?> feature, String indexName);
}
