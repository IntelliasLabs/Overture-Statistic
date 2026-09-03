/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.reader;

import java.io.File;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.parquet.io.InputFile;
import org.springframework.stereotype.Component;

/**
 * Factory responsible for selecting the appropriate {@link FileContentReader}
 * based on the URI scheme and delegating the file read operation.
 */
@Component
@RequiredArgsConstructor
public class FileReaderFactory {

  private final List<FileContentReader> readers;

  /**
   * Reads a file from the given path by delegating to the correct {@link FileContentReader}.
   *
   * @param path a file path or URI
   * @return the file content wrapped as an {@link InputFile}
   * @throws IllegalArgumentException if no reader supports the given path
   */
  public InputFile readFile(String path) {
    if (path == null || path.isBlank()) {
      throw new IllegalArgumentException("Path must not be null or blank");
    }

    URI uri = toUri(path);

    return readers.stream()
        .filter(reader -> reader.supports(uri))
        .findFirst()
        .map(reader -> reader.readFile(uri))
        .orElseThrow(() -> new IllegalArgumentException("Unsupported path format: " + path));
  }

  /**
   * Converts the given path to a valid URI.
   *
   * @param path a file path or URI
   * @return a valid URI instance
   */
  private URI toUri(String path) {
    try {
      if (path.startsWith("https://") || path.startsWith("s3://") || path.startsWith("file:///")) {
        return URI.create(path);
      }

      return new File(path).getAbsoluteFile().toPath().toUri();

    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid path or URI: " + path, e);
    }
  }
}
