/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.dashboard.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Panel implements Serializable {
  private PanelType type;
  private String panelRefName;
  private String config;
  private String panelIndex;
  private GridData gridData;
}
