/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob;

import com.intellias.mobility.statistic.framework.analytics.job.AnalyticsJobDefinitionProvider;
import com.intellias.mobility.statistic.framework.common.IndexManager;
import com.intellias.mobility.statistic.framework.config.AnalyticsProperties;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

@RequiredArgsConstructor
public class DifferenceJobDefinitionProvider implements AnalyticsJobDefinitionProvider {
  public static final String SOURCE_VERSION_KEY = "source_version";
  public static final String TARGET_VERSION_KEY = "target_version";

  private final String jobName;
  private final IndexManager indexManager;
  private final Job job;
  private final String sourceIndexParamKey;
  private final String targetIndexParamKey;
  private final StorageProperties storageProperties;
  private final AnalyticsProperties analyticsProperties;

  @Override
  public String jobName() {
    return jobName;
  }

  @Override
  public String jobDescription() {
    return """
       This job locates all feature indices, reads features and create difference between them if feature with a given featureId has two versions.
       If not, then it will be added to 'added' or 'deleted' set. \n
       Also job accumulates general statistic from two versions across index and at the end makes general difference between them. \n
           It accepts two request parameters—source_version and target_version—to find certain features by featureId and version. \n
           For example. \n
           { \n
             "source_version": "1", \n
             "target_version": "2" \n
           } \n
      """;
  }

  @Override
  public Job job() {
    return job;
  }

  @Override
  public List<JobParameters> jobParameters(Map<String, Object> requestParameters) {
    return indexManager.getAllFeatureIndexList().stream()
        .map(indexName -> this.buildJobParameters(indexName, requestParameters))
        .toList();
  }

  @Override
  public List<JobParameters> initialJobParameters() {
    return indexManager.getAllFeatureIndexList().stream()
        .map(indexName -> this.buildJobParameters(indexName, Map.of()))
        .toList();
  }

  private JobParameters buildJobParameters(
      String indexName, Map<String, Object> runtimeParameters) {
    return new JobParameters(Map.of(
        sourceIndexParamKey,
        new JobParameter<>(indexName, String.class),
        targetIndexParamKey,
        new JobParameter<>(buildDestIndex(), String.class),
        SOURCE_VERSION_KEY,
        buildIndexNameParameter(runtimeParameters.getOrDefault(SOURCE_VERSION_KEY, "")),
        TARGET_VERSION_KEY,
        buildIndexNameParameter(runtimeParameters.getOrDefault(TARGET_VERSION_KEY, ""))));
  }

  private String buildDestIndex() {
    return storageProperties.indexPrefix() + analyticsProperties.diffPerFeatureResultIndexPrefix();
  }

  private JobParameter<String> buildIndexNameParameter(Object value) {
    return switch (value) {
      case String index -> new JobParameter<>(index, String.class);
      default -> throw new IllegalStateException("Unexpected value: " + value);
    };
  }
}
