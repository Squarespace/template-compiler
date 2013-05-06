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
  
//  public static final String _WORD = _ABC + "(" + _ABC09UH + "*" + _ABC09 + ")*";
  
  public static final String _WORD = _ABC + _ABC09UH + "*+";
  
  public static final String _DOTWORD = _WORD + "(\\." + _WORD + ")*+";

  // Compiled regular expressions
  
  public static final Pattern ARGUMENTS = Pattern.compile("[^}]+");
  
  public static final Pattern BOOLEAN_OP = Pattern.compile("&&|\\|\\|");
  
  public static final Pattern FORMATTER = Pattern.compile(_WORD);
  
  /**
   *  Matches instructions and predicates in their instruction form.
   *  Examples:
   *    .section
   *    .plural?
   *    .main-image?
   */
  public static final Pattern KEYWORD = Pattern.compile("\\." + _WORD + "\\??");

  public static final Pattern ONESPACE = Pattern.compile("\\s");

  /**
   * Predicates can appear as instructions or part of an OR instruction. We need this
   * separate pattern to match them when following an OR.
   * Examples:
   * 
   *   plural?
   *   collectionTypeNameEquals?
   */
  public static final Pattern PREDICATE = Pattern.compile(_WORD + "\\?");

  // We may want to relax this pattern later to allow other @-prefixed variable names.
  // Sticking strictly to the upstream JSONT behavior for now.
  public static final Pattern VARIABLE = Pattern.compile(_DOTWORD + "|@index|@");
  
  public static final Pattern WHITESPACE = Pattern.compile("\\s+");

  // Required word following REPEATED instruction, e.g. {.repeated section foo.bar}
  public static final Pattern WORD_SECTION = Pattern.compile("section");

  // Required word following ALTERNATES_WITH instruction, e.g. {.alternates with}
  public static final Pattern WORD_WITH = Pattern.compile("with");

}
