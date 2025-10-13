package org.itmo;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BFSSerialBenchmark {
    private static final int EDGES_PER_VERTEX = 5;
    private final Random random = new Random(42);

    @Param({"50000", "100000", "1000000"})
    public int size;

    private Graph graph;

    @Setup(Level.Iteration)
    public void setup() {
        int edges = size * EDGES_PER_VERTEX;
        graph = new RandomGraphGenerator().generateGraph(random, size, edges);
    }

    @Benchmark
    public void benchmarkSerialBfs() {
        graph.bfs(0);
    }
}
