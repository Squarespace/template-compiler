package com.squarespace.template;

import static com.squarespace.template.InstructionType.ALTERNATES_WITH;
import static com.squarespace.template.InstructionType.IF;
import static com.squarespace.template.InstructionType.OR_PREDICATE;
import static com.squarespace.template.InstructionType.PREDICATE;
import static com.squarespace.template.InstructionType.ROOT;
import static com.squarespace.template.InstructionType.SECTION;
import static com.squarespace.template.SyntaxErrorType.DEAD_CODE_BLOCK;
import static com.squarespace.template.SyntaxErrorType.EOF_IN_BLOCK;
import static com.squarespace.template.SyntaxErrorType.MISMATCHED_END;
import static com.squarespace.template.SyntaxErrorType.NOT_ALLOWED_AT_ROOT;
import static com.squarespace.template.SyntaxErrorType.NOT_ALLOWED_IN_BLOCK;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;

import com.squarespace.template.Instructions.AlternatesWithInst;
import com.squarespace.template.Instructions.EndInst;
import com.squarespace.template.Instructions.PredicateInst;
import com.squarespace.template.Instructions.RepeatedInst;
import com.squarespace.template.Instructions.RootInst;


/**
 * State machine which accepts instructions and assembles them into a valid tree.
 * This class captures all rules for how instructions can be composed to form a
 * well-formed executable instruction tree.
 * 
 * Each state represents a scope within the template. Each scope consists of
 * one or more instructions which execute if the branching condition is true.
 * 
 * Some block instructions also have a single alternative instruction which is taken
 * if the branching condition is false.
 */
public class CodeMachine implements CodeSink {

  private final Deque<Instruction> stack = new ArrayDeque<>();
  
  private State state;

  private RootInst root;
  
  private List<ErrorInfo> errors;
  
  private boolean validate = false;
  
  private Instruction current;
  
  private int instructionCount;
  
  public CodeMachine() {
    this.root = new RootInst();
    this.current = root;
    this.state = state_ROOT;
  }

  /**
   * Return the root instruction.
   */
  public RootInst getCode() {
    return root;
  }
  
  public List<ErrorInfo> getErrors() {
    return (errors == null) ? Collections.<ErrorInfo>emptyList() : errors;
  }
  
  public void setValidate() {
    this.validate = true;
    if (this.errors == null) {
      this.errors = new ArrayList<>(4);
    }
  }

  /**
   * Once the assembly is complete, verify that we're back at the root node, e.g. all opened
   * scopes have been properly closed. If not, there may be a bug in the state machine. 
   * This is a sanity check to ensure that the state machine always reaches the final, 
   * expected state.
   */
  public void complete() {
    if (validate) {
      // When errors occur in validation mode we will almost certainly see the machine in a bad state. 
      // Any relevant errors will have already been captured, so just return.
      return;
    }
    
    // These should never happen when the machine is driven by the tokenizer, since it always
    // (or should always) feed EOF as the final instruction.  The individual states should handle
    // EOF and raise the appropriate error, if any.
    if (current != root) {
      throw new RuntimeException("Unclosed " + currentInfo() + ": perhaps an EOF was not fed to the machine?"
          + " If not, this represents a bug in the state machine.");
    }
    if (state != state_EOF) {
      throw new RuntimeException("Machine never processed EOF, indicating (a) it was never fed an EOF "
          + "(bad test?) or (b) the state machine has a bug.");
    }
  }
  
  public int getInstructionCount() {
    return instructionCount;
  }
  
  /**
   * Accept one or more instructions and either push them onto the stack or allow
   * the current state to process each one, and conditionally transitions to a new state.
   */
  public void accept(Instruction ... instructions) throws CodeSyntaxException {
    for (Instruction inst : instructions) {
      switch (inst.getType()) {
  
        // These block instructions open a new scope, so they can occur in any state,
        // We handle them here to shorten the switch bodies of the individual states.
        case IF:
        case PREDICATE:
        case REPEATED:
        case SECTION:
          state = pushConsequent(inst);
          break;
          
        default:
          state = state.transition(inst);
      }
      // Count all instructions accepted by the machine.
      instructionCount++;
    }
  }

  /**
   * Push a block instruction onto the stack.
   */
  private State pushConsequent(Instruction inst) {
    addConsequent(inst);
    return push(inst);
  }

  /**
   * Just modify the instruction stack. Exists to support special block instructions
   * like ALTERNATES_WITH. 
   */
  private State push(Instruction inst) {
    stack.push(current);
    current = inst;
    return stateFor(inst);
  }
  
  /**
   * Pop a block instruction off the stack.  The stack should never return null,
   * and this is enforced by the state machine.
   */
  private State pop() throws CodeSyntaxException {
    try {
      current = stack.pop();
    } catch (NoSuchElementException e) {
      throw new RuntimeException("Popped the ROOT instruction off the stack, which should never happen! "
          + "Possible bug in state machine.");
    }
    return stateFor(current);
  }
  
  /**
   * Add the instruction to the consequent block of the current instruction.
   */
  private void addConsequent(Instruction inst) {
    ((BlockInstruction)current).getConsequent().add(inst);
  }
  
  /**
   * Set the instruction as the alternative branch of the current instruction.
   */
  private void setAlternative(Instruction inst) {
    ((BlockInstruction)current).setAlternative(inst);
  }

  /**
   * Populate an ErrorInfo with basic state and return it to be augmented before
   * raising an exception.
   */
  private ErrorInfo error(SyntaxErrorType code, Instruction inst) {
    ErrorInfo info = new ErrorInfo(code);
    info.code(code);
    info.type(inst.getType());
    info.line(inst.getLineNumber());
    info.offset(inst.getCharOffset());
    return info;
  }
  
  /**
   * In validation mode this adds an error to the errors list. Otherwise it will raise
   * an exception that wraps the error.
   */
  private void fail(ErrorInfo info) throws CodeSyntaxException {
    if (validate) {
      errors.add(info);
    } else {
      throw new CodeSyntaxException(info);
    }
  }
  
  /**
   * Returns a string describing (in English) the offset to the current instruction.
   */
  private String currentInfo() {
    StringBuilder buf = new StringBuilder();
    current.repr(buf, false);
    buf.append(" started at line ");
    buf.append(current.getLineNumber());
    buf.append(" char ");
    buf.append(current.getCharOffset());
    return buf.toString();
  }
  
  /**
   * Maps block instruction type to its state, so we don't have to maintain a 
   * separate stack for both instructions and states.
   */
  private State stateFor(Instruction inst) {
    InstructionType type = inst.getType();
    switch (type) {
      
      case ALTERNATES_WITH:
        return state_ALTERNATES_WITH;
      case IF:
        return state_IF;
      case OR_PREDICATE:
        return state_OR_PREDICATE;
      case PREDICATE:
        return state_PREDICATE;
      case REPEATED:
        return state_REPEATED;
      case ROOT:
        return state_ROOT;
      case SECTION:
        return state_SECTION;
        
      default:
        throw new RuntimeException("machine fail: attempt to find state for non-block instruction " + type);
    }
  }
    
  
  // State definitions below
  
  /**
   * ALTERNATES_WITH state. Special block which is executed between each pass over the
   * consequent block for a REPEATED instruction.
   */
  private State state_ALTERNATES_WITH = new State() {
    
    @Override
    public State transition(Instruction inst) throws CodeSyntaxException {
      InstructionType type = inst.getType();
      switch (type) {

        case EOF:
          fail(error(EOF_IN_BLOCK, inst).data(currentInfo()));
          return state_EOF;

        case ALTERNATES_WITH:
          fail(error(NOT_ALLOWED_IN_BLOCK, inst).data(ALTERNATES_WITH));
          break;

        case OR_PREDICATE:
          // Special case where an OR_PREDICATE follows an ALTERNATES_WITH.
          //
          // We need to close off the ALTERNATES_WITH block, add the OR_PREDICATE to
          // the REPEATED as its alternate branch, and switch into the OR_PREDICATE scope.
          setAlternative(new EndInst());
          pop();
          setAlternative(inst);
          current = inst;
          return state_OR_PREDICATE;

        case END:
          // Special case, since an END following an ALTERNATES_WITH actually closes
          // the parent REPEATED instruction.  We need to pop twice.
          pop();
          setAlternative(inst);
          return pop();

        default:
          addConsequent(inst);
      }
      return this;
    }
  };
  
  /**
   * IF state. A conditional block whose branching condition consists of testing
   * if one or more variables is "truthy", and joins these tests with either a 
   * logical AND or OR operator.
   */
  private State state_IF = new State() {

    @Override
    public State transition(Instruction inst) throws CodeSyntaxException {
      InstructionType type = inst.getType();
      switch (type) {
        
        case EOF:
          fail(error(EOF_IN_BLOCK, inst).data(currentInfo()));
          return state_EOF;
          
        case ALTERNATES_WITH:
          fail(error(NOT_ALLOWED_IN_BLOCK, inst).data(IF));
          break;
        
        case END:
          setAlternative(inst);
          return pop();
          
        case OR_PREDICATE:
          setAlternative(inst);
          current = inst;
          return state_OR_PREDICATE;
          
        default:
          addConsequent(inst);
      }
      
      return this;
    }
  };
  
  /**
   * PREDICATE state. A conditional block structure. It has a consequent block
   * which is executed when the branching condition is true, and one alternate which
   * is executed if the branching condition is false.
   */
  private State state_PREDICATE = new State() {
    
    @Override
    public State transition(Instruction inst) throws CodeSyntaxException {
      InstructionType type = inst.getType();
      switch (type) {
        
        case EOF:
          fail(error(EOF_IN_BLOCK, inst).data(currentInfo()));
          return state_EOF;

        case ALTERNATES_WITH:
          fail(error(NOT_ALLOWED_IN_BLOCK, inst).data(PREDICATE));
          break;
          
        case END:
          setAlternative(inst);
          return pop();
          
        case OR_PREDICATE:
          setAlternative(inst);
          current = inst;
          return state_OR_PREDICATE;
          
        default:
          addConsequent(inst);
      }
      
      return this;
    }
  };
  
  /**
   * OR_PREDICATE state. Basically the identical state as the PREDICATE, but performs
   * some additional checking of an empty OR followed by another OR, which will never
   * execute.
   */
  private State state_OR_PREDICATE = new State() {
    
    @Override
    public State transition(Instruction inst) throws CodeSyntaxException {
      InstructionType type = inst.getType();
      switch (type) {
        
        case EOF:
          fail(error(EOF_IN_BLOCK, inst).data(currentInfo()));
          return state_EOF;

        case ALTERNATES_WITH:
          fail(error(NOT_ALLOWED_IN_BLOCK, inst).data(OR_PREDICATE));
          break;
          
        case END:
          setAlternative(inst);
          return pop();
          
        case OR_PREDICATE:
          // Check to ensure that we don't have an {.or} following an {.or} with a null predicate.
          PredicateInst parent = ((PredicateInst)current);
          if (parent.getType() == OR_PREDICATE && parent.getPredicate() == null) {
            fail(error(DEAD_CODE_BLOCK, inst));
            break;
          }
          setAlternative(inst);
          current = inst;
          return state_OR_PREDICATE;

        default:
          addConsequent(inst);
      }
      
      return this;
    }
  };
  
  /**
   * REPEATED state. A block which iterates over an array of elements and executes
   * its consequent block for each element. Interleaves executing its optional 
   * ALTERNATES_WITH block.
   */
  private State state_REPEATED = new State() {
    
    @Override
    public State transition(Instruction inst) throws CodeSyntaxException {
      InstructionType type = inst.getType();
      switch (type) {

        case EOF:
          fail(error(EOF_IN_BLOCK, inst).data(currentInfo()));
          return state_EOF;

        case OR_PREDICATE:
          setAlternative(inst);
          current = inst;
          return state_OR_PREDICATE;
                                
        case ALTERNATES_WITH:
          // Special block that lives only within the repeat instruction
          ((RepeatedInst)current).setAlternatesWith((AlternatesWithInst)inst);
          return push(inst);

        case END:
          setAlternative(inst);
          return pop();

        default:
          addConsequent(inst);
      }
      return this;
    }
  };
  
  /**
   * SECTION state. Represents opening a section scope.
   */
  private State state_SECTION = new State() {
    
    @Override
    public State transition(Instruction inst) throws CodeSyntaxException {
      InstructionType type = inst.getType();
      switch (type) {

        case EOF:
          fail(error(EOF_IN_BLOCK, inst).data(currentInfo()));
          return state_EOF;
          
        case ALTERNATES_WITH:
          fail(error(NOT_ALLOWED_IN_BLOCK, inst).data(SECTION));
          break;
        
        case OR_PREDICATE:
          setAlternative(inst);
          current = inst;
          return state_OR_PREDICATE;
        
        case END:
          setAlternative(inst);
          return pop();
          
        default:
          addConsequent(inst);
      }
      
      return this;
    }
    
  };
  
  /**
   * ROOT state. The outermost state in the machine. Used to ensure that all opened
   * scopes are properly closed, and only valid instructions exist at the top level
   * of the template.  It is also the instruction that is returned after a successful
   * compile, as the start of execution.
   */
  private State state_ROOT = new State() {

    @Override
    public State transition(Instruction inst) throws CodeSyntaxException {
      InstructionType type = inst.getType();
      switch (type) {

        case EOF:
          setAlternative(inst);
          return state_EOF;

        case END:
          fail(error(MISMATCHED_END, inst));
          break;
          
        case ALTERNATES_WITH:
        case OR_PREDICATE:
          fail(error(NOT_ALLOWED_AT_ROOT, inst).data(ROOT));
          break;
          
        default:
          addConsequent(inst);
      }
      
      return this;
    }
    
  };
  
  /**
   * Final state of the machine. Once reached the machine will accept no
   * additional instructions -- doing so will raise an error.  EOF should be the 
   * last thing a parser feeds to the state machine.
   */
  private State state_EOF = new State() {

    @Override
    public State transition(Instruction inst) throws CodeSyntaxException {
      throw new RuntimeException("CodeMachine should never try to transition from the EOF state. "
          + "This is either a bug in the state machine or instructions were fed to the state machine "
          + "after EOF.");
    }
    
  };
  
  /**
   * Represents a machine state for a block instruction.
   */
  interface State {
    
    public State transition(Instruction inst) throws CodeSyntaxException;
  
  }
}
