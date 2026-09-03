/**
 Copyright ©2024-2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard.widget;

import static com.intellias.mobility.statistic.framework.ServiceUtils.readJsonFromFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.springframework.data.util.Pair;

public abstract class KibanaWidget {

  protected List<String> indexes;
  protected String field;
  protected ObjectMapper objectMapper;
  // Widget parameters, should be updated for more complex layouts
  protected int width = 24;
  protected int height = 15;

  public KibanaWidget(List<String> indexes, String field, ObjectMapper objectMapper) {
    this.indexes = indexes;
    this.field = field;
    this.objectMapper = objectMapper;
  }

  /*
  Returns two json objects:
  * for map panel
  * for index panel
   */
  public Pair<JsonNode, JsonNode> build(List<String> dataViewUuids, int range) {

    List<String> jsonPaths = getJsonPaths();
    JsonNode mapJson = readJsonFromFile(objectMapper, jsonPaths.getFirst());
    JsonNode mapPanel = mapJson.get("main");
    JsonNode mapReferences = mapJson.get("reference");
    String panelUuid = java.util.UUID.randomUUID().toString();
    List<String> layerUuids = dataViewUuids.stream()
        .map(ignored -> java.util.UUID.randomUUID().toString())
        .toList();

    mapPanel = buildPanel(mapPanel, indexes, dataViewUuids, layerUuids, panelUuid, range);
    mapReferences = buildReference(mapReferences, dataViewUuids, layerUuids, panelUuid);

    if (range > -1) {
      // layout part
      int y = (range / 2) * height;
      int x = (range - (y * 2)) * width;
      ((ObjectNode) mapPanel.path("gridData")).put("y", y);
      ((ObjectNode) mapPanel.path("gridData")).put("x", x);
    }

    return Pair.of(mapPanel, mapReferences);
  }

  public abstract JsonNode buildReference(
      JsonNode reference, List<String> dataViewUuids, List<String> layerUuids, String panelUuid);

  public abstract JsonNode buildPanel(
      JsonNode panel,
      List<String> indexes,
      List<String> dataViewUuids,
      List<String> layerUuids,
      String panelUuid,
      int range);

  public abstract List<String> getJsonPaths();

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KibanaWidget that = (KibanaWidget) o;
    return width == that.width
        && height == that.height
        && Objects.equals(indexes, that.indexes)
        && Objects.equals(field, that.field)
        && Objects.equals(objectMapper, that.objectMapper);
  }

  @Override
  public int hashCode() {
    return Objects.hash(indexes, field, objectMapper, width, height);
  }

  public <T> Stream<T> iteratorToStream(Iterator<T> iter) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false);
  }
}
