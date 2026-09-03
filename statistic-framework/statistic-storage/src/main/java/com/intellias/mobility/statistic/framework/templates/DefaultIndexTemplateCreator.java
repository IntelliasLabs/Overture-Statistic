/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.templates;

import com.intellias.mobility.statistic.framework.storage.StorageProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DefaultIndexTemplateCreator implements IndexTemplateCreator {
  private final String pointTemplate =
      """
      {
        "index_patterns": ["$IndexPatternName*"],
        "template": {
          "mappings": {
            "_source": {
              "enabled": true
            },
            "properties": {
              "geometry": {
                "type": "geo_point"
              },
              "timestamp" : {
                "type": "date"
              }
            },
            "dynamic_templates": [
              {
                "geometry_field": {
                  "match": "geometry",
                  "mapping": {
                    "type": "geo_shape"
                  }
                }
              }
            ]
          }
        },
        "_meta": {
          "description": "template for point features"
        }
      }
      """;

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

  final List<String> shapeGeometryTypes = List.of(
      "LineString",
      "Polygon",
      "MultiPoint",
      "MultiLineString",
      "MultiPolygon",
      "GeometryCollection");

  private final StorageProperties properties;

  @Override
  public void createTemplates(IndexTemplateManager templateManager) {
    var pointTemplateName = properties.indexPrefix() + "-point";
    templateManager.createIndexTemplate(
        pointTemplateName,
        pointTemplate.replace("$IndexPatternName", properties.indexPrefix() + "-point"));
    log.info("Created Default index template: {}", pointTemplateName);

    shapeGeometryTypes.forEach(shapeGeo -> {
      var templateName = properties.indexPrefix() + "-" + shapeGeo.toLowerCase();
      templateManager.createIndexTemplate(
          templateName,
          shapeTemplate.replace(
              "$IndexPatternName", properties.indexPrefix() + "-" + shapeGeo.toLowerCase()));

      log.info("Created Default index template: {}", templateName);
    });
  }
}
