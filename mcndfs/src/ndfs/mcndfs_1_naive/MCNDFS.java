package ndfs.mcndfs_1_naive;

import java.util.Map;

import graph.State;
import graph.Graph;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ndfs.NDFS;
import ndfs.Result;
import ndfs.CycleFound;
import ndfs.NoCycleFound;


public class MCNDFS implements NDFS{

    private Graph graph;
    private Colors colors;
    
    private static Red red;
    private static Counter count;
    private Map<State, Boolean> pink;
    
    
    // Initialize all the static fields
    static{
        red = new Red(new HashMap<State, Boolean>());
        count = new Counter(new HashMap<State, Integer>());
    }


    public MCNDFS(Graph graph, Map<State, Color> colorStore) {
        this.graph = graph;
        this.colors = new Colors(colorStore);
        this.pink = new HashMap<State, Boolean>();
    }


    @Override
    public void init() {}


    private void dfsRed(State s) throws Result {
        pink.put(s, true);
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.CYAN)) {
                throw new CycleFound();
            }
            else if (pink.get(t)==false && red.isRed(t)==false) {
                dfsRed(t);
            }
        }
        if (s.isAccepting()){
            try {
                count.decrementCounter(s);

            } catch (Exception ex) {
                Logger.getLogger(MCNDFS.class.getName()).log(Level.SEVERE, null, ex);
            }
            while(count.getValue(s) != 0){ //await s.count == 0
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(MCNDFS.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        red.setRed(s, true);
        pink.put(s, false);
    }


    private void dfsBlue(State s) throws Result {
        colors.color(s, Color.CYAN);
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.WHITE) && red.isRed(t)==false) {
                dfsBlue(t);
            }
        }
        if (s.isAccepting()) {
            count.incrementCounter(s);
            dfsRed(s);
        }
        else {
            colors.color(s, Color.BLUE);
        }
    }
    

    private void mcndfs(State s) throws Result {
        dfsBlue(s);
        throw new NoCycleFound();
    }


    public void ndfs() throws Result {
        mcndfs(graph.getInitialState());
    }


}