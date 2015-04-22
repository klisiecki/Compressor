package com.company;


import java.util.BitSet;

public class CustomBitSet extends BitSet {

    public void print() {
        for (int i = 0; i < length(); i++) {
            System.out.print(get(i) ? "1" : "0");
        }
    }

    public void add(int from, int to, short value) {
        for (int i = from; i <= to; i++) {
            if ((value & (1 << i-from)) > 0) {
                set(i);
            } else {
                clear(i);
            }
        }
    }

    public void addConversed(int from, int to, short value) {
        if (value < 0) {
            value = (short) ~value;
            set(from);
        }
        add(from + 1, to, value);
    }
}
