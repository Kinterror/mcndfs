package ndfs.mcndfs_1_naive;


import java.util.Map;
import graph.State;


class Counter {

    private Map<State, Integer> map;

    
    Counter(Map<State, Integer> map) {
        this.map = map;
    }

    synchronized void incrementCounter(State state) {
        if(map.get(state)==null){//The first time a state is visited, it isn't in the map, so we add it and set his value to 0
            map.put(state, 0);
        }
        map.put(state, map.get(state)+1);
    }
    
    synchronized void decrementCounter(State state) throws Exception{
        if(map.get(state)==null)
            throw new Exception("Error: the state is not in the map");
        else if(map.get(state)<=0)
            throw new Exception("Error: the state's count value is below or equal 0");
        
        map.put(state, map.get(state)-1);
    }

    synchronized int getValue(State state){
        return map.get(state);
    }
    
}
