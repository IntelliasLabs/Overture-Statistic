/**
 Copyright ©2024-2025 Intellias
 */
package com.intellias.mobility.statistic.framework.ingres;

import com.intellias.statistic.model.feature.StatisticFeature;
import com.intellias.statistic.model.feature.StatisticFeatureCollection;
import com.intellias.statistic.model.geometry.StatisticGeometry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** REST controller for ingesting statistic features. */
@RestController
@RequiredArgsConstructor
public class IngresController implements IngresControllerOpenApi {
  private final IngresService ingresService;

  /** {@inheritDoc} */
  @Override
  public boolean save(@RequestBody StatisticFeature<?> feature) {
    var indexName = toIndexName(feature);
    ingresService.processAndStore(feature, indexName);
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean saveAll(@RequestBody StatisticFeatureCollection<StatisticGeometry> collection) {
    var features = collection.getFeatures();

    if (!features.isEmpty()) {
      var indexName = toIndexName(features.getFirst());
      ingresService.processAndStoreAll(features, indexName);
    }
    return true;
  }

  /** Resolves index name from feature type. */
  String toIndexName(StatisticFeature<?> feature) {
    return feature.getProperties().getFeatureType().toLowerCase();
  }
}
