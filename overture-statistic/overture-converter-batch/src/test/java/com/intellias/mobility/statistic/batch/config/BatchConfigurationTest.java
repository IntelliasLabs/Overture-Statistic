/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BatchConfigurationTest {

  private BatchConfiguration batchConfiguration;

  @BeforeEach
  void setUp() {
    batchConfiguration = new BatchConfiguration();
  }

  @Test
  void isEnabledReturnsTrueIfJobIsEnabled() {
    batchConfiguration.setEnabledJobs(Set.of("jobOne", "jobTwo"));

    assertTrue(batchConfiguration.isEnabled("jobOne"));
    assertTrue(batchConfiguration.isEnabled("jobTwo"));
  }

  @Test
  void isEnabledReturnsFalseIfJobIsNotEnabled() {
    batchConfiguration.setEnabledJobs(Set.of("jobTwo", "jobThree"));

    assertFalse(batchConfiguration.isEnabled("jobOne"));
  }

  @Test
  void isEnabledReturnsFalseIfJobsAreNotEnabled() {
    batchConfiguration.setEnabledJobs(null);
    assertFalse(batchConfiguration.isEnabled("jobOne"));
  }

  @Test
  void getParametersForShouldReturnEmptyMapIfNotParametersForJob() {
    batchConfiguration.setParameters(Map.of("jobTwo", Map.of()));
    assertTrue(batchConfiguration.getParametersFor("jobTwo").isEmpty());
  }

  @Test
  void getParametersForShouldReturnParametersForJob() {
    Map<String, String> params = Map.of("param1", "value1", "param2", "value2");
    batchConfiguration.setParameters(Map.of("jobOne", params));
    assertEquals(batchConfiguration.getParametersFor("jobOne"), params);
  }
}
