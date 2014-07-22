package com.squarespace.template;

import static org.testng.Assert.assertTrue;

import java.util.regex.Pattern;

import org.testng.annotations.Test;


public class TreeEmitterTest extends UnitTestBase {

  private static final Pattern SPACES = Pattern.compile("\\s+");

  @Test
  public void testTreeEmitter() throws CodeException {
    String raw = "{.repeated section a}a{.or even? b}b{.end}";
    Instruction inst = compiler().compile(raw).code();
    String result = SPACES.matcher(TreeEmitter.get(inst)).replaceAll(" ");
    assertTrue(result.contains("REPEATED true: TEXT alternates: null false: OR_PREDICATE"));
  }

}
