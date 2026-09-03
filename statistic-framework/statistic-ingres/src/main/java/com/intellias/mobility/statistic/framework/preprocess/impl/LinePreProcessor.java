/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.preprocess.impl;

import static com.intellias.statistic.model.util.GeoTools.getLineStringSegment;
import static com.intellias.statistic.model.util.JtsGeometryConverter.fromJtsLineString;
import static com.intellias.statistic.model.util.JtsGeometryConverter.toJtsLineString;

import com.intellias.mobility.statistic.framework.preprocess.PreProcessor;
import com.intellias.statistic.model.attribute.Range;
import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.attribute.RangeAttributeValue;
import com.intellias.statistic.model.feature.LineFeature;
import com.intellias.statistic.model.feature.StatisticFeature;
import com.intellias.statistic.model.geometry.MultiLineGeometry;
import com.intellias.statistic.model.util.GeoTools;
import com.intellias.statistic.model.util.JtsGeometryConverter;
import java.util.List;
import org.locationtech.jts.geom.LineString;
import org.springframework.data.util.Pair;

/**
 * Enriches {@link LineFeature} and its range attributes with length information.
 */
public class LinePreProcessor implements PreProcessor {
  @Override
  public boolean isApplicable(StatisticFeature<?> feature, String indexName) {
    return feature instanceof LineFeature;
  }

  @Override
  public StatisticFeature<?> process(StatisticFeature<?> feature) {
    return switch (feature) {
      case LineFeature line -> enhance(line);
      default -> throw new IllegalStateException("Unexpected feature: " + feature);
    };
  }

  /**
   * Calculates feature length and propagates it to all range attributes.
   */
  static LineFeature enhance(LineFeature line) {
    if (line.getProperties().getLengthMeters() <= 0.0) {
      line.getProperties()
          .setLengthMeters(GeoTools.calculateLength(
              JtsGeometryConverter.toJtsLineString(line.getGeometry()).getCoordinates()));
    }

    enhanceRanges(line);
    return line;
  }

  /** Updates all ranges and attributes with actual geometry and length. */
  static void enhanceRanges(LineFeature line) {
    line.getProperties().getRangeAttributes().stream()
        .flatMap(ra -> ra.getValues().stream().flatMap(rav -> rav.getRanges().stream()))
        .forEach(range -> enhanceRange(line, range));

    // propagate to RangeAttributeValue
    line.getProperties().getRangeAttributes().stream()
        .flatMap(ra -> ra.getValues().stream())
        .forEach(LinePreProcessor::enhanceRangeAttributeValue);

    // propagate to RangeAttribute
    line.getProperties().getRangeAttributes().forEach(LinePreProcessor::enhanceRangeAttribute);
  }

  /** Updates geometry and length of a single {@link RangeAttribute}. */
  private static void enhanceRangeAttribute(RangeAttribute ra) {
    // convert to JTS LineString list
    List<LineString> lineStrings = ra.getValues().stream()
        .flatMap(rav -> rav.getGeometry().retrieveLines().stream())
        .map(JtsGeometryConverter::toJtsLineString)
        .toList();

    var geoAndLength = propagateGeoAndLength(lineStrings);

    ra.setGeometry(geoAndLength.getFirst());
    ra.setLengthMeters(geoAndLength.getSecond());
  }

  /** Updates geometry and length of a {@link RangeAttributeValue}. */
  private static void enhanceRangeAttributeValue(RangeAttributeValue rangeAttributeValue) {
    // convert to JTS LineString list
    List<LineString> lineStrings = rangeAttributeValue.getRanges().stream()
        .map(Range::getGeometry)
        .map(JtsGeometryConverter::toJtsLineString)
        .toList();

    var geoAndLength = propagateGeoAndLength(lineStrings);

    rangeAttributeValue.setGeometry(geoAndLength.getFirst());
    rangeAttributeValue.setLengthMeters(geoAndLength.getSecond());
  }

  /** Merges provided lines and calculates their total length. */
  static Pair<MultiLineGeometry, Double> propagateGeoAndLength(List<LineString> lineStrings) {
    // merge lineStrings
    var mergedLineStrings = GeoTools.mergeConnectedLines(lineStrings);

    // calculate length based on merged lines
    var lengthMeters = calculateLength(mergedLineStrings);

    return Pair.of(toMultilineGeometry(mergedLineStrings), lengthMeters);
  }

  /** Converts merged JTS line strings to a {@link MultiLineGeometry}. */
  static MultiLineGeometry toMultilineGeometry(List<LineString> mergedLineStrings) {
    return new MultiLineGeometry(
        mergedLineStrings.stream().map(JtsGeometryConverter::fromJtsLineString).toList());
  }

  /** Sums up lengths of all merged line strings. */
  static Double calculateLength(List<LineString> mergedLineStrings) {
    return mergedLineStrings.stream()
        .map(ls -> GeoTools.calculateLength(ls.getCoordinates()))
        .reduce(0.0, Double::sum);
  }

  /** Updates geometry and length for the given {@link Range}. */
  static void enhanceRange(LineFeature line, Range range) {
    var rangeLine = fromJtsLineString(getLineStringSegment(
        toJtsLineString(line.getGeometry()), range.getStart(), range.getEnd()));
    range.setGeometry(rangeLine);
    range.setLengthMeters(
        GeoTools.calculateLength(JtsGeometryConverter.toJtsLineString(rangeLine).getCoordinates()));
  }
}
