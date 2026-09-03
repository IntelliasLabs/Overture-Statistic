/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.mobility.statistic.framework.range.RangeDocument;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import com.intellias.statistic.model.attribute.Range;
import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.attribute.RangeAttributeValue;
import com.intellias.statistic.model.feature.LineFeature;
import com.intellias.statistic.model.feature.LineFeatureProperties;
import com.intellias.statistic.model.feature.PointFeature;
import com.intellias.statistic.model.feature.PointFeatureProperties;
import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.LonLat;
import com.intellias.statistic.model.geometry.PointGeometry;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class RangeAttributeDocumentMaterializerTest {

  @Test
  void materializeCreatesSummaryAndValueDocumentsForLineFeatures() {
    var materializer = new RangeAttributeDocumentMaterializer(
        new StorageProperties("statistic", "feature-properties", 100));

    var lineFeature = new LineFeature(
        "feature-1",
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.137156353662704, 49.60200863275489)),
            new PointGeometry(new LonLat(6.13764026954567, 49.60122805471545)))),
        LineFeatureProperties.withRangeAttributes(
            "v1",
            "road",
            new Date(),
            List.of(
                new RangeAttribute(
                    "speed",
                    List.of(
                        new RangeAttributeValue("50", List.of(new Range(0.0, 1.0)), 10.0, null)),
                    10.0,
                    null),
                new RangeAttribute(
                    "name",
                    List.of(
                        new RangeAttributeValue("A6", List.of(new Range(0.0, 1.0)), 10.0, null)),
                    10.0,
                    null))));

    var writes = materializer.materialize(lineFeature, "road");

    assertEquals(4, writes.size());
    assertTrue(writes.stream()
        .allMatch(write -> write.indexName().equals("statistic-rangeattribute-linestring-road")));
    assertTrue(writes.stream()
        .map(write -> write.document().getClass())
        .allMatch(RangeDocument.class::equals));
  }

  @Test
  void materializeIsNotApplicableForNonLineFeatures() {
    var materializer = new RangeAttributeDocumentMaterializer(
        new StorageProperties("statistic", "feature-properties", 100));
    var pointFeature = new PointFeature(
        "point-1",
        new PointGeometry(new LonLat(6.1, 49.6)),
        new PointFeatureProperties("v1", "poi", new Date()));

    assertFalse(materializer.isApplicable(pointFeature, "poi"));
  }
}
