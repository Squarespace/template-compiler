package com.squarespace.template.plugins;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;



public class PluginUtilsTest {

  @Test
  public void testRemoveTags() {
    assertEquals(PluginUtils.removeTags("<\nhello < >world"), "world");
  }
}
