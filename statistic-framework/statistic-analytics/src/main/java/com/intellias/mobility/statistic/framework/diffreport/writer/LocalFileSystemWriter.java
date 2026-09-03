/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class LocalFileSystemWriter implements FileSystemWriter {

  @Override
  public OutputStream openStream(DiffReportDestination destination) throws IOException {
    var outputPath = destination.localPath();
    var parentDir = outputPath.getParent();
    if (parentDir != null) {
      Files.createDirectories(parentDir);
    }

    return Files.newOutputStream(
        outputPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);
  }
}
