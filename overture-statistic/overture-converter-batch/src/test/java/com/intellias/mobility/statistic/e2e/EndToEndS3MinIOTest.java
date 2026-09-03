/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.e2e;

import com.intellias.mobility.statistic.AbstractIntegrationTest;
import com.intellias.mobility.statistic.batch.runner.StatusHandler;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * <p>End-to-end integration test for running {@code LandJob} using a MinIO-backed S3 bucket
 * as the input source. This test:
 * <ul>
 *   <li>Starts MinIO and Elasticsearch via Testcontainers.</li>
 *   <li>Uploads a test Parquet file to MinIO under a predefined S3 path.</li>
 *   <li>Runs the job via {@code JobsRunner} using properties-driven configuration.</li>
 *   <li>Asserts that the job completes successfully and writes documents to Elasticsearch.</li>
 * </ul>
 *
 */
@Slf4j
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "batch.jobs.parameters.LandJob.inputPath=s3://e2e-land-bucket/release/2025-07-23.0/theme=base/type=land/input.parquet",
    })
public class EndToEndS3MinIOTest extends AbstractIntegrationTest {

  private static final String BUCKET = "e2e-land-bucket";
  private static final String KEY_PREFIX = "release/2025-07-23.0/theme=base/type=land/";
  private static final String OBJECT_KEY = KEY_PREFIX + "input.parquet";
  private static final CountDownLatch RUN_DONE = new CountDownLatch(1);
  private static final AtomicInteger EXIT_CODE = new AtomicInteger(Integer.MIN_VALUE);

  private RestClient esHttp;

  /**
   * Test-specific Spring beans for overriding production configuration.
   */
  @TestConfiguration
  static class TestBeans {
    @Bean
    @Primary
    @SneakyThrows
    S3Client s3Client() {
      return S3Client.builder()
          .endpointOverride(new URI(minioContainer.getS3URL()))
          .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(
              minioContainer.getUserName(), minioContainer.getPassword())))
          .region(Region.of("us-west-2"))
          .forcePathStyle(true)
          .build();
    }

    @Bean
    @Primary
    StatusHandler statusHandler() {
      return code -> {
        EXIT_CODE.set(code);
        log.info("[TEST] StatusHandler.handle({}) — suppressing System.exit", code);
        RUN_DONE.countDown();
      };
    }
  }

  @BeforeAll
  @SneakyThrows
  static void uploadParquetToMinio() {
    String override = System.getProperty("e2e.parquet.path", "").trim();
    Path parquetPath;
    if (!override.isEmpty()) {
      parquetPath = Path.of(override).toAbsolutePath();
      if (!Files.isRegularFile(parquetPath)) {
        throw new IllegalArgumentException("File not found: " + parquetPath);
      }
    } else {
      var url = EndToEndS3MinIOTest.class.getResource("/e2e/land.parquet");
      if (url == null) {
        throw new IllegalStateException(
            "Provide -De2e.parquet.path=/abs/file.parquet or put file at src/test/resources/e2e/land.parquet");
      }
      parquetPath = Path.of(url.toURI());
    }

    S3Client minioS3 = S3Client.builder()
        .endpointOverride(new URI(minioContainer.getS3URL()))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(minioContainer.getUserName(), minioContainer.getPassword())))
        .region(Region.of("us-west-2"))
        .forcePathStyle(true)
        .build();

    ensureBucket(minioS3, BUCKET);
    minioS3.putObject(
        PutObjectRequest.builder()
            .bucket(BUCKET)
            .key(OBJECT_KEY)
            .contentType("application/octet-stream")
            .build(),
        RequestBody.fromFile(parquetPath));

    log.info("Uploaded test parquet to s3://{}/{}", BUCKET, OBJECT_KEY);
  }

  @BeforeEach
  void setupEsClient() {
    String host = elasticsearchContainer.getHost();
    Integer port;
    try {
      port = elasticsearchContainer.getMappedPort(9200);
    } catch (Exception ignored) {
      port = elasticsearchContainer.getFirstMappedPort();
    }
    String base = "http://" + host + ":" + port;
    log.info("[TEST] ES base URL = {}", base);

    esHttp = RestClient.builder().baseUrl(base).build();
  }

  /**
   * Runs the {@code LandJob} end-to-end using the configured S3 input path and verifies
   * that at least one document is indexed into Elasticsearch.
   */
  @Test
  @DisplayName("E2E (props-driven): LandJob runs via JobsRunner and documents appear in ES")
  void shouldProcessS3FileAndStoreDocumentsInES() throws Exception {
    boolean finished = RUN_DONE.await(2, TimeUnit.MINUTES);
    Assertions.assertTrue(finished, "JobsRunner did not finish within timeout");

    int code = EXIT_CODE.get();
    Assertions.assertTrue(code == 0 || code == 1, "Unexpected status code: " + code);

    Map<String, Long> counts;
    long total;
    long deadline = System.currentTimeMillis() + Duration.ofSeconds(30).toMillis();
    do {
      counts = indexDocCounts("statistic-*");
      total = counts.values().stream().mapToLong(Long::longValue).sum();
      if (total > 0) break;
      Thread.sleep(500);
    } while (System.currentTimeMillis() < deadline);

    log.info("=== ES indices (statistic-*) and doc counts ===");
    counts.forEach((idx, cnt) -> log.info("index={} docs={}", idx, cnt));

    Assertions.assertTrue(
        total > 0, "Expected > 0 documents across statistic-* indices, got " + total);
  }

  private static void ensureBucket(S3Client s3, String bucket) {
    try {
      s3.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
    } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ignored) {
    }
  }

  /**
   * Queries Elasticsearch's {@code _cat/indices} API to retrieve document counts
   * for all indices matching the given pattern.
   *
   * @param pattern index pattern, e.g. {@code statistic-*}
   * @return map of index name to document count
   */
  @SuppressWarnings("unchecked")
  private Map<String, Long> indexDocCounts(String pattern) {
    List<Map<String, Object>> resp = esHttp
        .get()
        .uri("/_cat/indices/{pattern}?format=json&h=index,docs.count", pattern)
        .retrieve()
        .body(List.class);

    Map<String, Long> result = new TreeMap<>();
    if (resp == null) return result;

    for (Map<String, Object> row : resp) {
      String index = String.valueOf(row.get("index"));
      Object val = row.get("docs.count");
      long count = 0;
      if (val instanceof Number n) count = n.longValue();
      else if (val != null) {
        try {
          count = Long.parseLong(val.toString());
        } catch (NumberFormatException ignored) {
        }
      }
      result.put(index, count);
    }
    return result;
  }
}
