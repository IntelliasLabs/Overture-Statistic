/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.dashboards.controller.diff;

import com.intellias.mobility.statistic.framework.dashboards.dto.DifferenceRequest;
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

@RequestMapping("/diff-dashboard")
public interface DiffDashboardControllerOpenApi {

  @PostMapping(
      path = "/feature-count-diff",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Feature Count Diff Visualization",
      description =
          "Creates a visualization showing differences in feature counts between versions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare feature counts between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createFeatureCountDiff(@RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/feature-property-types-count-diff",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Feature Property Types Count Diff Visualization",
      description =
          "Creates a visualization showing differences in feature property types counts between versions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare feature property types counts between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createFeaturePropertyTypesCountDiff(
      @RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/feature-property-values-count-diff",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Feature Property Unique Values Count Diff Visualization",
      description =
          "Creates a visualization showing differences in feature property unique values counts between versions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare feature property unique values counts between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createFeaturePropertyValuesCountDiff(
      @RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/range-attribute-types-count-diff",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Range Attribute Types Count Diff Visualization",
      description =
          "Creates a visualization showing differences in range attribute types counts between versions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare range attribute types counts between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createRangeAttributeTypesCountDiff(
      @RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/range-attribute-values-count-diff",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Range Attribute Unique Values Count Diff Visualization",
      description =
          "Creates a visualization showing differences in range attribute unique values counts between versions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare range attribute unique values counts between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createRangeAttributeValuesCountDiff(
      @RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/length-diff",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Length(meters) Diff Visualization for Line and MultiLine Feature Types",
      description = "Creates a visualization showing differences in length between versions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare features' length between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createLengthDiff(@RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/area-diff",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Area Diff Visualization for Polygon and MultiPolygon Feature Types",
      description = "Creates a visualization showing differences in area between versions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare features' area between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createAreaDiff(@RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/feature-per-property-type-count-diff",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Feature Count Diff Per Feature Property Type Visualization",
      description =
          "Creates a visualization showing differences in feature counts that have certain property type between versions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare feature counts that have certain property type area between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createFeaturePerPropertyTypeCountDiff(
      @RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/feature-per-range-attribute-type-count-diff",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Feature Count Diff Per Range Attribute Type Visualization",
      description =
          "Creates a visualization showing differences in feature counts that have certain range attribute type between versions.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare feature counts that have certain range attribute type area between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createFeaturePerRangeAttributeTypeCountDiff(
      @RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/changed-features-count",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Changed Feature Count Between Two Versions Visualization",
      description = "Creates a visualization showing number of changed features between versions. "
          + "**IMPORTANT** – This will be available only after running "
          + "[`POST /analytics-job/execute/{jobName}`](#/analytics-job-controller/executeJob) "
          + "with `jobName` set to `featureDifferenceJob`.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare changed feature counts between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createChangedFeaturesCount(
      @RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/added-features-count",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create added Feature Count Between Two Versions Visualization",
      description = "Creates a visualization showing number of added features between versions. "
          + "**IMPORTANT** – This will be available only after running "
          + "[`POST /analytics-job/execute/{jobName}`](#/analytics-job-controller/executeJob) "
          + "with `jobName` set to `featureDifferenceJob`.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare added feature counts between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createAddedFeaturesCount(@RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/deleted-features-count",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create deleted Feature Count Between Two Versions Visualization",
      description = "Creates a visualization showing number of deleted features between versions. "
          + "**IMPORTANT** – This will be available only after running "
          + "[`POST /analytics-job/execute/{jobName}`](#/analytics-job-controller/executeJob) "
          + "with `jobName` set to `featureDifferenceJob`.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualization successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare deleted feature counts between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createDeletedFeaturesCount(
      @RequestBody DifferenceRequest differenceRequest);

  @PostMapping(
      path = "/all-available",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE)
  @Operation(
      summary = "Create Difference Visualizations",
      description = "Creates a visualizations showing difference between versions. "
          + "**IMPORTANT** – This will be available only after running "
          + "[`POST /analytics-job/execute/{jobName}`](#/analytics-job-controller/executeJob) "
          + "with `jobName` set to `featureDifferenceJob`.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Visualizations successfully created if the required data exists.",
            content =
                @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description =
          "Contains source and target version identifiers to compare changes between them.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = DifferenceRequest.class)))
  ResponseEntity<String> createAllAvailable(@RequestBody DifferenceRequest differenceRequest);
}
