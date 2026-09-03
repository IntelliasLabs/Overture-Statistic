/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.intellias.statistic.model.geometry.StatisticGeometry;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a collection of GeoJSON features.
 *
 * @param <G> the specific geometry type of the features in this collection
 */
@Data
@JsonTypeName("Feature")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticFeatureCollection<G extends StatisticGeometry> {

  private final String type = "FeatureCollection";

  // Make the list parametrized by the same generic type, G
  private List<StatisticFeature<G>> features = new ArrayList<>();

  /**
   * Adds a single feature to the collection.
   */
  public void addFeature(StatisticFeature<G> feature) {
    this.features.add(feature);
  }

  /**
   * Adds multiple features to the collection.
   */
  public void addFeatures(List<StatisticFeature<G>> features) {
    this.features.addAll(features);
  }
}
