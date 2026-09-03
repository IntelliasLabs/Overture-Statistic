/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "s3")
@Data
public class S3ConfigProperties {
  private String endpoint;
  private String accessKey;
  private String secretKey;
  private String region;
  private Integer maxKeys;
}
