/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.controller;

import com.intellias.mobility.statistic.framework.diffreport.model.DiffReportRequest;
import com.intellias.mobility.statistic.framework.diffreport.model.DiffReportType;
import com.intellias.mobility.statistic.framework.diffreport.service.DiffReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DiffReportController implements DiffReportControllerOpenApi {

  private final DiffReportService diffReportService;

  @Override
  public ResponseEntity<String> exportDiffPerFeature(
      @RequestBody DiffReportRequest diffReportRequest) {
    diffReportService.exportDiffReport(diffReportRequest, DiffReportType.PER_FEATURE);
    return ResponseEntity.ok("Report per feature exported successfully!");
  }

  @Override
  public ResponseEntity<String> exportDiffPerFeatureType(
      @RequestBody DiffReportRequest diffReportRequest) {
    diffReportService.exportDiffReport(diffReportRequest, DiffReportType.PER_FEATURE_TYPE);
    return ResponseEntity.ok("Report per feature type exported successfully!");
  }
}
