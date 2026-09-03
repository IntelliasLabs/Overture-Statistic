/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.dataview.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataView implements Serializable {

  private String name;

  /**
   * Comma-separated list of data streams, indices, and aliases that you want to search. Supports wildcards (*).
   */
  private String title;
}
