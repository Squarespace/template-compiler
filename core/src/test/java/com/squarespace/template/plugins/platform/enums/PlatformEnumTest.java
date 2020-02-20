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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;


public class PlatformEnumTest {

  @Test
  public void testCodeMap() {
    assertEquals(SliceType.fromCode(10), SliceType.CUSTOM_FORM);
  }

  @Test
  public void testNameMap() {
    assertEquals(SliceType.fromName("custom-form"), SliceType.CUSTOM_FORM);
  }

  @Test
  public void testUndefined() {
    assertEquals(SliceType.fromCode(-100), SliceType.UNDEFINED);
    assertEquals(SliceType.fromName("this-will-never-exist"), SliceType.UNDEFINED);
  }

}
