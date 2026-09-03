/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.visualization;

import java.util.HashMap;
import java.util.Map;

public class VisualizationBuilder {

  public String buildVisualization(VisualizationType type, Map<String, String> parameters) {
    Map<String, String> valuesToReplace = buildKeyAndValueToReplace(parameters);
    return buildPanel(type.getJsonPath(), valuesToReplace);
  }

  private String buildPanel(String jsonPath, Map<String, String> valuesToReplace) {
    String jsonTemplate = VisualizationUtil.readFileContent(jsonPath);

    for (Map.Entry<String, String> entry : valuesToReplace.entrySet()) {
      jsonTemplate = jsonTemplate.replace(entry.getKey(), entry.getValue());
    }

    return jsonTemplate;
  }

  private Map<String, String> buildKeyAndValueToReplace(Map<String, String> params) {
    Map<String, String> result = new HashMap<>();

    // generated
    result.put("$INDEX_COLUMN_ID", VisualizationUtil.generateId());
    result.put("$PROPERTY_TYPE_COLUMN_ID", VisualizationUtil.generateId());
    result.put("$FIRST_VERSION_COUNT_COLUMN_ID", VisualizationUtil.generateId());
    result.put("$SECOND_VERSION_COUNT_COLUMN_ID", VisualizationUtil.generateId());
    result.put("$SUMMARY_COLUMN_ID", VisualizationUtil.generateId());
    result.put("$DATASOURCE_LAYER_ID", VisualizationUtil.generateId());
    result.put("$CONTROLLER_ID", VisualizationUtil.generateId());
    result.put("$FIRST_COLUMN_ID", VisualizationUtil.generateId());
    result.put("$SECOND_COLUMN_ID", VisualizationUtil.generateId());
    result.put("$THIRD_COLUMN_ID", VisualizationUtil.generateId());
    result.put("$MAP_ID_FIRST", VisualizationUtil.generateId());
    result.put("$MAP_ID_SECOND", VisualizationUtil.generateId());
    result.put("$MAP_ID_THIRD", VisualizationUtil.generateId());
    result.put("$MAP_ID_FOURTH", VisualizationUtil.generateId());
    result.put("$MAP_ID_FIFTH", VisualizationUtil.generateId());
    result.put("$MAP_ID_SIXTH", VisualizationUtil.generateId());
    result.put("$MAP_ID_SEVENTH", VisualizationUtil.generateId());

    // from parameters
    if (params.containsKey("DATA_VIEW_ID")) {
      result.put("$DATA_VIEW_ID", params.get("DATA_VIEW_ID"));
    }

    if (params.containsKey("DATA_VIEW_ID_FIRST")) {
      result.put("$DATA_VIEW_ID_FIRST", params.get("DATA_VIEW_ID_FIRST"));
    }

    if (params.containsKey("DATA_VIEW_ID_SECOND")) {
      result.put("$DATA_VIEW_ID_SECOND", params.get("DATA_VIEW_ID_SECOND"));
    }

    if (params.containsKey("DATA_VIEW_ID_THIRD")) {
      result.put("$DATA_VIEW_ID_THIRD", params.get("DATA_VIEW_ID_THIRD"));
    }

    if (params.containsKey("SOURCE_VERSION")) {
      result.put("$SOURCE_VERSION", params.get("SOURCE_VERSION"));
    }

    if (params.containsKey("TARGET_VERSION")) {
      result.put("$TARGET_VERSION", params.get("TARGET_VERSION"));
    }

    if (params.containsKey("OPERATION_TYPE")) {
      result.put("$OPERATION_TYPE", params.get("OPERATION_TYPE"));
    }

    if (params.containsKey("SOURCE_FIELD")) {
      result.put("$SOURCE_FIELD", params.get("SOURCE_FIELD"));
    }

    if (params.containsKey("SOURCE_FIELD_PRIMARY")) {
      result.put("$SOURCE_FIELD_PRIMARY", params.get("SOURCE_FIELD_PRIMARY"));
    }

    if (params.containsKey("SOURCE_FIELD_SECONDARY")) {
      result.put("$SOURCE_FIELD_SECONDARY", params.get("SOURCE_FIELD_SECONDARY"));
    }

    if (params.containsKey("SOURCE_FIELD_PRIMARY_LABEL")) {
      result.put("$SOURCE_FIELD_PRIMARY_LABEL", params.get("SOURCE_FIELD_PRIMARY_LABEL"));
    }

    if (params.containsKey("SOURCE_FIELD_SECONDARY_LABEL")) {
      result.put("$SOURCE_FIELD_SECONDARY_LABEL", params.get("SOURCE_FIELD_SECONDARY_LABEL"));
    }

    if (params.containsKey("PANEL_TITLE")) {
      result.put("$PANEL_TITLE", params.get("PANEL_TITLE"));
    }

    if (params.containsKey("PRIMARY_BUCKET_LABEL")) {
      result.put("$PRIMARY_BUCKET_LABEL", params.get("PRIMARY_BUCKET_LABEL"));
    }

    if (params.containsKey("SECONDARY_BUCKET_LABEL")) {
      result.put("$SECONDARY_BUCKET_LABEL", params.get("SECONDARY_BUCKET_LABEL"));
    }

    return result;
  }
}
