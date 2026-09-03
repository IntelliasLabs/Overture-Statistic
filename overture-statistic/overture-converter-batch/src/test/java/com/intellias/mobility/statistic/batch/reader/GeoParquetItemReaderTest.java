/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.reader;

import static com.intellias.mobility.statistic.util.TestDataUtils.makeInputFile;
import static org.assertj.core.api.Assertions.assertThat;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import com.intellias.mobility.statistic.util.ByteArrayInputFile;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.springframework.batch.item.ExecutionContext;

class GeoParquetItemReaderTest {

  @Test
  void readShouldReturnAllRecords() throws Exception {
    ByteArrayInputFile inputFile = makeInputFile();
    GeoParquetItemReader reader = new GeoParquetItemReader(inputFile, 0, 1, "v1.0");

    try {
      reader.open(new ExecutionContext());

      OvertureItem overtureItem = reader.read();

      Assertions.assertNotNull(overtureItem);

      assertThat(overtureItem.getId()).isEqualTo("23e81262-d6ed-45a3-a1a0-4bc6a2a887d8");

      // geometry verification
      Point geom = (Point) overtureItem.getGeometry();
      assertThat(geom.getX()).isEqualTo(-139.2728);
      assertThat(geom.getY()).isEqualTo(-89.998);

      Map<String, Object> properties = overtureItem.getProperties();
      GenericRecord bbox = (GenericRecord) properties.get("bbox");
      assertThat((Double) bbox.get("xmin")).isEqualTo(-139.27281);
      assertThat((Double) bbox.get("xmax")).isEqualTo(-139.2728);
      assertThat((Double) bbox.get("ymin")).isEqualTo(-89.998);
      assertThat((Double) bbox.get("ymax")).isEqualTo(-89.997986);

      // has each filed was unwrapped correctly
      GenericRecord names = (GenericRecord) properties.get("names");
      assertThat(names.get("primary").toString()).isEqualTo("Amundsen–Scott South Pole");

      Map<CharSequence, CharSequence> rawCommon =
          (Map<CharSequence, CharSequence>) names.get("common");

      Map<String, String> common = rawCommon.entrySet().stream()
          .collect(
              Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
      assertThat(common)
          .hasSize(5)
          .containsEntry("ko", "아문센-스콧 남극점 기지")
          .containsEntry("en", "Amundsen–Scott South Pole Station")
          .containsEntry("hu", "Amundsen-Scott kutatóállomás")
          .containsEntry("mk", "Станица Амундсен-Скот")
          .containsEntry("zh", "阿蒙森-斯科特南极站");

      List<GenericRecord> rules = (List<GenericRecord>) names.get("rules");
      assertThat(rules).hasSize(2);

      GenericRecord rw0 = rules.getFirst();
      assertThat(rw0.get("variant").toString()).isEqualTo("official");
      assertThat(rw0.get("value").toString()).isEqualTo("Amundsen–Scott South Pole Station");

      GenericRecord rw1 = rules.get(1);
      assertThat(rw1.get("variant").toString()).isEqualTo("alternate");
      assertThat(rw1.get("value").toString()).isEqualTo("Amundsen–Scott Base");

      Assertions.assertEquals("v1.0", overtureItem.getVersion());
    } finally {
      reader.close();
    }
  }
}
