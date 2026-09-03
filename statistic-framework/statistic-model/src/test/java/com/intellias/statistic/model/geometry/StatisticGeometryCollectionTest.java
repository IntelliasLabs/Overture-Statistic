/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class StatisticGeometryCollectionTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @SneakyThrows
  @Test
  void testGeometryCollectionConstructor() {
    LonLat luxembourg1 = new LonLat(6.1296, 49.6117); // Center of Luxembourg City
    LonLat luxembourg2 = new LonLat(6.1310, 49.6125);

    PointGeometry point1 = new PointGeometry(luxembourg1);
    PointGeometry point2 = new PointGeometry(luxembourg2);

    StatisticGeometryCollection geometryCollection =
        new StatisticGeometryCollection(Arrays.asList(point1, point2));

    var json = objectMapper.writeValueAsString(geometryCollection);
    var deserialized = objectMapper.readValue(json, StatisticGeometryCollection.class);

    assertEquals(geometryCollection.getGeometries(), deserialized.getGeometries());
    assertEquals(geometryCollection, deserialized);
  }

  @SneakyThrows
  @Test
  void testGeometryCollectionConstructor2() {
    LonLat luxembourg1 = new LonLat(6.1296, 49.6117); // Center of Luxembourg City
    LonLat luxembourg2 = new LonLat(6.1310, 49.6125);

    PointGeometry point1 = new PointGeometry(luxembourg1);
    PointGeometry point2 = new PointGeometry(luxembourg2);

    StatisticGeometryCollection geometryCollection =
        new StatisticGeometryCollection(Arrays.asList(point1, point2));

    var json = objectMapper.writeValueAsString(geometryCollection);
    var deserialized = objectMapper.readValue(json, StatisticGeometry.class);

    assertEquals(geometryCollection, deserialized);
  }
}
