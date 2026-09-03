/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import lombok.SneakyThrows;

public class ServiceUtils {

  @SneakyThrows
  public static JsonNode readJsonFromFile(ObjectMapper objectMapper, String path) {

    String dataJson = new String(
        Objects.requireNonNull(ServiceUtils.class.getResourceAsStream(path)).readAllBytes());
    return objectMapper.readTree(dataJson);
  }
}
