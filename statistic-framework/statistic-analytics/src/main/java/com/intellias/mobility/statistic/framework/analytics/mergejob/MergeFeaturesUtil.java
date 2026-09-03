/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.mergejob;

import static com.intellias.statistic.model.util.JtsGeometryConverter.*;

import com.intellias.statistic.model.attribute.RangeAttribute;
import com.intellias.statistic.model.feature.*;
import com.intellias.statistic.model.geometry.MultiLineGeometry;
import com.intellias.statistic.model.geometry.MultiPolygonGeometry;
import com.intellias.statistic.model.util.JtsGeometryConverter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.PolygonArea;
import net.sf.geographiclib.PolygonResult;
import org.locationtech.jts.geom.*;

@UtilityClass
public class MergeFeaturesUtil {
  private final GeometryFactory geometryFactory = new GeometryFactory();

  // methods to merge different features
  // MultiPolygonFeature + MultiPolygonFeature
  // MultiPolygonFeature + PolygonFeature
  // PolygonFeature + PolygonFeature
  // MultiLineFeature + MultiLineFeature
  // MultiLineFeature + LineFeature
  // LineFeature + LineFeature

  public static MultiPolygonFeature mergeMultiPolygonFeatures(
      MultiPolygonFeature baseFeature, MultiPolygonFeature nextFeature) {
    var featureId = mergeId(baseFeature.getFeatureId(), nextFeature.getFeatureId());

    var mergedProperties = mergeFeatureProperties(
        baseFeature.getProperties().getFeatureProperties(),
        nextFeature.getProperties().getFeatureProperties());

    var polygonProperties =
        mergePolygonFeatureProperties(baseFeature.getProperties(), mergedProperties);

    var mergedGeometry = toJtsMultiPolygon(baseFeature.getGeometry())
        .union(toJtsMultiPolygon(nextFeature.getGeometry()));

    MultiPolygon multiGeom = convertToMultiPolygon(mergedGeometry);

    polygonProperties.setArea(calculateGeometryArea(multiGeom));

    return MultiPolygonFeature.builder()
        .featureId(featureId)
        .properties(polygonProperties)
        .geometry(JtsGeometryConverter.fromJtsMultiPolygon(multiGeom))
        .build();
  }

  public static MultiPolygonFeature mergePolygonFeatures(
      PolygonFeature baseFeature, PolygonFeature nextFeature) {
    var featureId = mergeId(baseFeature.getFeatureId(), nextFeature.getFeatureId());

    var mergedProperties = mergeFeatureProperties(
        baseFeature.getProperties().getFeatureProperties(),
        nextFeature.getProperties().getFeatureProperties());

    var polygonProperties =
        mergePolygonFeatureProperties(baseFeature.getProperties(), mergedProperties);

    var mergedGeometry =
        toJtsPolygon(baseFeature.getGeometry()).union(toJtsPolygon(nextFeature.getGeometry()));

    MultiPolygon multiGeom = convertToMultiPolygon(mergedGeometry);

    polygonProperties.setArea(calculateGeometryArea(multiGeom));

    return MultiPolygonFeature.builder()
        .featureId(featureId)
        .properties(polygonProperties)
        .geometry(JtsGeometryConverter.fromJtsMultiPolygon(multiGeom))
        .build();
  }

  public static MultiPolygonFeature mergeMultiPolygonWithPolygonFeatures(
      MultiPolygonFeature baseFeature, PolygonFeature nextFeature) {

    var convertedPolygonFeature = MultiPolygonFeature.builder()
        .featureId(nextFeature.getFeatureId())
        .properties(nextFeature.getProperties())
        .geometry(new MultiPolygonGeometry(List.of(nextFeature.getGeometry())))
        .build();

    return mergeMultiPolygonFeatures(baseFeature, convertedPolygonFeature);
  }

  public static MultiLineFeature mergeMultiLineWithLineFeature(
      MultiLineFeature baseFeature, LineFeature nextFeature) {

    var convertedMultiLineFeature = MultiLineFeature.builder()
        .featureId(nextFeature.getFeatureId())
        .properties(nextFeature.getProperties())
        .geometry(new MultiLineGeometry(List.of(nextFeature.getGeometry())))
        .build();

    return mergeMultiLineFeatures(baseFeature, convertedMultiLineFeature);
  }

  public static MultiLineFeature mergeMultiLineFeatures(
      MultiLineFeature baseFeature, MultiLineFeature nextFeature) {
    var featureId = mergeId(baseFeature.getFeatureId(), nextFeature.getFeatureId());

    var mergedProperties = mergeFeatureProperties(
        baseFeature.getProperties().getFeatureProperties(),
        nextFeature.getProperties().getFeatureProperties());

    var multiProperties = mergeLineProperties(baseFeature.getProperties(), mergedProperties);

    multiProperties.setRangeAttributes(mergeRangeAttributes(
        baseFeature.getProperties().getRangeAttributes(),
        nextFeature.getProperties().getRangeAttributes()));

    MultiLineString multiLineString = mergeMultiLineStrings(List.of(
        toJtsMultiLineString(baseFeature.getGeometry()),
        toJtsMultiLineString(nextFeature.getGeometry())));

    var multiLineGeom = JtsGeometryConverter.fromJtsMultiLineString(multiLineString);

    return MultiLineFeature.builder()
        .featureId(featureId)
        .properties(multiProperties)
        .geometry(multiLineGeom)
        .build();
  }

  public static MultiLineFeature mergeLineFeatures(
      LineFeature baseFeature, LineFeature nextFeature) {
    var featureId = mergeId(baseFeature.getFeatureId(), nextFeature.getFeatureId());

    var mergedProperties = mergeFeatureProperties(
        baseFeature.getProperties().getFeatureProperties(),
        nextFeature.getProperties().getFeatureProperties());

    var multiProperties = mergeLineProperties(baseFeature.getProperties(), mergedProperties);

    multiProperties.setRangeAttributes(mergeRangeAttributes(
        baseFeature.getProperties().getRangeAttributes(),
        nextFeature.getProperties().getRangeAttributes()));

    var lineStrings = List.of(
        toJtsLineString(baseFeature.getGeometry()), toJtsLineString(nextFeature.getGeometry()));

    var multiLineGeom = new MultiLineGeometry(lineStrings.stream()
        .map(JtsGeometryConverter::fromJtsLineString)
        .collect(Collectors.toList()));

    return MultiLineFeature.builder()
        .featureId(featureId)
        .properties(multiProperties)
        .geometry(multiLineGeom)
        .build();
  }

  private static String mergeId(String baseId, String nextId) {
    // new id is first of sorted
    return Stream.of(baseId, nextId).sorted().findFirst().orElse(baseId);
  }

  private static PolygonFeatureProperties mergePolygonFeatureProperties(
      PolygonFeatureProperties polygonFeatureProperties, List<FeatureProperty> featureProperties) {
    var properties = new PolygonFeatureProperties(
        polygonFeatureProperties.getVersion(),
        polygonFeatureProperties.getFeatureType(),
        getDate(polygonFeatureProperties.getTimestamp()),
        featureProperties);
    properties.setGlobalSourceId(polygonFeatureProperties.getGlobalSourceId());
    return properties;
  }

  private static LineFeatureProperties mergeLineProperties(
      LineFeatureProperties lineFeatureProperties, List<FeatureProperty> featureProperties) {
    var properties = new LineFeatureProperties(
        lineFeatureProperties.getVersion(),
        lineFeatureProperties.getFeatureType(),
        getDate(lineFeatureProperties.getTimestamp()),
        featureProperties);
    properties.setGlobalSourceId(lineFeatureProperties.getGlobalSourceId());
    return properties;
  }

  private static MultiLineString mergeMultiLineStrings(List<MultiLineString> lineStrings) {
    List<LineString> allLineStrings = new ArrayList<>();

    for (MultiLineString multiLineString : lineStrings) {
      for (int i = 0; i < multiLineString.getNumGeometries(); i++) {
        allLineStrings.add((LineString) multiLineString.getGeometryN(i));
      }
    }

    return geometryFactory.createMultiLineString(allLineStrings.toArray(new LineString[0]));
  }

  private static MultiPolygon convertToMultiPolygon(Geometry geometry) {
    MultiPolygon multiGeom;
    if (geometry instanceof MultiPolygon) {
      multiGeom = (MultiPolygon) geometry;
    } else if (geometry instanceof Polygon) {
      multiGeom = geometryFactory.createMultiPolygon(new Polygon[] {(Polygon) geometry});
    } else {
      throw new IllegalArgumentException(
          "Unsupported geometry type: " + geometry.getClass().getName());
    }
    return multiGeom;
  }

  private static List<FeatureProperty> mergeFeatureProperties(
      List<FeatureProperty> baseFeatureProperties, List<FeatureProperty> nextFeatureProperties) {

    Map<String, Set<String>> mergedMap = new HashMap<>();

    for (FeatureProperty property : baseFeatureProperties) {
      mergedMap
          .computeIfAbsent(property.getKey(), k -> new HashSet<>())
          .addAll(property.getValues());
    }

    for (FeatureProperty property : nextFeatureProperties) {
      mergedMap
          .computeIfAbsent(property.getKey(), k -> new HashSet<>())
          .addAll(property.getValues());
    }

    return mergedMap.entrySet().stream()
        .map(entry -> new FeatureProperty(entry.getKey(), new ArrayList<>(entry.getValue())))
        .collect(Collectors.toList());
  }

  private static List<RangeAttribute> mergeRangeAttributes(
      List<RangeAttribute> baseAttributes, List<RangeAttribute> nextAttributes) {
    return Stream.of(baseAttributes, nextAttributes)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private static Date getDate(String timestamp) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestamp, formatter);
    return Date.from(zonedDateTime.toInstant());
  }

  public static double calculateGeometryArea(Geometry geometry) {
    if (geometry instanceof MultiPolygon multiPolygon) {
      return IntStream.range(0, multiPolygon.getNumGeometries())
          .mapToObj(multiPolygon::getGeometryN)
          .map(Polygon.class::cast)
          .mapToDouble(MergeFeaturesUtil::calculatePolygonArea)
          .sum();
    }

    return calculatePolygonArea((Polygon) geometry);
  }

  private static double calculatePolygonArea(Polygon polygon) {
    double area = calculateRingArea(polygon.getExteriorRing().getCoordinates());

    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      area -= calculateRingArea(polygon.getInteriorRingN(i).getCoordinates());
    }

    return area;
  }

  private static double calculateRingArea(Coordinate[] coordinates) {
    PolygonArea polygonArea = new PolygonArea(Geodesic.WGS84, false);
    for (Coordinate coordinate : coordinates) {
      polygonArea.AddPoint(coordinate.y, coordinate.x); // lat, lon
    }
    PolygonResult result = polygonArea.Compute(false, true);
    return Math.abs(result.area);
  }
}
