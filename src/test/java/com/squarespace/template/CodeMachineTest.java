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

import static com.squarespace.template.SyntaxErrorType.DEAD_CODE_BLOCK;
import static com.squarespace.template.SyntaxErrorType.EOF_IN_BLOCK;
import static com.squarespace.template.SyntaxErrorType.NOT_ALLOWED_IN_BLOCK;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMachine;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.CodeSyntaxException;
import com.squarespace.template.ErrorInfo;
import com.squarespace.template.ErrorType;


@Test( groups={ "unit" })
public class CodeMachineTest extends UnitTestBase {

  @Test
  public void testBasics() throws CodeSyntaxException {
    CodeMaker mk = maker();
    CodeMachine cm = machine();
    cm.accept(mk.text("A"), mk.var("@"), mk.eof());
    cm.complete();
    assertEquals(cm.getInstructionCount(), 3);
  }
  
  @Test
  public void testUnexpected() {
    CodeMaker mk = maker();
    CodeMachine cm = machine();
    try {
      cm.accept(mk.eof(), mk.eof());
      fail("expected RuntimeException");
    } catch (RuntimeException e) {
    
      // We're good.

    } catch (CodeSyntaxException e) {
      fail("did not expect CodeSyntaxException");
    }
  }
  
  @Test
  public void testCodeMachineValidation() throws CodeException {
    assertErrors("{.if a}", EOF_IN_BLOCK);
    assertErrors("{.if a}{.alternates with}", NOT_ALLOWED_IN_BLOCK, EOF_IN_BLOCK);
    assertErrors("{.if a}{.alternates with}{.end}", NOT_ALLOWED_IN_BLOCK);
    
    assertErrors("{.plural?}", EOF_IN_BLOCK);
    assertErrors("{.plural?}{.alternates with}", NOT_ALLOWED_IN_BLOCK, EOF_IN_BLOCK);
    assertErrors("{.plural?}{.alternates with}{.end}", NOT_ALLOWED_IN_BLOCK);
    
    assertErrors("{.plural?}{.or}", EOF_IN_BLOCK);
    assertErrors("{.plural?}{.or}{.or}", DEAD_CODE_BLOCK, EOF_IN_BLOCK);
    assertErrors("{.plural?}{.or singular?}{.or}{.or}", DEAD_CODE_BLOCK, EOF_IN_BLOCK);
    assertErrors("{.plural?}{.or}{.alternates with}{.end}", NOT_ALLOWED_IN_BLOCK);
    
    assertErrors("{.section a}", EOF_IN_BLOCK);
    assertErrors("{.section a}{.alternates with}", NOT_ALLOWED_IN_BLOCK, EOF_IN_BLOCK);
    assertErrors("{.section a}{.alternates with}{.end}", NOT_ALLOWED_IN_BLOCK);
    
    assertErrors("{.repeated section a}", EOF_IN_BLOCK);
    assertErrors("{.repeated section a}{.alternates with}", EOF_IN_BLOCK);
    assertErrors("{.repeated section a}{.alternates with}{.alternates with}", NOT_ALLOWED_IN_BLOCK, EOF_IN_BLOCK);
  }
  
  private void assertErrors(String template, ErrorType ... expected) throws CodeException {
    List<ErrorInfo> errors = validate(template);
    List<ErrorType> actual = errorTypes(errors);
    assertEquals(actual.toArray(), expected);
  }
  
  private List<ErrorInfo> validate(String template) throws CodeException {
    return compiler().validate(template).errors();
  }
  
}
