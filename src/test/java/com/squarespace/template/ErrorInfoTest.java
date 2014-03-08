/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
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

package com.squarespace.template;

import static org.testng.Assert.assertEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.ErrorInfo;
import com.squarespace.template.ErrorType;
import com.squarespace.template.MapBuilder;
import com.squarespace.template.MapFormat;


@Test( groups={ "unit" })
public class ErrorInfoTest {

  static class DummyType implements ErrorType {

    private MapFormat prefix = new MapFormat("%(line)s");

    private MapFormat format;

    public DummyType(String format) {
      this.format = new MapFormat(format, "?");
    }

    @Override
    public String prefix(Map<String, Object> params) {
      return prefix.apply(params);
    }

    @Override
    public String message(Map<String, Object> params) {
      return format.apply(params);
    }
  }

  private static final DummyType DUMMY1 = new DummyType("%(name)s %(data)s %(repr)s");

  private static final DummyType DUMMY2 = new DummyType("x %(name)s y");

  @Test
  public void testRepr() {
    ErrorInfo info = new ErrorInfo(DUMMY1);
    info.name("foo").line(7);
    MapBuilder<String, Object> builder = info.getBuilder();
    assertEquals(builder.get().get("name"), "foo");
    assertEquals(info.getMessage(), "7: foo ? ?");

    info.repr("bar");
    assertEquals(info.getMessage(), "7: foo ? bar");

  }

  @Test
  public void testBasicUsage() {
    ErrorInfo info = new ErrorInfo(DUMMY2).line(3).name("foo");
    assertEquals(info.getMessage(), "3: x foo y");
  }

  @Test
  public void testJsonUsage() {
    ErrorInfo info = new ErrorInfo(DUMMY2).line(123).name("foo bar");
    JsonNode node = info.toJson();
    assertEquals(node.get("level").asText(), "ERROR");
    assertEquals(node.get("line").asInt(), 123);
    assertEquals(node.get("offset").asInt(), 0);
  }

}
