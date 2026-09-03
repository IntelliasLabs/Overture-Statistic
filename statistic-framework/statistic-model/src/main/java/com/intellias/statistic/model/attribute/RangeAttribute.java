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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RangeAttribute {
  private String key;
  private List<RangeAttributeValue> values;
  private Double lengthMeters;
  private MultiLineGeometry geometry;

  public RangeAttribute(String key, List<RangeAttributeValue> values) {
    this.key = key;
    this.values = values;
  }

  public RangeAttribute(String key, List<RangeAttributeValue> values, Double lengthMeters) {
    this.key = key;
    this.values = values;
    this.lengthMeters = lengthMeters;
  }
}
