package ndfs.mcndfs_extensions;



import java.util.Map;

import graph.State;



class ColorsExt {

    private Map<State, ColorExt> map;
    

    ColorsExt(Map<State, ColorExt> map) {
        this.map = map;
    }


    boolean hasColor(State state, ColorExt color) {
        if (color == ColorExt.WHITE) {
            return map.get(state) == null;
        }
        else {
            return map.get(state) == color;
        }
    }


    void color(State state, ColorExt color) {
        map.put(state, color);
    }
}
