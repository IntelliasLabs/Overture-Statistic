/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.writer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.intellias.mobility.statistic.framework.ingres.IngresService;
import com.intellias.statistic.model.feature.LineFeature;
import com.intellias.statistic.model.feature.LineFeatureProperties;
import com.intellias.statistic.model.geometry.LineGeometry;
import com.intellias.statistic.model.geometry.PointGeometry;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

class StatisticFeatureWriterTest {

  private IngresService ingresService;
  private StatisticFeatureWriter writer;

  @BeforeEach
  void setUp() {
    ingresService = mock(IngresService.class);
    writer = new StatisticFeatureWriter(ingresService);
  }

  @Test
  void shouldCallIngresServiceWithLowercasedIndexName() {
    LineFeature feature = createFeature("LineFeature");
    writer.write(Chunk.of(feature));
    verify(ingresService).processAndStoreAll(eq(List.of(feature)), eq("linefeature"));
  }

  @Test
  void shouldRouteToUnknownIndexWhenFeatureTypeIsNull() {
    LineFeature feature = createFeature(null);
    writer.write(Chunk.of(feature));
    verify(ingresService).processAndStoreAll(eq(List.of(feature)), eq("unknown"));
  }

  @Test
  void shouldNotCallIngresServiceWhenChunkIsEmpty() {
    writer.write(Chunk.of());
    verifyNoInteractions(ingresService);
  }

  @Test
  void shouldGroupByIndexAndBatchPerType() {
    LineFeature f1 = createFeature("LineFeature");
    LineFeature f2 = createFeature("PointFeature");
    LineFeature f3 = createFeature(null); // -> "unknown"

    writer.write(Chunk.of(f1, f2, f3));

    verify(ingresService)
        .processAndStoreAll(
            argThat(list -> list.size() == 1 && list.contains(f1)), eq("linefeature"));
    verify(ingresService)
        .processAndStoreAll(
            argThat(list -> list.size() == 1 && list.contains(f2)), eq("pointfeature"));
    verify(ingresService)
        .processAndStoreAll(argThat(list -> list.size() == 1 && list.contains(f3)), eq("unknown"));

    verifyNoMoreInteractions(ingresService);
  }

  // Verifies trimming at '_' (e.g., "road_multiPolygon" -> "road").
  @Test
  void shouldTrimIndexNameBeforeUnderscore() {
    LineFeature feature = createFeature("Road_multiPolygon");
    writer.write(Chunk.of(feature));
    verify(ingresService).processAndStoreAll(eq(List.of(feature)), eq("road"));
  }

  private LineFeature createFeature(String featureType) {
    LineGeometry geometry =
        new LineGeometry(List.of(new PointGeometry(1.0, 2.0), new PointGeometry(3.0, 4.0)));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    ZonedDateTime zonedDateTime = ZonedDateTime.parse("2025-04-06T12:30:00.000+0000", formatter);
    Date date = Date.from(zonedDateTime.toInstant());
    LineFeatureProperties properties = new LineFeatureProperties("1.0", featureType, date);
    return new LineFeature(
        "id-" + (featureType == null ? "null" : featureType), geometry, properties);
  }
}
