/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.config.AnalyticsProperties;
import com.intellias.mobility.statistic.framework.diffreport.elastic.ElasticsearchService;
import com.intellias.mobility.statistic.framework.diffreport.service.DiffReportService;
import com.intellias.mobility.statistic.framework.diffreport.writer.FileSystemWriterFactory;
import com.intellias.mobility.statistic.framework.diffreport.writer.LocalFileSystemWriter;
import com.intellias.mobility.statistic.framework.diffreport.writer.S3FileSystemWriter;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(DiffReportProperties.class)
public class DiffReportConfiguration {

  private final ObjectMapper objectMapper;
  private final ElasticsearchOperations elasticsearchOperations;
  private final StorageProperties storageProperties;
  private final AnalyticsProperties analyticsProperties;
  private final DiffReportProperties diffReportProperties;

  @Bean
  ElasticsearchService elasticsearchService() {
    return new ElasticsearchService(
        elasticsearchOperations, storageProperties, analyticsProperties);
  }

  @Bean
  FileSystemWriterFactory fileSystemWriterFactory() {
    return new FileSystemWriterFactory(
        new LocalFileSystemWriter(), new S3FileSystemWriter(S3Client::create));
  }

  @Bean
  DiffReportService diffReportService() {
    return new DiffReportService(
        objectMapper, elasticsearchService(), diffReportProperties, fileSystemWriterFactory());
  }
}
