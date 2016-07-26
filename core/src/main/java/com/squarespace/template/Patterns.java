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

import java.util.regex.Pattern;


public class Patterns {

  public static final char EOF_CHAR = '\uFFFF';

  public static final char META_LEFT_CHAR = '{';

  public static final char META_RIGHT_CHAR = '}';

  public static final char NEWLINE_CHAR = '\n';

  public static final char POUND_CHAR = '#';

  public static final String _ABC = "[a-zA-Z]";

  public static final String _ABC09 = "[a-zA-Z0-9]";

  public static final String _ABC09UH = "[a-zA-Z0-9_-]";

  public static final String _PATH = "[./a-zA-Z0-9_-]";

  public static final String _WORD = _ABC + _ABC09UH + "*+";

  public static final String _WORD_OR_DIGITS = "(" + _WORD + "|\\d+)";

  public static final String _DOTWORD = _WORD_OR_DIGITS + "(\\." + _WORD_OR_DIGITS + ")*+";

  // Compiled regular expressions

  public static final Pattern ARGUMENTS = Pattern.compile("[^|}]+");

  public static final Pattern BOOLEAN_OP = Pattern.compile("&&|\\|\\|");

  public static final Pattern FORMATTER = Pattern.compile(_WORD);

  /**
   *  Matches instructions as well as predicates in their dot-prefixed form.
   *  Examples:
   *
   *    .section
   *    .plural?
   *    .main-image?
   */
  public static final Pattern KEYWORD = Pattern.compile("\\." + _WORD + "\\??");

  public static final Pattern LOCAL_VARIABLE = Pattern.compile("@" + _WORD);

  public static final Pattern ONESPACE = Pattern.compile("\\s");

  public static final Pattern PATH = Pattern.compile(_PATH + "+");

  /**
   * Predicates can appear as instructions or part of an OR instruction. We need this
   * separate pattern to match them when following an OR.
   * Examples:
   *
   *   plural?
   *   collectionTypeNameEquals?
   */
  public static final Pattern PREDICATE = Pattern.compile(_WORD + "\\?");

  public static final Pattern PREDICATE_ARGUMENTS = Pattern.compile("[^}]+");

  public static final Pattern VARIABLE = Pattern.compile("@*" + _DOTWORD + "|@");

  public static final Pattern WHITESPACE = Pattern.compile("\\s+");

  public static final Pattern WHITESPACE_NBSP = Pattern.compile("[\\s\u200b\u00a0]+");

  // Required word following REPEATED instruction, e.g. ".repeated section foo.bar"
  public static final Pattern WORD_SECTION = Pattern.compile("section");

  // Required word following ALTERNATES_WITH instruction, e.g. ".alternates with"
  public static final Pattern WORD_WITH = Pattern.compile("with");

}
