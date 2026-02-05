package com.slotsense.core;

import java.util.*;

public class SpinContext {
    // entry params
    public double bet;
    public long seed;
    public  Map<String, Object> config;
    public  Map<String, Object> state;

    //spin
    public List<List<Object>> board;
    public List<Map<String,Object>> wins;
    public long totalWin;
    public List<String> events;

    //outcome
    public long payout;
    public Map<String, Object> stateDelta;

    public RNG rng;

    public SpinContext(double bet, Long seed, Map<String, Object> config, Map<String, Object> state) {
        this.bet = bet;
        this.seed = (seed != null) ? seed : new Random().nextLong();
        this.config = (config != null) ? config : new HashMap<>();
        this.state = (state != null) ? state : new HashMap<>();

        boolean debug = getBoolean(config, "debug_rng", getBoolean(config, "debug", false));
        this.rng = new RNG(this.seed, debug);

        resetTransient();
    }

    public SpinContext(double bet, Long seed, Map<String, Object> config) {
        this(bet, seed, config, null);
    }

    public SpinContext(double bet) {
        this(bet, null, null, null);
    }

    public void resetTransient() {
        this.board = null;
        this.wins = new ArrayList<>();
        this.totalWin = 0;
        this.events = new ArrayList<>();
        this.payout = 0;
        this.stateDelta = new HashMap<>();
    }

    private static boolean getBoolean(Map<String, Object> map, String key, boolean def) {
        if (map == null) return def;
        Object v = map.get(key);
        if (v == null) return def;
        if (v instanceof Boolean) return (Boolean) v;
        return Boolean.parseBoolean(String.valueOf(v));
    }

}
