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

import java.util.Map;

import static com.squarespace.template.GeneralUtils.getOrDefault;
import static com.squarespace.template.plugins.platform.enums.EnumUtils.stringValueMap;

public enum ScarcityCalculationType implements PlatformEnum {

  TOTAL_STOCK(0, "total-stock"),
  PER_VARIANT(1, "per-variant");

  private static final Map<String, ScarcityCalculationType> STRING_MAP = stringValueMap(ScarcityCalculationType.class);

  private final int code;

  private final String stringValue;

  ScarcityCalculationType(int code, String stringValue) {
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

  public static ScarcityCalculationType fromString(String stringValue) {
    return getOrDefault(STRING_MAP, stringValue, TOTAL_STOCK);
  }

}
