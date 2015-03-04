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

import static com.squarespace.template.GeneralUtils.splitVariable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;


/**
 * Implementations of specific JSONT instructions.
 */
public class Instructions {

  // Reasonable defaults for initial embedded instruction list sizes.

  private static final int VARIABLE_LIST_LEN = 2;

  private static final int ROOT_BLOCK_LEN = 10;

  private static final int CONSEQUENT_BLOCK_LEN = 4;

  private static final int ALTERNATES_BLOCK_LEN = 2;

  /**
   * Special case instruction. Contains a block but never executes an alternate,
   * as it is not conditional.. it only exists to enhance the implementation of
   * the REPEAT instruction.
   */
  static class AlternatesWithInst extends BlockInstruction {

    public AlternatesWithInst() {
      super(ALTERNATES_BLOCK_LEN);
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof AlternatesWithInst) && blockEquals((AlternatesWithInst)obj);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.ALTERNATES_WITH;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      ctx.execute(consequent.getInstructions());
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf, recurse);
    }

  }


  /**
   * Set a local variable's value.
   */
  static class BindVarInst extends BaseInstruction {

    private final String name;

    private final Object[] variable;

    public BindVarInst(String key, String variable) {
      this.name = key;
      this.variable = splitVariable(variable);
    }

    public String getName() {
      return name;
    }

    public Object[] getVariable() {
      return variable;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      JsonNode value = ctx.resolve(variable);
      ctx.setVar(name, value);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof BindVarInst) {
        BindVarInst other = (BindVarInst)obj;
        return name.equals(other.name) && Arrays.equals(variable, other.variable);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.BINDVAR;
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf);
    }

  }


  /**
   * Terminal instruction representing a comment. Implementation is a NOOP.
   */
  static class CommentInst extends BaseInstruction {

    private final StringView view;

    private final boolean multiLine;

    public CommentInst(StringView view) {
      this(view, false);
    }

    public CommentInst(StringView view, boolean multiLine) {
      this.view = view;
      this.multiLine = multiLine;
    }

    public StringView getView() {
      return view;
    }

    public boolean isMultiLine() {
      return multiLine;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof CommentInst) {
        CommentInst other = (CommentInst) obj;
        return multiLine == other.multiLine && view.equals(other.view);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.COMMENT;
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf);
    }

  }

  /**
   * Instruction that closes a block instruction. Implementation is a NOOP.
   */
  static class EndInst extends BaseInstruction {

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof EndInst);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.END;
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf);
    }

  }

  /**
   * Marker instruction indicating the end of the parse. Implementation is a NOOP,
   * and it has no visible representation in the template.
   */
  static class EofInst extends BaseInstruction {

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof EofInst);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.EOF;
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      // NO VISIBLE REPRESENTATION
    }

  }

  /**
   * Conditional block which tests one or more variables for "truthiness" and then
   * joins the boolean values with either an OR or AND operator.
   *
   * Note: this was added to the JavaScript JSON-Template engine by someone at Squarespace
   * before my time, and is not as expressive as it could be, e.g. there is no operator
   * precedence, grouping, etc.  This implementation replicates the behavior of the JS
   * version. - phensley
   */
  static class IfInst extends BlockInstruction {

    private static final List<Operator> EMPTY_OPS = Arrays.<Operator>asList();

    private final List<Object[]> variables = new ArrayList<>(VARIABLE_LIST_LEN);

    private final List<Operator> operators;

    public IfInst(List<String> vars, List<Operator> ops) {
      super(CONSEQUENT_BLOCK_LEN);
      for (String name : vars) {
        Object[] parts = splitVariable(name);
        variables.add(parts);
      }
      this.operators = (ops == null) ? EMPTY_OPS : ops;
    }

    public List<Object[]> getVariables() {
      return variables;
    }

    public List<Operator> getOperators() {
      return operators;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof IfInst)) {
        return false;
      }
      IfInst other = (IfInst) obj;
      return variableListEquals(variables, other.variables)
          && operators.equals(other.operators)
          && blockEquals(other);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.IF;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      // Set initial boolean using truth value of first var.
      boolean result = GeneralUtils.isTruthy(ctx.resolve(variables.get(0)));
      for (int i = 1, size = variables.size(); i < size; i++) {
        Object[] var = variables.get(i);
        Operator op = operators.get(i - 1);
        boolean value = GeneralUtils.isTruthy(ctx.resolve(var));
        result = (op == Operator.LOGICAL_OR) ? (result || value) : (result && value);
        if (op == Operator.LOGICAL_OR) {
          if (result) {
            break;
          }
        } else if (!result) {
          break;
        }
      }

      // Based on the boolean result, take a branch.
      if (result) {
        ctx.execute(consequent.getInstructions());
      } else {
        ctx.execute(alternative);
      }
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf, recurse);
    }

  }

  /**
   * Represents a conditional which tests a predicate.
   *
   * NOTE: I'm not sure why this form of if expression was added to the language,
   * since it is redundant with a normal predicate call:
   *
   *  {.if foo?}  does the same thing as  {.foo?}
   *
   * I've implemented it to preserve compatibility with the JavaScript JSONT
   * code, but it can be removed.
   */
  static class IfPredicateInst extends BlockInstruction {

    private final Predicate predicate;

    private final Arguments arguments;

    public IfPredicateInst(Predicate predicate, Arguments arguments) {
      super(CONSEQUENT_BLOCK_LEN);
      this.predicate = predicate;
      this.arguments = arguments;
    }

    public Predicate getPredicate() {
      return predicate;
    }

    public Arguments getArguments() {
      return arguments;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof IfPredicateInst) {
        IfPredicateInst other = (IfPredicateInst) obj;
        return predicate.equals(other.predicate);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.IF;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      if (predicate.apply(ctx, arguments)) {
        ctx.execute(consequent.getInstructions());
      } else {
        ctx.execute(alternative);
      }
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf, recurse);
    }
  }

  /**
   * Base class for instructions which emit literal characters directly into the output.
   */
  static abstract class LiteralInst extends BaseInstruction {

    private final String name;

    private final String value;

    public LiteralInst(String name, String value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof LiteralInst)) {
        return false;
      }
      LiteralInst other = (LiteralInst) obj;
      return name.equals(other.name) && value.equals(other.value);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public void invoke(Context ctx) {
      ctx.buffer().append(value);
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf);
    }

  }

  /**
   * Represents either a left or right meta character, e.g. '{' or '}'.
   * Made this a runtime determination in case in the future we move
   * to a 2-character meta sequence, like "{{" and "}}".
   */
  static class MetaInst extends BaseInstruction {

    private final boolean isLeft;

    public MetaInst(boolean isLeft) {
      this.isLeft = isLeft;
    }

    public boolean isLeft() {
      return this.isLeft;
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof MetaInst) ? ((MetaInst)obj).isLeft == isLeft : false;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return (isLeft) ? InstructionType.META_LEFT : InstructionType.META_RIGHT;
    }

    @Override
    public void invoke(Context ctx) {
      ctx.buffer().append(isLeft ? ctx.getMetaLeft() : ctx.getMetaRight());
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf);
    }

  }

  /**
   * Emits a newline character to the output.
   */
  static class NewlineInst extends LiteralInst {

    public NewlineInst() {
      super("newline", "\n");
    }

    @Override
    public InstructionType getType() {
      return InstructionType.NEWLINE;
    }

  }

  /**
   * Represents a boolean-valued function.
   */
  static class PredicateInst extends BlockInstruction {

    private InstructionType type = InstructionType.PREDICATE;

    private final Predicate impl;

    private final Arguments args;

    public PredicateInst(Predicate impl, Arguments args) {
      super(CONSEQUENT_BLOCK_LEN);
      this.impl = impl;
      this.args = args;
    }

    public Predicate getPredicate() {
      return impl;
    }

    public Arguments getArguments() {
      return args;
    }

    /**
     * Set the instruction type to OR. This is identical to a normal predicate,
     * but serves as a marker to detect invalid placement of {.or} directives.
     */
    public void setOr() {
      type = InstructionType.OR_PREDICATE;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof PredicateInst)) {
        return false;
      }
      PredicateInst other = (PredicateInst) obj;
      if (impl != other.impl) {
        return false;
      }
      return args.equals(other.args) && blockEquals(other);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return type;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      if (impl != null) {
        // If we have a predicate instance, we execute the consequents only if the
        // predicate evaluates to true. If the predicate evaluates to false, we
        // execute the alternative.
        if (impl.apply(ctx, args)) {
          ctx.execute(consequent.getInstructions());
        } else {
          ctx.execute(alternative);
        }
      } else {
        // Without a predicate we always execute the consequents. This represents
        // the "else" in an if / else chain.
        ctx.execute(consequent.getInstructions());
      }
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf, recurse);
    }

  }

  /**
   * Represents a block of instructions to be executed with context set to each
   * element of an array.
   */
  static class RepeatedInst extends BlockInstruction {

    private final Object[] variable;

    private AlternatesWithInst alternatesWith;

    public RepeatedInst(String name) {
      super(CONSEQUENT_BLOCK_LEN);
      this.variable = splitVariable(name);
    }

    public Object[] getVariable() {
      return variable;
    }

    /**
     * Optional block which is executed once between executions of the consequent block.
     */
    public void setAlternatesWith(AlternatesWithInst inst) {
      alternatesWith = inst;
    }

    public AlternatesWithInst getAlternatesWith() {
      return alternatesWith;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof RepeatedInst)) {
        return false;
      }
      RepeatedInst other = (RepeatedInst) obj;
      if (!Arrays.equals(variable, other.variable)) {
        return false;
      }
      return equals(alternatesWith, other.alternatesWith) && blockEquals(other);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.REPEATED;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      ctx.pushSection(variable);
      if (ctx.initIteration()) {
        // We have an array node and can now iterate.
        int lastIndex = ctx.arraySize() - 1;
        while (ctx.hasNext()) {
          int index = ctx.currentIndex();

          // Push the array element onto the stack to be processed by the consequent.
          ctx.pushNext();

          ctx.execute(consequent.getInstructions());

          // In between each pass, execute the alternatesWith block.
          // Note: We must do this here to ensure any variables created inside the
          // consequent block are available to the alternates-with block.
          if (index < lastIndex) {
            ctx.execute(alternatesWith);
          }

          ctx.pop();

          // Point to next array element.
          ctx.increment();
        }
        ctx.pop();

      } else {
        ctx.pop();
        ctx.execute(alternative);
      }
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf, recurse);
    }

  }

  /**
   * Represents the root instruction for a whole template. A special marker
   * instruction, it helps enforce that all scopes were properly closed before
   * accepting the template as valid.
   *
   * For example, you could create a SECTION at the start of your template and
   * never close it, which would execute fine, but would be considered invalid,
   * since each SECTION *must* have a corresponding END tag.
   */
  static class RootInst extends BlockInstruction {

    public RootInst() {
      super(ROOT_BLOCK_LEN);
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof RootInst) && blockEquals((RootInst)obj);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.ROOT;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      ctx.execute(consequent.getInstructions());
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf, recurse);
    }

  }

  /**
   * Represents a nested scope within the JSON tree.
   */
  static class SectionInst extends BlockInstruction {

    private final Object[] variable;

    public SectionInst(String name) {
      super(CONSEQUENT_BLOCK_LEN);
      this.variable = splitVariable(name);
    }

    public Object[] getVariable() {
      return variable;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof SectionInst)) {
        return false;
      }
      SectionInst other = (SectionInst) obj;
      return Arrays.equals(variable, other.variable) && blockEquals(other);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.SECTION;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      ctx.pushSection(variable);
      JsonNode node = ctx.node();
      if (GeneralUtils.isTruthy(node)) {
        ctx.execute(consequent.getInstructions());
        ctx.pop();
      } else {
        ctx.pop();
        ctx.execute(alternative);
      }
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf, recurse);
    }

  }

  /** Outputs a literal space character */
  static class SpaceInst extends LiteralInst {

    public SpaceInst() {
      super("space", " ");
    }

    @Override
    public InstructionType getType() {
      return InstructionType.SPACE;
    }

  }

  /** Outputs a literal tab character */
  static class TabInst extends LiteralInst {

    public TabInst() {
      super("tab", "\t");
    }

    @Override
    public InstructionType getType() {
      return InstructionType.TAB;
    }

  }

  /**
   * Represents a range of characters to be copied directly into the output buffer.
   */
  static class TextInst extends BaseInstruction {

    private final StringView view;

    public TextInst(StringView view) {
      this.view = view;
    }

    public StringView getView() {
      return view;
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof TextInst) && view.equals(((TextInst)obj).view);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.TEXT;
    }

    @Override
    public void invoke(Context ctx) {
      ctx.buffer().append(view.data(), view.start(), view.end());
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf);
    }
  }

  /**
   * Represents the value of a JSON node, with an optional list of formatters,
   * For example, "{name|foo|bar}" will do the following:
   *
   * First the "foo" formatter will modify the current stack frame's node and
   * replace it with a new value.  Then "bar" will do the same.  Once all the
   * formatters have been applied, the switch statement at the end of the
   * invoke() method will determine which representation to emit for the final
   * value.
   */
  static class VariableInst extends BaseInstruction {

    private final Object[] variable;

    private final List<FormatterCall> formatters;

    public VariableInst(String name) {
      this(name, Collections.<FormatterCall>emptyList());
    }

    public VariableInst(String name, List<FormatterCall> formatters) {
      this.variable = splitVariable(name);
      this.formatters = formatters;
    }

    public Object[] getVariable() {
      return variable;
    }

    public List<FormatterCall> getFormatters() {
      return formatters;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof VariableInst) {
        VariableInst other = (VariableInst) obj;
        return Arrays.equals(variable, other.variable) && formatters.equals(other.formatters);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.VARIABLE;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      ctx.push(variable);

      // Apply any formatters.
      for (FormatterCall formatter : formatters) {
        Formatter impl = formatter.getFormatter();
        impl.apply(ctx, formatter.getArguments());
      }

      // Finally, output the result.
      emitJsonNode(ctx, ctx.node());
      ctx.pop();
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf);
    }

  }


  private static void emitJsonNode(Context ctx, JsonNode node) {
    StringBuilder buf = ctx.buffer();

    if (node.isNumber()) {
      // Formatting of numbers depending on type
      switch (node.numberType()) {
        case BIG_INTEGER:
          buf.append(((BigIntegerNode)node).bigIntegerValue().toString());
          break;

        case BIG_DECIMAL:
          buf.append(((DecimalNode)node).decimalValue().toPlainString());
          break;

        case INT:
        case LONG:
          buf.append(node.asLong());
          break;

        case FLOAT:
        case DOUBLE:
          double val = node.asDouble();
          buf.append(Double.toString(val));
          break;

        default:
          break;
      }

    } else if (node.isArray()) {
      // Javascript Array.toString() will comma-delimit the elements.
      for (int i = 0, size = node.size(); i < size; i++) {
        if (i >= 1) {
          buf.append(",");
        }
        buf.append(node.path(i).asText());
      }

    } else if (!node.isNull() && !node.isMissingNode()) {
      buf.append(node.asText());
    }
  }

}
