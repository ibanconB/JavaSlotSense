package com.slotsense.contract.v1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpinResult {
    public String schemaVersion = "1.0.0";
    public String type = "spin";

    public String spinType;     // base|free
    public int spinIndex;

    public long generatedSequence;  // seed del spin
    public double bet;

    public BoardState board;

    public List<Map<String, Object>> wins = new ArrayList<>();
    public Payout payout;

    public List<FeatureEvent> bonusInfo = new ArrayList<>();

   // public List<String> events = new ArrayList<>();

    public SpinResult() {}
}
