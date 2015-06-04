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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Properties about the build.
 */
public class BuildProperties {

  private static final String UNDEFINED = "<undefined>";

  private static final String BUILD_PROPERTIES = "build.properties";

  private static final Properties properties = load();

  public static String version() {
    return get("build.version");
  }

  public static String date() {
    return get("build.date");
  }

  public static String commit() {
    return get("build.commit");
  }

  private static String get(String name) {
    String value = properties.getProperty(name, UNDEFINED);
    return (value.startsWith("@")) ? UNDEFINED : value;
  }

  private static Properties load() {
    Properties properties = new Properties();
    try (InputStream in = BuildProperties.class.getResourceAsStream(BUILD_PROPERTIES)) {
      if (in == null) {
        System.err.println("WARNING: build.properties could not be located");
      } else {
        properties.load(in);
      }
    } catch (IOException e) {
      System.err.println("WARNING: build.properties failed to load: " + e.getMessage());
    }
    return properties;
  }

}
