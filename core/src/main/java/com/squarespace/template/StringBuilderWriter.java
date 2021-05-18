package com.squarespace.template;

import java.io.IOException;
import java.io.Writer;

public class StringBuilderWriter extends Writer {

  private final StringBuilder buf;

  public StringBuilderWriter(StringBuilder buf) {
    this.buf = buf;
  }

  @Override
  public void close() throws IOException {
    // NOTHING
  }

  @Override
  public void flush() throws IOException {
    // NOTHING
  }

  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    buf.append(cbuf, off, len);
  }
}
