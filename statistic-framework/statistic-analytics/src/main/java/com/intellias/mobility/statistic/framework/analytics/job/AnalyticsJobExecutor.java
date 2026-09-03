/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

/**
 *  This class is responsible for managing and executing analytics jobs.
 *  It holds a map of registered job definition providers, a job launcher, and a job explorer.
 *  It provides methods to list registered jobs, retrieve them with parameters, and execute jobs if they are not already running.
 */
public class AnalyticsJobExecutor {
  private final Map<String, AnalyticsJobDefinitionProvider> jobDefinitionProviderMap;
  private final JobLauncher jobLauncher;
  private final AnalyticsJobExplorer jobExplorer;

  public AnalyticsJobExecutor(
      Set<AnalyticsJobDefinitionProvider> jobDefinitionProviders,
      JobLauncher jobLauncher,
      AnalyticsJobExplorer jobExplorer) {
    this.jobDefinitionProviderMap = jobDefinitionProviders.stream()
        .collect(Collectors.toMap(AnalyticsJobDefinitionProvider::jobName, jdp -> jdp));

    this.jobLauncher = jobLauncher;
    this.jobExplorer = jobExplorer;
  }

  /**
   * Returns a list of registered job definitions.
   */
  public List<JobTemplateDefinition> registeredJobList() {
    return jobDefinitionProviderMap.values().stream()
        .map(jd ->
            new JobTemplateDefinition(jd.jobName(), jd.jobDescription(), jd.initialJobParameters()))
        .collect(Collectors.toList());
  }

  /**
   * Executes the job if it's not currently running.
   *
   * @param jobName The name of the job to execute.
   * @return A list of JobExecutions, which will be empty if the job was already running or not found.
   */
  public List<JobExecutionStatus> execute(String jobName, Map<String, Object> runtimeParameters) {
    // verify if job is managed by this.
    if (jobDefinitionProviderMap.containsKey(jobName)) {
      // verify if job is not running otherwise return current status
      var jobDef = jobDefinitionProviderMap.get(jobName);
      var jobStatuses =
          jobExplorer.isJobsRunning(jobDef.jobName(), jobDef.jobParameters(runtimeParameters));

      return jobStatuses.notRunningJobs().stream()
          .map(jbInfo -> runJob(jobDef.job(), jbInfo))
          .map(je -> new JobExecutionStatus(
              je.getJobInstance().getJobName(),
              je.getJobParameters(),
              je.getStatus(),
              je.getStartTime(),
              je.getCreateTime(),
              je.getEndTime(),
              je.getLastUpdated(),
              je.getExitStatus()))
          .toList();
    }
    return List.of();
  }

  @SneakyThrows
  private JobExecution runJob(Job job, JobParameters jobParameters) {
    var withRunId = new HashMap<>(jobParameters.getParameters());
    withRunId.put("run.id", new JobParameter<>(UUID.randomUUID().toString(), String.class));

    return jobLauncher.run(job, new JobParameters(withRunId));
  }
}
