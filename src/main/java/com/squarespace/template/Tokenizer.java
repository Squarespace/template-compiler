package com.squarespace.template;

import static com.squarespace.template.Patterns.EOF_CHAR;
import static com.squarespace.template.Patterns.META_LEFT_CHAR;
import static com.squarespace.template.Patterns.META_RIGHT_CHAR;
import static com.squarespace.template.Patterns.NEWLINE_CHAR;
import static com.squarespace.template.Patterns.POUND_CHAR;
import static com.squarespace.template.SyntaxErrorType.EXTRA_CHARS;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_ARGS_INVALID;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_INVALID;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_NEEDS_ARGS;
import static com.squarespace.template.SyntaxErrorType.FORMATTER_UNKNOWN;
import static com.squarespace.template.SyntaxErrorType.IF_EMPTY;
import static com.squarespace.template.SyntaxErrorType.IF_EXPECTED_VAROP;
import static com.squarespace.template.SyntaxErrorType.IF_TOO_MANY_OPERATORS;
import static com.squarespace.template.SyntaxErrorType.IF_TOO_MANY_VARS;
import static com.squarespace.template.SyntaxErrorType.INVALID_INSTRUCTION;
import static com.squarespace.template.SyntaxErrorType.MISSING_SECTION_KEYWORD;
import static com.squarespace.template.SyntaxErrorType.MISSING_WITH_KEYWORD;
import static com.squarespace.template.SyntaxErrorType.OR_EXPECTED_PREDICATE;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_ARGS_INVALID;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_NEEDS_ARGS;
import static com.squarespace.template.SyntaxErrorType.PREDICATE_UNKNOWN;
import static com.squarespace.template.SyntaxErrorType.VARIABLE_EXPECTED;
import static com.squarespace.template.SyntaxErrorType.WHITESPACE_EXPECTED;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.template.Instructions.PredicateInst;


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

  private final static int IF_VARIABLE_LIMIT = 5;
  
  private final String raw;

  private final int length;
  
  private final CodeSink sink;

  private final PredicateTable predicateTable;
  
  private final FormatterTable formatterTable;
  
  private final TokenMatcher matcher;
  
  private final CodeMaker maker;

  private State state;

  private int textLine;
  
  private int textOffset;

  private int instLine;
  
  private int instOffset;
  
  private int index = 0;

  private int save = 0;
  
  private int metaLeft = -1;
  
  private int lineCounter = 0;
  
  private int lineIndex = 0;

  
  public Tokenizer(String raw, CodeSink sink, FormatterTable formatterTable, PredicateTable predicateTable) {
    this.raw = raw;
    this.length = raw.length();
    this.sink = sink;
    this.formatterTable = formatterTable;
    this.predicateTable = predicateTable;
    this.matcher = new TokenMatcher(raw);
    this.maker = new CodeMaker();
    this.state = state_INITIAL;
  }
  
  public void consume() throws CodeSyntaxException {
    do {
      state = state.transition();
    } while (state != state_EOF);
  }
  
  private char getc(int index) {
    if (index >= length) {
      return Patterns.EOF_CHAR;
    }
    return raw.charAt(index);
  }
  
  private void emitInstruction(Instruction inst) throws CodeSyntaxException {
    inst.setLineNumber(instLine + 1);
    inst.setCharOffset(instOffset + 1);
    sink.accept(inst);
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

  private ErrorInfo<SyntaxErrorType> error(SyntaxErrorType code) {
    ErrorInfo<SyntaxErrorType> mk = new ErrorInfo<>(code);
    mk.code(code);
    mk.line(instLine + 1);
    mk.offset(instOffset);
    return mk;
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
  boolean matchMeta(int start, int end) throws CodeSyntaxException {
    if (!(start < end)) {
      throw new RuntimeException("Start position should always be less than end. Bug in tokenizer.");
    }
    int innerStart = start + 1;
    int innerEnd = end - 1;
    
    // Emit a comment, skipping over the "#".
    if (getc(innerStart) == '#') {
      emitInstruction(maker.comment(raw, innerStart + 1, innerEnd));
      return true;
    }
    
    // Start token matching everything between '{' and '}'
    matcher.region(innerStart, innerEnd);
    
    Instruction inst = parseKeyword();
    if (inst != null) {
      emitInstruction(inst);
      return true;
    }
    
    inst = parseVariable();
    if (inst != null) {
      emitInstruction(inst);
      return true;
    }
    return false;
  }

  /**
   * Attempt to parse the range into a keyword instruction.
   */
  private Instruction parseKeyword() throws CodeSyntaxException {
    if (!matcher.keyword()) {
      return null;
    }
    
    StringView keyword = matcher.consume();
    if (keyword.lastChar() == '?') {
      Predicate predicate = resolvePredicate(keyword.subview(1, keyword.length()));
      return parsePredicate(predicate);
    }
    
    InstructionType type = InstructionTable.get(keyword);
    if (type == null) {
      throw new CodeSyntaxException(error(INVALID_INSTRUCTION).data(keyword));
    }
    return parseInstruction(type, matcher.pointer(), matcher.end());
  }
  
  /**
   * We've found the start of an instruction. Parse the rest of the range.
   */
  private Instruction parseInstruction(InstructionType type, int start, int end) throws CodeSyntaxException {

    switch (type) {

      case ALTERNATES_WITH:
        // Look for SPACE "WITH" EOF
        if (!matcher.space()) {
          throw new CodeSyntaxException(error(SyntaxErrorType.WHITESPACE_EXPECTED).data(matcher.remainder()));
        }
        matcher.consume();
        if (!matcher.wordWith()) {
          throw new CodeSyntaxException(error(MISSING_WITH_KEYWORD).data(matcher.remainder()));
        }
        matcher.consume();
        if (!matcher.finished()) {
          throw new CodeSyntaxException(error(EXTRA_CHARS).type(type).data(matcher.remainder()));
        }
        return maker.alternates();
        
      case END:
      case META_LEFT:
      case META_RIGHT:
      case NEWLINE:
      case SPACE:
      case TAB:
        // Nothing should follow these instructions.
        if (!matcher.finished()) {
          throw new CodeSyntaxException(error(EXTRA_CHARS).type(type).data(matcher.remainder()));
        }
        return maker.simple(type);

      case IF:
        return parseIfExpression();
        
      case OR_PREDICATE:
        if (matcher.space()) {
          matcher.consume();

          if (!matcher.predicate()) {
            throw new CodeSyntaxException(error(OR_EXPECTED_PREDICATE).type(type).data(matcher.remainder()));
          }
          Predicate predicate = resolvePredicate(matcher.consume());
          PredicateInst inst = parsePredicate(predicate);
          inst.setOr();
          return inst;

        }
        if (!matcher.finished()) {
          throw new CodeSyntaxException(error(EXTRA_CHARS).type(type).data(matcher.remainder()));
        }
        return maker.or();
        
      case REPEATED:
      case SECTION:
        return parseSection(type);
        
      default:
        throw new RuntimeException("Resolution failure: instruction type '" + type + "' has no text representation.");
    }
  }
  
  /**
   * Lookup the keyword in the predicate table, raise an error if unknown.
   */
  private Predicate resolvePredicate(StringView keyword) throws CodeSyntaxException {
    Predicate predicate = predicateTable.get(keyword);
    if (predicate == null) {
      throw new CodeSyntaxException(error(PREDICATE_UNKNOWN).data(keyword.repr()));
    }
    return predicate;
  }

  /**
   * After we've resolved a predicate implementation, parse its optional arguments.
   */
  private PredicateInst parsePredicate(Predicate predicate) throws CodeSyntaxException {
    StringView rawArgs = null;
    if (matcher.arguments()) {
      rawArgs = matcher.consume();
    }

    Arguments args = Constants.EMPTY_ARGUMENTS;
    if (rawArgs == null) {
      if (predicate.requiresArgs()) {
        throw new CodeSyntaxException(error(PREDICATE_NEEDS_ARGS).data(predicate));
      }
    } else {
      try {
        args = new Arguments(rawArgs);
        predicate.validateArgs(args);
      } catch (ArgumentsException e) {
        String identifier = predicate.getIdentifier();
        throw new CodeSyntaxException(error(PREDICATE_ARGS_INVALID).name(identifier).data(e.getMessage()));
      }
    }
    return maker.predicate(predicate, args);
  }
  
  /**
   * Parse boolean expression inside an IF instruction.
   * 
   * NOTE: This does not currently enforce one type of boolean operator in the expression, so you
   * can mix OR with AND if you want, but there is no operator precedence or parenthesis so the
   * result may not be what was expected.
   */
  private Instruction parseIfExpression() throws CodeSyntaxException {
    if (!matcher.whitespace()) {
      throw new CodeSyntaxException(error(WHITESPACE_EXPECTED).data(matcher.remainder()));
    }
    matcher.consume();
    
    // Loop, looking for variables and operators. If we find N variables, we'll need N-1 operators.
    List<String> vars = new ArrayList<>();
    List<Operator> ops = new ArrayList<>();
    int count = 0;
    while (matcher.variable()) {
      vars.add(matcher.consume().repr());
      if (matcher.whitespace()) {
        matcher.consume();
      }
      
      if (count == IF_VARIABLE_LIMIT) {
        throw new CodeSyntaxException(error(IF_TOO_MANY_VARS).limit(IF_VARIABLE_LIMIT));
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
      throw new CodeSyntaxException(error(IF_EXPECTED_VAROP).data(matcher.remainder()));
    }
    if (vars.size() == 0) {
      throw new CodeSyntaxException(error(IF_EMPTY));
    }
    if (vars.size() != (ops.size() + 1)) {
      throw new CodeSyntaxException(error(IF_TOO_MANY_OPERATORS));
    }
    
    return maker.ifn(vars, ops);
  }
  
  /**
   * Parse a SECTION or REPEATED instruction:
   * 
   *   ".section" VARIABLE
   *   ".repeated section" VARIABLE
   * 
   */
  private Instruction parseSection(InstructionType type) throws CodeSyntaxException {
    if (!matcher.whitespace()) {
      throw new CodeSyntaxException(error(WHITESPACE_EXPECTED).data(matcher.remainder()));
    }
    matcher.consume();
    
    if (type == InstructionType.REPEATED) {
      if (!matcher.wordSection()) {
        throw new CodeSyntaxException(error(MISSING_SECTION_KEYWORD).data(matcher.remainder()));
      }
      matcher.consume();
      if (!matcher.whitespace()) {
        throw new CodeSyntaxException(error(WHITESPACE_EXPECTED).data(matcher.remainder()));
      }
      matcher.consume();
    }
    
    if (!matcher.variable()) {
      throw new CodeSyntaxException(error(VARIABLE_EXPECTED).data(matcher.remainder()));
    }
    StringView variable = matcher.consume();
    if (!matcher.finished()) {
      throw new CodeSyntaxException(error(EXTRA_CHARS).type(type).data(matcher.remainder()));
    }

    if (type == InstructionType.REPEATED) {
      return maker.repeated(variable.repr());
    }
    return maker.section(variable.repr());
  }

  /**
   * Parses the characters between (start, end) and returns one of:
   * 
   * 1) null, if no match for a variable name found.
   * 2) VariableInst, if variable name but no piped formatter found.
   * 3) FormatterInst, if variable name plus a valid piped formatter with optional arguments.
   * 4) throws error
   */
  private Instruction parseVariable() throws CodeSyntaxException {
    if (!matcher.variable()) {
      return null;
    }
    StringView variable = matcher.consume();
    
    // Variable with no formatter applied.
    if (!matcher.pipe()) {
      if (!matcher.finished()) {
        return null;
      }
      return maker.var(variable.repr());
    }

    // We have a formatter to parse.
    matcher.consume();
    if (!matcher.formatter()) {
      throw new CodeSyntaxException(error(FORMATTER_INVALID).name(matcher.remainder()));
    }
    StringView name = matcher.consume();
    Formatter formatter = formatterTable.get(name);
    if (formatter == null) {
      throw new CodeSyntaxException(error(FORMATTER_UNKNOWN).name(name));
    }
    
    StringView rawArgs = null;
    if (matcher.arguments()) {
      rawArgs = matcher.consume();
    }
    
    Arguments args = Constants.EMPTY_ARGUMENTS;
    if (rawArgs == null) {
      if (formatter.requiresArgs()) {
        throw new CodeSyntaxException(error(FORMATTER_NEEDS_ARGS).data(formatter));
      }
    } else {
      try {
        args = new Arguments(rawArgs);
        formatter.validateArgs(args);
      } catch (ArgumentsException e) {
        String identifier = formatter.getIdentifier();
        throw new CodeSyntaxException(error(FORMATTER_ARGS_INVALID).name(identifier).data(e.getMessage()));
      }
    }

    return maker.formatter(variable.repr(), formatter, args);
  }
  
  /**
   * Initial state for the tokenizer machine, representing the outermost parse scope.
   * This machine is pretty simple as it only needs to track an alternating sequence
   * of text / instructions.
   */
  private final State state_INITIAL = new State() {
    
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
            return state_EOF;
          
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
              return state_MULTILINE_COMMENT;
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
        }
        
        index++;
      }
    }
    
  };
  
  /**
   * MULTILINE COMMENT state.  {## ... ##}
   */
  private final State state_MULTILINE_COMMENT = new State() {
    @Override
    public State transition() throws CodeSyntaxException {
      int start = index;
      while (true) {
        char ch = getc(index);
        switch (ch) {

          case EOF_CHAR:
            throw new CodeSyntaxException(error(SyntaxErrorType.EOF_IN_COMMENT));
          
          case NEWLINE_CHAR:
            lineCounter++;
            lineIndex = index + 1;
            break;
            
          case POUND_CHAR:
            // Look-ahead for ##} sequence to terminate the comment block.
            if (getc(index + 1) == POUND_CHAR && getc(index + 2) == META_RIGHT_CHAR) {
              emitInstruction(maker.mcomment(raw, start, index));
              // Skip over multi-line suffix.
              index += 3;
              save = index;
              
              // Return to outer state.
              return state_INITIAL;
            }
            break;
        }
        
        index++;
      }
    }
  };
  
  /**
   * Terminal state when EOF on the input is reached.
   */
  private final State state_EOF = new State() {
    
    @Override
    public State transition() throws CodeSyntaxException {
      throw new RuntimeException("Tokenizer should never try to transition from the EOF state. "
          + "This is either a bug in the state machine or perhaps a tokenizer instance was reused.");
    }
    
  };
  
  interface State {
    
    public State transition() throws CodeSyntaxException;
    
  }
}
