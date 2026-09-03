/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifferencePerFeature {
  private DifferencePerFeatureMetadata metadata;
  /**
   * Represents the difference in geometry between the source and target.
   * <p>
   * The interpretation of this value depends on the geometry type:
   * <ul>
   *   <li>If the type is <b>POINT</b>, {@code geometryDifference} represents the distance between the source and target points.</li>
   *   <li>If the type is <b>LINE or MULTILINE</b>, {@code geometryDifference} represents the difference in length between the source and target lines.</li>
   *   <li>If the type is <b>POLYGON or MULTIPOLYGON</b>, {@code geometryDifference} represents the difference in area between the source and target polygons.</li>
   *   <li>If the type is <b>GEOMETRYCOLLECTION or MULTIPOINT</b>, {@code geometryDifference} will be null currently</li>
   * </ul>
   */
  private Double geometryDifference;

  private Map<String, Set<String>> addedFeatureProperties;
  private Map<String, Set<String>> deletedFeatureProperties;
  private Map<String, Set<String>> addedRangeAttributes;
  private Map<String, Set<String>> deletedRangeAttributes;

  public DifferencePerFeature(DifferencePerFeatureMetadata metadata) {
    this.metadata = metadata;
  }
}
