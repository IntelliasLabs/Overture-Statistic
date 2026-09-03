/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = BatchConfigurationIntegrationTest.TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:/application.properties")
class BatchConfigurationIntegrationTest {

  @Autowired
  private BatchConfiguration batchConfiguration;

  @EnableConfigurationProperties(BatchConfiguration.class)
  static class TestConfig {}

  @Test
  void shouldLoadNewBaseProperties() {
    assertNotNull(batchConfiguration.getInputBasePath(), "inputBasePath should not be null");
    assertNotNull(batchConfiguration.getDataVersion(), "dataVersion should not be null");
    assertTrue(
        batchConfiguration.isEnabled("SegmentJob") || batchConfiguration.isEnabled("LandJob"));
  }

  @Nested
  @SpringBootTest(
      classes = BatchConfigurationIntegrationTest.TestConfig.class,
      properties = {
        "BATCH_ENABLED_JOBS=mockJob",
        "INPUT_BASE_PATH=s3://custom-bucket/data",
        "DATA_VERSION=v999"
      })
  @ActiveProfiles("test")
  @TestPropertySource(locations = "classpath:/application.properties")
  class OverrideTest {
    @Autowired
    private BatchConfiguration batchConfiguration;

    @Test
    void shouldOverrideWithEnvironmentVariables() {
      assertEquals("s3://custom-bucket/data", batchConfiguration.getInputBasePath());
      assertEquals("v999", batchConfiguration.getDataVersion());
      assertTrue(batchConfiguration.isEnabled("mockJob"));

      // Verify that job parameters are resolved correctly
      Map<String, String> buildingParams = batchConfiguration.getParametersFor("BuildingJob");
      assertEquals(
          "s3://custom-bucket/data/theme=buildings/type=building/building.parquet",
          buildingParams.get("inputPath"));
      assertEquals("v999", buildingParams.get("version"));

      Map<String, String> landCoverParams = batchConfiguration.getParametersFor("landCoverJob");
      assertEquals(
          "s3://custom-bucket/data/theme=base/type=land_cover/land_cover.parquet",
          landCoverParams.get("inputPath"));
    }
  }
}
