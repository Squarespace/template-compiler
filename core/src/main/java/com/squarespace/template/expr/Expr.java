package com.squarespace.template.expr;

import static com.squarespace.template.expr.Conversions.asbool;
import static com.squarespace.template.expr.Conversions.asint;
import static com.squarespace.template.expr.Conversions.asliteral;
import static com.squarespace.template.expr.Conversions.asnode;
import static com.squarespace.template.expr.Conversions.asnum;
import static com.squarespace.template.expr.Conversions.asstr;
import static com.squarespace.template.expr.Conversions.ch;
import static com.squarespace.template.expr.Conversions.decimali;
import static com.squarespace.template.expr.Conversions.hexi;
import static com.squarespace.template.expr.Operations.mul;
import static com.squarespace.template.expr.Tokens.MINUS_ONE;
import static com.squarespace.template.expr.Tokens.bool;
import static com.squarespace.template.expr.Tokens.num;
import static com.squarespace.template.expr.Tokens.str;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.template.Context;
import com.squarespace.template.ErrorInfo;
import com.squarespace.template.ExecuteErrorType;
import com.squarespace.template.GeneralUtils;
import com.squarespace.template.TokenMatcher;

/**
 *  Expression evaluation using an extended version of Dijkstra's "shunting
 *  yard" algorithm with JavaScript semantics. This algorithm was chosen as
 *  it is simple, sufficient, and can be implemented compactly, minimizing
 *  the size of the code and making it easier to verify correct.
 *
 *  Features:
 *
 *   - Number, Boolean, Null, and String types
 *   - String parsing of backslash char, hex, and unicode escapes
 *     - unicode escapes can contain up to 6 hex characters with a
 *       value of <= 0x10FFFF
 *     - "\n", "\x20", "\u2018", "\u01f600" are all valid strings
 *   - Decimal and hex numbers
 *   - Assignment statements
 *   - Variable references
 *   - Operator precedence
 *   - Common math, binary and logical operators
 *   - Both strict and non-strict equality
 *   - Nesting of sub-expressions with parenthesis
 *   - Single pass evaluation of multiple semicolon-delimited expressions
 *   - String equality and concatenation
 *   - Function calls with a variable number of arguments
 *     - example: max(1, 2, 3, ..., N) is accepted
 *   - Type conversion functions
 *   - Common constants (pi, e, etc)
 *   - Configurable parse and reduce limits
 *     - maximum tokens an expression can contain
 *     - maximum length of a concatenated string
 *
 *  The algorithm assumes the expression is well-formed and performs minimal
 *  validation.  A malformed expression should either terminate evaluation
 *  early or produce no value.
 */
public class Expr {

  private static final String E_MISMATCHED_OP = "Mismatched operator found:";
  private static final String E_UNEXPECTED_OPERATOR = "Unexpected operator found during evaluation:";

  private static final ExprOptions DEFAULT_OPTIONS = new ExprOptions();

  // Value for the PI constant in the expression engine
  public static final double PI = 3.141592653589793;

  // Value for the E constant in the expression engine
  public static final double E = 2.718281828459045;

  // Expressions ready to evaluate
  private final List<List<Token>> expr = new ArrayList<>();

  // Errors that occur during tokenization or assembly. Evaluation errors will
  // be appended directly to the context's errors array.
  private final List<String> errors = new ArrayList<>();

  // Temporary storage to build expressions
  private final Stack<Token> tokens = new Stack<>();

  // reasonable limits to bound the computation
  private final int maxTokens;
  private final int maxStringLen;

  private final TokenMatcher matcher;
  private final String raw;

  /**
   * Construct an expression from the given string. Construction will
   * only tokenize the input.
   */
  public Expr(String raw) {
    this(raw, null);
  }

  /**
   * Construct an expression from the given string and options.
   * Construction will only tokenize the input.
   */
  public Expr(String raw, ExprOptions options) {
    if (options == null) {
      options = DEFAULT_OPTIONS;
    }
    this.maxTokens = options.maxTokens();
    this.maxStringLen = options.maxStringLen();
    this.matcher = new TokenMatcher(raw);
    this.raw = raw;
    this.tokenize(raw, 0, raw.length());
  }

  @Override
  public String toString() {
    return "Expression[" + new TextNode(this.raw) + "]";
  }

  /**
   * Errors that occur when parsing and assembling the expression.
   * Runtime errors are appended directly to the Context.
   */
  public List<String> errors() {
    return this.errors;
  }

  /**
   * Tokens parsed from the input string.
   */
  public Stack<Token> tokens() {
    return this.tokens;
  }

  /**
   * Expressions assembled from the input tokens.
   */
  public List<List<Token>> expressions() {
    return this.expr;
  }

  /**
   * Built-in functions.
   *TODO: future, make these configurable
   */
  private final Map<String, FunctionDef> FUNCTIONS = new HashMap<String, FunctionDef>() {
    {
      put("max", Functions::max);
      put("min", Functions::min);
      put("abs", Functions::abs);
      put("num", Functions::num);
      put("str", Functions::str);
      put("bool", Functions::bool);
    }
  };

  /**
   * Built-in constants.
   * TODO: future, make these configurable
   */
  private final Map<String, Token> CONSTANTS = new HashMap<String, Token>(){{
    put("null", Tokens.NULL);
    put("true", Tokens.TRUE);
    put("false", Tokens.FALSE);
    put("PI", Tokens.PI);
    put("E", Tokens.E);
    put("Infinity", Tokens.INFINITY);
    put("NaN", Tokens.NAN);
  }};

  /**
   * Reduce each of the expressions to its simplest form, then return the final value
   * of the last expression as output.
   */
  public JsonNode reduce(Context ctx) {
    int len = this.expr.size();
    JsonNode r = null;
    for (int i = 0; i < len; i++) {
      List<Token> expr = this.expr.get(i);
      r = this.reduceExpr(ctx, expr);
    }
    return r;
  }

  /**
   * Reduce an expression to its simplest form.
   */
  public JsonNode reduceExpr(Context ctx, List<Token> expr) {
    Stack<Token> stack = new Stack<>();

    loop: for (Token t : expr) {
      switch (t.type) {
        case BOOLEAN:
        case STRING:
        case NUMBER:
        case NULL:
        case VARIABLE:
        case ARGS:
          stack.push(t);
          continue;

        case CALL: {
          List<Token> args = new ArrayList<>();
          Token top = stack.top();

          // Pop values from the stack until we hit the argument delimiter.
          while (top != null && top.type != ExprTokenType.ARGS) {
            Token arg = asliteral(ctx, stack.pop());
            if (arg != null) {
              args.add(arg);
            }
            top = stack.top();
          }

          // Pop the argument delimiter
          stack.pop();

          // Reverse the arguments and apply them.
          Collections.reverse(args);

          // Get a reference to the function implementation.
          CallToken call = (CallToken)t;
          FunctionDef fimpl = FUNCTIONS.get(call.name);

          // The function is guaranteed to exist here, since its existence
          // was verified when the call token was constructed.
          Token r = fimpl.apply(args);
          if (r == null) {
            ErrorInfo error = ctx.error(ExecuteErrorType.EXPRESSION_REDUCE)
                .data("Error calling function " + call.name);
            ctx.addError(error);
            break loop;
          }

          // Push the result onto the stack.
          stack.push(r);
          continue;
        }

        case OPERATOR: {
          // Unary operators directly manipulate top of stack to avoid a pop-then-push
          Operator o = ((OperatorToken)t).value;
          switch (o.type) {
            case MINUS: {
              Token arg = asliteral(ctx, stack.top());
              stack.top(arg == null ? null : mul(MINUS_ONE, arg));
              continue;
            }

            case PLUS: {
              // unary plus casts the argument to number but doesn't change sign
              Token arg = asliteral(ctx, stack.top());
              stack.top(arg == null ? null : num(asnum(arg)));
              continue;
            }

            case LNOT: {
              Token arg = asliteral(ctx, stack.top());
              stack.top(arg == null ? null : bool(!asbool(arg)));
              continue;
            }

            case BNOT: {
              Token arg = asliteral(ctx, stack.top());
              stack.top(arg == null ? null : num(~asint(arg)));
              continue;
            }

            case ASN: {
              Token b = asliteral(ctx, stack.pop());
              Token a = stack.pop();
              // Make sure the arguments to the assignment are valid
              if (a != null && a.type == ExprTokenType.VARIABLE && b != null) {
                Object[] name = ((VarToken)a).name;
                if (name != null && name.length == 1 && name[0] instanceof String && ((String)name[0]).charAt(0) == '@') {
                  // Set the variable in the context.
                  ctx.setVar((String)name[0], asnode(b));
                }
              }
              // When an assignment operator is encountered, we consider the expression
              // complete. This leaves no result.
              stack.top(null);
              break loop;
            }

            default:
              // fall through to handle all binary operators
          }

          // Binary operators, pop 2 args from the stack and push result
          Token b = asliteral(ctx, stack.pop());
          Token a = asliteral(ctx, stack.pop());

          // Validate operator args are present and valid
          if (a == null || b == null) {
            // Invalid arguments to operator, bail out.
            ErrorInfo error = ctx.error(ExecuteErrorType.EXPRESSION_REDUCE)
                .data("Invalid arguments to operator " + o.desc);
            ctx.addError(error);
            break loop;
          }

          Token r = null;
          switch (o.type) {
            case MUL:
              r = mul(a, b);
              break;
            case DIV: {
              double v = asnum(b);
              r = num(v == 0 ? Double.NaN : asnum(a) / v);
              break;
            }
            case ADD:
              // Numeric addition or string concatenation.
              if (a.type == ExprTokenType.STRING || b.type == ExprTokenType.STRING) {
                // Convert both arguments to string
                String _a = asstr(a);
                String _b = asstr(b);
                // Ensure a concatenated string won't exceed the configured limit.
                if (this.maxStringLen > 0 && (_a.length() + _b.length() > this.maxStringLen)) {
                  ErrorInfo error = ctx.error(ExecuteErrorType.EXPRESSION_REDUCE)
                      .data("Concatenation would exceed maximum string length " + this.maxStringLen);
                  ctx.addError(error);
                  break loop;
                }
                r = str(_a + _b);
              } else {
                r = num(asnum(a) + asnum(b));
              }
              break;
            case SUB:
              r = num(asnum(a) - asnum(b));
              break;
            case POW:
              r = num(Math.pow(asnum(a), asnum(b)));
              break;
            case MOD: {
              double v = asnum(b);
              r = num(v == 0 ? Double.NaN : asnum(a) % v);
              break;
            }
            case SHL:
              r = num(asint(a) << asint(b));
              break;
            case SHR:
              r = num(asint(a) >> asint(b));
              break;
            case LT:
              r = Operations.lt(a, b);
              break;
            case LTEQ:
              r = Operations.lteq(a, b);
              break;
            case GT:
              r = Operations.gt(a, b);
              break;
            case GTEQ:
              r = Operations.gteq(a, b);
              break;
            case EQ:
              r = Operations.eq(a, b);
              break;
            case NEQ:
              r = Operations.neq(a, b);
              break;
            case SEQ:
              r = Operations.seq(a, b);
              break;
            case SNEQ:
              r = Operations.sneq(a, b);
              break;
            case BAND:
              r = num(asint(a) & asint(b));
              break;
            case BXOR:
              r = num(asint(a) ^ asint(b));
              break;
            case BOR:
              r = num(asint(a) | asint(b));
              break;
            case LAND:
              r = bool(asbool(a) && asbool(b));
              break;
            case LOR:
              r = bool(asbool(a) || asbool(b));
              break;

            default:
              // all unary and binary operators should be handled above.
              // other operators (parenthesis, semicolon) are eliminated when
              // the expressions are assembled.
              this.errors.add(E_UNEXPECTED_OPERATOR + " " + o.desc);
              stack.top(null);
              break loop;
          }

          // Push result onto stack
          stack.push(r);
        }
      }
    }

    // Return a valid literal from the top of the stack, or null
    // if an unexpected token is present.
    Token r = stack.top();
    if (r != null) {
      // Ensure the value is a literal
      Token v = asliteral(ctx, r);
      if (v != null) {
        // We have a supported value.
        switch (v.type) {
          case BOOLEAN:
            return ((BooleanToken)v).value ? BooleanNode.TRUE : BooleanNode.FALSE;
          case NUMBER:
            return new DoubleNode(((NumberToken)v).value);
          case STRING:
            return new TextNode(((StringToken)v).value);
          case NULL:
            return NullNode.getInstance();
          default:
            // Fall through
            break;
        }
      }
      // The token was an unexpected type, which is an error
      ErrorInfo error = ctx.error(ExecuteErrorType.EXPRESSION_REDUCE)
          .data("Reduce error: unexpected token on stack");
      ctx.addError(error);
    }

    return null;
  }

  /**
   * Iterate over tokens and build expressions using the shunting yard algorithm.
   */
  public void build() {
    // If any tokenization errors occurred, refuse to assemble the expression. The
    // token array is likely incomplete and therefore the expression should be
    // considered invalid.
    if (!this.errors.isEmpty()) {
      return;
    }

    // Output stack containing final RPN expression
    List<Token> out = new ArrayList<>();

    // Stack used to shunt operator tokens during expression construction
    Stack<Token> ops = new Stack<>();

    for (Token t : this.tokens.elems()) {
      switch (t.type) {
        case OPERATOR: {
          Operator o = ((OperatorToken)t).value;
          switch (o.type) {
            case SEMI:
              // Semicolons separate multiple expressions. Push the expression
              // we've accumulated and reset the state.
              this.pushExpr(out, ops);
              out = new ArrayList<>();
              break;

            case LPRN:
              // Opens a nested expression or function call
              ops.push(t);
              break;

            case COMMA: {
              // Argument separator outputs all non-operators until we hit
              // a left parenthesis
              Token top = ops.top();
              while (cond1(top)) {
                out.add(ops.pop());
                top = ops.top();
              }
              break;
            }

            case RPRN: {
              // Output all non-operator tokens until we hit the matching
              // left parenthesis.
              Token top = ops.top();
              while (cond1(top)) {
                out.add(ops.pop());
                top = ops.top();
              }
              // Ensure parenthesis are balanced.
              if (top == null ||
                  top.type != ExprTokenType.OPERATOR ||
                  ((OperatorToken)top).value.type != OperatorType.LPRN) {
                this.errors.add(E_MISMATCHED_OP + " " + ((OperatorToken)t).value.desc);
                return;
              }

              ops.pop();

              // If a function call token preceeded the left parenthesis, pop it to the output
              top = ops.top();
              if (top != null && top.type == ExprTokenType.CALL) {
                out.add(ops.pop());
              }
              break;
            }

            default: {
              // We have an operator. Before we can send it to the output
              // we need to pop all other operators with higher precedence,
              // or the same precedence with left associativity. We also stop
              // at non-operators and left parenthesis.
              Token top = ops.top();
              while (cond2(top, o.prec)) {
                out.add(ops.pop());
                top = ops.top();
              }
              ops.push(t);
              break;
            }
          }
          break;
        }

        case CALL:
          // Delimit the end of the argument list for the function call
          out.add(Tokens.ARGS);

          // Push the call onto the operator stack. Once all arguments have
          // been output we pop the call and output it.
          ops.push(t);
          break;

        default:
          out.add(t);
          break;
      }
    }
    this.pushExpr(out, ops);
  }

  /**
   * Condition for build() method.
   */
  private boolean cond1(Token t) {
    if (t == null) {
      return false;
    }
    if (!(t instanceof OperatorToken)) {
      return false;
    }
    Operator o = ((OperatorToken)t).value;
    return o.type != OperatorType.LPRN;
  }

  /**
   * Condition for build() method.
   */
  private boolean cond2(Token t, int prec) {
    if (t == null) {
      return false;
    }
    if (!(t instanceof OperatorToken)) {
      return false;
    }
    Operator o = ((OperatorToken)t).value;
    return o.type != OperatorType.LPRN && (o.prec > prec ||
        (o.prec == prec && o.assoc == Assoc.LEFT));
  }

  /**
   * Push a token.
   */
  private void push(Token t) {
    // If an error occurs, we stop accepting tokens.
    if (!this.errors.isEmpty()) {
      return;
    }

    if (t.type == ExprTokenType.OPERATOR) {
      OperatorToken ot = (OperatorToken)t;
      switch (ot.value.type) {
        // convert unary plus / minus
        case SUB:
        case ADD: {
          Token top = this.tokens.top();
          if (top == null ||
              (top.type == ExprTokenType.OPERATOR &&
                  ((OperatorToken)top).value.type != OperatorType.RPRN)) {
            t = ot.value.type == OperatorType.SUB ? Operators.MINUS : Operators.PLUS;
          }
          break;
        }

        case LPRN: {
          Token top = this.tokens.top();
          if (top != null &&
              top.type == ExprTokenType.VARIABLE &&
              ((VarToken)top).name != null &&
              ((VarToken)top).name.length == 1) {
            // Check if name corresponds to a valid built-in function, and
            // convert the name to a function call token.
            Object name = ((VarToken)top).name[0];
            if (FUNCTIONS.containsKey(name)) {
              this.tokens.top(new CallToken((String)name));
            } else {
              this.errors.add("Invalid function: " + name);
              return;
            }
          }
          break;
        }

        default:
          break;
      }
    }
    this.tokens.push(t);
    if (this.maxTokens > 0 && this.tokens.length() > this.maxTokens) {
      this.errors.add("Expression exceeds the maximum number of allowed tokens: " + this.maxTokens);
    }
  }

  /**
   * Push an expression.
   */
  private void pushExpr(List<Token> queue, Stack<Token> ops) {
    while (ops.length() > 0) {
      Token t = ops.pop();
      // We detect unexpected operators here.
      if (t.type == ExprTokenType.OPERATOR) {
        OperatorToken o = (OperatorToken)t;
        switch (o.value.type) {
          case LPRN:
          case RPRN:
            this.errors.add(E_MISMATCHED_OP + " " + o.value.desc);
            return;
          default:
            break;
        }
      }
      queue.add(t);
    }
    if (!queue.isEmpty()) {
      this.expr.add(queue);
    }
  }

  /**
   * Tokenize the string input.
   */
  public void tokenize(String str, int i, int len) {
    loop: while (i < len) {
      char c0 = str.charAt(i);
      char c1 = ch(str, i + 1);
      switch (c0) {
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
          // check for a hexadecimal number prefix
          boolean hx = c0 == '0' && (c1 == 'x' || c1 == 'X');
          i = hx ? this.hex(str, i + 2, len) : this.decimal(str, i, len);
          if (i < 0) {
            // the hex / decimal parse methods emit errors, so no need to
            break loop;
          }
          continue;

        case '"':
        case '\'':
          i = string(str, i + 1, len, c0);
          if (i == -1) {
            break loop;
          }
          continue;

        case '*':
          if (c1 == '*') {
            i++;
            this.push(Operators.POW);
          } else {
            this.push(Operators.MUL);
          }
          break;

        case '/':
          this.push(Operators.DIV);
          break;

        case '%':
          this.push(Operators.MOD);
          break;

        case '+':
          this.push(Operators.ADD);
          break;

        case '-':
          this.push(Operators.SUB);
          break;

        case '=':
          if (c1 == '=') {
            i++;
            if (ch(str, i + 1) == '=') {
              i++;
              this.push(Operators.SEQ);
            } else {
              this.push(Operators.EQ);
            }
          } else {
            this.push(Operators.ASN);
          }
          break;

        case '!':
          if (c1 == '=') {
            i++;
            if (ch(str, i + 1) == '=') {
              i++;
              this.push(Operators.SNEQ);
            } else {
              this.push(Operators.NEQ);
            }
          } else {
            this.push(Operators.LNOT);
          }
          break;

        case '<':
          if (c1 == '<') {
            i++;
            this.push(Operators.SHL);
          } else if (c1 == '=') {
            i++;
            this.push(Operators.LTEQ);
          } else {
            this.push(Operators.LT);
          }
          break;

        case '>':
          if (c1 == '>') {
            i++;
            this.push(Operators.SHR);
          } else if (c1 == '=') {
            i++;
            this.push(Operators.GTEQ);
          } else {
            this.push(Operators.GT);
          }
          break;

        case '~':
          this.push(Operators.BNOT);
          break;

        case '&':
          if (c1 == '&') {
            i++;
            this.push(Operators.LAND);
          } else {
            this.push(Operators.BAND);
          }
          break;

        case '|':
          if (c1 == '|') {
            i++;
            this.push(Operators.LOR);
          } else {
            this.push(Operators.BOR);
          }
          break;

        case '^':
          this.push(Operators.BXOR);
          break;

        case ' ':
        case '\n':
        case '\t':
        case '\r':
        case '\u00a0':
          break;

        case ',':
          this.push(Operators.COMMA);
          break;

        case ';':
          this.push(Operators.SEMI);
          break;

        case '(':
          this.push(Operators.LPRN);
          break;

        case ')':
          this.push(Operators.RPRN);
          break;

        default: {
          // Here we handle variable references, and named functions / constants.
          // We use the matchVariable to cover these cases and disambiguate them.
          // Function names are whitelisted and must be immediately followed by an
          // open parenthesis.

          matcher.region(i, len);
          if (matcher.variable()) {
            int end = matcher.matchEnd();
            Object[] raw = GeneralUtils.splitVariable(str.substring(i, end));
            i = end;

            if (raw != null && raw.length == 1 && raw[0] instanceof String) {
              // Names for constants. These names can conflict with references to
              // context variables on the immediate node, e.g. { "PI": "apple" }.
              // To disambiguate, use references of the form "@.PI" or bind
              // local variables before calling the expression.
              String name = (String)raw[0];
              Token value = CONSTANTS.get(name);
              if (value != null) {
                this.push(value);
                continue;
              }

              // Fall through and assume this is a variable reference. If followed
              // immediately by a left parenthesis it may be a function call, but
              // we determine that in the push() method.
            }
            this.push(new VarToken(raw));
            continue;
          }

          // input character we can't handle
          this.errors.add("Unexpected " + charName(c0) + " at " + i + ": " + StringEscapeUtils.escapeJava("" + c0));
          i = -1;
          break;
        }
      }

      if (i == -1) {
        // Error occurred, bail out
        break;
      }
      i++;
    }
  }

  /**
   * Scan a decimal number and push a token, or an error message.
   */
  private int decimal(String str, int i, int len) {
    int j = decimali(str, i, len);
    switch (j) {
      case -2:
        this.errors.add("Expected a digit after exponent in decimal number");
        break;

      case -3:
        this.errors.add("Duplicate decimal point in number");
        break;

      case -4:
        this.errors.add("Unexpected decimal point in exponent");
        break;
    }

    if (j < 0) {
      return -1;
    }

    // no need to consider radix as numbers will always be in decimal
    String text = str.substring(i, j);
    this.push(new NumberToken(Double.parseDouble(text)));
    return j;
  }

  /**
   * Parse a hexadecimal integer number.
   */
  private int hex(String str, int i, int len) {
    int j = hexi(str, i, len);
    if (i == j) {
      this.errors.add("Expected digits after start of hex number");
      return -1;
    }

    String text = str.substring(i, j);
    double value = Conversions.hexnum(text);
    this.push(new NumberToken(value));
    return j;
  }

  /**
   * Parse a string literal.
   */
  private int string(String str, int i, int len, char end) {
    // Accumulate decoded characters
    StringBuilder s = new StringBuilder();

    int j;

    while (i < len) {
      char c = str.charAt(i);
      j = i + 1;

      // Handle backslash escape sequences
      if (c == '\\' && j < len) {
        c = str.charAt(j);
        switch (c) {
          // Newline
          case 'n':
            s.append('\n');
            break;
          // Tab
          case 't':
            s.append('\t');
            break;
          // Form feed
          case 'f':
            s.append('\f');
            break;
          // Carriage return
          case 'r':
            s.append('\r');
            break;
          // Byte
          case 'x': {
            // Skip over escape
            i += 2;

            // Check if we have enough characters to parse the full escape
            int lim = i + 2;
            if (lim >= len) {
              this.errors.add(E_INVALID_HEX);
              return -1;
            }

            // find end of hex char sequence
            int k = hexi(str, i, lim);
            if (k != lim) {
              this.errors.add(E_INVALID_HEX);
              return -1;
            }

            // Decode range of chars as 2-digit hex number
            int code = Integer.parseInt(str.substring(i, k), 16);

            // Eliminate unwanted ascii control bytes here.
            if (code <= 0x08 || (code >= 0x0e && code < 0x20)) {
              // emit replacement char
              s.append(' ');
            } else {
              // if k === lim above, hex string is valid
              s.append((char)code);
            }

            // Skip over escape sequence and continue
            i = k;
            continue;
          }

          // Unicode character escape 4 or 8 digits '\u0000' or '\U00000000'
          case 'u':
          case 'U': {

            // Skip over escape
            i += 2;

            // a unicode escape can contain 4 or 8 characters.
            int lim = i + (c == 'u' ? 4 : 8);

            // find end of hex char sequence
            int k = hexi(str, i, lim < len ? lim : len);

            // escape sequence end must match limit
            if (k != lim) {
              this.errors.add(E_INVALID_UNICODE);
              return -1;
            }

            // Decode range of chars as 4- or 8-digit hex number. It is possible
            // for an 8-digit hex value to exceed the range of int, so we parse
            // as long and then constrain with a conditional.
            String repr = str.substring(i, k);
            long code = Long.parseLong(repr, 16);

            // Eliminate unwanted ascii control byte here. Also eliminate
            // out of range invalid Unicode characters.
            if (code <= 0x08 || (code >= 0x0e && code < 0x20) || code > 0x10FFFF) {
              // emit replacement char
              s.append(' ');

            } else if (code > 0xFFFF) {
              // convert to a surrogate pair
              code -= 0x10000;
              s.append((char)(((code / 0x400) | 0) + 0xd800));
              s.append((char)((code % 0x400) + 0xdc00));

            } else {
              // append the char directly
              s.append((char)code);
            }

            // Skip over escape sequence and continue
            i = k;
            continue;
          }

          // Literal character
          default:
            s.append(c);
            break;
        }

        // If we're here, the escape was length 2, so skip it
        i += 2;
        continue;
      }

      // If we've found the matching string delimiter, push the string token
      if (c == end) {
        i++;
        this.push(new StringToken(s.toString()));
        return i;
      }

      // Bare line separators aren't allowed in strings
      switch (c) {
        case '\n':
        case '\r':
          this.errors.add("Illegal bare " + charName(c) + " character in string literal");
          return -1;
      }

      // append the character to the output and advance
      s.append(c);
      i++;
    }

    // Matching end delimiter was never found
    this.errors.add("Unterminated string");
    return -1;
  }


  private static final Map<Character, String> CHARS = new HashMap<Character, String>(){{
    put('\b', "backspace");
    put('\f', "form feed");
    put('\n', "line feed");
    put('\r', "carriage return");
    put('\t', "tab");
  }};

  private static final String charName(char c) {
    String r = CHARS.get(c);
    return r != null ? r : c <= 0x001f ? "control character" : "character";
  }

  // Error messages used more than once
  private static final String E_INVALID_HEX = "Invalid 2-char hex escape found";
  private static final String E_INVALID_UNICODE = "Invalid unicode escape found";

}
