/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.dashboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControlGroupInput {
  private String chainingSystem;
  private String controlStyle;
  private String ignoreParentSettingsJSON;
  private String panelsJSON;
  private Boolean showApplySelections;
}
