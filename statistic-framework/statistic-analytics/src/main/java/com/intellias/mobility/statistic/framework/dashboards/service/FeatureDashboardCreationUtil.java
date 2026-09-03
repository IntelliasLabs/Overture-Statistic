/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.service;

import static com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationType.*;
import static com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationType.TABLE_PROPERTIES_FOR_TYPE;

import com.intellias.mobility.statistic.framework.dashboards.dashboard.model.PanelType;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationService;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FeatureDashboardCreationUtil {

  public static List<String> createFeatureMainVisualizations(
      VisualizationService visualizationService,
      String dashboardId,
      String dataViewId,
      String sourceField) {
    // create map
    var visualizationMap = visualizationService.createVisualization(
        FEATURE_MAP, dashboardId, Map.of("DATA_VIEW_ID", dataViewId), PanelType.MAP);

    // create total count
    var visualizationIdTotalCount = visualizationService.createVisualization(
        TOTAL_COUNT, dashboardId, Map.of("DATA_VIEW_ID", dataViewId), PanelType.LENS);

    // create pie feature type
    var visualizationIdPieFeatureType = visualizationService.createVisualization(
        PIE_FEATURE_TYPE,
        dashboardId,
        Map.of("DATA_VIEW_ID", dataViewId, "SOURCE_FIELD", sourceField),
        PanelType.LENS);

    // create table feature type
    var visualizationIdTableFeatureType = visualizationService.createVisualization(
        TABLE_FIELD_COUNT,
        dashboardId,
        Map.of("DATA_VIEW_ID", dataViewId, "SOURCE_FIELD", sourceField),
        PanelType.LENS);

    return List.of(
        visualizationMap,
        visualizationIdTotalCount,
        visualizationIdPieFeatureType,
        visualizationIdTableFeatureType);
  }

  public static List<String> createFeatureMetricsVisualizations(
      VisualizationService visualizationService,
      String dashboardId,
      String dataViewIdPolygons,
      String fieldName) {
    // create length metrics
    var visualizationIdSumLength = visualizationService.createVisualization(
        FEATURE_METRICS,
        dashboardId,
        Map.of(
            "DATA_VIEW_ID", dataViewIdPolygons, "OPERATION_TYPE", "sum", "SOURCE_FIELD", fieldName),
        PanelType.LENS);

    var visualizationIdMaxLength = visualizationService.createVisualization(
        FEATURE_METRICS,
        dashboardId,
        Map.of(
            "DATA_VIEW_ID", dataViewIdPolygons, "OPERATION_TYPE", "max", "SOURCE_FIELD", fieldName),
        PanelType.LENS);

    var visualizationIdMinLength = visualizationService.createVisualization(
        FEATURE_METRICS,
        dashboardId,
        Map.of(
            "DATA_VIEW_ID", dataViewIdPolygons, "OPERATION_TYPE", "min", "SOURCE_FIELD", fieldName),
        PanelType.LENS);

    var visualizationIdAvgLength = visualizationService.createVisualization(
        FEATURE_METRICS,
        dashboardId,
        Map.of(
            "DATA_VIEW_ID",
            dataViewIdPolygons,
            "OPERATION_TYPE",
            "average",
            "SOURCE_FIELD",
            fieldName),
        PanelType.LENS);

    return List.of(
        visualizationIdSumLength,
        visualizationIdMaxLength,
        visualizationIdMinLength,
        visualizationIdAvgLength);
  }

  public static List<String> createFeatureAttributesVisualizations(
      VisualizationService visualizationService,
      String dashboardId,
      String dataViewId,
      String sourceField) {
    var visualizationIdPieFeatureProperties = visualizationService.createVisualization(
        PIE_FEATURE_TYPE,
        dashboardId,
        Map.of("DATA_VIEW_ID", dataViewId, "SOURCE_FIELD", sourceField),
        PanelType.LENS);

    var visualizationIdTableFeatureProperties = visualizationService.createVisualization(
        TABLE_FIELD_COUNT,
        dashboardId,
        Map.of("DATA_VIEW_ID", dataViewId, "SOURCE_FIELD", sourceField),
        PanelType.LENS);

    return List.of(visualizationIdPieFeatureProperties, visualizationIdTableFeatureProperties);
  }

  public static String createFeatureAttributeTableVisualization(
      VisualizationService visualizationService,
      String dashboardId,
      String dataViewId,
      String sourceFieldPrimary,
      String sourceFieldSecondary,
      String tableTitle,
      String primaryBucketLabel,
      String secondaryBucketLabel) {
    return visualizationService.createVisualization(
        TABLE_PROPERTIES_FOR_TYPE,
        dashboardId,
        Map.of(
            "DATA_VIEW_ID", dataViewId,
            "PANEL_TITLE", tableTitle,
            "PRIMARY_BUCKET_LABEL", primaryBucketLabel,
            "SECONDARY_BUCKET_LABEL", secondaryBucketLabel,
            "SOURCE_FIELD_PRIMARY", sourceFieldPrimary,
            "SOURCE_FIELD_SECONDARY", sourceFieldSecondary),
        PanelType.LENS);
  }
}
