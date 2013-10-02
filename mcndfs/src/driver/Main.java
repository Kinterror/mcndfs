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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ndfs.nndfs.Color;

import ndfs.NDFS;
import ndfs.NDFSFactory;
import ndfs.Result;
import ndfs.mcndfs_1_naive.MCNDFS;

public class Main {

    private static class ArgumentException extends Exception {

        private static final long serialVersionUID = 1L;
        private ExecutorService executor;

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


        final ExecutorService executor = Executors.newFixedThreadPool(nrWorkers);

        for (int i = 0; i < nrWorkers; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Graph graph = GraphFactory.createGraph(file);
                        Map<State, ndfs.mcndfs_1_naive.Color> colorStore = new HashMap<State, ndfs.mcndfs_1_naive.Color>();
                        Map<State, Boolean> pinkStore = new HashMap<State, Boolean>();
                        
                        NDFS ndfs = NDFSFactory.createMCNDFSNaive(graph, colorStore, pinkStore);

                        long start = System.currentTimeMillis();

                        long end;
                        try {
                            ndfs.ndfs();
                            throw new Error("No result returned by " + version);
                        } catch (Result r) {
                            end = System.currentTimeMillis();
                            System.out.println("thread " +Thread.currentThread().getName()+ " - " + r.getMessage());
                            System.out.printf("%s took %d ms\n", version, end - start);
                            
                        }

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });



        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        /*
         Graph graph = GraphFactory.createGraph(file);
         NDFS ndfs;
         ndfs = NDFSFactory.createMCNDFSNaive(graph, colorStore);

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
         */


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
                
                //Map<State, ndfs.mcndfs_1_naive.Color> map = new HashMap<State, ndfs.mcndfs_1_naive.Color>();
                //Map<State, Boolean> mapPink = new HashMap<State, Boolean>();
                runMCNDFS("multicore", file, nrWorkers);
                break;
                
                /*
                for (int i = 0; i < nrWorkers; i++) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Map<State, ndfs.mcndfs_1_naive.Color> map = new HashMap<State, ndfs.mcndfs_1_naive.Color>();
                            try {
                                runMCNDFS("multicore", map, file);
                            } catch (FileNotFoundException | ArgumentException ex) {
                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    });
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(1, TimeUnit.DAYS);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
                */
                
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
