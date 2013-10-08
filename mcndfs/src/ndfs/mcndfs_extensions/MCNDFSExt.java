package ndfs.mcndfs_extensions;

import graph.State;
import graph.Graph;

import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Random;

import ndfs.CycleFound;
import ndfs.NDFS;
import ndfs.NoCycleFound;
import ndfs.Result;



public class MCNDFSExt implements NDFS {

    private Graph graph;
    private ColorsExt colors;
    private static RedExt red;
    private static CounterExt count;
    private boolean allred; //added

    // Initialize all the static fields
    static {
        red = new RedExt(new HashMap<State, Boolean>());
        count = new CounterExt(new HashMap<State, Integer>());
    }

    public MCNDFSExt(Graph graph, Map<State, ColorExt> colorStore) {
        this.graph = graph;
        this.colors = new ColorsExt(colorStore);
    }

    @Override
    public void init() {
    }

    private void dfsRed(State s) throws Result {
        colors.color(s, ColorExt.PINK);
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, ColorExt.CYAN)) {
                throw new CycleFound(Thread.currentThread().getName());
            }
            if (colors.hasColor(t, ColorExt.PINK) == false && red.isRed(t) == false) {
                dfsRed(t);
            }
        }
        if (s.isAccepting()) {
            decrementCounter(s); //changed to private method
            await(s); //changed to private method
        }
        red.setRed(s, true);
    }

    private void dfsBlue(State s) throws Result {
        allred = true; //added, critical section?
        colors.color(s, ColorExt.CYAN);
        List<State> listOfStates = graph.post(s);
        Collections.shuffle(listOfStates, new Random( System.nanoTime()));
        for (State t : listOfStates) {
            if (colors.hasColor(t, ColorExt.CYAN) && (s.isAccepting() || t.isAccepting())){
                throw new CycleFound(Thread.currentThread().getName());
            }
            if (colors.hasColor(t, ColorExt.WHITE) && red.isRed(t) == false) {
                dfsBlue(t);
            }
            if (red.isRed(t)){ //added, critical section?
                allred = false; //added
            }
        }
        if (allred){ //added
            red.setRed(s, true); //added, critical section?
        }
        else if (s.isAccepting()) {
            count.incrementCounter(s);
            dfsRed(s);
        } 
        colors.color(s, ColorExt.BLUE);
    }

    private void mcndfs(State s) throws Result {
        dfsBlue(s);
        throw new NoCycleFound();
    }

    @Override
    public void ndfs() throws Result {
        mcndfs(graph.getInitialState());
    }

    private void decrementCounter(State s) {
        try {
            count.decrementCounter(s);
        } catch (Exception ex) {
            Logger.getLogger(MCNDFSExt.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }

    private void await(State s) {
        count.lock.lock();
            try {
                while (count.getValue(s) > 0) {
                    count.equalZero.await();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MCNDFSExt.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                count.lock.unlock();
            }
    }
}