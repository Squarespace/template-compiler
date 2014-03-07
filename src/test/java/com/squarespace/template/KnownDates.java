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

import org.testng.annotations.Test;


/**
 * Testing date formatting routines requires known fixed dates for validation.
 */
@Test( groups={ "unit" })
public class KnownDates {

  public static final long JAN_01_1970_071510_UTC = 26110000L;

  public static final long MAY_13_2013_010000_UTC = 1368406800000L;
  
  public static final long NOV_15_2013_123030_UTC = 1384518630000L;


}

