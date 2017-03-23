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

package com.squarespace.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;
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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import java.util.concurrent.TimeUnit;

@Fork(1)
@Measurement(iterations = 5, time = 5)
@Warmup(iterations = 10, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class CompilerBenchmark {

  @Benchmark
  public void variableInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.compile("{hello}"));
  }

  @Benchmark
  public void dotVariableInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.compile("{hello.world}"));
  }

  @Benchmark
  public void bracketVariableInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.compile("{hello[var]}"));
  }

  @Benchmark
  public void formatterInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.compile("{hello|json}"));
  }

  @Benchmark
  public void sectionInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.compile("{.section foo}{bar}{.end}"));
  }

  @Benchmark
  public void repeatedSectionInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.compile("{.repeated section foos}{bar}{.end}"));
  }

  @Benchmark
  public void ifInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.compile("{.if foo}{bar}{.end}"));
  }

  @Benchmark
  public void simpleTemplate(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.compile("{.section foo}{.if bar}baz{.or}qux{.end}{.end}"));
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    private Compiler compiler;

    @Setup
    public void setupCompiler() throws RunnerException {
      try {
        this.compiler = new Compiler(formatterTable(), predicateTable());
      } catch (Exception e) {
        throw new RunnerException("Failed to init benchmark state", e);
      }
    }

    public CompiledTemplate compile(String template) throws CodeException {
      return compiler.compile(template);
    }

    private static FormatterTable formatterTable() {
      FormatterTable table = new FormatterTable();
      table.register(new CoreFormatters());
      return table;
    }

    private static PredicateTable predicateTable() {
      PredicateTable table = new PredicateTable();
      table.register(new CorePredicates());
      return table;
    }

  }

}
