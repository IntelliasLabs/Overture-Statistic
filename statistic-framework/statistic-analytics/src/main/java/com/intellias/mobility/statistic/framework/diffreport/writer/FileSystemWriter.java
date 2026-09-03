/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.writer;

import java.io.IOException;
import java.io.OutputStream;

public interface FileSystemWriter {

  OutputStream openStream(DiffReportDestination destination) throws IOException;
}
