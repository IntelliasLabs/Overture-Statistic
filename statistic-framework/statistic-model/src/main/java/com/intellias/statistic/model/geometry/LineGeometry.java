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
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Line String Geometry
 */
@Data
@NoArgsConstructor
@JsonTypeName("LineString")
public class LineGeometry implements StatisticGeometry {

  @JsonProperty("type")
  private final String type = "LineString";

  @JsonProperty("coordinates")
  private List<List<Double>> coordinates = new ArrayList<>();

  public LineGeometry(List<PointGeometry> points) {
    this.coordinates =
        points.stream().map(PointGeometry::getCoordinates).collect(Collectors.toList());
  }

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonIgnore
  public List<PointGeometry> retrievePoints() {
    return coordinates.stream()
        .map(coords -> new PointGeometry(new LonLat(coords.getFirst(), coords.get(1))))
        .collect(Collectors.toList());
  }

  @JsonIgnore
  public void enrichPoints(List<PointGeometry> points) {
    this.coordinates =
        points.stream().map(PointGeometry::getCoordinates).collect(Collectors.toList());
  }
}
