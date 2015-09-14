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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;


public class InstructionTableTest extends UnitTestBase {

  @Test
  public void testTable() {
    StringViewMap<StringView, InstructionType> table = InstructionTable.getTable();
    assertEquals(table.get(new StringView(".end")), InstructionType.END);
    assertEquals(table.get(new StringView(".var")), InstructionType.BINDVAR);
  }

  @Test
  public void testSymbols() {
    String[] symbols = InstructionTable.getSymbols();
    boolean found1 = false;
    boolean found2 = false;
    boolean found3 = false;
    for (String symbol : symbols) {
      found1 |= symbol.equals(".end");
      found2 |= symbol.equals(".var");
      found3 |= symbol.equals(".repeated section");
    }
    assertTrue(found1 && found2 && found3, "Missing expected instructions");
  }

  @Test
  public void testDumpSymbolTables() {
    PredicateTable predicateTable = predicateTable();
    FormatterTable formatterTable = formatterTable();

    String instructions = InstructionTable.dump();
    String predicates = predicateTable.dump();
    String formatters = formatterTable.dump();

    // Used to tune the symbol table sizes.
    if (DEBUG) {
      System.out.println("\nINSTRUCTION TABLE:");
      System.out.println(instructions);
      System.out.println("\nPREDICATE TABLE:");
      System.out.println(predicates);
      System.out.println("============================\n");
      System.out.println("\nFORMATTER TABLE:");
      System.out.println(formatters);
      System.out.println("============================\n");
    }
  }

}
