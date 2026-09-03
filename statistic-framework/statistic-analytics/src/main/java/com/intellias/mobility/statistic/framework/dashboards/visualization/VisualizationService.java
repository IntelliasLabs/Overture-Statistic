/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.visualization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.DashboardService;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.model.PanelType;
import com.intellias.mobility.statistic.framework.dashboards.kibana.KibanaApiService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class VisualizationService {

  private final VisualizationBuilder visualizationBuilder;
  private final DashboardService dashboardService;
  private final KibanaApiService kibanaApiService;
  private final ObjectMapper objectMapper;

  /**
   * Creates a visualization from a predefined template and adds it to a dashboard.
   *
   * @param type the type of the visualization, which defines the path to the JSON template.
   * @param dashboardId the ID of the dashboard where the visualization will be added.
   * @param params a map of placeholder values to be replaced in the template.
   *               The keys should match placeholders in the JSON template (e.g., DATA_VIEW_ID, SOURCE_VERSION, TARGET_VERSION).
   *               Required keys depend on the type of visualization:
   *               <ul>
   *                 <li>Standard visualization: DATA_VIEW_ID</li>
   *                 <li>Diff visualization: DATA_VIEW_ID, SOURCE_VERSION, TARGET_VERSION</li>
   *                 <li>Map visualization: DATA_VIEW_ID_FIRST, DATA_VIEW_ID_SECOND, DATA_VIEW_ID_THIRD</li>
   *               </ul>
   * @param panelType the panel type to add to the dashboard (e.g., LENS, MAP)
   * @return the ID of the created visualization
   */
  public String createVisualization(
      VisualizationType type, String dashboardId, Map<String, String> params, PanelType panelType) {
    var visualization = visualizationBuilder.buildVisualization(type, params);
    var visualizationId = saveVisualization(visualization);
    dashboardService.addVisualization(dashboardId, visualizationId, panelType);
    return visualizationId;
  }

  @SneakyThrows
  private String saveVisualization(String visualizationJson) {
    var response = kibanaApiService.makePostRequestToKibanaApi(
        "/saved_objects/_bulk_create", "[" + visualizationJson + "]", String.class);
    return objectMapper
        .readTree(response)
        .path("saved_objects")
        .get(0)
        .path("id")
        .asText();
  }
}
