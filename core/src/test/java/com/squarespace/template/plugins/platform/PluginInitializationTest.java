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

package com.squarespace.template.plugins.platform;

import static org.testng.Assert.fail;

import org.testng.annotations.Test;


public class PluginInitializationTest extends PlatformUnitTestBase {

  @Test
  public void testInitialization() {
    try {
      // Forces the compiler to be constructed with all of the platform
      // plugins registered and initialized. Ensuring that any
      // initialization-related errors are detected early.
      compiler();
    } catch (IllegalArgumentException e) {
      fail("Constructing the compiler failed.", e);
    }
  }

}
