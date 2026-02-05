package com.slotsense.steps;

import com.slotsense.core.RNG;
import com.slotsense.core.SpinContext;
import com.slotsense.core.Step;

import java.util.*;

public class PreSpinSetupStep implements Step {
    @Override
    public void run(SpinContext ctx) {
        // Marca si este spin es free o base (igual que Python)
        String spinType = String.valueOf(ctx.state.getOrDefault("_spin_type", "base"));
        boolean isFree = "free".equals(spinType);
        ctx.state.put("is_free_spin", isFree);

        // Selección de reels según modo (si tienes reels_free)
        if (isFree && ctx.config.containsKey("reels_free")) {
            ctx.config.put("active_reels", ctx.config.get("reels_free"));
        } else {
            ctx.config.put("active_reels", ctx.config.get("reels"));
        }

        int rows = toInt(ctx.config.get("rows"), 3);
        int cols = toInt(ctx.config.get("cols"), 3);

        // injections = matriz rows x cols de nulls
        List<List<Object>> injections = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            List<Object> row = new ArrayList<>();
            for (int c = 0; c < cols; c++) row.add(null);
            injections.add(row);
        }
        ctx.state.put("injections", injections);

        // symbol_injection config
        @SuppressWarnings("unchecked")
        Map<String, Object> injCfg = (Map<String, Object>) ctx.config.getOrDefault("symbol_injection", new HashMap<>());

        // Wilds
        double wildChance = toDouble(injCfg.get("wild_chance"), 0.0); // en %
        Object wildSym = ctx.config.get("wild_symbol"); // puede ser "14" o 14
        RNG wildRng = ctx.rng.fork("WildInjection");

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double roll = wildRng.rand("wild.roll") * 100.0;
                if (roll < wildChance) {
                    injections.get(r).set(c, wildSym);
                }
            }
        }

        // Bonus
        double bonusChance = toDouble(injCfg.get("bonus_chance"), 0.0); // en %
        Object bonusSym = ctx.config.get("bonus_symbol");
        int bonusMax = toInt(ctx.config.get("bonus_max"), 999999);

        RNG bonusRng = ctx.rng.fork("BonusInjection");

        int bonusCount = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (bonusCount >= bonusMax) return; // igual que tu Python
                double roll = bonusRng.rand("bonus.roll") * 100.0;
                if (roll < bonusChance) {
                    injections.get(r).set(c, bonusSym);
                    bonusCount++;
                }
            }
        }
    }

    private static int toInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(String.valueOf(o));
    }

    private static double toDouble(Object o, double def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).doubleValue();
        return Double.parseDouble(String.valueOf(o));
    }

}
