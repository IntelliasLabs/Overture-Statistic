/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor.base;

import org.springframework.stereotype.Component;

@Component
public class LandUseOvertureProcessor extends BaseThemeOvertureProcessor {
  @Override
  public String getProcessorName() {
    return "LandUse";
  }
}
