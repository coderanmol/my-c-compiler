package MyGCC;

import java.util.*;
import java.util.AbstractMap.*;

public class RegisterManager {
  /**
   * Lists all the registers that currently contain a variable
   **/
  private HashMap<Register, String> usedRegisters;
  //private HashMap<Integer, Integer> usedStackPositions;
  
  public RegisterManager() {
    this.usedRegisters = new HashMap<Register, String>();
    //this.usedStackPositions = new HashMap<Integer, String>();
  }
  
  public boolean isListedVariable(String var) {
    return this.usedRegisters.containsValue(var);
  }
  
  /*public boolean isListedValue(int i) {
    return this.usedStackPositions.containsValue(i);
  }*/
  
  public boolean isRegisterUsed(Register r) {
    return this.usedRegisters.containsKey(r);
  }
  
  /**
   * Returns a Register that has no assigned variable.
   * 'e' for callee-saved, 'r' for caller-saved
   **/
  public Register getFreeRegister(Register.RegisterType type) {
    ArrayList<HashSet<Register>> al = new ArrayList<HashSet<Register>>();
    HashSet<Register> list;
    switch(type) {
      case CALLER_SAVED:
        list = Register.getCallerSaved();
        al.add(list);
        break;
      case CALLEE_SAVED:
        list = Register.getCalleeSaved();
        al.add(list);
        break;
      default:
        list = Register.getCallerSaved();
        al.add(list);
        list = Register.getCalleeSaved();
        al.add(list);
    }
    
    Register r;
    Iterator<Register> iter;
    
    for(HashSet<Register> hsr : al) {
      iter = hsr.iterator();
      
      while(iter.hasNext()) {
        r = iter.next();
        if(!this.usedRegisters.containsKey(r)) {
          return r;
        }
      }
    }
    return freeUselessRegister(type); // No free registers have been found, we should now attempt to free some by pushing to the stack.
  }
  

  /**
   * Returns a pair of Registers, in order to perform more complex tasks
   **/
  public SimpleEntry<Register, Register> getTwoRegisters(Register.RegisterType type) { // TODO Resolve : When two registers are called upon, they will both have the same type ? (yes ?)
    ArrayList<HashSet<Register>> al = new ArrayList<HashSet<Register>>();   
    HashSet<Register> list;
    
    switch(type) {
      case CALLER_SAVED:
        list = Register.getCallerSaved();
        al.add(list);
        break;
      case CALLEE_SAVED:
        list = Register.getCalleeSaved();
        al.add(list);
        break;
    }

    
    Iterator<Register> iter;
    Register r = null;
    Register r1 = null;
    Register r2 = null;
    
    for(HashSet<Register> hsr : al) {
      iter = hsr.iterator();
    
      while(iter.hasNext()) {
        r = iter.next();
        if(!this.usedRegisters.containsKey(r))
          if(r1 == null)
            r1 = r;
          else
            r2 = r;
      }
    
      if(r1 != null && r2 != null)
        return (new SimpleEntry<Register, Register>(r1, r2));
    }


    System.err.println("Assigning two registers failed");
    return null; //TODO attempt to free two registers by pushing some to the stack
  }
  
  /**
   * Adds the variable var to a free register of type "type"
   **/
  public Register addVariableToRegister(String var, Register.RegisterType type) {
    
    if(!isListedVariable(var)) {
      Register r = this.getFreeRegister(type);
      this.usedRegisters.put(r, var);
      return r;
      
    } else {
      Set<Register> regSet = this.usedRegisters.keySet();
      for(Register reg : regSet) {
        if(reg.getType().equals(type)) {
          if(this.usedRegisters.get(reg).equals(var))
            return reg;
        } else {
          freeRegister(reg);
          Register r = this.getFreeRegister(type);
          this.usedRegisters.put(r, var);
          return r;
        }
      }
    }
    System.err.println("Variable was listed, but no corresponding Register was found in usedRegisters");
    return null;
  }
  
  public SimpleEntry<Register, Register> addTwoVariablesToRegisters(String var1, String var2, Register.RegisterType type) {
    Register r1, r2;
    SimpleEntry<Register, Register> se;
    
    boolean b1, b2;
    b1 = !isListedVariable(var1);
    b2 = !isListedVariable(var2);
    if(b1 && b2) {
      se = getTwoRegisters(type);
      r1 = se.getKey();
      this.usedRegisters.put(r1, var1);
      r2 = se.getValue();
      this.usedRegisters.put(r2, var2);
      return se;
    }
    
    r1 = addVariableToRegister(var1, type);
    r2 = addVariableToRegister(var2, type);

    if(r1 != null && r2 != null)
      return new SimpleEntry<Register, Register>(r1, r2);
      
    System.err.println("One or several of the registers were null");
    return null;
  }
  
  public String getRegisterContent(Register reg) {
    if(this.usedRegisters.containsKey(reg))
      return this.usedRegisters.get(reg);
    return null;
  }
  
  /**
   * Removes the specified Register from the list of usedRegisters.
   **/
  public void freeRegister(Register r) {
    if(this.usedRegisters.containsKey(r))
      this.usedRegisters.remove(r);
    else
      System.err.println("The Register did not have any variables set to it.");
  }
  
  /**
   * Removes the Register that is linked to var from the list of usedRegisters.
   **/
  public void freeVariable(String var) {
    Set<Register> regSet = new HashSet<Register>();
    if(this.usedRegisters.containsValue(var))
      regSet = this.usedRegisters.keySet();
      for(Register reg : regSet)
        if(this.usedRegisters.get(reg).equals(var))
          this.usedRegisters.remove(reg);
    else
      System.err.println("The variable was not set to any registers.");
  }
  

  public Register freeUselessRegister(Register.RegisterType type) {
    /* 
     * TODO find a register that can be removed from the list of used registers with the "type" category
     * IMPORTANT to establish the constraints required on freeing registers
     */
    return null;
  }
  
  public static void main(String[] args) {
    RegisterManager rm = new RegisterManager();
    SimpleEntry<Register, Register> se;
    se = rm.addTwoVariablesToRegisters("a", "b", Register.RegisterType.CALLEE_SAVED);
    System.out.println(se.toString());
    se = rm.addTwoVariablesToRegisters("c", "d", Register.RegisterType.CALLEE_SAVED);
    System.out.println(se.toString());
    se = rm.addTwoVariablesToRegisters("a", "c", Register.RegisterType.CALLEE_SAVED);
    System.out.println(se.toString());
    se = rm.addTwoVariablesToRegisters("a", "c", Register.RegisterType.CALLER_SAVED);
    System.out.println(se.toString());
    /*Register r1 = rm.addVariableToRegister("a", 'e');
    Register r2 = rm.addVariableToRegister("b", 'e');
    Register r3 = rm.addVariableToRegister("c", 'e');
    Register r4 = rm.addVariableToRegister("d", 'e');
    Register r5 = rm.addVariableToRegister("b", 'e');
    Register r6 = rm.addVariableToRegister("e", 'e');
    Register r7 = rm.addVariableToRegister("f", 'e');
    Register r8 = rm.addVariableToRegister("g", 'e');
    Register r9 = rm.addVariableToRegister("h", 'e');
    Register r10 = rm.addVariableToRegister("i", 'e');
    System.out.println(r.toString());
    System.out.println("se : " + se.toString());
    System.out.println("r1 : " + r1.toString());
    System.out.println("r2 : " + r2.toString());
    System.out.println("r3 : " + r3.toString());
    System.out.println("r4 : " + r4.toString());
    System.out.println("r5 : " + r5.toString());
    System.out.println("r6 : " + r6.toString());
    System.out.println("r7 : " + r7.toString());
    System.out.println("r8 : " + r8.toString());
    System.out.println("r9 : " + r9.toString());
    System.out.println("r10 : " + r10.toString());*/
  }

}
