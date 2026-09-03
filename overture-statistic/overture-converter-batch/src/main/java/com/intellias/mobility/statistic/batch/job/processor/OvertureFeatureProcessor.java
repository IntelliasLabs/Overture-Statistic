/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.job.processor;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.statistic.model.feature.StatisticFeature;
import org.springframework.batch.item.ItemProcessor;

/**
 * The central interface for creating Overture data converters that are dynamically registered as Spring Batch Jobs.
 * The system automatically scans for all beans that implement this interface and creates a unique Job for each one.
 * To add a new converter, a developer should follow these steps:
 *
 * 1. Create a new class that implements OvertureFeatureProcessor.
 * 2. Annotate the class with @Component so Spring can discover it.
 * 3. Implement the getProcessorName() method to return a unique name (e.g., "POI", "Building").
 * 4. Implement the process(OvertureItem) method with the conversion logic.
 *
 * This dynamic registration mechanism is handled by the DynamicJobRegistrar class, which uses the JobCreationFactory
 * to construct the necessary Job and Step beans at application startup.
 **/
public interface OvertureFeatureProcessor<O extends StatisticFeature>
    extends ItemProcessor<OvertureItem, O> {

  /**
   * Returns the unique name of the processor.
   * This name is used to create the corresponding Job name (e.g., "POI" becomes "POIJob").
   *
   * @return The processor name.
   */
  String getProcessorName();
}
