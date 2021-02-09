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

import java.util.List;

import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.BindVarInst;
import com.squarespace.template.Instructions.CommentInst;
import com.squarespace.template.Instructions.CtxVarInst;
import com.squarespace.template.Instructions.EndInst;
import com.squarespace.template.Instructions.EvalInst;
import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.IncludeInst;
import com.squarespace.template.Instructions.InjectInst;
import com.squarespace.template.Instructions.LiteralInst;
import com.squarespace.template.Instructions.MacroInst;
import com.squarespace.template.Instructions.MetaInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.RepeatedInst;
import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.Instructions.SectionInst;
import com.squarespace.template.Instructions.TextInst;
import com.squarespace.template.Instructions.VariableInst;


/**
 * Given an instruction, (recursively) emits the canonical representation.
 */
public class ReprEmitter {

  private ReprEmitter() {
  }

  public static String get(Instruction inst, boolean recurse) {
    StringBuilder buf = new StringBuilder();
    inst.repr(buf, recurse);
    return buf.toString();
  }

  public static String get(Arguments args, boolean includeDelimiter) {
    StringBuilder buf = new StringBuilder();
    emit(args, includeDelimiter, buf);
    return buf.toString();
  }

  public static String get(Variables variables) {
    StringBuilder buf = new StringBuilder();
    emitVariables(variables, buf);
    return buf.toString();
  }

  public static String get(Object[] names) {
    StringBuilder buf = new StringBuilder();
    emitNames(names, buf);
    return buf.toString();
  }

  private static void emitPreprocess(Instruction inst, StringBuilder buf) {
    if (inst.inPreprocessScope()) {
      buf.append('^');
    }
  }

  public static void emit(AlternatesWithInst inst, StringBuilder buf, boolean recurse) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".alternates with}");
    if (recurse) {
      emitBlock(inst, buf, recurse);
    }
  }

  public static void emit(CommentInst inst, StringBuilder buf) {
    boolean preproc = inst.inPreprocessScope() && !inst.isMultiLine();
    buf.append('{');
    if (preproc) {
      buf.append('^');
    }
    buf.append('#');
    if (inst.isMultiLine()) {
      buf.append('#');
    }
    StringView view = inst.getView();
    buf.append(view.data(), view.start(), view.end());
    if (inst.isMultiLine()) {
      buf.append("##");
    }
    buf.append('}');
  }

  public static void emit(EndInst inst, StringBuilder buf) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".end}");
  }

  public static void emit(EvalInst inst, StringBuilder buf) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".eval ");
    buf.append(inst.body());
    buf.append('}');

  }

  public static void emit(Arguments args, StringBuilder buf) {
    emit(args, true, buf);
  }

  public static void emit(Arguments args, boolean includeDelimiter, StringBuilder buf) {
    if (args.isEmpty()) {
      return;
    }
    char delimiter = args.getDelimiter();
    List<String> argList = args.getArgs();
    for (int i = 0, size = argList.size(); i < size; i++) {
      // In some cases we want to get the arguments' representation without the prefixed
      // delimiter. When rendering the template representation we want the prefix.
      if (includeDelimiter || i > 0) {
        buf.append(delimiter);
      }
      buf.append(argList.get(i));
    }
  }

  public static void emit(IfInst inst, StringBuilder buf, boolean recurse) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".if ");
    emitIfExpression(inst, buf);
    buf.append('}');
    if (recurse) {
      emitBlock(inst, buf, recurse);
    }
  }

  public static void emitIfExpression(IfInst inst, StringBuilder buf) {
    List<Object[]> variables = inst.getVariables();
    List<Operator> operators = inst.getOperators();

    // There is always at least one variable.
    emitNames(variables.get(0), buf);
    for (int i = 1, size = variables.size(); i < size; i++) {
      Operator op = operators.get(i - 1);
      if (op == Operator.LOGICAL_AND) {
        buf.append(" && ");
      } else {
        buf.append(" || ");
      }
      emitNames(variables.get(i), buf);
    }
  }

  public static void emit(IfPredicateInst inst, StringBuilder buf, boolean recurse) {
    Predicate predicate = inst.getPredicate();
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".if ");
    buf.append(predicate.identifier());
    emit(inst.getArguments(), buf);
    buf.append('}');
    if (recurse) {
      emitBlock(inst, buf, recurse);
    }
  }

  public static void emit(IncludeInst inst, StringBuilder buf) {
    buf.append("{.include");
    emit(inst.getArguments(), buf);
    buf.append('}');
  }

  public static void emit(InjectInst inst, StringBuilder buf, boolean recurse) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".inject ");
    buf.append(inst.variable());
    buf.append(' ');
    buf.append(inst.filename());
    buf.append('}');
  }

  public static void emit(LiteralInst inst, StringBuilder buf) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".").append(inst.getName()).append('}');
  }

  public static void emit(MetaInst inst, StringBuilder buf) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append('.');
    if (inst.isLeft()) {
      buf.append("meta-left");
    } else {
      buf.append("meta-right");
    }
    buf.append('}');
  }

  public static void emit(MacroInst inst, StringBuilder buf, boolean recurse) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".macro ");
    buf.append(inst.name());
    buf.append('}');
    if (recurse) {
      emitBlock(inst.root(), buf, true);
    }
  }

  public static void emit(PredicateInst inst, StringBuilder buf, boolean recurse) {
    Predicate predicate = inst.getPredicate();
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append('.');
    if (inst.getType() == InstructionType.PREDICATE) {
      buf.append(predicate.identifier());
    } else {
      buf.append("or");
      if (predicate != null) {
        buf.append(' ').append(predicate.identifier());
      }
    }
    emit(inst.getArguments(), buf);
    buf.append('}');
    if (recurse) {
      emitBlock(inst, buf, recurse);
    }
  }

  public static void emit(RepeatedInst inst, StringBuilder buf, boolean recurse) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".repeated section ");
    emitNames(inst.getVariable(), buf);
    buf.append('}');
    if (recurse) {
      // Special case of a block, since we want the "alternates with" block to
      // be emitted between the consequent and alternative.
      emitBlock(inst.getConsequent(), buf, recurse);
      AlternatesWithInst a2 = inst.getAlternatesWith();
      if (a2 != null) {
        a2.repr(buf, recurse);
      }
      Instruction alt = inst.getAlternative();
      if (alt != null) {
        alt.repr(buf, recurse);
      }
    }
  }

  public static void emit(RootInst inst, StringBuilder buf, boolean recurse) {
    if (recurse) {
      emitBlock(inst, buf, recurse);
      Instruction alt = inst.getAlternative();
      if (alt != null) {
        alt.repr(buf, recurse);
      }
    }
  }

  public static void emit(BindVarInst inst, StringBuilder buf) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".var ").append(inst.getName()).append(' ');
    emitVariables(inst.getVariables(), buf);
    emit(inst.getFormatters(), buf);
    buf.append('}');
  }

  public static void emit(CtxVarInst inst, StringBuilder buf) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".ctx ").append(inst.getName()).append(' ');
    List<Binding> bindings = inst.getBindings();
    int size = bindings.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(' ');
      }
      Binding binding = bindings.get(i);
      buf.append(binding.getName()).append('=');
      emitNames(binding.getReference(), buf);
    }
    buf.append('}');
  }

  public static void emit(SectionInst inst, StringBuilder buf, boolean recurse) {
    buf.append('{');
    emitPreprocess(inst, buf);
    buf.append(".section ");
    emitNames(inst.getVariable(), buf);
    buf.append('}');
    if (recurse) {
      emitBlock(inst, buf, recurse);
    }
  }

  public static void emit(TextInst inst, StringBuilder buf) {
    StringView view = inst.getView();
    buf.append(view.data(), view.start(), view.end());
  }

  public static void emit(VariableInst inst, StringBuilder buf) {
    buf.append('{');
    emitPreprocess(inst, buf);
    emitVariables(inst.getVariables(), buf);
    emit(inst.getFormatters(), buf);
    buf.append('}');
  }

  public static void emit(List<FormatterCall> formatters, StringBuilder buf) {
    if (formatters == null) {
      return;
    }
    int size = formatters.size();
    for (int i = 0; i < size; i++) {
      FormatterCall call = formatters.get(i);
      buf.append('|');
      buf.append(call.getFormatter().identifier());
      emit(call.getArguments(), buf);
    }

  }

  public static void emitVariables(Variables variables, StringBuilder buf) {
    int count = variables.count();
    for (int i = 0; i < count; i++) {
      if (i > 0) {
        buf.append(",");
      }
      emitNames(variables.get(i).name(), buf);
    }
  }

  public static void emitNames(Object[] names, StringBuilder buf) {
    if (names == null) {
      buf.append("@");
      return;
    }
    for (int i = 0, len = names.length; i < len; i++) {
      if (i > 0) {
        buf.append('.');
      }
      Object name = names[i];
      if (name instanceof Integer) {
        buf.append((int) name);
      } else {
        buf.append((String) name);
      }
    }
  }

  private static void emitBlock(BlockInstruction inst, StringBuilder buf, boolean recurse) {
    emitBlock(inst.getConsequent(), buf, recurse);
    Instruction alt = inst.getAlternative();
    if (alt != null) {
      alt.repr(buf, recurse);
    }
  }

  private static void emitBlock(Block block, StringBuilder buf, boolean recurse) {
    List<Instruction> instructions = block.getInstructions();
    if (instructions == null) {
      return;
    }
    int size = instructions.size();
    for (int i = 0; i < size; i++) {
      instructions.get(i).repr(buf, recurse);
    }
  }

}
