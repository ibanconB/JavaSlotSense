package com.slotsense.contract.v1;

public class Payout {
    public long creditsWon;
    public long totalWin;

    public Payout() {}
    public Payout(long creditsWon, long totalWin) {
        this.creditsWon = creditsWon;
        this.totalWin = totalWin;
    }
}
