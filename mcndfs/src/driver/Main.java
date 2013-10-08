package driver;

import java.io.File;
import java.io.FileNotFoundException;

import graph.GraphFactory;
import graph.Graph;
import graph.State;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import ndfs.nndfs.Color;
import ndfs.NDFS;
import ndfs.NDFSFactory;
import ndfs.Result;

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
        System.out.println("    <version> is one of {seq, multicore, multicoreExtended}");
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
            System.out.printf("\n%s took %d ms\n", version, end - start);
        }
    }

    private static void runMCNDFS(final String version, final File file, int nrWorkers) throws FileNotFoundException, ArgumentException {

        ExecutorService executor = Executors.newFixedThreadPool(nrWorkers);
        CompletionService<String> taskCompletionService = new ExecutorCompletionService<String>(executor);

        for (int i = 0; i < nrWorkers; i++) {
            taskCompletionService.submit(new ndfs.mcndfs_1_naive.ThreadWorker(file, version));
        }
        
        for (int i = 0; i < nrWorkers; i++) {

            try {
                Future<String> stringResult = taskCompletionService.take();
                String res = stringResult.get();
                if (res.toLowerCase().contains("found a cycle")) {
                    //toLowerCase() because the strings from the CycleFound's methods class
                    //contain sometimes "Found a cycle" or "found a cycle".
                    System.out.println(res);
                    executor.shutdownNow();
                    break;
                }else{
                    System.out.println(res);
                }
            } catch (InterruptedException e) {
                System.out.println("Error Interrupted exception");
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                System.out.println("Error get() threw exception");
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        System.out.println("\nAll threads are done (MCNDFS Naive).");
    }
    
    
    private static void runMCNDFSExt(final String version, final File file, int nrWorkers) throws FileNotFoundException, ArgumentException {

        ExecutorService executor = Executors.newFixedThreadPool(nrWorkers);
        CompletionService<String> taskCompletionService = new ExecutorCompletionService<String>(executor);

        for (int i = 0; i < nrWorkers; i++) {
            taskCompletionService.submit(new ndfs.mcndfs_extensions.ThreadWorkerExt(file, version));
        }
        
        for (int i = 0; i < nrWorkers; i++) {

            try {
                Future<String> stringResult = taskCompletionService.take();
                String res = stringResult.get();
                if (res.toLowerCase().contains("found a cycle")) {
                    //toLowerCase() because the strings from the CycleFound's methods class
                    //contain sometimes "Found a cycle" or "found a cycle".
                    System.out.println(res);
                    executor.shutdownNow();
                    break;
                }else{
                    System.out.println(res);
                }
            } catch (InterruptedException e) {
                System.out.println("Error Interrupted exception");
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                System.out.println("Error get() threw exception");
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        System.out.println("\nAll threads are done (MCNDFS Extended).");
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
                
            case "multicoreExtended":{
                if (nrWorkers <= 0) {
                    throw new ArgumentException("multicoreExtended can only work with at least 1 worker");
                }
                runMCNDFSExt("multicore extended", file, nrWorkers);
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
