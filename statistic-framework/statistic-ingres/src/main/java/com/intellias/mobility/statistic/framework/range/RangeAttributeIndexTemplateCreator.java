/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.range;

import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import com.intellias.mobility.statistic.framework.templates.IndexTemplateCreator;
import com.intellias.mobility.statistic.framework.templates.IndexTemplateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Registers the range-attribute index template from the ingress module.
 *
 * <p>Keeping this bean in ingress aligns template ownership with the runtime component that writes
 * range documents while still participating in the shared {@link IndexTemplateCreator} startup
 * mechanism managed by storage.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class RangeAttributeIndexTemplateCreator implements IndexTemplateCreator {
  private final String shapeTemplate =
      """
      {
        "index_patterns": ["$IndexPatternName*"],
        "template": {
          "mappings": {
            "dynamic_templates": [
              {
                "geometry_field": {
                  "match": "geometry",
                  "mapping": {
                    "type": "geo_shape"
                  }
                }
              },
              {
                "timestamp_field": {
                  "match": "timestamp",
                  "mapping": {
                    "type": "date"
                  }
                }
              }
            ]
          }
        },
        "_meta": {
          "description": "template for mapping geometry and timestamp fields"
        }
      }
      """;

  private final StorageProperties properties;

  @Override
  public void createTemplates(IndexTemplateManager templateManager) {
    var shapeTemplateName =
        properties.indexPrefix() + "-" + RangeAttributeIndexSupport.RANGE_ATTR_INDEX_PREFIX;
    templateManager.createIndexTemplate(
        shapeTemplateName, shapeTemplate.replace("$IndexPatternName", shapeTemplateName));
    log.info("Created range attribute index template: {}", shapeTemplateName);
  }
}
