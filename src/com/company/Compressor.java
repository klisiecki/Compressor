package com.company;

import java.util.Arrays;

public class Compressor {
    private static final int HIST_SIZE = 10;

    public static int initializePackage(CompressedPackage compressedPackage, short[] data, int maxPackageSize) {
        System.out.println(Arrays.toString(createStats(data)) + "");
        return 0;
    }

    public static int calculateDataCount(short[] data, CompressMode mode, int maxPackageSize) {
        int[] hist = new int[HIST_SIZE];
        int headerSize = 0;
        short value;

        switch (mode) {
            case GROWTHS:
                headerSize = 28;
                value = data[0];
                int dataSize;
                for (int i = 1; i < data.length; i++) {
                    hist[getBitsForGrowth(data[i] - value)]++;
                    dataSize = getMaxBits(hist) * i;
                    if (headerSize + dataSize > maxPackageSize) {
                        return i; // +1 bo pierwsza wartość jest już w nagłówku, -1 bo ostatnia wartość się nie zmieściła
                    }
                    value = data[i];
                }
                return data.length;

                //break;

            case MIXED:
                headerSize = 28;
                int maxData = 0;
                int maxDataPosition = 0;
                int maxDataConstantValues = 0;
                int maxDataGrowths = 0;
                value = data[0];
                for (int i = 1; i < data.length; i++) {
                    hist[getBitsForGrowth(data[i] - value)]++;
                    for (int j = 1; j < HIST_SIZE; j++) {
                        int constantValues = getValuesCountAbove(hist, j);
                        int growths = i - constantValues;
                        int size = constantValues * (10 + 1) + (j + 1) * growths; //int size = constantValues * (8 + 1) + j * (growths + 1); // dlaczego 8+1? a nie 10+1? // a nie (j + 1) * growths?
                        if (i > maxData && size + headerSize <= maxPackageSize) {
                            maxData = i;
                            maxDataPosition = j;
                            maxDataConstantValues = constantValues;
                            maxDataGrowths = growths;
                        }
                    }
                }
                System.out.println("maxDataPosition = " + maxDataPosition +
                        "(values = " + maxDataConstantValues + ", " +
                        "growths = " + maxDataGrowths + ")");
                return maxData;  // +1 bo pierwsza wartość jest już w nagłówku ?

            case VALUES:
                headerSize = 15;
                return Math.min( ((maxPackageSize - headerSize) / 10), data.length);
        }
        return 0;
    }

    private static int getMaxBits(int[] hist) {
        for (int i = HIST_SIZE-1; i >= 0; i--) {
            if (hist[i] > 0) {
                return i;
            }
        }
        return 0;
    }

    private static int getValuesCountAbove(int[] hist, int index) {
        int result = 0;
        for (int i = index + 1; i < HIST_SIZE; i++) {
            result += hist[i];
        }
        return result;
    }

    private static int[] createStats(short[] data) {
        int[] hist = new int[HIST_SIZE];
        for (int i = 1; i < data.length; i++) {
            int growth = data[i] - data[i-1];
            hist[getBitsForGrowth(growth)]++;
        }

        return hist;
    }

    private static int getBitsForGrowth(int growth) {
//        System.out.print("calculating bits for " + growth + " ");
        if (growth > Short.MAX_VALUE || growth < Short.MIN_VALUE) {
            throw new RuntimeException("Growth too big");
        }
        int growthShort = (short) growth;
        if (growthShort < 0) {
            growthShort = ~growth;
        }
        for (int i = 9; i >= 0; i--) {
            int bit = (growthShort & (1 << i));
            //System.out.print(bit > 0 ? "1" : "0");
            //System.out.println("bit " + i + " = " + (growth & (1 << i)));
            if (bit > 0) { //if ((growthShort & (1 << i)) > 0) {
//                System.out.println();
//                return growthShort < 0 ? i + 1 : i;
                return i;
            }
        }
//        System.out.println();
        return 0;
    }

}
