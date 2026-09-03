/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.writer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

class S3FileSystemWriterTest {

  @Test
  void shouldUploadPayloadToResolvedBucketAndKey() throws Exception {
    var s3Client = mock(S3Client.class);
    var writer = new S3FileSystemWriter(() -> s3Client);

    try (var outputStream = writer.openStream(
        new DiffReportDestination("s3://diff-reports/prefix", "per-feature", "report.json"))) {
      outputStream.write("payload".getBytes(UTF_8));
    }

    var requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
    var bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

    verify(s3Client).putObject(requestCaptor.capture(), bodyCaptor.capture());

    assertEquals("diff-reports", requestCaptor.getValue().bucket());
    assertEquals("prefix/per-feature/report.json", requestCaptor.getValue().key());
    assertArrayEquals(
        "payload".getBytes(UTF_8),
        bodyCaptor.getValue().contentStreamProvider().newStream().readAllBytes());
  }

  @Test
  void shouldUseMultipartUploadForLargePayloads() throws Exception {
    var s3Client = mock(S3Client.class);
    when(s3Client.createMultipartUpload(any(CreateMultipartUploadRequest.class)))
        .thenReturn(CreateMultipartUploadResponse.builder().uploadId("upload-1").build());
    when(s3Client.uploadPart(any(UploadPartRequest.class), any(RequestBody.class)))
        .thenReturn(
            UploadPartResponse.builder().eTag("etag-1").build(),
            UploadPartResponse.builder().eTag("etag-2").build());

    var writer = new S3FileSystemWriter(() -> s3Client, 5);

    try (var outputStream = writer.openStream(
        new DiffReportDestination("s3://diff-reports/prefix", "per-feature", "report.json"))) {
      outputStream.write("12345".getBytes(UTF_8));
      outputStream.write("678".getBytes(UTF_8));
    }

    var uploadRequestCaptor = ArgumentCaptor.forClass(UploadPartRequest.class);
    var requestBodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
    var completeRequestCaptor = ArgumentCaptor.forClass(CompleteMultipartUploadRequest.class);

    verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    verify(s3Client, times(2))
        .uploadPart(uploadRequestCaptor.capture(), requestBodyCaptor.capture());
    verify(s3Client).completeMultipartUpload(completeRequestCaptor.capture());

    var uploadRequests = uploadRequestCaptor.getAllValues();
    var requestBodies = requestBodyCaptor.getAllValues();

    assertEquals(1, uploadRequests.get(0).partNumber());
    assertEquals(2, uploadRequests.get(1).partNumber());
    assertArrayEquals(
        "12345".getBytes(UTF_8),
        requestBodies.get(0).contentStreamProvider().newStream().readAllBytes());
    assertArrayEquals(
        "678".getBytes(UTF_8),
        requestBodies.get(1).contentStreamProvider().newStream().readAllBytes());

    var completedParts = completeRequestCaptor.getValue().multipartUpload().parts();
    assertEquals(
        List.of(1, 2), completedParts.stream().map(part -> part.partNumber()).toList());
  }

  @Test
  void shouldResolveBucketOnlyPath() {
    var writer = new S3FileSystemWriter(() -> mock(S3Client.class));
    var location = writer.resolveLocation(
        new DiffReportDestination("s3://diff-reports", "per-feature-type", "report.json"));

    assertEquals("diff-reports", location.bucket());
    assertEquals("per-feature-type/report.json", location.key());
  }
}
