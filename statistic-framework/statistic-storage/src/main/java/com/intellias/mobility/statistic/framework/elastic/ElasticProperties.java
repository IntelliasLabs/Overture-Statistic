/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.elastic;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "statistic-app.elastic")
public record ElasticProperties(
    String baseUrl,
    String token,
    @DefaultValue("30s") Duration socketTimeout,
    @DefaultValue("10s") Duration connectionTimeout) {}
