package ndfs.mcndfs_1_naive;

import java.util.Map;

import graph.State;
import graph.Graph;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import ndfs.CycleFound;

import ndfs.NDFS;
import ndfs.NoCycleFound;
import ndfs.Result;
import java.util.List;
import java.util.Random;

public class MCNDFS implements NDFS {

    private Graph graph;
    private Colors colors;
    private static Red red;
    private static Counter count;
    private Pink pink;

    // Initialize all the static fields
    static {
        red = new Red(new HashMap<State, Boolean>());
        count = new Counter(new HashMap<State, Integer>());
    }

    public MCNDFS(Graph graph, Map<State, Color> colorStore, Map<State, Boolean> pinkStore) {
        this.graph = graph;
        this.colors = new Colors(colorStore);
        this.pink = new Pink(pinkStore);
    }

    @Override
    public void init() {
    }

    private void dfsRed(State s) throws Result {
        pink.setPink(s, true);
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.CYAN)) {
                throw new CycleFound(Thread.currentThread().getName());

            }
            if (pink.isPink(t) == false && red.isRed(t) == false) {
                dfsRed(t);
            }
        }
        if (s.isAccepting()) {
            try {
                count.decrementCounter(s);
            } catch (Exception ex) {
                Logger.getLogger(MCNDFS.class.getName()).log(Level.SEVERE, null, ex);
            }

            count.lock.lock();
            try {
                while (count.getValue(s) > 0) {
                    count.equalZero.await();
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MCNDFS.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                count.lock.unlock();
            }
        }
        red.setRed(s, true);
        pink.setPink(s, false);
    }

    private void dfsBlue(State s) throws Result {
        colors.color(s, Color.CYAN);
        List<State> listOfStates = graph.post(s);
        Collections.shuffle(listOfStates, new Random( System.nanoTime()));
        for (State t : listOfStates) {
            if (colors.hasColor(t, Color.WHITE) && red.isRed(t) == false) {
                dfsBlue(t);
            }
        }
        if (s.isAccepting()) {
            count.incrementCounter(s);
            dfsRed(s);
        } else {
            colors.color(s, Color.BLUE);
        }
    }

    private void mcndfs(State s) throws Result {
        dfsBlue(s);
        throw new NoCycleFound();
    }

    @Override
    public void ndfs() throws Result {
        mcndfs(graph.getInitialState());
    }
}
