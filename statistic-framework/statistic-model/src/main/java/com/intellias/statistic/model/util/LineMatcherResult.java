/**
 Copyright ©2025 Intellias
 */
package com.intellias.statistic.model.util;

import java.util.Map;

public record LineMatcherResult(
    Map<String, Double> orphansACoverage,
    Map<String, Double> orphansBCoverage,
    Map<String, Map<String, Double>> aToBRelations,
    Map<String, Map<String, Double>> bToARelations) {}
