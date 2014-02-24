package com.squarespace.template;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.squarespace.template.Patterns;


/**
 * Built-in pattern match tests, ensuring matched character ranges are fully checked.
 */
@Test( groups={ "unit" })
public class PatternsTest {

  @Test
  public void testFormatter() {
    isFormatter("pluralize");
    isFormatter("foo-bar");

    notFormatter(" foo");
    notFormatter(".bar");
    notFormatter("-foo");
    notFormatter("2foo");
    notFormatter("pluralize?");
  }

  @Test
  public void testKeyword() {
    isKeyword(".a");
    isKeyword(".if");
    isKeyword(".ifn");
    isKeyword(".section");
    isKeyword(".plural?");
    isKeyword(".foo-Bar?");
    isKeyword(".foo_");
    
    notKeyword(" .foo");
    notKeyword("foo.bar");
    notKeyword(".foo-bar!");
    notKeyword(".123foo");
  }
  
  @Test
  public void testVariable() {
    isVariable("a.b.c");
    isVariable("foo.bar.baz");
    isVariable("@");
    isVariable("@index");
    isVariable("0");
    isVariable("1");
    isVariable("12");
    isVariable("0.name");
    isVariable("0.1.2.name");
    
    notVariable(" foo");
    notVariable(".foo");
    notVariable("-foo.bar");
    notVariable("12foo");
    notVariable("@foo");
    notVariable("0 .foo");
    notVariable("0. foo");
    notVariable(".0");
    notVariable("0.");
  }

  private void isFormatter(String str) {
    assertTrue(matches(str, Patterns.FORMATTER));
  }

  private void notFormatter(String str) {
    assertFalse(matches(str, Patterns.FORMATTER));
  }

  private void isKeyword(String str) {
    assertTrue(matches(str, Patterns.KEYWORD));
  }
  
  private void notKeyword(String str) {
    assertFalse(matches(str, Patterns.KEYWORD));
  }
  
  private void isVariable(String str) {
    assertTrue(matches(str, Patterns.VARIABLE));
  }
  
  private void notVariable(String str) {
    assertFalse(matches(str, Patterns.VARIABLE));
  }

  private boolean matches(String str, Pattern pattern) {
    Matcher matcher = pattern.matcher(str);
    return matcher.lookingAt() && (matcher.start() == 0) && (matcher.end() == str.length());
  }
}
