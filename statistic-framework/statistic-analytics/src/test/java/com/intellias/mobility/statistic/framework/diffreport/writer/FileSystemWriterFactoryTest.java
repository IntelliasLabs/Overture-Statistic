/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.writer;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;

class FileSystemWriterFactoryTest {

  private final FileSystemWriter localFileSystemWriter = new LocalFileSystemWriter();
  private final FileSystemWriter s3FileSystemWriter =
      new S3FileSystemWriter(() -> mock(S3Client.class));
  private final FileSystemWriterFactory fileSystemWriterFactory =
      new FileSystemWriterFactory(localFileSystemWriter, s3FileSystemWriter);

  @Test
  void shouldSelectLocalWriterForFilesystemPath() {
    assertSame(localFileSystemWriter, fileSystemWriterFactory.create("/tmp/diff-reports"));
  }

  @Test
  void shouldSelectS3WriterForBucketOnlyPath() {
    assertSame(s3FileSystemWriter, fileSystemWriterFactory.create("s3://diff-reports"));
  }

  @Test
  void shouldSelectS3WriterForBucketPrefixPath() {
    assertSame(
        s3FileSystemWriter, fileSystemWriterFactory.create("s3://diff-reports/reports/export"));
  }

  @Test
  void shouldRejectBlankOutputPath() {
    assertThrows(IllegalArgumentException.class, () -> fileSystemWriterFactory.create("  "));
  }

  @Test
  void shouldRejectMalformedS3OutputPath() {
    assertThrows(IllegalArgumentException.class, () -> fileSystemWriterFactory.create("s3://"));
  }
}
