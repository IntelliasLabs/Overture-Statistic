/**
 Copyright ©2025 Intellias
 */
package com.intellias.mobility.statistic.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.parquet.io.DelegatingSeekableInputStream;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.io.SeekableInputStream;

public class ByteArrayInputFile implements InputFile {
  private final byte[] data;

  public ByteArrayInputFile(byte[] data) {
    this.data = data;
  }

  @Override
  public long getLength() {
    return data.length;
  }

  @Override
  public SeekableInputStream newStream() throws IOException {
    return new DelegatingSeekableInputStream(new SeekableByteArrayInputStream(data)) {
      @Override
      public void seek(long newPos) throws IOException {
        ((SeekableByteArrayInputStream) getStream()).setPos((int) newPos);
      }

      @Override
      public long getPos() throws IOException {
        return ((SeekableByteArrayInputStream) getStream()).getPos();
      }
    };
  }

  private static class SeekableByteArrayInputStream extends ByteArrayInputStream {
    public SeekableByteArrayInputStream(byte[] buf) {
      super(buf);
    }

    public void setPos(int pos) {
      this.pos = pos;
    }

    public int getPos() {
      return this.pos;
    }
  }
}
