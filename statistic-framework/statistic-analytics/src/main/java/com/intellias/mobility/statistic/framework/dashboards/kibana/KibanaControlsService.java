/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.kibana;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.DashboardService;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.model.ControlGroupInput;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.model.PanelType;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.model.Reference;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationUtil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service for dashboard controls
 */
@Service
public class KibanaControlsService {

  private final DashboardService dashboardService;

  public KibanaControlsService(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  public void createDashboardControllers(
      String dashboardId, String dataViewId, List<String> controllerFieldNames) {
    var controlGroupInput = create(controllerFieldNames, dataViewId);
    var controllerRef = controllerFieldNames.stream()
        .map(v -> new Reference(
            dataViewId,
            PanelType.INDEX_PATTERN,
            "controlGroup_" + VisualizationUtil.generateId() + ":optionsListDataView"))
        .toList();

    dashboardService.addControllers(dashboardId, controlGroupInput, controllerRef);
  }

  private ControlGroupInput create(List<String> fieldNames, String dataViewId) {
    String controlStyle = "HIERARCHICAL";
    String chainingSystem = "oneLine";
    String ignoreParentSettingsJSON =
        "{\"ignoreFilters\":false,\"ignoreQuery\":false,\"ignoreTimerange\":false,\"ignoreValidations\":false}";
    boolean useMargins = false;

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode panelsNode = objectMapper.createObjectNode();

    for (int i = 0; i < fieldNames.size(); i++) {
      String fieldName = fieldNames.get(i);
      String controllerId = VisualizationUtil.generateId();

      ObjectNode sortNode = objectMapper.createObjectNode();
      sortNode.put("by", "_count");
      sortNode.put("direction", "desc");

      ArrayNode selectedOptions = objectMapper.createArrayNode();

      ObjectNode explicitInput = objectMapper.createObjectNode();
      explicitInput.put("id", controllerId);
      explicitInput.put("dataViewId", dataViewId);
      explicitInput.put("fieldName", fieldName);
      explicitInput.putNull("title");
      explicitInput.put("searchTechnique", "prefix");
      explicitInput.putNull("runPastTimeout");
      explicitInput.putNull("singleSelect");
      explicitInput.set("selectedOptions", selectedOptions);
      explicitInput.set("sort", sortNode);
      explicitInput.put("existsSelected", false);
      explicitInput.put("exclude", false);
      explicitInput.putNull("placeholder");
      explicitInput.putNull("hideActionBar");
      explicitInput.putNull("hideExclude");
      explicitInput.putNull("hideExists");
      explicitInput.putNull("hideSort");

      ObjectNode panelConfig = objectMapper.createObjectNode();
      panelConfig.put("grow", false);
      panelConfig.put("order", i);
      panelConfig.put("type", "optionsListControl");
      panelConfig.put("width", "medium");
      panelConfig.set("explicitInput", explicitInput);

      panelsNode.set(controllerId, panelConfig);
    }

    String panelsJson = panelsNode.toString();

    return new ControlGroupInput(
        controlStyle, chainingSystem, ignoreParentSettingsJSON, panelsJson, useMargins);
  }
}
