/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.templates;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.intellias.mobility.statistic.framework.TestcontainersConfiguration;
import com.intellias.mobility.statistic.framework.elastic.model.IndexTemplatesResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.elasticsearch.client.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest()
@ActiveProfiles("test")
class DefaultIndexTemplateCreatorTest extends TestcontainersConfiguration {

  @Autowired
  private IndexTemplateManager indexTemplateManager;

  @DisplayName("Should create default index templates")
  @Test
  void createDefaultTemplateTest() {
    IndexTemplatesResponse resp = indexTemplateManager.getIndexTemplate("statistic-point");
    assertFalse(resp.index_templates().isEmpty());
  }

  @SneakyThrows
  private String getContent(Response res) {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(res.getEntity().getContent()))) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }
}
