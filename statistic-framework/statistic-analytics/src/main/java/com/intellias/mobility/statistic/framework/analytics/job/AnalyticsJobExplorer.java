/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.explore.JobExplorer;

@RequiredArgsConstructor
public class AnalyticsJobExplorer {

  private static final int PAGE_SIZE = 10;

  private final JobExplorer jobExplorer;

  /**
   * Verify which combination of jobName & jobParameters is running now.
   * The verification is based only on the provided parameters.
   *   - If a matching jobInstance is running, add an entry with the actual & provided params to 'runningJobs'.
   *   - Otherwise, add an entry with just the provided parameters to 'notRunningJobs'.
   *
   * "Matching" means: for each key in the provided parameters, the job instance's latest execution
   * must have the same key/value. Extra parameters in the job instance are ignored.
   */
  public IsJobsRunningResponse isJobsRunning(String jobName, List<JobParameters> jobParameters) {

    record ParamExecution(JobParameters params, JobExecution execution) {}

    List<ParamExecution> paramExecutions = jobParameters.stream()
        .map(p -> new ParamExecution(p, findMatchingRunningExecution(jobName, p)))
        .toList();

    Map<Boolean, List<ParamExecution>> partitioned =
        paramExecutions.stream().collect(Collectors.partitioningBy(pe -> pe.execution != null));

    List<RunningJobInfo> runningJobs = partitioned.get(true).stream()
        .map(pe -> {
          JobExecution exec = pe.execution;
          return new RunningJobInfo(
              exec.getJobInstance().getInstanceId(),
              toSimpleMap(pe.params),
              toSimpleMap(exec.getJobParameters()));
        })
        .toList();

    List<JobParameters> notRunningJobs =
        partitioned.get(false).stream().map(pe -> pe.params).toList();

    return new IsJobsRunningResponse(runningJobs, notRunningJobs);
  }

  /**
   * Finds a running job execution (if any) for the given jobName that "matches" the providedParams.
   * "Matches" means all keys in providedParams have identical values in the actual job parameters,
   * ignoring extra keys in the actual parameters.
   */
  private JobExecution findMatchingRunningExecution(String jobName, JobParameters providedParams) {
    return fetchAllJobInstances(jobName)
        .flatMap(instance -> {
          List<JobExecution> execs = new ArrayList<>(jobExplorer.getJobExecutions(instance));
          execs.sort(Comparator.comparing(JobExecution::getCreateTime).reversed());
          return execs.stream();
        })
        .filter(exec -> matchesAtLeastTheseParams(exec.getJobParameters(), providedParams))
        .filter(JobExecution::isRunning)
        .findFirst()
        .orElse(null);
  }

  private Stream<JobInstance> fetchAllJobInstances(String jobName) {
    return Stream.iterate(0, startIndex -> startIndex + PAGE_SIZE)
        .map(startIndex -> jobExplorer.findJobInstancesByJobName(jobName, startIndex, PAGE_SIZE))
        .takeWhile(list -> !list.isEmpty())
        .flatMap(List::stream);
  }

  /**
   * Returns true if for every (key,value) in 'requiredParams',
   * the 'actual' JobParameters has the same key with an equal value.
   * Extra keys in 'actual' are ignored.
   */
  private boolean matchesAtLeastTheseParams(JobParameters actual, JobParameters required) {
    Map<String, JobParameter<?>> actualParams = actual.getIdentifyingParameters();
    Map<String, JobParameter<?>> requiredParams = required.getIdentifyingParameters();

    return requiredParams.entrySet().stream()
        .allMatch(entry -> actualParams.containsKey(entry.getKey())
            && Objects.equals(actualParams.get(entry.getKey()), entry.getValue()));
  }

  private Map<String, Object> toSimpleMap(JobParameters jobParameters) {
    return toSimpleMap(jobParameters.getParameters());
  }

  private Map<String, Object> toSimpleMap(Map<String, JobParameter<?>> paramMap) {
    return paramMap.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey, e -> e.getValue().getValue(), (v1, v2) -> v1, LinkedHashMap::new));
  }

  // -------------------------------------------------------------------------
  // POJO classes for the 'isJobsRunning' response
  // -------------------------------------------------------------------------
  public record IsJobsRunningResponse(
      List<RunningJobInfo> runningJobs, List<JobParameters> notRunningJobs) {}

  public record RunningJobInfo(
      Long jobInstanceId,
      Map<String, Object> providedParameters,
      Map<String, Object> actualParameters) {}
}
