package com.company;

import java.util.Arrays;

public class Compressor {
    private static final int HIST_SIZE = 10;


    public void initializePackage(CompressedPackage compressedPackage, short[] data, int maxPackageSize) {
        System.out.println(Arrays.toString(createStats(data)) + "");

        System.out.println();

        calculateDataCountResult growthsResult = calculateDataCount(data, CompressMode.GROWTHS, maxPackageSize);
        calculateDataCountResult mixedResult = calculateDataCount(data, CompressMode.MIXED, maxPackageSize);
        calculateDataCountResult valuesResult = calculateDataCount(data, CompressMode.VALUES, maxPackageSize);

        System.out.println("\n\nGrowths mode statistics:");
        growthsResult.print();
        System.out.println("\nMixed mode statistics:");
        mixedResult.print();
        System.out.println("\nValues mode statistics:");
        valuesResult.print();

        // Tylko tymczasowo:
        compressedPackage.initialize(CompressMode.GROWTHS, growthsResult.dataCount, growthsResult.growthBitsNum);
        compress(compressedPackage, data);
        compressedPackage.initialize(CompressMode.MIXED, mixedResult.dataCount, mixedResult.growthBitsNum);
        compress(compressedPackage, data);
        compressedPackage.initialize(CompressMode.VALUES, valuesResult.dataCount, valuesResult.growthBitsNum);
        compress(compressedPackage, data);

        // Potem to odkomentować:
//        if(growthsResult.dataCount >= mixedResult.dataCount
//                && growthsResult.dataCount >= valuesResult.dataCount){ // growths mode
//            compressedPackage.initialize(CompressMode.GROWTHS, growthsResult.dataCount,
//                    growthsResult.growthBitsNum);
//        }
//        else if(mixedResult.dataCount >= valuesResult.dataCount){ // mixed mode
//            compressedPackage.initialize(CompressMode.MIXED, mixedResult.dataCount,
//                    mixedResult.growthBitsNum);
//        }
//        else{ // values mode
//            compressedPackage.initialize(CompressMode.VALUES, valuesResult.dataCount,
//                    valuesResult.growthBitsNum);
//        }

        return;
    }

    private calculateDataCountResult calculateDataCount(short[] data, CompressMode mode, int maxPackageSize) {
        calculateDataCountResult result = new calculateDataCountResult();
        int[] hist = new int[HIST_SIZE];
        int headerSize = 0;
        int dataSize;
        int growthBits = 0;
        short value;
        boolean progress;

        switch (mode) {
            case GROWTHS:
                System.out.println("\n\n\nGROWTHS MODE CALCULATE:");
                headerSize = 28;
                value = data[0];

                for (int i = 1; i < data.length; i++) {
                    progress = false;
                    hist[getBitsForGrowth(data[i] - value)-1]++;
                    growthBits = getMaxBits(hist) +1; // +1 na znak

                    System.out.print("\nhist = " + Arrays.toString(hist));
                    System.out.print("\n  growthBits = " + growthBits);

                    dataSize = growthBits * i;
                    if (headerSize + dataSize < maxPackageSize) {
                        result.growthBitsNum = growthBits;
                        result.dataCount = i;
                        result.packageSize = headerSize + dataSize;
                        progress = true;
                    }
                    value = data[i];
                    if(!progress) break;
                }

                return result;

            case MIXED:
                System.out.println("\n\n\nMIXED MODE CALCULATE:");
                headerSize = 28;
                value = data[0];
                for (int i = 1; i < data.length; i++) {
                    progress = false;
                    System.out.println("\n\ni = " + i);
//                    System.out.println("\ndata[i] = " + data[i] +  "   value = " + value);
//                    System.out.println("getBitsForGrowth(data[i] - value) = " + getBitsForGrowth(data[i] - value));
                    hist[getBitsForGrowth(data[i] - value)-1]++;

                    System.out.print("hist = " + Arrays.toString(hist));

                    for (int j = 0; j < HIST_SIZE; j++) {
                        int constantValues = getValuesCountAbove(hist, j);
                        int growths = i - constantValues;

                        growthBits  = j +1 +1; // +1 bo dla j=0 zajmuje 1 bit, +1 na znak

                        dataSize = constantValues * (10 + 1) // +1 bo jeden bit na typ wartości
                                + growths * (growthBits +1); // +1 bo jeden bit na typ wartości

                        System.out.print("\n  j = " + j);
                        System.out.print("  constantValues = " + constantValues);
                        System.out.print("  growths = " + growths);
                        System.out.print("  size = " + dataSize);

                        if (i > result.dataCount && dataSize + headerSize <= maxPackageSize) {
                            System.out.print("\n\t\t\tNowy max = " + i);
                            result.dataCount = i;
                            result.growthBitsNum = growthBits;
                            result.constantValuesNum = constantValues;
                            result.growthValuesNum = growths;
                            result.packageSize = headerSize + dataSize;
                            progress = true;
                        }
                    }

                    value = data[i];
                    if(!progress) break; // jeśli w tym przebiegu pętli nie udało się poprawić wyniku, to już nigdy się nie uda
                }

                return result;

            case VALUES:
                System.out.println("\n\n\nVALUES MODE CALCULATE:");
                headerSize = 15;
                result.dataCount = Math.min( ((maxPackageSize - headerSize) / 10), data.length);
                dataSize = result.dataCount * 10;
                result.packageSize = dataSize + headerSize;
                return result;
        }
        return null;
    }

    private class calculateDataCountResult{
        public int dataCount = 0;
        public int packageSize = 0;
        public int growthBitsNum = 0;
        public int growthValuesNum = 0;
        public int constantValuesNum = 0;

        public void print(){
            System.out.println("dataCount = " + dataCount);
            System.out.println("packageSize = " + packageSize);
            System.out.println("growthBitsNum = " + growthBitsNum);
            System.out.println("growthValuesNum = " + growthValuesNum);
            System.out.println("constantValuesNum = " + constantValuesNum);
        }
    }

    private static int getMaxBits(int[] hist) {
        for (int i = HIST_SIZE-1; i >= 0; i--) {
            if (hist[i] > 0) {
                return i +1; // +1 bo dla hist[0] potrzeba już jednego bitu
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
            growthShort = -growth;
        }
        for (int i = 9; i >= 0; i--) {
            int bit = (growthShort & (1 << i));
            //System.out.print(bit > 0 ? "1" : "0");
            //System.out.println("bit " + i + " = " + (growth & (1 << i)));
            if (bit > 0) { //if ((growthShort & (1 << i)) > 0) {
//                System.out.println();
//                return growthShort < 0 ? i + 1 : i;
                return i+1;
            }
        }
//        System.out.println();
        return 0;
    }

    public void compress(CompressedPackage compressedPackage, short[] data){

        if(compressedPackage.getMode() == CompressMode.GROWTHS){
            compressGrowthsMode(compressedPackage,data);
        }
        else if(compressedPackage.getMode() == CompressMode.MIXED){
            compressMixedMode(compressedPackage,data);
        }
        else if(compressedPackage.getMode() == CompressMode.VALUES){
            compressValuesMode(compressedPackage,data);
        }
    }

    private void compressGrowthsMode(CompressedPackage compressedPackage, short[] data){
        short value = data[0];

        System.out.println("\n\n\nHEADER 28 bit:");
        System.out.println("Mode: GROWTHS (bit 0-1) ");
        System.out.println("Bits for growth: " + compressedPackage.getGrowthBits() + " (bit 2-4)");
        System.out.println("Length: " + compressedPackage.getDataCount() + " (bit 5-17)");
        System.out.println("Initial value: " + value + " (bit 18-27)");

        int growth;
        for (int i = 1; i <= compressedPackage.getDataCount(); i++) {
            growth = data[i] - value;
            System.out.println("Growth: " + growth + "   (" + data[i] + ")");

            value = data[i];
        }

    }

    private void compressMixedMode(CompressedPackage compressedPackage, short[] data){
        short value = data[0];
        int maxGrowthValue = (int)Math.pow(2.0, compressedPackage.getGrowthBits()-1.0)-1;
        int minGrowthValue = -(int)Math.pow(2.0, compressedPackage.getGrowthBits()-1.0);

        System.out.println("\n\n\nHEADER 28 bit:");
        System.out.println("Mode: MIXED (bit 0-1) ");
        System.out.println("Bits for growth: " + compressedPackage.getGrowthBits() + " (bit 2-4)");
        System.out.println("Length: " + compressedPackage.getDataCount() + " (bit 5-17)");
        System.out.println("Initial value: " + value + " (bit 18-27)");

        System.out.println("\nminGrowthValue = " + minGrowthValue);
        System.out.println("maxGrowthValue = " + maxGrowthValue);

        int growth;
        for (int i = 1; i <= compressedPackage.getDataCount(); i++) {
            growth = data[i] - value;

            if(growth<=maxGrowthValue && growth>=minGrowthValue) {
                System.out.println("Growth: " + growth + "   (" + data[i] + ")");
            }
            else {
                System.out.println("Value: " + data[i]);
            }

            value = data[i];
        }

    }

    private void compressValuesMode(CompressedPackage compressedPackage, short[] data){

        System.out.println("\n\n\nHEADER 15 bit:");
        System.out.println("Mode: VALUES (bit 0-1) ");
        System.out.println("Length: " + compressedPackage.getDataCount() + " (bit 2-14)");

        for (int i = 0; i < compressedPackage.getDataCount(); i++) {
            System.out.println("Value: " + data[i]);
        }
    }

}
