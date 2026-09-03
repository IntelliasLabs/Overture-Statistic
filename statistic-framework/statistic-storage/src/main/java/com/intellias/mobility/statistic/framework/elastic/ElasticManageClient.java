/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

@RequiredArgsConstructor
public class ElasticManageClient {
  private final RestClient restClient;
  private final ObjectMapper objectMapper; // Inject Jackson's ObjectMapper for JSON parsing

  @SneakyThrows
  public Response perform(String method, String endpoint, String body) {
    Request request = new Request(method, endpoint);
    request.setJsonEntity(body);
    return restClient.performRequest(request);
  }

  @SneakyThrows
  public Response perform(String method, String endpoint) {
    Request request = new Request(method, endpoint);
    return restClient.performRequest(request);
  }

  @SneakyThrows
  public <T> T perform(String method, String endpoint, Class<T> clazz) {
    Response response = perform(method, endpoint);
    try (var inputStream = response.getEntity().getContent()) {
      return objectMapper.readValue(inputStream, clazz);
    }
  }

  @SneakyThrows
  public <T> T perform(String method, String endpoint, String body, Class<T> clazz) {
    Response response = perform(method, endpoint, body);
    try (var inputStream = response.getEntity().getContent()) {
      return objectMapper.readValue(inputStream, clazz);
    }
  }

  @SneakyThrows
  public String performString(String method, String endpoint) {
    return inputStreamToString(perform(method, endpoint).getEntity().getContent());
  }

  @SneakyThrows
  public String inputStreamToString(InputStream inputStream) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }
}
