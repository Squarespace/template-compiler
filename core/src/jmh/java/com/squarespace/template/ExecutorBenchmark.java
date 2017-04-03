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
public class ExecutorBenchmark {

  @Benchmark
  public void variableInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.execute(state.variableInst));
  }

  @Benchmark
  public void dotVariableInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.execute(state.dotVariableInst));
  }

  @Benchmark
  public void formatterInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.execute(state.formatterInst));
  }

  @Benchmark
  public void sectionInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.execute(state.sectionInst));
  }

  @Benchmark
  public void repeatedSectionInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.execute(state.repeatedSectionInst));
  }

  @Benchmark
  public void ifInst(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.execute(state.ifInst));
  }

  @Benchmark
  public void simpleTemplate(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.execute(state.simpleTemplate));
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    private Compiler compiler;

    private JsonNode simpleJsonNode;

    public Instruction variableInst;

    public Instruction dotVariableInst;

    public Instruction formatterInst;

    public Instruction sectionInst;

    public Instruction repeatedSectionInst;

    public Instruction ifInst;

    public Instruction simpleTemplate;

    @Setup
    public void setupCompiler() throws RunnerException {
      try {
        String simpleJsonText = GeneralUtils.loadResource(ExecutorBenchmark.class, "executor-bench.json");
        this.simpleJsonNode = JsonUtils.decode(simpleJsonText);
        this.compiler = new Compiler(formatterTable(), predicateTable());
        this.variableInst = compiler.compile("{hello}").code();
        this.dotVariableInst = compiler.compile("{hello.world}").code();
        this.formatterInst = compiler.compile("{hello|json}").code();
        this.sectionInst = compiler.compile("{.section foo}{bar}{.end}").code();
        this.repeatedSectionInst = compiler.compile("{.repeated section foos}{bar}{.end}").code();
        this.ifInst = compiler.compile("{.if foo}{bar}{.end}").code();
        this.simpleTemplate = compiler.compile("{.section foo}{.if bar}baz{.or}qux{.end}{.end}").code();
      } catch (Exception e) {
        throw new RunnerException("Failed to init benchmark state", e);
      }
    }

    public Context execute(Instruction instruction) throws CodeException {
      return compiler.newExecutor().code(instruction).json(simpleJsonNode).safeExecution(true).execute();
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
