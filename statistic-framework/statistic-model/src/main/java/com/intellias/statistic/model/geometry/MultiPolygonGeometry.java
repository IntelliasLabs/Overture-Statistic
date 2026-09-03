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
 * MultiPolygon geometry.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("MultiPolygon")
public class MultiPolygonGeometry implements StatisticGeometry {

  @JsonProperty("type")
  private final String type = "MultiPolygon";

  @JsonProperty("coordinates")
  private List<List<List<List<Double>>>> coordinates = new ArrayList<>();

  public MultiPolygonGeometry(List<PolygonGeometry> polygons) {
    this.coordinates =
        polygons.stream().map(PolygonGeometry::getCoordinates).collect(Collectors.toList());
  }

  @JsonIgnore
  public List<PolygonGeometry> getPolygons() {
    return coordinates.stream()
        .map(polygonCoords -> new PolygonGeometry(
            new LineGeometry(polygonCoords.getFirst().stream()
                .map(coords -> new PointGeometry(new LonLat(coords.getFirst(), coords.get(1))))
                .collect(Collectors.toList())),
            polygonCoords.subList(1, polygonCoords.size()).stream()
                .map(innerCoords -> new LineGeometry(innerCoords.stream()
                    .map(coords -> new PointGeometry(new LonLat(coords.getFirst(), coords.get(1))))
                    .collect(Collectors.toList())))
                .collect(Collectors.toList())))
        .collect(Collectors.toList());
  }

  @JsonIgnore
  public void setPolygons(List<PolygonGeometry> polygons) {
    this.coordinates =
        polygons.stream().map(PolygonGeometry::getCoordinates).collect(Collectors.toList());
  }
}
