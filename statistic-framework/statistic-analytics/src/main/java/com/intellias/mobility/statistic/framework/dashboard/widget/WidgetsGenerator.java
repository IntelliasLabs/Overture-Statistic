/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboard.widget;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.intellias.mobility.statistic.framework.dashboard.DashboardGenerator;
import com.intellias.mobility.statistic.framework.dashboard.widget.provider.WidgetProvider;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(DashboardGenerator.class)
@RequiredArgsConstructor
public class WidgetsGenerator {

  private final ObjectMapper objectMapper;
  private final List<WidgetProvider> widgetProviders;

  public List<KibanaWidget> buildWidgets(List<String[]> widgetsData) {

    Map<String, WidgetProvider> providersMap = widgetProviders.stream()
        .collect(Collectors.toMap(WidgetProvider::getName, widgetProvider -> widgetProvider));

    return widgetsData.stream()
        .map(array ->
            providersMap.get(array[2]).buildWidget(List.of(array[0]), array[1], objectMapper))
        .toList();
  }

  public List<String[]> jsonToWidgetsData(JsonNode jsonNode) {
    // [index, field, widget]
    return iteratorToStream(jsonNode.fields())
        .filter(entry -> !entry.getKey().equals("name"))
        .flatMap(this::extractIndexWithData)
        .toList();
  }

  private Stream<String[]> extractIndexWithData(Entry<String, JsonNode> entry) {
    return iteratorToStream(entry.getValue().fields())
        .flatMap(this::extractFieldsWithWidgets)
        .peek(array -> array[0] = entry.getKey());
  }

  private Stream<String[]> extractFieldsWithWidgets(Entry<String, JsonNode> entryInternal) {
    ArrayNode value = (ArrayNode) entryInternal.getValue();
    return iteratorToStream(value.elements()).map(JsonNode::asText).map(widget ->
        new String[] {"", entryInternal.getKey(), widget});
  }

  private <T> Stream<T> iteratorToStream(Iterator<T> iter) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false);
  }

  @Bean
  public List<KibanaWidget> widgetsForGeneratedDashboard() {
    return List.of(
        new MultiGeoMap(List.of(), "", objectMapper),
        new MultiCountHistogram(List.of(), "", objectMapper),
        new MultiLenghtHistogram(List.of(), "", objectMapper));
  }
}
