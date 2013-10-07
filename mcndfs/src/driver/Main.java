package driver;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Map;
import java.util.HashMap;

import graph.GraphFactory;
import graph.Graph;
import graph.State;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import ndfs.nndfs.Color;

import ndfs.NDFS;
import ndfs.NDFSFactory;
import ndfs.Result;
import ndfs.mcndfs_1_naive.ThreadWorker;

public class Main {

    private static class ArgumentException extends Exception {

        private static final long serialVersionUID = 1L;

        ArgumentException(String message) {
            super(message);
        }
    };

    private static void printUsage() {
        System.out.println("Usage: bin/ndfs <file> <version> <nrWorkers>");
        System.out.println("  where");
        System.out.println("    <file> is a Promela file (.prom)");
        System.out.println("    <version> is one of {seq, multicore}");
    }

    private static void runNDFS(String version, Map<State, Color> colorStore,
            File file) throws FileNotFoundException, ArgumentException {

        Graph graph = GraphFactory.createGraph(file);
        NDFS ndfs;
        ndfs = NDFSFactory.createNNDFS(graph, colorStore);

        long start = System.currentTimeMillis();

        long end;
        try {
            ndfs.ndfs();
            throw new Error("No result returned by " + version);
        } catch (Result r) {
            end = System.currentTimeMillis();
            System.out.println(r.getMessage());
            System.out.printf("%s took %d ms\n", version, end - start);
        }
    }

    // To factor with runNDFS ? problem with Color argument
    private static void runMCNDFS(final String version, final File file, int nrWorkers) throws FileNotFoundException, ArgumentException {

        ExecutorService executor = Executors.newFixedThreadPool(nrWorkers);
        Collection<Callable<String>> solvers = new ArrayList<Callable<String>>();
        CompletionService<String> ecs = new ExecutorCompletionService<String>(executor);
        
        List<Future<String>> futures = new ArrayList<Future<String>>(nrWorkers);
        String result = "no cycle...";
        try {
            
        
        }finally{
            for (Future<String> f : futures)
                f.cancel(true);
        }
        for (int i = 0; i < nrWorkers; i++) {
            Callable<String> callable = new ThreadWorker(file, version);
            Future<String> future = executor.submit(callable);
            list.add(future);
        }

        //Retrieve the result
        for (Future<String> fut : list) {
            try {
                if (fut.get().contains("found") || fut.get().contains("Found")) {
                    executor.shutdownNow();
                    System.out.println(fut.get());
                    break;
                } else if (fut.get().contains("not")) {
                    System.out.println("No cycle found.");
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        System.out.println("All threads are done.");
    }

    private static void dispatch(final File file, String version, int nrWorkers)
            throws ArgumentException, FileNotFoundException {
        switch (version) {
            case "seq": {
                if (nrWorkers != 1) {
                    throw new ArgumentException("seq can only run with 1 worker");
                }
                Map<State, ndfs.nndfs.Color> map = new HashMap<State, ndfs.nndfs.Color>();
                runNDFS("seq", map, file);
                break;
            }
            case "multicore": {
                if (nrWorkers <= 0) {
                    throw new ArgumentException("multicore can only work with at least 1 worker");
                }

                runMCNDFS("multicore", file, nrWorkers);
                break;

            }
            default:
                throw new ArgumentException("Unkown version: " + version);
        }
    }

    public static void main(String[] argv) {
        try {
            if (argv.length != 3) {
                throw new ArgumentException("Wrong number of arguments");
            }
            File file = new File(argv[0]);
            String version = argv[1];
            int nrWorkers = new Integer(argv[2]);

            dispatch(file, version, nrWorkers);

        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            printUsage();
        } catch (NumberFormatException e) {
            System.err.println(e.getMessage());
            printUsage();
        }
    }
}
