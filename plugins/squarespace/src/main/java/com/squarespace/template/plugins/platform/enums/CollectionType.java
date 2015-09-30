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

import static com.squarespace.template.plugins.platform.enums.EnumUtils.codeMap;

import java.util.Map;


/**
 * This mirrors the Commons enum of the same name.
 */
public enum CollectionType implements PlatformEnum {

  UNDEFINED(-1, "undefined"),
  COLLECTION_TYPE_GENERIC(1, "generic"),
  COLLECTION_TYPE_SUBSCRIPTION(2, "subscription"),
  TWITTER(3, "twitter"),
  FOURSQUARE(4, "foursquare"),
  INSTAGRAM(5, "instagram"),
  GALLERY_BLOCK(6, "gallery-block"),
  TEMPLATE_PAGE(7, "template-page"),
  SPLASH_PAGE(8, "splash-page"),
  COLLECTION_TYPE_PAGE(10, "page"),
  FIVEHUNDREDPIX(11, "fivehundredpix"),
  FLICKR(12, "flickr"),
  PRODUCTS(13, "products"),
  SLIDE_GALLERY(15, "slide-gallery"),
  SLIDE_ALBUM(16, "slide-album"),
  SLIDE_VIDEO(17, "slide-video"),
  ALBUM_BLOCK(18, "album-block");

  private final int code;

  private final String stringValue;

  private static final Map<Integer, CollectionType> CODE_MAP = codeMap(CollectionType.class);

  private CollectionType(int code, String strValue) {
    this.code = code;
    this.stringValue = strValue;
  }

  @Override
  public int code() {
    return code;
  }

  @Override
  public String stringValue() {
    return stringValue;
  }

  public static CollectionType fromCode(int code) {
    return CODE_MAP.getOrDefault(code, UNDEFINED);
  }

}
