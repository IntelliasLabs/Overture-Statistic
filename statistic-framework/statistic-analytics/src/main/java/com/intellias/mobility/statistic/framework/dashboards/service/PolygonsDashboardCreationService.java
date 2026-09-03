/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.service;

import com.intellias.mobility.statistic.framework.common.IndexManager;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.DashboardService;
import com.intellias.mobility.statistic.framework.dashboards.dataview.DataViewService;
import com.intellias.mobility.statistic.framework.dashboards.kibana.KibanaControlsService;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class PolygonsDashboardCreationService {
  private static final String DASHBOARD_NAME = "Polygons Dashboard";

  private static final String FEATURE_TYPE_FILED = "properties.featureType.keyword";
  private static final String VERSION_FILED = "properties.version.keyword";
  private static final String FEATURE_PROPERTIES_KEY_FILED =
      "properties.featureProperties.key.keyword";
  private static final String FEATURE_PROPERTIES_VALUES_FILED =
      "properties.featureProperties.values.keyword";

  private static final List<String> CONTROLLER_FIELDS = List.of(FEATURE_TYPE_FILED, VERSION_FILED);

  private static final String STATISTIC_PREFIX = "Statistic";

  private static final String POLYGON_FEATURE_PROPERTIES_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " polygon feature properties";
  private static final String POLYGONS_FEATURES = STATISTIC_PREFIX + " polygons features";

  private final DataViewService dataViewService;
  private final DashboardService dashboardService;
  private final VisualizationService visualizationService;
  private final KibanaControlsService kibanaControlsService;

  private final Map<String, Supplier<Set<String>>> dataViewIndexSuppliers;

  public PolygonsDashboardCreationService(
      IndexManager indexManager,
      DataViewService dataViewService,
      DashboardService dashboardService,
      VisualizationService visualizationService,
      KibanaControlsService kibanaControlsService) {
    this.dashboardService = dashboardService;
    this.dataViewService = dataViewService;
    this.visualizationService = visualizationService;
    this.kibanaControlsService = kibanaControlsService;

    this.dataViewIndexSuppliers = Map.of(
        POLYGONS_FEATURES,
        indexManager::getPolygonFeatureIndexList,
        POLYGON_FEATURE_PROPERTIES_DATA_VIEW_NAME,
        indexManager::getPolygonFeaturePropertiesIndexList);
  }

  public String createPolygonsDashboard() {
    var polygonsDataViewName = POLYGONS_FEATURES;
    var polygonsIndex = dataViewIndexSuppliers.get(polygonsDataViewName).get();
    var featurePropertiesDataViewName = POLYGON_FEATURE_PROPERTIES_DATA_VIEW_NAME;
    var featurePropertiesIndex =
        dataViewIndexSuppliers.get(featurePropertiesDataViewName).get();
    if (polygonsIndex.isEmpty()) {
      return String.format(
          "There are no indexes for %s. Dashboard was not created!", polygonsIndex);
    }
    if (featurePropertiesIndex.isEmpty()) {
      return String.format(
          "There are no indexes for %s. Dashboard was not created!", featurePropertiesDataViewName);
    }

    var dataViewIdPolygons =
        dataViewService.getDataViewIdOrCreate(polygonsDataViewName, polygonsIndex);
    var dataViewIdFeatureProperties = dataViewService.getDataViewIdOrCreate(
        featurePropertiesDataViewName, featurePropertiesIndex);

    var dashboardId = dashboardService.getDashboardIdOrCreate(DASHBOARD_NAME);

    // create controllers
    kibanaControlsService.createDashboardControllers(
        dashboardId, dataViewIdPolygons, CONTROLLER_FIELDS);

    // create visualizations
    var visualisations =
        createVisualizations(dashboardId, dataViewIdPolygons, dataViewIdFeatureProperties);

    return String.format(
        "Polygons dashboard: %s was successfully created! With controller and visualisations: %s",
        dashboardId, visualisations);
  }

  private List<String> createVisualizations(
      String dashboardId, String dataViewIdPolygons, String dataViewIdFeatureProperties) {

    List<String> mainVisualizations = FeatureDashboardCreationUtil.createFeatureMainVisualizations(
        visualizationService, dashboardId, dataViewIdPolygons, FEATURE_TYPE_FILED);

    List<String> lengthMetrics = FeatureDashboardCreationUtil.createFeatureMetricsVisualizations(
        visualizationService, dashboardId, dataViewIdPolygons, "properties.area");

    List<String> featurePropertyCounts =
        FeatureDashboardCreationUtil.createFeatureAttributesVisualizations(
            visualizationService, dashboardId, dataViewIdPolygons, FEATURE_PROPERTIES_KEY_FILED);

    var featurePropertiesByType =
        FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
            visualizationService,
            dashboardId,
            dataViewIdPolygons,
            FEATURE_PROPERTIES_KEY_FILED,
            FEATURE_TYPE_FILED,
            "Polygon Feature Properties by Type Table",
            "Top 100 Polygon Feature Properties",
            "Top 20 Polygon Feature Types");

    var featurePropertyValues =
        FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
            visualizationService,
            dashboardId,
            dataViewIdFeatureProperties,
            "key.keyword",
            "value.keyword",
            "Polygon Feature Property Values Table",
            "Top 100 Polygon Feature Properties",
            "Top 20 Polygon Feature Property Values");

    return Stream.of(
            lengthMetrics,
            featurePropertyCounts,
            List.of(featurePropertiesByType, featurePropertyValues),
            mainVisualizations)
        .flatMap(java.util.Collection::stream)
        .toList();
  }
}
