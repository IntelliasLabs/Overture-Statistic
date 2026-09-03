/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.templates;

import com.intellias.mobility.statistic.framework.elastic.model.AcknowledgementResponse;
import com.intellias.mobility.statistic.framework.elastic.model.IndexTemplatesResponse;

public interface IndexTemplateManager {
  /**
   * Create component template, new way of creating index
   * How to create template <a href="https://www.elastic.co/guide/en/elasticsearch/reference/8.17/index-templates.html">please check</a>
   * @param componentTemplateName template name
   * @param body template body json
   * @return response
   */
  AcknowledgementResponse createComponentTemplate(String componentTemplateName, String body);

  /**
   * Create index template,
   * How to create template <a href="https://www.elastic.co/guide/en/elasticsearch/reference/8.17/index-templates.html">please check</a>
   * @param indexTemplateName template name
   * @param body template body json
   * @return response
   */
  AcknowledgementResponse createIndexTemplate(String indexTemplateName, String body);

  IndexTemplatesResponse getIndexTemplate(String indexTemplateName);
}
