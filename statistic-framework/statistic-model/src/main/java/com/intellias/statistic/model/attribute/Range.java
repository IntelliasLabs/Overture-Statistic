/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.attribute;

import com.intellias.statistic.model.geometry.LineGeometry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Range {
  private double start;
  private double end;
  private double lengthMeters;
  private LineGeometry geometry;

  public Range(double start, double end) {
    this.start = start;
    this.end = end;
  }

  public Range(double start, double end, double lengthMeters) {
    this.start = start;
    this.end = end;
    this.lengthMeters = lengthMeters;
  }
}
