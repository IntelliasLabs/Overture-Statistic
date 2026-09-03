/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.attribute;

import com.intellias.statistic.model.geometry.MultiLineGeometry;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RangeAttributeValue {
  private String value;
  private List<Range> ranges;
  private Double lengthMeters;
  private MultiLineGeometry geometry;

  public RangeAttributeValue(String value, List<Range> ranges) {
    this.value = value;
    this.ranges = ranges;
  }
}
