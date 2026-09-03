/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.registrar;

import com.intellias.mobility.statistic.batch.job.processor.OvertureFeatureProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Registers Spring Batch jobs dynamically at startup with multi-threading support.
 *
 * For each OvertureFeatureProcessor bean, it creates:
 * - a worker step for data processing
 * - a manager step to run workers in parallel
 * - a job that runs both steps
 */
@Component
public class DynamicJobRegistrar implements BeanFactoryPostProcessor, ApplicationContextAware {

  private static final String WORKER_STEP = "WorkerStep";
  private static final String JOB_SUFFIX = "Job";
  private static final String CREATE_JOB_METHOD_NAME = "createJob";
  private static final String JOB_CREATION_FACTORY_BEAN_NAME = "jobCreationFactory";
  private static final String PARQUET_ITEM_READER_BEAN_NAME = "parquetItemReader";
  private static final String STATISTIC_FEATURE_WRITER_BEAN_NAME = "statisticFeatureWriter";
  private static final String MANAGER_STEP = "ManagerStep";
  private static final String CREATE_WORKER_STEP = "createWorkerStep";
  private static final String CREATE_MANAGER_STEP = "createManagerStep";

  private ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;

    String[] processorBeanNames =
        applicationContext.getBeanNamesForType(OvertureFeatureProcessor.class);

    for (String beanName : processorBeanNames) {
      OvertureFeatureProcessor processor =
          applicationContext.getBean(beanName, OvertureFeatureProcessor.class);
      String processorName = processor.getProcessorName();
      String jobName = processorName + JOB_SUFFIX;
      String managerStepName = processorName + MANAGER_STEP;
      String workerStepName = processorName + WORKER_STEP;

      // Register the worker step definition (handles actual data processing)
      BeanDefinitionBuilder workerStepBuilder = BeanDefinitionBuilder.genericBeanDefinition(
              Step.class)
          .setFactoryMethodOnBean(CREATE_WORKER_STEP, JOB_CREATION_FACTORY_BEAN_NAME)
          .addConstructorArgReference(PARQUET_ITEM_READER_BEAN_NAME)
          .addConstructorArgReference(STATISTIC_FEATURE_WRITER_BEAN_NAME)
          .addConstructorArgReference(beanName);
      registry.registerBeanDefinition(workerStepName, workerStepBuilder.getBeanDefinition());

      // Register the manager step definition (coordinates worker step execution)
      BeanDefinitionBuilder managerStepBuilder = BeanDefinitionBuilder.genericBeanDefinition(
              Step.class)
          .setFactoryMethodOnBean(CREATE_MANAGER_STEP, JOB_CREATION_FACTORY_BEAN_NAME)
          .addConstructorArgValue(managerStepName)
          .addConstructorArgReference(workerStepName);
      registry.registerBeanDefinition(managerStepName, managerStepBuilder.getBeanDefinition());

      // Register the job definition (encapsulates both steps)
      BeanDefinitionBuilder jobBuilder = BeanDefinitionBuilder.genericBeanDefinition(Job.class)
          .setFactoryMethodOnBean(CREATE_JOB_METHOD_NAME, JOB_CREATION_FACTORY_BEAN_NAME)
          .addConstructorArgValue(jobName)
          .addConstructorArgReference(managerStepName);
      registry.registerBeanDefinition(jobName, jobBuilder.getBeanDefinition());
    }
  }
}
