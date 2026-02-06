package com.slotsense.demo;




import com.fasterxml.jackson.core.JsonProcessingException;
import com.slotsense.core.SpinContext;
import com.slotsense.engines.GenericEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

public class Main {
    public static void main(String[] args) throws JsonProcessingException {
        Map<String, Object> config = new HashMap<>();
        config.put("symbols", List.of("A", "B", "C", "D"));
        config.put("weights", List.of("50", "30", "15", "5"));
        config.put("payouts", Map.of("A", 1, "B", 2, "C", 5, "D", 10));
        config.put("evaluationMode", "winningLines");
      //  config.put("debug_rng", true);
        config.put("wild_symbol", "W");
        config.put("bonus_symbol", "S");
        config.put("non_paying_symbols", List.of("S"));
        config.put("bonus_trigger_count", 1);
        config.put("bonus_award_free_spins", 5);
        config.put("bonus_retrigger_award", 5);
        config.put("max_free_spins_total", 10);

        config.put("rows", 3);
        config.put("cols", 3);

        config.put("reels", List.of(
                List.of("A","A","B","C","A","D","B","A"),
                List.of("A","B","B","C","A","A","D","B"),
                List.of("B","A","C","A","A","D","B","C")
        ));

        config.put("symbol_injection", Map.of(
                "wild_chance", 20,   // 20%
                "bonus_chance", 10   // 10%
        ));

        // Si quieres paylines, ejemplo 3x3 simples (3 líneas horizontales)
        config.put("paylines", List.of(
                List.of(List.of(0,0), List.of(0,1), List.of(0,2)),
                List.of(List.of(1,0), List.of(1,1), List.of(1,2)),
                List.of(List.of(2,0), List.of(2,1), List.of(2,2))
        ));
        config.put("min_match", 3);

        SpinContext ctx = new SpinContext(1.0, 123L, config);
        GenericEngine engine = new GenericEngine();

        ObjectMapper mapper = new ObjectMapper();
       // mapper.enable(SerializationFeature.INDENT_OUTPUT);
        List<Object> result = engine.spin(ctx);
        System.out.println(mapper.writeValueAsString(result));

    }
}
