/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.config;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.reader.GeoParquetItemReader;
import com.intellias.mobility.statistic.infrastructure.reader.FileReaderFactory;
import org.apache.parquet.io.InputFile;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConverterJobConfiguration {

  @Bean
  @StepScope
  public InputFile inputFile(
      @Value("#{stepExecutionContext['filePath']}") String path, FileReaderFactory readerFactory) {
    return readerFactory.readFile(path);
  }

  @Bean
  @StepScope
  public ItemStreamReader<OvertureItem> parquetItemReader(
      InputFile inputFile,
      @Value("#{stepExecutionContext['startRow']}") long startRow,
      @Value("#{stepExecutionContext['endRow']}") long endRow,
      @Value("#{jobParameters['version']}") String version) {
    return new GeoParquetItemReader(inputFile, startRow, endRow, version);
  }
}
