/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.config;

import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "statistic-app.analytics")
public record AnalyticsProperties(
    Optional<String> indexWidgetsJson,
    String diffPerFeatureTypeResultIndexPrefix,
    String diffPerFeatureResultIndexPrefix) {}
