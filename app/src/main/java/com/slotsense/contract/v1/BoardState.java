package com.slotsense.contract.v1;

import java.util.List;

public class BoardState {
    public String mode;            // "reels"
    public int cols;
    public int rows;
    public List<Object> symbols;   // flat row-major
    public List<Integer> stopPositions;

    public BoardState() {}
}
