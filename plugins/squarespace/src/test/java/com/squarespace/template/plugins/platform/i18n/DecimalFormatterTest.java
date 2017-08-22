/**
 * Copyright (c) 2017 SQUAResPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIes OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.template.plugins.platform.i18n;

import static com.squarespace.cldr.CLDR.Locale.de;
import static com.squarespace.cldr.CLDR.Locale.en_US;
import static com.squarespace.cldr.CLDR.Locale.es;
import static com.squarespace.cldr.CLDR.Locale.fr;
import static org.testng.Assert.fail;

import java.math.BigDecimal;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.node.DecimalNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Context;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.platform.PlatformUnitTestBase;


/**
 * Tests for the decimal formatter.
 *
 * NOTE: incomplete
 */
public class DecimalFormatterTest extends PlatformUnitTestBase {

  private static final DecimalFormatter deCIMAL = new DecimalFormatter();
  private final CodeMaker mk = maker();

  @Test
  public void testArguments() {
    String args = " ";
    run(en_US, "8915.34567", " style:decimal", "8915.346");
    run(en_US, "8915.34567", " style:percent", "891535%");
    run(en_US, "8915.34567", " style:permille", "8915346‰");
    run(en_US, "8915.34567", " style:short", "8.9K");
    run(en_US, "8915.34567", " style:long", "8.9 thousand");

    args = " mode:significant";
    run(en_US, "89.34567", args, "89.346");

    args += " minimumSignificantDigits: 1 maxSig:6";
    run(en_US, "89.34567", args, "89.3457");
  }

  @Test
  public void testRounding() {
    String args = " style:decimal round:truncate minFrac:2";
    run(en_US, "1.2345", args, "1.23");
    run(en_US, "1.5999", args, "1.59");

    args = " style:decimal round:floor minFrac:2";
    run(en_US, "1.2345", args, "1.23");
    run(en_US, "1.5999", args, "1.59");

    args = " style:decimal round:half-even minFrac:2";
    run(en_US, "1.2345", args, "1.23");
    run(en_US, "1.5999", args, "1.60");
  }

  @Test
  public void testStandard() {
    String args = " style:decimal grouping";
    run(en_US, "0", args, "0");
    run(en_US, "1", args, "1");
    run(en_US, "3.59", args, "3.59");
    run(en_US, "1200", args, "1,200");
    run(en_US, "-15789.12", args, "-15,789.12");
    run(en_US, "99999", args, "99,999");
    run(en_US, "-100200300.40", args, "-100,200,300.4");
    run(en_US, "10000000001", args, "10,000,000,001");

    run(es, "0", args, "0");
    run(es, "1", args, "1");
    run(es, "3.59", args, "3,59");
    run(es, "1200", args, "1.200");
    run(es, "-15789.12", args, "-15.789,12");
    run(es, "99999", args, "99.999");
    run(es, "-100200300.40", args, "-100.200.300,4");
    run(es, "10000000001", args, "10.000.000.001");

    args = " style:decimal no-grouping";
    run(en_US, "-15789.12", args, "-15789.12");
    run(en_US, "99999", args, "99999");
    run(en_US, "-100200300.40", args, "-100200300.4");
    run(en_US, "10000000001", args, "10000000001");

    args = " style:decimal minFrac:4";
    run(en_US, "-15789.12", args, "-15789.1200");

    args = " style:decimal minInt:5 no-group";
    run(en_US, "123.45", args, "00123.45");
  }

  @Test
  public void testFractions() {
    String args = " round:truncate maxFrac:3";
    run(en_US, "3.14159", args, "3.141");

    args = " maxFrac:1 round:ceil";
    run(en_US, "3.14159", args, "3.2");
  }

  @Test
  public void testShort() {
    String args = " style:short mode:significant";
    run(en_US, "0", args, "0");
    run(en_US, "1", args, "1");
    run(en_US, "3.59", args, "3.6");
    run(en_US, "1200", args, "1.2K");
    run(en_US, "-15789.12", args, "-15.8K");
    run(en_US, "99999", args, "100K");
    run(en_US, "-100200300.40", args, "-100.2M");
    run(en_US, "10000000001", args, "10B");

    run(de, "0", args, "0");
    run(de, "1", args, "1");
    run(de, "3.59", args, "3,6");
    run(de, "1200", args, "1,2 Tsd.");
    run(de, "-15789.12", args, "-15,8 Tsd.");
    run(de, "99999", args, "100 Tsd.");
    run(de, "-100200300.40", args, "-100,2 Mio.");
    run(de, "10000000001", args, "10 Mrd.");

    args += " minSig:3 maxSig:4";
    run(en_US, "0", args, "0.00");
    run(en_US, "1", args, "1.00");
    run(en_US, "3.59", args, "3.59");
    run(en_US, "1200", args, "1.20K");
    run(en_US, "-15789.12", args, "-15.79K");
    run(en_US, "99999", args, "100K");
    run(en_US, "-100200300.40", args, "-100.2M");
    run(en_US, "10000000001", args, "10.0B");
  }

  @Test
  public void testLong() {
    String args = " style:long mode:significant";
    run(en_US, "0", args, "0");
    run(en_US, "1", args, "1");
    run(en_US, "3.59", args, "3.6");
    run(en_US, "1200", args, "1.2 thousand");
    run(en_US, "2000", args, "2 thousand");
    run(en_US, "-15789.12", args, "-15.8 thousand");
    run(en_US, "99999", args, "100 thousand");
    run(en_US, "-100200300.40", args, "-100.2 million");
    run(en_US, "10000000001", args, "10 billion");

    run(fr, "0", args, "0");
    run(fr, "1", args, "1");
    run(fr, "3.59", args, "3,6");
    run(fr, "1200", args, "1,2 millier");
    run(fr, "2000", args, "2 mille");
    run(fr, "-15789.12", args, "-15,8 mille");
    run(fr, "99999", args, "100 mille");
    run(fr, "-100200300.40", args, "-100,2 millions");
    run(fr, "10000000001", args, "10 milliards");

    args += " minSig:3 maxSig:4";
    run(en_US, "0", args, "0.00");
    run(en_US, "1", args, "1.00");
    run(en_US, "3.59", args, "3.59");
    run(en_US, "1200", args, "1.20 thousand");
    run(en_US, "2000", args, "2.00 thousand");
    run(en_US, "-15789.12", args, "-15.79 thousand");
    run(en_US, "99999", args, "100 thousand");
    run(en_US, "-100200300.40", args, "-100.2 million");
    run(en_US, "10000000001", args, "10.0 billion");

    run(fr, "0", args, "0,00");
    run(fr, "1", args, "1,00");
    run(fr, "3.59", args, "3,59");
    run(fr, "1200", args, "1,20 millier");
    run(fr, "2000", args, "2,00 mille");
    run(fr, "-15789.12", args, "-15,79 mille");
    run(fr, "99999", args, "100 mille");
    run(fr, "-100200300.40", args, "-100,2 millions");
    run(fr, "10000000001", args, "10,0 milliards");

    args = " style:long mode:significant-maxfrac minSig:1 maxFrac:2";
    run(en_US, "3.59", args, "3.59");
    run(en_US, "3.519", args, "3.52");
    run(en_US, "3.5999", args, "3.6");
  }

  private void run(CLDR.Locale locale, String number, String args, String expected) {
    try {
      String json = new DecimalNode(new BigDecimal(number)).toString();
      String actual = format(locale, mk.args(args), json);
      Assert.assertEquals(actual, expected);
    } catch (CodeException e) {
      fail("formatter raised an error", e);
    }
  }

  private static String format(CLDR.Locale locale,  Arguments args, String json) throws CodeException {
    Context ctx = new Context(JsonUtils.decode(json));
    ctx.cldrLocale(locale);
    deCIMAL.validateArgs(args);
    Variables variables = new Variables("@", ctx.node());
    deCIMAL.apply(ctx, args, variables);
    return variables.first().node().asText();
  }

}
