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

import static com.squarespace.template.Patterns.EOF_CHAR;
import static com.squarespace.template.Patterns.META_LEFT_CHAR;
import static com.squarespace.template.Patterns.META_RIGHT_CHAR;
import static com.squarespace.template.Patterns.NEWLINE_CHAR;
import static com.squarespace.template.Patterns.POUND_CHAR;
import static com.squarespace.template.SyntaxErrorType.BINDVAR_EXPECTS_NAME;
import static com.squarespace.template.SyntaxErrorType.CTXVAR_EXPECTS_BINDINGS;
import static com.squarespace.template.SyntaxErrorType.CTXVAR_EXPECTS_NAME;
import static com.squarespace.template.SyntaxErrorType.EXTRA_CHARS;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_ARGS_INVALID;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_INVALID;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_NEEDS_ARGS;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_UNKNOWN;
import static com.squarespace.template.SyntaxErrorType.IF_EMPTY;
import static com.squarespace.template.SyntaxErrorType.IF_EXPECTED_VAROP;
import static com.squarespace.template.SyntaxErrorType.IF_TOO_MANY_OPERATORS;
import static com.squarespace.template.SyntaxErrorType.IF_TOO_MANY_VARS;
import static com.squarespace.template.SyntaxErrorType.INJECT_EXPECTS_NAME;
import static com.squarespace.template.SyntaxErrorType.INJECT_EXPECTS_PATH;
import static com.squarespace.template.SyntaxErrorType.INVALID_INSTRUCTION;
import static com.squarespace.template.SyntaxErrorType.MACRO_EXPECTS_NAME;
import static com.squarespace.template.SyntaxErrorType.MISSING_SECTION_KEYWORD;
import static com.squarespace.template.SyntaxErrorType.MISSING_VARIABLE_NAME;
import static com.squarespace.template.SyntaxErrorType.MISSING_WITH_KEYWORD;
import static com.squarespace.template.SyntaxErrorType.OR_EXPECTED_PREDICATE;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_ARGS_INVALID;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_NEEDS_ARGS;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_UNKNOWN;
import static com.squarespace.template.SyntaxErrorType.VARIABLE_EXPECTED;
import static com.squarespace.template.SyntaxErrorType.WHITESPACE_EXPECTED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.squarespace.template.Instructions.BindVarInst;
import com.squarespace.template.Instructions.CtxVarInst;
import com.squarespace.template.Instructions.InjectInst;
import com.squarespace.template.Instructions.MacroInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.VariableInst;


/**
 * Tokenizes characters into instructions, expressing all rules for well-formed
 * combinations of instructions.
 *
 * The Tokenizer is also responsible for the internal composition of an instruction,
 * where the CodeMachine is responsible for the overall composition of instructions
 * into an executable form.
 *
 * If a potential instruction sequence cannot be parsed into an instruction, the
 * degree to which it matches an instruction pattern is an important factor in
 * whether to throw an error or emit the invalid instruction sequence as plain text.
 */
public class Tokenizer {

  // Increased at the request of template developers.
  private final static int IF_VARIABLE_LIMIT = 30;

  private final String raw;
  private final int length;
  private final CodeSink sink;
  private final PredicateTable predicateTable;
  private final FormatterTable formatterTable;
  private final TokenMatcher matcher;
  private final CodeMaker maker;

  private State state;
  private List<ErrorInfo> errors;

  boolean validate = false;
  boolean preprocess = false;

  private int textLine;
  private int textOffset;
  private int instLine;
  private int instOffset;
  private int index = 0;
  private int save = 0;
  private int metaLeft = -1;
  private int lineCounter = 0;
  private int lineIndex = 0;

  public Tokenizer(
      String raw,
      CodeSink sink,
      FormatterTable formatterTable,
      PredicateTable predicateTable) {

    this(raw, sink, false, formatterTable, predicateTable);
  }

  public Tokenizer(
      String raw,
      CodeSink sink,
      boolean preprocess,
      FormatterTable formatterTable,
      PredicateTable predicateTable) {

    this.raw = raw;
    this.length = raw.length();
    this.sink = sink;
    this.preprocess = preprocess;
    this.formatterTable = formatterTable;
    this.predicateTable = predicateTable;
    this.matcher = new TokenMatcher(raw);
    this.maker = new CodeMaker();
    this.state = stateInitial;
  }

  public boolean consume() throws CodeSyntaxException {
    do {
      state = state.transition();
    } while (state != stateEOF);
    sink.complete();
    return (validate) ? errors.size() == 0 : true;
  }

  /**
   * Puts the Tokenizer in a mode where it collects a list of errors, rather than
   * throwing an exception on the first parse error.
   */
  public void setValidate() {
    this.validate = true;
    if (this.errors == null) {
      this.errors = new ArrayList<>(4);
    }
  }

  public void setPreprocess() {
    this.preprocess = true;
  }

  public List<ErrorInfo> getErrors() {
    if (errors == null) {
      errors = new ArrayList<>(0);
    }
    return errors;
  }

  private char getc(int index) {
    return (index < length) ? raw.charAt(index) : Patterns.EOF_CHAR;
  }

  private void emitInstruction(Instruction inst, boolean preprocessorScope) throws CodeSyntaxException {
    inst.setLineNumber(instLine + 1);
    inst.setCharOffset(instOffset + 1);
    if (preprocessorScope) {
      inst.setPreprocessScope();
    }
    sink.accept(inst);
  }

  private void emitInstruction(Instruction inst) throws CodeSyntaxException {
    emitInstruction(inst, preprocess);
  }

  private boolean emitInvalid() throws CodeSyntaxException {
    sink.accept(maker.text(new StringView(raw, matcher.start() - 1, matcher.end() + 1)));
    return true;
  }

  /**
   * Text line numbering is tracked separately from other instructions.
   */
  private void emitText(int start, int end) throws CodeSyntaxException {
    Instruction inst = maker.text(raw, start, end);
    inst.setLineNumber(textLine + 1);
    inst.setCharOffset(textOffset + 1);
    sink.accept(inst);
  }

  private ErrorInfo error(SyntaxErrorType code) {
    return error(code, 0, false);
  }

  private ErrorInfo error(SyntaxErrorType code, boolean textLoc) {
    return error(code, 0, textLoc);
  }

  /**
   * Include an offset to nudge the error message character offset right to the position of the
   * error.
   */
  private ErrorInfo error(SyntaxErrorType code, int offset, boolean textLoc) {
    ErrorInfo info = new ErrorInfo(code);
    info.code(code);
    if (textLoc) {
      info.line(textLine + 1);
      info.offset(textOffset + 1);
    } else {
      info.line(instLine + 1);
      info.offset(instOffset + 1 + offset);
    }
    return info;
  }

  private void fail(ErrorInfo info) throws CodeSyntaxException {
    if (!validate) {
      throw new CodeSyntaxException(info);
    }
    errors.add(info);
  }

  /**
   * Attempt to parse the meta-delimited chars into an Instruction. The start parameter must
   * point to the location of a '{' character in the raw string.  The end parameter must
   * point 1 character past a '}' character in the raw string, and start must be < end.
   *
   * Depending on how the parse fails, this may throw a syntax exception. It all depends
   * on the degree of ambiguity.
   *
   * For example if the starting sequence for an instruction is found and the rest is
   * invalid, we may throw syntax error.  Instead, if the parse is clearly not an instruction
   * candidate we ignore the entire sequence and emit a text instruction. An example of
   * the latter is if the first character inside the META_LEFT is whitespace.  We require
   * that instructions begin immediately following the META_LEFT character.
   *
   * Package-private to facilitate unit testing.
   */
  private boolean matchMeta(int start, int end) throws CodeSyntaxException {
    if (!(start < end)) {
      throw new RuntimeException("Start position should always be less than end. Bug in tokenizer.");
    }

    // Start token matching everything between '{' and '}'
    matcher.region(start + 1, end - 1);

    // See if the current instruction is scoped to the pre-processor.
    if (matcher.peek(0, '^')) {
      // If not in pre-processing mode, skip it.
      if (!preprocess) {
        return true;
      }
      matcher.seek(1);

    } else if (preprocess) {
      // Normal instructions in pre-processing mode are output as text.
      return false;
    }

    // Emit a comment, skipping over the "#".
    if (matcher.peek(0, '#')) {
      matcher.seek(1);
      Instruction comment = maker.comment(raw, matcher.pointer(), matcher.end());
      emitInstruction(comment);
      return true;
    }

    return parseKeyword() || parseVariable();
  }

  /**
   * Attempt to parse the range into a keyword instruction.
   */
  private boolean parseKeyword() throws CodeSyntaxException {
    if (!matcher.keyword()) {
      return false;
    }

    StringView keyword = matcher.consume();
    if (keyword.lastChar() == '?') {
      Predicate predicate = resolvePredicate(keyword.subview(1, keyword.length()));
      Arguments args = parsePredicateArguments(predicate);
      if (args == null) {
        return emitInvalid();
      }
      emitInstruction(maker.predicate(predicate, args));
      return true;
    }

    InstructionType type = InstructionTable.get(keyword);
    if (type == null) {
      fail(error(INVALID_INSTRUCTION).data(keyword));
      return emitInvalid();
    }
    return parseInstruction(type, matcher.pointer(), matcher.end());
  }

  /**
   * We've found the start of an instruction. Parse the rest of the range.
   */
  private boolean parseInstruction(InstructionType type, int start, int end) throws CodeSyntaxException {
    switch (type) {

      case ALTERNATES_WITH:
        // Look for SPACE "WITH" EOF
        if (!matcher.space()) {
          fail(error(SyntaxErrorType.WHITESPACE_EXPECTED).data(matcher.remainder()));
          return emitInvalid();
        }
        matcher.consume();
        if (!matcher.wordWith()) {
          fail(error(MISSING_WITH_KEYWORD).data(matcher.remainder()));
          return emitInvalid();
        }
        matcher.consume();
        if (!matcher.finished()) {
          fail(error(EXTRA_CHARS).type(type).data(matcher.remainder()));
          return emitInvalid();
        }
        emitInstruction(maker.alternates());
        return true;

      case BINDVAR:
      {
        if (!skipWhitespace()) {
          return emitInvalid();
        }

        // Parse the variable name.
        if (!matcher.localVariable()) {
          fail(error(BINDVAR_EXPECTS_NAME).data(matcher.remainder()));
          return emitInvalid();
        }
        String name = matcher.consume().repr();

        if (!skipWhitespace()) {
          return emitInvalid();
        }

        Variables vars = parseVariables();
        if (vars == null) {
          fail(error(MISSING_VARIABLE_NAME).data(matcher.remainder()));
          return emitInvalid();
        }

        BindVarInst instruction = maker.bindvar(name, vars);
        List<FormatterCall> formatters = parseFormatters(instruction, start);
        if (formatters == null) {
          emitInstruction(instruction);
        } else if (!formatters.isEmpty()) {
          instruction.setFormatters(formatters);
          emitInstruction(instruction);
        }
        return true;
      }

      case CTXVAR:
      {
        if (!skipWhitespace()) {
          return emitInvalid();
        }

        // Parse the variable name
        if (!matcher.localVariable()) {
          fail(error(CTXVAR_EXPECTS_NAME).data(matcher.remainder()));
          return emitInvalid();
        }
        String name = matcher.consume().repr();
        if (!skipWhitespace()) {
          return emitInvalid();
        }

        List<Binding> bindings = parseBindings();
        if (bindings == null) {
          fail(error(CTXVAR_EXPECTS_BINDINGS).data(matcher.remainder()));
          return emitInvalid();
        }

        CtxVarInst instruction = maker.ctxvar(name, bindings);
        emitInstruction(instruction);
        return true;
      }

      case END:
      case META_LEFT:
      case META_RIGHT:
      case NEWLINE:
      case SPACE:
      case TAB:
        // Nothing should follow these instructions.
        if (!matcher.finished()) {
          fail(error(EXTRA_CHARS).type(type).data(matcher.remainder()));
          return emitInvalid();
        }
        emitInstruction(maker.simple(type));
        return true;

      case IF:
        return parseIfExpression();

      case INJECT:
      {
        if (!skipWhitespace()) {
          return emitInvalid();
        }

        if (!matcher.localVariable()) {
          fail(error(INJECT_EXPECTS_NAME).data(matcher.remainder()));
          return emitInvalid();
        }
        String variable = matcher.consume().repr();

        if (!skipWhitespace()) {
          return emitInvalid();
        }

        if (!matcher.path()) {
          fail(error(INJECT_EXPECTS_PATH).data(matcher.remainder()));
          return emitInvalid();
        }
        String path = matcher.consume().repr();

        StringView rawArgs = null;
        Arguments args = Constants.EMPTY_ARGUMENTS;
        if (matcher.arguments()) {
          rawArgs = matcher.consume();
          args = new Arguments(rawArgs);
        }

        InjectInst instruction = maker.inject(variable, path, args);
        emitInstruction(instruction);
        return true;
      }

      case MACRO:
      {
        if (!skipWhitespace()) {
          return emitInvalid();
        }

        if (!matcher.path()) {
          fail(error(MACRO_EXPECTS_NAME));
          return emitInvalid();
        }
        StringView path = matcher.consume();

        if (!matcher.finished()) {
          fail(error(EXTRA_CHARS).type(type).data(matcher.remainder()));
          return emitInvalid();
        }

        MacroInst inst = maker.macro(path.repr());
        emitInstruction(inst);
        return true;
      }

      case OR_PREDICATE:
        if (matcher.space()) {
          matcher.consume();

          if (!matcher.predicate()) {
            fail(error(OR_EXPECTED_PREDICATE).type(type).data(matcher.remainder()));
            return emitInvalid();
          }
          Predicate predicate = resolvePredicate(matcher.consume());
          Arguments args = parsePredicateArguments(predicate);
          if (args == null) {
            // Error was emitted by parsePredicateArguments()
            return emitInvalid();
          }
          PredicateInst inst = maker.predicate(predicate, args);
          inst.setOr();
          emitInstruction(inst);
          return true;

        }
        if (!matcher.finished()) {
          fail(error(EXTRA_CHARS).type(type).data(matcher.remainder()));
          return emitInvalid();
        }
        emitInstruction(maker.or());
        return true;

      case REPEATED:
      case SECTION:
        return parseSection(type);

      default:
        throw new RuntimeException("Resolution failure: instruction type '" + type + "' has no text representation.");
    }
  }

  private boolean skipWhitespace() throws CodeSyntaxException {
    boolean result = matcher.space();
    if (!result) {
      fail(error(WHITESPACE_EXPECTED).data(matcher.remainder()));
    }
    matcher.consume();
    return result;
  }

  /**
   * Lookup the keyword in the predicate table, raise an error if unknown.
   */
  private Predicate resolvePredicate(StringView keyword) throws CodeSyntaxException {
    Predicate predicate = predicateTable.get(keyword);
    if (predicate == null) {
      fail(error(PREDICATE_UNKNOWN).data(keyword.repr()));
      // Emit a dummy predicate with this name.
      return new BasePredicate(keyword.repr(), false) { };
    }
    return predicate;
  }

  /**
   * After we've resolved a predicate implementation, parse its optional arguments.
   */
  private Arguments parsePredicateArguments(Predicate predicate) throws CodeSyntaxException {
    StringView rawArgs = null;
    if (matcher.predicateArgs()) {
      rawArgs = matcher.consume();
    }

    Arguments args = null;
    if (rawArgs == null) {
      args = new Arguments();
      if (predicate.requiresArgs()) {
        fail(error(PREDICATE_NEEDS_ARGS).data(predicate));
        return null;
      }
    } else {
      args = new Arguments(rawArgs);
    }

    try {
      predicate.validateArgs(args);
    } catch (ArgumentsException e) {
      String identifier = predicate.identifier();
      fail(error(PREDICATE_ARGS_INVALID).name(identifier).data(e.getMessage()));
      return null;
    }
    return args;
  }

  /**
   * Parse boolean expression inside an IF instruction.
   *
   * NOTE: This does not currently enforce one type of boolean operator in the expression, so you
   * can mix OR with AND if you want, but there is no operator precedence or parenthesis so the
   * result may not be what was expected.
   */
  private boolean parseIfExpression() throws CodeSyntaxException {
    if (!matcher.whitespace()) {
      fail(error(WHITESPACE_EXPECTED).data(matcher.remainder()));
      return emitInvalid();
    }
    matcher.consume();

    // First, check if this is a predicate expression. If so, parse it.
    if (matcher.predicate()) {
      Predicate predicate = resolvePredicate(matcher.consume());
      Arguments args = parsePredicateArguments(predicate);
      if (args == null) {
        return emitInvalid();
      }
      try {
        emitInstruction(maker.ifpred(predicate, args));
      } catch (ArgumentsException e) {
        String identifier = predicate.identifier();
        fail(error(PREDICATE_ARGS_INVALID).name(identifier).data(e.getMessage()));
      }
      return true;
    }

    // Otherwise, this is an expression involving variable tests and operators.
    // If we find N variables, we'll need N-1 operators.
    List<String> vars = new ArrayList<>();
    List<Operator> ops = new ArrayList<>();
    int count = 0;
    while (matcher.variable()) {
      vars.add(matcher.consume().repr());
      if (matcher.whitespace()) {
        matcher.consume();
      }

      if (count == IF_VARIABLE_LIMIT) {
        fail(error(IF_TOO_MANY_VARS).limit(IF_VARIABLE_LIMIT));
        return emitInvalid();
      }
      count++;

      if (!matcher.operator()) {
        break;
      }

      Operator op = matcher.consume().repr().equals("&&") ? Operator.LOGICAL_AND : Operator.LOGICAL_OR;
      ops.add(op);
      if (matcher.whitespace()) {
        matcher.consume();
      }
    }

    if (!matcher.finished()) {
      fail(error(IF_EXPECTED_VAROP).data(matcher.remainder()));
      return emitInvalid();
    }
    if (vars.size() == 0) {
      fail(error(IF_EMPTY));
      return emitInvalid();
    }
    if (vars.size() != (ops.size() + 1)) {
      fail(error(IF_TOO_MANY_OPERATORS));
      return emitInvalid();
    }

    emitInstruction(maker.ifexpn(vars, ops));
    return true;
  }

  /**
   * Parse a SECTION or REPEATED instruction:
   *
   *   ".section" VARIABLE
   *   ".repeated section" VARIABLE
   *
   */
  private boolean parseSection(InstructionType type) throws CodeSyntaxException {
    if (!matcher.whitespace()) {
      fail(error(WHITESPACE_EXPECTED).data(matcher.remainder()));
      return emitInvalid();
    }
    matcher.consume();

    if (type == InstructionType.REPEATED) {
      if (!matcher.wordSection()) {
        fail(error(MISSING_SECTION_KEYWORD).data(matcher.remainder()));
        return emitInvalid();
      }
      matcher.consume();
      if (!matcher.whitespace()) {
        fail(error(WHITESPACE_EXPECTED).data(matcher.remainder()));
        return emitInvalid();
      }
      matcher.consume();
    }

    if (!matcher.variable()) {
      fail(error(VARIABLE_EXPECTED).data(matcher.remainder()));
      return emitInvalid();
    }
    StringView variable = matcher.consume();
    if (!matcher.finished()) {
      fail(error(EXTRA_CHARS).type(type).data(matcher.remainder()));
      return emitInvalid();
    }

    if (type == InstructionType.REPEATED) {
      emitInstruction(maker.repeated(variable.repr()));
    } else {
      emitInstruction(maker.section(variable.repr()));
    }
    return true;
  }

  /**
   * Parses a variable reference that can consist of one or more variable names followed
   * by an optional list of formatters. Formatters can be chained so the output of one
   * formatter can be "piped" into the next.
   */
  private boolean parseVariable() throws CodeSyntaxException {
    int start = matcher.matchStart();
    Variables vars = parseVariables();
    if (vars == null) {
      return false;
    }
    VariableInst instruction = maker.var(vars);
    List<FormatterCall> formatters = parseFormatters(instruction, start);
    if (formatters == null) {
      emitInstruction(instruction);
    } else if (!formatters.isEmpty()) {
      instruction.setFormatters(formatters);
      emitInstruction(instruction);
    }
    return true;
  }

  /**
   * Parse one or more variable references. Returns null if an error occurred.
   */
  private Variables parseVariables() throws CodeSyntaxException {
    if (!matcher.variable()) {
      return null;
    }

    boolean requirePipe = false;
    StringView token = matcher.consume();
    Variables vars = new Variables(token.repr());

    while (matcher.variablesDelimiter()) {
      matcher.consume();
      if (matcher.finished()) {
        return null;
      }
      if (matcher.pipe() || !matcher.variable()) {
        return null;
      }
      vars.add(matcher.consume().repr());
      requirePipe = true;
    }

    boolean matchedPipe = matcher.peek(0, '|');
    // If we see JavaScript boolean or operator, skip. This would fail anyway
    // when we tried to parse the second '|' as a formatter name, so since
    // we've already matched one pipe, sanity-check here.
    if (matchedPipe && matcher.peek(1, '|')) {
      return null;
    }

    // Multiple variable syntax is only allowed when passing variables to
    // formatters, which requires a pipe character immediately after the last variable.
    if (requirePipe) {
      if (!matchedPipe) {
        return null;
      }
    }
    return vars;
  }

  private List<Binding> parseBindings() throws CodeSyntaxException {
    List<Binding> bindings = new ArrayList<>();
    while (matcher.word()) {
      StringView name = matcher.consume();
      if (!matcher.equalsign()) {
        break;
      }
      matcher.skip();
      if (!matcher.variable()) {
        break;
      }

      Object[] reference = GeneralUtils.splitVariable(matcher.consume().repr());
      Binding binding = new Binding(name.repr(), reference);
      bindings.add(binding);
      if (!matcher.whitespace()) {
        break;
      }
      matcher.skip();
    }
    return bindings.isEmpty() ? null : bindings;
  }

  /**
   * Parse a formatter chain that may follow either a variable reference
   * or bind instruction.
   *
   * Returns:
   *   null             - no PIPE character was seen, so no formatters exist.
   *   <empty List>     - we saw a PIPE but encountered an error
   *   <non-empty List> - we parsed a valid list of formatters.
   */
  private List<FormatterCall> parseFormatters(Formattable formattable, int start) throws CodeSyntaxException {
    List<FormatterCall> formatters = null;
    while (matcher.pipe()) {
      matcher.consume();

      if (!matcher.formatter()) {
        fail(error(FORMATTER_INVALID, matcher.pointer() - start, false).name(matcher.remainder()));
        emitInvalid();
        return Collections.emptyList();
      }

      StringView name = matcher.consume();
      Formatter formatter = formatterTable.get(name);
      if (formatter == null) {
        fail(error(FORMATTER_UNKNOWN, matcher.matchStart() - start, false).name(name));
        emitInvalid();
        return Collections.emptyList();
      }

      StringView rawArgs = null;
      if (matcher.arguments()) {
        rawArgs = matcher.consume();
      }

      Arguments args = Constants.EMPTY_ARGUMENTS;
      if (formatter.requiresArgs() && rawArgs == null) {
          fail(error(FORMATTER_NEEDS_ARGS, matcher.matchStart() - start, false).data(formatter));
          emitInvalid();
          return Collections.emptyList();
      } else {
        args = new Arguments(rawArgs);
      }

      try {
        formatter.validateArgs(args);
      } catch (ArgumentsException e) {
        String identifier = formatter.identifier();
        fail(error(FORMATTER_ARGS_INVALID, matcher.matchStart() - start, false)
            .name(identifier)
            .data(e.getMessage()));
        emitInvalid();
        return Collections.emptyList();
      }

      if (formatters == null) {
        formatters = new ArrayList<>(2);
      }
      formatters.add(new FormatterCall(formatter, args));
    }

    // If the initial matcher.pipe() fails to enter the loop, this indicates an
    // unexpected character exists after the instruction.
    if (!matcher.finished()) {
      emitInvalid();
      return Collections.emptyList();
    }

    // If we parsed all the way to the ending meta character, we can return
    // a valid formatter list.
    return formatters;
  }

  /**
   * Initial state for the tokenizer machine, representing the outermost parse scope.
   * This machine is pretty simple as it only needs to track an alternating sequence
   * of text / instructions.
   */
  private final State stateInitial = new State() {

    @Override
    public State transition() throws CodeSyntaxException {

      while (true) {
        char ch = getc(index);
        switch (ch) {

          case EOF_CHAR:
            // Input is finished. See if we have any text left to flush.
            if (save < length) {
              emitText(save, length);
            }
            // A bit niche, but indicate the line number where the EOF occurred.
            instLine = lineCounter;
            instOffset = index - lineIndex;
            emitInstruction(maker.eof());
            return stateEOF;

          case NEWLINE_CHAR:
            // Keep track of which line we're currently on.
            lineCounter++;
            lineIndex = index + 1;
            break;

          case META_LEFT_CHAR:
            instLine = lineCounter;
            instOffset = index - lineIndex;

            // Peek ahead to see if this is a multiline comment.
            if ((getc(index + 1) == '#') && getc(index + 2) == '#') {
              // Flush any text before the comment.
              if (save < index) {
                emitText(save, index);
              }

              index += 3;
              return stateMultilineComment;
            }

            // Skip over duplicate META_LEFT characters until we find the last one
            // before the corresponding META_RIGHT.
            metaLeft = index;
            break;

          case META_RIGHT_CHAR:
            // We found the right-hand boundary of a potential instruction.
            if (metaLeft != -1) {

              // Flush the text leading up to META_RIGHT, then attempt to parse the instruction.
              if (save < metaLeft) {
                emitText(save, metaLeft);
              }
              if (!matchMeta(metaLeft, index + 1)) {
                // Nothing looked like a keyword or variable, so treat the sequence as plain text.
                emitText(metaLeft, index + 1);
              }
              metaLeft = -1;

            } else {

              // Not an instruction candidate. Treat the entire sequence up to META_RIGHT as plain text.
              emitText(save, index + 1);
            }

            // Set starting line of next instruction.
            textLine = lineCounter;
            textOffset = index + 1 - lineIndex;
            save = index + 1;
            break;

          default:
            break;
        }

        index++;
      }
    }

  };

  /**
   * MULTILINE COMMENT state.  {## ... ##}
   */
  private final State stateMultilineComment = new State() {
    @Override
    public State transition() throws CodeSyntaxException {
      int start = index;
      while (true) {
        char ch = getc(index);
        switch (ch) {

          case EOF_CHAR:
            emitInstruction(maker.mcomment(raw, start, index));
            fail(error(SyntaxErrorType.EOF_IN_COMMENT, true));
            return stateEOF;

          case NEWLINE_CHAR:
            lineCounter++;
            lineIndex = index + 1;
            break;

          case POUND_CHAR:
            // Look-ahead for ##} sequence to terminate the comment block.
            if (getc(index + 1) == POUND_CHAR && getc(index + 2) == META_RIGHT_CHAR) {
              emitInstruction(maker.mcomment(raw, start, index), false);
              // Skip over multi-line suffix.
              index += 3;
              save = index;

              // Return to outer state.
              return stateInitial;
            }
            break;

          default:
            break;
        }

        index++;
      }
    }
  };

  /**
   * Terminal state when EOF on the input is reached.
   */
  private final State stateEOF = new State() {

    @Override
    public State transition() throws CodeSyntaxException {
      throw new RuntimeException("Tokenizer should never try to transition from the EOF state. "
          + "This is either a bug in the state machine or perhaps a tokenizer instance was reused.");
    }

  };

  interface State {

    State transition() throws CodeSyntaxException;

  }

}
