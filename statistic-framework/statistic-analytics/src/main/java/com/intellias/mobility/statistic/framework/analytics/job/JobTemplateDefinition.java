/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.analytics.job;

import java.util.List;
import org.springframework.batch.core.JobParameters;

public record JobTemplateDefinition(
    String name, String description, List<JobParameters> parameters) {}
