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

import java.util.HashMap;
import java.util.Map;


public class EnumUtils {

  private EnumUtils() {
  }
  
  public static <T extends PlatformEnum> Map<Integer, T> codeMap(Class<T> enumClass) {
    Map<Integer, T> map = new HashMap<>();
    for (T value : enumClass.getEnumConstants()) {
      map.put(value.code(), value);
    }
    return map;
  }
  
  public static <T extends PlatformEnum> Map<String, T> stringValueMap(Class<T> enumClass) {
    Map<String, T> map = new HashMap<>();
    for (T value : enumClass.getEnumConstants()) {
      map.put(value.stringValue(), value);
    }
    return map;
  }

}
