/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.visualization;

import com.intellias.mobility.statistic.framework.ServiceUtils;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class VisualizationUtil {
  public String generateId() {
    return java.util.UUID.randomUUID().toString();
  }

  @SneakyThrows
  public String readFileContent(String path) {
    return new String(
        Objects.requireNonNull(ServiceUtils.class.getResourceAsStream(path)).readAllBytes());
  }
}
