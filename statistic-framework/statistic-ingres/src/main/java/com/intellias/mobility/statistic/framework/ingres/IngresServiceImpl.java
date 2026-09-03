/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.ingres;

import com.intellias.mobility.statistic.framework.preprocess.PreProcessService;
import com.intellias.mobility.statistic.framework.storage.StorageService;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Implementation of {@link IngresService} using a preprocessing pipeline and storage service. */
@RequiredArgsConstructor
public class IngresServiceImpl implements IngresService {
  private final PreProcessService preProcessService;
  private final StorageService storageService;

  /** {@inheritDoc} */
  @Override
  public void processAndStore(StatisticFeature feature, String indexSuffixName) {
    var preProcessResult = preProcessService.preprocess(feature, indexSuffixName);
    storageService.save(
        preProcessResult.feature(), indexSuffixName, preProcessResult.auxiliaryDocumentWrites());
  }

  /** {@inheritDoc} */
  @Override
  public void processAndStoreAll(
      List<? extends StatisticFeature<?>> features, String indexSuffixName) {
    var preProcessed = features.stream()
        .map(feature -> preProcessService.preprocess(feature, indexSuffixName))
        .toList();
    storageService.saveAll(
        preProcessed.stream().map(result -> (StatisticFeature) result.feature()).toList(),
        indexSuffixName,
        preProcessed.stream()
            .flatMap(result -> result.auxiliaryDocumentWrites().stream())
            .toList());
  }
}
