package com.squarespace.template;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.Bundle;
import com.squarespace.cldrengine.api.CalendarDate;
import com.squarespace.cldrengine.api.CurrencyFormatOptions;
import com.squarespace.cldrengine.api.CurrencyType;
import com.squarespace.cldrengine.api.DateFormatOptions;
import com.squarespace.cldrengine.api.DateIntervalFormatOptions;
import com.squarespace.cldrengine.api.Decimal;
import com.squarespace.cldrengine.api.DecimalFormatOptions;
import com.squarespace.cldrengine.api.MessageArgConverter;
import com.squarespace.cldrengine.api.MessageFormatFuncMap;
import com.squarespace.cldrengine.api.MessageFormatter;
import com.squarespace.cldrengine.api.MessageFormatterOptions;
import com.squarespace.cldrengine.decimal.DecimalConstants;
import com.squarespace.cldrengine.messageformat.evaluation.DefaultMessageArgConverter;

/**
 * Hooks custom formatting functions into the @phensley/cldr message formatter.
 */
public class MessageFormats {

  private static final String DEFAULT_ZONE = "America/New_York";

  private final CLDR cldr;
  private final MessageArgConverter converter;
  private final MessageFormatter formatter;
  private String zoneId = DEFAULT_ZONE;

  public MessageFormats(CLDR cldr) {
    this.cldr = cldr;
    Bundle bundle = cldr.General.bundle();
    this.converter = new ArgConverter();
    MessageFormatterOptions options = MessageFormatterOptions.build()
        .cacheSize(100)
        .converter(converter)
        .formatters(formatters())
        .language(bundle.language())
        .region(bundle.region());
    this.formatter = new MessageFormatter(options);
  }

  public void setTimeZone(String zoneId) {
    this.zoneId = zoneId;
  }

  public MessageFormatter formatter() {
    return this.formatter;
  }

  private MessageFormatFuncMap formatters() {
    MessageFormatFuncMap map = new MessageFormatFuncMap();
    map.put("money", this::currency);
    map.put("currency", this::currency);
    map.put("datetime", this::datetime);
    map.put("datetime-interval", this::interval);
    map.put("number", this::decimal);
    map.put("decimal", this::decimal);
    return map;
  }

  /**
   * Currency message formatter.
   */
  private String currency(List<Object> args, List<String> options) {
    if (args.isEmpty()) {
      return "";
    }
    JsonNode node = (JsonNode) args.get(0);
    Decimal value = this.converter.asDecimal(node.path("decimalValue"));
    String code = this.converter.asString(node.path("currencyCode"));
    CurrencyType currency = CurrencyType.fromString(code);
    CurrencyFormatOptions opts = OptionParsers.currency(options);
    return cldr.Numbers.formatCurrency(value, currency, opts);
  }

  /**
   * Datetime message formatter.
   */
  private String datetime(List<Object> args, List<String> options) {
    if (args.isEmpty()) {
      return "";
    }
    JsonNode node = (JsonNode) args.get(0);
    long epoch = node.asLong();
    CalendarDate date = cldr.Calendars.toGregorianDate(epoch, zoneId);
    DateFormatOptions opts = OptionParsers.datetime(options);
    return cldr.Calendars.formatDate(date, opts);
  }

  /**
   * Number / decimal message formatter.
   */
  private String decimal(List<Object> args, List<String> options) {
    if (args.isEmpty()) {
      return "";
    }
    JsonNode node = (JsonNode) args.get(0);
    Decimal value = this.converter.asDecimal(node);
    DecimalFormatOptions opts = OptionParsers.decimal(options);
    return cldr.Numbers.formatDecimal(value, opts);
  }

  /**
   * Datetime interval message formatter.
   */
  private String interval(List<Object> args, List<String> options) {
    if (args.size() < 2) {
      return "";
    }

    JsonNode v1 = (JsonNode) args.get(0);
    JsonNode v2 = (JsonNode) args.get(1);
    CalendarDate start = cldr.Calendars.toGregorianDate(v1.asLong(0), zoneId);
    CalendarDate end = cldr.Calendars.toGregorianDate(v2.asLong(0), zoneId);
    DateIntervalFormatOptions opts = OptionParsers.interval(options);
    return cldr.Calendars.formatDateInterval(start, end, opts);
  }

  private static class ArgConverter extends DefaultMessageArgConverter {

    @Override
    public Decimal asDecimal(Object arg) {
      if (arg instanceof JsonNode) {
        JsonNode node = (JsonNode) arg;
        JsonNode decimal = node.path("decimalValue");
        if (!decimal.isMissingNode()) {
          return new Decimal(decimal.asText());
        }
        switch (node.getNodeType()) {
          case BOOLEAN:
            return node.asBoolean() ? DecimalConstants.ONE : DecimalConstants.ZERO;
          case NULL:
          case MISSING:
          case ARRAY:
          case OBJECT:
            return DecimalConstants.ZERO;
          case NUMBER:
          case STRING:
          default:
            try {
              return new Decimal(node.asText());
            } catch (Exception e) {
              return DecimalConstants.ZERO;
            }
        }
      }
      try {
        return super.asDecimal(arg);
      } catch (Exception e) {
        return DecimalConstants.ZERO;
      }
    }

    @Override
    public String asString(Object arg) {
      if (arg instanceof JsonNode) {
        JsonNode node = (JsonNode) arg;
        switch (node.getNodeType()) {
          case BOOLEAN:
            return node.asBoolean() ? "true" : "false";
          case NULL:
          case MISSING:
            return "";
          case ARRAY:
          case OBJECT:
          case NUMBER:
          case STRING:
          default:
            return node.asText();
        }
      }
      try {
        return super.asString(arg);
      } catch (Exception e) {
        return "";
      }
    }
  }
}
