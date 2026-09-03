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
public class LinesDashboardCreationService {

  private static final String DASHBOARD_NAME = "Lines Dashboard";

  private static final String FEATURE_TYPE_FILED = "properties.featureType.keyword";
  private static final String FEATURE_PROPERTIES_KEY_FILED =
      "properties.featureProperties.key.keyword";
  private static final String FEATURE_PROPERTIES_VALUES_FILED =
      "properties.featureProperties.values.keyword";

  private static final List<String> CONTROLLER_FIELDS = List.of(
      "properties.featureType.keyword",
      "properties.version.keyword",
      "properties.rangeAttributes.key.keyword");

  private static final String STATISTIC_PREFIX = "Statistic";

  private static final String LINESTRING_FEATURE_PROPERTIES_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " linestring feature properties";
  private static final String LINES_FEATURES = STATISTIC_PREFIX + " lines features";

  private final DataViewService dataViewService;
  private final DashboardService dashboardService;
  private final VisualizationService visualizationService;
  private final KibanaControlsService kibanaControlsService;

  private final Map<String, Supplier<Set<String>>> dataViewIndexSuppliers;

  public LinesDashboardCreationService(
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
        LINES_FEATURES,
        indexManager::getLineFeatureIndexList,
        LINESTRING_FEATURE_PROPERTIES_DATA_VIEW_NAME,
        indexManager::getLineFeaturePropertiesIndexList);
  }

  public String createLinesDashboard() {
    var linesDataViewName = LINES_FEATURES;
    var linesIndex = dataViewIndexSuppliers.get(linesDataViewName).get();
    var featurePropertiesDataViewName = LINESTRING_FEATURE_PROPERTIES_DATA_VIEW_NAME;
    var featurePropertiesIndex =
        dataViewIndexSuppliers.get(featurePropertiesDataViewName).get();
    if (linesIndex.isEmpty()) {
      return String.format("There are no indexes for %s. Dashboard was not created!", linesIndex);
    }
    if (featurePropertiesIndex.isEmpty()) {
      return String.format(
          "There are no indexes for %s. Dashboard was not created!", featurePropertiesDataViewName);
    }

    var dataViewIdLines = dataViewService.getDataViewIdOrCreate(linesDataViewName, linesIndex);
    var dataViewIdFeatureProperties = dataViewService.getDataViewIdOrCreate(
        featurePropertiesDataViewName, featurePropertiesIndex);

    var dashboardId = dashboardService.getDashboardIdOrCreate(DASHBOARD_NAME);

    // create controllers
    kibanaControlsService.createDashboardControllers(
        dashboardId, dataViewIdLines, CONTROLLER_FIELDS);

    // create visualizations
    var visualisations =
        createVisualizations(dashboardId, dataViewIdLines, dataViewIdFeatureProperties);

    return String.format(
        "Lines dashboard: %s was successfully created! With controller and visualisations: %s",
        dashboardId, visualisations);
  }

  private List<String> createVisualizations(
      String dashboardId, String dataViewIdLines, String dataViewIdFeatureProperties) {

    List<String> mainVisualizations = FeatureDashboardCreationUtil.createFeatureMainVisualizations(
        visualizationService, dashboardId, dataViewIdLines, "properties.featureType.keyword");

    List<String> lengthMetrics = FeatureDashboardCreationUtil.createFeatureMetricsVisualizations(
        visualizationService, dashboardId, dataViewIdLines, "properties.lengthMeters");

    List<String> featurePropertyCounts =
        FeatureDashboardCreationUtil.createFeatureAttributesVisualizations(
            visualizationService, dashboardId, dataViewIdLines, FEATURE_PROPERTIES_KEY_FILED);

    var featurePropertiesByType =
        FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
            visualizationService,
            dashboardId,
            dataViewIdLines,
            FEATURE_PROPERTIES_KEY_FILED,
            FEATURE_TYPE_FILED,
            "Line Feature Properties by Type Table",
            "Top 100 Line Feature Properties",
            "Top 20 Line Feature Types");

    var featurePropertyValues =
        FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
            visualizationService,
            dashboardId,
            dataViewIdFeatureProperties,
            "key.keyword",
            "value.keyword",
            "Line Feature Property Values Table",
            "Top 100 Line Feature Properties",
            "Top 20 Line Feature Property Values");

    List<String> rangePropertyCounts =
        FeatureDashboardCreationUtil.createFeatureAttributesVisualizations(
            visualizationService,
            dashboardId,
            dataViewIdLines,
            "properties.rangeAttributes.key.keyword");

    var rangePropertiesByType =
        FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
            visualizationService,
            dashboardId,
            dataViewIdLines,
            "properties.rangeAttributes.key.keyword",
            FEATURE_TYPE_FILED,
            "Range Attribute by Type Table",
            "Top 100 Range Attributes",
            "Top 20 Feature Types");

    var rangePropertyValues = FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
        visualizationService,
        dashboardId,
        dataViewIdLines,
        "properties.rangeAttributes.key.keyword",
        "properties.rangeAttributes.values.value.keyword",
        "Range Attribute Values Table",
        "Top 100 Range Attributes",
        "Top 20 Range Attribute Values");

    return Stream.of(
            lengthMetrics,
            featurePropertyCounts,
            List.of(featurePropertiesByType, featurePropertyValues),
            rangePropertyCounts,
            List.of(rangePropertiesByType, rangePropertyValues),
            mainVisualizations)
        .flatMap(java.util.Collection::stream)
        .toList();
  }
}
