package com.squarespace.template;

import java.util.List;

import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.CommentInst;
import com.squarespace.template.Instructions.EndInst;
import com.squarespace.template.Instructions.IfInst;
import com.squarespace.template.Instructions.IfPredicateInst;
import com.squarespace.template.Instructions.LiteralInst;
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
  
  public static String get(Object[] names) {
    StringBuilder buf = new StringBuilder();
    emitNames(names, buf);
    return buf.toString();
  }
  
  public static void emit(AlternatesWithInst inst, StringBuilder buf, boolean recurse) {
    buf.append("{.alternates with}");
    if (recurse) {
      emitBlock(inst, buf, recurse);
    }
  }
  
  public static void emit(CommentInst inst, StringBuilder buf) {
    buf.append("{#");
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
    buf.append("{.end}");
  }

  public static void emit(Arguments args, StringBuilder buf) {
    emit(args, true, buf);
  }
  
  public static void emit(Arguments args, boolean includeDelimiter, StringBuilder buf) {
    if (args == null || args.isEmpty()) {
      return;
    }
    char delimiter = args.getDelimiter();
    List<String> argList = args.getArgs();
    for (int i = 0; i < argList.size(); i++) {
      // In some cases we want to get the arguments' representation without the prefixed
      // delimiter. When rendering the template representation we want the prefix.
      if (includeDelimiter || i > 0) {
        buf.append(delimiter);
      }
      buf.append(argList.get(i));
    }
  }

  public static void emit(IfInst inst, StringBuilder buf, boolean recurse) {
    buf.append("{.if ");
    List<Object[]> variables = inst.getVariables();
    List<Operator> operators = inst.getOperators();
    
    // There is always at least one variable.
    emitNames(variables.get(0), buf);
    for (int i = 1; i < variables.size(); i++) {
      Operator op = operators.get(i - 1);
      if (op == Operator.LOGICAL_AND) {
        buf.append(" && ");
      } else {
        buf.append(" || ");
      }
      emitNames(variables.get(i), buf);
    }
    buf.append('}');
  }
  
  public static void emit(IfPredicateInst inst, StringBuilder buf, boolean recurse) {
    Predicate predicate = inst.getPredicate();
    buf.append("{.if ");
    buf.append(predicate.getIdentifier());
    emit(inst.getArguments(), buf);
    buf.append('}');
  }
  
  public static void emit(LiteralInst inst, StringBuilder buf) {
    buf.append("{.").append(inst.getName()).append('}');
  }

  public static void emit(MetaInst inst, StringBuilder buf) {
    buf.append("{.");
    if (inst.isLeft()) {
      buf.append("meta-left");
    } else {
      buf.append("meta-right");
    }
    buf.append('}');
  }
  
  public static void emit(PredicateInst inst, StringBuilder buf, boolean recurse) {
    Predicate predicate = inst.getPredicate();
    buf.append("{.");
    if (inst.getType() == InstructionType.PREDICATE) {
      buf.append(predicate.getIdentifier());
    } else {
      buf.append("or");
      if (predicate != null) {
        buf.append(' ').append(predicate.getIdentifier());
      }
    }
    emit(inst.getArguments(), buf);
    buf.append('}');

    if (recurse) {
      emitBlock(inst, buf, recurse);
    }
  }
  
  public static void emit(RepeatedInst inst, StringBuilder buf, boolean recurse) {
    buf.append("{.repeated section ");
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
  
  public static void emit(SectionInst inst, StringBuilder buf, boolean recurse) {
    buf.append("{.section ");
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
    emitNames(inst.getVariable(), buf);
    List<FormatterCall> formatters = inst.getFormatters();
    for (FormatterCall formatter : formatters) {
      buf.append('|');
      buf.append(formatter.getFormatter().getIdentifier());
      emit(formatter.getArguments(), buf);
    }
    buf.append('}');
  }
  
  public static void emitNames(Object[] names, StringBuilder buf) {
    if (names == null) {
      buf.append("@");
      return;
    }
    for (int i = 0; i < names.length; i++) {
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
    for (Instruction inst : instructions) {
      inst.repr(buf, recurse);
    }
  }
  
  
  
}
