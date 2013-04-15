package com.squarespace.template;

import static com.squarespace.template.CharGroups.WHITESPACE;

import java.util.ArrayList;
import java.util.List;


/**
 * Utility functions for processing CharSequences.
 */
public class StringViewUtils {

  public static StringView trim(StringView seq) {
    return trim(seq, WHITESPACE);
  }

  public static StringView trim(StringView seq, CharGroup predicate) {
    return rightTrim(leftTrim(seq, predicate), predicate);
  }
  
  public static StringView leftTrim(StringView seq, CharGroup predicate) {
    int len = seq.length();
    int start = 0;
    while (start < len) {
      if (!predicate.contains(seq.charAt(start))) {
        break;
      }
      start++;
    }
    return seq.subview(start, len);
  }

  public static StringView rightTrim(StringView seq, CharGroup predicate) {
    int len = seq.length();
    int start = 0;
    int end = len;
    while (end > start) {
      if (!predicate.contains(seq.charAt(end-1))) {
        break;
      }
      end--;
    }
    return seq.subview(start, end);
  }
  
  public static List<StringView> split(StringView seq, CharGroup predicate) {
    List<StringView> result = new ArrayList<>();
    int start = 0;
    int end = 0;
    boolean flag = false;
    for (int i = 0; i < seq.length(); i++) {
      boolean isBoundary = predicate.contains(seq.charAt(i));
      if (isBoundary) {
        if (!flag) {
          result.add(seq.subview(start, end + 1));
        }
        flag = true;
      } else {
        if (flag) {
          start = end = i;
          flag = false;
        } else {
          end = i;
        }
      }
    }
    if (!flag) {
      result.add(seq.subview(start, end + 1));
    }
    return result;
  }


}
