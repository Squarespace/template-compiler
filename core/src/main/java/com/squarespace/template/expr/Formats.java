package com.squarespace.template.expr;

import com.squarespace.template.dtoa.DToA;
import com.squarespace.template.v8dtoa.FastDtoa;

/**
 * Miscellaneous formatting and parsing functions.
 */
public class Formats {

  private Formats() { }

  public static String number(double d) {
    if (Double.isNaN(d))
        return "NaN";
    if (d == Double.POSITIVE_INFINITY)
        return "Infinity";
    if (d == Double.NEGATIVE_INFINITY)
        return "-Infinity";
    if (d == 0.0)
        return "0";

    // V8 FastDtoa can't convert all numbers, so try it first but
    // fall back to old DToA in case it fails
    String result = FastDtoa.numberToString(d);
    if (result != null) {
        return result;
    }
    StringBuilder buffer = new StringBuilder();
    DToA.JS_dtostr(buffer, DToA.DTOSTR_STANDARD, 0, d);
    return buffer.toString();
  }

}
