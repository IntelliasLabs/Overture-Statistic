/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard.widget.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.dashboard.DashboardGenerator;
import com.intellias.mobility.statistic.framework.dashboard.widget.CountHistogram;
import com.intellias.mobility.statistic.framework.dashboard.widget.KibanaWidget;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(DashboardGenerator.class)
public class CountHistogramProvider implements WidgetProvider {

  @Override
  public KibanaWidget buildWidget(List<String> indexes, String field, ObjectMapper objectMapper) {
    return new CountHistogram(indexes, field, objectMapper);
  }

  @Override
  public String getName() {
    return "countHistogram";
  }
}
