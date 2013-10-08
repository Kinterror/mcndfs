package ndfs.mcndfs_extensions;

import graph.Graph;
import graph.GraphFactory;
import graph.State;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import ndfs.NDFS;
import ndfs.NDFSFactory;
import ndfs.Result;

public class ThreadWorkerExt implements Callable<String> {

    private File file;
    private String version;

    public ThreadWorkerExt(File file, String version) {
        this.file = file;
        this.version = version;

    }

    @Override
    public String call() throws Exception {
        System.out.println(Thread.currentThread().getName() + " has just started (Multicore Extended)");
        try {
            Graph graph = GraphFactory.createGraph(file);
            Map<State, ndfs.mcndfs_extensions.ColorExt> colorStore = new HashMap<State, ndfs.mcndfs_extensions.ColorExt>();

            NDFS ndfs = NDFSFactory.createMCNDFSExtended(graph, colorStore);

            long start = System.currentTimeMillis();

            long end;
            try {
                ndfs.ndfs();
                throw new Error("\nNo result returned by " + version);
            } catch (Result r) {
                
                end = System.currentTimeMillis();
                return "\n" + r.getMessage() + " in " + (end-start) + " ms";
            }

        } catch (FileNotFoundException ex) {
            System.out.println(ex + "File not found !"); //This exception will already be catched in the main function
        }

        return Thread.currentThread().getName() + " finished without catching result";
    }
}
