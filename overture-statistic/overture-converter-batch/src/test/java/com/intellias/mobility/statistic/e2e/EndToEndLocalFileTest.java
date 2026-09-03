/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.e2e;

import com.intellias.mobility.statistic.AbstractIntegrationTest;
import com.intellias.mobility.statistic.batch.runner.StatusHandler;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

/**
 * End-to-end integration test verifying that the batch job
 * can process a local Parquet file and store results in Elasticsearch.
 *
 * <p>This test:
 * <ul>
 *   <li>Overrides the production {@link StatusHandler} to prevent System.exit
 *       and capture the exit code for assertions.</li>
 *   <li>Uses a {@link CountDownLatch} to wait until the batch job finishes.</li>
 *   <li>Verifies that the job exit code is either 0 (success) or 1 (partial success).</li>
 *   <li>Polls Elasticsearch until at least one document is indexed.</li>
 * </ul>
 *
 * <p>It is configured via {@link TestPropertySource} to run the {@code LandJob}
 * with a local test Parquet file.
 */
@Slf4j
@TestPropertySource(
    properties = {
      "batch.jobs.parameters.LandJob.inputPath=classpath:e2e/land.parquet",
    })
@ActiveProfiles("test")
public class EndToEndLocalFileTest extends AbstractIntegrationTest {

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
    StatusHandler statusHandler() {
      return code -> {
        EXIT_CODE.set(code);
        log.info("[TEST] StatusHandler.handle({}) — suppressing System.exit", code);
        RUN_DONE.countDown();
      };
    }
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
   * Runs the {@code LandJob} end-to-end with a local Parquet file
   * and asserts that documents are stored in Elasticsearch.
   *
   * @throws Exception if interrupted while waiting for job completion
   */
  @Test
  @DisplayName(
      "E2E (local-file): LandJob processes input file and stores documents in Elasticsearch")
  void shouldProcessLocalFileAndStoreDocumentsInES() throws Exception {
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
        total > 0, "Expected >0 documents across statistic-* indices, got " + total);
  }
  /**
   * Queries Elasticsearch's {@code _cat/indices} API to get document counts per index
   * matching the given pattern.
   *
   * @param pattern the index pattern, e.g. {@code statistic-*}
   * @return a map from index name to document count
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
