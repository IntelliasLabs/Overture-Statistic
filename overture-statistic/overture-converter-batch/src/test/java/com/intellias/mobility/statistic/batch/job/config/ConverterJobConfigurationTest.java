/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellias.mobility.statistic.batch.reader.GeoParquetItemReader;
import com.intellias.mobility.statistic.infrastructure.reader.FileReaderFactory;
import org.apache.parquet.io.InputFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.configuration.annotation.StepScope;

/**
 * Unit tests for {@link ConverterJobConfiguration}.
 */
class ConverterJobConfigurationTest {

  @Test
  @DisplayName("Should create InputFile bean using FileReaderFactory")
  void shouldCreateInputFileUsingFactory() {
    // Given
    String path = "file:///tmp/test.parquet";
    FileReaderFactory readerFactory = mock(FileReaderFactory.class);
    InputFile expectedInputFile = mock(InputFile.class);
    when(readerFactory.readFile(path)).thenReturn(expectedInputFile);

    ConverterJobConfiguration configuration = new ConverterJobConfiguration();

    // When
    InputFile actual = configuration.inputFile(path, readerFactory);

    // Then
    assertThat(actual).isSameAs(expectedInputFile);
  }

  @Test
  @DisplayName("Should create GeoParquetItemReader with provided InputFile")
  void shouldCreateGeoParquetItemReader() {
    // Given
    InputFile inputFile = mock(InputFile.class);
    ConverterJobConfiguration configuration = new ConverterJobConfiguration();

    long startRow = 0L;
    long endRow = 999L;

    String version = "1.0";

    // When
    var reader = configuration.parquetItemReader(inputFile, startRow, endRow, version);

    // Then
    assertNotNull(reader, "Reader must not be null");
    assertThat(reader).isInstanceOf(GeoParquetItemReader.class);
  }

  @Test
  @DisplayName("Beans in configuration should be StepScoped")
  void beansShouldBeStepScoped() throws NoSuchMethodException {
    // Then
    assertThat(ConverterJobConfiguration.class
            .getMethod("inputFile", String.class, FileReaderFactory.class)
            .isAnnotationPresent(StepScope.class))
        .isTrue();

    assertThat(ConverterJobConfiguration.class
            .getMethod("parquetItemReader", InputFile.class, long.class, long.class, String.class)
            .isAnnotationPresent(StepScope.class))
        .isTrue();
  }
}
