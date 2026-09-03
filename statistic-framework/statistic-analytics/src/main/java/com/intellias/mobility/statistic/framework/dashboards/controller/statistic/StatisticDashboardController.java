/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.controller.statistic;

import com.intellias.mobility.statistic.framework.dashboards.service.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class StatisticDashboardController implements StatisticDashboardControllerOpenApi {

  private final CommonDashboardCreationService commonDashboardCreationService;
  private final LinesDashboardCreationService linesDashboardCreationService;
  private final PointsDashboardCreationService pointsDashboardCreationService;
  private final PolygonsDashboardCreationService polygonsDashboardCreationService;
  private final RangeAttributesDashboardCreationService rangeAttributesDashboardCreationService;

  @Override
  public ResponseEntity<String> createCommonDashboard() {
    return ResponseEntity.ok(commonDashboardCreationService.createCommonDashboard());
  }

  @Override
  public ResponseEntity<String> createAllAvailable() {
    return ResponseEntity.ok(String.join(
        ", ",
        List.of(
            commonDashboardCreationService.createCommonDashboard(),
            pointsDashboardCreationService.createPointsDashboard(),
            linesDashboardCreationService.createLinesDashboard(),
            polygonsDashboardCreationService.createPolygonsDashboard(),
            rangeAttributesDashboardCreationService.createRangeAttributesDashboard())));
  }

  @Override
  public ResponseEntity<String> createPointsDashboard() {
    return ResponseEntity.ok(pointsDashboardCreationService.createPointsDashboard());
  }

  @Override
  public ResponseEntity<String> createLinesDashboard() {
    return ResponseEntity.ok(linesDashboardCreationService.createLinesDashboard());
  }

  @Override
  public ResponseEntity<String> createPolygonsDashboard() {
    return ResponseEntity.ok(polygonsDashboardCreationService.createPolygonsDashboard());
  }

  @Override
  public ResponseEntity<String> createRangeAttributesDashboard() {
    return ResponseEntity.ok(
        rangeAttributesDashboardCreationService.createRangeAttributesDashboard());
  }
}
