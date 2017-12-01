/**
 * Copyright (c) 2015 SQUARESPACE, Inc.
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

import static com.squarespace.template.ReprEmitter.emitNames;
import static com.squarespace.template.ReprEmitter.emitVariables;

import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.BindVarInst;
import com.squarespace.template.Instructions.CommentInst;
import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.RepeatedInst;
import com.squarespace.template.Instructions.SectionInst;
import com.squarespace.template.Instructions.TextInst;
import com.squarespace.template.Instructions.VariableInst;


/**
 * Emits a string representation of an instruction tree, for debugging
 * purposes.
 */
public class TreeEmitter {

  private static final int INCR = 2;

  private TreeEmitter() {
  }

  public static String get(Instruction inst) {
    StringBuilder buf = new StringBuilder();
    emit(inst, 0, buf);
    return buf.toString();
  }

  public static void emit(Instruction inst, int depth, StringBuilder buf) {
    if (inst == null) {
      return;
    }
    InstructionType type = inst.getType();
    emitHeader(type, inst, depth, buf);
    switch (type) {
      case ALTERNATES_WITH:
      case IF:
      case OR_PREDICATE:
      case PREDICATE:
      case SECTION:
        emitConsequent((BlockInstruction) inst, depth, buf);
        emitAlternative((BlockInstruction) inst, depth, buf);
        break;

      case REPEATED:
        emitConsequent((BlockInstruction) inst, depth, buf);
        emitAlternatesWith((RepeatedInst) inst, depth, buf);
        emitAlternative((BlockInstruction) inst, depth, buf);
        break;

      case ROOT:
        emitBlock(((BlockInstruction) inst).getConsequent(), depth, buf);
        break;

      default:
        break;
    }
  }

  private static void emitHeader(InstructionType type, Instruction inst, int depth, StringBuilder buf) {
    if (type.equals(InstructionType.ROOT)) {
      return;
    }

    indent(depth, buf);
    buf.append(type.toString());
    buf.append(" {").append(inst.getLineNumber()).append(',').append(inst.getCharOffset()).append("}");
    switch (type) {

      case BINDVAR:
        BindVarInst bindvar = (BindVarInst)inst;
        buf.append(' ').append(bindvar.getName()).append(" = ");
        emitVariables(bindvar.getVariables(), buf);
        break;

      case COMMENT:
        CommentInst comment = (CommentInst)inst;
        buf.append(' ');
        emitEscapedString(comment.getView(), buf);
        break;

      case IF:
      {
        if (inst instanceof IfInst) {
          buf.append(' ');
          ReprEmitter.emitIfExpression((IfInst)inst, buf);
        } else {
          IfPredicateInst predicateInst = (IfPredicateInst) inst;
          Predicate predicate = predicateInst.getPredicate();
          if (predicate != null) {
            buf.append(' ').append(predicate);
            Arguments args = predicateInst.getArguments();
            if (!args.isEmpty()) {
              buf.append(' ');
              emitArgs(args, buf);
            }
          }
        }
        break;
      }

      case OR_PREDICATE:
      case PREDICATE:
        PredicateInst predicateInst = (PredicateInst)inst;
        Predicate predicate = predicateInst.getPredicate();
        if (predicate != null) {
          buf.append(' ').append(predicate);
          Arguments args = predicateInst.getArguments();
          if (!args.isEmpty()) {
            buf.append(' ');
            emitArgs(args, buf);
          }
        }
        break;

      case REPEATED:
        RepeatedInst repeated = (RepeatedInst)inst;
        buf.append(' ');
        emitNames(repeated.getVariable(), buf);
        break;

      case SECTION:
        SectionInst section = (SectionInst)inst;
        buf.append(' ');
        emitNames(section.getVariable(), buf);
        break;

      case TEXT:
        TextInst text = (TextInst)inst;
        buf.append(' ');
        emitEscapedString(text.getView(), buf);
        break;

      case VARIABLE:
      {
        VariableInst varInst = (VariableInst)inst;
        Variables variables = varInst.getVariables();
        buf.append(' ');
        ReprEmitter.emitVariables(variables, buf);
        for (FormatterCall formatterCall : varInst.getFormatters()) {
          buf.append('\n');
          indent(depth + INCR, buf);
          buf.append("| ");
          buf.append(formatterCall.getFormatter().identifier());
          Arguments args = formatterCall.getArguments();
          if (!args.isEmpty()) {
            buf.append(' ');
            emitArgs(args, buf);
          }
        }
        break;
      }

      default:
        break;
    }
    buf.append('\n');
  }

  private static void emitArgs(Arguments args, StringBuilder buf) {
    List<String> rawArgs = args.getArgs();
    if (!rawArgs.isEmpty()) {
      buf.append("delim='");
      buf.append(StringEscapeUtils.escapeJava("" + args.getDelimiter()));
      buf.append("' parsed=").append(args.getArgs());
    }
  }

  private static void emitEscapedString(StringView view, StringBuilder buf) {
    String raw = view.toString();
    int length = raw.length();
    int maxLen = Math.min(40, length);
    buf.append("(len=").append(length).append(") ");
    buf.append('"').append(StringEscapeUtils.escapeJava(raw.substring(0, maxLen)));
    if (maxLen != raw.length()) {
      buf.append(" ...");
    }
    buf.append('"');
  }

  private static void emitConsequent(BlockInstruction inst, int depth, StringBuilder buf) {
    Block block = inst.getConsequent();
    if (block != null) {
      emitBlock(block, depth + INCR, buf);
    }
  }

  private static void emitAlternatesWith(RepeatedInst inst, int depth, StringBuilder buf) {
    AlternatesWithInst alternatesWith = inst.getAlternatesWith();
    if (alternatesWith != null) {
      emit(alternatesWith, depth + INCR, buf);
    }
  }

  private static void emitAlternative(BlockInstruction inst, int depth, StringBuilder buf) {
    Instruction alternative = inst.getAlternative();
    if (alternative != null) {
      emit(alternative, depth, buf);
    }
  }

  private static void emitBlock(Block block, int depth, StringBuilder buf) {
    emit(block.getInstructions(), depth, buf);
  }

  private static void emit(List<Instruction> instructions, int depth, StringBuilder buf) {
    if (instructions == null) {
      return;
    }
    int size = instructions.size();
    for (int i = 0; i < size; i++) {
      emit(instructions.get(i), depth, buf);
    }
  }

  private static void indent(int depth, StringBuilder buf) {
    for (int i = 0; i < depth; i++) {
      buf.append(' ');
    }
  }
}
