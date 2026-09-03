/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob;

import com.intellias.mobility.statistic.framework.analytics.diffjob.accumulator.StatisticAccumulator;
import com.intellias.mobility.statistic.framework.analytics.diffjob.elastic.ElasticsearchRepository;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferenceMetadata;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeature;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeatureMetadata;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.FeaturePropertiesMap;
import com.intellias.mobility.statistic.framework.analytics.diffjob.util.DifferenceUtil;
import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.attribute.RangeAttributeValue;
import com.intellias.statistic.model.feature.*;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class StatisticAccumulatorProcessor<T extends StatisticFeature<?>>
    implements ItemProcessor<String, DifferencePerFeature> {
  private final StatisticAccumulator statisticAccumulator;
  private final ElasticsearchRepository elasticsearchRepository;
  private final String indexName;
  private final String sourceVersion;
  private final String targetVersion;
  private final Class<T> featureClass;

  public StatisticAccumulatorProcessor(
      StatisticAccumulator statisticAccumulator,
      ElasticsearchRepository elasticsearchRepository,
      String indexName,
      String sourceVersion,
      String targetVersion) {
    this.statisticAccumulator = statisticAccumulator;
    this.elasticsearchRepository = elasticsearchRepository;
    this.indexName = indexName;
    this.sourceVersion = sourceVersion;
    this.targetVersion = targetVersion;
    this.featureClass = identifyFeatureClass(indexName);
  }

  @Override
  public DifferencePerFeature process(@NonNull String featureSourceId) {
    log.info("Processing - {}", featureSourceId);
    checkAndSetMetadata();

    var sourceFeature = fetchFeature(featureSourceId, sourceVersion);
    var targetFeature = fetchFeature(featureSourceId, targetVersion);

    sourceFeature.ifPresent(feature -> {
      processSourceFeatureProperties(feature, targetFeature);
      processSourceRangeAttributes(feature);
    });
    targetFeature.ifPresent(feature -> {
      processTargetFeatureProperties(feature, sourceFeature);
      processTargetRangeAttributes(feature);
    });

    return sourceFeature
        .flatMap(source -> targetFeature.map(target -> {
          var metadata = new DifferencePerFeatureMetadata(
              indexName, sourceVersion, targetVersion, featureSourceId);
          var differencePerFeature = DifferenceUtil.compareFeatures(source, target, metadata);
          if (!differencePerFeature.equals(new DifferencePerFeature(metadata))) {
            return differencePerFeature;
          } else {
            return null;
          }
        }))
        .orElse(null);
  }

  private void checkAndSetMetadata() {
    statisticAccumulator
        .getMetadata()
        .compareAndSet(null, new DifferenceMetadata(indexName, sourceVersion, targetVersion));
  }

  private Optional<T> fetchFeature(String featureSourceId, String version) {
    return elasticsearchRepository.findFeature(featureSourceId, version, indexName, featureClass);
  }

  private void processSourceFeatureProperties(T feature, Optional<T> targetFeature) {
    if (targetFeature.isEmpty()) {
      statisticAccumulator.getDeletedFeatureIds().add(feature.getFeatureId());
    }
    feature.getProperties().getFeatureProperties().forEach(prop -> statisticAccumulator
        .getSourceFeatureProperties()
        .putValues(prop.getKey(), Set.copyOf(prop.getValues())));
  }

  private void processTargetFeatureProperties(T feature, Optional<T> sourceFeature) {
    if (sourceFeature.isEmpty()) {
      statisticAccumulator.getAddedFeatureIds().add(feature.getFeatureId());
    }
    feature.getProperties().getFeatureProperties().forEach(prop -> statisticAccumulator
        .getTargetFeatureProperties()
        .putValues(prop.getKey(), Set.copyOf(prop.getValues())));
  }

  private void processSourceRangeAttributes(T feature) {
    processRangeAttributes(feature, true);
  }

  private void processTargetRangeAttributes(T feature) {
    processRangeAttributes(feature, false);
  }

  private void processRangeAttributes(T feature, boolean isSource) {
    var accumulator = isSource
        ? statisticAccumulator.getSourceFeatureRangeAttributes()
        : statisticAccumulator.getTargetFeatureRangeAttributes();

    if (feature instanceof LineFeature lineFeature) {
      collectAndStoreRangeAttributes(
          Set.copyOf(lineFeature.getProperties().getRangeAttributes()), accumulator);
    } else if (feature instanceof MultiLineFeature multiLineFeature) {
      collectAndStoreRangeAttributes(
          Set.copyOf(multiLineFeature.getProperties().getRangeAttributes()), accumulator);
    }
  }

  private void collectAndStoreRangeAttributes(
      Set<RangeAttribute> rangeAttributes, FeaturePropertiesMap accumulator) {
    rangeAttributes.stream()
        .collect(Collectors.toMap(RangeAttribute::getKey, t -> t.getValues().stream()
            .map(RangeAttributeValue::getValue)
            .collect(Collectors.toSet())))
        .forEach(accumulator::putValues);
  }

  @SuppressWarnings("unchecked")
  private Class<T> identifyFeatureClass(String indexName) {
    String lowerCaseIndexName = indexName.toLowerCase();

    if (lowerCaseIndexName.contains("geometrycollection")) {
      return (Class<T>) GeometryCollectionFeature.class;
    } else if (lowerCaseIndexName.contains("multipoint")) {
      return (Class<T>) MultiPointFeature.class;
    } else if (lowerCaseIndexName.contains("multilinestring")) {
      return (Class<T>) MultiLineFeature.class;
    } else if (lowerCaseIndexName.contains("multipolygon")) {
      return (Class<T>) MultiPolygonFeature.class;
    } else if (lowerCaseIndexName.contains("point")) {
      return (Class<T>) PointFeature.class;
    } else if (lowerCaseIndexName.contains("linestring")) {
      return (Class<T>) LineFeature.class;
    } else if (lowerCaseIndexName.contains("polygon")) {
      return (Class<T>) PolygonFeature.class;
    } else {
      throw new IllegalStateException("Unexpected value: " + lowerCaseIndexName);
    }
  }
}
