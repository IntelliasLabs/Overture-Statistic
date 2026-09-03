/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.controller.diff;

import com.intellias.mobility.statistic.framework.common.IndexManager;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.DashboardService;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.model.PanelType;
import com.intellias.mobility.statistic.framework.dashboards.dataview.DataViewService;
import com.intellias.mobility.statistic.framework.dashboards.dto.DifferenceRequest;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationService;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DiffDashboardController implements DiffDashboardControllerOpenApi {

  private static final String DASHBOARD_NAME = "Difference Dashboard";

  private static final String STATISTIC_PREFIX = "Statistic";

  private static final String ALL_FEATURES_DATA_VIEW_NAME = STATISTIC_PREFIX + " all features";
  private static final String ALL_FEATURE_PROPERTIES_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " all feature properties";
  private static final String LINE_FEATURES_DATA_VIEW_NAME = STATISTIC_PREFIX + " lines features";
  private static final String ALL_RANGE_ATTRIBUTES_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " all range attributes";
  private static final String POLYGON_FEATURES_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " polygons features";
  private static final String DIFFERENCE_PER_FEATURE_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " difference per feature";
  private static final String DIFFERENCE_PER_FEATURE_TYPE_DATA_VIEW_NAME =
      STATISTIC_PREFIX + " difference per feature type";

  private static final Map<VisualizationType, String> DATA_VIEW_MAP = Map.ofEntries(
      Map.entry(VisualizationType.FEATURE_COUNT_DIFF, ALL_FEATURES_DATA_VIEW_NAME),
      Map.entry(VisualizationType.FEATURE_PROPERTY_TYPES_COUNT_DIFF, ALL_FEATURES_DATA_VIEW_NAME),
      Map.entry(VisualizationType.FEATURE_PROPERTY_VALUES_COUNT_DIFF, ALL_FEATURES_DATA_VIEW_NAME),
      Map.entry(VisualizationType.RANGE_ATTRIBUTE_TYPES_COUNT_DIFF, LINE_FEATURES_DATA_VIEW_NAME),
      Map.entry(VisualizationType.RANGE_ATTRIBUTE_VALUES_COUNT_DIFF, LINE_FEATURES_DATA_VIEW_NAME),
      Map.entry(VisualizationType.LENGTH_DIFF, LINE_FEATURES_DATA_VIEW_NAME),
      Map.entry(VisualizationType.AREA_DIFF, POLYGON_FEATURES_DATA_VIEW_NAME),
      Map.entry(
          VisualizationType.FEATURE_PER_PROPERTY_TYPE_COUNT_DIFF,
          ALL_FEATURE_PROPERTIES_DATA_VIEW_NAME),
      Map.entry(
          VisualizationType.FEATURE_PER_RANGE_ATTRIBUTE_TYPE_COUNT_DIFF,
          ALL_RANGE_ATTRIBUTES_DATA_VIEW_NAME),
      Map.entry(VisualizationType.CHANGED_FEATURES_COUNT, DIFFERENCE_PER_FEATURE_DATA_VIEW_NAME),
      Map.entry(VisualizationType.ADDED_FEATURES_COUNT, DIFFERENCE_PER_FEATURE_TYPE_DATA_VIEW_NAME),
      Map.entry(
          VisualizationType.DELETED_FEATURES_COUNT, DIFFERENCE_PER_FEATURE_TYPE_DATA_VIEW_NAME));

  private final DataViewService dataViewService;
  private final DashboardService dashboardService;
  private final VisualizationService visualizationService;

  private final Map<String, Supplier<Set<String>>> dataViewIndexSuppliers;

  public DiffDashboardController(
      IndexManager indexManager,
      DataViewService dataViewService,
      DashboardService dashboardService,
      VisualizationService visualizationService) {
    this.dashboardService = dashboardService;
    this.dataViewService = dataViewService;
    this.visualizationService = visualizationService;

    this.dataViewIndexSuppliers = Map.of(
        ALL_FEATURES_DATA_VIEW_NAME, indexManager::getAllFeatureIndexList,
        ALL_FEATURE_PROPERTIES_DATA_VIEW_NAME, indexManager::getAllFeaturePropertiesIndexList,
        LINE_FEATURES_DATA_VIEW_NAME, indexManager::getLineFeatureIndexList,
        ALL_RANGE_ATTRIBUTES_DATA_VIEW_NAME, indexManager::getAllRangeAttributesIndexList,
        POLYGON_FEATURES_DATA_VIEW_NAME, indexManager::getPolygonFeatureIndexList,
        DIFFERENCE_PER_FEATURE_DATA_VIEW_NAME, indexManager::getDifferencePerFeatureIndex,
        DIFFERENCE_PER_FEATURE_TYPE_DATA_VIEW_NAME, indexManager::getDifferencePerFeatureTypeIndex);
  }

  @Override
  public ResponseEntity<String> createFeatureCountDiff(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(
        createVisualization(differenceRequest, VisualizationType.FEATURE_COUNT_DIFF));
  }

  @Override
  public ResponseEntity<String> createFeaturePropertyTypesCountDiff(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(createVisualization(
        differenceRequest, VisualizationType.FEATURE_PROPERTY_TYPES_COUNT_DIFF));
  }

  @Override
  public ResponseEntity<String> createFeaturePropertyValuesCountDiff(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(createVisualization(
        differenceRequest, VisualizationType.FEATURE_PROPERTY_VALUES_COUNT_DIFF));
  }

  @Override
  public ResponseEntity<String> createRangeAttributeTypesCountDiff(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(
        createVisualization(differenceRequest, VisualizationType.RANGE_ATTRIBUTE_TYPES_COUNT_DIFF));
  }

  @Override
  public ResponseEntity<String> createRangeAttributeValuesCountDiff(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(createVisualization(
        differenceRequest, VisualizationType.RANGE_ATTRIBUTE_VALUES_COUNT_DIFF));
  }

  @Override
  public ResponseEntity<String> createLengthDiff(@RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(createVisualization(differenceRequest, VisualizationType.LENGTH_DIFF));
  }

  @Override
  public ResponseEntity<String> createAreaDiff(@RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(createVisualization(differenceRequest, VisualizationType.AREA_DIFF));
  }

  @Override
  public ResponseEntity<String> createFeaturePerPropertyTypeCountDiff(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(createVisualization(
        differenceRequest, VisualizationType.FEATURE_PER_PROPERTY_TYPE_COUNT_DIFF));
  }

  @Override
  public ResponseEntity<String> createFeaturePerRangeAttributeTypeCountDiff(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(createVisualization(
        differenceRequest, VisualizationType.FEATURE_PER_RANGE_ATTRIBUTE_TYPE_COUNT_DIFF));
  }

  @Override
  public ResponseEntity<String> createChangedFeaturesCount(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(
        createVisualization(differenceRequest, VisualizationType.CHANGED_FEATURES_COUNT));
  }

  @Override
  public ResponseEntity<String> createAddedFeaturesCount(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(
        createVisualization(differenceRequest, VisualizationType.ADDED_FEATURES_COUNT));
  }

  @Override
  public ResponseEntity<String> createDeletedFeaturesCount(
      @RequestBody DifferenceRequest differenceRequest) {
    return ResponseEntity.ok(
        createVisualization(differenceRequest, VisualizationType.DELETED_FEATURES_COUNT));
  }

  @Override
  public ResponseEntity<String> createAllAvailable(DifferenceRequest differenceRequest) {
    List<String> createdVisualizationIds = new ArrayList<>();
    createdVisualizationIds.add(createFeatureCountDiff(differenceRequest).getBody());
    createdVisualizationIds.add(
        createFeaturePropertyTypesCountDiff(differenceRequest).getBody());
    createdVisualizationIds.add(
        createFeaturePropertyValuesCountDiff(differenceRequest).getBody());
    createdVisualizationIds.add(
        createRangeAttributeTypesCountDiff(differenceRequest).getBody());
    createdVisualizationIds.add(
        createRangeAttributeValuesCountDiff(differenceRequest).getBody());
    createdVisualizationIds.add(createLengthDiff(differenceRequest).getBody());
    createdVisualizationIds.add(createAreaDiff(differenceRequest).getBody());
    createdVisualizationIds.add(
        createFeaturePerPropertyTypeCountDiff(differenceRequest).getBody());
    createdVisualizationIds.add(
        createFeaturePerRangeAttributeTypeCountDiff(differenceRequest).getBody());
    createdVisualizationIds.add(createChangedFeaturesCount(differenceRequest).getBody());
    createdVisualizationIds.add(createAddedFeaturesCount(differenceRequest).getBody());
    createdVisualizationIds.add(createDeletedFeaturesCount(differenceRequest).getBody());

    return ResponseEntity.ok(String.join(", ", createdVisualizationIds));
  }

  private String createVisualization(
      DifferenceRequest differenceRequest, VisualizationType visualizationType) {
    var dataViewName = DATA_VIEW_MAP.get(visualizationType);

    var indexes = dataViewIndexSuppliers.get(dataViewName).get();
    if (indexes.isEmpty()) {
      return String.format(
          "There are no indexes for %s. Visualization was not created!", dataViewName);
    }

    var dashboardId = dashboardService.getDashboardIdOrCreate(DASHBOARD_NAME);
    var dataViewId = dataViewService.getDataViewIdOrCreate(dataViewName, indexes);

    var visualizationId = visualizationService.createVisualization(
        visualizationType,
        dashboardId,
        Map.of(
            "DATA_VIEW_ID", dataViewId,
            "SOURCE_VERSION", differenceRequest.getSourceVersion(),
            "TARGET_VERSION", differenceRequest.getTargetVersion()),
        PanelType.LENS);

    return String.format("Visualization: %s was successfully created!", visualizationId);
  }
}
