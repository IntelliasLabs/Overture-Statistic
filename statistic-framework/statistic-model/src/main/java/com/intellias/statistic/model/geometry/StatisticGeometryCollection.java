/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.geometry;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonTypeName("GeometryCollection")
public class StatisticGeometryCollection implements StatisticGeometry {

  @JsonProperty("type")
  private final String type = "GeometryCollection";

  private List<StatisticGeometry> geometries;

  public void addGeometry(StatisticGeometry statisticGeometry) {
    if (statisticGeometry != null) {
      this.geometries.add(statisticGeometry);
    }
  }

  public List<StatisticGeometry> getGeometries() {
    return new ArrayList<>(geometries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(type + ": [");
    for (StatisticGeometry statisticGeometry : geometries) {
      sb.append(statisticGeometry.toString()).append(", ");
    }
    if (!geometries.isEmpty()) {
      sb.setLength(sb.length() - 2); // Remove trailing comma and space
    }
    sb.append("]");
    return sb.toString();
  }
}
