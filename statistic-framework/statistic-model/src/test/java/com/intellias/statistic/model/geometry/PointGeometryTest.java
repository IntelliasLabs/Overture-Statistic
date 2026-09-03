/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class PointGeometryTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @SneakyThrows
  @Test
  void testPointGeometryConstructor() {
    LonLat coordinates = new LonLat(10.0, 20.0);
    PointGeometry point = new PointGeometry(coordinates);

    var json = objectMapper.writeValueAsString(point);
    var deserialized = objectMapper.readValue(json, PointGeometry.class);

    assertEquals(point, deserialized);
  }

  @SneakyThrows
  @Test
  void testPointGeometryConstructor2() {
    LonLat coordinates = new LonLat(10.0, 20.0);
    PointGeometry point = new PointGeometry(coordinates);

    var json = objectMapper.writeValueAsString(point);
    var deserialized = objectMapper.readValue(json, StatisticGeometry.class);

    assertEquals(point, deserialized);
  }
}
