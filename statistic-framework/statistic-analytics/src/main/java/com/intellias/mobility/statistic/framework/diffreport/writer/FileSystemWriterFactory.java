/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.writer;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class FileSystemWriterFactory {

  private final FileSystemWriter localFileSystemWriter;
  private final FileSystemWriter s3FileSystemWriter;

  public FileSystemWriter create(String diffReportOutFolderPath) {
    if (!StringUtils.hasText(diffReportOutFolderPath)) {
      throw new IllegalArgumentException("Diff report output path must not be blank");
    }

    if (!diffReportOutFolderPath.startsWith("s3://")) {
      return localFileSystemWriter;
    }

    validateS3Path(diffReportOutFolderPath);
    return s3FileSystemWriter;
  }

  private void validateS3Path(String diffReportOutFolderPath) {
    var uri = URI.create(diffReportOutFolderPath);
    if (!"s3".equalsIgnoreCase(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
      throw new IllegalArgumentException(
          "Diff report S3 output path must use the format s3://<bucket>[/prefix]");
    }
  }
}
