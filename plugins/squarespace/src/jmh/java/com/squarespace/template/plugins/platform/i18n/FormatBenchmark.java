/**
 * Copyright (c) 2017 Squarespace, Inc.
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

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.RunnerException;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.CodeException;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.JsonUtils;
import com.squarespace.template.StringView;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.CoreFormatters.FormatFormatter;


@Fork(1)
@Measurement(iterations = 5, time = 5)
@Warmup(iterations = 3, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class FormatBenchmark {

  private static final Formatter FORMAT = new FormatFormatter();
  private static final Formatter PLURAL = new MessageFormatter();

  private static final Arguments FORMAT_ARGS = new Arguments(new StringView(" count name"));
  private static final Arguments PLURAL_ARGS = new Arguments(new StringView(" count name"));

  private static final JsonNode JSON1 = JsonUtils.decode("{\"messages\": {"
      + "\"format\": \"There is {0} entry posted to the {1} blog.\","
      + "\"plural\": \"There {0 one{is # entry} other{are # entries}} posted to the {1} blog.\"},"
      + "\"count\": 2, \"name\": \"Apple\"}");

  private static final Variables PLURAL_MESSAGE = new Variables("@", JSON1.path("messages").path("plural"));
  private static final Variables FORMAT_MESSAGE = new Variables("@", JSON1.path("messages").path("format"));

  @Benchmark
  public void format(BenchmarkState state) throws CodeException {
    state.execute(FORMAT, FORMAT_ARGS, FORMAT_MESSAGE);
  }

  @Benchmark
  public void plural(BenchmarkState state) throws CodeException {
    state.execute(PLURAL, PLURAL_ARGS, PLURAL_MESSAGE);
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    @Setup
    public void setup() throws RunnerException, ArgumentsException {
      FORMAT.validateArgs(FORMAT_ARGS);
      PLURAL.validateArgs(PLURAL_ARGS);
    }

    public void execute(Formatter formatter, Arguments args, Variables variables) throws CodeException {
      Context ctx = new Context(JSON1);
      formatter.apply(ctx, args, variables);
    }

  }

  public static void main(String[] args) throws Exception {
    BenchmarkState state = new BenchmarkState();
    state.setup();
    state.execute(FORMAT, FORMAT_ARGS, FORMAT_MESSAGE);
    System.out.println(FORMAT_MESSAGE.first().node());

  }
}
