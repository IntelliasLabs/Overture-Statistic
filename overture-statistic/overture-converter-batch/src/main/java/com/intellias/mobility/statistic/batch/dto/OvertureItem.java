/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.dto;

import java.io.Serializable;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

@Data
@NoArgsConstructor
public class OvertureItem implements Serializable {
  private String id;
  private Geometry geometry;
  private String version;

  @EqualsAndHashCode.Exclude
  private Map<String, Object> properties;
}
