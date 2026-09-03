/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.runner;

import com.intellias.mobility.statistic.batch.config.BatchConfiguration;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobPrinter {

  private final BatchConfiguration batchConfiguration;

  public void printAvailableJobs(List<Job> availableJobs) {
    if (availableJobs.isEmpty()) {
      log.info("No jobs available.");
      return;
    }

    log.info("Available jobs:");
    availableJobs.stream()
        .sorted(Comparator.comparing((Job job) -> !batchConfiguration.isEnabled(job.getName()))
            .thenComparing(Job::getName))
        .forEach(job -> {
          String statusLabel = batchConfiguration.isEnabled(job.getName()) ? "Enabled" : "Disabled";
          log.info(" {} ({})", job.getName(), statusLabel);
        });
  }
}
