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
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.squarespace.cldr.CLDR;
import com.squarespace.template.Arguments;
import com.squarespace.template.CodeException;
import com.squarespace.template.Context;
import com.squarespace.template.Formatter;
import com.squarespace.template.StringView;
import com.squarespace.template.Variables;


@Fork(1)
@Measurement(iterations = 5, time = 5)
@Warmup(iterations = 3, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class UnitBenchmark {

  private static final Formatter UNIT = new UnitFormatter();
  private static final Arguments ARGS_BYTE = new Arguments(new StringView(" in:byte"));
  private static final Arguments ARGS_LEN = new Arguments(new StringView(" in:inch compact:length"));
  private static final Arguments ARGS_SEQ_LEN1 = new Arguments(new StringView(" in:inch sequence:yard,foot,inch"));
  private static final Arguments ARGS_SEQ_LEN2 = new Arguments(new StringView(" in:inch group sequence:mile,inch"));
  private static final Arguments ARGS_SEQ_LEN3 = new Arguments(new StringView(" in:inch group sequence:mile"));
  private static final JsonNode VAR1 = new LongNode(12345);
  private static final JsonNode VAR2 = new LongNode(12345456);

  @Benchmark
  public void format(BenchmarkState state) throws CodeException {
    state.execute(UNIT, ARGS_BYTE, VAR1);
  }

  @Benchmark
  public void formatCompactLength(BenchmarkState state) throws CodeException {
    state.execute(UNIT, ARGS_LEN, VAR1);
  }

  @Benchmark
  public void formatSequenceLength1(BenchmarkState state) throws CodeException {
    state.execute(UNIT, ARGS_SEQ_LEN1, VAR1);
  }

  @Benchmark
  public void formatSequenceLength2(BenchmarkState state) throws CodeException {
    state.execute(UNIT, ARGS_SEQ_LEN2, VAR2);
  }

  @Benchmark
  public void formatSequenceLength3(BenchmarkState state) throws CodeException {
    state.execute(UNIT, ARGS_SEQ_LEN3, VAR2);
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    private Variables vars = new Variables("@");

    public void execute(Formatter formatter, Arguments args, JsonNode node) throws CodeException {
      formatter.validateArgs(args);
      Context ctx = new Context(node);
      ctx.cldrLocale(CLDR.Locale.en_US);
      vars.resolve(ctx);
      formatter.apply(ctx, args, vars);
    }
  }

  public static void main(String[] args) throws Exception {
    BenchmarkState state = new BenchmarkState();
    state.execute(UNIT, ARGS_BYTE, VAR1);
    System.out.println(state.vars.first().node());

    state.execute(UNIT, ARGS_LEN, VAR1);
    System.out.println(state.vars.first().node());

    state.execute(UNIT, ARGS_SEQ_LEN1, VAR1);
    System.out.println(state.vars.first().node());

    state.execute(UNIT, ARGS_SEQ_LEN2, VAR2);
    System.out.println(state.vars.first().node());

    state.execute(UNIT, ARGS_SEQ_LEN3, VAR2);
    System.out.println(state.vars.first().node());
  }

}
