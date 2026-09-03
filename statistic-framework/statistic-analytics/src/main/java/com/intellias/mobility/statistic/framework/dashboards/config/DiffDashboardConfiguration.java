/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.dashboards.dashboard.DashboardService;
import com.intellias.mobility.statistic.framework.dashboards.dataview.DataViewService;
import com.intellias.mobility.statistic.framework.dashboards.kibana.KibanaApiService;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationBuilder;
import com.intellias.mobility.statistic.framework.dashboards.visualization.VisualizationService;
import com.intellias.mobility.statistic.framework.elastic.ElasticProperties;
import com.intellias.mobility.statistic.framework.kibana.KibanaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(value = {KibanaProperties.class, ElasticProperties.class})
public class DiffDashboardConfiguration {

  private final ObjectMapper objectMapper;
  private final KibanaProperties kibanaProperties;
  private final ElasticProperties elasticProperties;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  VisualizationBuilder visualizationBuilder() {
    return new VisualizationBuilder();
  }

  @Bean
  KibanaApiService kibanaApiService() {
    return new KibanaApiService(restTemplate(), objectMapper, kibanaProperties, elasticProperties);
  }

  @Bean
  DashboardService dashboardService() {
    return new DashboardService(kibanaApiService(), objectMapper);
  }

  @Bean
  VisualizationService visualizationService() {
    return new VisualizationService(
        visualizationBuilder(), dashboardService(), kibanaApiService(), objectMapper);
  }

  @Bean
  DataViewService dataViewService() {
    return new DataViewService(kibanaApiService(), objectMapper);
  }
}
