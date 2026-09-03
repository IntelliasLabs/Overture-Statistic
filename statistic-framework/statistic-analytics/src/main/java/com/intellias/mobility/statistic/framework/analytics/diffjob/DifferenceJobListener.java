/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob;

import static com.intellias.mobility.statistic.framework.analytics.diffjob.DifferenceJobConfiguration.JOB_ID_KEY;
import static com.intellias.mobility.statistic.framework.analytics.diffjob.util.DifferenceUtil.ADDED_KEY;
import static com.intellias.mobility.statistic.framework.analytics.diffjob.util.DifferenceUtil.DELETED_KEY;

import com.intellias.mobility.statistic.framework.analytics.diffjob.accumulator.StatisticAccumulatorManager;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeatureType;
import com.intellias.mobility.statistic.framework.analytics.diffjob.util.DifferenceUtil;
import com.intellias.mobility.statistic.framework.config.AnalyticsProperties;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties({AnalyticsProperties.class, StorageProperties.class})
public class DifferenceJobListener implements JobExecutionListener {

  private final StatisticAccumulatorManager statisticAccumulatorManager;
  private final ElasticsearchOperations elasticsearchOperations;
  private final AnalyticsProperties analyticsProperties;
  private final StorageProperties storageProperties;

  @Override
  public void afterJob(JobExecution jobExecution) {
    var jobId =
        jobExecution.getJobParameters().getParameter(JOB_ID_KEY).getValue().toString();
    var statistic = statisticAccumulatorManager.getAccumulator(jobId);

    var differencePerFeatureType = new DifferencePerFeatureType();
    differencePerFeatureType.setMetadata(statistic.getMetadata().get());
    differencePerFeatureType.setAddedFeatureIds(
        statistic.getAddedFeatureIds().isEmpty() ? null : statistic.getAddedFeatureIds());
    differencePerFeatureType.setDeletedFeatureIds(
        statistic.getDeletedFeatureIds().isEmpty() ? null : statistic.getDeletedFeatureIds());

    var featurePropertiesDiff = DifferenceUtil.compareMaps(
        statistic.getSourceFeatureProperties().getMap(),
        statistic.getTargetFeatureProperties().getMap());
    differencePerFeatureType.setAddedFeatureProperties(featurePropertiesDiff.get(ADDED_KEY));
    differencePerFeatureType.setDeletedFeatureProperties(featurePropertiesDiff.get(DELETED_KEY));

    var rangeAttributesDiff = DifferenceUtil.compareMaps(
        statistic.getSourceFeatureRangeAttributes().getMap(),
        statistic.getTargetFeatureRangeAttributes().getMap());
    differencePerFeatureType.setAddedRangeAttributes(rangeAttributesDiff.get(ADDED_KEY));
    differencePerFeatureType.setDeletedRangeAttributes(rangeAttributesDiff.get(DELETED_KEY));

    elasticsearchOperations.save(
        differencePerFeatureType,
        IndexCoordinates.of(storageProperties.indexPrefix()
            + analyticsProperties.diffPerFeatureTypeResultIndexPrefix()));

    statisticAccumulatorManager.removeAccumulator(jobId);
  }
}
