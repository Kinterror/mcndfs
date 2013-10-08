package ndfs.mcndfs_extensions;


import java.util.Map;
import graph.State;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class CounterExt {

    private Map<State, Integer> map;
    
    final Lock lock = new ReentrantLock();
    final Condition equalZero = lock.newCondition();

    
    CounterExt(Map<State, Integer> map) {
        this.map = map;
    }

    void incrementCounter(State state) {
        lock.lock();
        try {
            if (map.get(state) == null) {   //The first time a state is visited,
                                            //it isn't in the map, so we add it and set his value to 0.
                map.put(state, 0);
            }
            map.put(state, map.get(state) + 1);
        } finally {
            lock.unlock();
        }
    }
    
    void decrementCounter(State state) throws Exception {
        lock.lock();
        try {
            if (map.get(state) == null) {
                throw new Exception("Error: the state is not in the map");
            } else if (map.get(state) <= 0) {
                throw new Exception("Error: the state's count value is below or equal 0");
            }
            else{
                map.put(state, map.get(state) - 1);
                equalZero.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
    
    //public

    int getValue(State state){
        return map.get(state);
    }
    
}
