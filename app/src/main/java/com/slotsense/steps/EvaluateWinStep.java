package com.slotsense.steps;

import com.slotsense.core.SpinContext;
import com.slotsense.core.Step;
import java.util.*;

public class EvaluateWinStep implements Step {
    @Override
    public void run(SpinContext ctx) {
        String mode = String.valueOf(ctx.config.getOrDefault("evaluationMode", "winningLines"));

        if ("winningLines".equals(mode)) {
            evaluateWinningLines(ctx);
        }

        ctx.events.add("wins_evaluated");
    }

    @SuppressWarnings("unchecked")
    private void evaluateWinningLines(SpinContext ctx) {
        List<List<Object>> board = ctx.board;
        if (board == null) return;

        Map<String, Object> payouts = (Map<String, Object>) ctx.config.getOrDefault("payouts", new HashMap<>());
        List<List<List<Integer>>> paylines = (List<List<List<Integer>>>) ctx.config.getOrDefault("paylines", new ArrayList<>());

        Object wild = ctx.config.get("wild_symbol");
        Set<String> nonPaying = new HashSet<>();
        Object np = ctx.config.get("non_paying_symbols");
        if (np instanceof List) for (Object s : (List<?>) np) nonPaying.add(String.valueOf(s));

        int minMatch = toIntOr(ctx.config.get("min_match"), 3);

        long total = 0;
        List<Map<String, Object>> wins = new ArrayList<>();

        if (paylines.isEmpty()) {
            ctx.totalWin = 0;
            ctx.wins = wins;
            return;
        }

        for (int lineIndex = 0; lineIndex < paylines.size(); lineIndex++) {
            List<List<Integer>> coords = paylines.get(lineIndex);

            List<Object> lineSymbols = new ArrayList<>();
            for (List<Integer> rc : coords) {
                int r = rc.get(0), c = rc.get(1);
                lineSymbols.add(board.get(r).get(c));
            }

            Object baseSymbol = null;
            for (Object s : lineSymbols) {
                boolean isWild = (wild != null && String.valueOf(s).equals(String.valueOf(wild)));
                if (!isWild && !nonPaying.contains(String.valueOf(s))) {
                    baseSymbol = s;
                    break;
                }
            }
            if (baseSymbol == null) continue;

            int streak = 0;
            for (Object s : lineSymbols) {
                boolean matchesBase = String.valueOf(s).equals(String.valueOf(baseSymbol));
                boolean isWild = (wild != null && String.valueOf(s).equals(String.valueOf(wild)));
                if (matchesBase || isWild) streak++;
                else break;
            }

            if (streak >= minMatch) {
                Object payoutObj = payouts.get(String.valueOf(baseSymbol));
                long symbolPayout = (payoutObj == null) ? 0 : Long.parseLong(String.valueOf(payoutObj));
                long winAmount = (long) (symbolPayout * streak * ctx.bet);

                if (winAmount > 0) {
                    total += winAmount;
                    Map<String, Object> w = new HashMap<>();
                    w.put("line", lineIndex);
                    w.put("symbol", baseSymbol);
                    w.put("count", streak);
                    w.put("win", winAmount);
                    wins.add(w);
                }
            }
        }

        ctx.totalWin = total;
        ctx.wins = wins;
    }

    private static int toIntOr(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(String.valueOf(o));
    }
}

