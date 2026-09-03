/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework;

import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class TestcontainersConfiguration {

  static final ElasticsearchContainer container;

  static {
    container = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.15.3"))
        .withEnv("discovery.type", "single-node")
        .withEnv("xpack.security.enabled", "false")
        .withEnv("xpack.security.http.ssl.enabled", "false")
        .withEnv("xpack.security.transport.ssl.enabled", "false")
        .withStartupTimeout(Duration.ofMinutes(3));

    container.start();
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("statistic-app.elastic.base-url", container::getHttpHostAddress);
  }

  /**
   * Elasticsearch's indexing process has not completed by the time your test executes the search query.
   * This is due to Elasticsearch's asynchronous nature, where indexing operations (like elasticsearchOperations.save())
   * do not guarantee immediate availability of the document for search.
   *
   * <p>
   * <b>Why is this happening?</b>
   * <ul>
   *   <li><b>Asynchronous Indexing:</b>
   *   <br>Elasticsearch writes data to the transaction log and memory before committing it to the index.
   *   This process can take a moment, especially in a containerized environment.</li>
   *
   *   <li><b>Refresh Interval:</b>
   *   <br>Elasticsearch indexes become available for search after a refresh operation.
   *   By default, the refresh interval is set to <b>1 second</b> (<code>index.refresh_interval</code>),
   *   meaning a document might not be visible for search until the next refresh.</li>
   *
   *   <li><b>Test Environment Constraints:</b>
   *   <br>In tests, the Elasticsearch container may be slower to process indexing operations due to resource constraints
   *   or slower disk I/O.</li>
   * </ul>
   * </p>
   *
   * <p>
   * <b>Solution:</b>
   * This method forces Elasticsearch to refresh the index immediately after saving the document.
   * This ensures the document is available for search in tests.
   * </p>
   */
  protected static void refreshIndex(
      String index, ElasticsearchOperations elasticsearchOperations) {
    elasticsearchOperations.indexOps(IndexCoordinates.of(index)).refresh();
  }

  protected static void deleteAllIndexes(ElasticManageClient elasticManageClient) {
    var indexesStr = elasticManageClient.performString("GET", "/_cat/indices?h=index&index=*");
    Arrays.stream(indexesStr.split("\n"))
        .map(indexName -> indexName.replace("\r", ""))
        .filter(indexName -> !indexName.startsWith("."))
        .forEach(indexName -> elasticManageClient.performString("DELETE", "/" + indexName));
  }
}
