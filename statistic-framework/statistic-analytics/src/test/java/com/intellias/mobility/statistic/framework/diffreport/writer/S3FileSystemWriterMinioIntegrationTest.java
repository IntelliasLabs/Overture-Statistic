/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.writer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@Testcontainers
class S3FileSystemWriterMinioIntegrationTest {

  private static final String ACCESS_KEY = "minioadmin";
  private static final String SECRET_KEY = "minioadmin";
  private static final String BUCKET = "diff-report-test";

  @Container
  static final GenericContainer<?> minio = new GenericContainer<>(
          DockerImageName.parse("minio/minio:RELEASE.2025-09-07T16-13-09Z"))
      .withExposedPorts(9000)
      .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
      .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
      .withCommand("server", "/data");

  @BeforeAll
  static void ensureBucket() {
    try (var s3Client = createClient()) {
      try {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build());
      } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException ignored) {
      }
    }
  }

  @Test
  void shouldWriteSmallObjectToMinio() throws Exception {
    var key = "small/" + UUID.randomUUID() + ".json";

    try (var s3Client = createClient();
        var outputStream = new S3FileSystemWriter(() -> s3Client)
            .openStream(
                new DiffReportDestination("s3://" + BUCKET + "/reports", "per-feature", key))) {
      outputStream.write("{\"status\":\"ok\"}".getBytes(UTF_8));
    }

    try (var s3Client = createClient()) {
      var objectBytes = s3Client.getObjectAsBytes(
          builder -> builder.bucket(BUCKET).key("reports/per-feature/" + key));
      assertEquals("{\"status\":\"ok\"}", objectBytes.asUtf8String());
    }
  }

  @Test
  void shouldWriteMultipartObjectToMinio() throws Exception {
    var key = "multipart/" + UUID.randomUUID() + ".json";
    var payload = "1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    try (var s3Client = createClient();
        var outputStream = new S3FileSystemWriter(() -> s3Client, 5)
            .openStream(new DiffReportDestination(
                "s3://" + BUCKET + "/reports", "per-feature-type", key))) {
      outputStream.write(payload.getBytes(UTF_8));
    }

    try (var s3Client = createClient()) {
      var objectBytes = s3Client.getObjectAsBytes(
          builder -> builder.bucket(BUCKET).key("reports/per-feature-type/" + key));
      assertEquals(payload, objectBytes.asUtf8String());
    }
  }

  private static S3Client createClient() {
    return S3Client.builder()
        .endpointOverride(URI.create("http://" + minio.getHost() + ":" + minio.getMappedPort(9000)))
        .credentialsProvider(
            StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
        .region(Region.of("us-east-1"))
        .forcePathStyle(true)
        .build();
  }
}
