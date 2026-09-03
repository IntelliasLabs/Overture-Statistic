/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.intellias.mobility.statistic.framework.diffreport.config.DiffReportProperties;
import com.intellias.mobility.statistic.framework.diffreport.elastic.ElasticsearchService;
import com.intellias.mobility.statistic.framework.diffreport.model.DiffReportRequest;
import com.intellias.mobility.statistic.framework.diffreport.model.DiffReportType;
import com.intellias.mobility.statistic.framework.diffreport.writer.DiffReportDestination;
import com.intellias.mobility.statistic.framework.diffreport.writer.FileSystemWriterFactory;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;

@RequiredArgsConstructor
public class DiffReportService {

  private final ObjectMapper objectMapper;
  private final ElasticsearchService elasticsearchService;
  private final DiffReportProperties diffReportProperties;
  private final FileSystemWriterFactory fileSystemWriterFactory;

  private static final int BATCH_SIZE = 1;

  @SneakyThrows
  public void exportDiffReport(DiffReportRequest request, DiffReportType type) {
    var destination = buildDestination(request, type);
    var fileSystemWriter = fileSystemWriterFactory.create(destination.basePath());
    try (var iterator = getIteratorForType(request, type);
        var outputStream = fileSystemWriter.openStream(destination);
        var fileWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        var jsonWriter =
            objectMapper.writerWithDefaultPrettyPrinter().writeValuesAsArray(fileWriter)) {
      writeBatch(iterator, jsonWriter);
    }
  }

  private SearchHitsIterator<?> getIteratorForType(DiffReportRequest request, DiffReportType type) {
    return switch (type) {
      case PER_FEATURE -> elasticsearchService.getDifferencePerFeatureIterator(request);
      case PER_FEATURE_TYPE -> elasticsearchService.getDifferencePerFeatureTypeIterator(request);
    };
  }

  @SneakyThrows
  private <T> void writeBatch(SearchHitsIterator<T> iterator, SequenceWriter jsonWriter) {
    List<T> batch = new ArrayList<>(BATCH_SIZE);

    while (iterator.hasNext()) {
      batch.add(iterator.next().getContent());

      if (batch.size() == BATCH_SIZE) {
        jsonWriter.write(batch);
        batch.clear();
      }
    }
    if (!batch.isEmpty()) {
      jsonWriter.write(batch);
    }
  }

  private DiffReportDestination buildDestination(
      DiffReportRequest diffReportRequest, DiffReportType diffReportType) {
    return new DiffReportDestination(
        diffReportProperties.diffReportOutFolderPath(),
        diffReportType.getPathValue(),
        buildFileName(diffReportRequest));
  }

  private String buildFileName(DiffReportRequest diffReportRequest) {
    return String.format(
        "%s-%s-%s.json",
        diffReportRequest.getIndexName(),
        diffReportRequest.getSourceVersion(),
        diffReportRequest.getTargetVersion());
  }
}
