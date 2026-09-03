/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

@UtilityClass
public class GeoSystemProjector {

  @SneakyThrows
  public static Geometry projectWgsToMetric(Geometry feature) {
    Point centroid = feature.getCentroid();
    MathTransform transform = getTransformWgsToMetric(centroid);
    return JTS.transform(feature, transform);
  }

  @SneakyThrows
  public static MathTransform getTransformWgsToMetric(Point centroid) {
    CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326", true);
    CoordinateReferenceSystem targetCRS = toMetricCRS(centroid);
    return CRS.findMathTransform(sourceCRS, targetCRS, true);
  }

  @SneakyThrows
  public static CoordinateReferenceSystem toMetricCRS(Point centroid) {
    return CRS.decode(toMetricCode(centroid), true);
  }

  public static String toMetricCode(Point centroid) {
    return "AUTO:42001," + centroid.getX() + "," + centroid.getY();
  }
}
