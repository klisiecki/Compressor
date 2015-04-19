package com.company;

import java.util.BitSet;

public class CompressedPackage {
    private BitSet data;
    private CompressMode mode;
    private int dataCount;

    private int growthBits;

    public CompressMode getMode() {
        return mode;
    }

    public int getDataCount() {
        return dataCount;
    }

    public int getGrowthBits() {
        return growthBits;
    }

    public void initialize(CompressMode mode, int dataCount, int growthBits) {
        this.mode = mode;
        this.dataCount = dataCount;
        this.growthBits = growthBits;
    }


    public CompressedPackage() {

    }
}
