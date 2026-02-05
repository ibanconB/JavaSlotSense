package com.slotsense.steps;

import com.slotsense.core.Step;
import com.slotsense.core.SpinContext;

import java.util.*;

public class CountBonusStep implements Step {

    @Override
    public void run(SpinContext ctx) {
        Object bonusSym = ctx.config.get("bonus_symbol");
        if (bonusSym == null) return;
        if (ctx.board == null) return;

        int triggerCount = toInt(ctx.config.get("bonus_trigger_count"), 3);
        int award = toInt(ctx.config.get("bonus_award_free_spins"), 10);
        int retriggerAward = toInt(ctx.config.get("bonus_retrigger_award"), award);

        int count = 0;
        for (List<Object> row : ctx.board) {
            for (Object cell : row) {
                if (String.valueOf(cell).equals(String.valueOf(bonusSym))) count++;
            }
        }

        ctx.state.put("bonus_count", count);

        if (count < triggerCount) return;

        boolean alreadyActive = Boolean.TRUE.equals(ctx.state.get("free_spins_active"));

        if (!alreadyActive) {
            // trigger
            ctx.state.put("free_spins_active", true);
            ctx.state.put("free_spins_left", award);
            ctx.events.add("bonus_triggered:" + count);
        } else {
            // retrigger
            int left = toInt(ctx.state.get("free_spins_left"), 0);
            ctx.state.put("free_spins_left", left + retriggerAward);
            ctx.events.add("bonus_retriggered:" + count);
        }
    }

    private static int toInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        return Integer.parseInt(String.valueOf(o));
    }
}
