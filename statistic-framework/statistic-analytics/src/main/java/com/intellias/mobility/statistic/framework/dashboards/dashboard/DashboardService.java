/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.model.*;
import com.intellias.mobility.statistic.framework.dashboards.kibana.KibanaApiService;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class DashboardService {

  private final KibanaApiService kibanaApiService;
  private final ObjectMapper objectMapper;

  public String getDashboardIdOrCreate(String name) {
    var dashboardIdOptional = getDashboardIdByName(name);

    return dashboardIdOptional.orElseGet(() -> createDashboard(name));
  }

  public void addVisualization(String dashboardId, String visualizationId, PanelType panelType) {
    var dashboard = getDashboardById(dashboardId);

    var panelId = VisualizationUtil.generateId();
    var panelName = String.format("%s:panel_%s", panelId, panelId);
    var reference = new Reference(visualizationId, panelType, panelName);
    var panel = buildPanel(panelId);

    var updatedReferences = new ArrayList<>(dashboard.getReferences());
    updatedReferences.add(reference);
    var updatedPanelsJSON = updatePanelsJSON(dashboard.getAttributes().getPanelsJSON(), panel);

    var updateBody = buildUpdateBody(updatedReferences, updatedPanelsJSON);

    kibanaApiService.makePutRequestToKibanaApi(
        "/saved_objects/dashboard/" + dashboardId, updateBody, Dashboard.class);
  }

  public void addControllers(
      String dashboardId, ControlGroupInput controlGroupInput, List<Reference> references) {
    var dashboard = getDashboardById(dashboardId);
    var oldAttributes = dashboard.getAttributes();

    var newAttributes = new Attributes(
        oldAttributes.getTitle(),
        oldAttributes.getPanelsJSON(),
        oldAttributes.getOptionsJSON(),
        oldAttributes.getKibanaSavedObjectMeta());
    newAttributes.setControlGroupInput(controlGroupInput);

    var updatedReferences = new ArrayList<>(dashboard.getReferences());
    updatedReferences.addAll(references);

    var updateBody = buildUpdateBody(newAttributes, updatedReferences);

    kibanaApiService.makePutRequestToKibanaApi(
        "/saved_objects/dashboard/" + dashboardId, updateBody, Dashboard.class);
  }

  private Map<String, Object> buildUpdateBody(
      Attributes attributes, List<Reference> updatedReferences) {
    return Map.of(
        "attributes", attributes,
        "references", updatedReferences);
  }

  private Map<String, Object> buildUpdateBody(
      List<Reference> updatedReferences, String updatedPanelsJSON) {
    return Map.of(
        "attributes", Map.of("panelsJSON", updatedPanelsJSON), "references", updatedReferences);
  }

  @SneakyThrows
  private String updatePanelsJSON(String oldJson, Panel panel) {
    if (oldJson.isEmpty()) oldJson = "[]";
    List<Panel> panels = objectMapper.readValue(
        oldJson, objectMapper.getTypeFactory().constructCollectionType(List.class, Panel.class));
    panels.add(panel);
    return objectMapper.writeValueAsString(panels);
  }

  private Panel buildPanel(String panelId) {
    var gridData = new GridData(0, 0, 24, 15, panelId);

    return new Panel(
        PanelType.LENS,
        String.format("panel_%s", panelId),
        "embeddableConfig\":{\"enhancements\":{}},\"",
        panelId,
        gridData);
  }

  @SneakyThrows
  private String createDashboard(String name) {
    var attributes = new Attributes(
        name,
        "[]",
        "{\"useMargins\":true,\"syncColors\":false,\"syncCursor\":true,\"syncTooltips\":false,\"hidePanelTitles\":false}",
        new KibanaSavedObjectMeta(
            "{\"query\":{\"query\":\"\",\"language\":\"kuery\"},\"filter\":[]}"));

    var dashboardCreationRequestBody = new Dashboard(attributes, List.of());

    var response = kibanaApiService.makePostRequestToKibanaApi(
        "/saved_objects/dashboard", dashboardCreationRequestBody, String.class);
    return objectMapper.readTree(response).get("id").asText();
  }

  @SneakyThrows
  private Optional<String> getDashboardIdByName(String name) {
    var parameters = Map.of(
        "type", "dashboard",
        "search_fields", "title",
        "search", name);
    var response = kibanaApiService.makeGetRequestToKibanaApi(
        "/saved_objects/_find", parameters, String.class);

    return extractAllIdAndName(response).stream()
        .filter(idWithName -> name.equals(idWithName.title))
        .findFirst()
        .map(idWithName -> idWithName.id);
  }

  @SneakyThrows
  private List<Response> extractAllIdAndName(String response) {
    JsonNode rootNode = objectMapper.readTree(response);
    JsonNode savedObjects = rootNode.path("saved_objects");

    return StreamSupport.stream(savedObjects.spliterator(), false)
        .map(savedObject -> new Response(
            savedObject.get("id").asText(),
            savedObject.get("attributes").get("title").asText()))
        .collect(Collectors.toList());
  }

  private Dashboard getDashboardById(String dashboardId) {
    return kibanaApiService.makeGetRequestToKibanaApi(
        "/saved_objects/dashboard/" + dashboardId, Map.of(), Dashboard.class);
  }

  private record Response(String id, String title) {}
}
