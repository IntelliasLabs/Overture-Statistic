/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard.widget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.List;
import java.util.stream.IntStream;
import lombok.SneakyThrows;

public class MultiGeoMap extends KibanaWidget {

  private static final List<String> jsonPath = List.of("/kibana/panel/multigeomap.json");

  public MultiGeoMap(List<String> indexes, String field, ObjectMapper objectMapper) {
    super(indexes, field, objectMapper);
  }

  @Override
  public JsonNode buildReference(
      JsonNode reference, List<String> dataViewUuids, List<String> layerUuids, String panelUuid) {

    ArrayNode array = objectMapper.createArrayNode();

    IntStream.range(0, dataViewUuids.size()).boxed().forEach(index -> {
      var dataViewUuid = dataViewUuids.get(index);
      var refCopy = reference.deepCopy();
      ((ObjectNode) refCopy)
          .put("name", panelUuid + ":layer_" + (index + 1) + "_source_index_pattern");
      ((ObjectNode) refCopy).put("id", dataViewUuid);
      array.add(refCopy);
    });
    return array;
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

    String vectorTileUuid = java.util.UUID.randomUUID().toString();
    String panelAsString = panel.toString();

    var updatedPanel = panelAsString
        .replace("panelUuid", panelUuid)
        .replace("dataViewUuid", dataViewUuids.getFirst());

    var mapJson = objectMapper.readTree(updatedPanel);

    ArrayNode array = (ArrayNode) objectMapper.readTree(
        mapJson.get("embeddableConfig").get("attributes").get("layerListJSON").asText());

    String mapLayerAsString = iteratorToStream(array.elements())
        .filter(node -> node.get("id").asText().equals("layerMapUuid"))
        .findFirst()
        .orElseThrow()
        .toString();

    JsonNode tileLayer = iteratorToStream(array.elements())
        .filter(node -> !(node.get("id").asText().equals("layerMapUuid")))
        .findFirst()
        .orElseThrow();

    var tileLayerAsString = tileLayer.toString().replace("vectorTileUuid", vectorTileUuid);

    ArrayNode layerListArray = objectMapper.createArrayNode();
    layerListArray.add(objectMapper.readTree(tileLayerAsString));

    IntStream.range(0, dataViewUuids.size()).boxed().forEach(index -> {
      String geoFieldUuid = java.util.UUID.randomUUID().toString();
      String layerMapUuid = java.util.UUID.randomUUID().toString();

      var updatedMapLayer = mapLayerAsString
          .replace("geoFieldUuid", geoFieldUuid)
          .replace("layerMapUuid", layerMapUuid)
          .replace(
              "layer_1_source_index_pattern", ("layer_" + (index + 1) + "_source_index_pattern"));

      try {
        layerListArray.add(objectMapper.readTree(updatedMapLayer));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    });

    ((ObjectNode) mapJson.get("embeddableConfig").get("attributes"))
        .set("layerListJSON", TextNode.valueOf(layerListArray.toString()));

    return mapJson;
  }

  @Override
  public List<String> getJsonPaths() {
    return jsonPath;
  }
}
