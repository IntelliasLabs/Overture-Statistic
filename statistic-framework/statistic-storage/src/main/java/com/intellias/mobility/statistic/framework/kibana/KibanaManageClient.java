/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.kibana;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

@RequiredArgsConstructor
public class KibanaManageClient {
  private final RestClient restClient;

  @SneakyThrows
  public Response performWithBody(String method, String endpoint, String body) {
    Request request = new Request(method, endpoint);
    request.setJsonEntity(body);
    return restClient.performRequest(request);
  }
}
