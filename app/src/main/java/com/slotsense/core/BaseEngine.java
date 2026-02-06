package com.slotsense.core;

import java.util.*;
import com.slotsense.contract.v1.BoardState;
import com.slotsense.contract.v1.Payout;
import com.slotsense.contract.v1.SessionResult;
import com.slotsense.contract.v1.SpinResult;

public class BaseEngine {
    protected final List<Step> steps;

    public BaseEngine(List<Step> steps) {
        this.steps = steps;
    }

    public List<Object> spin(SpinContext ctx) {
        final long sessionSeed = ctx.seed;
        final RNG sessionRng = (ctx.rng != null) ? ctx.rng : new RNG(sessionSeed, false);
        ctx.rng = sessionRng;

        final long[] totalSessionWin = {0};

        java.util.function.BiFunction<String, Integer, SpinResult> runOne =
                (spinType, spinIndex) -> {

                    ctx.resetTransient();
                    ctx.state.put("_spin_type", spinType);

                    RNG spinRng = sessionRng.fork(spinType + "_" + spinIndex);
                    ctx.rng = spinRng;
                    ctx.seed = spinRng.seed;

                    for (Step s : steps) s.run(ctx);

                    totalSessionWin[0] += ctx.totalWin;

                    SpinResult out = new SpinResult();
                    out.spinType = spinType;
                    out.spinIndex = spinIndex;
                    out.bet = ctx.bet;
                    out.generatedSequence = ctx.seed;

                    int rows = toIntOr(ctx.config.get("rows"), 3);
                    int cols = toIntOr(ctx.config.get("cols"), 3);

                    BoardState b = new BoardState();
                    b.mode = "reels";
                    b.rows = rows;
                    b.cols = cols;
                    b.symbols = flattenRowMajor(ctx.board);

                    Object sp = ctx.state.get("stop_positions");
                    if (sp instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<Integer> stopPos = (List<Integer>) sp;
                        b.stopPositions = stopPos;
                    }
                    out.board = b;

                    out.wins = new ArrayList<>(ctx.wins);
                    out.payout = new Payout(ctx.totalWin, ctx.totalWin);
                    out.bonusInfo = new ArrayList<>(ctx.bonusInfo);


                    return out;
                };

        SpinResult baseResult = runOne.apply("base", 0);

        List<SpinResult> freeSpins = new ArrayList<>();
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

            ctx.state.put("free_spins_left", toInt(ctx.state.get("free_spins_left")) - 1);

            freeSpins.add(runOne.apply("free", guard));

            if (toInt(ctx.state.get("free_spins_left")) <= 0) {
                ctx.state.put("free_spins_active", false);
            }
        }

        ctx.state.remove("_spin_type");
        ctx.state.remove("is_free_spin");

        if (freeSpins.isEmpty()) return List.of(baseResult);

        SessionResult sessionOut = new SessionResult();
        sessionOut.bet = ctx.bet;
        sessionOut.generatedSequence = sessionSeed;
        sessionOut.creditsWon = totalSessionWin[0];
        sessionOut.baseSpin = baseResult;
        sessionOut.freeSpins = freeSpins;

        return List.of(sessionOut);
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

    private static List<Object> flattenRowMajor(List<List<Object>> board) {
        List<Object> out = new ArrayList<>();
        if (board == null) return out;
        for (List<Object> row : board) out.addAll(row);
        return out;
    }
}
