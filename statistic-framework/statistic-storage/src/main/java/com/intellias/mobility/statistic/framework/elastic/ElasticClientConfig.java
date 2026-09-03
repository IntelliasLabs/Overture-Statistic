/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.elastic;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;

@ConditionalOnProperty(prefix = "statistic-app.elastic", name = "base-url")
@ConditionalOnClass({ElasticsearchConfiguration.class, ClientConfiguration.class})
@Configuration
@EnableConfigurationProperties(value = {ElasticProperties.class})
@EnableElasticsearchRepositories
@RequiredArgsConstructor
public class ElasticClientConfig extends ElasticsearchConfiguration {
  private final ElasticProperties elasticProperties;

  @Override
  public ClientConfiguration clientConfiguration() {
    return ClientConfiguration.builder()
        .connectedTo(elasticProperties.baseUrl())
        .withHeaders(() -> {
          HttpHeaders headers = new HttpHeaders();
          headers.add("Authorization", "ApiKey " + elasticProperties.token());
          return headers;
        })
        .withConnectTimeout(elasticProperties.connectionTimeout())
        .withSocketTimeout(elasticProperties.socketTimeout())
        .build();
  }
}
