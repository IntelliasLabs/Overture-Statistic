/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/analytics-job")
public interface AnalyticsJobControllerOpenApi {

  @PostMapping(
      path = "/execute/{jobName}",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Execute a job",
      description =
          "Executes a job using the provided job name and runtime parameters. "
              + "To see the required runtime parameters for each job in description and available job names, "
              + "you can first call the [`GET /job-list`](#/job-list) endpoint. "
              + "Then, you can execute the job using `POST /execute/{jobName}` with the appropriate parameters.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description =
                "Job successfully started to execute, returning a list of job execution statuses.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JobExecutionStatus.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "The runtime parameters to execute the job. The required parameters "
          + "for each job can be retrieved via the [`GET /job-list`](#/job-list) endpoint.",
      required = true,
      content =
          @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(type = "object")))
  List<JobExecutionStatus> executeJob(
      @PathVariable("jobName") final String jobName,
      @RequestBody final Map<String, Object> runtimeParameters);

  @GetMapping(path = "/job-list", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "List of Registered Jobs",
      description =
          "Returns a list of all registered job templates, along with the detailed runtime parameters "
              + "in description required for each job. "
              + "For each job, you can call the [`POST /execute/{jobName}`](#/executeJob) endpoint, "
              + "providing the runtime parameters for the specified job to execute it.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved list of registered job templates",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = JobTemplateDefinition.class)))
      })
  List<JobTemplateDefinition> listOfRegisteredJobs();
}
