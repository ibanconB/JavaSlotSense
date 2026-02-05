package com.slotsense.steps;

import com.slotsense.core.RNG;
import com.slotsense.core.SpinContext;
import com.slotsense.core.Step;

import java.util.*;

public class BuildBoardStep implements Step {
    @Override
    public void run(SpinContext ctx) {
        RNG rng = ctx.rng.fork(this.getClass().getSimpleName());

        int rows = toIntOr(ctx.config.get("rows"), 3);
        int cols = toIntOr(ctx.config.get("cols"), 3);

        @SuppressWarnings("unchecked")
        List<Object> symbols = (List<Object>) ctx.config.get("symbols");
        if (symbols == null || symbols.isEmpty()) {
            throw new IllegalArgumentException("Config must include 'symbols' list");
        }

        @SuppressWarnings("unchecked")
        List<Object> weightsRaw = (List<Object>) ctx.config.get("weights");

        List<RNG.Pair<Object, Double>> seq = new ArrayList<>();
        if (weightsRaw != null && weightsRaw.size() == symbols.size()) {
            for (int i = 0; i < symbols.size(); i++) {
                seq.add(new RNG.Pair<>(
                        symbols.get(i),
                        Double.parseDouble(String.valueOf(weightsRaw.get(i)))
                ));
            }
        } else {
            double prob = 100.0 / symbols.size();
            for (Object s : symbols) seq.add(new RNG.Pair<>(s, prob));
        }

        // injections (si existe)
        @SuppressWarnings("unchecked")
        List<List<Object>> injections = (List<List<Object>>) ctx.state.get("injections");

        List<List<Object>> board = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            List<Object> row = new ArrayList<>();
            for (int c = 0; c < cols; c++) {

                Object injected = null;
                if (injections != null) {
                    injected = injections.get(r).get(c);
                }

                Object sym;
                if (injected != null) {
                    sym = injected; // respeta la inyección
                } else {
                    sym = rng.weightedChoice(seq, "weighted_choice");
                }

                row.add(sym);
            }
            board.add(row);
        }

        ctx.board = board;
        ctx.events.add("board_built");
    }

    private static int toIntOr(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(String.valueOf(o));
    }
}
