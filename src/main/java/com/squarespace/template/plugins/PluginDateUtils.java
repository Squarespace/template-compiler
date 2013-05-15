package com.squarespace.template.plugins;

import static com.squarespace.template.plugins.PluginUtils.leftPad;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;


public class PluginDateUtils {

  static enum DatePartType {
    YEAR("year"),
    MONTH("month"),
    WEEK("week"),
    DAY("day"),
    HOUR("hour"),
    MINUTE("minute"),
    SECOND("second")
    ;
    
    private String name;
    
    private DatePartType(String name) {
      this.name = name;
    }
    
    @Override
    public String toString() {
      return name;
    }
  }
  
  private static void humanizeDatePlural(int value, DatePartType type, StringBuilder buf) {
    buf.append("about ");
    switch (type) {
      case HOUR:
        if (value == 1) {
          buf.append("an hour");
        } else {
          buf.append(value).append(' ').append(type).append('s');
        }
        break;
        
      default:
        if (value == 1) {
          buf.append("a ").append(type);
        } else {
          buf.append(value).append(' ').append(type).append('s');
        }
    }
    buf.append(" ago");
  }

  public static void humanizeDate(long value, boolean showSeconds, StringBuilder buf) {
    int offset = TimeZone.getDefault().getRawOffset();
    Duration delta = new Duration(System.currentTimeMillis() - value + offset);

    int days = (int)delta.getStandardDays();
    int years = (int)Math.floor(days / 365.0);
    if (years > 0) {
      humanizeDatePlural(years, DatePartType.YEAR, buf);
      return;
    }
    int months = (int)Math.floor(days / 30.0);
    if (months > 0) {
      humanizeDatePlural(months, DatePartType.MONTH, buf);
      return;
    }
    int weeks = (int)Math.floor(days / 7.0);
    if (weeks > 0) {
      humanizeDatePlural(weeks, DatePartType.WEEK, buf);
      return;
    }
    if (days > 0) {
      humanizeDatePlural(days, DatePartType.DAY, buf);
      return;
    }
    int hours = (int)delta.getStandardHours();
    if (hours > 0) {
      humanizeDatePlural(hours, DatePartType.HOUR, buf);
      return;
    }
    int mins = (int)delta.getStandardMinutes();
    if (mins > 0) {
      humanizeDatePlural(mins, DatePartType.MINUTE, buf);
      return;
    }
    int secs = (int)delta.getStandardSeconds();
    if (showSeconds) {
      humanizeDatePlural(secs, DatePartType.SECOND, buf);
      return;
    }
    buf.append("less than a minute ago");
  }
  
  static enum DateTimeAggregate {
    FULL,
    H240_M0,
    HHMMSSP,
    MMDDYY,
    MMDDYYYY,
    YYYYMMDD
  }
  
  public static boolean sameDay(long instant1, long instant2, String tzName) {
    DateTimeZone zone = null;
    try {
      zone = DateTimeZone.forID(tzName);
    } catch (IllegalArgumentException e) {
      zone = DateTimeZone.getDefault();
    }
    DateTime date1 = new DateTime(instant1, zone);
    DateTime date2 = new DateTime(instant2, zone);
    return (date1.year().get() == date2.year().get()) &&
        (date1.monthOfYear().get() == date2.monthOfYear().get()) &&
        (date1.dayOfMonth().get() == date2.dayOfMonth().get());
  }
  
  /**
   * Takes a strftime()-compatible format string and outputs the properly formatted date.
   */
  public static void formatDate(String fmt, long instant, String tzName, StringBuilder buf) {
    DateTimeZone zone = null;
    try {
      zone = DateTimeZone.forID(tzName);
    } catch (IllegalArgumentException e) {
      zone = DateTimeZone.getDefault();
    }
    DateTime date = new DateTime(instant, zone);
    int index = 0;
    int len = fmt.length();
    while (index < len) {
      char c1 = fmt.charAt(index);
      index++;
      if (c1 != '%' || index == len) {
        buf.append(c1);
        continue;
      }
      char c2 = fmt.charAt(index);
      switch (c2) {
        case 'A': buf.append(date.dayOfWeek().getAsText()); break;
        case 'a': buf.append(date.dayOfWeek().getAsShortText()); break;
        case 'B': buf.append(date.monthOfYear().getAsText()); break;
        case 'b': buf.append(date.monthOfYear().getAsShortText()); break;
        case 'C': leftPad(date.centuryOfEra().get(), '0', 2, buf); break;
        case 'c': formatAggregate(DateTimeAggregate.FULL, date, buf); break;
        case 'D': formatAggregate(DateTimeAggregate.MMDDYY, date, buf); break;
        case 'd': leftPad(date.dayOfMonth().get(), '0', 2, buf); break;
        case 'e': leftPad(date.dayOfMonth().get(), ' ', 2, buf); break;
        case 'F': formatAggregate(DateTimeAggregate.YYYYMMDD, date, buf); break;
        case 'G': buf.append(date.year().get()); break;
        case 'g': leftPad(date.yearOfCentury().get(), '0', 2, buf); break;
        case 'H': leftPad(date.hourOfDay().get(), '0', 2, buf); break;
        case 'h': buf.append(date.monthOfYear().getAsShortText()); break;
        case 'I': leftPad(date.get(DateTimeFieldType.clockhourOfHalfday()), '0', 2, buf); break;
        case 'j': leftPad(date.dayOfYear().get(), '0', 3, buf); break;
        case 'k': leftPad(date.get(DateTimeFieldType.clockhourOfDay()), ' ', 2, buf); break;
        case 'l': leftPad(date.get(DateTimeFieldType.clockhourOfHalfday()), ' ', 2, buf); break;
        case 'M': leftPad(date.minuteOfHour().get(), '0', 2, buf); break;
        case 'm': leftPad(date.monthOfYear().get(), '0', 2, buf); break;
        case 'n': buf.append('\n'); break;
        case 'P': buf.append(date.get(DateTimeFieldType.halfdayOfDay()) == 0 ? "am" : "pm"); break;
        case 'p': buf.append(date.get(DateTimeFieldType.halfdayOfDay()) == 0 ? "AM" : "PM"); break;
        case 'R': formatAggregate(DateTimeAggregate.H240_M0, date, buf); break;
        case 'S': leftPad(date.secondOfMinute().get(), '0', 2, buf); break;
        case 's': buf.append(instant / 1000); break;
        case 't': buf.append('\t'); break;
        case 'T':
          // Equivalent of %H:%M:%S
          formatAggregate(DateTimeAggregate.H240_M0, date, buf);
          buf.append(':');
          leftPad(date.secondOfMinute().get(), '0', 2, buf);
          break;

        case 'U': 
          // TODO: fix week-of-year number
          leftPad(date.weekOfWeekyear().get(), '0', 2, buf);
          break;

        case 'u': buf.append(date.dayOfWeek().get()); break;

        case 'V': 
          // TODO: fix week-of-year number
          leftPad(date.weekOfWeekyear().get(), '0', 2, buf);
          break;
        
        case 'v':
          // Equivalent of %e-%b-%Y
          leftPad(date.dayOfMonth().get(), ' ', 2, buf);
          buf.append('-');
          buf.append(date.monthOfYear().getAsShortText());
          buf.append('-');
          buf.append(date.getYear());
          break;
          
        case 'W': 
          // TODO: fix week-of-year number
          break;
          
        case 'w': buf.append(date.dayOfWeek().get()); break;          
        case 'X': formatAggregate(DateTimeAggregate.HHMMSSP, date, buf); break;
        case 'x': formatAggregate(DateTimeAggregate.MMDDYYYY, date, buf); break;
        case 'Y': buf.append(date.getYear()); break;
        case 'y': leftPad(date.getYearOfCentury(), '0', 2, buf); break;
        case 'Z': buf.append(zone.getShortName(date.getMillis())); break; //buf.append(date.getZone().getShortName(date.getMillis())); break;
        case 'z':
          int offset = date.getZone().getOffset(instant) / 60000;
          int hours = (int)Math.floor(offset / 60);
          int minutes = (hours * 60) - offset;
          if (offset < 0) {
            buf.append('-');
          }
          leftPad(Math.abs(hours), '0', 2, buf);
          leftPad(Math.abs(minutes), '0', 2, buf);
          break;
          
        default:
          // no match, emit literals.
          buf.append(c1).append(c2);
      }
      index++;
    }
  }
  
  private static void formatAggregate(DateTimeAggregate type, DateTime date, StringBuilder buf) {
    switch (type) {
      case FULL:
        buf.append(date.dayOfWeek().getAsShortText());
        buf.append(' ');
        leftPad(date.dayOfMonth().get(), '0', 2, buf);
        buf.append(' ');
        buf.append(date.monthOfYear().getAsShortText());
        buf.append(' ');
        buf.append(date.year().get());
        buf.append(' ');
        leftPad(date.get(DateTimeFieldType.clockhourOfHalfday()), '0', 2, buf);
        buf.append(':');
        leftPad(date.minuteOfHour().get(), '0', 2, buf);
        buf.append(':');
        leftPad(date.secondOfMinute().get(), '0', 2, buf);
        buf.append(' ');
        buf.append(date.get(DateTimeFieldType.halfdayOfDay()) == 0 ? "AM": "PM");
        buf.append(' ');
        buf.append(date.getZone().getShortName(date.getMillis()));
        break;
      
      case H240_M0:
        leftPad(date.get(DateTimeFieldType.clockhourOfDay()), '0', 2, buf);
        buf.append(':');
        leftPad(date.minuteOfHour().get(), '0', 2, buf);
        break;
        
      case HHMMSSP:
        leftPad(date.get(DateTimeFieldType.hourOfHalfday()), '0', 2, buf);
        buf.append(':');
        leftPad(date.getMinuteOfHour(), '0', 2, buf);
        buf.append(':');
        leftPad(date.getSecondOfMinute(), '0', 2, buf);
        buf.append(' ');
        buf.append(date.get(DateTimeFieldType.halfdayOfDay()) == 0 ? "AM": "PM");
        break;
        
      case MMDDYY:
        leftPad(date.getMonthOfYear(), '0', 2, buf);
        buf.append('/');
        leftPad(date.dayOfMonth().get(), '0', 2, buf);
        buf.append('/');
        leftPad(date.yearOfCentury().get(), '0', 2, buf);
        break;

      case MMDDYYYY:
        leftPad(date.getMonthOfYear(), '0', 2, buf);
        buf.append('/');
        leftPad(date.dayOfMonth().get(), '0', 2, buf);
        buf.append('/');
        buf.append(date.getYear());
        break;

      case YYYYMMDD:
        buf.append(date.year().get());
        buf.append('-');
        leftPad(date.monthOfYear().get(), '0', 2, buf);
        buf.append('-');
        leftPad(date.dayOfMonth().get(), '0', 2, buf);
        break;
    }
  }

}
