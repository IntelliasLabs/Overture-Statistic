/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.controller;

import com.intellias.mobility.statistic.framework.diffreport.model.DiffReportRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/diff-report")
public interface DiffReportControllerOpenApi {

  @PostMapping(
      path = "/export-diff-per-feature-report",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Export diff per feature from diff job result as json file",
      description = "Creates a diff per feature json file report from diff job result and save it")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Json file saved successfully!",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers and index name to get certain diff result",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DiffReportRequest.class)))
  ResponseEntity<String> exportDiffPerFeature(@RequestBody DiffReportRequest diffReportRequest);

  @PostMapping(
      path = "/export-diff-per-feature-type-report",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Export diff per feature type from diff job result as json file",
      description =
          "Creates a diff per feature type json file report from diff job result and save it")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Json file saved successfully!",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers and index name to get certain diff result",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DiffReportRequest.class)))
  ResponseEntity<String> exportDiffPerFeatureType(@RequestBody DiffReportRequest diffReportRequest);
}
