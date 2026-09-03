/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.registrar;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.partitioner.MultiResourcePartitioner;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.net.SocketTimeoutException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.io.ParseException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A factory for creating Spring Batch Job and Step beans with partitioning and parallel execution.
 * Used by the DynamicJobRegistrar to build jobs dynamically from bean definitions.
 */
@Configuration("jobCreationFactory")
@RequiredArgsConstructor
public class JobCreationFactory {

  public static final int RETRY_LIMIT = 8;
  public static final int SKIP_LIMIT = 100;
  public static final String THREAD_NAME_PREFIX = "batch-worker-";
  public static final long INITIAL_INTERVAL = 1000L;
  public static final double MULTIPLIER = 2.0;
  public static final long MAX_INTERVAL = 30000L;

  @Value("${batch.jobs.chunk-size:1000}")
  private int chunkSize;

  @Value("${batch.jobs.grid-size:10}")
  private int gridSize;

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final MultiResourcePartitioner partitioner;

  public Step createWorkerStep(
      ItemReader<OvertureItem> reader,
      ItemWriter<StatisticFeature> writer,
      ItemProcessor<OvertureItem, StatisticFeature> processor) {
    return new StepBuilder("workerStep-" + processor.getClass().getSimpleName(), jobRepository)
        .<OvertureItem, StatisticFeature>chunk(chunkSize, transactionManager)
        .faultTolerant()
        .retryLimit(RETRY_LIMIT)
        .retry(DataAccessResourceFailureException.class)
        .retry(SocketTimeoutException.class)
        .retry(CannotCreateTransactionException.class)
        .backOffPolicy(buildBackoffPolicy())
        .skipLimit(SKIP_LIMIT)
        .skip(ParseException.class)
        .skip(NumberFormatException.class)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  public Step createManagerStep(String stepName, Step workerStep) {
    return new StepBuilder(stepName, jobRepository)
        .partitioner(workerStep.getName(), partitioner)
        .step(workerStep)
        .gridSize(gridSize)
        .taskExecutor(taskExecutor())
        .build();
  }

  public Job createJob(String jobName, Step managerStep) {
    return new JobBuilder(jobName, jobRepository).start(managerStep).build();
  }

  private TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(gridSize);
    executor.setMaxPoolSize(gridSize);
    executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
    executor.initialize();
    return executor;
  }

  private BackOffPolicy buildBackoffPolicy() {
    ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
    policy.setInitialInterval(INITIAL_INTERVAL);
    policy.setMultiplier(MULTIPLIER);
    policy.setMaxInterval(MAX_INTERVAL);
    return policy;
  }
}
