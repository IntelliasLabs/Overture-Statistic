/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.util;

import com.intellias.statistic.model.geometry.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import lombok.experimental.UtilityClass;
import org.locationtech.jts.geom.*;

/**
 * Utility class to convert between StatisticGeometry classes and JTS Geometry.
 */
@UtilityClass
public class JtsGeometryConverter {
  private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

  /**
   * Convert PointGeometry -> JTS Point.
   */
  public static Point toJtsPoint(PointGeometry pg) {
    LonLat lonLat = pg.getLonLat();
    Coordinate coord = new Coordinate(lonLat.getLon(), lonLat.getLat());
    return GEOMETRY_FACTORY.createPoint(coord);
  }

  /**
   * Convert JTS Point -> PointGeometry.
   */
  public static PointGeometry fromJtsPoint(Point point) {
    if (point == null) return null;
    Coordinate c = point.getCoordinate();
    LonLat lonLat = new LonLat(c.x, c.y);
    return new PointGeometry(lonLat);
  }

  /**
   * Convert LineStringGeometry -> JTS LineString.
   */
  public static LineString toJtsLineString(LineGeometry lsg) {
    Coordinate[] coords = lsg.retrievePoints().stream()
        .map(pg -> new Coordinate(pg.getLonLat().getLon(), pg.getLonLat().getLat()))
        .toArray(Coordinate[]::new);
    return GEOMETRY_FACTORY.createLineString(coords);
  }

  /**
   * Convert JTS LineString -> LineStringGeometry.
   */
  public static LineGeometry fromJtsLineString(LineString jtsLine) {
    return new LineGeometry(Arrays.stream(jtsLine.getCoordinates())
        .map(c -> new PointGeometry(new LonLat(c.x, c.y)))
        .toList());
  }

  /**
   * Convert PolygonGeometry -> JTS Polygon.
   */
  public static Polygon toJtsPolygon(PolygonGeometry pg) {
    var outerRing = ringFromCoords(pg.getOuterRing());
    var holes = pg.getInnerRings().stream()
        .map(JtsGeometryConverter::ringFromCoords)
        .toArray(LinearRing[]::new);
    return GEOMETRY_FACTORY.createPolygon(outerRing, holes);
  }

  /**
   * Convert JTS Polygon -> PolygonGeometry.
   */
  public static PolygonGeometry fromJtsPolygon(Polygon polygon) {
    var outer = lineFromLinearRing(polygon.getExteriorRing());
    var holes = IntStream.range(0, polygon.getNumInteriorRing())
        .mapToObj(polygon::getInteriorRingN)
        .map(JtsGeometryConverter::lineFromLinearRing)
        .toList();

    return new PolygonGeometry(outer, holes);
  }

  /**
   * Convert MultiPointGeometry -> JTS MultiPoint.
   */
  public static MultiPoint toJtsMultiPoint(MultiPointGeometry mpg) {
    return GEOMETRY_FACTORY.createMultiPointFromCoords(mpg.getPoints().stream()
        .map(p -> new Coordinate(p.getLon(), p.getLat()))
        .toArray(Coordinate[]::new));
  }

  /**
   * Convert JTS MultiPoint -> MultiPointGeometry.
   */
  public static MultiPointGeometry fromJtsMultiPoint(MultiPoint mp) {
    return new MultiPointGeometry(Arrays.stream(mp.getCoordinates())
        .map(c -> new PointGeometry(c.getX(), c.getY()))
        .toList());
  }

  /**
   * Convert MultiLineGeometry -> JTS MultiLineString.
   */
  public static MultiLineString toJtsMultiLineString(MultiLineGeometry mls) {
    return GEOMETRY_FACTORY.createMultiLineString(mls.retrieveLines().stream()
        .map(JtsGeometryConverter::toJtsLineString)
        .toArray(LineString[]::new));
  }

  /**
   * Convert JTS MultiLineString -> MultiLineGeometry.
   */
  public static MultiLineGeometry fromJtsMultiLineString(MultiLineString jtsMls) {
    return new MultiLineGeometry(IntStream.range(0, jtsMls.getNumGeometries())
        .mapToObj(i -> (LineString) jtsMls.getGeometryN(i))
        .map(JtsGeometryConverter::fromJtsLineString)
        .toList());
  }

  /**
   * Convert MultiPolygonGeometry -> JTS MultiPolygon.
   */
  public static MultiPolygon toJtsMultiPolygon(MultiPolygonGeometry mpg) {
    return GEOMETRY_FACTORY.createMultiPolygon(
        mpg.getPolygons().stream().map(JtsGeometryConverter::toJtsPolygon).toArray(Polygon[]::new));
  }

  /**
   * Convert JTS MultiPolygon -> MultiPolygonGeometry.
   */
  public static MultiPolygonGeometry fromJtsMultiPolygon(MultiPolygon jtsMp) {
    return new MultiPolygonGeometry(IntStream.range(0, jtsMp.getNumGeometries())
        .mapToObj(i -> (Polygon) jtsMp.getGeometryN(i))
        .map(JtsGeometryConverter::fromJtsPolygon)
        .toList());
  }

  /**
   * Convert StatisticGeometryCollection -> JTS GeometryCollection.
   */
  public static GeometryCollection toJtsGeometryCollection(StatisticGeometryCollection gcg) {
    List<Geometry> list = gcg.getGeometries().stream()
        .map(JtsGeometryConverter::toJtsGeometry) // a dispatch method we'll define
        .toList();
    return GEOMETRY_FACTORY.createGeometryCollection(list.toArray(new Geometry[0]));
  }

  /**
   * Convert JTS GeometryCollection -> StatisticGeometryCollection.
   */
  public static StatisticGeometryCollection fromJtsGeometryCollection(GeometryCollection jtsGc) {
    return new StatisticGeometryCollection(IntStream.range(0, jtsGc.getNumGeometries())
        .mapToObj(jtsGc::getGeometryN)
        .map(JtsGeometryConverter::fromJtsGeometry)
        .toList());
  }

  /**
   * StatisticGeometry -> JTS Geometry.
   */
  public static Geometry toJtsGeometry(StatisticGeometry customGeom) {
    if (customGeom == null) return null;

    return switch (customGeom.getType()) {
      case "Point" -> toJtsPoint((PointGeometry) customGeom);
      case "LineString" -> toJtsLineString((LineGeometry) customGeom);
      case "Polygon" -> toJtsPolygon((PolygonGeometry) customGeom);
      case "MultiPoint" -> toJtsMultiPoint((MultiPointGeometry) customGeom);
      case "MultiLineString" -> toJtsMultiLineString((MultiLineGeometry) customGeom);
      case "MultiPolygon" -> toJtsMultiPolygon((MultiPolygonGeometry) customGeom);
      case "GeometryCollection" -> toJtsGeometryCollection(
          (StatisticGeometryCollection) customGeom);
      default -> throw new IllegalStateException(
          "Unexpected geometry type: " + customGeom.getType());
    };
  }

  /**
   * JTS Geometry -> StatisticGeometry.
   */
  public static StatisticGeometry fromJtsGeometry(Geometry jtsGeom) {
    return switch (jtsGeom) {
      case Point point -> fromJtsPoint(point);
      case LineString lineString -> fromJtsLineString(lineString);
      case Polygon polygon -> fromJtsPolygon(polygon);
      case MultiPoint multiPoint -> fromJtsMultiPoint(multiPoint);
      case MultiLineString multiLineString -> fromJtsMultiLineString(multiLineString);
      case MultiPolygon multiPolygon -> fromJtsMultiPolygon(multiPolygon);
      case GeometryCollection geometryCollection ->
      // Could be a single collection or others, but JTS lumps them all in GeometryCollection
      fromJtsGeometryCollection(geometryCollection);
      default -> null;
    };
  }

  /**
   * Helper to make a LinearRing from a LineGeometry.
   */
  public static LinearRing ringFromCoords(LineGeometry coords) {
    return GEOMETRY_FACTORY.createLinearRing(coords.retrievePoints().stream()
        .map(p -> new Coordinate(p.getLon(), p.getLat()))
        .toArray(Coordinate[]::new));
  }

  public static LineGeometry lineFromLinearRing(LinearRing linearRing) {
    return new LineGeometry(Arrays.stream(linearRing.getCoordinates())
        .map(c -> new PointGeometry(new LonLat(c.getX(), c.getY())))
        .toList());
  }
}
