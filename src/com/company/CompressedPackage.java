package com.company;

import java.util.BitSet;

public class CompressedPackage {
    private BitSet data;
    private CompressMode mode;
    private int dataCount;

    private int growthBits;
    private short firstValue;


    public CompressedPackage() {

    }
}
