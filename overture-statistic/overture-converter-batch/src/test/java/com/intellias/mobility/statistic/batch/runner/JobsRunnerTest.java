/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.runner;

import static org.mockito.Mockito.*;

import com.intellias.mobility.statistic.batch.config.BatchConfiguration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

class JobsRunnerTest {
  @Mock
  private StatusHandler jobStatusHandler;

  @Mock
  private JobLauncher jobLauncher;

  @Mock
  private JobPrinter jobPrinter;

  @Mock
  private BatchConfiguration batchConfiguration;

  private JobsRunner jobsRunner;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    this.jobsRunner = new JobsRunner(jobStatusHandler, jobLauncher, jobPrinter, batchConfiguration);
  }

  @Test
  void runJobsRunsOnlyEnabledJobsAndPrintsAvailableJobs() throws Exception {
    Job jobOne = mock(Job.class);
    when(jobOne.getName()).thenReturn("jobOne");
    Job jobTwo = mock(Job.class);
    when(jobTwo.getName()).thenReturn("jobTwo");

    JobExecution execution = mock(JobExecution.class);
    when(execution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);

    when(batchConfiguration.getEnabledJobs()).thenReturn(Set.of("jobOne"));
    when(batchConfiguration.isEnabled("jobOne")).thenReturn(true);
    when(batchConfiguration.isEnabled("jobTwo")).thenReturn(false);

    when(batchConfiguration.getParametersFor(anyString())).thenReturn(Map.of("param1", "value1"));
    when(batchConfiguration.getParametersFor("jobOne")).thenReturn(Map.of("version", "1.0"));
    when(batchConfiguration.getParametersFor("jobTwo")).thenReturn(Map.of("version", "1.0"));

    List<Job> availableJobs = List.of(jobOne, jobTwo);
    jobsRunner.runJobs(availableJobs);

    verify(jobPrinter).printAvailableJobs(availableJobs);
    verify(jobLauncher).run(eq(jobOne), any(JobParameters.class));
    verify(jobLauncher, never()).run(eq(jobTwo), any(JobParameters.class));
  }

  @Test
  void runJobsHandlesFailureCorrectly() throws Exception {
    Job failingJob = mock(Job.class);
    when(failingJob.getName()).thenReturn("failingJob");

    JobExecution failedExecution = mock(JobExecution.class);
    when(failedExecution.getExitStatus()).thenReturn(ExitStatus.FAILED);

    when(batchConfiguration.getEnabledJobs()).thenReturn(Set.of("failingJob"));
    when(batchConfiguration.isEnabled("failingJob")).thenReturn(true);
    when(jobLauncher.run(eq(failingJob), any(JobParameters.class))).thenReturn(failedExecution);
    when(batchConfiguration.getParametersFor(anyString())).thenReturn(Map.of());

    List<Job> availableJobs = List.of(failingJob);
    jobsRunner.runJobs(availableJobs);

    verify(jobLauncher).run(eq(failingJob), any(JobParameters.class));
  }
}
