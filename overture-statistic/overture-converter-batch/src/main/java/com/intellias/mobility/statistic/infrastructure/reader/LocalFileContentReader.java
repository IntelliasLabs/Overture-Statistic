/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.reader;

import java.io.IOException;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalFileContentReader implements FileContentReader {

  @Override
  public boolean supports(URI uri) {
    return uri.getScheme() == null || "file".equalsIgnoreCase(uri.getScheme());
  }

  @Override
  public InputFile readFile(URI uri) {
    try {
      log.info("Successfully read local file: {}", uri);
      return HadoopInputFile.fromPath(new Path(uri), new Configuration());
    } catch (IOException e) {
      log.error("Error reading local file at URI = {}", uri, e);
      throw new RuntimeException("Failed to read local file", e);
    }
  }
}
