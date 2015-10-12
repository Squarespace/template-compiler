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
import static com.squarespace.template.plugins.platform.enums.EnumUtils.codeMap;
import static com.squarespace.template.plugins.platform.enums.EnumUtils.stringValueMap;

import java.util.Map;


/**
 * This mirrors the Commons enum of the same name.
 */
public enum SliceType implements PlatformEnum {

  UNDEFINED(-1, "undefined"),
  HEADING(1, "heading"),
  BODY(3, "body"),
  IMAGE(4, "image"),
  GALLERY(5, "gallery"),
  VIDEO(6, "video"),
  SOCIAL_ICONS(7, "social-icons"),
  BUTTONS(8, "buttons"),
  NAVIGATION(9, "navigation"),
  CUSTOM_FORM(10, "custom-form"),
  NEWSLETTER(11, "newsletter"),
  ALBUM(12, "album"),
  MAP(13, "map"),
  LOGO(14, "logo"),
  ACTION(15, "action"),
  FORM(16, "form"),
  LOCK(17, "lock"),
  PASSWORD(18, "password"),
  TWITTER(19, "twitter");

  private static final Map<Integer, SliceType> CODE_MAP = codeMap(SliceType.class);

  private static final Map<String, SliceType> NAME_MAP = stringValueMap(SliceType.class);

  private final int code;

  private final String stringValue;

  private SliceType(int code, String stringValue) {
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

  public static SliceType fromName(String name) {
    return getOrDefault(NAME_MAP, name, UNDEFINED);
  }

  public static SliceType fromCode(int code) {
    return getOrDefault(CODE_MAP, code, UNDEFINED);
  }

}
