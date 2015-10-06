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
import org.openjdk.jmh.runner.RunnerException;

import com.fasterxml.jackson.databind.JsonNode;
import com.squarespace.template.plugins.CoreFormatters;
import com.squarespace.template.plugins.CorePredicates;


@Fork(1)
@Measurement(iterations = 5, time = 5)
@Warmup(iterations = 10, time = 2)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class FormatterBenchmark {

  @Benchmark
  public void embeddedTemplate(BenchmarkState state) throws CodeException {
    state.executeEmbedded();
  }

  @Benchmark
  public void nativeFormatter(BenchmarkState state) throws CodeException {
    state.executeNative();
  }

  @State(Scope.Benchmark)
  public static class BenchmarkState {

    private Compiler compiler;

    private Instruction invokeEmbedded;

    private Instruction invokeNative;

    private JsonNode jsonNode;

    @Setup
    public void setupCompiler() throws RunnerException {
      try {
        this.compiler = new Compiler(formatterTable(), predicateTable());
        this.invokeEmbedded = compiler.compile("{@|embedded}").code();
        this.invokeNative = compiler.compile("{@|static}").code();
        this.jsonNode = JsonUtils.decode(GeneralUtils.loadResource(FormatterBenchmark.class, "formatter-bench.json"));
      } catch (Exception e) {
        throw new RunnerException("Failed to init benchmark state", e);
      }
    }

    public Context executeEmbedded() throws CodeException {
      return execute(invokeEmbedded);
    }

    public Context executeNative() throws CodeException {
      return execute(invokeNative);
    }

    private Context execute(Instruction template) throws CodeException {
      return compiler.newExecutor().code(template).json(jsonNode).safeExecution(true).execute();
    }

    private static FormatterTable formatterTable() {
      FormatterTable table = new FormatterTable();
      table.register(new CoreFormatters());
      table.register(new BenchmarkSupport());
      return table;
    }

    private static PredicateTable predicateTable() {
      PredicateTable table = new PredicateTable();
      table.register(new CorePredicates());
      return table;
    }

  }

  public static void main(String[] args) throws Exception {
    BenchmarkState state = new BenchmarkState();
    state.setupCompiler();
    int count = Integer.parseInt(args[0]);
    for (int i = 0; i < count; i++) {
      state.executeEmbedded();
//      if (i % 1000 == 0) {
//        System.out.println(i);
//      }
    }
  }

}
