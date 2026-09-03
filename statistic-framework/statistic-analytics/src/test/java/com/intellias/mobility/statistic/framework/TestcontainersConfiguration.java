/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework;

import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.rnorth.ducttape.TimeoutException;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@Testcontainers
public abstract class TestcontainersConfiguration {

  static final ElasticsearchContainer elastic;
  static final GenericContainer<?> kibana;
  static final Network network = Network.newNetwork();

  static {
    elastic = new ElasticsearchContainer(
            DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.15.3"))
        .withNetwork(network)
        .withNetworkAliases("elasticsearch")
        .withEnv("discovery.type", "single-node")
        .withEnv("xpack.security.enabled", "false")
        .withEnv("xpack.security.http.ssl.enabled", "false")
        .withEnv("xpack.security.transport.ssl.enabled", "false")
        .withStartupTimeout(Duration.ofMinutes(3));

    elastic.start();

    kibana = new GenericContainer<>(DockerImageName.parse("docker.elastic.co/kibana/kibana:8.15.3"))
        .withExposedPorts(5601)
        .withNetwork(network)
        .withNetworkAliases("kibana")
        .withEnv("ELASTICSEARCH_HOSTS", "http://elasticsearch:9200")
        .dependsOn(elastic)
        .waitingFor(waitStrategy().withStartupTimeout(Duration.ofMinutes(2)));

    kibana.start();
  }

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("statistic-app.elastic.base-url", elastic::getHttpHostAddress);
    String kibanaUrl = kibana.getHost() + ":" + kibana.getMappedPort(5601);
    registry.add("statistic-app.kibana.base-url", () -> kibanaUrl);
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

  protected static AbstractWaitStrategy waitStrategy() {
    return new AbstractWaitStrategy() {
      @Override
      protected void waitUntilReady() {
        String kibanaUrl = "http://"
            + waitStrategyTarget.getHost()
            + ":"
            + waitStrategyTarget.getMappedPort(5601)
            + "/api/status";

        RestTemplate restTemplate = new RestTemplate();
        try {
          Unreliables.retryUntilTrue((int) startupTimeout.getSeconds(), TimeUnit.SECONDS, () -> {
            ResponseEntity<String> response = restTemplate.getForEntity(kibanaUrl, String.class);
            JSONObject jsonResponse = new JSONObject(response.getBody());

            var serverState =
                jsonResponse.getJSONObject("status").getJSONObject("overall").getString("level");

            if ("available".equalsIgnoreCase(serverState)) {
              return true;
            } else {
              log.info("⏳ Kibana answer is not good enough:{}", serverState);
              return false;
            }
          });
        } catch (TimeoutException e) {
          throw new ContainerLaunchException("Timed out waiting for container to become healthy");
        }
      }
    };
  }
}
