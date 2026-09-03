/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.writer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileSystemWriterTest {

  @TempDir
  Path tempDir;

  @Test
  void shouldCreateMissingDirectoriesAndWriteToResolvedPath() throws Exception {
    var writer = new LocalFileSystemWriter();
    var destination = new DiffReportDestination(
        tempDir.resolve("exports").toString(), "per-feature", "report.json");

    try (var outputStream = writer.openStream(destination)) {
      outputStream.write("{\"status\":\"ok\"}".getBytes(UTF_8));
    }

    var expectedFile = tempDir.resolve("exports").resolve("per-feature").resolve("report.json");
    assertTrue(Files.exists(expectedFile));
    assertEquals("{\"status\":\"ok\"}", Files.readString(expectedFile));
  }
}
