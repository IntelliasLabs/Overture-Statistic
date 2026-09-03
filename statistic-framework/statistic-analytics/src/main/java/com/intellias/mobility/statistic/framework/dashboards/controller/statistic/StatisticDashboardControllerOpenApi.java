/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.controller.statistic;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/statistic-dashboard")
public interface StatisticDashboardControllerOpenApi {

  @PostMapping(path = "/common", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Common Dashboard",
      description = "Creates a dashboard that shows general statistics across all geometries.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Common dashboard successfully created.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema =
                        @Schema(
                            implementation = String.class,
                            example =
                                "Common dashboard: dashboard-1235-1343 was successfully created! With controller and visualisations: [2984-4241, 9292-2ijs4]")))
      })
  ResponseEntity<String> createCommonDashboard();

  @PostMapping(path = "/all", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create All Dashboards",
      description = "Creates all statistic dashboards in a single request.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "All statistic dashboards successfully created.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema =
                        @Schema(
                            implementation = String.class,
                            example =
                                "Common dashboard: dashboard-1 was successfully created!, Points dashboard: dashboard-2 was successfully created!, Lines dashboard: dashboard-3 was successfully created!, Polygons dashboard: dashboard-4 was successfully created!, Range Attributes dashboard: dashboard-5 was successfully created!")))
      })
  ResponseEntity<String> createAllAvailable();

  @PostMapping(path = "/points", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Points Dashboard",
      description = "Creates a dashboard for point features.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Points dashboard successfully created.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema =
                        @Schema(
                            implementation = String.class,
                            example =
                                "Points dashboard: dashboard-1235-1343 was successfully created! With controller and visualisations: [2984-4241, 9292-2ijs4]")))
      })
  ResponseEntity<String> createPointsDashboard();

  @PostMapping(path = "/lines", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Lines Dashboard",
      description = "Creates a dashboard for line features.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lines dashboard successfully created.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema =
                        @Schema(
                            implementation = String.class,
                            example =
                                "Lines dashboard: dashboard-1235-1343 was successfully created! With controller and visualisations: [2984-4241, 9292-2ijs4]")))
      })
  ResponseEntity<String> createLinesDashboard();

  @PostMapping(path = "/polygons", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Polygons Dashboard",
      description = "Creates a dashboard for polygons features.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Polygons dashboard successfully created.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema =
                        @Schema(
                            implementation = String.class,
                            example =
                                "Polygons dashboard: dashboard-1235-1343 was successfully created! With controller and visualisations: [2984-4241, 9292-2ijs4]")))
      })
  ResponseEntity<String> createPolygonsDashboard();

  @PostMapping(path = "/rangeAttributes", produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Range Attributes Dashboard",
      description = "Creates a dashboard for range attributes.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Range Attributes dashboard successfully created.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema =
                        @Schema(
                            implementation = String.class,
                            example =
                                "Range Attributes dashboard: dashboard-1235-1343 was successfully created! With controller and visualisations: [2984-4241, 9292-2ijs4]")))
      })
  ResponseEntity<String> createRangeAttributesDashboard();
}
