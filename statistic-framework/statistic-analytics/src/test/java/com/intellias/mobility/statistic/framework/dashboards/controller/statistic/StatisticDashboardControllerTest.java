/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.controller.statistic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intellias.mobility.statistic.framework.common.IndexManager;
import com.intellias.mobility.statistic.framework.dashboards.service.CommonDashboardCreationService;
import com.intellias.mobility.statistic.framework.dashboards.service.LinesDashboardCreationService;
import com.intellias.mobility.statistic.framework.dashboards.service.PointsDashboardCreationService;
import com.intellias.mobility.statistic.framework.dashboards.service.PolygonsDashboardCreationService;
import com.intellias.mobility.statistic.framework.dashboards.service.RangeAttributesDashboardCreationService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class StatisticDashboardControllerTest {

  @Test
  void createAllAvailableReturnsResultsFromAllDashboardCreators() {
    var controller = new StatisticDashboardController(
        new RecordingCommonDashboardCreationService(),
        new RecordingLinesDashboardCreationService(),
        new RecordingPointsDashboardCreationService(),
        new RecordingPolygonsDashboardCreationService(),
        new RecordingRangeAttributesDashboardCreationService());

    var response = controller.createAllAvailable();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(
        "common-dashboard, points-dashboard, lines-dashboard, polygons-dashboard, range-attributes-dashboard",
        response.getBody());
  }

  private static class RecordingCommonDashboardCreationService
      extends CommonDashboardCreationService {
    RecordingCommonDashboardCreationService() {
      super(new RecordingIndexManager(), null, null, null, null);
    }

    @Override
    public String createCommonDashboard() {
      return "common-dashboard";
    }
  }

  private static class RecordingLinesDashboardCreationService
      extends LinesDashboardCreationService {
    RecordingLinesDashboardCreationService() {
      super(new RecordingIndexManager(), null, null, null, null);
    }

    @Override
    public String createLinesDashboard() {
      return "lines-dashboard";
    }
  }

  private static class RecordingPointsDashboardCreationService
      extends PointsDashboardCreationService {
    RecordingPointsDashboardCreationService() {
      super(new RecordingIndexManager(), null, null, null, null);
    }

    @Override
    public String createPointsDashboard() {
      return "points-dashboard";
    }
  }

  private static class RecordingPolygonsDashboardCreationService
      extends PolygonsDashboardCreationService {
    RecordingPolygonsDashboardCreationService() {
      super(new RecordingIndexManager(), null, null, null, null);
    }

    @Override
    public String createPolygonsDashboard() {
      return "polygons-dashboard";
    }
  }

  private static class RecordingRangeAttributesDashboardCreationService
      extends RangeAttributesDashboardCreationService {
    RecordingRangeAttributesDashboardCreationService() {
      super(new RecordingIndexManager(), null, null, null, null);
    }

    @Override
    public String createRangeAttributesDashboard() {
      return "range-attributes-dashboard";
    }
  }

  private static class RecordingIndexManager extends IndexManager {
    RecordingIndexManager() {
      super(null, null, null, null);
    }

    @Override
    public Set<String> getAllFeatureIndexList() {
      return Set.of();
    }

    @Override
    public Set<String> getAllFeaturePropertiesIndexList() {
      return Set.of();
    }

    @Override
    public Set<String> getLineFeatureIndexList() {
      return Set.of();
    }

    @Override
    public Set<String> getPointFeatureIndexList() {
      return Set.of();
    }

    @Override
    public Set<String> getPolygonFeatureIndexList() {
      return Set.of();
    }

    @Override
    public Set<String> getAllRangeAttributesIndexList() {
      return Set.of();
    }
  }
}
