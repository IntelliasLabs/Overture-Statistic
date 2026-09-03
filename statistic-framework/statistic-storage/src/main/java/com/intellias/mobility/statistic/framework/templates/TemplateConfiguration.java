/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.elastic.ElasticClientConfig;
import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnBean(ElasticClientConfig.class)
@Configuration
@RequiredArgsConstructor
public class TemplateConfiguration {
  private final RestClient restClient;

  @Bean
  ElasticManageClient elasticManageClient(ObjectMapper objectMapper) {
    return new ElasticManageClient(restClient, objectMapper);
  }

  @Bean
  IndexTemplateManager indexTemplateManager(ElasticManageClient elasticManageClient) {
    return new IndexTemplateManagerImpl(elasticManageClient);
  }

  @Bean
  IndexTemplateService indexTemplateService(
      List<IndexTemplateCreator> templateCreators, IndexTemplateManager indexTemplateManager) {
    return new IndexTemplateService(templateCreators, indexTemplateManager);
  }

  @Bean
  DefaultIndexTemplateCreator defaultIndexTemplateCreator(StorageProperties storageProperties) {
    return new DefaultIndexTemplateCreator(storageProperties);
  }
}
