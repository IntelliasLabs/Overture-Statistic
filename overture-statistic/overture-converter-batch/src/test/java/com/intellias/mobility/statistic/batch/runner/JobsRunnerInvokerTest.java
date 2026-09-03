/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.runner;

import static org.mockito.Mockito.*;

import com.intellias.mobility.statistic.batch.config.BatchConfiguration;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;

class JobsRunnerInvokerTest {

  private JobsRunner jobsRunner;
  private List<Job> availableJobs;
  private JobsRunnerInvoker invoker;

  @BeforeEach
  void setUp() {
    jobsRunner = mock(JobsRunner.class);
    Job job1 = mock(Job.class);
    Job job2 = mock(Job.class);
    availableJobs = List.of(job1, job2);

    BatchConfiguration batchConfiguration = mock(BatchConfiguration.class);
    when(batchConfiguration.getEnabledJobs()).thenReturn(Set.of("job1", "job2"));

    invoker = new JobsRunnerInvoker(jobsRunner, availableJobs);
  }

  @Test
  void onApplicationReadyCallsRunJobsWithCorrectParameters() {
    invoker.onApplicationReady();

    verify(jobsRunner, times(1)).runJobs(availableJobs);
  }
}
