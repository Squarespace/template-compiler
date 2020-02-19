/**
 * Copyright (c) 2020 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squarespace.template.plugins.platform.i18n;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.cldrengine.CLDR;
import com.squarespace.cldrengine.api.MessageArgs;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.MessageFormats;

public class MessageFormatsTest {

  @Test
  public void testBasicArgs() {
    String actual;
    CLDR cldr = CLDR.get("en");
    MessageFormats formats = new MessageFormats(cldr);
    formats.setTimeZone("America/New_York");
    String message = "{0 select abc {ABC} def {DEF} true {T} false {F}}";

    actual = formats.formatter().format(message, args().add(new TextNode("abc")));
    assertEquals(actual, "ABC");

    actual = formats.formatter().format(message, args().add(new TextNode("def")));
    assertEquals(actual, "DEF");

    actual = formats.formatter().format(message, args().add(new TextNode("ghi")));
    assertEquals(actual, "");

    actual = formats.formatter().format(message, args().add(new LongNode(123)));
    assertEquals(actual, "");

    actual = formats.formatter().format(message, args().add(BooleanNode.TRUE));
    assertEquals(actual, "T");

    actual = formats.formatter().format(message, args().add(BooleanNode.FALSE));
    assertEquals(actual, "F");

    actual = formats.formatter().format(message, args().add(NullNode.getInstance()));
    assertEquals(actual, "");

    actual = formats.formatter().format(message, args().add(MissingNode.getInstance()));
    assertEquals(actual, "");

    actual = formats.formatter().format(message, args().add(JsonUtils.createArrayNode()));
    assertEquals(actual, "");

    actual = formats.formatter().format(message, args().add(JsonUtils.createObjectNode()));
    assertEquals(actual, "");

    message = "{0 plural one {ONE} other {OTHER}}";

    actual = formats.formatter().format(message, args().add("123"));
    assertEquals(actual, "OTHER");

    actual = formats.formatter().format(message, args().add("1"));
    assertEquals(actual, "ONE");

    actual = formats.formatter().format(message, args().add("undefined"));
    assertEquals(actual, "OTHER");

    actual = formats.formatter().format(message, args().add(new LongNode(123)));
    assertEquals(actual, "OTHER");

    actual = formats.formatter().format(message, args().add(new LongNode(1)));
    assertEquals(actual, "ONE");

    actual = formats.formatter().format(message, args().add(BooleanNode.TRUE));
    assertEquals(actual, "ONE");

    actual = formats.formatter().format(message, args().add(BooleanNode.FALSE));
    assertEquals(actual, "OTHER");

    actual = formats.formatter().format(message, args().add(MissingNode.getInstance()));
    assertEquals(actual, "OTHER");

    actual = formats.formatter().format(message, args().add(NullNode.getInstance()));
    assertEquals(actual, "OTHER");

    actual = formats.formatter().format(message, args().add(new TextNode("abc")));
    assertEquals(actual, "OTHER");

    actual = formats.formatter().format(message, args().add(JsonUtils.createObjectNode()));
    assertEquals(actual, "OTHER");

    ObjectNode money = JsonUtils.createObjectNode();
    money.put("decimalValue", new DecimalNode(new BigDecimal("1.2320001")));
    actual = formats.formatter().format(message, args().add(money));
    assertEquals(actual, "OTHER");

    money.put("decimalValue", new DecimalNode(new BigDecimal("1")));
    actual = formats.formatter().format(message, args().add(money));
    assertEquals(actual, "ONE");
  }

  @Test
  public void testPlural() {
    String actual;
    CLDR cldr = CLDR.get("en");
    MessageFormats formats = new MessageFormats(cldr);
    formats.setTimeZone("America/New_York");
    String message = "you have {0} item{0 plural one{} other{s}} in your cart";

    actual = formats.formatter().format(message, args().add(JsonUtils.decode("1")));
    assertEquals(actual, "you have 1 item in your cart");

    actual = formats.formatter().format(message, args().add(JsonUtils.decode("1.0")));
    assertEquals(actual, "you have 1.0 items in your cart");

    actual = formats.formatter().format(message, args().add(JsonUtils.decode("3")));
    assertEquals(actual, "you have 3 items in your cart");
  }

  @Test
  public void testCurrency() {
    String actual;
    CLDR cldr = CLDR.get("en");
    MessageFormats formats = new MessageFormats(cldr);
    formats.setTimeZone("America/New_York");
    String message = "{0 currency style:standard}";

    ObjectNode money = money("12345.789", "USD");
    actual = formats.formatter().format(message, args().add(money));
    assertEquals(actual, "$12,345.79");
  }

  @Test
  public void testDecimal() {
    String actual;
    CLDR cldr = CLDR.get("en");
    MessageFormats formats = new MessageFormats(cldr);
    formats.setTimeZone("America/New_York");
    String message = "{0 decimal style:short}";

    actual = formats.formatter().format(message, args().add(new TextNode("12345.789")));
    assertEquals(actual, "12K");
  }

  @Test
  public void testDateTime() {
    String actual;
    CLDR cldr = CLDR.get("en");
    MessageFormats formats = new MessageFormats(cldr);
    formats.setTimeZone("America/New_York");
    String message = "{0 datetime date:long time:medium}";
    long epoch = 1582129775000L;

    actual = formats.formatter().format(message, args().add(new LongNode(epoch)));
    assertEquals(actual, "February 19, 2020 at 11:29:35 AM");
  }

  @Test
  public void testInterval() {
    String actual;
    CLDR cldr = CLDR.get("en");
    MessageFormats formats = new MessageFormats(cldr);
    formats.setTimeZone("America/New_York");
    String message = "{0;1 datetime-interval}";
    long epoch = 1582129775000L;

    long extra = 86400000 + 3600000;
    MessageArgs args = args().add(new LongNode(epoch)).add(new LongNode(epoch + extra));
    actual = formats.formatter().format(message, args);
    assertEquals(actual, "Feb 19 – 20, 2020");
  }

  private ObjectNode money(String value, String currency) {
    ObjectNode o = JsonUtils.createObjectNode();
    o.put("decimalValue", value);
    o.put("currencyCode", currency);
    return o;
  }

  private MessageArgs args() {
    return MessageArgs.build();
  }
}
