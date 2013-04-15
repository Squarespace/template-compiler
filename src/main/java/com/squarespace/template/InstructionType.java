package com.squarespace.template;


public enum InstructionType {

  ALTERNATES_WITH("Alternates With"),
  COMMENT("Comment"),
  END("End"),
  EOF("EOF"),
  FORMATTER("Formatter"),
  IF("If"),
  META_LEFT("Meta Left"),
  META_RIGHT("Meta Right"),
  NEWLINE("Newline"),
  OR_PREDICATE("Or Predicate"),
  PREDICATE("Predicate"),
  REPEATED("Repeat"),
  ROOT("Root"),
  SECTION("Section"),
  SPACE("Space"),
  TAB("Tab"),
  TEXT("Text"),
  VARIABLE("Variable")
  ;
  
  private String name;
  
  private InstructionType(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }

}
