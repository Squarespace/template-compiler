package com.squarespace.template;

import static com.squarespace.template.CharGroups.WHITESPACE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;


public class StringViewTest {

  @Test
  public void testRepr() {
    String s1 = "foobar";
    StringView view = new StringView(s1);
    assertEquals(view.repr(), s1);
    
    String s2 = "..........abc..";
    StringView v1 = new StringView(s2, 10, 13);
    assertEquals(v1.start(), 10);
    assertEquals(v1.end(), 13);
    assertEquals(v1.length(), 3);
    
    // Testing with initial string of length 10
    StringView v2 = new StringView("abcdefghij");

    // "abcdefghij"[1:5] == "bcde"
    StringView v3 = v2.subview(1, 5);
    assertEquals(v3.start(), 1);
    assertEquals(v3.end(), 5);
    assertEquals(v3.repr(), "bcde");

    // "bcde"[1:2] == "c"
    StringView v4 = v3.subview(1, 2); 
    assertEquals(v4.start(), 2);
    assertEquals(v4.end(), 3);
    assertEquals(v4.repr(), "c");
  }

  @Test
  public void testLength() {
    String orig = "abcde";
    StringView view = new StringView(orig, -2, 17);
    assertEquals(5, view.length());
    assertEquals('a', view.charAt(0));
    assertEquals('e', view.charAt(4));
    
    view = new StringView(orig, 1, 4);
    assertEquals(3, view.length());
    assertEquals('b', view.charAt(0));
    assertEquals('d', view.charAt(2));
  }
  
  @Test
  public void testBuilder() {
    String orig = "foobar";
    StringView view = new StringView(orig, 1, 5);
    StringBuilder buf = new StringBuilder();
    buf.append(view);
    assertEquals("ooba", buf.toString());
  }
  
  @Test
  public void testHash() {
    String data = "abcde";
    StringView s1 = new StringView(data, 1, 4);
    StringView s2 = new StringView(data, 1, 4);
    assertEquals(s1, s2);
    assertEquals(s1.hashCode(), s2.hashCode());
    
    StringView s3 = new StringView(data, 2, 5);
    assertNotEquals(s1, s3, "not equal");

    data = "abcdefghijklmnopqrstuvwxyz";
    for (int i = 0; i < data.length(); i++) {
      StringView v = new StringView(data.substring(0, i));
      v.hashCode();
    }
  }
  
  @Test
  public void testEquals() {
    String s1 = "..........abc..";
    String s2 = "xyzabcxyz";
    StringView v1 = new StringView(s1, 10, 13);
    StringView v2 = new StringView(s2, 3, 6);
    assertEquals(v1, v2);
    StringView v3 = new StringView(s1, 9, 12);
    assertNotEquals(v1, v3);
  }
  
  @Test
  public void testSplit() {
    StringView view = new StringView("ab \t \n cde \r fg");
    List<StringView> res = StringViewUtils.split(view, WHITESPACE);
    Assert.assertEquals(res.size(), 3);
    assertEquals(res.get(0), view.subview(0, 2));
    assertEquals(res.get(1), view.subview(7, 10));
    assertEquals(res.get(2), view.subview(13, 15));

    view = new StringView("");
    res = StringViewUtils.split(view, CharGroups.WHITESPACE);
    Assert.assertEquals(res.size(), 1);
  }
  
  @Test
  public void testLeftTrim() {
    StringView view = new StringView("\t \nabc\t \n");
    StringView expected = new StringView("abc\t \n");
    StringView res = StringViewUtils.leftTrim(view, WHITESPACE);
    assertEquals(res, expected);
  }

  @Test
  public void testRightTrim() {
    StringView view = new StringView("\t \nabc\t \n");
    StringView expected = new StringView("\t \nabc");
    StringView res = StringViewUtils.rightTrim(view, WHITESPACE);
    assertEquals(res, expected);
  }

  @Test
  public void testTrim() {
    StringView view = new StringView(" \t \n \r ");
    StringView expected = new StringView("");
    assertEquals(StringViewUtils.trim(view), expected);
  }

}
