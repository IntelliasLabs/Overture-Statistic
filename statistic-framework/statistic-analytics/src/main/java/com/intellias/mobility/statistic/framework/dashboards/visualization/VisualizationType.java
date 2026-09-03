/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.visualization;

import lombok.Getter;

@Getter
public enum VisualizationType {

  // diff
  FEATURE_COUNT_DIFF("/kibana/diff/FeatureCountDiff.json"),
  FEATURE_PROPERTY_TYPES_COUNT_DIFF("/kibana/diff/FeaturePropertyTypesCountDiff.json"),
  FEATURE_PROPERTY_VALUES_COUNT_DIFF("/kibana/diff/FeaturePropertyValuesCountDiff.json"),
  RANGE_ATTRIBUTE_TYPES_COUNT_DIFF("/kibana/diff/RangeAttributeTypesCountDiff.json"),
  RANGE_ATTRIBUTE_VALUES_COUNT_DIFF("/kibana/diff/RangeAttributeValuesCountDiff.json"),
  LENGTH_DIFF("/kibana/diff/LengthDiff.json"),
  AREA_DIFF("/kibana/diff/AreaDiff.json"),
  FEATURE_PER_PROPERTY_TYPE_COUNT_DIFF("/kibana/diff/FeaturePerPropertyTypeCountDiff.json"),
  FEATURE_PER_RANGE_ATTRIBUTE_TYPE_COUNT_DIFF(
      "/kibana/diff/FeaturePerRangeAttributeTypeCountDiff.json"),
  CHANGED_FEATURES_COUNT("/kibana/diff/ChangedFeaturesCount.json"),
  ADDED_FEATURES_COUNT("/kibana/diff/AddedFeaturesCount.json"),
  DELETED_FEATURES_COUNT("/kibana/diff/DeletedFeaturesCount.json"),

  // statistic
  TABLE_FIELD_COUNT("/kibana/statistic/tableFeatureType.json"),
  COMMON_MAP("/kibana/statistic/commonMap.json"),
  TOTAL_COUNT("/kibana/statistic/totalCount.json"),
  PIE_FEATURE_TYPE("/kibana/statistic/pieFeatureType.json"),
  BAR_FEATURE_PROPERTIES("/kibana/statistic/barFeatureProperties.json"),
  TABLE_PROPERTIES_FOR_TYPE("/kibana/statistic/tablePropertiesForType.json"),
  FEATURE_METRICS("/kibana/statistic/featureMetrics.json"),
  FEATURE_MAP("/kibana/statistic/featureMap.json");

  private final String jsonPath;

  VisualizationType(String jsonPath) {
    this.jsonPath = jsonPath;
  }
}
