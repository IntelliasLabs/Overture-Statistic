/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.reader.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellias.mobility.statistic.infrastructure.reader.FileContentReader;
import com.intellias.mobility.statistic.infrastructure.reader.FileReaderFactory;
import com.intellias.mobility.statistic.infrastructure.reader.LocalFileContentReader;
import com.intellias.mobility.statistic.infrastructure.reader.S3FileContentReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.parquet.io.InputFile;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Slf4j
class FileReaderUnitTest {

  @Test
  void shouldReadLocalFileDirectly() throws IOException {
    log.info("Starting test: shouldReadLocalFileDirectly");

    File tempFile = File.createTempFile("test-local", ".txt");
    String expected = "Test content from local file";
    Files.writeString(tempFile.toPath(), expected);

    FileContentReader reader = new LocalFileContentReader();
    URI uri = tempFile.toPath().toUri();

    InputFile inputFile = reader.readFile(uri);
    String actual = new String(inputFile.newStream().readAllBytes(), StandardCharsets.UTF_8);

    assertEquals(expected, actual);
    log.info("Passed: shouldReadLocalFileDirectly");
  }

  @Test
  void shouldReadLocalFileViaFactory() throws IOException {
    log.info("Starting test: shouldReadLocalFileViaFactory");

    File tempFile = File.createTempFile("test-factory", ".txt");
    String expected = "Factory-based file content";
    Files.writeString(tempFile.toPath(), expected);

    FileReaderFactory factory = new FileReaderFactory(List.of(new LocalFileContentReader()));
    InputFile inputFile = factory.readFile(tempFile.getAbsolutePath());

    String actual = new String(inputFile.newStream().readAllBytes(), StandardCharsets.UTF_8);
    assertEquals(expected, actual);
    log.info("Passed: shouldReadLocalFileViaFactory");
  }

  @Test
  void shouldReadS3FileMocked() throws IOException {
    log.info("Starting test: shouldReadS3FileMocked");

    String expectedContent = "Mocked S3 content";
    S3Client mockS3 = mock(S3Client.class);

    when(mockS3.getObject(any(GetObjectRequest.class), any(ResponseTransformer.class)))
        .thenAnswer(invocation -> {
          ResponseTransformer<GetObjectResponse, GetObjectResponse> transformer =
              invocation.getArgument(1);

          InputStream contentStream =
              new ByteArrayInputStream(expectedContent.getBytes(StandardCharsets.UTF_8));

          GetObjectResponse response = GetObjectResponse.builder().build();

          transformer.transform(response, AbortableInputStream.create(contentStream));

          return response;
        });

    FileContentReader reader = new S3FileContentReader(mockS3);
    URI uri = URI.create("s3://my-bucket/my/key.txt");

    InputFile inputFile = reader.readFile(uri);
    String actual = new String(inputFile.newStream().readAllBytes(), StandardCharsets.UTF_8);

    assertEquals(expectedContent, actual);
    log.info("Passed: shouldReadS3FileMocked");
  }

  @Test
  void shouldFailOnS3MissingBucket() {
    log.info("Starting test: shouldFailOnS3MissingBucket");

    S3Client mockS3 = mock(S3Client.class);
    FileContentReader reader = new S3FileContentReader(mockS3);

    URI invalidUri = URI.create("s3:///justkey.txt");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> reader.readFile(invalidUri));

    log.info("Passed: shouldFailOnS3MissingBucket");
  }
}
