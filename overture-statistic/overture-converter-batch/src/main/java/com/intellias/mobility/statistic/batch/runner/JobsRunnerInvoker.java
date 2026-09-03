/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.runner;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobsRunnerInvoker {

  private final JobsRunner jobsRunner;
  private final List<Job> availableJobs;

  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady() {
    jobsRunner.runJobs(availableJobs);
  }
}
