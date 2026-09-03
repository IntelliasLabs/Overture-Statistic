/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.range;

import com.intellias.statistic.model.feature.StatisticFeature;

/**
 * Shared helpers for the stable {@code rangeattribute-*} index naming contract.
 *
 * <p>Ingress uses these helpers when writing derivative range documents, and analytics uses the
 * same prefix when discovering indices for dashboards and index-management features.</p>
 */
public final class RangeAttributeIndexSupport {
  public static final String RANGE_ATTR_INDEX_PREFIX = "rangeattribute";

  private RangeAttributeIndexSupport() {}

  /**
   * Builds the destination range-attribute index name from the source feature metadata.
   */
  public static String buildIndexName(
      String storageIndexPrefix, StatisticFeature<?> feature, String indexSuffixName) {
    return buildIndexName(
        storageIndexPrefix, feature.getGeometry().getType().toLowerCase(), indexSuffixName);
  }

  /**
   * Builds the destination range-attribute index name from explicit geometry information.
   */
  public static String buildIndexName(
      String storageIndexPrefix, String geometryType, String indexSuffixName) {
    return String.format(
        "%s-%s-%s-%s",
        storageIndexPrefix, RANGE_ATTR_INDEX_PREFIX, geometryType.toLowerCase(), indexSuffixName);
  }
}
