/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.dataview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.dashboards.dataview.model.DataView;
import com.intellias.mobility.statistic.framework.dashboards.dataview.model.DataViewCreationRequestBody;
import com.intellias.mobility.statistic.framework.dashboards.kibana.KibanaApiService;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class DataViewService {

  private final KibanaApiService kibanaApiService;
  private final ObjectMapper objectMapper;

  public String getDataViewIdOrCreate(String name, Set<String> indexes) {
    var dataViewIdOptional = getDataViewIdByNameAndIndexes(name, indexes);

    return dataViewIdOptional.orElseGet(() -> createDataView(name, indexes));
  }

  @SneakyThrows
  public String createDataView(String name, Set<String> indexes) {
    var title = String.join(",", indexes);
    var dataView = new DataView(name, title);

    var dataViewCreationRequestBody = new DataViewCreationRequestBody(dataView, true);

    var response = kibanaApiService.makePostRequestToKibanaApi(
        "/data_views/data_view", dataViewCreationRequestBody, String.class);
    return objectMapper.readTree(response).get("data_view").get("id").asText();
  }

  @SneakyThrows
  public Optional<String> getDataViewIdByNameAndIndexes(String name, Set<String> indexes) {
    var parameters = Map.of(
        "type", "index-pattern",
        "search_fields", "name",
        "search", name);

    var response = kibanaApiService.makeGetRequestToKibanaApi(
        "/saved_objects/_find", parameters, String.class);
    var idWithIndexes = extractAllIdWithTitle(response);

    return idWithIndexes.stream()
        .filter(res -> res.indexes.equals(indexes) && res.name.equals(name))
        .findFirst()
        .map(res -> res.id);
  }

  @SneakyThrows
  private List<Response> extractAllIdWithTitle(String response) {
    var rootNode = objectMapper.readTree(response);
    var savedObjects = rootNode.path("saved_objects");

    return StreamSupport.stream(savedObjects.spliterator(), false)
        .map(savedObject -> {
          var id = savedObject.get("id").asText();

          var attributes = savedObject.path("attributes");
          var name = attributes.get("name").asText();
          var title = attributes.get("title").asText();

          var indexes = Arrays.stream(title.split(",")).collect(Collectors.toSet());
          return new Response(id, name, indexes);
        })
        .toList();
  }

  private record Response(String id, String name, Set<String> indexes) {}
}
