/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.property;

import com.intellias.mobility.statistic.framework.property.model.AbstractFeatureProperty;
import com.intellias.statistic.model.feature.StatisticFeature;
import java.util.List;

public interface FeaturePropertyDocumentsBuilder {
  List<AbstractFeatureProperty> buildFeaturePropertyDocuments(StatisticFeature<?> statisticFeature);
}
