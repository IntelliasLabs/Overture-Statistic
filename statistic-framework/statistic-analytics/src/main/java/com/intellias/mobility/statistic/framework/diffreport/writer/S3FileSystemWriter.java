/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.writer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

public class S3FileSystemWriter implements FileSystemWriter {

  private static final int DEFAULT_PART_SIZE_BYTES = 8 * 1024 * 1024;

  private final Supplier<S3Client> s3ClientSupplier;
  private final int partSizeBytes;

  public S3FileSystemWriter(Supplier<S3Client> s3ClientSupplier) {
    this(s3ClientSupplier, DEFAULT_PART_SIZE_BYTES);
  }

  S3FileSystemWriter(Supplier<S3Client> s3ClientSupplier, int partSizeBytes) {
    this.s3ClientSupplier = s3ClientSupplier;
    this.partSizeBytes = partSizeBytes;
  }

  @Override
  public OutputStream openStream(DiffReportDestination destination) {
    var location = resolveLocation(destination);
    return new S3UploadOnCloseOutputStream(
        s3ClientSupplier.get(), location.bucket(), location.key(), partSizeBytes);
  }

  S3Location resolveLocation(DiffReportDestination destination) {
    var uri = URI.create(destination.basePath());
    var bucket = uri.getHost();
    if (!StringUtils.hasText(bucket)) {
      throw new IllegalArgumentException(
          "S3 diff-report output path must include a bucket: " + destination.basePath());
    }

    var prefix = trimSlashes(uri.getPath());
    var key = StringUtils.hasText(prefix)
        ? prefix + "/" + destination.relativePath()
        : destination.relativePath();
    return new S3Location(bucket, key);
  }

  private String trimSlashes(String value) {
    if (!StringUtils.hasText(value)) {
      return "";
    }

    var normalized = value;
    while (normalized.startsWith("/")) {
      normalized = normalized.substring(1);
    }
    while (normalized.endsWith("/")) {
      normalized = normalized.substring(0, normalized.length() - 1);
    }
    return normalized;
  }

  record S3Location(String bucket, String key) {}

  private static final class S3UploadOnCloseOutputStream extends OutputStream {

    private final ByteArrayOutputStream delegate = new ByteArrayOutputStream();
    private final S3Client s3Client;
    private final String bucket;
    private final String key;
    private final int partSizeBytes;
    private final List<CompletedPart> completedParts = new ArrayList<>();
    private boolean closed;
    private String uploadId;
    private int partNumber = 1;

    private S3UploadOnCloseOutputStream(
        S3Client s3Client, String bucket, String key, int partSizeBytes) {
      this.s3Client = s3Client;
      this.bucket = bucket;
      this.key = key;
      this.partSizeBytes = partSizeBytes;
    }

    @Override
    public void write(int b) {
      delegate.write(b);
      uploadBufferedPartIfNeeded();
    }

    @Override
    public void write(byte[] b, int off, int len) {
      delegate.write(b, off, len);
      uploadBufferedPartIfNeeded();
    }

    @Override
    public void close() throws IOException {
      if (closed) {
        return;
      }
      closed = true;

      try {
        if (uploadId == null) {
          uploadSingleObject(delegate.toByteArray());
        } else {
          uploadFinalPartIfNeeded();
          completeMultipartUpload();
        }
      } catch (RuntimeException exception) {
        abortMultipartUploadQuietly();
        throw new IOException("Failed to upload diff report to S3", exception);
      } finally {
        delegate.close();
      }
    }

    private void uploadBufferedPartIfNeeded() {
      if (delegate.size() < partSizeBytes) {
        return;
      }

      initiateMultipartUploadIfNeeded();
      uploadCurrentBuffer();
    }

    private void uploadSingleObject(byte[] payload) {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType("application/json")
              .build(),
          RequestBody.fromBytes(payload));
    }

    private void initiateMultipartUploadIfNeeded() {
      if (uploadId != null) {
        return;
      }

      uploadId = s3Client
          .createMultipartUpload(CreateMultipartUploadRequest.builder()
              .bucket(bucket)
              .key(key)
              .contentType("application/json")
              .build())
          .uploadId();
    }

    private void uploadFinalPartIfNeeded() {
      if (delegate.size() > 0) {
        uploadCurrentBuffer();
      }
    }

    private void uploadCurrentBuffer() {
      var payload = delegate.toByteArray();
      delegate.reset();

      var response = s3Client.uploadPart(
          UploadPartRequest.builder()
              .bucket(bucket)
              .key(key)
              .uploadId(uploadId)
              .partNumber(partNumber)
              .contentLength((long) payload.length)
              .build(),
          RequestBody.fromBytes(payload));

      completedParts.add(
          CompletedPart.builder().partNumber(partNumber).eTag(response.eTag()).build());
      partNumber++;
    }

    private void completeMultipartUpload() {
      s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
          .bucket(bucket)
          .key(key)
          .uploadId(uploadId)
          .multipartUpload(
              CompletedMultipartUpload.builder().parts(completedParts).build())
          .build());
    }

    private void abortMultipartUploadQuietly() {
      if (uploadId == null) {
        return;
      }

      try {
        s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
            .bucket(bucket)
            .key(key)
            .uploadId(uploadId)
            .build());
      } catch (RuntimeException ignored) {
        // Preserve the original upload failure.
      }
    }
  }
}
