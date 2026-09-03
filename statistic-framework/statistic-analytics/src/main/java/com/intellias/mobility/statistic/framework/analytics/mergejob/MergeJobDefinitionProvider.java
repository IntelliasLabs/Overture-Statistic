/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.mergejob;

import com.intellias.mobility.statistic.framework.analytics.job.AnalyticsJobDefinitionProvider;
import com.intellias.mobility.statistic.framework.common.IndexManager;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;

@RequiredArgsConstructor
public class MergeJobDefinitionProvider implements AnalyticsJobDefinitionProvider {
  public static final String MERGE_NAME = "merged";
  private final String jobName;
  private final IndexManager indexManager;
  private final Job job;
  private final String sourceIndexParamKey;
  private final String targetIndexParamKey;

  private String SOURCE_INDEXES = "sourceIndexes";
  private String VERSIONS = "versions";

  @Override
  public String jobName() {
    return jobName;
  }

  @Override
  public String jobDescription() {
    return """
            This job merges geometries of features that share the same globalSourceId and version
            from the specified source indexes. The merged result is then stored in a new index.

            The parameter "sourceIndexes" is required. It should contain a list of index names
            from which the features will be read and merged.

            Optionally, you can provide a "versions" parameter — a list of versions to filter features.
            If "versions" is not specified, features of all available versions will be processed.

            Currently, merging is only supported for the following feature types:
            - multipolygon
            - polygon
            - line
            - multiline

            Example parameters:
            {
                "sourceIndexes": ["statistic-multipolygon-admin-display-area-set", "statistic-multipolygon-display-area-set"],
                "versions": ["v1", "v2"]
            }
            """;
  }

  @Override
  public Job job() {
    return job;
  }

  @Override
  public List<JobParameters> jobParameters(Map<String, Object> requestParameters) {
    List<String> indexNames = (List<String>) requestParameters.get(SOURCE_INDEXES);

    @SuppressWarnings("unchecked")
    List<String> versionsList = (List<String>) requestParameters.get(VERSIONS);
    Optional<Set<String>> versionsOpt = Optional.ofNullable(versionsList).map(HashSet::new);

    return indexNames.stream()
        .map(indexName -> buildJobParameters(indexName, versionsOpt))
        .toList();
  }

  @Override
  public List<JobParameters> initialJobParameters() {
    return indexManager.getFeatureIndexForMergingList().stream()
        .map(indexName -> this.buildJobParameters(indexName, Optional.empty()))
        .toList();
  }

  private JobParameters buildJobParameters(String indexName, Optional<Set<String>> versions) {
    Map<String, JobParameter<?>> params = new HashMap<>();

    params.put(sourceIndexParamKey, new JobParameter<>(indexName, String.class));
    params.put(targetIndexParamKey, new JobParameter<>(buildDestIndex(indexName), String.class));

    versions.ifPresent(ver -> {
      String joinedVersions = String.join(",", ver);
      params.put(VERSIONS, new JobParameter<>(joinedVersions, String.class));
    });

    return new JobParameters(params);
  }

  private String buildDestIndex(String indexName) {
    var newIndexName = changeTargetIndexType(indexName);
    return String.format("%s-%s", newIndexName, MERGE_NAME);
  }

  private String changeTargetIndexType(String sourceIndex) {
    if (sourceIndex.contains("-multipolygon")) {
      return sourceIndex; // multipolygon -> multipolygon
    } else if (sourceIndex.contains("-multiline")) {
      return sourceIndex; // multiline -> multiline
    } else if (sourceIndex.contains("-polygon")) {
      return sourceIndex.replace("-polygon", "-multipolygon"); // polygon -> multipolygon
    } else if (sourceIndex.contains("-line")) {
      return sourceIndex.replace("-line", "-multiline"); // line -> multiline
    }
    throw new IllegalArgumentException("Unsupported index type: " + sourceIndex + " for merging");
  }
}
