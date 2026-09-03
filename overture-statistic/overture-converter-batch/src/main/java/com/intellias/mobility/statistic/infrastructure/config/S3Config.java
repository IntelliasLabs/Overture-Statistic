/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.infrastructure.config;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
@RequiredArgsConstructor
public class S3Config {

  private final S3ConfigProperties props;

  @Bean
  public S3Client s3Client() {
    S3ClientBuilder builder = S3Client.builder();

    if (props.getRegion() == null || props.getRegion().isBlank()) {
      throw new IllegalArgumentException(
          "S3 region must be configured. Please set 's3.region' in your properties.");
    }
    builder.region(Region.of(props.getRegion()));

    if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
      builder.endpointOverride(URI.create(props.getEndpoint()));
      builder.forcePathStyle(true);
    }

    if (props.getAccessKey() != null && !props.getAccessKey().isBlank()) {
      builder.credentialsProvider(StaticCredentialsProvider.create(
          AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())));
    } else {
      builder.credentialsProvider(AwsCredentialsProviderChain.of(
          DefaultCredentialsProvider.create(), AnonymousCredentialsProvider.create()));
    }

    return builder.build();
  }
}
