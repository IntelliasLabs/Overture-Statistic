/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.diffjob;

import static com.intellias.mobility.statistic.framework.analytics.diffjob.DifferenceJobDefinitionProvider.SOURCE_VERSION_KEY;
import static com.intellias.mobility.statistic.framework.analytics.diffjob.DifferenceJobDefinitionProvider.TARGET_VERSION_KEY;

import com.intellias.mobility.statistic.framework.analytics.diffjob.accumulator.StatisticAccumulatorManager;
import com.intellias.mobility.statistic.framework.analytics.diffjob.common.CustomFeatureIdIterator;
import com.intellias.mobility.statistic.framework.analytics.diffjob.elastic.ElasticsearchRepository;
import com.intellias.mobility.statistic.framework.analytics.diffjob.model.DifferencePerFeature;
import com.intellias.mobility.statistic.framework.analytics.job.AnalyticsJobConfiguration;
import com.intellias.mobility.statistic.framework.common.IndexManager;
import com.intellias.mobility.statistic.framework.config.AnalyticsProperties;
import com.intellias.mobility.statistic.framework.elastic.ElasticsearchItemWriter;
import com.intellias.mobility.statistic.framework.storage.StorageProperties;
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
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@ConditionalOnBean({AnalyticsJobConfiguration.class})
@AutoConfiguration(after = {AnalyticsJobConfiguration.class})
@EnableConfigurationProperties({StorageProperties.class, AnalyticsProperties.class})
public class DifferenceJobConfiguration {
  public static final String JOB_NAME = "featureDifferenceJob";

  public static final String SOURCE_INDEX_PARAM_KEY = "sourceIndexName";
  public static final String TARGET_INDEX_PARAM_KEY = "targetIndexName";
  public static final String JOB_ID_KEY = "run.id";

  private static final String STEP_NAME = "featureDifferenceStep";
  private static final String ITEM_READER = "featureDifferenceReader";
  private static final String ITEM_WRITER = "featureDifferenceWriter";
  private static final String ITEM_PROCESSOR = "featureDifferenceProcessor";

  private final ElasticsearchOperations elasticsearchOperations;
  private final IndexManager indexManager;
  private final ElasticsearchRepository elasticsearchRepository;
  private final StatisticAccumulatorManager statisticAccumulatorManager;
  private final StorageProperties storageProperties;
  private final AnalyticsProperties analyticsProperties;

  @Bean
  @Qualifier(JOB_NAME) Job featureDifferenceJob(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      DifferenceJobListener differenceJobListener,
      DifferenceStepLoggingListener differenceStepLoggingListener,
      @Qualifier(ITEM_READER) FeatureIdReader featureDifferenceReader,
      @Qualifier(ITEM_PROCESSOR) StatisticAccumulatorProcessor<?> featureDifferenceProcessor,
      @Qualifier(ITEM_WRITER) ElasticsearchItemWriter<DifferencePerFeature> featureDifferenceWriter) {

    Step step = new StepBuilder(STEP_NAME, jobRepository)
        .<String, DifferencePerFeature>chunk(1, transactionManager)
        .listener(differenceStepLoggingListener)
        .reader(featureDifferenceReader)
        .processor(featureDifferenceProcessor)
        .writer(featureDifferenceWriter)
        .build();

    return new JobBuilder(JOB_NAME, jobRepository)
        .incrementer(new RunIdIncrementer())
        .listener(differenceJobListener)
        .start(step)
        .build();
  }

  @Bean
  @StepScope
  @Qualifier(ITEM_READER) FeatureIdReader featureSourceIdReader(
      @Value("#{jobParameters['" + SOURCE_INDEX_PARAM_KEY + "']}") String sourceIndex) {
    var customIterator = new CustomFeatureIdIterator(elasticsearchRepository, sourceIndex);
    return new FeatureIdReader(customIterator);
  }

  @Bean
  @StepScope
  @Qualifier(ITEM_PROCESSOR) StatisticAccumulatorProcessor<?> featureDifferenceProcessor(
      @Value("#{jobParameters['" + SOURCE_INDEX_PARAM_KEY + "']}") String sourceIndexName,
      @Value("#{jobParameters['" + SOURCE_VERSION_KEY + "']}") String sourceVersion,
      @Value("#{jobParameters['" + TARGET_VERSION_KEY + "']}") String targetVersion,
      @Value("#{jobParameters['" + JOB_ID_KEY + "']}") String jobId) {
    return new StatisticAccumulatorProcessor<>(
        statisticAccumulatorManager.getAccumulator(jobId),
        elasticsearchRepository,
        sourceIndexName,
        sourceVersion,
        targetVersion);
  }

  @Bean
  @StepScope
  @Qualifier(ITEM_WRITER) ElasticsearchItemWriter<DifferencePerFeature> featureDifferenceWriter(
      @Value("#{jobParameters['" + TARGET_INDEX_PARAM_KEY + "']}") String targetIndexName) {
    return new ElasticsearchItemWriter<>(elasticsearchOperations, targetIndexName);
  }

  @Bean
  DifferenceStepLoggingListener differenceStepLoggingListener() {
    return new DifferenceStepLoggingListener();
  }

  @Bean
  DifferenceJobDefinitionProvider differenceJobDefinitionProvider(
      @Qualifier(JOB_NAME) Job featureDifferenceJob) {
    return new DifferenceJobDefinitionProvider(
        JOB_NAME,
        indexManager,
        featureDifferenceJob,
        SOURCE_INDEX_PARAM_KEY,
        TARGET_INDEX_PARAM_KEY,
        storageProperties,
        analyticsProperties);
  }
}
