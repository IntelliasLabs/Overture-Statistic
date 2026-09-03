/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.statistic.model.attribute.*;
import com.intellias.statistic.model.feature.*;
import com.intellias.statistic.model.geometry.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for LinePreProcessor.
 */
class LinePreProcessorTest {

  @SuppressWarnings("FieldCanBeLocal")
  private LineGeometry testLineGeometry;

  private LineFeature testLineFeature;

  @BeforeEach
  void setUp() {
    testLineGeometry = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.137156353662704, 49.60200863275489)),
        new PointGeometry(new LonLat(6.13764026954567, 49.60122805471545)),
        new PointGeometry(new LonLat(6.14197400512117, 49.602768290455515)),
        new PointGeometry(new LonLat(6.142608472612494, 49.60118623768196)),
        new PointGeometry(new LonLat(6.142500935748842, 49.600858669680406)),
        new PointGeometry(new LonLat(6.140941651237199, 49.599255645597566))));

    List<RangeAttribute> rangeAttributes = new ArrayList<>();
    rangeAttributes.add(new RangeAttribute(
        "speed",
        List.of(new RangeAttributeValue("50", List.of(new Range(0.0, 0.3), new Range(0.3, 0.6))))));
    rangeAttributes.add(new RangeAttribute(
        "name",
        List.of(
            new RangeAttributeValue("FirstName", List.of(new Range(0.0, 0.3))),
            new RangeAttributeValue("SecondName", List.of(new Range(0.3, 0.6))))));

    testLineFeature = new LineFeature(
        "1",
        testLineGeometry,
        LineFeatureProperties.withRangeAttributes("v1", "line", new Date(), rangeAttributes));
  }

  @Test
  void testIsApplicable() {
    assertTrue(new LinePreProcessor().isApplicable(testLineFeature, "test-index"));
  }

  @Test
  void testProcessEnhanceLength() {
    LineFeature processedFeature = (LineFeature) new LinePreProcessor().process(testLineFeature);
    assertTrue(processedFeature.getProperties().getLengthMeters() > 0);

    processedFeature.getProperties().getRangeAttributes().forEach(ra -> {
      assertNotNull(ra.getGeometry());
      assertTrue(ra.getLengthMeters() > 0);

      ra.getValues().forEach(rav -> {
        assertNotNull(rav.getGeometry());
        assertTrue(rav.getLengthMeters() > 0);
      });
    });
  }

  @Test
  void testEnhanceRange() {
    Range range = new Range(0.0, 0.6);
    LinePreProcessor.enhanceRange(testLineFeature, range);

    assertNotNull(range.getGeometry());
    assertTrue(range.getLengthMeters() > 0);
  }
}
