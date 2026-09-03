/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.writer;

import java.nio.file.Path;
import java.util.StringJoiner;

public record DiffReportDestination(String basePath, String reportTypePath, String fileName) {

  public Path localPath() {
    return Path.of(basePath).resolve(reportTypePath).resolve(fileName);
  }

  public String relativePath() {
    return new StringJoiner("/").add(reportTypePath).add(fileName).toString();
  }
}
