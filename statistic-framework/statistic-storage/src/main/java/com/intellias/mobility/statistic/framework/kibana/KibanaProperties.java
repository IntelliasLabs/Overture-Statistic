/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.kibana;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "statistic-app.kibana")
public record KibanaProperties(String baseUrl) {}
