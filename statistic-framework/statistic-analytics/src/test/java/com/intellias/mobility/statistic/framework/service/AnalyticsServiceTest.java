/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.config.AnalyticsProperties;
import com.intellias.mobility.statistic.framework.dashboard.DashboardGenerator;
import com.intellias.mobility.statistic.framework.dashboard.widget.Count;
import com.intellias.mobility.statistic.framework.dashboard.widget.CountHistogram;
import com.intellias.mobility.statistic.framework.dashboard.widget.GeoMap;
import com.intellias.mobility.statistic.framework.dashboard.widget.KibanaWidget;
import com.intellias.mobility.statistic.framework.dashboard.widget.WidgetsGenerator;
import com.intellias.mobility.statistic.framework.dashboard.widget.provider.CountHistogramProvider;
import com.intellias.mobility.statistic.framework.dashboard.widget.provider.CountProvider;
import com.intellias.mobility.statistic.framework.dashboard.widget.provider.GeoMapProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class AnalyticsServiceTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void testService() {
    var properties =
        new AnalyticsProperties(Optional.of("src/test/resources/widgets.json"), "", "");
    var widgetsGenerator = new WidgetsGenerator(
        objectMapper,
        List.of(new GeoMapProvider(), new CountProvider(), new CountHistogramProvider()));

    ArgumentCaptor<String> string1Captor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> string2Captor = ArgumentCaptor.forClass(String.class);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<KibanaWidget>> widgetCaptor = ArgumentCaptor.forClass(List.class);
    DashboardGenerator dashboardGenerator = mock(DashboardGenerator.class);

    var service =
        new AnalyticsService(objectMapper, properties, dashboardGenerator, widgetsGenerator);

    service.processJson();

    verify(dashboardGenerator)
        .buildDashboard(string1Captor.capture(), string2Captor.capture(), widgetCaptor.capture());

    var capturedIndex = string1Captor.getValue();
    var capturedDashboardName = string2Captor.getValue();
    var capturedList = widgetCaptor.getValue();

    assertEquals("statIndex", capturedIndex);
    assertEquals("Dashboard name", capturedDashboardName);
    assertEquals(
        Set.of(
            new GeoMap(List.of("statIndex"), "geometry", objectMapper),
            new CountHistogram(List.of("statIndex"), "roadType.keyword", objectMapper),
            new Count(List.of("statIndex"), "roadType.keyword", objectMapper)),
        new HashSet<>(capturedList));
  }
}
