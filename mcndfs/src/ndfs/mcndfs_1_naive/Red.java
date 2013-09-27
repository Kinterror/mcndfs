package ndfs.mcndfs_1_naive;


import java.util.Map;

import graph.State;



class Red {

    private Map<State, Boolean> map;

    
    Red(Map<State, Boolean> map) {
        this.map = map;
    }

    
    synchronized boolean isRed(State state) {
        if(map.get(state)==null){//The first time a state is visited, it isn't in the map, so we add it and set his color
            map.put(state, false);
        }
        return map.get(state);
    }


    synchronized void setRed(State state, Boolean b) {
        map.put(state, b);
    }
}
