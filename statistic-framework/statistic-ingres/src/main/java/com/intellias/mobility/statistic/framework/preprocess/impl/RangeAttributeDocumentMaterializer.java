/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess.impl;

import com.intellias.mobility.statistic.framework.preprocess.DerivedDocumentMaterializer;
import com.intellias.mobility.statistic.framework.range.RangeAttributeIndexSupport;
import com.intellias.mobility.statistic.framework.range.RangeDocument;
import com.intellias.mobility.statistic.framework.storage.AuxiliaryDocumentWrite;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import com.intellias.statistic.model.feature.LineFeature;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.util.Pair;

/**
 * Converts enriched line-feature range attributes into dashboard-compatible range documents.
 *
 * <p>The line preprocessors already populate range geometries and propagated lengths. This
 * materializer reuses that enrichment to emit both range-attribute summary documents and
 * range-attribute value documents into the existing {@code rangeattribute-*} indices.</p>
 */
public class RangeAttributeDocumentMaterializer implements DerivedDocumentMaterializer {
  private final String storageIndexPrefix;

  public RangeAttributeDocumentMaterializer(StorageProperties storageProperties) {
    this.storageIndexPrefix = storageProperties.indexPrefix();
  }

  @Override
  public boolean isApplicable(StatisticFeature<?> feature, String indexName) {
    return feature instanceof LineFeature;
  }

  @Override
  public List<AuxiliaryDocumentWrite> materialize(StatisticFeature<?> feature, String indexName) {
    var lineFeature = (LineFeature) feature;
    var destinationIndex =
        RangeAttributeIndexSupport.buildIndexName(storageIndexPrefix, lineFeature, indexName);

    var rangeAttributes = lineFeature.getProperties().getRangeAttributes().stream()
        .map(rangeAttribute -> new RangeDocument(
            rangeAttribute.getKey(),
            lineFeature.getProperties().getVersion(),
            lineFeature.getProperties().getTimestamp(),
            rangeAttribute.getLengthMeters(),
            lineFeature.getFeatureId(),
            lineFeature.getProperties().getFeatureType(),
            rangeAttribute.getGeometry()));

    var rangeAttributeValues = lineFeature.getProperties().getRangeAttributes().stream()
        .flatMap(rangeAttribute -> rangeAttribute.getValues().stream()
            .map(rangeAttributeValue -> Pair.of(rangeAttribute.getKey(), rangeAttributeValue)))
        .map(pair -> new RangeDocument(
            pair.getFirst(),
            pair.getSecond().getValue(),
            lineFeature.getProperties().getVersion(),
            lineFeature.getProperties().getTimestamp(),
            pair.getSecond().getLengthMeters(),
            lineFeature.getFeatureId(),
            lineFeature.getProperties().getFeatureType(),
            pair.getSecond().getGeometry()));

    return Stream.concat(rangeAttributes, rangeAttributeValues)
        .map(document -> new AuxiliaryDocumentWrite(destinationIndex, document))
        .toList();
  }
}
