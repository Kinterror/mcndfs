package ndfs.mcndfs_extensions;


import java.util.Map;

import graph.State;



class RedExt {

    private Map<State, Boolean> map;

    
    RedExt(Map<State, Boolean> map) {
        this.map = map;
    }

    
    synchronized boolean isRed(State state) {
        if(map.get(state)==null){//The first time a state is visited, it isn't in the map, so we add it and set its value to false
            map.put(state, false);
        }
        return map.get(state);
    }


    synchronized void setRed(State state, Boolean b) {
        map.put(state, b);
    }
}
