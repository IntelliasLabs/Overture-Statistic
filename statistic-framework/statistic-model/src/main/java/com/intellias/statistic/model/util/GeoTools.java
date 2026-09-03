/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.util;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;
import org.geotools.referencing.GeodeticCalculator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.linearref.LengthIndexedLine;
import org.locationtech.jts.operation.linemerge.LineMerger;

@UtilityClass
public class GeoTools {

  private static final GeometryFactory geometryFactory = new GeometryFactory();

  public static double calculateLength(Coordinate[] coordinates) {
    return IntStream.range(1, coordinates.length)
        .mapToDouble(i -> calculateDistance(coordinates[i - 1], coordinates[i]))
        .sum();
  }

  public static double calculateDistance(Coordinate aCoord, Coordinate bCoord) {
    GeodeticCalculator gc = new GeodeticCalculator();
    gc.setStartingGeographicPoint(aCoord.x, aCoord.y);
    gc.setDestinationGeographicPoint(bCoord.x, bCoord.y);
    return gc.getOrthodromicDistance();
  }

  public static List<LineString> mergeConnectedLines(List<LineString> lines) {
    LineMerger merger = new LineMerger();

    lines.forEach(merger::add);

    Collection<?> merged = merger.getMergedLineStrings();
    return merged.stream()
        .filter(LineString.class::isInstance)
        .map(LineString.class::cast)
        .collect(Collectors.toList());
  }

  public static LineString getLineStringSegment(
      LineString lineString, double startPercent, double endPercent) {
    double totalLength = lineString.getLength();

    double startLength = totalLength * startPercent;
    double endLength = totalLength * endPercent;

    LengthIndexedLine indexedLine = new LengthIndexedLine(lineString);
    LineString segment = (LineString) indexedLine.extractLine(startLength, endLength);

    return geometryFactory.createLineString(segment.getCoordinates());
  }
}
