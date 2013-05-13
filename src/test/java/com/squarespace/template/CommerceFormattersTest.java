package com.squarespace.template;

import static com.squarespace.template.CommerceFormatters.MONEY_FORMAT;

import org.testng.annotations.Test;


public class CommerceFormattersTest extends UnitTestBase {

  @Test
  public void testMoneyFormat() throws CodeException {
    assertFormatter(MONEY_FORMAT, "12413.13", "12,413.13");
    assertFormatter(MONEY_FORMAT, "1.00", "1.00");
    assertFormatter(MONEY_FORMAT, "1000", "1,000.00");
  }
  
}
