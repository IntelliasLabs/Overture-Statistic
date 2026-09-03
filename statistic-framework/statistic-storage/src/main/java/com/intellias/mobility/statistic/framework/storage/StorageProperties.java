/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "statistic-app.storage")
public record StorageProperties(
    String indexPrefix, String featurePropertiesIndexSuffix, int batchSize) {}
