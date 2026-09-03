/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DifferencePerFeatureMetadata extends DifferenceMetadata {
  private String featureId;

  public DifferencePerFeatureMetadata(
      String sourceIndex, String sourceVersion, String targetVersion, String featureId) {
    super(sourceIndex, sourceVersion, targetVersion);
    this.featureId = featureId;
  }
}
