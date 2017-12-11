/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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

package com.squarespace.template.plugins;

public class FormatUtils {

  public static class FormatArg {

    public final Object[] name;
    public String value;

    public FormatArg(Object[] name) {
      this.name = name;
    }
  }


  /**
   * Performs positional substitution of arguments in a pattern string in a single pass.
   */
  public static void format(String pattern, FormatArg[] args, StringBuilder buf) {
    // position in pattern
    int i = 0;

    // indicates which slot to emit when we finish parsing a tag
    //  0  - inside tag
    // -1  - outside tag
    // -2  - inside tag but ignoring
    int index = -1;

    // max arguments we can substitute
    int limit = args.length;

    int length = pattern.length();
    while (i < length) {
      char ch = pattern.charAt(i);
      if (index == -2 && ch == '}') {
        index = -1;

      } else if (index != -1) {
        switch (ch) {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            // support > 9 arguments
            if (index > 0) {
              index *= 10;
            }
            index += (int)(ch - '0');
            break;

          case '}':
            if (index < limit) {
              buf.append(args[index].value);
            }
            index = -1;
            break;

          default:
            index = -2;
            break;
        }

      } else if (ch == '{') {
        index = 0;

      } else if (index != -2) {
        buf.append(ch);
      }

      i++;
    }
  }

}

