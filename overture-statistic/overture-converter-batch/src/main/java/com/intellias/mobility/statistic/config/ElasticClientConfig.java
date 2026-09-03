/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.config;

import com.intellias.mobility.statistic.framework.elastic.ElasticProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
@Primary
public class ElasticClientConfig extends ElasticsearchConfiguration {
  private final ElasticProperties elasticProperties;

  @Value("${statistic-app.elasticsearch.socket-timeout:30s}")
  private Duration socketTimeout;

  @Value("${statistic-app.elasticsearch.connection-timeout:10s}")
  private Duration connectionTimeout;

  @Override
  public ClientConfiguration clientConfiguration() {
    return ClientConfiguration.builder()
        .connectedTo(elasticProperties.baseUrl())
        .withHeaders(() -> {
          HttpHeaders headers = new HttpHeaders();
          headers.add("Authorization", "ApiKey " + elasticProperties.token());
          return headers;
        })
        .withConnectTimeout(connectionTimeout)
        .withSocketTimeout(socketTimeout)
        .build();
  }
}
