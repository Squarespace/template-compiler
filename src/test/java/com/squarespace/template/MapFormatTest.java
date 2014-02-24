package com.squarespace.template;

import static org.testng.Assert.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.squarespace.template.MapBuilder;
import com.squarespace.template.MapFormat;


@Test( groups={ "unit" })
public class MapFormatTest {

  @Test
  public void testBasics() {
    MapFormat format = new MapFormat("%(first)s %(age)d", "???");
    assertEquals(format.getFormat(), "%s %d");
  }
  
  @Test
  public void testStrings() {
    Map<String, Object> map = mapmaker().put("last", "Space").put("first", "Square").get();
    assertEquals(format("%(first)s %(last)s", map), "Square Space");
    assertEquals(format("%(last)-10s", map), "Space     ");
    assertEquals(format("%(last)10s", map), "     Space");
  }
  
  @Test
  public void testTypes() {
    Map<String, Object> map = mapmaker().put("year", 2013).put("month", "Nov").put("day", 12).get();
    assertEquals(format("The date is %(month)s %(day)d, %(year)d", map), "The date is Nov 12, 2013");

    map = mapmaker().put("year", 2038).put("amt", 213.57912).get();
    assertEquals(format("In %(year)d the market lost %(amt).2f points", map), "In 2038 the market lost 213.58 points");
    
    map = mapmaker().put("y1", 11353.13).put("y2", -13553.75).get();
    assertEquals(format("gain/loss y1: %(y1)(,.2f y2: %(y2)(,.2f", map), "gain/loss y1: 11,353.13 y2: (13,553.75)");
  }

  @Test
  public void testMissing() {
    Map<String, Object> map = mapmaker().put("a", "A").put("c", "C").get();
    String raw = "%(a)s %(b)s %(c)s";
    assertEquals(format(raw, map), "A  C");
    assertEquals(format(raw, "{_}", map), "A {_} C");
  }
  
  private String format(String raw, Map<String, Object> map) {
    return new MapFormat(raw).apply(map);
  }
  
  private String format(String raw, String nullPlaceholder, Map<String, Object> map) {
    return new MapFormat(raw, nullPlaceholder).apply(map);
  }

  private MapBuilder<String, Object> mapmaker() {
    return new MapBuilder<String, Object>();
  }
  
}
