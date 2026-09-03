/**
 Copyright ©2024-2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.dashboard.widget.KibanaWidget;
import com.intellias.mobility.statistic.framework.dashboard.widget.WidgetsGenerator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

  private final ObjectMapper objectMapper;
  private final DashboardGenerator dashboardGenerator;
  private final WidgetsGenerator widgetsGenerator;

  @SneakyThrows
  @RequestMapping(
      path = "/create",
      method = {RequestMethod.GET},
      produces = "application/json")
  @ResponseBody
  public String createDashboard(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      return "File is empty!";
    }

    JsonNode jsonNode = objectMapper.readTree(file.getInputStream());
    String dashboardName = jsonNode.get("name").toString();

    // [index, field, widget]
    var widgetsData = widgetsGenerator.jsonToWidgetsData(jsonNode);

    List<KibanaWidget> widgets = widgetsGenerator.buildWidgets(widgetsData);

    // TODO - enhance service to multi-index work
    var singleIndex = widgetsData.getFirst()[0];

    return dashboardGenerator.buildDashboard(singleIndex, dashboardName, widgets);
  }

  @SneakyThrows
  @RequestMapping(
      path = "/generate",
      method = {RequestMethod.GET},
      produces = "application/json")
  @ResponseBody
  public String generateRangedDashboard() {
    return dashboardGenerator.generateDashboard("Dashboard for ranged features");
  }
}
