/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.geometry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lon Lat container.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LonLat {
  private double lon;
  private double lat;
}
