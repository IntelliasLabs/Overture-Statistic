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
 * MultiLineString geometry.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("MultiLineString")
public class MultiLineGeometry implements StatisticGeometry {

  @JsonProperty("type")
  private final String type = "MultiLineString";

  @JsonProperty("coordinates")
  private List<List<List<Double>>> coordinates = new ArrayList<>();

  public MultiLineGeometry(List<LineGeometry> lines) {
    this.coordinates =
        lines.stream().map(LineGeometry::getCoordinates).collect(Collectors.toList());
  }

  @JsonIgnore
  public List<LineGeometry> retrieveLines() {
    return coordinates.stream()
        .map(lineCoords -> new LineGeometry(lineCoords.stream()
            .map(coords -> new PointGeometry(new LonLat(coords.getFirst(), coords.get(1))))
            .collect(Collectors.toList())))
        .collect(Collectors.toList());
  }

  @JsonIgnore
  public void enrichLines(List<LineGeometry> lines) {
    this.coordinates =
        lines.stream().map(LineGeometry::getCoordinates).collect(Collectors.toList());
  }
}
