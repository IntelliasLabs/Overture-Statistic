/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import java.util.List;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;

/**
 * The interface describes the job definitions required to be managed by AnalyticsJobExecutor.
 * Requirements:
 *  <b>
 *    <ul>
 *      <li>The jobName method should return the same name that the job has in the job() method.</li>
 *      <li>The Job should be a Spring bean.</li>
 *      <li>jobParameters() specifies the parameters for a template job; these parameters will be provided during the starting phase.</li>
 *    </ul>
 *  </b>
 */
public interface AnalyticsJobDefinitionProvider {
  String jobName();

  String jobDescription();

  Job job();

  /**
   * Build job parameters based on request parameters if such designed.
   * @return built parameters initial + requests parameters.
   */
  List<JobParameters> jobParameters(Map<String, Object> requestParameters);

  /**
   * Parameters that job will build by itself.
   */
  List<JobParameters> initialJobParameters();
}
