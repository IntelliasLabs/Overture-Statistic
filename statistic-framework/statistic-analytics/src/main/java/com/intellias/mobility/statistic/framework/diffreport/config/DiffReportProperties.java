/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.diffreport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("statistic-app.analytics.diff-report")
public record DiffReportProperties(String diffReportOutFolderPath) {}
