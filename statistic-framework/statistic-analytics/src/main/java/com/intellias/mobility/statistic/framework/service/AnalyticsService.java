/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.config.AnalyticsProperties;
import com.intellias.mobility.statistic.framework.dashboard.DashboardGenerator;
import com.intellias.mobility.statistic.framework.dashboard.widget.KibanaWidget;
import com.intellias.mobility.statistic.framework.dashboard.widget.WidgetsGenerator;
import com.intellias.mobility.statistic.framework.kibana.KibanaManageClient;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "statistic-app.analytics", value = "index-widgets-json")
@ConditionalOnBean(KibanaManageClient.class)
public class AnalyticsService {

  private final ObjectMapper objectMapper;
  private final AnalyticsProperties appConfig;
  private final DashboardGenerator dashboardGenerator;
  private final WidgetsGenerator widgetsGenerator;

  @SneakyThrows
  public void processJson() {

    // Read JSON from file
    // TODO: implement auto generation of json, if absent
    String jsonContent =
        new String(Files.readAllBytes(Paths.get(appConfig.indexWidgetsJson().orElseThrow())));
    JsonNode jsonNode = objectMapper.readTree(jsonContent);

    String dashboardName = jsonNode.get("name").asText();

    // [index, field, widget]
    var widgetsData = widgetsGenerator.jsonToWidgetsData(jsonNode);

    List<KibanaWidget> widgets = widgetsGenerator.buildWidgets(widgetsData);

    // TODO - enhance service to multi-index work
    var singleIndex = widgetsData.getFirst()[0];

    dashboardGenerator.buildDashboard(singleIndex, dashboardName, widgets);
  }
}
