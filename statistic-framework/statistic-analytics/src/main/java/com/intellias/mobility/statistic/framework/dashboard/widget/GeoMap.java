/**
 Copyright ©2024-2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard.widget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import lombok.SneakyThrows;

/**
 * Represent most beautiful widget in Kibana
 */
public class GeoMap extends KibanaWidget {

  private static final List<String> jsonPath = List.of("/kibana/panel/geomap.json");

  public GeoMap(List<String> indexes, String field, ObjectMapper objectMapper) {
    super(indexes, field, objectMapper);
  }

  @Override
  public JsonNode buildReference(
      JsonNode reference, List<String> dataViewUuids, List<String> layerUuid, String panelUuid) {
    ((ObjectNode) reference).put("name", panelUuid + ":layer_1_source_index_pattern");
    ((ObjectNode) reference).put("id", dataViewUuids.getFirst());
    return reference;
  }

  @SneakyThrows
  @Override
  public JsonNode buildPanel(
      JsonNode panel,
      List<String> indexes,
      List<String> dataViewUuids,
      List<String> layerUuid,
      String panelUuid,
      int range) {
    String vectorTileUuid = java.util.UUID.randomUUID().toString();
    String geometryLayerUuid = java.util.UUID.randomUUID().toString();
    String fieldUuid = java.util.UUID.randomUUID().toString();

    String panelAsString = panel.toString();

    var updatedPanel = panelAsString
        .replace("fieldNotUuid", field)
        .replace("panelUuid", panelUuid)
        .replace("dataViewUuid", dataViewUuids.getFirst())
        .replace("vectorTileUuid", vectorTileUuid)
        .replace("geometryLayerUuid", geometryLayerUuid)
        .replace("fieldUuid", fieldUuid);

    return objectMapper.readTree(updatedPanel);
  }

  @Override
  public List<String> getJsonPaths() {
    return jsonPath;
  }
}
