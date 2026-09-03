/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard.widget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import org.springframework.data.util.Pair;

public class MultiLenghtHistogram extends KibanaWidget {

  private static final List<String> jsonPath = List.of("/kibana/panel/multiLenghtHistogram.json");

  public MultiLenghtHistogram(List<String> indexes, String field, ObjectMapper objectMapper) {
    super(indexes, field, objectMapper);
  }

  @Override
  public JsonNode buildReference(
      JsonNode reference, List<String> dataViewUuids, List<String> layerUuids, String panelUuid) {

    ArrayNode array = objectMapper.createArrayNode();
    var referenceAsString = reference.toString();

    IntStream.range(0, dataViewUuids.size()).boxed().forEach(index -> {
      var newRef = referenceAsString
          .replace("dataViewUuid", dataViewUuids.get(index))
          .replace("panelUuid", panelUuid)
          .replace("layerUuid", layerUuids.get(index));
      try {
        array.add(objectMapper.readTree(newRef));
      } catch (JsonProcessingException ignored) {

      }
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

    String panelAsString = panel.toString();

    var updatedPanel = objectMapper.readTree(panelAsString.replace("panelUuid", panelUuid));

    var referenceAsString =
        updatedPanel.get("embeddableConfig").get("attributes").get("references").toString();

    ArrayNode arrayRef = objectMapper.createArrayNode();

    for (int i = 0; i < dataViewUuids.size(); i++) {
      var newRef = referenceAsString
          .replace("dataViewUuid", dataViewUuids.get(i))
          .replace("layerUuid", layerUuids.get(i));
      try {
        arrayRef.add(objectMapper.readTree(newRef));
      } catch (JsonProcessingException ignored) {
      }
    }

    ((ObjectNode) updatedPanel.get("embeddableConfig").get("attributes"))
        .set("references", arrayRef);

    ArrayNode layersArray = (ArrayNode) updatedPanel
        .get("embeddableConfig")
        .get("attributes")
        .get("state")
        .get("visualization")
        .get("layers");

    var columnXYuuids = IntStream.range(0, dataViewUuids.size())
        .boxed()
        .map(index -> Pair.of(
            java.util.UUID.randomUUID().toString(), java.util.UUID.randomUUID().toString()))
        .toList();

    ArrayNode arrayLayers = objectMapper.createArrayNode();

    String layer0 = (layersArray.get(0).toString())
        .replace("layerUuid0", layerUuids.getFirst())
        .replace("columnUuid1", columnXYuuids.getFirst().getFirst())
        .replace("columnUuid2", columnXYuuids.getFirst().getSecond());
    arrayLayers.add(objectMapper.readTree(layer0));

    String layerN = layersArray.get(1).toString();
    for (int i = 1; i < dataViewUuids.size(); i++) {
      var newLayer = layerN
          .replace("columnUuid1", columnXYuuids.get(i).getFirst())
          .replace("columnUuid2", columnXYuuids.get(i).getSecond())
          .replace("layerUuid", layerUuids.get(i));
      try {
        arrayLayers.add(objectMapper.readTree(newLayer));
      } catch (JsonProcessingException ignored) {
      }
    }

    ((ObjectNode) updatedPanel
            .get("embeddableConfig")
            .get("attributes")
            .get("state")
            .get("visualization"))
        .set("layers", arrayLayers);

    var fullLayerAsString = updatedPanel
        .get("embeddableConfig")
        .get("attributes")
        .get("state")
        .get("datasourceStates")
        .get("formBased")
        .get("layers")
        .get("layerUuid")
        .toString();

    ((ObjectNode) updatedPanel
            .get("embeddableConfig")
            .get("attributes")
            .get("state")
            .get("datasourceStates")
            .get("formBased")
            .get("layers"))
        .remove("layerUuid");

    for (int i = 0; i < dataViewUuids.size(); i++) {
      var updatedFullLayer = fullLayerAsString
          .replace("columnUuid2", columnXYuuids.get(i).getSecond())
          .replace("columnUuid1", columnXYuuids.get(i).getFirst());

      var updatedFullLayerJson = objectMapper.readTree(updatedFullLayer);

      ((ObjectNode) updatedPanel
              .get("embeddableConfig")
              .get("attributes")
              .get("state")
              .get("datasourceStates")
              .get("formBased")
              .get("layers"))
          .set(layerUuids.get(i), updatedFullLayerJson);
    }

    return updatedPanel;
  }

  @Override
  public List<String> getJsonPaths() {
    return jsonPath;
  }
}
