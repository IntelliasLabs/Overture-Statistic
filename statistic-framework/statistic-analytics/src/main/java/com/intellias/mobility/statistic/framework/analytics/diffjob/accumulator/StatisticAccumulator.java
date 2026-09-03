/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob.accumulator;

import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferenceMetadata;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.FeaturePropertiesMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Data;

@Data
public class StatisticAccumulator {
  private AtomicReference<DifferenceMetadata> metadata;
  private FeaturePropertiesMap sourceFeatureProperties;
  private FeaturePropertiesMap targetFeatureProperties;
  private FeaturePropertiesMap sourceFeatureRangeAttributes;
  private FeaturePropertiesMap targetFeatureRangeAttributes;
  private Set<String> addedFeatureIds;
  private Set<String> deletedFeatureIds;

  public StatisticAccumulator() {
    this.metadata = new AtomicReference<>();
    this.sourceFeatureProperties = new FeaturePropertiesMap();
    this.targetFeatureProperties = new FeaturePropertiesMap();
    this.sourceFeatureRangeAttributes = new FeaturePropertiesMap();
    this.targetFeatureRangeAttributes = new FeaturePropertiesMap();
    this.addedFeatureIds = ConcurrentHashMap.newKeySet();
    this.deletedFeatureIds = ConcurrentHashMap.newKeySet();
  }
}
