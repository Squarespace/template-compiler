/**
 * Copyright (c) 2016 SQUARESPACE, Inc.
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


public enum BackgroundSource implements PlatformEnum {

  UNDEFINED(-1, "undefined"),
  UPLOAD(1, "upload"),
  INSTAGRAM(2, "instagram"),
  VIDEO(3, "video"),
  NONE(4, "none");

  private final int code;

  private final String name;

  BackgroundSource(int code, String name) {
    this.code = code;
    this.name = name;
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public String stringValue() {
    return name;
  }

}
