package com.slotsense.core;

import java.util.*;

public class BaseEngine {
    protected final List<com.slotsense.core.Step> steps;

    public BaseEngine(List<Step> steps) {
        this.steps = steps;
    }

    public Map<String, Object> spin(SpinContext ctx) {
        final long sessionSeed = ctx.seed;
        final RNG sessionRng = (ctx.rng != null) ? ctx.rng : new RNG(sessionSeed, false);
        ctx.rng = sessionRng;

        final long[] totalSessionWin = {0};

        // función local (Java: método privado inline con lambda no tan cómodo -> lo hacemos como bloque)
        java.util.function.BiFunction<String, Integer, Map<String, Object>> runOne =
                (spinType, spinIndex) -> {

                    ctx.resetTransient();
                    ctx.state.put("_spin_type", spinType);

                    RNG spinRng = sessionRng.fork(spinType + "_" + spinIndex);
                    ctx.rng = spinRng;
                    ctx.seed = spinRng.seed;

                    for (Step s : steps) s.run(ctx);

                    ctx.payout = ctx.totalWin;
                    totalSessionWin[0] += ctx.totalWin;

                    Map<String, Object> out = new HashMap<>();
                    out.put("spinType", spinType);
                    out.put("spinIndex", spinIndex);
                    out.put("reelLayout", flattenBoard(ctx.board));
                    out.put("creditsWon", ctx.totalWin);
                    out.put("bet", ctx.bet);
                    out.put("events", new ArrayList<>(ctx.events));
                    out.put("generatedSequence", ctx.seed);
                    out.put("stopPositions", ctx.state.get("stop_positions"));

                    return out;
                };

        Map<String, Object> baseResult = runOne.apply("base", 0);

        List<Map<String, Object>> freeSpins = new ArrayList<>();
        int guard = 0;

        while (Boolean.TRUE.equals(ctx.state.get("free_spins_active"))
                && toInt(ctx.state.get("free_spins_left")) > 0) {

            int maxFs = toIntOr(ctx.config.get("max_free_spins_total"), 10);
            guard++;
            if (guard > maxFs) {
                ctx.state.put("free_spins_active", false);
                ctx.state.put("free_spins_left", 0);
                ctx.events.add("free_spins_cap_reached");
                break;
            }

            // consumir 1 FS antes
            ctx.state.put("free_spins_left", toInt(ctx.state.get("free_spins_left")) - 1);

            freeSpins.add(runOne.apply("free", guard));

            if (toInt(ctx.state.get("free_spins_left")) <= 0) {
                ctx.state.put("free_spins_active", false);
            }
        }

        ctx.state.remove("_spin_type");
        ctx.state.remove("is_free_spin");

        if (freeSpins.isEmpty()) return baseResult;

        Map<String, Object> sessionOut = new HashMap<>();
        sessionOut.put("bet", ctx.bet);
        sessionOut.put("generatedSequence", sessionSeed);
        sessionOut.put("creditsWon", totalSessionWin[0]);
        sessionOut.put("baseSpin", baseResult);
        sessionOut.put("freeSpins", freeSpins);

        Map<String, Object> st = new HashMap<>();
        st.put("free_spins_active", Boolean.TRUE.equals(ctx.state.get("free_spins_active")));
        st.put("free_spins_left", toInt(ctx.state.get("free_spins_left")));
        st.put("bonus_count", toInt(ctx.state.get("bonus_count")));
        sessionOut.put("state", st);

        if (sessionRng.debug) {
            sessionOut.put("rngTrace", sessionRng.trace);
            sessionOut.put("subSeeds", sessionRng.forks);
        }

        return sessionOut;
    }

    private static List<Object> flattenBoard(List<List<Object>> board) {
        List<Object> flat = new ArrayList<>();
        if (board == null) return flat;
        for (List<Object> row : board) flat.addAll(row);
        return flat;
    }

    private static int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(String.valueOf(o));
    }

    private static int toIntOr(Object o, int def) {
        if (o == null) return def;
        return toInt(o);
    }
}
