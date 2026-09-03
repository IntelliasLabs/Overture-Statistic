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
public class PointsDashboardCreationService {

  private static final String DASHBOARD_NAME = "Points Dashboard";

  private static final String FEATURE_TYPE_FILED = "properties.featureType.keyword";
  private static final String VERSION_FILED = "properties.version.keyword";
  private static final String FEATURE_PROPERTIES_KEY_FILED =
      "properties.featureProperties.key.keyword";
  private static final String FEATURE_PROPERTIES_VALUES_FILED =
      "properties.featureProperties.values.keyword";

  private static final List<String> CONTROLS_FIELDS = List.of(FEATURE_TYPE_FILED, VERSION_FILED);

  private static final String STATISTIC_PREFIX = "Statistic";

  private static final String POINT_FEATURE_PROPERTIES_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " point feature properties";
  private static final String POINTS_FEATURES = STATISTIC_PREFIX + " points features";

  private final DataViewService dataViewService;
  private final DashboardService dashboardService;
  private final VisualizationService visualizationService;
  private final KibanaControlsService kibanaControlsService;

  private final Map<String, Supplier<Set<String>>> dataViewIndexSuppliers;

  public PointsDashboardCreationService(
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
        POINTS_FEATURES, indexManager::getPointFeatureIndexList,
        POINT_FEATURE_PROPERTIES_DATA_VIEW_NAME, indexManager::getPointFeaturePropertiesIndexList);
  }

  public String createPointsDashboard() {
    var pointsDataViewName = POINTS_FEATURES;
    var pointsIndex = dataViewIndexSuppliers.get(pointsDataViewName).get();
    var featurePropertiesDataViewName = POINT_FEATURE_PROPERTIES_DATA_VIEW_NAME;
    var featurePropertiesIndex =
        dataViewIndexSuppliers.get(featurePropertiesDataViewName).get();
    if (pointsIndex.isEmpty()) {
      return String.format("There are no indexes for %s. Dashboard was not created!", pointsIndex);
    }
    if (featurePropertiesIndex.isEmpty()) {
      return String.format(
          "There are no indexes for %s. Dashboard was not created!", featurePropertiesDataViewName);
    }

    var dataViewIdPoints = dataViewService.getDataViewIdOrCreate(pointsDataViewName, pointsIndex);
    var dataViewIdFeatureProperties = dataViewService.getDataViewIdOrCreate(
        featurePropertiesDataViewName, featurePropertiesIndex);

    var dashboardId = dashboardService.getDashboardIdOrCreate(DASHBOARD_NAME);

    // create controllers
    kibanaControlsService.createDashboardControllers(
        dashboardId, dataViewIdPoints, CONTROLS_FIELDS);

    // create visualizations
    var visualisations =
        createVisualizations(dashboardId, dataViewIdPoints, dataViewIdFeatureProperties);

    return String.format(
        "Points dashboard: %s was successfully created! With controller and visualisations: %s",
        dashboardId, visualisations);
  }

  private List<String> createVisualizations(
      String dashboardId, String dataViewIdPoints, String dataViewIdFeatureProperties) {

    List<String> mainVisualizations = FeatureDashboardCreationUtil.createFeatureMainVisualizations(
        visualizationService, dashboardId, dataViewIdPoints, FEATURE_TYPE_FILED);

    List<String> featurePropertyCounts =
        FeatureDashboardCreationUtil.createFeatureAttributesVisualizations(
            visualizationService, dashboardId, dataViewIdPoints, FEATURE_PROPERTIES_KEY_FILED);

    var featurePropertiesByType =
        FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
            visualizationService,
            dashboardId,
            dataViewIdPoints,
            FEATURE_PROPERTIES_KEY_FILED,
            FEATURE_TYPE_FILED,
            "Point Feature Properties by Type Table",
            "Top 100 Point Feature Properties",
            "Top 20 Point Feature Types");

    var featurePropertyValues =
        FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
            visualizationService,
            dashboardId,
            dataViewIdFeatureProperties,
            "key.keyword",
            "value.keyword",
            "Point Feature Property Values Table",
            "Top 100 Point Feature Properties",
            "Top 20 Point Feature Property Values");

    return Stream.of(
            featurePropertyCounts,
            List.of(featurePropertiesByType, featurePropertyValues),
            mainVisualizations)
        .flatMap(java.util.Collection::stream)
        .toList();
  }
}
