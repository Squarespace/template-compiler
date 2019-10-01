/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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

import java.util.EnumMap;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.BindVarInst;
import com.squarespace.template.Instructions.BlockInst;
import com.squarespace.template.Instructions.CommentInst;
import com.squarespace.template.Instructions.CtxVarInst;
import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.InjectInst;
import com.squarespace.template.Instructions.MacroInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.RepeatedInst;
import com.squarespace.template.Instructions.RootInst;
import com.squarespace.template.Instructions.SectionInst;
import com.squarespace.template.Instructions.TextInst;
import com.squarespace.template.Instructions.VariableInst;


/**
 * Emits a compact abstract syntax tree describing a compiled template, using
 * S-expressions encoded in JSON.
 */
public class AstEmitter {

  // Syntax tree version indicate by single integer on root node.
  private static final JsonNode VERSION = new IntNode(1);

  // Instead of "null" or "[]" we append a single zero digit. Empty arrays
  // occur frequently, so we save a bit of space here. We use this in places
  // where it is highly-likely to occur.
  private static final JsonNode FAST_NULL = new IntNode(0);

  // No-operator opcode should never occur, but adding for safety.
  private static final IntNode NOOP = opcode(-1);

  private AstEmitter() {
  }

  public static JsonNode get(Instruction inst) {
    return emit(inst);
  }

  private static JsonNode emit(Instruction raw) {
    InstructionType type = raw.getType();
    switch (type) {
      case TEXT:
      {
        ArrayNode obj = composite(raw);
        TextInst inst = (TextInst) raw;
        obj.add(inst.getView().toString());
        return obj;
      }

      case VARIABLE:
      {
        ArrayNode obj = composite(raw);
        VariableInst inst = (VariableInst) raw;
        obj.add(variables(inst.getVariables()));
        obj.add(formatters(inst.getFormatters()));
        return obj;
      }

      case SECTION:
      {
        ArrayNode obj = composite(raw);
        SectionInst inst = (SectionInst) raw;
        obj.add(variable(inst.getVariable()));
        emit(obj, inst);
        return obj;
      }

      case REPEATED:
      {
        ArrayNode obj = composite(raw);
        RepeatedInst inst = (RepeatedInst) raw;
        obj.add(variable(inst.getVariable()));
        emit(obj, inst);
        AlternatesWithInst alt = inst.getAlternatesWith();
        if (alt == null) {
          obj.add(FAST_NULL);
        } else {
          obj.add(block(alt.consequent));
        }
        return obj;
      }

      case PREDICATE:
      case OR_PREDICATE:
      {
        ArrayNode obj = composite(raw);
        PredicateInst inst = (PredicateInst) raw;
        Predicate predicate = inst.getPredicate();
        if (predicate == null) {
          obj.add(FAST_NULL);
        } else {
          obj.add(predicate.identifier());
        }
        obj.add(arguments(inst.getArguments()));
        emit(obj, inst);
        return obj;
      }

      case BINDVAR:
      {
        ArrayNode obj = composite(raw);
        BindVarInst inst = (BindVarInst) raw;
        obj.add(inst.getName());
        obj.add(variables(inst.getVariables()));
        obj.add(formatters(inst.getFormatters()));
        return obj;
      }

      case CTXVAR:
      {
        ArrayNode obj = composite(raw);
        CtxVarInst inst = (CtxVarInst) raw;
        obj.add(inst.getName());
        obj.add(bindings(inst.getBindings()));
        return obj;
      }

      case ALTERNATES_WITH:
      {
        // Inlined as part of REPEATED above.
        return NOOP;
      }

      case IF:
      {
        if (raw instanceof IfInst) {
          ArrayNode obj = composite(raw);
          IfInst inst = (IfInst) raw;
          obj.add(operators(inst.getOperators()));
          obj.add(variables(inst.getVariables()));
          emit(obj, inst);
          return obj;

        } else {
          // This form of IF is equivalent to a predicate, so just add a
          // predicate to the tree.
          ArrayNode obj = JsonUtils.createArrayNode();
          obj.add(type(InstructionType.PREDICATE));
          IfPredicateInst inst = (IfPredicateInst) raw;
          Predicate predicate = inst.getPredicate();
          if (predicate == null) {
            obj.add(FAST_NULL);
          } else {
            obj.add(predicate.identifier());
          }
          obj.add(arguments(inst.getArguments()));
          emit(obj, inst);
          return obj;
        }
      }

      case INJECT:
      {
        ArrayNode obj = composite(raw);
        InjectInst inst = (InjectInst) raw;
        obj.add(inst.variable());
        obj.add(inst.filename());
        obj.add(arguments(inst.arguments()));
        return obj;
      }

      case MACRO:
      {
        ArrayNode obj = composite(raw);
        MacroInst inst = (MacroInst) raw;
        obj.add(inst.name());
        obj.add(block(inst.getConsequent()));
        return obj;
      }

      case COMMENT:
      {
        ArrayNode obj = composite(raw);
        CommentInst inst = (CommentInst) raw;
        obj.add(inst.getView().toString());
        obj.add(inst.isMultiLine() ? 1 : 0);
        return obj;
      }

      case ROOT:
      {
        ArrayNode obj = composite(raw);
        obj.add(VERSION);
        emit(obj, (RootInst) raw);
        return obj;
      }

      default:
        // Other instructions are atomic; their type is mapped directly to an opcode.
        return type(raw.getType());
    }
  }

  private static ArrayNode composite(Instruction inst) {
    ArrayNode array = JsonUtils.createArrayNode();
    array.add(type(inst.getType()));
    return array;
  }

  private static IntNode type(InstructionType type) {
    return OPCODES.getOrDefault(type, NOOP);
  }

  /**
   * Appends the consequent block and alternative instruction to a JSON array.
   */
  private static void emit(ArrayNode obj, BlockInst inst) {
    obj.add(block(inst.consequent));
    obj.add(emit(inst.alternative));
  }

  /**
   * Encodes a block's instructions into a JSON array.
   */
  private static JsonNode block(Block block) {
    if (block == null) {
      return FAST_NULL;
    }
    List<Instruction> instructions = block.getInstructions();
    if (instructions == null) {
      return FAST_NULL;
    }
    ArrayNode array = JsonUtils.createArrayNode();
    for (Instruction inst : instructions) {
      array.add(emit(inst));
    }
    return array;
  }

  /**
   * Encodes a list of operators as a JSON array. Each operator is a
   * single integer digit, with 0=OR 1=AND.
   */
  private static JsonNode operators(List<Operator> operators) {
    ArrayNode array = JsonUtils.createArrayNode();
    for (Operator operator : operators) {
      array.add(operator == Operator.LOGICAL_AND ? 1 : 0);
    }
    return array;
  }

  /**
   * Encodes a variable name as a string.
   */
  private static ArrayNode variable(Object[] name) {
    ArrayNode result = JsonUtils.createArrayNode();
    if (name == null) {
      result.add("@");

    } else {
      for (Object obj : name) {
        if (obj instanceof Integer) {
          result.add((Integer) obj);
        } else {
          result.add(obj.toString());
        }
      }
    }
    return result;
  }

  /**
   * Encodes an array of bindings as a JSON array.
   */
  private static ArrayNode bindings(List<Binding> bindings) {
    ArrayNode array = JsonUtils.createArrayNode();
    for (Binding b : bindings) {
      array.add(binding(b));
    }
    return array;
  }

  private static ArrayNode binding(Binding binding) {
    ArrayNode array = JsonUtils.createArrayNode();
    array.add(binding.getName());
    array.add(variable(binding.getReference()));
    return array;
  }

  /**
   * Encodes an array of variables as a JSON array.
   */
  private static ArrayNode variables(List<Object[]> variables) {
    ArrayNode array = JsonUtils.createArrayNode();
    for (Object[] name : variables) {
      array.add(variable(name));
    }
    return array;
  }

  /**
   * Encodes a list of variables as a JSON array.
   */
  private static ArrayNode variables(Variables variables) {
    ArrayNode array = JsonUtils.createArrayNode();
    int count = variables.count();
    for (int i = 0; i < count; i++) {
      Object[] name = variables.get(i).name();
      array.add(variable(name));
    }
    return array;
  }

  /**
   * Returns a single zero if the list of formatters is empty.
   *
   * Otherwise it returns an array where each element has one of two forms:
   *
   *  ["<name>", 0]        - a formatter with no arguments.
   *  ["<name>", [..]]  - a formatter with one or more arguments
   */
  private static JsonNode formatters(List<FormatterCall> formatters) {
    if (formatters.isEmpty()) {
      return FAST_NULL;
    }
    ArrayNode array = JsonUtils.createArrayNode();
    for (FormatterCall call : formatters) {
      ArrayNode obj = array.addArray();
      obj.add(call.getFormatter().identifier());

      Arguments args = call.getArguments();
      if (!args.isEmpty()) {
        obj.add(arguments(call.getArguments()));
      }
    }
    return array;
  }

  /**
   * Returns a single zero if the list of arguments is empty.
   * Otherwise it returns an array containing the arguments:
   *
   *   [["<arg1>", ... "<argN>"], "<delimiter>"]
   */
  private static JsonNode arguments(Arguments arguments) {
    int count = arguments.count();
    if (count == 0) {
      return FAST_NULL;
    }
    ArrayNode array = JsonUtils.createArrayNode();
    for (int i = 0; i < count; i++) {
      array.add(arguments.get(i));
    }
    ArrayNode container = JsonUtils.createArrayNode();
    container.add(array);
    container.add(String.valueOf(arguments.getDelimiter()));
    return container;
  }

  /**
   * Mapping from Instruction type to integer opcode.
   *
   * WARNING: These opcode values have been roughly ordered by frequency
   * to minimize the number of opcode digits used for the most common
   * instructions. These values must not be changed.
   */
  private static final EnumMap<InstructionType, IntNode> OPCODES =
      new EnumMap<InstructionType, IntNode>(InstructionType.class) {{
        put(InstructionType.TEXT, opcode(0));
        put(InstructionType.VARIABLE, opcode(1));
        put(InstructionType.SECTION, opcode(2));
        put(InstructionType.END, opcode(3));
        put(InstructionType.REPEATED, opcode(4));
        put(InstructionType.PREDICATE, opcode(5));
        put(InstructionType.BINDVAR, opcode(6));
        put(InstructionType.OR_PREDICATE, opcode(7));
        put(InstructionType.IF, opcode(8));
        put(InstructionType.INJECT, opcode(9));

        put(InstructionType.MACRO, opcode(10));
        put(InstructionType.COMMENT, opcode(11));
        put(InstructionType.META_LEFT, opcode(12));
        put(InstructionType.META_RIGHT, opcode(13));
        put(InstructionType.NEWLINE, opcode(14));
        put(InstructionType.SPACE, opcode(15));
        put(InstructionType.TAB, opcode(16));
        put(InstructionType.ROOT, opcode(17));
        put(InstructionType.EOF, opcode(18));

        // AlternatesWith never appears in this syntax tree, but its type
        // may be referenced in code.
        put(InstructionType.ALTERNATES_WITH, opcode(19));

        // TODO: Struct and Atom are not yet implemented in the server compiler.

        // New Ctxvar instruction
        put(InstructionType.CTXVAR, opcode(22));
      }};

  private static IntNode opcode(int code) {
    return new IntNode(code);
  }
}
