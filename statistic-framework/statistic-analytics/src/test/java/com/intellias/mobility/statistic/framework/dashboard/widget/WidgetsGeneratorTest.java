/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard.widget;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.dashboard.widget.provider.CountHistogramProvider;
import com.intellias.mobility.statistic.framework.dashboard.widget.provider.CountProvider;
import com.intellias.mobility.statistic.framework.dashboard.widget.provider.GeoMapProvider;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class WidgetsGeneratorTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testWidgetsGenerator() {
    var widgetsGenerator = new WidgetsGenerator(
        objectMapper,
        List.of(new GeoMapProvider(), new CountProvider(), new CountHistogramProvider()));

    var widgetData = List.of(
        new String[] {"indexA", "geometry", "map"},
        new String[] {"indexA", "car", "count"},
        new String[] {"indexA", "cat", "countHistogram"},
        new String[] {"indexB", "cat", "countHistogram"});

    List<KibanaWidget> kibanaWidgets = widgetsGenerator.buildWidgets(widgetData);

    assertEquals(
        Set.of(
            new GeoMap(List.of("indexA"), "geometry", objectMapper),
            new CountHistogram(List.of("indexA"), "cat", objectMapper),
            new CountHistogram(List.of("indexB"), "cat", objectMapper),
            new Count(List.of("indexA"), "car", objectMapper)),
        new HashSet<>(kibanaWidgets));
  }
}
