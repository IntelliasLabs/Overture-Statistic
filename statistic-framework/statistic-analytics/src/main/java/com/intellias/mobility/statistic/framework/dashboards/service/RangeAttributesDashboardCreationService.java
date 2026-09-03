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
public class RangeAttributesDashboardCreationService {
  private static final String DASHBOARD_NAME = "Range Attributes Dashboard";

  private static final String FEATURE_TYPE_FIELD = "featureType.keyword";
  private static final String VERSION_FIELD = "version.keyword";
  private static final String KEY_FIELD = "key.keyword";
  private static final String VALUE_FIELD = "value.keyword";

  private static final List<String> CONTROLLER_FIELDS =
      List.of(FEATURE_TYPE_FIELD, VERSION_FIELD, KEY_FIELD, VALUE_FIELD);

  private static final String STATISTIC_PREFIX = "Statistic";

  private static final String ALL_RANGE_ATTRIBUTES_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " all range attributes";

  private final DataViewService dataViewService;
  private final DashboardService dashboardService;
  private final VisualizationService visualizationService;
  private final KibanaControlsService kibanaControlsService;

  private final Map<String, Supplier<Set<String>>> dataViewIndexSuppliers;

  public RangeAttributesDashboardCreationService(
      IndexManager indexManager,
      DataViewService dataViewService,
      DashboardService dashboardService,
      VisualizationService visualizationService,
      KibanaControlsService kibanaControlsService) {
    this.dashboardService = dashboardService;
    this.dataViewService = dataViewService;
    this.visualizationService = visualizationService;
    this.kibanaControlsService = kibanaControlsService;

    this.dataViewIndexSuppliers =
        Map.of(ALL_RANGE_ATTRIBUTES_DATA_VIEW_NAME, indexManager::getAllRangeAttributesIndexList);
  }

  public String createRangeAttributesDashboard() {
    var rangeAttributesDataViewName = ALL_RANGE_ATTRIBUTES_DATA_VIEW_NAME;
    var rangeAttributesIndex =
        dataViewIndexSuppliers.get(rangeAttributesDataViewName).get();
    if (rangeAttributesIndex.isEmpty()) {
      return String.format(
          "There are no indexes for %s. Dashboard was not created!", rangeAttributesIndex);
    }

    var dataViewIdRangeAttributes =
        dataViewService.getDataViewIdOrCreate(rangeAttributesDataViewName, rangeAttributesIndex);

    var dashboardId = dashboardService.getDashboardIdOrCreate(DASHBOARD_NAME);

    // create controllers
    kibanaControlsService.createDashboardControllers(
        dashboardId, dataViewIdRangeAttributes, CONTROLLER_FIELDS);

    // create visualizations
    var visualisations = createVisualizations(dashboardId, dataViewIdRangeAttributes);

    return String.format(
        "Range Attributes dashboard: %s was successfully created! With controller and visualisations: %s",
        dashboardId, visualisations);
  }

  private List<String> createVisualizations(String dashboardId, String dataViewIdRangeAttributes) {

    List<String> mainVisualizations = FeatureDashboardCreationUtil.createFeatureMainVisualizations(
        visualizationService, dashboardId, dataViewIdRangeAttributes, FEATURE_TYPE_FIELD);

    List<String> lengthMetrics = FeatureDashboardCreationUtil.createFeatureMetricsVisualizations(
        visualizationService, dashboardId, dataViewIdRangeAttributes, "lengthMeters");

    List<String> rangeAttributeCounts =
        FeatureDashboardCreationUtil.createFeatureAttributesVisualizations(
            visualizationService, dashboardId, dataViewIdRangeAttributes, KEY_FIELD);

    var rangeAttributeByType =
        FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
            visualizationService,
            dashboardId,
            dataViewIdRangeAttributes,
            KEY_FIELD,
            FEATURE_TYPE_FIELD,
            "Range Attribute by Type Table",
            "Top 100 Range Attributes",
            "Top 20 Feature Types");

    var rangeAttributeValues =
        FeatureDashboardCreationUtil.createFeatureAttributeTableVisualization(
            visualizationService,
            dashboardId,
            dataViewIdRangeAttributes,
            KEY_FIELD,
            VALUE_FIELD,
            "Range Attribute Values Table",
            "Top 100 Range Attributes",
            "Top 20 Range Attribute Values");

    return Stream.of(
            lengthMetrics,
            rangeAttributeCounts,
            List.of(rangeAttributeByType, rangeAttributeValues),
            mainVisualizations)
        .flatMap(java.util.Collection::stream)
        .toList();
  }
}
