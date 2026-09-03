/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class DummyJobConfig {

  @Bean
  public Step failingJobStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("failingJobStep", jobRepository)
        .tasklet(
            (contribution, chunkContext) -> {
              log.info("Running failingJob step");
              return RepeatStatus.FINISHED;
            },
            transactionManager)
        .build();
  }

  @Bean
  public Step anotherJobStep(
      JobRepository jobRepository, PlatformTransactionManager transactionManager) {
    return new StepBuilder("anotherJobStep", jobRepository)
        .tasklet(
            (contribution, chunkContext) -> {
              log.info("Running anotherJob step with param2");
              return RepeatStatus.FINISHED;
            },
            transactionManager)
        .build();
  }

  @Bean
  public Job failingJob(JobRepository jobRepository, Step failingJobStep) {
    return new JobBuilder("failingJob", jobRepository).start(failingJobStep).build();
  }

  @Bean
  public Job anotherJob(JobRepository jobRepository, Step anotherJobStep) {
    return new JobBuilder("anotherJob", jobRepository).start(anotherJobStep).build();
  }
}
