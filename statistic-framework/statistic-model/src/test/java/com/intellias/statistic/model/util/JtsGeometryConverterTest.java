/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.statistic.model.geometry.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

class JtsGeometryConverterTest {

  @Test
  void testPointConversion() {
    PointGeometry pointGeometry = new PointGeometry(new LonLat(6.13, 49.61));
    Point jtsPoint = JtsGeometryConverter.toJtsPoint(pointGeometry);
    PointGeometry convertedBack = JtsGeometryConverter.fromJtsPoint(jtsPoint);
    assertEquals(pointGeometry, convertedBack);
  }

  @Test
  void testLineStringConversion() {
    LineGeometry lineGeometry = new LineGeometry(List.of(
        new PointGeometry(new LonLat(6.13, 49.61)), new PointGeometry(new LonLat(6.15, 49.62))));
    LineString jtsLine = JtsGeometryConverter.toJtsLineString(lineGeometry);
    LineGeometry convertedBack = JtsGeometryConverter.fromJtsLineString(jtsLine);
    assertEquals(lineGeometry, convertedBack);
  }

  @Test
  void testPolygonConversion() {
    PolygonGeometry polygonGeometry = new PolygonGeometry(
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.13, 49.61)),
            new PointGeometry(new LonLat(6.15, 49.62)),
            new PointGeometry(new LonLat(6.16, 49.60)),
            new PointGeometry(new LonLat(6.13, 49.61)))),
        List.of());
    Polygon jtsPolygon = JtsGeometryConverter.toJtsPolygon(polygonGeometry);
    PolygonGeometry convertedBack = JtsGeometryConverter.fromJtsPolygon(jtsPolygon);
    assertEquals(polygonGeometry, convertedBack);
  }

  @Test
  void testMultiPointConversion() {
    MultiPointGeometry multiPointGeometry = new MultiPointGeometry(List.of(
        new PointGeometry(new LonLat(6.13, 49.61)), new PointGeometry(new LonLat(6.15, 49.62))));
    MultiPoint jtsMultiPoint = JtsGeometryConverter.toJtsMultiPoint(multiPointGeometry);
    MultiPointGeometry convertedBack = JtsGeometryConverter.fromJtsMultiPoint(jtsMultiPoint);
    assertEquals(multiPointGeometry, convertedBack);
  }

  @Test
  void testMultiLineStringConversion() {
    MultiLineGeometry multiLineGeometry = new MultiLineGeometry(List.of(
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.13, 49.61)),
            new PointGeometry(new LonLat(6.15, 49.62)))),
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.16, 49.63)),
            new PointGeometry(new LonLat(6.17, 49.64))))));
    MultiLineString jtsMultiLineString =
        JtsGeometryConverter.toJtsMultiLineString(multiLineGeometry);
    MultiLineGeometry convertedBack =
        JtsGeometryConverter.fromJtsMultiLineString(jtsMultiLineString);
    assertEquals(multiLineGeometry, convertedBack);
  }

  @Test
  void testMultiPolygonConversion() {
    MultiPolygonGeometry multiPolygonGeometry =
        new MultiPolygonGeometry(List.of(new PolygonGeometry(
            new LineGeometry(List.of(
                new PointGeometry(new LonLat(6.13, 49.61)),
                new PointGeometry(new LonLat(6.15, 49.62)),
                new PointGeometry(new LonLat(6.16, 49.60)),
                new PointGeometry(new LonLat(6.13, 49.61)))),
            List.of())));
    MultiPolygon jtsMultiPolygon = JtsGeometryConverter.toJtsMultiPolygon(multiPolygonGeometry);
    MultiPolygonGeometry convertedBack = JtsGeometryConverter.fromJtsMultiPolygon(jtsMultiPolygon);
    assertEquals(multiPolygonGeometry, convertedBack);
  }

  @Test
  void testGeometryCollectionConversion() {
    StatisticGeometryCollection geometryCollection = new StatisticGeometryCollection(List.of(
        new PointGeometry(new LonLat(6.13, 49.61)),
        new LineGeometry(List.of(
            new PointGeometry(new LonLat(6.15, 49.62)),
            new PointGeometry(new LonLat(6.16, 49.60))))));
    GeometryCollection jtsCollection =
        JtsGeometryConverter.toJtsGeometryCollection(geometryCollection);
    StatisticGeometryCollection convertedBack =
        JtsGeometryConverter.fromJtsGeometryCollection(jtsCollection);
    assertEquals(geometryCollection, convertedBack);
  }
}
