/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.kibana;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.client.Response;

@RequiredArgsConstructor
public class KibanaManager {

  private final KibanaManageClient kibanaManageClient;

  public String createAndReturnString(String body) {
    var response =
        kibanaManageClient.performWithBody("POST", "/api/content_management/rpc/create", body);
    return responceBodyToString(response);
  }

  public String updateAndReturnString(String body) {
    var response =
        kibanaManageClient.performWithBody("POST", "/api/content_management/rpc/update", body);
    return responceBodyToString(response);
  }

  @SneakyThrows
  private String responceBodyToString(Response response) {
    return new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
  }
}
