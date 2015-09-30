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

package com.squarespace.template.plugins.platform;

import static com.squarespace.template.GeneralUtils.isTruthy;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.BasePredicate;
import com.squarespace.template.CodeExecuteException;
import com.squarespace.template.Context;
import com.squarespace.template.Predicate;
import com.squarespace.template.PredicateRegistry;
import com.squarespace.template.StringView;
import com.squarespace.template.SymbolTable;


/**
 * Extracted from Commons library at commit ed6b7ee3b23839afe998a23544dd6b2188b60fca
 */
public class SocialPredicates implements PredicateRegistry {

  @Override
  public void registerPredicates(SymbolTable<StringView, Predicate> table) {
    table.add(COMMENTS);
    table.add(DISQUS);
  }

  public static final Predicate COMMENTS = new BasePredicate("comments?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode settings = ctx.resolve("websiteSettings");
      JsonNode node = ctx.node();
      boolean commentsOn = node.path("commentState").asInt() == 1;
      if (!commentsOn && node.path("publicCommentCount").asInt() > 0) {
        commentsOn = true;
      }
      if (!settings.isMissingNode() && !isTruthy(settings.path("commentsEnabled"))) {
        commentsOn = false;
      }
      return commentsOn;
    }
  };

  public static final Predicate DISQUS = new BasePredicate("disqus?", false) {
    @Override
    public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
      JsonNode settings = ctx.resolve("websiteSettings");
      return isTruthy(settings.path("disqusShortName"));
    }
  };

}
