/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.elastic.model;

import java.util.List;
import java.util.Map;

public record IndexTemplateDetails(
    List<String> index_patterns,
    Map<String, Object> template,
    int priority,
    Map<String, String> _meta) {}
