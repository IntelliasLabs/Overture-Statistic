/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.reader;

import java.net.URI;
import org.apache.parquet.io.InputFile;

/**
 * Defines a strategy for reading file content from a given URI.
 *
 * <p>Implementations support different schemes such as local files or S3.
 */
public interface FileContentReader {
  boolean supports(URI uri);

  InputFile readFile(URI uri);
}
