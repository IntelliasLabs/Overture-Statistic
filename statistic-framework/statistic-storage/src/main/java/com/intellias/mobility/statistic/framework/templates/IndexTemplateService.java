/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.framework.templates;

import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IndexTemplateService {
  private final List<IndexTemplateCreator> templateCreators;
  private final IndexTemplateManager indexTemplateManager;

  @PostConstruct
  public void init() {
    templateCreators.forEach(tc -> tc.createTemplates(indexTemplateManager));
  }
}
