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
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.squarespace.template.Arguments;
import com.squarespace.template.ArgumentsException;
import com.squarespace.template.CodeException;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.StringView;
import com.squarespace.template.Variables;
import com.squarespace.template.plugins.CoreFormatters.DateFormatter;


@Fork(1)
@Measurement(iterations = 5, time = 5)
@Warmup(iterations = 3, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class DateBenchmark {

  private static final Formatter DATE = new DateFormatter();

  private static final Arguments DATE_ARGS = new Arguments(new StringView(" %c"));

  private static final Formatter DATETIME = new DateTimeFormatter();

  private static final Arguments DATETIME_ARGS = new Arguments(new StringView(" date-full time-full"));

  private static final Variables VARIABLES = new Variables("@", new LongNode(1));

  private static final JsonNode JSON = new TextNode("");

  @Benchmark
  public void genericDate(BenchmarkState state) throws CodeException {
    state.execute(DATE, DATE_ARGS, VARIABLES);
  }

  @Benchmark
  public void internationalDateFull(BenchmarkState state) throws CodeException {
    state.execute(DATETIME, DATETIME_ARGS, VARIABLES);
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    @Setup
    public void setup() throws RunnerException, ArgumentsException {
      DATE.validateArgs(DATE_ARGS);
      DATETIME.validateArgs(DATETIME_ARGS);
    }

    public void execute(Formatter formatter, Arguments args, Variables variables) throws CodeException {
      Context ctx = new Context(JSON);
      formatter.apply(ctx, args, variables);
    }
  }

  public static void main(String[] args) throws Exception {
    BenchmarkState state = new BenchmarkState();
    state.setup();
    state.execute(DATE, DATE_ARGS, VARIABLES);
    System.out.println(VARIABLES.first().node());
    state.execute(DATETIME, DATETIME_ARGS, VARIABLES);
    System.out.println(VARIABLES.first().node());
  }
}
