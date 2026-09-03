/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifferencePerFeatureType {
  private DifferenceMetadata metadata;
  private Map<String, Set<String>> addedFeatureProperties;
  private Map<String, Set<String>> deletedFeatureProperties;
  private Map<String, Set<String>> addedRangeAttributes;
  private Map<String, Set<String>> deletedRangeAttributes;
  private Set<String> addedFeatureIds;
  private Set<String> deletedFeatureIds;
}
