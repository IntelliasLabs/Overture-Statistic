/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MultiPoint geometry.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("MultiPoint")
public class MultiPointGeometry implements StatisticGeometry {

  @JsonProperty("type")
  private final String type = "MultiPoint";

  @JsonProperty("coordinates")
  private List<List<Double>> coordinates = new ArrayList<>();

  public MultiPointGeometry(List<PointGeometry> points) {
    this.coordinates =
        points.stream().map(PointGeometry::getCoordinates).collect(Collectors.toList());
  }

  @JsonIgnore
  public List<PointGeometry> getPoints() {
    return coordinates.stream()
        .map(coords -> new PointGeometry(new LonLat(coords.getFirst(), coords.get(1))))
        .collect(Collectors.toList());
  }

  @JsonIgnore
  public void setPoints(List<PointGeometry> points) {
    this.coordinates =
        points.stream().map(PointGeometry::getCoordinates).collect(Collectors.toList());
  }
}
