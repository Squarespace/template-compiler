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


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.CodeMaker;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.Instruction;
import com.squarespace.template.TestSuiteRunner;
import com.squarespace.template.plugins.platform.ContentFormatters.ColorWeightFormatter;
import com.squarespace.template.plugins.platform.ContentFormatters.HeightFormatter;
import com.squarespace.template.plugins.platform.ContentFormatters.ResizedHeightForWidthFormatter;
import com.squarespace.template.plugins.platform.ContentFormatters.ResizedWidthForHeightFormatter;
import com.squarespace.template.plugins.platform.ContentFormatters.SqspThumbForHeightFormatter;
import com.squarespace.template.plugins.platform.ContentFormatters.SqspThumbForWidthFormatter;
import com.squarespace.template.plugins.platform.ContentFormatters.TimesinceFormatter;
import com.squarespace.template.plugins.platform.ContentFormatters.WidthFormatter;


/**
 * Extracted from Commons library at commit ab4ba7a6f2b872a31cb6449ae9e96f5f5b30f471
 */
public class ContentFormattersTest extends PlatformUnitTestBase {

  private static final Formatter COLOR_WEIGHT = new ColorWeightFormatter();

  private static final Formatter HEIGHT = new HeightFormatter();

  private static final Formatter RESIZED_WIDTH_FOR_HEIGHT = new ResizedWidthForHeightFormatter();

  private static final Formatter RESIZED_HEIGHT_FOR_WIDTH = new ResizedHeightForWidthFormatter();

  private static final Formatter SQSP_THUMB_FOR_HEIGHT = new SqspThumbForHeightFormatter();

  private static final Formatter SQSP_THUMB_FOR_WIDTH = new SqspThumbForWidthFormatter();

  private static final Formatter TIMESINCE = new TimesinceFormatter();

  private static final Formatter WIDTH = new WidthFormatter();

  private final TestSuiteRunner runner = new TestSuiteRunner(compiler(), ContentFormattersTest.class);

  @Test
  public void testAbsUrl() throws CodeException {
    String template = "{a|AbsUrl}";
    String json = "{\"base-url\": \"http://foobar.com/foo\", \"a\": \"abc\"}";
    Instruction code = compiler().compile(template).code();
    Context ctx = compiler().newExecutor().code(code).json(json).execute();
    String result = eval(ctx);
    assertEquals(result, "http://foobar.com/foo/abc");
  }

  @Test
  public void testAudioPlayer() {
    runner.run(
      "f-audio-player-1.html"
    );
  }

  @Test
  public void testCapitalize() throws CodeException {
    String template = "{user.name|capitalize}";
    String json = "{\"user\": {\"name\": \"Bob Smith\"}}";
    Instruction code = compiler().compile(template).code();
    Context ctx = compiler().newExecutor().code(code).json(json).execute();
    String result = eval(ctx);
    assertEquals(result, "BOB SMITH");
  }

  @Test
  public void testImage() {
    runner.run(
        "f-image-1.html",
        "f-image-2.html",
        "f-image-3.html",
        "f-image-4.html",
        "f-image-5.html",
        "f-image-6.html"
        );
  }

  @Test
  public void testImageColor() {
    runner.run(
        "f-image-color-1.html",
        "f-image-color-2.html",
        "f-image-color-3.html",
        "f-image-color-4.html",
        "f-image-color-5.html"
        );
  }

  @Test
  public void testImageMeta() {
    runner.run(
        "f-image-meta-1.html",
        "f-image-meta-2.html"
        );
  }

  @Test
  public void testSrcSet() {
    runner.run(
        "f-image-src-set-1.html"
        );
  }

  @Test
  public void testChildImageMeta() {
    runner.run(
        "f-child-image-meta-1.html",
        "f-child-image-meta-2.html",
        "f-child-image-meta-3.html",
        "f-child-image-meta-4.html",
        "f-child-image-meta-5.html",
        "f-child-image-meta-6.html"
        );
  }

  @Test
  public void testCoverImageMeta() {
    runner.run(
        "f-cover-image-meta-1.html"
        );
  }

  @Test
  public void testColorWeight() throws CodeException {
    assertFormatter(COLOR_WEIGHT, "\"#fff\"", "light");
    assertFormatter(COLOR_WEIGHT, "\"#000\"", "dark");
    assertFormatter(COLOR_WEIGHT, "\"ffffff\"", "light");
    assertFormatter(COLOR_WEIGHT, "\"000000\"", "dark");
    assertFormatter(COLOR_WEIGHT, "\"#aaa\"", "light");
    assertFormatter(COLOR_WEIGHT, "\"#444\"", "dark");
    assertFormatter(COLOR_WEIGHT, "\"800000\"", "light");
    assertFormatter(COLOR_WEIGHT, "\"7fffff\"", "dark");
  }

  @Test
  public void testHumanizeDuration() {
    runner.run(
        "f-humanize-duration-1.html"
        );
  }

  @Test
  public void testItemClasses() {
    runner.run(
        "f-item-classes-1.html",
        "f-item-classes-2.html",
        "f-item-classes-3.html"
        );
  }

  @Test
  public void testResize() throws CodeException {
    CodeMaker mk = maker();
    Arguments args = mk.args(" 50");
    String json = "\"100x200\"";
    assertFormatter(RESIZED_HEIGHT_FOR_WIDTH, args, json, "100");
    assertFormatter(RESIZED_WIDTH_FOR_HEIGHT, args, json, "25");

    args = mk.args(" 600");
    json = "\"1200x2400\"";
    assertFormatter(RESIZED_HEIGHT_FOR_WIDTH, args, json, "1200");
    assertFormatter(RESIZED_WIDTH_FOR_HEIGHT, args, json, "300");

    assertInvalidArgs(RESIZED_HEIGHT_FOR_WIDTH, mk.args(""));
    assertInvalidArgs(RESIZED_WIDTH_FOR_HEIGHT, mk.args(""));
  }

  @Test
  public void testSquarespaceThumbnail() throws CodeException {
    CodeMaker mk = maker();
    Arguments args = mk.args(" 50");
    String json = "\"100x200\"";
    assertFormatter(SQSP_THUMB_FOR_WIDTH, args, json, "100w");
    assertFormatter(SQSP_THUMB_FOR_HEIGHT, args, json, "100w");

    args = mk.args(" 600");
    json = "\"1200x2400\"";
    assertFormatter(SQSP_THUMB_FOR_WIDTH, args, json, "750w");
    assertFormatter(SQSP_THUMB_FOR_HEIGHT, args, json, "300w");
  }

  @Test
  public void testTimeSince() throws CodeException {
    String now = Long.toString(System.currentTimeMillis() - (1000 * 15));
    String result = format(TIMESINCE, now);
    assertTrue(result.contains("less than a minute ago"));

    runner.run("f-timesince-1.html");
  }

  @Test
  public void testVideo() {
    runner.run(
        "f-video-1.html",
        "f-video-2.html",
        "f-video-3.html"
        );
  }

  @Test
  public void testWidthHeight() throws CodeException {
    String json = "\"100x200\"";
    assertFormatter(WIDTH, json, "100");
    assertFormatter(HEIGHT, json, "200");
  }

}
