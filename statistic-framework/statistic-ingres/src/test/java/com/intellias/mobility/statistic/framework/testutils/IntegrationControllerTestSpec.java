/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.testutils;

import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class IntegrationControllerTestSpec extends TestcontainersConfiguration {
  @Autowired
  protected TestRestTemplate restTemplate;

  @Value(value = "${local.server.port}")
  protected int port;

  protected String buildUrl(String uri) {
    return String.format("http://localhost:%s/%s", port, uri);
  }

  protected HttpHeaders buildHeaders() {
    var mediaType = new MediaType("application", "json");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(mediaType);
    headers.setAccept(List.of(mediaType));

    return headers;
  }
}
