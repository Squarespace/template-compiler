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
import java.util.Objects;

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
  public static class AlternatesWithInst extends BlockInst {

    AlternatesWithInst() {
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
  public static class BindVarInst extends BaseInstruction implements Formattable {

    private final String name;
    private final Variables variables;
    private List<FormatterCall> formatters;

    BindVarInst(String key, String variable) {
      this.name = key;
      this.variables = new Variables(variable);
      setFormatters(null);
    }

    public String getName() {
      return name;
    }

    public Variables getVariables() {
      return variables;
    }

    @Override
    public List<FormatterCall> getFormatters() {
      return formatters;
    }

    @Override
    public void setFormatters(List<FormatterCall> formatters) {
      this.formatters = formatters == null ? Collections.<FormatterCall>emptyList() : formatters;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      variables.resolve(ctx);
      applyFormatters(ctx, formatters, variables);
      ctx.setVar(name, variables.first().node());
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof BindVarInst) {
        BindVarInst other = (BindVarInst)obj;
        return name.equals(other.name)
            && Objects.equals(variables, other.variables)
            && Objects.equals(formatters, other.formatters);
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
   * Represents a generic block of instructions (consequent) and an optional
   * alternative block.
   */
  public static abstract class BlockInst extends BaseInstruction implements BlockInstruction {

    /**
     * Consequent block of instructions.
     */
    protected Block consequent;

    /**
     * Alternative instruction.
     */
    protected Instruction alternative;

    /**
     * Constructs a block instruction with the initial capacity for the consequent block.
     */
    BlockInst(int consequentsLen) {
      consequent = new Block(consequentsLen);
    }

    /**
     * Returns the consequent block.
     */
    public Block getConsequent() {
      return consequent;
    }

    /**
     * Set the instruction to execute instead of the consequents.
     *
     * For non-conditional blocks, like section, the alternate is simply the END
     * instruction, indicating a complete parse.
     */
    public void setAlternative(Instruction inst) {
      alternative = inst;
    }

    /**
     * Sets the alternative instruction.
     */
    public Instruction getAlternative() {
      return alternative;
    }

    /**
     * Indicates if the two lists of {@code Object[]} are equal.
     */
    protected boolean variableListEquals(List<Object[]> t1, List<Object[]> t2) {
      if (t1 == null) {
        return (t2 == null);
      }
      if (t2 != null) {
        int sz = t1.size();
        if (sz != t2.size()) {
          return false;
        }
        for (int i = 0; i < sz; i++) {
          if (!Arrays.equals(t1.get(i), t2.get(i))) {
            return false;
          }
        }
        return true;
      }
      return false;
    }

    /**
     * Indicates if the current instruction's consequent and alternative are equal to that
     * of the {@code other) instruction.
     */
    protected boolean blockEquals(BlockInst other) {
      boolean res = (consequent == null) ? (other.consequent == null) : consequent.equals(other.consequent);
      if (!res) {
        return false;
      }
      return (alternative == null) ? (other.alternative == null) : alternative.equals(other.alternative);
    }

    /**
     * Helper equals() method to simplify null checking.
     */
    protected static boolean equals(Instruction i1, Instruction i2) {
      return (i1 == null) ? (i2 == null) : i1.equals(i2);
    }

    /**
     * Helper equals() method to simplify null checking.
     */
    protected static boolean blockEquals(Block b1, Block b2) {
      return (b1 == null) ? (b2 == null) : b1.equals(b2);
    }

  }

  /**
   * Terminal instruction representing a comment. Implementation is a NOOP.
   */
  public static class CommentInst extends BaseInstruction {

    private final StringView view;

    private final boolean multiLine;

    CommentInst(StringView view) {
      this(view, false);
    }

    CommentInst(StringView view, boolean multiLine) {
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
  public static class EndInst extends BaseInstruction {

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
  public static class EofInst extends BaseInstruction {

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
  public static class IfInst extends BlockInst {

    private static final List<Operator> EMPTY_OPS = Arrays.<Operator>asList();

    private final List<Object[]> variables = new ArrayList<>(VARIABLE_LIST_LEN);

    private final List<Operator> operators;

    IfInst(List<String> vars, List<Operator> ops) {
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
   * since it is redundant with a normal predicate call.
   *
   * Example:  {.if foo?}  does the same thing as  {.foo?}
   */
  public static class IfPredicateInst extends BlockInst {

    private final Predicate predicate;

    private final Arguments arguments;

    IfPredicateInst(Predicate predicate, Arguments arguments) {
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
   * Used to inject a JSON object and bind it to a variable. Optional
   * arguments to add this to global scope.
   */
  public static class InjectInst extends BaseInstruction {

    private final String variable;

    private final String filename;

    private final Arguments arguments;

    InjectInst(String variable, String filename, Arguments arguments) {
      this.variable = variable;
      this.filename = filename;
      this.arguments = arguments;
    }

    public String variable() {
      return variable;
    }

    public String filename() {
      return filename;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof InjectInst) {
        InjectInst other = (InjectInst) obj;
        return variable.equals(other.variable)
            && filename.equals(other.filename)
            && arguments.equals(other.arguments);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public InstructionType getType() {
      return InstructionType.INJECT;
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      JsonNode node = ctx.getInjectable(filename);
      ctx.setVar(variable, node);
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf, recurse);
    }
  }

  /**
   * Base class for instructions which emit literal characters directly into the output.
   */
  public static abstract class LiteralInst extends BaseInstruction {

    private final String name;

    private final String value;

    LiteralInst(String name, String value) {
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
   * Represents a named block of template code that can be applied.
   */
  public static class MacroInst extends BaseInstruction implements BlockInstruction {

    private final String name;

    private final RootInst root;

    MacroInst(String name) {
      this.name = name;
      this.root = new RootInst();
    }

    public String name() {
      return name;
    }

    public RootInst root() {
      return root;
    }

    @Override
    public Block getConsequent() {
      return root.getConsequent();
    }

    @Override
    public Instruction getAlternative() {
      return root.getAlternative();
    }

    @Override
    public void setAlternative(Instruction inst) {
      root.setAlternative(inst);
    }

    @Override
    public InstructionType getType() {
      return InstructionType.MACRO;
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof MacroInst)) {
        return false;
      }
      MacroInst other = (MacroInst) obj;
      return name.equals(other.name) && root.equals(other.root);
    }

    @Override
    public int hashCode() {
      return super.hashCode();
    }

    @Override
    public void invoke(Context ctx) throws CodeExecuteException {
      // This registers the macro in the current scope. Macros must be
      // applied to produce output, e.g. {@|apply <macro name>}
      ctx.setMacro(name, root);
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf, recurse);
    }

  }

  /**
   * Represents either a left or right meta character, e.g. '{' or '}'.
   * Made this a runtime determination in case in the future we move
   * to a 2-character meta sequence, like "{{" and "}}".
   */
  public static class MetaInst extends BaseInstruction {

    private final boolean isLeft;

    MetaInst(boolean isLeft) {
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
  public static class NewlineInst extends LiteralInst {

    NewlineInst() {
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
  public static class PredicateInst extends BlockInst {

    private InstructionType type = InstructionType.PREDICATE;

    private final Predicate impl;

    private final Arguments args;

    PredicateInst(Predicate impl, Arguments args) {
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
  public static class RepeatedInst extends BlockInst {

    private final Object[] variable;

    private AlternatesWithInst alternatesWith;

    RepeatedInst(String name) {
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
  public static class RootInst extends BlockInst {

    RootInst() {
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
  public static class SectionInst extends BlockInst {

    private final Object[] variable;

    SectionInst(String name) {
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
  public static class SpaceInst extends LiteralInst {

    SpaceInst() {
      super("space", " ");
    }

    @Override
    public InstructionType getType() {
      return InstructionType.SPACE;
    }

  }

  /** Outputs a literal tab character */
  static class TabInst extends LiteralInst {

    TabInst() {
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
  public static class TextInst extends BaseInstruction {

    private final StringView view;

    TextInst(StringView view) {
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
  public static class VariableInst extends BaseInstruction implements Formattable {

    private final Variables variables;
    private List<FormatterCall> formatters;

    VariableInst(String name) {
      this(name, null);
    }

    VariableInst(String name, List<FormatterCall> formatters) {
      this(new Variables(name), formatters);
    }

    VariableInst(Variables variables, List<FormatterCall> formatters) {
      this.variables = variables;
      setFormatters(formatters);
    }

    public Variables getVariables() {
      return variables;
    }

    @Override
    public List<FormatterCall> getFormatters() {
      return formatters;
    }

    @Override
    public void setFormatters(List<FormatterCall> formatters) {
      this.formatters = formatters == null ? Collections.<FormatterCall>emptyList() : formatters;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof VariableInst) {
        VariableInst other = (VariableInst) obj;
        return Objects.equals(variables, other.variables)
            && Objects.equals(formatters, other.formatters);
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
      int count = variables.count();
      for (int i = 0; i < count; i++) {
        variables.get(i).resolve(ctx);
      }

      Variable first = variables.first();
      ctx.push(first.node());
      applyFormatters(ctx, formatters, variables);

      // Finally, output the result.
      if (!first.missing()) {
        emitJsonNode(ctx.buffer(), first.node());
      }
      ctx.pop();
    }

    @Override
    public void repr(StringBuilder buf, boolean recurse) {
      ReprEmitter.emit(this, buf);
    }

  }

  private static void applyFormatters(Context ctx, List<FormatterCall> formatters, Variables variables)
      throws CodeExecuteException {

    CodeLimiter limiter = ctx.getCodeLimiter();
    int size = formatters.size();
    for (int i = 0; i < size; i++) {
      FormatterCall call = formatters.get(i);
      limiter.check();
      Formatter impl = call.getFormatter();
      impl.apply(ctx, call.getArguments(), variables);
    }
  }

  private static void emitJsonNode(StringBuilder buf, JsonNode node) {
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
      // JavaScript Array.toString() will comma-delimit the elements.
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
