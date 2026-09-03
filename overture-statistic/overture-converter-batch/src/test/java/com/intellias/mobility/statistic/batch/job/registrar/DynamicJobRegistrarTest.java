/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.registrar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.batch.job.processor.OvertureFeatureProcessor;
import com.intellias.statistic.model.feature.StatisticFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

/**
 * Unit tests for {@link DynamicJobRegistrar}.
 *
 * Verifies that the registrar correctly creates Job and Step bean definitions
 * without needing a full Spring context.
 */
@ExtendWith(MockitoExtension.class)
class DynamicJobRegistrarTest {

  @Test
  @DisplayName("Should register partitioned Job and Step bean definitions for each processor")
  void shouldRegisterPartitionedJobBeanDefinitions() {
    // Given
    var dummyProcessor = new DummyProcessor();
    var processorBeanName = "dummyProcessor";

    var applicationContext = mock(ApplicationContext.class);
    when(applicationContext.getBeanNamesForType(OvertureFeatureProcessor.class))
        .thenReturn(new String[] {processorBeanName});
    when(applicationContext.getBean(processorBeanName, OvertureFeatureProcessor.class))
        .thenReturn(dummyProcessor);

    var beanFactory = new DefaultListableBeanFactory();
    var registrar = new DynamicJobRegistrar();
    registrar.setApplicationContext(applicationContext);

    // When
    registrar.postProcessBeanFactory(beanFactory);

    // Then
    var processorName = dummyProcessor.getProcessorName();
    var expectedJobName = processorName + "Job";
    var expectedManagerStepName = processorName + "ManagerStep";
    var expectedWorkerStepName = processorName + "WorkerStep";

    // Verify all three bean definitions were created
    assertThat(beanFactory.containsBeanDefinition(expectedWorkerStepName)).isTrue();
    assertThat(beanFactory.containsBeanDefinition(expectedManagerStepName)).isTrue();
    assertThat(beanFactory.containsBeanDefinition(expectedJobName)).isTrue();

    // Verify Job definition
    var jobDefinition = beanFactory.getBeanDefinition(expectedJobName);
    assertThat(jobDefinition.getBeanClassName()).isEqualTo(Job.class.getName());
    String managerStepRef = getFirstConstructorArgReference(jobDefinition);
    assertThat(managerStepRef).isEqualTo(expectedManagerStepName);

    // Verify Manager Step definition
    var managerStepDefinition = beanFactory.getBeanDefinition(expectedManagerStepName);
    assertThat(managerStepDefinition.getBeanClassName()).isEqualTo(Step.class.getName());
    String workerStepRef = getFirstConstructorArgReference(managerStepDefinition);
    assertThat(workerStepRef).isEqualTo(expectedWorkerStepName);
  }

  private String getFirstConstructorArgReference(BeanDefinition definition) {
    ConstructorArgumentValues.ValueHolder valueHolder =
        definition.getConstructorArgumentValues().getArgumentValue(1, String.class);
    return ((org.springframework.beans.factory.config.RuntimeBeanReference) valueHolder.getValue())
        .getBeanName();
  }

  /**
   * Simple dummy processor used only for testing purposes.
   */
  private static class DummyProcessor implements OvertureFeatureProcessor<StatisticFeature> {
    @Override
    public String getProcessorName() {
      return "TEST";
    }

    @Override
    public StatisticFeature process(OvertureItem item) {
      return null;
    }
  }
}
