/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.common.IndexManager;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.DashboardService;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.model.PanelType;
import com.intellias.mobility.statistic.framework.dashboards.dataview.DataViewService;
import com.intellias.mobility.statistic.framework.dashboards.kibana.KibanaControlsService;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationService;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FeatureDashboardPropertiesDataViewTest {

  @Test
  void pointsDashboardUsesFeaturePropertiesDataViewForPropertiesTable() {
    var dataViewService = new RecordingDataViewService();
    var visualizationService = new RecordingVisualizationService();
    var service = new PointsDashboardCreationService(
        new RecordingIndexManager(),
        dataViewService,
        new RecordingDashboardService(),
        visualizationService,
        new RecordingKibanaControlsService());

    service.createPointsDashboard();

    assertEquals("Statistic point feature properties", dataViewService.capturedNames.get(1));
    assertEquals(
        "Statistic point feature properties-id",
        visualizationService
            .findPropertiesTableParams("Point Feature Property Values Table")
            .get("DATA_VIEW_ID"));
    assertEquals(
        "key.keyword",
        visualizationService
            .findPropertiesTableParams("Point Feature Property Values Table")
            .get("SOURCE_FIELD_PRIMARY"));
    assertEquals(
        "value.keyword",
        visualizationService
            .findPropertiesTableParams("Point Feature Property Values Table")
            .get("SOURCE_FIELD_SECONDARY"));
    assertEquals(
        "Point Feature Property Values Table",
        visualizationService
            .findPropertiesTableParams("Point Feature Property Values Table")
            .get("PANEL_TITLE"));
  }

  @Test
  void polygonsDashboardUsesFeaturePropertiesDataViewForPropertiesTable() {
    var dataViewService = new RecordingDataViewService();
    var visualizationService = new RecordingVisualizationService();
    var service = new PolygonsDashboardCreationService(
        new RecordingIndexManager(),
        dataViewService,
        new RecordingDashboardService(),
        visualizationService,
        new RecordingKibanaControlsService());

    service.createPolygonsDashboard();

    assertEquals("Statistic polygon feature properties", dataViewService.capturedNames.get(1));
    assertEquals(
        "Statistic polygon feature properties-id",
        visualizationService
            .findPropertiesTableParams("Polygon Feature Property Values Table")
            .get("DATA_VIEW_ID"));
    assertEquals(
        "key.keyword",
        visualizationService
            .findPropertiesTableParams("Polygon Feature Property Values Table")
            .get("SOURCE_FIELD_PRIMARY"));
    assertEquals(
        "value.keyword",
        visualizationService
            .findPropertiesTableParams("Polygon Feature Property Values Table")
            .get("SOURCE_FIELD_SECONDARY"));
    assertEquals(
        "Polygon Feature Property Values Table",
        visualizationService
            .findPropertiesTableParams("Polygon Feature Property Values Table")
            .get("PANEL_TITLE"));
  }

  @Test
  void linesDashboardUsesFeaturePropertiesDataViewForPropertiesTable() {
    var dataViewService = new RecordingDataViewService();
    var visualizationService = new RecordingVisualizationService();
    var service = new LinesDashboardCreationService(
        new RecordingIndexManager(),
        dataViewService,
        new RecordingDashboardService(),
        visualizationService,
        new RecordingKibanaControlsService());

    service.createLinesDashboard();

    assertEquals("Statistic linestring feature properties", dataViewService.capturedNames.get(1));
    assertEquals(
        "Statistic linestring feature properties-id",
        visualizationService
            .findPropertiesTableParams("Line Feature Property Values Table")
            .get("DATA_VIEW_ID"));
    assertEquals(
        "key.keyword",
        visualizationService
            .findPropertiesTableParams("Line Feature Property Values Table")
            .get("SOURCE_FIELD_PRIMARY"));
    assertEquals(
        "value.keyword",
        visualizationService
            .findPropertiesTableParams("Line Feature Property Values Table")
            .get("SOURCE_FIELD_SECONDARY"));
    assertEquals(
        "Line Feature Property Values Table",
        visualizationService
            .findPropertiesTableParams("Line Feature Property Values Table")
            .get("PANEL_TITLE"));
  }

  private static class RecordingIndexManager extends IndexManager {
    RecordingIndexManager() {
      super(null, null, null, null);
    }

    @Override
    public Set<String> getPointFeatureIndexList() {
      return Set.of("statistic-point-point");
    }

    @Override
    public Set<String> getPolygonFeatureIndexList() {
      return Set.of("statistic-polygon-polygon");
    }

    @Override
    public Set<String> getLineFeatureIndexList() {
      return Set.of("statistic-linestring-line");
    }

    @Override
    public Set<String> getAllFeaturePropertiesIndexList() {
      return Set.of("statistic-feature-properties");
    }

    @Override
    public Set<String> getPointFeaturePropertiesIndexList() {
      return Set.of("statistic-point-point-feature-properties");
    }

    @Override
    public Set<String> getPolygonFeaturePropertiesIndexList() {
      return Set.of("statistic-polygon-polygon-feature-properties");
    }

    @Override
    public Set<String> getLineFeaturePropertiesIndexList() {
      return Set.of("statistic-linestring-line-feature-properties");
    }
  }

  private static class RecordingDataViewService extends DataViewService {
    private final List<String> capturedNames = new ArrayList<>();

    RecordingDataViewService() {
      super(null, new ObjectMapper());
    }

    @Override
    public String getDataViewIdOrCreate(String name, Set<String> indexes) {
      capturedNames.add(name);
      return name + "-id";
    }
  }

  private static class RecordingDashboardService extends DashboardService {
    RecordingDashboardService() {
      super(null, new ObjectMapper());
    }

    @Override
    public String getDashboardIdOrCreate(String name) {
      return "dashboard-id";
    }
  }

  private static class RecordingVisualizationService extends VisualizationService {
    private final List<Map<String, String>> tablePropertiesParams = new ArrayList<>();

    RecordingVisualizationService() {
      super(null, null, null, new ObjectMapper());
    }

    @Override
    public String createVisualization(
        VisualizationType type,
        String dashboardId,
        Map<String, String> params,
        PanelType panelType) {
      if (VisualizationType.TABLE_PROPERTIES_FOR_TYPE.equals(type)) {
        tablePropertiesParams.add(params);
      }
      return "visualization-id";
    }

    private Map<String, String> findPropertiesTableParams(String panelTitle) {
      return tablePropertiesParams.stream()
          .filter(params -> panelTitle.equals(params.get("PANEL_TITLE")))
          .findFirst()
          .orElseThrow();
    }
  }

  private static class RecordingKibanaControlsService extends KibanaControlsService {
    RecordingKibanaControlsService() {
      super(null);
    }

    @Override
    public void createDashboardControllers(
        String dashboardId, String dataViewId, List<String> controllerFieldNames) {
      // no-op for service wiring tests
    }
  }
}
