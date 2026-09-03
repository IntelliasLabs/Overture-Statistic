/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.util;

import com.intellias.mobility.statistic.batch.runner.StatusHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("test")
public class JobStatusHandlerNoOp implements StatusHandler {
  @Override
  public void handle(int exitCode) {}
}
