package org.itmo;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
public class BFSBenchmark {
    private static final int EDGES_PER_VERTEX = 5;
    private final Random random = new Random(42);

    @Param({"50000", "100000", "1000000"})
    public int size;

    @Param({"2", "4", "8", "12", "16", "24"})
    public int workers;

    @Param({"fixed", "caching", "workStealing", "virtual"})
    public String pool;

    private Graph graph;

    @Setup(Level.Iteration)
    public void setup() {
        int edges = size * EDGES_PER_VERTEX;
        graph = new RandomGraphGenerator().generateGraph(random, size, edges);
    }

    @Benchmark
    public void benchmarkParallelBfs() {
        ExecutorService es = switch (pool) {
            case "virtual" -> Executors.newVirtualThreadPerTaskExecutor();
            case "caching" -> Executors.newCachedThreadPool();
            case "workStealing" -> Executors.newWorkStealingPool(workers);
            default -> Executors.newFixedThreadPool(workers);
        };

        try {
            graph.parallelBFSWithLocalBatches(0, es, workers, true);
        } finally {
            es.shutdown();
        }
    }
}


