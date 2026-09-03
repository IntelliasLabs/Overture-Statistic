/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic;

import com.intellias.mobility.statistic.infrastructure.config.S3ConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(S3ConfigProperties.class)
public class OvertureConverterBatchApplication {

  public static void main(String[] args) {
    SpringApplication.run(OvertureConverterBatchApplication.class, args);
  }
}
