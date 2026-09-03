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
 * Polygon geometry.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("Polygon")
public class PolygonGeometry implements StatisticGeometry {

  @JsonProperty("type")
  private final String type = "Polygon";

  @JsonProperty("coordinates")
  private List<List<List<Double>>> coordinates = new ArrayList<>();

  public PolygonGeometry(LineGeometry outerRing, List<LineGeometry> innerRings) {
    this.coordinates = new ArrayList<>();
    // Add outer ring
    this.coordinates.add(outerRing.getCoordinates());
    // Add inner rings
    if (innerRings != null) {
      innerRings.forEach(ring -> this.coordinates.add(ring.getCoordinates()));
    }
  }

  @JsonIgnore
  public LineGeometry getOuterRing() {
    if (coordinates.isEmpty()) {
      return new LineGeometry();
    }
    return new LineGeometry(coordinates.getFirst().stream()
        .map(coords -> new PointGeometry(new LonLat(coords.getFirst(), coords.get(1))))
        .collect(Collectors.toList()));
  }

  @JsonIgnore
  public void setOuterRing(LineGeometry outerRing) {
    if (coordinates.isEmpty()) {
      coordinates.add(new ArrayList<>());
    }
    coordinates.set(0, outerRing.getCoordinates());
  }

  @JsonIgnore
  public List<LineGeometry> getInnerRings() {
    if (coordinates.size() <= 1) {
      return new ArrayList<>();
    }
    return coordinates.subList(1, coordinates.size()).stream()
        .map(ring -> new LineGeometry(ring.stream()
            .map(coords -> new PointGeometry(new LonLat(coords.getFirst(), coords.get(1))))
            .collect(Collectors.toList())))
        .collect(Collectors.toList());
  }

  @JsonIgnore
  public void setInnerRings(List<LineGeometry> innerRings) {
    List<List<List<Double>>> updatedCoordinates = new ArrayList<>();
    if (!coordinates.isEmpty()) {
      updatedCoordinates.add(coordinates.getFirst());
    }
    if (innerRings != null) {
      innerRings.forEach(ring -> updatedCoordinates.add(ring.getCoordinates()));
    }
    this.coordinates = updatedCoordinates;
  }
}
