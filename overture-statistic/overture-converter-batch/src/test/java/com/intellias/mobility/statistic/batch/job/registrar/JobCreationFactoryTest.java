/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.registrar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.OvertureFeatureProcessor;
import com.intellias.statistic.model.feature.StatisticFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Unit tests for {@link JobCreationFactory}.
 */
@ExtendWith(MockitoExtension.class)
class JobCreationFactoryTest {

  @Mock
  private JobRepository jobRepository;

  @Mock
  private PlatformTransactionManager transactionManager;

  @Mock
  private ItemReader<OvertureItem> reader;

  @Mock
  private ItemWriter<StatisticFeature> writer;

  @Mock
  private OvertureFeatureProcessor processor;

  @InjectMocks
  private JobCreationFactory jobCreationFactory;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jobCreationFactory, "chunkSize", 1000);
  }

  @Test
  @DisplayName("Should create a Step with the correct components and name")
  void shouldCreateStepWithCorrectComponents() {
    // Given
    String stepName = "workerStep-OvertureFeatureProcessor";

    // When
    Step step = jobCreationFactory.createWorkerStep(reader, writer, processor);

    // Then
    assertNotNull(step, "Step should not be null");

    assertThat(step.getName()).contains(stepName);

    Object tasklet = ReflectionTestUtils.getField(step, "tasklet");
    Object chunkProcessor = ReflectionTestUtils.getField(tasklet, "chunkProcessor");
    Object itemProcessor = ReflectionTestUtils.getField(chunkProcessor, "itemProcessor");
    Object itemWriter = ReflectionTestUtils.getField(chunkProcessor, "itemWriter");
    Object chunkProvider = ReflectionTestUtils.getField(tasklet, "chunkProvider");
    Object itemReader = ReflectionTestUtils.getField(chunkProvider, "itemReader");

    assertThat(itemProcessor).isEqualTo(processor);
    assertThat(itemWriter).isEqualTo(writer);
    assertThat(itemReader).isEqualTo(reader);
  }

  @Test
  @DisplayName("Should create a Job with the correct name")
  void shouldCreateJobWithCorrectName() {
    // Given
    String jobName = "testJob";
    Step mockStep = mock(Step.class);

    // When
    Job job = jobCreationFactory.createJob(jobName, mockStep);

    // Then
    assertNotNull(job, "Job should not be null");
    assertThat(job.getName()).isEqualTo(jobName);
  }
}
