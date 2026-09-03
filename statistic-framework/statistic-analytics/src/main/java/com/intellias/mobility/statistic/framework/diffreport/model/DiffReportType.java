/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.model;

import lombok.Getter;

@Getter
public enum DiffReportType {
  PER_FEATURE("per-feature"),
  PER_FEATURE_TYPE("per-feature-type");

  private final String pathValue;

  DiffReportType(String pathValue) {
    this.pathValue = pathValue;
  }
}
