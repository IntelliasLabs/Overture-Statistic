/**
 Copyright ©2024-2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard;

import static com.intellias.mobility.statistic.framework.range.RangeAttributeIndexSupport.RANGE_ATTR_INDEX_PREFIX;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellias.mobility.statistic.framework.ServiceUtils;
import com.intellias.mobility.statistic.framework.common.IndexManager;
import com.intellias.mobility.statistic.framework.dashboard.widget.KibanaWidget;
import com.intellias.mobility.statistic.framework.kibana.KibanaManager;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DashboardGenerator {

  private final KibanaManager kibanaManager;
  private final IndexManager indexManager;
  private final StorageProperties storageProperties;
  private final ObjectMapper objectMapper;
  private final List<KibanaWidget> widgetsForGeneratedDashboard;

  @SneakyThrows
  public String buildDashboard(String index, String dashboardName, List<KibanaWidget> widgets) {

    // 1. Create data view from indice
    String dataViewUuid = java.util.UUID.randomUUID().toString();
    var firstResponseString = createDataView(index, dataViewUuid);

    // 2. Create dashboard
    String dashboardDescription = dashboardName + " description"; // in future may be used
    var secondResponseString = createEmptyDashboard(dashboardName, dashboardDescription);

    String dashBoardUuid = objectMapper
        .readTree(secondResponseString)
        .get("result")
        .get("result")
        .get("item")
        .get("id")
        .asText();

    // 3. Fill dashboard by widgets

    var thirdResponseString = fillDashBoardByWidgets(
        dashboardName, widgets, dataViewUuid, dashBoardUuid, dashboardDescription);

    return "{\"answers\":["
        + firstResponseString
        + ","
        + secondResponseString
        + ","
        + thirdResponseString
        + "]}";
  }

  @SneakyThrows
  private String fillDashBoardByWidgets(
      String dashboardName,
      List<KibanaWidget> widgets,
      String dataViewUuid,
      String dashBoardUuid,
      String dashboardDescription) {
    List<Pair<JsonNode, JsonNode>> widgetsData = IntStream.range(0, widgets.size())
        .boxed()
        .map(indexI -> widgets.get(indexI).build(List.of(dataViewUuid), indexI))
        .toList();

    String panels = "["
        + String.join(
            ",",
            widgetsData.stream().map(Pair::getFirst).map(JsonNode::toString).toList())
        + "]";

    JsonNode filledDashboardJson =
        ServiceUtils.readJsonFromFile(objectMapper, "/kibana/filledDashboard.json");

    ArrayNode references = objectMapper.createArrayNode();
    for (Pair<JsonNode, JsonNode> widgetsDatum : widgetsData) {
      references.add(widgetsDatum.getSecond());
    }

    // when all dashboard done
    ((ObjectNode) filledDashboardJson).put("id", dashBoardUuid);
    ((ObjectNode) filledDashboardJson.path("data")).put("description", dashboardDescription);
    ((ObjectNode) filledDashboardJson.path("data")).put("panelsJSON", panels);
    ((ObjectNode) filledDashboardJson.path("data")).put("title", dashboardName);
    ((ObjectNode) filledDashboardJson.path("options")).set("references", references);

    return kibanaManager.updateAndReturnString(filledDashboardJson.toString());
  }

  @SneakyThrows
  private String createEmptyDashboard(String dashboardName, String dashboardDescription) {

    JsonNode startDashboardJson =
        ServiceUtils.readJsonFromFile(objectMapper, "/kibana/newDashboard.json");
    ((ObjectNode) startDashboardJson.path("data")).put("description", dashboardDescription);
    ((ObjectNode) startDashboardJson.path("data")).put("title", dashboardName);

    return kibanaManager.createAndReturnString(startDashboardJson.toString());
  }

  @SneakyThrows
  private String createDataView(String index, String dataViewUuid) {
    String dataViewName = index + " data view";
    JsonNode dataViewJson = ServiceUtils.readJsonFromFile(objectMapper, "/kibana/newDataView.json");

    ((ObjectNode) dataViewJson.path("data")).put("title", index);
    ((ObjectNode) dataViewJson.path("data")).put("name", dataViewName);
    ((ObjectNode) dataViewJson.path("options")).put("id", dataViewUuid);

    return kibanaManager.createAndReturnString(dataViewJson.toString());
  }

  @SneakyThrows
  public String generateDashboard(String dashboardForRangedFeatures) {

    var indexPattern = storageProperties.indexPrefix() + "-" + RANGE_ATTR_INDEX_PREFIX + "-*";
    List<String> indexList = indexManager.getIndexList(indexPattern);

    if (indexList.isEmpty() || (indexList.size() == 1 && indexList.getFirst().isEmpty())) {
      return "{\"error\":\"No index found\"}";
    }

    var dataViewUuidAndJson = indexList.stream()
        .map(indexName -> {
          String dataViewUuid = UUID.randomUUID().toString();
          var answer = createDataView(indexName, dataViewUuid);
          return Pair.of(dataViewUuid, answer);
        })
        .toList();

    String dashboardDescription = dashboardForRangedFeatures + " description";
    var dashboardResponseString =
        createEmptyDashboard(dashboardForRangedFeatures, dashboardDescription);

    String dashBoardUuid = objectMapper
        .readTree(dashboardResponseString)
        .get("result")
        .get("result")
        .get("item")
        .get("id")
        .asText();

    var dataViewUuids = dataViewUuidAndJson.stream().map(Pair::getFirst).toList();

    List<Pair<JsonNode, JsonNode>> widgetsData = widgetsForGeneratedDashboard.stream()
        .map(kibanaWidget -> kibanaWidget.build(dataViewUuids, -1))
        .toList();

    String panels = "["
        + String.join(
            ",",
            widgetsData.stream().map(Pair::getFirst).map(JsonNode::toString).toList())
        + "]";

    JsonNode filledDashboardJson =
        ServiceUtils.readJsonFromFile(objectMapper, "/kibana/filledDashboard.json");

    ArrayNode references = objectMapper.createArrayNode();
    for (Pair<JsonNode, JsonNode> widgetsDatum : widgetsData) {
      for (JsonNode jsonNode : widgetsDatum.getSecond()) {
        references.add(jsonNode);
      }
    }

    // when all dashboard done
    ((ObjectNode) filledDashboardJson).put("id", dashBoardUuid);
    ((ObjectNode) filledDashboardJson.path("data")).put("description", dashboardDescription);
    ((ObjectNode) filledDashboardJson.path("data")).put("panelsJSON", panels);
    ((ObjectNode) filledDashboardJson.path("data")).put("title", dashboardForRangedFeatures);
    ((ObjectNode) filledDashboardJson.path("options")).set("references", references);

    return kibanaManager.updateAndReturnString(filledDashboardJson.toString());
  }
}
