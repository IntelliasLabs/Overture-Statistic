/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.writer;

import com.intellias.mobility.statistic.framework.ingres.IngresService;
import com.intellias.statistic.model.feature.StatisticFeature;
import com.intellias.statistic.model.feature.StatisticFeatureProperties;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticFeatureWriter implements ItemWriter<StatisticFeature<?>> {

  private final IngresService ingresService;

  @Override
  public void write(Chunk<? extends StatisticFeature<?>> chunk) {
    log.info("=== WRITER CALLED ===");
    log.info("Chunk size: {}", chunk.size());
    log.info("Chunk empty: {}", chunk.isEmpty());

    Map<String, List<StatisticFeature<?>>> byIndex = chunk.getItems().stream()
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(it -> Optional.ofNullable(it.getProperties())
            .map(StatisticFeatureProperties::getFeatureType)
            .map(s -> s.toLowerCase(Locale.ROOT))
            .orElse("unknown")));

    byIndex.forEach((indexName, items) -> {
      indexName =
          indexName.contains("_") ? indexName.substring(0, indexName.indexOf("_")) : indexName;
      log.debug("Processing chunk with {} items for index: {}", items.size(), indexName);
      log.debug(
          "Chunk items: {}", items.stream().map(StatisticFeature::getFeatureId).toList());
      ingresService.processAndStoreAll(items, indexName);
      log.info(
          "Successfully processed chunk with {} items for index {} \n", items.size(), indexName);
    });
  }
}
