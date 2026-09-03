/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.templates;

import com.intellias.mobility.statistic.framework.elastic.ElasticManageClient;
import com.intellias.mobility.statistic.framework.elastic.model.AcknowledgementResponse;
import com.intellias.mobility.statistic.framework.elastic.model.IndexTemplatesResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IndexTemplateManagerImpl implements IndexTemplateManager {
  private final String INDEX_TEMPLATE_KEY = "/_index_template/";
  private final String INDEX_COMPONENT_TEMPLATE_KEY = "/_component_template/";

  private final ElasticManageClient elasticManageClient;

  public AcknowledgementResponse createComponentTemplate(
      String componentTemplateName, String body) {
    return elasticManageClient.perform(
        "PUT",
        INDEX_COMPONENT_TEMPLATE_KEY + componentTemplateName,
        body,
        AcknowledgementResponse.class);
  }

  public AcknowledgementResponse createIndexTemplate(String indexTemplateName, String body) {
    return elasticManageClient.perform(
        "PUT", INDEX_TEMPLATE_KEY + indexTemplateName, body, AcknowledgementResponse.class);
  }

  @Override
  public IndexTemplatesResponse getIndexTemplate(String indexTemplateName) {
    return elasticManageClient.perform(
        "GET", INDEX_TEMPLATE_KEY + indexTemplateName, IndexTemplatesResponse.class);
  }
}
