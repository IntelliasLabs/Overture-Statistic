/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.mergejob;

import com.intellias.mobility.statistic.framework.analytics.job.AnalyticsJobConfiguration;
import com.intellias.mobility.statistic.framework.analytics.mergejob.elastic.MergeElasticsearchRepository;
import com.intellias.mobility.statistic.framework.common.IndexManager;
import com.intellias.mobility.statistic.framework.config.AnalyticsProperties;
import com.intellias.mobility.statistic.framework.elastic.ElasticsearchItemWriter;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.util.Pair;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@ConditionalOnBean({AnalyticsJobConfiguration.class})
@AutoConfiguration(after = {AnalyticsJobConfiguration.class})
@EnableConfigurationProperties({StorageProperties.class, AnalyticsProperties.class})
public class MergeJobConfiguration {
  public static final String JOB_NAME = "mergeGeometryJob";
  public static final String ITEM_READER = "mergeReader";
  public static final String ITEM_PROCESSOR = "mergeProcessor";
  public static final String ITEM_WRITER = "mergeWriter";

  private final IndexManager indexManager;
  private final ElasticsearchOperations elasticsearchOperations;
  private final MergeElasticsearchRepository mergeElasticsearchRepository;

  private static final String SOURCE_INDEX_PARAM_KEY = "sourceIndexName";
  private static final String TARGET_INDEX_PARAM_KEY = "targetIndexName";
  private static final String VERSIONS = "versions";

  @Bean
  @StepScope
  @Qualifier(ITEM_WRITER) public ElasticsearchItemWriter<StatisticFeature<?>> mergeFeaturesItemWriter(
      @Value("#{jobParameters['" + TARGET_INDEX_PARAM_KEY + "']}") String targetIndexName) {
    return new ElasticsearchItemWriter<>(elasticsearchOperations, targetIndexName);
  }

  @Bean
  @StepScope
  @Qualifier(ITEM_READER) public MergeFeatureReader mergeFeaturesReader(
      @Value("#{jobParameters['" + SOURCE_INDEX_PARAM_KEY + "']}") String sourceIndexName,
      @Value("#{jobParameters['" + VERSIONS + "']}") String versionsStr) {

    Set<String> versions = versionsStr != null
        ? new HashSet<>(Arrays.asList(versionsStr.split(",")))
        : Collections.emptySet();

    MergeFeatureIdIterator mergeFeatureIdIterator =
        new MergeFeatureIdIterator(mergeElasticsearchRepository, sourceIndexName, versions);

    return new MergeFeatureReader(mergeFeatureIdIterator);
  }

  @Bean
  @StepScope
  @Qualifier(ITEM_PROCESSOR) public MergeFeaturesProcessor<StatisticFeature<?>> mergeFeaturesProcessor(
      @Value("#{jobParameters['" + SOURCE_INDEX_PARAM_KEY + "']}") String sourceIndexName) {
    return new MergeFeaturesProcessor<>(elasticsearchOperations, sourceIndexName);
  }

  @Bean
  @Qualifier(JOB_NAME) public Job mergeGeometryJob(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      @Qualifier(ITEM_READER) MergeFeatureReader reader,
      @Qualifier(ITEM_PROCESSOR) MergeFeaturesProcessor<StatisticFeature<?>> processor,
      @Qualifier(ITEM_WRITER) ElasticsearchItemWriter<StatisticFeature<?>> writer) {
    Step step = new StepBuilder("mergeGeometryStep", jobRepository)
        .<Pair<String, String>, StatisticFeature<?>>chunk(1, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();

    return new JobBuilder(JOB_NAME, jobRepository)
        .incrementer(new RunIdIncrementer())
        .start(step)
        .build();
  }

  @Bean
  MergeJobDefinitionProvider mergeJobDefinitionProvider(@Qualifier(JOB_NAME) Job mergeJob) {
    return new MergeJobDefinitionProvider(
        JOB_NAME, indexManager, mergeJob, SOURCE_INDEX_PARAM_KEY, TARGET_INDEX_PARAM_KEY);
  }
}
