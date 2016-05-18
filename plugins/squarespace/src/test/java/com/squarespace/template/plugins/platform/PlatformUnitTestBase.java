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


import com.squarespace.template.FormatterTable;
import com.squarespace.template.PredicateTable;
import com.squarespace.template.UnitTestBase;
import com.squarespace.template.plugins.platform.i18n.InternationalFormatters;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class PlatformUnitTestBase extends UnitTestBase {

  @Override
  public FormatterTable formatterTable() {
    FormatterTable table = super.formatterTable();
    table.register(new CommerceFormatters());
    table.register(new ContentFormatters());
    table.register(new SocialFormatters());
    table.register(new InternationalFormatters());
    return table;
  }

  @Override
  public PredicateTable predicateTable() {
    PredicateTable table = super.predicateTable();
    table.register(new CommercePredicates());
    table.register(new ContentPredicates());
    table.register(new SlidePredicates());
    table.register(new SocialPredicates());
    return table;
  }

}
