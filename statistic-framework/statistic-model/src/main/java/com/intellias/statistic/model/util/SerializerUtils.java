/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.statistic.model.feature.StatisticFeature;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SerializerUtils {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @SneakyThrows
  public static String toJson(Object feature) {
    return MAPPER.writeValueAsString(feature);
  }

  @SneakyThrows
  public static StatisticFeature fromJson(String json) {
    return MAPPER.readValue(json, StatisticFeature.class);
  }

  @SneakyThrows
  public static <T> T fromJson(String json, Class<T> clazz) {
    return MAPPER.readValue(json, clazz);
  }
}
