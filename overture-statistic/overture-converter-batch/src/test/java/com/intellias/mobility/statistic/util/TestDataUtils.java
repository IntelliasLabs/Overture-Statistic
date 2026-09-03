/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKBWriter;

public class TestDataUtils {
  public static ByteArrayInputFile makeInputFile() throws IOException {
    // providing schema for creating GenericRecord
    String avsc =
        """
                        {
                          "namespace": "com.example.demo.schema",
                          "type": "record",
                          "name": "DivisionRecord",
                          "fields": [
                            { "name": "id",                 "type": "string" },
                            { "name": "geometry",           "type": "bytes" },
                            { "name": "bbox",
                              "type": {
                                "name": "BBox","type":"record","fields":[
                                  { "name":"xmin","type":"double" },
                                  { "name":"xmax","type":"double" },
                                  { "name":"ymin","type":"double" },
                                  { "name":"ymax","type":"double" }
                                ]
                              }
                            },
                            { "name":"names",
                              "type":{
                                "name":"Names","type":"record","fields":[
                                  { "name":"primary",    "type":"string" },
                                  { "name":"common",
                                    "type":{
                                      "type":"map","values":"string"
                                    }
                                  },
                                  { "name":"rules",
                                    "type":{
                                      "type":"array","items":{
                                        "name":"NameRuleWrapper","type":"record","fields":[
                                          { "name":"element",
                                            "type":{
                                              "name":"NameRule","type":"record","fields":[
                                                { "name":"variant",     "type":"string" },
                                                { "name":"language",    "type":["null","string"], "default":null },
                                                { "name":"perspectives","type":["null","string"], "default":null },
                                                { "name":"value",       "type":"string" },
                                                { "name":"between",     "type":["null","string"], "default":null },
                                                { "name":"side",        "type":["null","string"], "default":null }
                                              ]
                                            }
                                          }
                                        ]
                                      }
                                    }
                                  }
                                ]
                              }
                            },
                            { "name": "version",            "type": "int" }
                          ]
                        }

                        """;
    Schema schema = new Schema.Parser().parse(avsc);

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(
            new OutputStreamOutputFile(out))
        .withSchema(schema)
        .build()) {

      // building test record with fields
      GenericRecord r = new GenericData.Record(schema);

      r.put("id", "23e81262-d6ed-45a3-a1a0-4bc6a2a887d8");
      // geometry as WKB bytes:
      WKBWriter wkb = new WKBWriter();
      GeometryFactory gf = new GeometryFactory();
      Point p = gf.createPoint(new Coordinate(-139.2728, -89.998));
      r.put("geometry", wkb.write(p));

      Schema bboxSchema = schema.getField("bbox").schema();
      GenericRecord bbox = new GenericData.Record(bboxSchema);
      bbox.put("xmin", -139.27281);
      bbox.put("xmax", -139.2728);
      bbox.put("ymin", -89.998);
      bbox.put("ymax", -89.997986);
      r.put("bbox", bbox);

      Schema namesSch = schema.getField("names").schema();
      GenericRecord names = new GenericData.Record(namesSch);
      names.put("primary", "Amundsen–Scott South Pole");

      Map<String, String> common = new HashMap<>();
      common.put("ko", "아문센-스콧 남극점 기지");
      common.put("en", "Amundsen–Scott South Pole Station");
      common.put("hu", "Amundsen-Scott kutatóállomás");
      common.put("mk", "Станица Амундсен-Скот");
      common.put("zh", "阿蒙森-斯科特南极站");
      names.put("common", common);

      List<GenericRecord> rulesList = new ArrayList<>();
      Schema ruleWrapSch = namesSch.getField("rules").schema().getElementType();
      Schema ruleSch = ruleWrapSch.getField("element").schema();

      GenericRecord ruleElem = new GenericData.Record(ruleSch);
      ruleElem.put("variant", "official");
      ruleElem.put("language", null);
      ruleElem.put("perspectives", null);
      ruleElem.put("value", "Amundsen–Scott South Pole Station");
      ruleElem.put("between", null);
      ruleElem.put("side", null);
      GenericRecord rw = new GenericData.Record(ruleWrapSch);
      rw.put("element", ruleElem);
      rulesList.add(rw);

      ruleElem = new GenericData.Record(ruleSch);
      ruleElem.put("variant", "alternate");
      ruleElem.put("language", null);
      ruleElem.put("perspectives", null);
      ruleElem.put("value", "Amundsen–Scott Base");
      ruleElem.put("between", null);
      ruleElem.put("side", null);
      rw = new GenericData.Record(ruleWrapSch);
      rw.put("element", ruleElem);
      rulesList.add(rw);

      names.put("rules", rulesList);

      r.put("names", names);

      r.put("version", 0);

      writer.write(r);
    }

    return new ByteArrayInputFile(out.toByteArray());
  }

  static class OutputStreamOutputFile implements OutputFile {
    private final ByteArrayOutputStream out;

    public OutputStreamOutputFile(ByteArrayOutputStream out) {
      this.out = out;
    }

    @Override
    public PositionOutputStream create(long blockSize) throws IOException {
      return new PositionOutputStream() {
        private long position = 0;

        @Override
        public void write(int b) throws IOException {
          out.write(b);
          position++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
          out.write(b, off, len);
          position += len;
        }

        @Override
        public long getPos() {
          return position;
        }

        @Override
        public void flush() throws IOException {
          out.flush();
        }

        @Override
        public void close() throws IOException {
          out.close();
        }
      };
    }

    @Override
    public PositionOutputStream createOrOverwrite(long blockSize) throws IOException {
      return create(blockSize);
    }

    @Override
    public boolean supportsBlockSize() {
      return false;
    }

    @Override
    public long defaultBlockSize() {
      return 0;
    }
  }
}
