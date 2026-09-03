/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(
    properties = {
      "spring.batch.job.enabled=false",
      "spring.main.allow-bean-definition-overriding=true"
    })
@Testcontainers
public abstract class AbstractIntegrationTest {

  @Container
  public static MinIOContainer minioContainer =
      new MinIOContainer("minio/minio:RELEASE.2025-07-23T15-54-02Z");

  @Container
  public static ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer(
          DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.15.3"))
      .withEnv("discovery.type", "single-node")
      .withEnv("xpack.security.enabled", "false");

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("statistic-app.elastic.base-url", elasticsearchContainer::getHttpHostAddress);
  }
}
