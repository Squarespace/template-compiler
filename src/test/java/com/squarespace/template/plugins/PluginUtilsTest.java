package com.squarespace.template.plugins;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.template.plugins.PluginUtils;



@Test( groups={ "unit" })
public class PluginUtilsTest {

  @Test
  public void testRemoveTags() {
    assertEquals(PluginUtils.removeTags("hi,<\nhello < >world"), "hi, world");
  }
}
