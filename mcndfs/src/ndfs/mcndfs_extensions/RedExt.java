package ndfs.mcndfs_extensions;

import graph.State;
import java.util.concurrent.ConcurrentHashMap;



class RedExt {

    private ConcurrentHashMap<State, Boolean> map;

        
    RedExt(ConcurrentHashMap<State, Boolean> map) {
        this.map = map;
    }

    
    boolean isRed(State state) {
        this.map.putIfAbsent(state, Boolean.FALSE);
        return map.get(state);
    }

    void setRed(State state, Boolean b) {
        map.put(state, b);
    }
}
