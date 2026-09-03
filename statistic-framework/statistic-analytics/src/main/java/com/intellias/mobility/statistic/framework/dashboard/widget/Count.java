/**
 Copyright ©2024-2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard.widget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import lombok.SneakyThrows;

public class Count extends KibanaWidget {

  private static final List<String> jsonPath = List.of("/kibana/panel/count.json");

  public Count(List<String> indexes, String field, ObjectMapper objectMapper) {
    super(indexes, field, objectMapper);
  }

  @Override
  public JsonNode buildReference(
      JsonNode reference, List<String> dataViewUuids, List<String> layerUuids, String panelUuid) {

    ((ObjectNode) reference)
        .put("name", panelUuid + ":indexpattern-datasource-layer-" + layerUuids.getFirst());
    ((ObjectNode) reference).put("id", dataViewUuids.getFirst());
    return reference;
  }

  @SneakyThrows
  @Override
  public JsonNode buildPanel(
      JsonNode panel,
      List<String> indexes,
      List<String> dataViewUuids,
      List<String> layerUuids,
      String panelUuid,
      int range) {

    String columnUuid = java.util.UUID.randomUUID().toString();

    String panelAsString = panel.toString();

    var updatedPanel = panelAsString
        .replace("fieldNotUuid", field)
        .replace("panelUuid", panelUuid)
        .replace("dataViewUuid", dataViewUuids.getFirst())
        .replace("layerUuid", layerUuids.getFirst())
        .replace("columnUuid", columnUuid);

    return objectMapper.readTree(updatedPanel);
  }

  @Override
  public List<String> getJsonPaths() {
    return jsonPath;
  }
}
