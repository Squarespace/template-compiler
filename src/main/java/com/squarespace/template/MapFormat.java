package com.squarespace.template;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;


/**
 * Support class for formatting error messages flexibly using a Map.
 */
public class MapFormat {

  private static final Pattern KEY_ESCAPE = Pattern.compile("%\\([a-zA-Z][a-zA-Z0-9]*\\)");
  
  private static final String EMPTY_STRING = "";
  
  private List<String> keys = new ArrayList<>();
  
  private String format;
  
  private String nullPlaceholder = EMPTY_STRING;
  
  public MapFormat(String raw) {
    init(raw);
  }
  
  public MapFormat(String raw, String nullPlaceholder) {
    this.nullPlaceholder = nullPlaceholder;
    init(raw);
  }
  
  private void init(String raw) {
    StringBuffer buf = new StringBuffer();
    Matcher matcher = KEY_ESCAPE.matcher(raw);
    int mark = 0;
    int length = raw.length();
    while (matcher.find()) {
      int start0 = matcher.start();
      if (mark < start0) {
        buf.append(raw.substring(mark, start0));
      }
      buf.append(raw.charAt(start0));
      keys.add(raw.substring(start0 + 2, matcher.end() - 1));
      mark = matcher.end();
    }
    if (mark < length) {
      buf.append(raw.substring(mark, length));
    }
    this.format = buf.toString();
  }
  
  public String getFormat() {
    return format;
  }
  
  public String apply(Map<String, Object> params) {
    List<Object> values = new ArrayList<>(params.size());
    for (String key : keys) {
      Object obj = params.get(key);
      if (obj == null) {
        obj = nullPlaceholder;
      }
      values.add(obj);
    }
    return StringEscapeUtils.escapeJava(String.format(format, values.toArray()));
  }
}
