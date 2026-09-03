/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.dashboard.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attributes implements Serializable {
  private String title;
  private String panelsJSON;
  private String optionsJSON;
  private KibanaSavedObjectMeta kibanaSavedObjectMeta;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ControlGroupInput controlGroupInput;

  public Attributes(
      String title,
      String panelsJSON,
      String optionsJSON,
      KibanaSavedObjectMeta kibanaSavedObjectMeta) {
    this.title = title;
    this.panelsJSON = panelsJSON;
    this.optionsJSON = optionsJSON;
    this.kibanaSavedObjectMeta = kibanaSavedObjectMeta;
    this.controlGroupInput = null;
  }

  public void setControlGroupInput(ControlGroupInput controlGroupInput) {
    this.controlGroupInput = controlGroupInput;
  }
}
