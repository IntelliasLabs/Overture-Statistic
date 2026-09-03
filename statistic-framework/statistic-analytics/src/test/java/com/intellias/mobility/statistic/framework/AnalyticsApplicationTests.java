/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AnalyticsApplicationTests extends TestcontainersConfiguration {

  @Test
  void contextLoads() {}
}
