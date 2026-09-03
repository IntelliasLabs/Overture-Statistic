/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.config;

import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "batch.jobs")
public class BatchConfiguration {

  private Set<String> enabledJobs;
  private Map<String, Map<String, String>> parameters;
  private String inputBasePath;
  private String dataVersion;

  public boolean isEnabled(String jobName) {
    return enabledJobs != null && enabledJobs.contains(jobName);
  }

  public Map<String, String> getParametersFor(String jobName) {
    return parameters != null ? parameters.getOrDefault(jobName, Map.of()) : Map.of();
  }
}
