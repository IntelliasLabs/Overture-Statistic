/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess;

import com.intellias.mobility.statistic.framework.storage.AuxiliaryDocumentWrite;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.List;

/**
 * Output of the ingress preprocessing stage.
 *
 * <p>The result contains the mutated source feature that should be stored in the regular feature
 * index as well as any derivative documents that need to be persisted to auxiliary indices during
 * the same storage operation.</p>
 *
 * @param feature the preprocessed source feature
 * @param auxiliaryDocumentWrites auxiliary documents emitted from materializers
 */
public record PreProcessResult(
    StatisticFeature<?> feature, List<AuxiliaryDocumentWrite> auxiliaryDocumentWrites) {
  public PreProcessResult {
    auxiliaryDocumentWrites =
        auxiliaryDocumentWrites == null ? List.of() : List.copyOf(auxiliaryDocumentWrites);
  }
}
