/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.kibana;

import static co.elastic.clients.util.ContentType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.elastic.ElasticProperties;
import com.intellias.mobility.statistic.framework.kibana.KibanaProperties;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
public class KibanaApiService {

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final KibanaProperties kibanaProperties;
  private final ElasticProperties elasticProperties;

  public <T> T makePostRequestToKibanaApi(String url, Object body, Class<T> clazz) {
    return makeRequestToKibanaApi(url, body, clazz, HttpMethod.POST);
  }

  public <T> T makePutRequestToKibanaApi(String url, Object body, Class<T> clazz) {
    return makeRequestToKibanaApi(url, body, clazz, HttpMethod.PUT);
  }

  @SneakyThrows
  public <T> T makeGetRequestToKibanaApi(
      String url, Map<String, String> queryParams, Class<T> clazz) {
    var headers = buildHttpHeaders();
    HttpEntity<Serializable> entity = new HttpEntity<>(headers);

    String fullUrl = UriComponentsBuilder.fromUriString(buildUrl(url))
        .queryParams(convertMapToMultiValueMap(queryParams))
        .toUriString();

    ResponseEntity<T> response = restTemplate.exchange(fullUrl, HttpMethod.GET, entity, clazz);

    return response.getBody();
  }

  @SneakyThrows
  private <T> T makeRequestToKibanaApi(String url, Object body, Class<T> clazz, HttpMethod method) {
    HttpHeaders headers = buildHttpHeaders();
    HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

    ResponseEntity<T> response = restTemplate.exchange(buildUrl(url), method, entity, clazz);

    return response.getBody();
  }

  private HttpHeaders buildHttpHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.AUTHORIZATION, "ApiKey " + elasticProperties.token());
    headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
    headers.set("kbn-xsrf", "true");
    return headers;
  }

  private String buildUrl(String endPoint) {
    return String.format("http://%s/api%s", kibanaProperties.baseUrl(), endPoint);
  }

  private MultiValueMap<String, String> convertMapToMultiValueMap(Map<String, String> map) {
    return map.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> List.of(entry.getValue()),
            (existing, replacement) -> existing,
            LinkedMultiValueMap::new));
  }
}
