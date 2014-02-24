package com.squarespace.template;

import java.util.List;

import com.squarespace.template.Instructions.RepeatedInst;


/**
 * Emits a string representation of an instruction tree, for debugging
 * purposes.
 */
public class TreeEmitter {

  public static String get(Instruction inst) {
    StringBuilder buf = new StringBuilder();
    emit(inst, 0, buf);
    return buf.toString();
  }
  
  public static void emit(Instruction inst, int depth, StringBuilder buf) {
    if (inst == null) {
      indent(depth, buf);
      buf.append("null\n");
      return;
    }
    InstructionType type = inst.getType();
    if (!type.equals(InstructionType.ROOT)) { 
      indent(depth, buf);
      buf.append(type.toString());
      buf.append('\n');
    }

    switch (inst.getType()) {
      case ALTERNATES_WITH:
      case IF:
      case OR_PREDICATE:
      case PREDICATE:
      case SECTION:
        emitConsequent((BlockInstruction) inst, depth + 2, buf);
        emitAlternative((BlockInstruction) inst, depth + 2, buf);
        break;
        
      case REPEATED:
        emitConsequent((BlockInstruction) inst, depth + 2, buf);
        emitAlternatesWith((RepeatedInst) inst, depth + 2, buf);
        emitAlternative((BlockInstruction) inst, depth + 2, buf);
        break;

      case ROOT:
        emitBlock(((BlockInstruction) inst).getConsequent(), depth, buf);

      default:
        break;
    }
  }
  
  private static void emitConsequent(BlockInstruction block, int depth, StringBuilder buf) {
    indent(depth, buf);
    buf.append("L:\n");
    emitBlock(block.getConsequent(), depth + 2, buf);
  }

  private static void emitAlternatesWith(RepeatedInst inst, int depth, StringBuilder buf) {
    indent(depth, buf);
    buf.append("A:\n");
    emitBlock(inst.getAlternatesWith().getConsequent(), depth + 2, buf);
    emit(inst.getAlternatesWith().getAlternative(), depth + 2, buf);
  }
  
  private static void emitAlternative(BlockInstruction block, int depth, StringBuilder buf) {
    indent(depth, buf);
    buf.append("R:\n");
    emit(block.getAlternative(), depth + 2, buf);
  }
  
  private static void emitBlock(Block block, int depth, StringBuilder buf) {
    if (block == null) {
      indent(depth, buf);
      buf.append("null\n");
      return;
    }
    emit(block.getInstructions(), depth, buf);
  }
  
  private static void emit(List<Instruction> instructions, int depth, StringBuilder buf) {
    if (instructions == null) {
      indent(depth, buf);
      buf.append("null\n");
      return;
    }
    for (Instruction inst : instructions) {
      emit(inst, depth, buf);
    }
  }
  
  private static void indent(int depth, StringBuilder buf) {
    for (int i = 0; i < depth; i++) {
      buf.append(' ');
    }
  }
}
