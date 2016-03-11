package com.company;


import java.util.BitSet;

public class CustomBitSet extends BitSet {

    public CustomBitSet(BitSet bitSet) {
        for (int i = 0; i < bitSet.size(); i++) {
            set(i, bitSet.get(i));
        }
    }

    public CustomBitSet() {

    }

    public String print() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length(); i++) {
            if (i % 8 == 0) {
                sb.append(" ");
            }
            sb.append(get(i) ? "1" : "0");
        }
        return sb.toString();
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

    public short getShort(int from, int to) {
        short result = 0;
        for (int i = from; i <= to; i++) {
            if (get(i)) {
                result += Math.pow(2, i-from);
            }
        }

        return result;
    }

    public short getConversed(int from, int to) {
        if (get(from)) {
            return (short) ~getShort(from+1, to);
        } else {
            return getShort(from+1, to);
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
