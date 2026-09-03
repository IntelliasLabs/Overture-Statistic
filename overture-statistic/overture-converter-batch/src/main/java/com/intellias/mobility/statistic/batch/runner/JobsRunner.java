/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.runner;

import com.intellias.mobility.statistic.batch.config.BatchConfiguration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobsRunner {

  private final StatusHandler jobStatusHandler;
  private final JobLauncher jobLauncher;
  private final JobPrinter jobPrinter;
  private final BatchConfiguration batchConfiguration;

  public void runJobs(List<Job> availableJobs) {
    jobPrinter.printAvailableJobs(availableJobs);
    executeEnabledJobs(availableJobs);
  }

  private void executeEnabledJobs(List<Job> availableJobs) {
    boolean anySuccess = false;
    boolean anyFailure = findMissingJobs(availableJobs);

    for (Job job : availableJobs) {
      String jobName = job.getName();

      if (batchConfiguration.isEnabled(jobName)) {
        boolean result = runJob(job, jobName);
        if (!anySuccess && result) {
          anySuccess = true;
        } else {
          anyFailure = true;
        }
      }
    }

    if (!anySuccess) jobStatusHandler.handle(2);
    else if (!anyFailure) jobStatusHandler.handle(0);
    else jobStatusHandler.handle(1);
  }

  private boolean findMissingJobs(List<Job> availableJobs) {
    Set<String> enabledJobs = batchConfiguration.getEnabledJobs();
    if (enabledJobs == null || enabledJobs.isEmpty()) {
      log.error("Enabled jobs are null or empty! Please check your configuration");
      return true;
    }
    Set<String> availableJobNames =
        availableJobs.stream().map(Job::getName).collect(Collectors.toSet());

    for (String enabledJob : enabledJobs) {
      if (!availableJobNames.contains(enabledJob)) {
        log.info(
            "Enabled job {} not found among available jobs; please check your configuration.",
            enabledJob);
        return true;
      }
    }
    return false;
  }

  private boolean runJob(Job job, String jobName) {
    try {
      JobParameters jobParameters = buildJobParameters(jobName);
      log.info("Launching job: {}", jobName);
      JobExecution execution = jobLauncher.run(job, jobParameters);

      if (execution.getExitStatus().equals(ExitStatus.COMPLETED)) {
        return true;
      } else {
        log.error("Job '{}' failed with status: {}.", jobName, execution.getExitStatus());
        return false;
      }
    } catch (Exception e) {
      log.error("Exception occurred while running job '{}': {}.", jobName, e.getMessage(), e);
      return false;
    }
  }

  private JobParameters buildJobParameters(String jobName) {
    JobParametersBuilder builder = new JobParametersBuilder();

    Map<String, String> configParams = batchConfiguration.getParametersFor(jobName);
    if (!configParams.isEmpty()) {
      String version = configParams.get("version");
      if (version == null || version.trim().isEmpty()) {
        throw new IllegalArgumentException("Version parameter must be specified and not empty.");
      }
      configParams.forEach(builder::addString);
    } else {
      log.warn("No parameters configured for job '{}'.", jobName);
    }
    builder.addLong("run.id", System.currentTimeMillis());

    return builder.toJobParameters();
  }
}
