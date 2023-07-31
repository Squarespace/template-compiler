package com.squarespace.template;

import static com.squarespace.template.TestCaseParser.escaped;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class TestCaseParserTest {

  @Test
  public void testEcaped() {
    assertEquals(escaped("!#${|}-"), "!#${|}-");
    assertEquals(escaped(" \t\n bar \u2019"), "\\x20\\x09\\x0a\\x20bar\\x20\\xe2\\x80\\x99");
  }
}
