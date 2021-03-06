package ndfs;

import java.util.Map;

import graph.Graph;
import graph.State;


public class NDFSFactory {



    public static NDFS createNNDFS(Graph graph, 
            Map<State, ndfs.nndfs.Color> map) {
        return new ndfs.nndfs.NNDFS(graph, map);
    }


    public static NDFS createMCNDFSNaive(Graph graph, Map<State,
            ndfs.mcndfs_1_naive.Color> map, Map<State, Boolean> pinkMap) {
        return new ndfs.mcndfs_1_naive.MCNDFS(graph, map, pinkMap);
    }
}
