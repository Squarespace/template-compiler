package com.squarespace.template;

import java.util.List;

import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.Bundle;
import com.squarespace.cldrengine.api.MessageFormatFuncMap;
import com.squarespace.cldrengine.api.MessageFormatter;
import com.squarespace.cldrengine.api.MessageFormatterOptions;

public class MessageFormats {

  private final CLDR cldr;
  private final MessageFormatter formatter;
  private final String zoneId;

  public MessageFormats(CLDR cldr, String zoneId) {
    this.cldr = cldr;
    Bundle bundle = cldr.General.bundle();
    MessageFormatterOptions options = MessageFormatterOptions.build()
        .cacheSize(100)
        .formatters(formatters())
        .language(bundle.language())
        .region(bundle.region());
    this.formatter = new MessageFormatter(options);
    this.zoneId = zoneId;
  }

  public MessageFormatter formatter() {
    return this.formatter;
  }

  private MessageFormatFuncMap formatters() {
    MessageFormatFuncMap map = new MessageFormatFuncMap();
    map.put("currency", this::currency);
    map.put("datetime", this::datetime);
    map.put("number", this::number);
    return map;
  }

  private String currency(List<Object> args, List<String> options) {
    //
    return "";
  }

  private String datetime(List<Object> args, List<String> options) {
    //
    return "";
  }

  private String number(List<Object> args, List<String> options) {
    //
    return "";
  }
}
