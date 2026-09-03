/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.reader.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intellias.mobility.statistic.AbstractIntegrationTest;
import com.intellias.mobility.statistic.batch.runner.JobsRunner;
import com.intellias.mobility.statistic.framework.storage.StorageService;
import com.intellias.mobility.statistic.framework.templates.IndexTemplateService;
import com.intellias.mobility.statistic.infrastructure.reader.FileReaderFactory;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.apache.parquet.io.InputFile;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Slf4j
public class LocalFileIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private FileReaderFactory factory;

  private static final String RELATIVE_PATH = "src/test/resources/example.txt";

  @Test
  public void testReadFileWithRelativePath() throws Exception {
    log.info("Starting test: testReadFileWithRelativePath");

    InputFile inputFile = factory.readFile(RELATIVE_PATH);
    byte[] content = inputFile.newStream().readAllBytes();
    String text = new String(content, StandardCharsets.UTF_8);

    log.info("Relative file content: {}", text.substring(0, Math.min(text.length(), 100)));
    assertTrue(
        text.contains("Hello") || !text.isBlank(),
        "Relative file does not contain expected content");
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    @Primary
    public StorageService storageService() {
      return Mockito.mock(StorageService.class);
    }

    @Bean
    @Primary
    public IndexTemplateService indexTemplateService() {
      return Mockito.mock(IndexTemplateService.class);
    }

    @Bean
    @Primary
    public JobsRunner jobsRunner() {
      return Mockito.mock(JobsRunner.class);
    }
  }
}
