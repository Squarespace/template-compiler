/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.FormatterTable;
import com.squarespace.template.JsonTemplateEngine;
import com.squarespace.template.PredicateTable;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;


public class JsonTemplateEngineTest {

  private static final FormatterTable FORMATTERS = new FormatterTable();

  private static final PredicateTable PREDICATES = new PredicateTable();

  private static final JsonTemplateEngine COMPILER;
  
  static {

    // Configure static plugins to replace defaults with your values.
    CoreFormatters.DATE.setTimezoneKey(Constants.TIMEZONE_KEY);
    
    FORMATTERS.register(new CoreFormatters());
    // Register additional formatters

    PREDICATES.register(new CorePredicates());
    // Register additional predicates

    COMPILER = new JsonTemplateEngine(FORMATTERS, PREDICATES);
  }

  @Test
  public void testCompile() throws CodeException {
    COMPILER.compile("{.section foo}{@}{.end}");
    
    try {
      COMPILER.compile("{.foo?}");
      Assert.fail("Expected CodeException");
    } catch (CodeException e) { }
  }
  
}
