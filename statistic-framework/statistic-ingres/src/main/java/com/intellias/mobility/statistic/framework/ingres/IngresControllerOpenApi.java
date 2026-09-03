/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.ingres;

import com.intellias.statistic.model.feature.StatisticFeature;
import com.intellias.statistic.model.feature.StatisticFeatureCollection;
import com.intellias.statistic.model.geometry.StatisticGeometry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/** API contract for {@link IngresController}. */
@RequestMapping("/ingress")
public interface IngresControllerOpenApi {

  @PostMapping(
      path = "/save",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Save a Statistic Feature",
      description = "Processes and stores a given StatisticFeature into the appropriate index.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Returns `true` if the feature was successfully processed and stored.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "boolean")))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "The StatisticFeature object to be processed and stored.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = StatisticFeature.class)))
  boolean save(@RequestBody StatisticFeature<?> feature);

  @PostMapping(
      path = "/save-all",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Save Multiple Statistic Features",
      description =
          "Processes and stores a collection of StatisticGeometry objects within a StatisticFeatureCollection.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Returns `true` if the collection of StatisticFeatures was successfully processed and stored.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(type = "boolean")))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "A collection of StatisticGeometry objects to be processed and stored.",
      required = true,
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = StatisticFeatureCollection.class)))
  boolean saveAll(@RequestBody StatisticFeatureCollection<StatisticGeometry> collection);
}
