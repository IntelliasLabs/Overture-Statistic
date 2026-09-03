/**
 Copyright ©2024-2025 Intellias
 */
package com.intellias.mobility.statistic.framework;

import com.intellias.mobility.statistic.framework.ingres.IngresProperties;
import com.intellias.mobility.statistic.framework.ingres.IngresService;
import com.intellias.mobility.statistic.framework.ingres.IngresServiceImpl;
import com.intellias.mobility.statistic.framework.preprocess.DerivedDocumentMaterializer;
import com.intellias.mobility.statistic.framework.preprocess.PreProcessService;
import com.intellias.mobility.statistic.framework.preprocess.PreProcessor;
import com.intellias.mobility.statistic.framework.preprocess.impl.*;
import com.intellias.mobility.statistic.framework.range.RangeAttributeIndexTemplateCreator;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import com.intellias.mobility.statistic.framework.storage.StorageService;
import com.intellias.mobility.statistic.framework.templates.IndexTemplateCreator;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring beans configuration for the ingress component.
 */
@EnableConfigurationProperties(IngresProperties.class)
@Configuration
public class IngresConfiguration {

  /** Creates default {@link PolygonPreProcessor} if none defined. */
  @ConditionalOnMissingBean(PolygonPreProcessor.class)
  @Bean
  PolygonPreProcessor polygonPreProcessor() {
    return new PolygonPreProcessor();
  }

  /** Creates default {@link MultiPolygonPreProcessor} if none defined. */
  @ConditionalOnMissingBean(MultiPolygonPreProcessor.class)
  @Bean
  MultiPolygonPreProcessor multiPolygonPreProcessor() {
    return new MultiPolygonPreProcessor();
  }

  /** Creates default {@link LinePreProcessor} if none defined. */
  @ConditionalOnMissingBean(LinePreProcessor.class)
  @Bean
  LinePreProcessor linePreProcessor() {
    return new LinePreProcessor();
  }

  /** Creates default {@link MultiLinePreProcessor} if none defined. */
  @ConditionalOnMissingBean(MultiLinePreProcessor.class)
  @Bean
  MultiLinePreProcessor multiLinePreProcessor() {
    return new MultiLinePreProcessor();
  }

  /** Aggregates all available preprocessors into a service. */
  @Bean
  PreProcessService preProcessService(
      List<PreProcessor> preProcessors,
      List<DerivedDocumentMaterializer> derivedDocumentMaterializers) {
    return new PreProcessService(preProcessors, derivedDocumentMaterializers);
  }

  @Bean
  RangeAttributeDocumentMaterializer rangeAttributeDocumentMaterializer(
      StorageProperties storageProperties) {
    return new RangeAttributeDocumentMaterializer(storageProperties);
  }

  @Bean
  @ConditionalOnMissingBean(name = "rangeAttributeIndexTemplateCreator")
  IndexTemplateCreator rangeAttributeIndexTemplateCreator(StorageProperties storageProperties) {
    return new RangeAttributeIndexTemplateCreator(storageProperties);
  }

  /** Provides the ingress service implementation. */
  @Bean
  IngresService ingresService(PreProcessService preProcessService, StorageService storageService) {
    return new IngresServiceImpl(preProcessService, storageService);
  }
}
