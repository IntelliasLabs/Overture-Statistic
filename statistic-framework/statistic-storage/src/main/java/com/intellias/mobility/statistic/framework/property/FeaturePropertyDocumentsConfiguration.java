/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.property;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeaturePropertyDocumentsConfiguration {

  @Bean
  @ConditionalOnMissingBean(FeaturePropertyDocumentsBuilder.class)
  FeaturePropertyDocumentsBuilder featurePropertyDocumentsBuilder() {
    return new FeaturePropertyDocumentsBuilderImpl();
  }
}
