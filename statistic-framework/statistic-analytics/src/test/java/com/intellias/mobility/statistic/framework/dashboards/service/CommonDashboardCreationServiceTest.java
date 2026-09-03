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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CommonDashboardCreationServiceTest {

  @Test
  void createCommonDashboardUsesPolygonIndexesForPolygonDataView() {
    Set<String> pointIndexes = Set.of("statistic-point-point");
    Set<String> lineIndexes = Set.of("statistic-linestring-line");
    Set<String> polygonIndexes = Set.of("statistic-polygon-polygon", "statistic-multipolygon-area");
    Set<String> featurePropertiesIndexes = Set.of("statistic-feature-properties");
    Set<String> allIndexes = new LinkedHashSet<>();
    allIndexes.addAll(pointIndexes);
    allIndexes.addAll(lineIndexes);
    allIndexes.addAll(polygonIndexes);

    var indexManager = new RecordingIndexManager(
        pointIndexes, lineIndexes, polygonIndexes, featurePropertiesIndexes, allIndexes);
    var dataViewService = new RecordingDataViewService();
    var dashboardService = new RecordingDashboardService();
    var visualizationService = new RecordingVisualizationService();
    var kibanaControlsService = new RecordingKibanaControlsService();

    var service = new CommonDashboardCreationService(
        indexManager,
        dataViewService,
        dashboardService,
        visualizationService,
        kibanaControlsService);

    service.createCommonDashboard();

    assertEquals(pointIndexes, dataViewService.capturedIndexes.get(0));
    assertEquals(lineIndexes, dataViewService.capturedIndexes.get(1));
    assertEquals(polygonIndexes, dataViewService.capturedIndexes.get(2));
    assertEquals(featurePropertiesIndexes, dataViewService.capturedIndexes.get(3));
    assertEquals(allIndexes, dataViewService.capturedIndexes.get(4));
    assertEquals("Statistic all feature properties", dataViewService.capturedNames.get(3));
    assertEquals("Common Dashboard", dashboardService.requestedDashboardName.getValue());
    assertEquals("Statistic all features-id", kibanaControlsService.capturedDataViewId);
    assertEquals(
        "Statistic all features-id",
        visualizationService.tablePropertiesForTypeParams.get("DATA_VIEW_ID"));
    assertEquals(
        "properties.featureProperties.key.keyword",
        visualizationService.tablePropertiesForTypeParams.get("SOURCE_FIELD_PRIMARY"));
    assertEquals(
        "properties.featureType.keyword",
        visualizationService.tablePropertiesForTypeParams.get("SOURCE_FIELD_SECONDARY"));
    assertEquals(
        "Common Feature Properties Table",
        visualizationService.tablePropertiesForTypeParams.get("PANEL_TITLE"));
    assertEquals(
        "Top 100 Feature Properties",
        visualizationService.tablePropertiesForTypeParams.get("PRIMARY_BUCKET_LABEL"));
    assertEquals(
        "Top 20 Feature Property Values",
        visualizationService.tablePropertiesForTypeParams.get("SECONDARY_BUCKET_LABEL"));
  }

  private static class RecordingIndexManager extends IndexManager {
    private final Set<String> pointIndexes;
    private final Set<String> lineIndexes;
    private final Set<String> polygonIndexes;
    private final Set<String> featurePropertiesIndexes;
    private final Set<String> allIndexes;

    RecordingIndexManager(
        Set<String> pointIndexes,
        Set<String> lineIndexes,
        Set<String> polygonIndexes,
        Set<String> featurePropertiesIndexes,
        Set<String> allIndexes) {
      super(null, null, null, null);
      this.pointIndexes = pointIndexes;
      this.lineIndexes = lineIndexes;
      this.polygonIndexes = polygonIndexes;
      this.featurePropertiesIndexes = featurePropertiesIndexes;
      this.allIndexes = allIndexes;
    }

    @Override
    public Set<String> getPointFeatureIndexList() {
      return pointIndexes;
    }

    @Override
    public Set<String> getLineFeatureIndexList() {
      return lineIndexes;
    }

    @Override
    public Set<String> getPolygonFeatureIndexList() {
      return polygonIndexes;
    }

    @Override
    public Set<String> getAllFeaturePropertiesIndexList() {
      return featurePropertiesIndexes;
    }

    @Override
    public Set<String> getAllFeatureIndexList() {
      return allIndexes;
    }
  }

  private static class RecordingDataViewService extends DataViewService {
    private final List<String> capturedNames = new ArrayList<>();
    private final List<Set<String>> capturedIndexes = new ArrayList<>();

    RecordingDataViewService() {
      super(null, new ObjectMapper());
    }

    @Override
    public String getDataViewIdOrCreate(String name, Set<String> indexes) {
      capturedNames.add(name);
      capturedIndexes.add(indexes);
      return name + "-id";
    }
  }

  private static class RecordingDashboardService extends DashboardService {
    private final Holder<String> requestedDashboardName = new Holder<>();

    RecordingDashboardService() {
      super(null, new ObjectMapper());
    }

    @Override
    public String getDashboardIdOrCreate(String name) {
      requestedDashboardName.setValue(name);
      return "dashboard-id";
    }
  }

  private static class RecordingVisualizationService extends VisualizationService {
    private Map<String, String> tablePropertiesForTypeParams;

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
        tablePropertiesForTypeParams = params;
      }
      return "visualization-id";
    }
  }

  private static class RecordingKibanaControlsService extends KibanaControlsService {
    private String capturedDataViewId;

    RecordingKibanaControlsService() {
      super(null);
    }

    @Override
    public void createDashboardControllers(
        String dashboardId, String dataViewId, List<String> controllerFieldNames) {
      capturedDataViewId = dataViewId;
    }
  }

  private static class Holder<T> {
    private T value;

    T getValue() {
      return value;
    }

    void setValue(T value) {
      this.value = value;
    }
  }
}
