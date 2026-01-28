package com.squarespace.template.plugins.platform.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.CalendarDate;
import com.squarespace.cldrengine.api.RelativeTimeFormatOptions;
import com.squarespace.template.compat.Patch;
import com.squarespace.template.Arguments;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.OptionParsers;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;

public class RelativeTimeFormatter extends BaseFormatter {

  public RelativeTimeFormatter() {
    super("relative-time", false);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    int count = variables.count();
    Variable v1 = variables.get(0);
    JsonNode n1 = v1.node();
    // Legacy, a missing or null value reads as epoch 0. Fixed, it
    // renders missing.
    if ((n1.isMissingNode() || n1.isNull())
        && !ctx.compatEnabled(Patch.DATETIME_MISSING_EPOCH)) {
      v1.setMissing();
      return;
    }
    Long now = ctx.now();
    long s = now == null ? System.currentTimeMillis() : now.longValue();
    long e = n1.asLong();
    if (count > 1) {
      JsonNode n2 = variables.get(1).node();
      // Legacy, a missing second operand reads as epoch 0. Fixed, it
      // renders missing.
      if ((n2.isMissingNode() || n2.isNull())
          && !ctx.compatEnabled(Patch.DATETIME_MISSING_EPOCH)) {
        v1.setMissing();
        return;
      }
      s = e;
      e = n2.asLong();
    }

    CLDR cldr = ctx.cldr();
    CalendarDate start = cldr.Calendars.toGregorianDate(s, "UTC");
    CalendarDate end = cldr.Calendars.toGregorianDate(e, "UTC");
    RelativeTimeFormatOptions opts = OptionParsers.relativetime(args);
    String res = cldr.Calendars.formatRelativeTime(start, end, opts);
    v1.set(res);
  }
}
