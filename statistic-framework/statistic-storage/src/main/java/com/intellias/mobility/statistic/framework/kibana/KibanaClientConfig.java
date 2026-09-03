/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.kibana;

import com.intellias.mobility.statistic.framework.elastic.ElasticProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.support.HttpHeaders;

@ConditionalOnProperty(prefix = "statistic-app.kibana", name = "base-url")
@ConditionalOnClass({ElasticsearchConfiguration.class, ElasticProperties.class})
@Configuration
@EnableConfigurationProperties(value = {ElasticProperties.class, KibanaProperties.class})
@RequiredArgsConstructor
public class KibanaClientConfig {

  private final ElasticProperties elasticProperties;
  private final KibanaProperties kibanaProperties;

  @Bean
  public KibanaManager kibanaManager() {
    var clientConfiguration = ClientConfiguration.builder()
        .connectedTo(kibanaProperties.baseUrl())
        .withHeaders(() -> {
          HttpHeaders headers = new HttpHeaders();
          headers.add("Authorization", "ApiKey " + elasticProperties.token());
          headers.add("Content-Type", "application/json");
          headers.add("kbn-xsrf", "true");
          return headers;
        })
        .build();

    KibanaManageClient kibanaManageClient =
        new KibanaManageClient(ElasticsearchClients.getRestClient(clientConfiguration));
    return new KibanaManager(kibanaManageClient);
  }
}
