/**
 Copyright ©2024 Intellias
 */
package com.intellias.mobility.statistic.framework.ingres;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for the ingress module. */
@ConfigurationProperties(prefix = "statistic.ingress")
public record IngresProperties() {}
