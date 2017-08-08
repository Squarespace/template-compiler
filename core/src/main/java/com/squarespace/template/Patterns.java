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

import static com.squarespace.compiler.match.Recognizers.charClass;
import static com.squarespace.compiler.match.Recognizers.characters;
import static com.squarespace.compiler.match.Recognizers.choice;
import static com.squarespace.compiler.match.Recognizers.digits;
import static com.squarespace.compiler.match.Recognizers.literal;
import static com.squarespace.compiler.match.Recognizers.notCharacters;
import static com.squarespace.compiler.match.Recognizers.oneOrMore;
import static com.squarespace.compiler.match.Recognizers.sequence;
import static com.squarespace.compiler.match.Recognizers.zeroOrMore;
import static com.squarespace.compiler.match.Recognizers.zeroOrOne;
import static com.squarespace.compiler.text.DefaultCharClassifier.DASH;
import static com.squarespace.compiler.text.DefaultCharClassifier.DIGIT;
import static com.squarespace.compiler.text.DefaultCharClassifier.LOWERCASE;
import static com.squarespace.compiler.text.DefaultCharClassifier.UNDERSCORE;
import static com.squarespace.compiler.text.DefaultCharClassifier.UPPERCASE;

import java.util.regex.Pattern;

import com.squarespace.compiler.match.Recognizers.Recognizer;
import com.squarespace.compiler.text.CharClassifier;
import com.squarespace.compiler.text.DefaultCharClassifier;


/**
 * Patterns and recognizers used in Tokenizer and some plugins.
 */
public class Patterns {

  private static final CharClassifier CHAR_CLASSIFIER = new DefaultCharClassifier();

  public static final char EOF_CHAR = '\uFFFF';
  public static final char META_LEFT_CHAR = '{';
  public static final char META_RIGHT_CHAR = '}';
  public static final char NEWLINE_CHAR = '\n';
  public static final char POUND_CHAR = '#';

  public static final Pattern ONESPACE = Pattern.compile("\\s");
  public static final Pattern WHITESPACE_RE = Pattern.compile("\\s+");
  public static final Pattern WHITESPACE_NBSP = Pattern.compile("[\\s\u200b\u00a0]+");

  public static final Recognizer ARGUMENTS = oneOrMore(notCharacters('|', '}'));
  public static final Recognizer BOOLEAN_OP = choice(literal("&&"), literal("||"));

  /**
   * Delimiter for passing multiple variables to a formatter. Dollar sign chosen
   * as it is the least likely to appear in JavaScript in the wild, minimizing
   * the chance this will produce invalid instructions when parsing pages
   * containing inlined JavaScript.
   *
   * Tested this syntax against a large amount of minified and un-minified JavaScript
   * libraries (jQuery, d3, moment) to confirm no increase in false matches.
   */
  public static final Recognizer VARIABLES_DELIMITER = sequence(
      oneOrMore(characters(' ')), characters('$'), oneOrMore(characters(' ')));

  /** Recognizer that matches Java's regular expression "\\s+" */
  public static final Recognizer WHITESPACE = oneOrMore(characters(' ', '\t', '\n', '\u000b', '\f', '\r'));

  /** Keyword in REPEATED instruction, e.g. ".repeated section foo.bar" */
  public static final Recognizer KEYWORD_SECTION = literal("section");

  /** Keyword in ALTERNATES_WITH instruction, e.g. ".alternates with" */
  public static final Recognizer KEYWORD_WITH = literal("with");

  /** Prefix of a word:  [a-zA-Z] */
  private static final Recognizer WORD_PREFIX =
      charClass(LOWERCASE | UPPERCASE, CHAR_CLASSIFIER);

  /** Suffix of a word:  [a-zA-Z0-9_-] */
  private static final Recognizer WORD_SUFFIX =
      charClass(LOWERCASE | UPPERCASE | DIGIT | UNDERSCORE | DASH, CHAR_CLASSIFIER);

  /** Word, in formatter identifiers, variable names, etc */
  private static final Recognizer WORD =
      sequence(WORD_PREFIX, zeroOrMore(WORD_SUFFIX));

  /** Formatter name is just a word, e.g. "json-pretty". */
  public static final Recognizer FORMATTER = WORD;

  /** Variable reference segment. */
  private static final Recognizer VARIABLE_REF_SEGMENT =
      choice(characters('@'), digits(), sequence(zeroOrOne(characters('@')), WORD));

  /** Variable reference, optionally dotted. */
  public static final Recognizer VARIABLE_REF_DOTTED =
      sequence(VARIABLE_REF_SEGMENT, zeroOrMore(sequence(characters('.'), VARIABLE_REF_SEGMENT)));

  /** Local variable definition */
  public static final Recognizer VARIABLE_DEFINITION =
      sequence(characters('@'), oneOrMore(WORD));

  /**
   *  Matches instructions as well as predicates in their dot-prefixed form.
   *  Examples:
   *
   *    .section
   *    .plural?
   *    .main-image?
   */
  public static final Recognizer RESERVED_WORD =
      sequence(characters('.'), WORD, zeroOrOne(characters('?')));

  /**
   * Path used for resolving JSON objects in Inject instruction.
   *
   *  [./a-zA-Z0-9_-]
   */
  public static final Recognizer PATH =
      oneOrMore(
          choice(
              charClass(LOWERCASE | UPPERCASE | DIGIT | UNDERSCORE | DASH, CHAR_CLASSIFIER),
              characters('.', '/')));

  /**
   * Predicates can appear as instructions or part of an OR instruction. We need this
   * separate pattern to match them when following an OR.
   * Examples:
   *
   *   plural?
   *   collectionTypeNameEquals?
   */
  public static final Recognizer PREDICATE = sequence(WORD, characters('?'));
  public static final Recognizer PREDICATE_ARGUMENTS = oneOrMore(notCharacters('}'));

}
