package com.squarespace.template;


public class CharGroups {

  public static final CharGroup WHITESPACE = new CharGroup() {
    @Override
    public boolean contains(char ch) {
      switch (ch) {
        case ' ':
        case '\f':
        case '\b':
        case '\n':
        case '\t':
        case '\r':
          return true;
      }
      return false;
    }
  };
  
}
