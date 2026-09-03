/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import java.util.Set;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@AutoConfiguration(after = {BatchAutoConfiguration.class})
@ConditionalOnBean({BatchAutoConfiguration.class})
@Configuration
public class AnalyticsJobConfiguration {

  @ConditionalOnBean(JobExplorer.class)
  @Bean
  AnalyticsJobExplorer analyticsJobExplorer(JobExplorer jobExplorer) {
    return new AnalyticsJobExplorer(jobExplorer);
  }

  @ConditionalOnBean(JobLauncher.class)
  @Bean
  @DependsOn("jobRegistry")
  AnalyticsJobExecutor analyticsJobExecutor(
      Set<AnalyticsJobDefinitionProvider> jobDefinitionProviders,
      @Qualifier("asyncJobLauncher") JobLauncher asyncJobLauncher,
      AnalyticsJobExplorer jobExplorer) {
    return new AnalyticsJobExecutor(jobDefinitionProviders, asyncJobLauncher, jobExplorer);
  }
}
