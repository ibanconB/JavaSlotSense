package com.slotsense.contract.v1;

import java.util.ArrayList;
import java.util.List;

public class SessionResult {
    public String schemaVersion = "1.0.0";
    public String type = "session";

    public double bet;
    public long generatedSequence;   // sessionSeed
    public long creditsWon;

    public SpinResult baseSpin;
    public List<SpinResult> freeSpins = new ArrayList<>();

    public SessionResult() {}
}
