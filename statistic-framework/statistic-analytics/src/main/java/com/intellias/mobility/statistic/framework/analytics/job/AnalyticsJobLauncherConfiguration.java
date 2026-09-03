/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;

@AutoConfiguration(after = {BatchAutoConfiguration.class})
@ConditionalOnBean({BatchAutoConfiguration.class})
@Configuration
public class AnalyticsJobLauncherConfiguration {
  @ConditionalOnBean(JobRepository.class)
  @Qualifier("asyncJobLauncher") @Bean
  JobLauncher asyncJobLauncher(JobRepository jobRepository) throws Exception {
    TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
    jobLauncher.setJobRepository(jobRepository);
    jobLauncher.setTaskExecutor(new VirtualThreadTaskExecutor("statistic-analytics"));
    jobLauncher.afterPropertiesSet();
    return jobLauncher;
  }
}
