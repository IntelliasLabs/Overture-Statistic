/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.runner;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Getter
@Profile("!test")
public class JobStatusHandler implements StatusHandler {

  private final ConfigurableApplicationContext context;

  @Override
  public void handle(int exitCode) {
    switch (exitCode) {
      case 0 -> log.info("All enabled jobs completed successfully.");
      case 1 -> log.warn(
          "Job Execution completed with partial success, some jobs succeeded, others failed.");
      default -> log.error("All enabled jobs failed.");
    }

    int springExitCode = SpringApplication.exit(context, () -> exitCode);
    System.exit(springExitCode);
  }
}
