/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.base;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class LandOvertureProcessor extends BaseThemeOvertureProcessor {
  @Override
  public String getProcessorName() {
    return "Land";
  }

  @Override
  protected boolean validateGeometry(@NotNull OvertureItem item) {
    return true;
  }
}
