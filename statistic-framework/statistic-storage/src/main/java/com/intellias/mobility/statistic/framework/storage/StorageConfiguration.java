/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.storage;

import com.intellias.mobility.statistic.framework.property.FeaturePropertyDocumentsBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
@Configuration
public class StorageConfiguration {
  private final StorageProperties storageProperties;

  @Bean
  @ConditionalOnBean({ElasticsearchOperations.class, FeaturePropertyDocumentsBuilder.class})
  StorageService elasticStorageService(
      ElasticsearchOperations elasticsearchOperations,
      FeaturePropertyDocumentsBuilder featurePropertyDocumentsBuilder) {
    return new StorageServiceImpl(
        elasticsearchOperations,
        featurePropertyDocumentsBuilder,
        storageProperties.indexPrefix(),
        storageProperties.featurePropertiesIndexSuffix(),
        storageProperties.batchSize());
  }
}
