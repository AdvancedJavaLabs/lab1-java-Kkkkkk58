package org.itmo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicIntegerArray;

class Graph {
    private final int V;
    private final ArrayList<Integer>[] adjList;
    private List<Integer> visitedVertices;

    Graph(int vertices) {
        this.V = vertices;
        adjList = new ArrayList[vertices];
        for (int i = 0; i < vertices; ++i) {
            adjList[i] = new ArrayList<>();
        }
    }

    void addEdge(int src, int dest) {
        if (!adjList[src].contains(dest)) {
            adjList[src].add(dest);
        }
    }

    void parallelBFSWithLocalBatches(int startVertex) {
        parallelBFSWithLocalBatches(startVertex, false);
    }

    void parallelBFSWithLocalBatches(int startVertex, boolean saveResult) {
        int nWorkers = Runtime.getRuntime().availableProcessors();
        try (ExecutorService executor = Executors.newFixedThreadPool(nWorkers)) {
            parallelBFSWithLocalBatches(startVertex, executor, nWorkers, saveResult);
        }
    }

    void parallelBFSWithLocalBatches(
            int startVertex,
            ExecutorService executor,
            int nWorkers,
            boolean saveResult
    ) {
        AtomicIntegerArray visited = new AtomicIntegerArray(V);

        List<Integer> currentLevelQueue = new ArrayList<>();

        visited.set(startVertex, 1);
        currentLevelQueue.add(startVertex);

        while (!currentLevelQueue.isEmpty()) {
            int batchSize = Math.max(1, (currentLevelQueue.size() + nWorkers - 1) / nWorkers);
            int workers = Math.min((currentLevelQueue.size() + batchSize - 1) / batchSize, nWorkers);

            CompletableFuture<List<Integer>>[] futures = new CompletableFuture[workers];
            List<Integer> finalCurrentLevelQueue = currentLevelQueue;
            for (int i = 0; i < workers; ++i) {
                int currentId = i;
                CompletableFuture<List<Integer>> future = CompletableFuture.supplyAsync(() -> processBatch(
                        finalCurrentLevelQueue,
                        currentId * batchSize,
                        (currentId + 1) * batchSize,
                        visited
                ), executor);

                futures[i] = future;
            }

            currentLevelQueue = new ArrayList<>();
            CompletableFuture.allOf(futures).join();
            for (CompletableFuture<List<Integer>> future : futures) {
                currentLevelQueue.addAll(future.join());
            }
        }

        if (saveResult) {
            saveVisitedVertices(visited);
        }
    }

    private List<Integer> processBatch(List<Integer> vertices, int start, int end, AtomicIntegerArray visited) {
        List<Integer> nextLevelVertices = new ArrayList<>();

        for (int i = start; i < end && i < vertices.size(); ++i) {
            int vertex = vertices.get(i);
            for (int n : adjList[vertex]) {
                if (visited.compareAndSet(n, 0, 1)) {
                    nextLevelVertices.add(n);
                }
            }
        }

        return nextLevelVertices;
    }

    void parallelBFSWithLocalBatchesBuggy(int startVertex, boolean saveResult) {
        int nWorkers = Runtime.getRuntime().availableProcessors();
        try (ExecutorService executor = Executors.newFixedThreadPool(nWorkers)) {
            parallelBFSWithLocalBatchesBuggy(startVertex, executor, nWorkers, saveResult);
        }
    }

    void parallelBFSWithLocalBatchesBuggy(
            int startVertex,
            ExecutorService executor,
            int nWorkers,
            boolean saveResult
    ) {
        int[] visited = new int[V];

        List<Integer> currentLevelQueue = new ArrayList<>();

        visited[startVertex] = 1;
        currentLevelQueue.add(startVertex);

        while (!currentLevelQueue.isEmpty()) {
            int batchSize = Math.max(1, (currentLevelQueue.size() + nWorkers - 1) / nWorkers);
            int workers = Math.min((currentLevelQueue.size() + batchSize - 1) / batchSize, nWorkers);

            CompletableFuture<List<Integer>>[] futures = new CompletableFuture[workers];
            List<Integer> finalCurrentLevelQueue = currentLevelQueue;
            for (int i = 0; i < workers; ++i) {
                int currentId = i;
                CompletableFuture<List<Integer>> future = CompletableFuture.supplyAsync(() -> processBatch(
                        finalCurrentLevelQueue,
                        currentId * batchSize,
                        (currentId + 1) * batchSize,
                        visited
                ), executor);

                futures[i] = future;
            }

            currentLevelQueue = new ArrayList<>();
            CompletableFuture.allOf(futures).join();
            for (CompletableFuture<List<Integer>> future : futures) {
                currentLevelQueue.addAll(future.join());
            }
        }

        if (saveResult) {
            saveVisitedVertices(visited);
        }
    }

    private List<Integer> processBatch(List<Integer> vertices, int start, int end, int[] visited) {
        List<Integer> nextLevelVertices = new ArrayList<>();

        for (int i = start; i < end && i < vertices.size(); ++i) {
            int vertex = vertices.get(i);
            for (int n : adjList[vertex]) {
                if (visited[n] == 0) {
                    visited[n] += 1;
                    nextLevelVertices.add(n);
                }
            }
        }

        return nextLevelVertices;
    }

    //Generated by ChatGPT
    void bfs(int startVertex) {
        boolean[] visited = new boolean[V];

        LinkedList<Integer> queue = new LinkedList<>();

        visited[startVertex] = true;
        queue.add(startVertex);

        while (!queue.isEmpty()) {
            startVertex = queue.poll();

            for (int n : adjList[startVertex]) {
                if (!visited[n]) {
                    visited[n] = true;
                    queue.add(n);
                }
            }
        }
    }

    private void saveVisitedVertices(AtomicIntegerArray visited) {
        visitedVertices = new ArrayList<>(V);
        for (int i = 0; i < V; ++i) {
            visitedVertices.add(visited.get(i));
        }
    }

    private void saveVisitedVertices(int[] visited) {
        visitedVertices = Arrays.stream(visited).boxed().toList();
    }

    List<Integer> getVisited() {
        return Objects.requireNonNull(visitedVertices, "Rerun bfs with result saving enabled!");
    }
}
