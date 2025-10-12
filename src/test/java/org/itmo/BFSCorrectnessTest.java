package org.itmo;

import java.util.List;
import java.util.Random;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.Z_Result;

@JCStressTest
@Outcome(id = "true", expect = Expect.ACCEPTABLE, desc = "Все вершины посещены ровно один раз")
@Outcome(id = "false", expect = Expect.FORBIDDEN, desc = "Гонка данных: некоторые вершины посещены несколько раз " +
        "или не посещены вовсе")
@State
public class BFSCorrectnessTest {
    private static final int VERTICES = 1000;
    private static final int EDGES = 5000;
    private static final Integer VISIT_COUNT = 1;

    private Graph graph;

    private void setupGraph() {
        graph = new RandomGraphGenerator().generateGraph(new Random(52), VERTICES, EDGES);
    }

    @Actor
    public void actor() {
        setupGraph();
        graph.parallelBFSWithLocalBatches(0, true);
    }

    @Arbiter
    public void arbiter(Z_Result r) {
        List<Integer> visited = graph.getVisited();
        r.r1 = visited.size() == VERTICES && visited.stream().allMatch(VISIT_COUNT::equals);
    }
}
