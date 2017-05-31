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

package com.squarespace.template.plugins.platform.i18n;

import static com.squarespace.compiler.match.Recognizers.literal;

import com.squarespace.cldr.plurals.PluralCategory;
import com.squarespace.compiler.match.Recognizers;
import com.squarespace.compiler.match.Recognizers.Recognizer;
import com.squarespace.compiler.text.Chars;
import com.squarespace.compiler.text.Scanner;
import com.squarespace.compiler.text.Scanner.Stream;


/**
 * Simple state machine that formats pluralized messages. There are 3 states,
 * traversing the string, tag and choice. Should be less than O(3N).
 *
 *          "There {0 one {is # entry} other {are # entries}} posted to the {1} blog."
 * <state>
 *  OUTER    ^______________________________________________________________________^
 *    TAG          ^________________________________________^               ^_^
 * CHOICE             ^______________^ ^___________________^                ^_^
 */
public class PluralFormat {

  private static final Recognizer CARDINAL = literal("cardinal");

  private static final Recognizer ORDINAL = literal("ordinal");

  private static final Recognizer[] MATCHERS = new Recognizer[] {
      literal("zero"),
      literal("one"),
      literal("two"),
      literal("few"),
      literal("many"),
      literal("other")
  };

  private static final Recognizer DIGITS = Recognizers.digits();

  /**
   * Formats a pluralization message.
   */
  public void format(String language, String raw, PluralArg[] args, StringBuilder buf) {
    // Scanner letting us track multiple streams over the same underlying string.
    Scanner s = new Scanner(raw);

    // Streams representing the states of the formatter:
    //   outer - text blocks and tags
    //     tag - index and choices
    //  choice - single choice
    //
    Scanner.Stream outer = s.stream();
    Scanner.Stream tag = s.stream();
    Scanner.Stream choice = s.stream();

    // Iterate over text and tags, alternately.
    int prev = 0;
    int length = raw.length();
    while (outer.seekBounds(tag, '{', '}')) {
      // Copy any leading characters to the output.
      emit(buf, raw, prev, tag.pos);
      prev = tag.end;

      // Process this tag, evaluating the choice, if any.
      process(language, buf, args, tag, choice);
    }

    // Copy the remaining trailing characters, if any.
    if (prev < length) {
      emit(buf, raw, prev, length);
    }
  }

  /**
   * Parse and evaluates a tag.
   */
  private void process(String language, StringBuilder buf, PluralArg[] args, Stream tag, Stream choice) {
    // Don't process the delimiters '{' and '}', just the contents.
    tag.pos++;
    tag.end--;

    // A tag must start with an index number.
    if (!tag.seek(DIGITS, choice)) {
      return;
    }

    // Parse the index number and ensure it points to a valid argument.
    int index = toInteger(tag.raw(), choice.pos, choice.end);
    if (index >= args.length) {
      return;
    }

    // Jump over the index value.
    tag.jump(choice);
    PluralArg arg = args[index];

    // Process a short tag like "{1}" by just appending the argument value.
    if (tag.peek() == Chars.EOF) {
      buf.append(arg.value);
      return;
    }

    // Check if plural type is specified: cardinal or ordinal. If not we default to 'cardinal'.
    tag.skipWs();
    boolean cardinal = true;
    if (tag.seek(CARDINAL, choice)) {
      tag.jump(choice);

    } else if (tag.seek(ORDINAL, choice)) {
      cardinal = false;
      tag.jump(choice);
    }

    // Evaluate a plural choice.
    PluralCategory category = arg.evaluate(language, cardinal);
    Recognizer matcher = MATCHERS[category.ordinal()];
    tag.skipWs();

    // Scan over the potential choices inside this tag. We will evaluate
    // the first choice that matches our plural category.
    while (tag.peek() != Chars.EOF) {
      if (tag.seek(matcher, choice)) {
        // Jump over the plural category.
        tag.jump(choice);

        // If the choice format is valid, evaluate it, then return.
        if (tag.peek() == '{' && tag.seekBounds(choice, '{', '}')) {
          eval(buf, choice, arg.value);
        }
        return;
      }

      // Skip over this choice and any trailing whitespace.
      if (!tag.seekBounds(choice, '{', '}')) {
        return;
      }
      tag.skipWs();
    }
  }

  /**
   * Evaluate the template for a given choice.
   */
  private static void eval(StringBuilder buf, Stream choice, String value) {
    // Don't process the delimiters '{' and '}', just the contents.
    choice.pos++;
    choice.end--;

    // Emit the characters, substituting instances of '#' with the argument value.
    char ch;
    while ((ch = choice.seek()) != Chars.EOF) {
      if (ch == '#') {
        buf.append(value);
      } else {
        buf.append(ch);
      }
    }
  }

  /**
   * Append characters from 'raw' in the range [pos, end) to the output buffer.
   */
  private static void emit(StringBuilder buf, String raw, int pos, int end) {
    while (pos < end) {
      buf.append(raw.charAt(pos));
      pos++;
    }
  }

  /**
   * Quick string to integer conversion.
   */
  public static int toInteger(CharSequence seq, int pos, int length) {
    int n = 0;
    int i = pos;
    while (i < length) {
      char c = seq.charAt(i);
      if (c >= '0' && c <= '9') {
        n *= 10;
        n += (int)(c - '0');
      } else {
        break;
      }
      i++;
    }
    return n;
  }

}
