/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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

package com.squarespace.template.plugins.platform.enums;

import static com.squarespace.template.GeneralUtils.getOrDefault;
import static com.squarespace.template.plugins.platform.enums.EnumUtils.stringValueMap;

import java.util.Map;

/**
 * This represents the type of scarcity message to be shown. It should mirror the commons enum of the same name.
 */
public enum ScarcityMessageType implements PlatformEnum {

  DEFAULT_SCARCITY_MESSAGE(0, "DEFAULT_SCARCITY_MESSAGE"),
  SCARCITY_MESSAGE_1(1, "SCARCITY_MESSAGE_1"),
  SCARCITY_MESSAGE_2(2, "SCARCITY_MESSAGE_2"),
  SCARCITY_MESSAGE_3(3, "SCARCITY_MESSAGE_3"),
  SCARCITY_MESSAGE_4(4, "SCARCITY_MESSAGE_4"),
  CUSTOM_SCARCITY_MESSAGE(5, "CUSTOM_SCARCITY_MESSAGE");

  private static final Map<String, ScarcityMessageType> STRING_MAP = stringValueMap(ScarcityMessageType.class);

  private final int code;

  private final String stringValue;

  ScarcityMessageType(int code, String stringValue) {
    this.code = code;
    this.stringValue = stringValue;
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public String stringValue() {
    return stringValue;
  }

  public static ScarcityMessageType fromString(String stringValue) {
    return getOrDefault(STRING_MAP, stringValue, DEFAULT_SCARCITY_MESSAGE);
  }

}
