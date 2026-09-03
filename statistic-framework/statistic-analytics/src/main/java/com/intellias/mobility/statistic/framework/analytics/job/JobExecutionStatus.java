/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import java.time.LocalDateTime;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobParameters;

public record JobExecutionStatus(
    String name,
    JobParameters parameters,
    BatchStatus status,
    LocalDateTime startTime,
    LocalDateTime createTime,
    LocalDateTime endTime,
    LocalDateTime lastUpdated,
    ExitStatus exitStatus) {}
