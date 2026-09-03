/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.storage;

import static java.util.stream.Collectors.*;

import com.intellias.mobility.statistic.framework.property.FeaturePropertyDocumentsBuilder;
import com.intellias.statistic.model.feature.*;
import com.intellias.statistic.model.geometry.StatisticGeometry;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query.SearchType;

@SuppressWarnings("rawtypes")
@Slf4j
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

  private final ElasticsearchOperations elasticsearchOperations;
  private final FeaturePropertyDocumentsBuilder featurePropertyDocumentsBuilder;
  private final String indexPrefix;
  private final String featurePropertiesIndexSuffix;
  private final int batchSize;

  @Override
  public void save(
      StatisticFeature feature,
      String indexNameSuffix,
      List<AuxiliaryDocumentWrite> auxiliaryDocumentWrites) {
    elasticsearchOperations.save(feature, toIndexName(indexNameSuffix, feature));

    var featureProperties = featurePropertyDocumentsBuilder.buildFeaturePropertyDocuments(feature);
    elasticsearchOperations.save(
        featureProperties, toPropertiesIndexName(indexNameSuffix, feature));
    saveAuxiliaryDocuments(auxiliaryDocumentWrites);
  }

  @Override
  public void saveAll(
      List<StatisticFeature> features,
      String indexNameSuffix,
      List<AuxiliaryDocumentWrite> auxiliaryDocumentWrites) {
    if (features != null && !features.isEmpty()) {
      saveInBatches(features, batchSize, batch -> saveBatch(batch, indexNameSuffix));
    }
    saveAuxiliaryDocuments(auxiliaryDocumentWrites);
  }

  public void saveBatch(List<StatisticFeature> features, String indexNameSuffix) {
    if (features.isEmpty()) return;

    var feature = features.getFirst();
    elasticsearchOperations.save(features, toIndexName(indexNameSuffix, feature));

    var featuresProperties = features.stream()
        .flatMap(ftr -> featurePropertyDocumentsBuilder.buildFeaturePropertyDocuments(ftr).stream())
        .toList();

    if (!featuresProperties.isEmpty()) {
      saveInBatches(
          featuresProperties,
          batchSize,
          batch ->
              elasticsearchOperations.save(batch, toPropertiesIndexName(indexNameSuffix, feature)));
    }
  }

  private <T> void saveInBatches(List<T> items, int batchSize, Consumer<List<T>> batchProcessor) {
    if (batchSize <= 0 || items.size() <= batchSize) {
      batchProcessor.accept(items);
    } else {
      IntStream.iterate(0, i -> i < items.size(), i -> i + batchSize)
          .mapToObj(i -> items.subList(i, Math.min(items.size(), i + batchSize)))
          .forEach(batchProcessor);
    }
  }

  private void saveAuxiliaryDocuments(List<AuxiliaryDocumentWrite> auxiliaryDocumentWrites) {
    if (auxiliaryDocumentWrites == null || auxiliaryDocumentWrites.isEmpty()) {
      return;
    }

    auxiliaryDocumentWrites.stream()
        .collect(groupingBy(
            write -> new AuxiliaryDocumentBatchKey(
                write.indexName(), write.document().getClass()),
            mapping(AuxiliaryDocumentWrite::document, toList())))
        .forEach((batchKey, documents) -> saveInBatches(
            documents, batchSize, batch -> saveAuxiliaryBatch(batch, batchKey.indexName())));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void saveAuxiliaryBatch(List<?> documents, String indexName) {
    elasticsearchOperations.save((List) documents, IndexCoordinates.of(indexName));
  }

  @Override
  public List<StatisticFeature> read(String indexName) {
    var clazz = getClassFromName(indexName);

    var query = NativeQuery.builder();

    query.withPageable(Pageable.unpaged()).withSearchType(SearchType.DFS_QUERY_THEN_FETCH);

    SearchHits<? extends StatisticFeature<?>> search =
        elasticsearchOperations.search(query.build(), clazz, IndexCoordinates.of(indexName));
    return search.getSearchHits().stream()
        .map(SearchHit::getContent)
        .map(capture -> (StatisticFeature) capture)
        .toList();
  }

  @Override
  public List<StatisticFeature> read(String indexName, String version) {
    var clazz = getClassFromName(indexName);

    var query = NativeQuery.builder();

    query.withPageable(Pageable.unpaged()).withSearchType(SearchType.DFS_QUERY_THEN_FETCH);
    // TODO add version to query
    SearchHits<? extends StatisticFeature<?>> search =
        elasticsearchOperations.search(query.build(), clazz, IndexCoordinates.of(indexName));

    return search.getSearchHits().stream()
        .map(SearchHit::getContent)
        .map(capture -> (StatisticFeature) capture)
        .toList();
  }

  private Class<? extends StatisticFeature<? extends StatisticGeometry>> getClassFromName(
      String indexName) {
    String[] array = indexName.split("-");
    String featureClass = array[array.length - 2].toUpperCase();
    return switch (featureClass) {
      case "LINESTRING" -> LineFeature.class;
      case "MULTILINESTRING" -> MultiLineFeature.class;
      case "GEOMETRYCOLLECTION" -> GeometryCollectionFeature.class;
      case "POLYGON" -> PolygonFeature.class;
      case "MULTIPOLYGON" -> MultiPolygonFeature.class;
      case "POINTGEOMETRY" -> PointFeature.class;
      case "MULTIPOINT" -> MultiPointFeature.class;
      default -> throw new IllegalStateException(
          "Unexpected value for StatisticFeature descendant: " + featureClass);
    };
  }

  IndexCoordinates toIndexName(String indexSuffixName, StatisticFeature feature) {
    return IndexCoordinates.of(String.format(
        "%s-%s-%s", indexPrefix, feature.getGeometry().getType().toLowerCase(), indexSuffixName));
  }

  IndexCoordinates toPropertiesIndexName(String indexSuffixName, StatisticFeature feature) {
    return IndexCoordinates.of(String.format(
        "%s-%s-%s-%s",
        indexPrefix,
        feature.getGeometry().getType().toLowerCase(),
        indexSuffixName,
        featurePropertiesIndexSuffix));
  }

  private record AuxiliaryDocumentBatchKey(String indexName, Class<?> documentClass) {}
}
