package com.squarespace.template;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.squarespace.cldrengine.CLDR;

public class LocaleManager {

  public static final String DEFAULT = "main";
  private final Map<String, Entry> entries = new HashMap<>(2);

  public LocaleManager() {
    this.entries.put(DEFAULT, new Entry(Locale.US));
  }

  public LocaleManager(Locale locale) {
    this.entries.put(DEFAULT, new Entry(locale));
  }

  public void setLocale(Locale locale) {
    this.entries.put(DEFAULT, new Entry(locale));
  }

  public void addLocale(String name, Locale locale) {
    this.entries.put(name, new Entry(locale));
  }

  public Entry get() {
    return this.entries.get(DEFAULT);
  }

  public Entry get(String name) {
    Entry entry = this.entries.get(name);
    return entry == null ? this.entries.get(DEFAULT) : entry;
  }

  public static class Entry {

    private final Locale locale;
    private final CLDR cldr;
    private MessageFormats formatter;

    public Entry(Locale locale) {
      this.locale = locale;
      this.cldr = CLDR.get(locale);
    }

    public Locale locale() {
      return this.locale;
    }

    public CLDR cldr() {
      return this.cldr;
    }

    public MessageFormats formatter() {
      if (this.formatter == null) {
        this.formatter = new MessageFormats(this.cldr);
      }
      return this.formatter;
    }
  }

}
