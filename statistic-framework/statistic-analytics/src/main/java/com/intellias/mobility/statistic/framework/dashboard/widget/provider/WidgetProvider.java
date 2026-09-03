/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard.widget.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.dashboard.widget.KibanaWidget;
import java.util.List;

public interface WidgetProvider {

  KibanaWidget buildWidget(List<String> indexes, String field, ObjectMapper objectMapper);

  String getName();
}
