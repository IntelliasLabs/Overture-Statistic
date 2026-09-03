/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.service;

import static com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationType.*;

import com.intellias.mobility.statistic.framework.common.IndexManager;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.DashboardService;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.model.PanelType;
import com.intellias.mobility.statistic.framework.dashboards.dataview.DataViewService;
import com.intellias.mobility.statistic.framework.dashboards.kibana.KibanaControlsService;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

@Service
public class CommonDashboardCreationService {

  private static final String DASHBOARD_NAME = "Common Dashboard";
  private static final String FEATURE_PROPERTIES_TABLE_TITLE = "Common Feature Properties Table";
  private static final String FEATURE_PROPERTIES_PRIMARY_BUCKET_LABEL =
      "Top 100 Feature Properties";
  private static final String FEATURE_PROPERTIES_SECONDARY_BUCKET_LABEL =
      "Top 20 Feature Property Values";

  private static final List<String> CONTROLLER_FIELDS =
      List.of("properties.featureType.keyword", "properties.version.keyword");

  private static final String STATISTIC_PREFIX = "Statistic";

  private static final String ALL_FEATURES_DATA_VIEW_NAME = STATISTIC_PREFIX + " all features";
  private static final String ALL_FEATURE_PROPERTIES_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " all feature properties";
  private static final String POINTS_FEATURES = STATISTIC_PREFIX + " points features";
  private static final String LINES_FEATURES = STATISTIC_PREFIX + " lines features";
  private static final String POLYGONS_FEATURES = STATISTIC_PREFIX + " polygons features";

  private final DataViewService dataViewService;
  private final DashboardService dashboardService;
  private final VisualizationService visualizationService;
  private final KibanaControlsService kibanaControlsService;

  private final Map<String, Supplier<Set<String>>> dataViewIndexSuppliers;

  public CommonDashboardCreationService(
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
        ALL_FEATURES_DATA_VIEW_NAME, indexManager::getAllFeatureIndexList,
        ALL_FEATURE_PROPERTIES_DATA_VIEW_NAME, indexManager::getAllFeaturePropertiesIndexList,
        POINTS_FEATURES, indexManager::getPointFeatureIndexList,
        LINES_FEATURES, indexManager::getLineFeatureIndexList,
        POLYGONS_FEATURES, indexManager::getPolygonFeatureIndexList);
  }

  public String createCommonDashboard() {

    // For maps.
    // we need points, lines, polygons separately, because there will be a conflict of geometry
    // types
    var pointsDataViewName = POINTS_FEATURES;

    var pointsIndex = dataViewIndexSuppliers.get(pointsDataViewName).get();

    var linesDataViewName = LINES_FEATURES;
    var linesIndex = dataViewIndexSuppliers.get(linesDataViewName).get();

    var polygonsDataViewName = POLYGONS_FEATURES;
    var polygonsIndex = dataViewIndexSuppliers.get(polygonsDataViewName).get();

    var featurePropertiesDataViewName = ALL_FEATURE_PROPERTIES_DATA_VIEW_NAME;
    var featurePropertiesIndex =
        dataViewIndexSuppliers.get(featurePropertiesDataViewName).get();

    if (pointsIndex.isEmpty() && linesIndex.isEmpty() && polygonsIndex.isEmpty()) {
      return String.format(
          "There are no indexes for %s, %s, %s . Dashboard was not created!",
          pointsIndex, linesIndex, polygonsIndex);
    }
    if (featurePropertiesIndex.isEmpty()) {
      return String.format(
          "There are no indexes for %s. Dashboard was not created!", featurePropertiesDataViewName);
    }

    var dataViewIdPoints = dataViewService.getDataViewIdOrCreate(pointsDataViewName, pointsIndex);
    var dataViewIdLines = dataViewService.getDataViewIdOrCreate(linesDataViewName, linesIndex);
    var dataViewIdPolygons =
        dataViewService.getDataViewIdOrCreate(polygonsDataViewName, polygonsIndex);
    dataViewService.getDataViewIdOrCreate(featurePropertiesDataViewName, featurePropertiesIndex);

    // name for all lens and controllers
    var dataViewNameCommon = ALL_FEATURES_DATA_VIEW_NAME;

    var indexesCommon = dataViewIndexSuppliers.get(dataViewNameCommon).get();
    if (indexesCommon.isEmpty()) {
      return String.format(
          "There are no indexes for %s. Dashboard was not created!", dataViewNameCommon);
    }

    // created data view for controllers and lens
    var dataViewIdCommon = dataViewService.getDataViewIdOrCreate(dataViewNameCommon, indexesCommon);

    // created common dashboard
    var dashboardId = dashboardService.getDashboardIdOrCreate(DASHBOARD_NAME);

    // create controllers
    kibanaControlsService.createDashboardControllers(
        dashboardId, dataViewIdCommon, CONTROLLER_FIELDS);

    // create visualizations
    var visualisations = createVisualizations(
        dashboardId, dataViewIdCommon, dataViewIdPoints, dataViewIdLines, dataViewIdPolygons);

    return String.format(
        "Common dashboard: %s was successfully created! With controller and visualisations: %s",
        dashboardId, visualisations);
  }

  private List<String> createVisualizations(
      String dashboardId,
      String dataViewIdCommon,
      String dataViewIdPoints,
      String dataViewIdLines,
      String dataViewIdPolygons) {
    // create map
    var visualizationMap = visualizationService.createVisualization(
        COMMON_MAP,
        dashboardId,
        Map.of(
            "DATA_VIEW_ID_FIRST", dataViewIdPoints,
            "DATA_VIEW_ID_SECOND", dataViewIdLines,
            "DATA_VIEW_ID_THIRD", dataViewIdPolygons),
        PanelType.MAP);

    // create total count
    var visualizationIdTotalCount = visualizationService.createVisualization(
        TOTAL_COUNT, dashboardId, Map.of("DATA_VIEW_ID", dataViewIdCommon), PanelType.LENS);

    // create pie feature type
    var visualizationIdPieFeatureType = visualizationService.createVisualization(
        PIE_FEATURE_TYPE,
        dashboardId,
        Map.of("DATA_VIEW_ID", dataViewIdCommon, "SOURCE_FIELD", "properties.featureType.keyword"),
        PanelType.LENS);

    // create table feature type
    var visualizationIdTableFeatureType = visualizationService.createVisualization(
        TABLE_FIELD_COUNT,
        dashboardId,
        Map.of("DATA_VIEW_ID", dataViewIdCommon, "SOURCE_FIELD", "properties.featureType.keyword"),
        PanelType.LENS);

    // create pie feature properties
    var visualizationIdPieFeatureProperties = visualizationService.createVisualization(
        PIE_FEATURE_TYPE,
        dashboardId,
        Map.of(
            "DATA_VIEW_ID",
            dataViewIdCommon,
            "SOURCE_FIELD",
            "properties.featureProperties.key.keyword"),
        PanelType.LENS);

    // create bar feature properties
    var visualizationIdBarFeatureProperties = visualizationService.createVisualization(
        BAR_FEATURE_PROPERTIES,
        dashboardId,
        Map.of("DATA_VIEW_ID", dataViewIdCommon),
        PanelType.LENS);

    // create table properties -> in type
    var visualizationIdTablePropertiesForType = visualizationService.createVisualization(
        TABLE_PROPERTIES_FOR_TYPE,
        dashboardId,
        Map.of(
            "DATA_VIEW_ID",
            dataViewIdCommon,
            "PANEL_TITLE",
            FEATURE_PROPERTIES_TABLE_TITLE,
            "PRIMARY_BUCKET_LABEL",
            FEATURE_PROPERTIES_PRIMARY_BUCKET_LABEL,
            "SECONDARY_BUCKET_LABEL",
            FEATURE_PROPERTIES_SECONDARY_BUCKET_LABEL,
            "SOURCE_FIELD_PRIMARY",
            "properties.featureProperties.key.keyword",
            "SOURCE_FIELD_SECONDARY",
            "properties.featureType.keyword"),
        PanelType.LENS);

    return List.of(
        visualizationMap,
        visualizationIdTotalCount,
        visualizationIdPieFeatureType,
        visualizationIdTableFeatureType,
        visualizationIdPieFeatureProperties,
        visualizationIdBarFeatureProperties,
        visualizationIdTablePropertiesForType);
  }
}
