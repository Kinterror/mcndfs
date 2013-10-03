
package ndfs.mcndfs_1_naive;

import graph.Graph;
import graph.GraphFactory;
import graph.State;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import ndfs.NDFS;
import ndfs.NDFSFactory;
import ndfs.Result;

    public class ThreadWorker implements Runnable{
        
        private File file;
        private String version;
        
        public ThreadWorker(File file, String version){
            this.file = file;
            this.version = version;
            
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " has just started");
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
                    System.out.println("thread " + Thread.currentThread().getName() + " - " + r.getMessage());
                    System.out.printf("%s took %d ms\n", version, end - start);
                }
                
            } catch (FileNotFoundException ex) {
                System.out.println("File not found !");
            }
        }
    }
