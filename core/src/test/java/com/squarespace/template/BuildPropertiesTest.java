/**
 *  Copyright, 2015, Squarespace, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.squarespace.template;

import static com.squarespace.template.BuildProperties.commit;
import static com.squarespace.template.BuildProperties.date;
import static com.squarespace.template.BuildProperties.version;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.IsEqual.equalTo;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.testng.annotations.Test;


public class BuildPropertiesTest {

  private static final String UNDEFINED = "<undefined>";

  @Test
  public void testProperties() {
    assertThat(commit(), anyOf(equalTo(UNDEFINED), patternMatcher("[0-9a-fA-F]+")));
    assertThat(date(), anyOf(equalTo(UNDEFINED), patternMatcher("[\\w:\\s]+")));
    assertThat(version(), anyOf(equalTo(UNDEFINED), patternMatcher("\\d+\\.\\d+\\.\\d+([-\\w]+)?(-SNAPSHOT)?")));
  }

  private Matcher<String> patternMatcher(String regex) {
    return new CustomMatcher<String>("matches pattern") {
      @Override
      public boolean matches(Object item) {
        return (item instanceof String) && ((String)item).matches(regex);
      }
    };
  }
}
