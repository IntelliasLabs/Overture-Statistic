/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.geometry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Point Geometry
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@JsonTypeName("Point")
public class PointGeometry implements StatisticGeometry {

  @JsonProperty("type")
  private final String type = "Point";

  @JsonProperty("coordinates")
  private List<Double> coordinates;

  public PointGeometry(LonLat lonLat) {
    this.coordinates = List.of(lonLat.getLon(), lonLat.getLat());
  }

  public PointGeometry(double lon, double lat) {
    this.coordinates = List.of(lon, lat);
  }

  @JsonIgnore
  public LonLat getLonLat() {
    if (coordinates == null || coordinates.size() < 2) {
      return null;
    }
    return new LonLat(coordinates.get(0), coordinates.get(1));
  }

  @JsonIgnore
  public void setLonLat(LonLat lonLat) {
    if (lonLat == null) {
      this.coordinates = null;
    } else {
      this.coordinates = List.of(lonLat.getLon(), lonLat.getLat());
    }
  }

  @JsonIgnore
  public double getLon() {
    return coordinates.getFirst();
  }

  @JsonIgnore
  public double getLat() {
    return coordinates.get(1);
  }
}
