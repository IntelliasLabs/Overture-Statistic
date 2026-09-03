/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.reader.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intellias.mobility.statistic.AbstractIntegrationTest;
import com.intellias.mobility.statistic.batch.runner.JobsRunner;
import com.intellias.mobility.statistic.framework.storage.StorageService;
import com.intellias.mobility.statistic.framework.templates.IndexTemplateService;
import com.intellias.mobility.statistic.infrastructure.reader.FileReaderFactory;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.parquet.io.InputFile;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Tag("integration")
public class S3IntegrationTest extends AbstractIntegrationTest {

  private static final String BUCKET_NAME = "reader";

  @Autowired
  private FileReaderFactory fileReaderFactory;

  @BeforeAll
  @SneakyThrows
  static void setupMinio() {
    MinioClient minioClient = MinioClient.builder()
        .endpoint(minioContainer.getS3URL())
        .credentials(minioContainer.getUserName(), minioContainer.getPassword())
        .build();

    minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
    assertTrue(
        minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build()));
    log.info("Bucket '{}' created successfully.", BUCKET_NAME);

    try (InputStream is =
        S3IntegrationTest.class.getClassLoader().getResourceAsStream("example.txt")) {
      minioClient.putObject(
          PutObjectArgs.builder().bucket(BUCKET_NAME).object("example.txt").stream(
                  is, Objects.requireNonNull(is).available(), -1)
              .build());
    }
    log.info("File 'example.txt' uploaded successfully to bucket '{}'.", BUCKET_NAME);
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

    @Bean
    @Primary
    @SneakyThrows
    public S3Client s3Client() {
      return S3Client.builder()
          .endpointOverride(new URI(minioContainer.getS3URL()))
          .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
              minioContainer.getUserName(), minioContainer.getPassword())))
          .region(Region.of("us-east-1"))
          .forcePathStyle(true)
          .build();
    }
  }

  @Test
  void testReadMinioFile() throws Exception {
    String path = "s3://reader/example.txt";
    log.info("Running integration test: testReadMinioFile with path={}", path);

    InputFile inputFile = fileReaderFactory.readFile(path);
    byte[] content = inputFile.newStream().readAllBytes();

    assertNotNull(content);
    log.info("S3 file content: '{}'", new String(content));
    assertTrue(new String(content).contains("Hello"), "File content mismatch");
  }
}
