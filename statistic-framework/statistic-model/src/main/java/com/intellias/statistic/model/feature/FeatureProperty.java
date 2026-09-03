/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.feature;

import java.util.List;
import lombok.*;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeatureProperty {
  private String key;
  private List<String> values;
}
