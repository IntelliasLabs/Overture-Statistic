/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob;

import static com.intellias.mobility.statistic.framework.analytics.diffjob.DifferenceJobDefinitionProvider.SOURCE_VERSION_KEY;
import static com.intellias.mobility.statistic.framework.analytics.diffjob.DifferenceJobDefinitionProvider.TARGET_VERSION_KEY;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class DifferenceStepLoggingListener implements StepExecutionListener {

  @Override
  public void beforeStep(StepExecution stepExecution) {
    var jobParameters = stepExecution.getJobParameters();
    log.info(
        "Starting step '{}' for sourceIndex='{}', sourceVersion='{}', targetVersion='{}', targetIndex='{}', jobExecutionId={}",
        stepExecution.getStepName(),
        jobParameters.getString(DifferenceJobConfiguration.SOURCE_INDEX_PARAM_KEY),
        jobParameters.getString(SOURCE_VERSION_KEY),
        jobParameters.getString(TARGET_VERSION_KEY),
        jobParameters.getString(DifferenceJobConfiguration.TARGET_INDEX_PARAM_KEY),
        stepExecution.getJobExecutionId());
  }

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    log.info(
        "Finished step '{}' with status='{}', readCount={}, writeCount={}, filterCount={}, skipCount={}, rollbackCount={}",
        stepExecution.getStepName(),
        stepExecution.getStatus(),
        stepExecution.getReadCount(),
        stepExecution.getWriteCount(),
        stepExecution.getFilterCount(),
        stepExecution.getSkipCount(),
        stepExecution.getRollbackCount());
    return stepExecution.getExitStatus();
  }
}
