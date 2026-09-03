/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellias.mobility.statistic.framework.diffreport.config.DiffReportProperties;
import com.intellias.mobility.statistic.framework.diffreport.elastic.ElasticsearchService;
import com.intellias.mobility.statistic.framework.diffreport.model.DiffReportRequest;
import com.intellias.mobility.statistic.framework.diffreport.model.DiffReportType;
import com.intellias.mobility.statistic.framework.diffreport.writer.DiffReportDestination;
import com.intellias.mobility.statistic.framework.diffreport.writer.FileSystemWriter;
import com.intellias.mobility.statistic.framework.diffreport.writer.FileSystemWriterFactory;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.elasticsearch.core.SearchHitsIterator;

class DiffReportServiceTest {

  @Test
  void shouldDelegateExportToSelectedWriter() throws Exception {
    var elasticsearchService = mock(ElasticsearchService.class);
    var iterator = mock(SearchHitsIterator.class);
    var fileSystemWriterFactory = mock(FileSystemWriterFactory.class);
    var fileSystemWriter = mock(FileSystemWriter.class);
    var properties = new DiffReportProperties("/tmp/diff-reports");
    var service = new DiffReportService(
        new ObjectMapper(), elasticsearchService, properties, fileSystemWriterFactory);

    when(elasticsearchService.getDifferencePerFeatureIterator(any())).thenReturn(iterator);
    when(fileSystemWriterFactory.create(properties.diffReportOutFolderPath()))
        .thenReturn(fileSystemWriter);
    when(fileSystemWriter.openStream(any())).thenReturn(new ByteArrayOutputStream());
    when(iterator.hasNext()).thenReturn(false);

    service.exportDiffReport(
        new DiffReportRequest("roads", "v1", "v2"), DiffReportType.PER_FEATURE);

    var destinationCaptor = ArgumentCaptor.forClass(DiffReportDestination.class);
    verify(fileSystemWriter).openStream(destinationCaptor.capture());

    assertEquals("/tmp/diff-reports", destinationCaptor.getValue().basePath());
    assertEquals("per-feature", destinationCaptor.getValue().reportTypePath());
    assertEquals("roads-v1-v2.json", destinationCaptor.getValue().fileName());
    verify(fileSystemWriterFactory).create("/tmp/diff-reports");
    verify(iterator).close();
  }
}
