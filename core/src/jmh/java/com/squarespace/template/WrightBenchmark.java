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
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;

@Fork(1)
@Measurement(iterations = 5, time = 5)
@Warmup(iterations = 10, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class WrightBenchmark {

  @Benchmark
  public void compile(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.compileWright());
  }

  @Benchmark
  public void execute(BenchmarkState state, Blackhole blackhole) throws CodeException {
    blackhole.consume(state.executeWright());
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    private Compiler compiler;
    private String wrightSource;
    private JsonNode wrightJsonNode;
    private ObjectNode wrightPartials;
    private Instruction wrightTemplate;

    @Setup
    public void setupCompiler() throws RunnerException {
      try {
        this.wrightSource = GeneralUtils.loadResource(WrightBenchmark.class, "wright.html");
        String wrightJsonText = GeneralUtils.loadResource(WrightBenchmark.class, "wright.json");
        String wrightPartialsText = GeneralUtils.loadResource(WrightBenchmark.class, "wright-partials.json");
        this.wrightJsonNode = JsonUtils.decode(wrightJsonText);
        this.wrightPartials = (ObjectNode)JsonUtils.decode(wrightPartialsText);
        this.compiler = new Compiler(formatterTable(), predicateTable());
        this.wrightTemplate = compiler.compile(wrightSource).code();
      } catch (Exception e) {
        throw new RunnerException("Failed to init benchmark state", e);
      }
    }

    public CompiledTemplate compileWright() throws CodeException {
      return compiler.compile(wrightSource);
    }

    public Context executeWright() throws CodeException {
      return compiler.newExecutor()
          .code(wrightTemplate)
          .json(wrightJsonNode)
          .partialsMap(wrightPartials)
          .safeExecution(true)
          .execute();
    }

    private static FormatterTable formatterTable() {
      FormatterTable table = new FormatterTable();
      table.register(new CoreFormatters());
      return table;
    }

    private static PredicateTable predicateTable() {
      PredicateTable table = new PredicateTable();
      table.register(new CorePredicates());
      table.register(new WrightPredicates());
      return table;
    }

    public static class WrightPredicates implements PredicateRegistry {
      @Override
      public void registerPredicates(SymbolTable<StringView, Predicate> table) {
        table.add(new VideoPredicate());
      }

      public static class VideoPredicate extends BasePredicate {
        public VideoPredicate() {
          super("background-source-video?", false);
        }

        @Override
        public boolean apply(Context ctx, Arguments args) throws CodeExecuteException {
          return true;
        }
      }
    }

  }

  public static void main(String[] args) throws Exception {
    BenchmarkState state = new BenchmarkState();
    state.setupCompiler();
    CompiledTemplate template = state.compileWright();
    System.out.println(template.errors());
    System.out.println(ReprEmitter.get(template.code(), true));
  }

}
