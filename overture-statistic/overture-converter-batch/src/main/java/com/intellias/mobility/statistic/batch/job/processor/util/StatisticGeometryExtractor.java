/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.util;

import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.MultiPolygonGeometry;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.geometry.PolygonGeometry;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

@UtilityClass
public final class StatisticGeometryExtractor {

  public static MultiPolygonGeometry getMultiPolygonGeometry(MultiPolygon multiPolygon) {
    return new MultiPolygonGeometry(IntStream.range(0, multiPolygon.getNumGeometries())
        .mapToObj(multiPolygon::getGeometryN)
        .map(polygon -> getPolygonGeometry((Polygon) polygon))
        .collect(Collectors.toList()));
  }

  public static PolygonGeometry getPolygonGeometry(Polygon polygon) {
    LineGeometry outerRing = getLineGeometry(polygon.getExteriorRing());

    List<LineGeometry> innerRing = IntStream.range(0, polygon.getNumInteriorRing())
        .mapToObj(i -> getLineGeometry(polygon.getInteriorRingN(i)))
        .collect(Collectors.toList());

    return new PolygonGeometry(outerRing, innerRing);
  }

  public static LineGeometry getLineGeometry(LineString ring) {
    return new LineGeometry(Arrays.stream(ring.getCoordinates())
        .map(StatisticGeometryExtractor::getPointGeometry)
        .collect(Collectors.toList()));
  }

  public static PointGeometry getPointGeometry(Point point) {
    return new PointGeometry(point.getCoordinate().x, point.getCoordinate().y);
  }

  private static PointGeometry getPointGeometry(Coordinate coordinate) {
    return new PointGeometry(coordinate.x, coordinate.y);
  }
}
