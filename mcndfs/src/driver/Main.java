package driver;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Map;
import java.util.HashMap;

import graph.GraphFactory;
import graph.Graph;
import graph.State;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ndfs.nndfs.Color;
import ndfs.mcndfs_1_naive.Color;

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
        System.out.println("    <version> is one of {seq, multicore}");
    }

    private static void runNDFS(String version, Map<State, Color> colorStore,
            File file) throws FileNotFoundException, ArgumentException {

        Graph graph = GraphFactory.createGraph(file);
        NDFS ndfs;
        switch (version) {
            case "seq":
                ndfs = NDFSFactory.createNNDFS(graph, colorStore);
                break;
                
            case "multicore":
                ndfs = NDFSFactory.createMCNDFSNaive(graph, colorStore);
                break;
                
            default:
                throw new ArgumentException("Unkown version: " + version);
        }
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

    private static void dispatch(File file, String version, int nrWorkers)
            throws ArgumentException, FileNotFoundException {
        switch (version) {
            case "seq":
                {
                    if (nrWorkers != 1) {
                        throw new ArgumentException("seq can only run with 1 worker");
                    }
                    Map<State, ndfs.nndfs.Color> map = new HashMap<State, ndfs.nndfs.Color>();
                    runNDFS("seq", map, file);
                    break;
                }
            case "multicore":
                {
                    if (nrWorkers == 0) {
                        throw new ArgumentException("multicore can only work with at least 1 worker");
                    }
                    
                    ExecutorService executor = Executors.newFixedThreadPool(nrWorkers);
                    //Runnable worker = new MyRunnable...
                    //executor.execute(worker);
                    
                    Map<State, ndfs.mcndfs_1_naive.Color> map = new HashMap<State, ndfs.mcndfs_1_naive.Color>();
                    runNDFS("multicore", map, file);
                    
                    //executor.shutdown();
                    //executor.awaitTermination();
                    
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
