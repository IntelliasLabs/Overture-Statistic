/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.batch.reader;

import com.intellias.mobility.statistic.batch.dto.OvertureItem;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.io.InputFile;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@StepScope
public class GeoParquetItemReader implements ItemStreamReader<OvertureItem> {

  private static final String FIELD_ID = "id";
  private static final String FIELD_GEOMETRY = "geometry";
  private static final String FIELD_VERSION = "version";
  private static final String NAME_OF_WRAPPED_FIELD = "element";
  private static final String CURRENT_ROW_KEY = "parquet.reader.current_row";

  private static final Set<String> SKIP_FIELDS = Set.of(FIELD_ID, FIELD_GEOMETRY, FIELD_VERSION);

  private final InputFile inputFile;

  private final long startRow;
  private final long endRow;
  private final String version;

  private WKBReader wkbReader;
  private ParquetReader<GenericRecord> parquetReader;
  private long currentRow = 0;

  public GeoParquetItemReader(
      InputFile inputFile,
      @Value("#{stepExecutionContext['startRow']}") long startRow,
      @Value("#{stepExecutionContext['endRow']}") long endRow,
      @Value("#{jobParameters['version']}") String version) {
    this.inputFile = inputFile;
    this.startRow = startRow;
    this.endRow = endRow;
    this.version = version;
  }

  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    log.debug("Opening Parquet file.");
    try {
      parquetReader = AvroParquetReader.genericRecordReader(inputFile);
      wkbReader = new WKBReader();

      long rowToSeek = this.startRow;

      if (executionContext.containsKey(CURRENT_ROW_KEY)) {
        long restoredRow = executionContext.getLong(CURRENT_ROW_KEY);
        log.info(
            "Restarting reader. Found saved state at row: {}. Current partition starts at: {}.",
            restoredRow,
            this.startRow);
        rowToSeek = Math.max(this.startRow, restoredRow);
      }

      this.currentRow = rowToSeek;

      if (rowToSeek > 0) {
        log.debug("Seeking to effective start row: {} in file", rowToSeek);
        for (long i = 0; i < rowToSeek; i++) {
          if (parquetReader.read() == null) {
            log.warn("File ended before reaching the seek row {}.", rowToSeek);
            this.currentRow = this.endRow + 1;
            return;
          }
        }
      }

      log.info("Reader opened. Starting to read from row: {}", this.currentRow);

    } catch (IOException e) {
      throw new ItemStreamException("Failed to open or seek in Parquet file. " + e);
    }
  }

  @Override
  public OvertureItem read() {
    if (currentRow > endRow) {
      return null;
    }

    try {
      GenericRecord record = parquetReader.read();

      if (record != null) {
        currentRow++;
        // some fields of type GenericRecord maybe wrapped with field "element"
        // this method unwrap any GenericRecord field
        unwrapElementWrappers(record);

        return getOvertureItem(record);
      }

      return null;
    } catch (ParseException e) {
      throw new ItemStreamException("Failed to read geometry field in file contents.", e);
    } catch (IOException e) {
      throw new ItemStreamException("Failed to read file contents.", e);
    }
  }

  @Override
  public void close() throws ItemStreamException {
    if (parquetReader != null) {
      try {
        parquetReader.close();
        log.debug("Closed ParquetReader for file");
      } catch (IOException e) {
        throw new ItemStreamException("Failed to close ParquetReader", e);
      }
    }

    if (inputFile instanceof AutoCloseable closeable) {
      try {
        closeable.close();
        log.debug("Released cached input file");
      } catch (Exception e) {
        throw new ItemStreamException("Failed to release cached input file", e);
      }
    }
  }

  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    executionContext.putLong(CURRENT_ROW_KEY, currentRow);
    log.debug("Saving reader state: currentRow = {}", currentRow);
  }

  private OvertureItem getOvertureItem(GenericRecord record) throws ParseException {
    OvertureItem overtureItem = new OvertureItem();

    overtureItem.setId(record.get(FIELD_ID).toString());
    overtureItem.setGeometry(getGeometryFromRecord(record));
    overtureItem.setVersion(version);

    HashMap<String, Object> properties = new HashMap<>();

    for (Schema.Field field : record.getSchema().getFields()) {
      String name = field.name();
      if (SKIP_FIELDS.contains(name)) {
        continue;
      }
      properties.put(name, record.get(name));
    }

    overtureItem.setProperties(properties);
    return overtureItem;
  }

  private Geometry getGeometryFromRecord(GenericRecord record) throws ParseException {
    ByteBuffer byteBuffer = (ByteBuffer) record.get(FIELD_GEOMETRY);
    Geometry geometry = null;

    if (byteBuffer != null && byteBuffer.remaining() > 0) {
      byte[] geometryBytes = new byte[byteBuffer.remaining()];
      // transfer bytes from buffer to byte array
      byteBuffer.get(geometryBytes);
      // reading bytes to Geometry
      geometry = wkbReader.read(geometryBytes);
    }

    if (geometry == null || geometry.isEmpty()) {
      throw new ParseException("Geometry is null or empty");
    }

    return geometry;
  }

  private void unwrapElementWrappers(GenericRecord record) {
    for (Schema.Field field : record.getSchema().getFields()) {
      Object value = record.get(field.name());
      Object unwrapped = unwrapGenericRecordFields(value);
      if (unwrapped != value) {
        record.put(field.name(), unwrapped);
      }
    }
  }

  private Object unwrapGenericRecordFields(Object value) {
    if (value instanceof GenericRecord record) {
      return unwrapGenericRecord(record);
    }
    if (value instanceof List<?> list) {
      return unwrapList(list);
    }

    return value;
  }

  private Object unwrapGenericRecord(GenericRecord record) {
    Schema schema = record.getSchema();
    // unwrap single‑field wrapper
    if (schema.getFields().size() == 1
        && NAME_OF_WRAPPED_FIELD.equals(schema.getFields().getFirst().name())) {
      return unwrapGenericRecordFields(record.get(NAME_OF_WRAPPED_FIELD));
    }
    // recurse into each of the field and check is there any wrapped field
    for (Schema.Field field : schema.getFields()) {
      Object inner = record.get(field.name());
      Object unwrapped = unwrapGenericRecordFields(inner);
      if (unwrapped != inner) {
        record.put(field.name(), unwrapped);
      }
    }
    return record;
  }

  private Object unwrapList(List<?> list) {
    List<Object> newList = new ArrayList<>(list.size());
    boolean changed = false;
    for (Object item : list) {
      Object unwrapped = unwrapGenericRecordFields(item);
      newList.add(unwrapped);
      if (unwrapped != item) {
        changed = true;
      }
    }
    if (changed) {
      return newList.size() == 1 ? newList.getFirst() : newList;
    }
    return list;
  }
}
