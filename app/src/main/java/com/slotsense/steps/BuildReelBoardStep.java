package com.slotsense.steps;

import com.slotsense.core.RNG;
import com.slotsense.core.Step;
import com.slotsense.core.SpinContext;

import java.util.*;

public class BuildReelBoardStep implements Step {
    @Override
    public void run(SpinContext ctx) {
        RNG rng = ctx.rng.fork(this.getClass().getSimpleName());

        int rows = toInt(ctx.config.get("rows"),3);
        int cols = toInt(ctx.config.get("cols"),3);

        Object active = ctx.config.get("active_reels");
        if(active == null) {
            active =  ctx.config.get("reels");
        }
        if (!(active instanceof List<?>)){
            throw new IllegalArgumentException("Config must include reels");
        }
        @SuppressWarnings("unchecked")
        List<List<Object>> reels = (List<List<Object>>) active;

        if (reels.size() != cols) {
            throw new IllegalArgumentException(
                    "reels columns (" + reels.size() + ") must match cols (" + cols + ")"
            );
        }

        // stop positions para debug/reproducibilidad
        List<Integer> stopPositions = new ArrayList<>();

        // board vacío rows x cols
        List<List<Object>> board = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            board.add(new ArrayList<>(Collections.nCopies(cols, null)));
        }

        // Generar ventana visible desde cada reel (wrap)
        for (int c = 0; c < cols; c++) {
            List<Object> reel = reels.get(c);
            if (reel == null || reel.isEmpty()) {
                throw new IllegalArgumentException("Reel column " + c + " is empty");
            }

            int stop = rng.randint(0, reel.size() - 1, "stop_pos.c" + c);
            stopPositions.add(stop);

            for (int r = 0; r < rows; r++) {
                int idx = (stop + r) % reel.size();
                board.get(r).set(c, reel.get(idx));
            }
        }

        // Aplicar injections encima (si existen)
        @SuppressWarnings("unchecked")
        List<List<Object>> injections = (List<List<Object>>) ctx.state.get("injections");
        if (injections != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Object inj = injections.get(r).get(c);
                    if (inj != null) {
                        board.get(r).set(c, inj);
                    }
                }
            }
        }

        ctx.board = board;
        List<Object> symbols = flattenRowMajor(board);
        ctx.emitBonusInfo("BuildReelBoard", Map.of(
                "cols", cols,
                "rows", rows,
                "stopPositions", stopPositions,
                "symbols", symbols
        ), null);
        ctx.state.put("stop_positions", stopPositions);
        ctx.events.add("reel_board_built");
    }

    private static int toInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(String.valueOf(o));
    }

    private static List<Object> flattenRowMajor(List<List<Object>> board) {
        List<Object> out = new ArrayList<>();
        for (List<Object> row : board) out.addAll(row);
        return out;
    }
}

