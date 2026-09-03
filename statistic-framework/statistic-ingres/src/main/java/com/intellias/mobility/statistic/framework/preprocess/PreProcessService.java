/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess;

import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.List;
import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;

/**
 * Service that delegates feature processing to a list of {@link PreProcessor} instances.
 */
@RequiredArgsConstructor
public class PreProcessService {
  private final List<PreProcessor> preProcessors;
  private final List<DerivedDocumentMaterializer> derivedDocumentMaterializers;

  public PreProcessService(List<PreProcessor> preProcessors) {
    this(preProcessors, List.of());
  }

  /**
   * Runs the provided feature through the chain of {@link PreProcessor} instances.
   *
   * @param feature the feature to preprocess
   * @param indexName name of the index that will store the feature
   * @return the feature after all applicable preprocessors have been applied
   */
  public PreProcessResult preprocess(final StatisticFeature feature, String indexName) {
    var processedFeature = StreamEx.of(preProcessors)
        .filter(preProcessor -> preProcessor.isApplicable(feature, indexName))
        .foldLeft(feature, (f, p) -> p.process(f));

    var auxiliaryDocumentWrites = StreamEx.of(derivedDocumentMaterializers)
        .filter(materializer -> materializer.isApplicable(processedFeature, indexName))
        .flatCollection(materializer -> materializer.materialize(processedFeature, indexName))
        .toList();

    return new PreProcessResult(processedFeature, auxiliaryDocumentWrites);
  }
}
