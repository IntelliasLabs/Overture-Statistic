/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic;

import java.time.Duration;
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
}
