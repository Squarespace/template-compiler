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

import java.util.Map;


/**
 * This mirrors the Commons enum of the same name.
 */
public enum RecordType implements PlatformEnum {

  UNDEFINED(-1, "undefined"),
  TEXT(1, "text"),
  IMAGE(2, "image"),
  QUOTE(4, "quote"),
  LINK(5, "link"),
  AUDIO(7, "audio"),
  VIDEO(8, "video"),
  STORE_ITEM(11, "store-item"),
  EVENT(12, "event"),
  GALLERY(14, "gallery"),
  BINARY(15, "binary"),
  CSSASSET(16, "css-asset"),
  TWEAKASSET(17, "tweak-asset"),
  DIGITALGOOD(18, "digital-good"),
  ATTACHMENT(19, "attachment"),
  EXPORT_WORDPRESS(20, "wordpress-export"),
  EXPORT_INTERNAL(21, "json-export"),
  SITE_SEARCH(30, "site-search"),
  ACTIVE_TIME(31, "active-time"),
  TWEET(50, "tweet"),
  CHECKIN(52, "checkin"),
  KBARTICLE(54, "kbarticle");

  private static final Map<Integer, RecordType> CODE_MAP = codeMap(RecordType.class);

  private final int code;

  private final String stringValue;

  RecordType(int code, String stringValue) {
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

  public static RecordType fromCode(int code) {
    return getOrDefault(CODE_MAP, code, UNDEFINED);
  }

}
