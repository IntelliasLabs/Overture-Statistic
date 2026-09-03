/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AnalyticsJobController implements AnalyticsJobControllerOpenApi {
  private final AnalyticsJobExecutor objectMapper;

  @Override
  public List<JobExecutionStatus> executeJob(
      @PathVariable("jobName") final String jobName,
      @RequestBody final Map<String, Object> runtimeParameters) {
    return objectMapper.execute(jobName, runtimeParameters);
  }

  @Override
  public List<JobTemplateDefinition> listOfRegisteredJobs() {
    return objectMapper.registeredJobList();
  }
}
