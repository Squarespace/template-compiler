package com.squarespace.template.plugins.platform.i18n;

import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.CalendarDate;
import com.squarespace.cldrengine.api.RelativeTimeFormatOptions;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.BaseFormatter;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.OptionParsers;
import com.squarespace.template.Options;
import com.squarespace.template.Variable;
import com.squarespace.template.Variables;

public class RelativeTimeFormatter extends BaseFormatter {

  public RelativeTimeFormatter() {
    super("relative-time", false);
  }

  @Override
  public void validateArgs(Arguments args) throws ArgumentsException {
    Options<RelativeTimeFormatOptions> options = OptionParsers.relativetime(args);
    args.setOpaque(options);
  }

  @Override
  public void apply(Context ctx, Arguments args, Variables variables) throws CodeExecuteException {
    int count = variables.count();
    Variable v1 = variables.get(0);
    Long now = ctx.now();
    long s = now == null ? System.currentTimeMillis() : now.longValue();
    long e = v1.node().asLong();
    if (count > 1) {
      s = e;
      e = variables.get(1).node().asLong();
    }

    @SuppressWarnings("unchecked")
    Options<RelativeTimeFormatOptions> opts = (Options<RelativeTimeFormatOptions>)args.getOpaque();
    CLDR cldr = ctx.localeManager().get(opts.localeName()).cldr();
    CalendarDate start = cldr.Calendars.toGregorianDate(s, "UTC");
    CalendarDate end = cldr.Calendars.toGregorianDate(e, "UTC");
    String res = cldr.Calendars.formatRelativeTime(start, end, opts.inner());
    v1.set(res);
  }
}
