/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.util;

import static org.junit.jupiter.api.Assertions.*;

import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.MultiPolygonGeometry;
import com.intellias.statistic.model.geometry.PointGeometry;
import com.intellias.statistic.model.geometry.PolygonGeometry;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

class StatisticGeometryExtractorTest {
  private static final GeometryFactory GF = new GeometryFactory();

  @Test
  void testGetPointGeometry() {
    Coordinate coord = new Coordinate(1.23, 4.56);
    PointGeometry pg = StatisticGeometryExtractor.getPointGeometry(GF.createPoint(coord));

    // assuming PointGeometry has getX() / getY()
    assertEquals(1.23, pg.getLon());
    assertEquals(4.56, pg.getLat());
  }

  @Test
  void testGetLineGeometry() {
    Coordinate[] coords =
        new Coordinate[] {new Coordinate(0, 0), new Coordinate(1, 1), new Coordinate(2, 3)};
    LineString ls = GF.createLineString(coords);

    LineGeometry lg = StatisticGeometryExtractor.getLineGeometry(ls);
    List<PointGeometry> pts = lg.getCoordinates().stream()
        .map(point -> new PointGeometry(point.getFirst(), point.get(1)))
        .toList(); // or getCoordinates()

    assertEquals(3, pts.size());
    assertEquals(new PointGeometry(0, 0), pts.get(0));
    assertEquals(new PointGeometry(1, 1), pts.get(1));
    assertEquals(new PointGeometry(2, 3), pts.get(2));
  }

  @Test
  void testGetPolygonGeometry_NoHoles() {
    // square with no hole
    Coordinate[] outer = new Coordinate[] {
      new Coordinate(0, 0),
      new Coordinate(0, 1),
      new Coordinate(1, 1),
      new Coordinate(1, 0),
      new Coordinate(0, 0)
    };
    Polygon poly = GF.createPolygon(outer);

    PolygonGeometry pg = StatisticGeometryExtractor.getPolygonGeometry(poly);

    // exterior ring
    LineGeometry shell = pg.getOuterRing();
    List<PointGeometry> pts = shell.getCoordinates().stream()
        .map(point -> new PointGeometry(point.getFirst(), point.get(1)))
        .toList();
    assertEquals(5, pts.size());
    assertEquals(new PointGeometry(0, 0), pts.get(0));
    assertEquals(new PointGeometry(0, 1), pts.get(1));
    assertEquals(new PointGeometry(1, 1), pts.get(2));
    assertEquals(new PointGeometry(1, 0), pts.get(3));
    assertEquals(new PointGeometry(0, 0), pts.get(4));

    // no holes
    assertTrue(pg.getInnerRings().isEmpty());
  }

  @Test
  void testGetPolygonGeometry_WithHole() {
    // outer square
    Coordinate[] outer = new Coordinate[] {
      new Coordinate(0, 0),
      new Coordinate(0, 4),
      new Coordinate(4, 4),
      new Coordinate(4, 0),
      new Coordinate(0, 0)
    };
    // hole: smaller square
    Coordinate[] hole = new Coordinate[] {
      new Coordinate(1, 1),
      new Coordinate(1, 2),
      new Coordinate(2, 2),
      new Coordinate(2, 1),
      new Coordinate(1, 1)
    };
    Polygon poly =
        GF.createPolygon(GF.createLinearRing(outer), new LinearRing[] {GF.createLinearRing(hole)});

    PolygonGeometry pg = StatisticGeometryExtractor.getPolygonGeometry(poly);

    // one hole
    var holes = pg.getInnerRings();
    assertEquals(1, holes.size());

    List<PointGeometry> hpts = holes.getFirst().getCoordinates().stream()
        .map(point -> new PointGeometry(point.getFirst(), point.get(1)))
        .toList();
    assertEquals(5, hpts.size());
    assertEquals(new PointGeometry(1, 1), hpts.get(0));
    assertEquals(new PointGeometry(1, 2), hpts.get(1));
    assertEquals(new PointGeometry(2, 2), hpts.get(2));
    assertEquals(new PointGeometry(2, 1), hpts.get(3));
    assertEquals(new PointGeometry(1, 1), hpts.get(4));
  }

  @Test
  void testGetMultiPolygonGeometry() {
    // two simple triangles
    Coordinate[] triA = new Coordinate[] {
      new Coordinate(0, 0), new Coordinate(1, 0), new Coordinate(0, 1), new Coordinate(0, 0)
    };
    Coordinate[] triB = new Coordinate[] {
      new Coordinate(2, 2), new Coordinate(3, 2), new Coordinate(2, 3), new Coordinate(2, 2)
    };
    Polygon pA = GF.createPolygon(triA);
    Polygon pB = GF.createPolygon(triB);
    MultiPolygon mp = GF.createMultiPolygon(new Polygon[] {pA, pB});

    MultiPolygonGeometry mpg = StatisticGeometryExtractor.getMultiPolygonGeometry(mp);
    List<PolygonGeometry> polys = mpg.getPolygons();

    assertEquals(2, polys.size());
    // first polygon
    assertEquals(
        pA.getExteriorRing().getNumPoints(),
        polys.get(0).getOuterRing().getCoordinates().size());
    // second polygon
    assertEquals(
        pB.getExteriorRing().getNumPoints(),
        polys.get(1).getOuterRing().getCoordinates().size());
  }
}
