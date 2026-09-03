/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.dashboard.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.io.Serializable;
import java.util.Arrays;

public enum PanelType implements Serializable {
  LENS("lens"),
  INDEX_PATTERN("index-pattern"),
  MAP("map");

  private final String value;

  PanelType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static PanelType fromString(String value) {
    return Arrays.stream(PanelType.values())
        .filter(panelType -> panelType.getValue().equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown value: " + value));
  }
}
