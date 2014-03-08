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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Provides a class to capture state about the error, prior to constructing the
 * exception itself. Lets us pass this object around to various places if necessary
 * before wrapping it in a CodeSyntaxException.
 *
 * The methods are named for the small set of keys used to do pattern substitution
 * in the error messages.  The methods allow more compact code, allow call
 * chaining, and to reduce typos when specifying key names, e.g. info.put("ofset", ...)
 */
public class ErrorInfo {

  private static final String CODE = "code";

  private static final String LINE = "line";

  private static final String OFFSET = "offset";

  private static final String TYPE = "type";

  private static final String DATA = "data";

  private static final String NAME = "name";

  private static final String LIMIT = "limit";

  private static final String REPR = "repr";

  private final ErrorType type;

  private final ErrorLevel level;

  private final MapBuilder<String, Object> builder = new MapBuilder<>();

  private List<ErrorInfo> children;

  public ErrorInfo(ErrorType type) {
    this(type, ErrorLevel.ERROR);
  }

  public ErrorInfo(ErrorType type, ErrorLevel level) {
    this.type = type;
    this.level = level;
  }

  public ErrorInfo child(ErrorInfo child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(child);
    return this;
  }

  public ErrorInfo child(List<ErrorInfo> errors) {
    if (children == null) {
      children = new ArrayList<>();
    }
    for (ErrorInfo child : errors) {
      children.add(child);
    }
    return this;
  }

  public ErrorInfo code(Object code) {
    builder.put(CODE, code);
    return this;
  }

  public ErrorInfo line(int line) {
    builder.put(LINE, line);
    return this;
  }

  public ErrorInfo offset(int offset) {
    builder.put(OFFSET, offset);
    return this;
  }

  public ErrorInfo type(Object type) {
    builder.put(TYPE, type);
    return this;
  }

  public ErrorInfo data(Object data) {
    builder.put(DATA, data);
    return this;
  }

  public ErrorInfo name(Object name) {
    builder.put(NAME, name);
    return this;
  }

  public ErrorInfo limit(Object limit) {
    builder.put(LIMIT, limit);
    return this;
  }

  public ErrorInfo repr(String repr) {
    builder.put(REPR, repr);
    return this;
  }

  public MapBuilder<String, Object> getBuilder() {
    return builder;
  }

  public ErrorType getType() {
    return type;
  }

  public ErrorLevel getLevel() {
    return level;
  }

  public List<ErrorInfo> getChildren() {
    if (children == null) {
      return Collections.emptyList();
    }
    return children;
  }

  public String getMessage() {
    return getMessage(false);
  }

  public String getMessage(boolean withChildren) {
    Map<String, Object> params = builder.get();
    StringBuilder buf = new StringBuilder();
    buf.append(type.prefix(params)).append(": ").append(type.message(params));

    if (withChildren && children != null) {
      buf.append(", causes follow: ");
      for (int i = 0, size = children.size(); i < size; i++) {
        if (i >= 1) {
          buf.append(", ");
        }
        buf.append(children.get(i).getMessage());
      }
    }
    return buf.toString();
  }

  public JsonNode toJson() {
    Map<String, Object> map = builder.get();
    ObjectNode obj = JsonUtils.createObjectNode();
    obj.put("level", level.toString());
    Integer line = (Integer)map.get(LINE);
    obj.put("line", (line == null) ? 0 : line);
    Integer offset = (Integer)map.get(OFFSET);
    obj.put("offset", (offset == null) ? 0 : offset);
    obj.put("type", type.toString());
    obj.put("prefix", type.prefix(map));
    obj.put("message", type.message(map));

    // Append any child errors that exist.
    ArrayNode list = JsonUtils.createArrayNode();
    obj.put("children", list);
    if (children != null) {
      for (ErrorInfo child : children) {
        list.add(child.toJson());
      }
    }
    return obj;
  }

}
