/**
 * Copyright (c) 2017 SQUARESPACE, Inc.
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
package com.squarespace.template.plugins.platform.i18n;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.cldr.MessageArg;
import com.squarespace.template.Context;
import com.squarespace.template.GeneralUtils;


/**
 * Argument for MessageFormat allowing a formatter to resolve
 * the argument's value on demand.
 */
public class MsgArg extends MessageArg {

  protected final Object[] name;

  protected Context ctx;

  protected JsonNode node;

  protected JsonNode currencyNode;

  protected boolean castFailed;

  protected String value;

  protected String currencyCode;

  protected BigDecimal number;

  public MsgArg(Object[] name) {
    this.name = name;
  }

  public void reset() {
    this.node = null;
    this.currencyNode = null;
    this.castFailed = false;
    this.value = null;
    this.currencyCode = null;
    this.number = null;
  }

  /**
   * Sets the context used to resolve the argument's value.
   */
  public void setContext(Context ctx) {
    this.ctx = ctx;
  }

  @Override
  public boolean resolve() {
    if (this.node == null) {
      this.node = ctx.resolve(name);
      // Peek to see if this argument is a Money
      JsonNode decimal = node.path("decimalValue");
      if (!decimal.isMissingNode()) {
        this.node = decimal;
        this.currencyNode = node.path("currencyCode");
      }
    }
    return true;
  }

  @Override
  public String currency() {
    if (this.currencyCode == null && this.currencyNode != null) {
      this.currencyCode = this.currencyNode.asText();
    }
    return this.currencyCode;
  }

  @Override
  public String asString() {
    if (this.value == null) {
      this.value = node == null ? "" : node.asText();
    }
    return this.value;
  }

  @Override
  public BigDecimal asBigDecimal() {
    if (!castFailed) {
      this.number = GeneralUtils.nodeToBigDecimal(node);
      if (number == null) {
        castFailed = true;
      }
    }
    return number;
  }

}