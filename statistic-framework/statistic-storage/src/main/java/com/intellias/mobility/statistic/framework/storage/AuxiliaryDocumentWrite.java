/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.storage;

import java.util.Objects;

/**
 * Describes an additional document that should be written during the same storage operation as the
 * source feature.
 *
 * @param indexName fully resolved Elasticsearch index name
 * @param document auxiliary document instance to persist
 */
public record AuxiliaryDocumentWrite(String indexName, Object document) {
  public AuxiliaryDocumentWrite {
    Objects.requireNonNull(indexName, "indexName must not be null");
    Objects.requireNonNull(document, "document must not be null");
  }
}
