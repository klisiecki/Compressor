package com.company;

public class Compressor {
    private static final int HIST_SIZE = 10;


    public void initializePackage(CompressedPackage compressedPackage, short[] data, int maxPackageSize) {
//        System.out.println(Arrays.toString(createStats(data)) + "");
//        System.out.println();

        analyzeDataResult growthsResult = analyzeDataGrowthsMode(data, maxPackageSize);
        analyzeDataResult mixedResult = analyzeDataMixedMode(data, maxPackageSize);
        analyzeDataResult valuesResult = analyzeDataValuesMode(data, maxPackageSize);

        System.out.println("\n\nGrowths mode statistics:");
        growthsResult.print();
        System.out.println("\nMixed mode statistics:");
        mixedResult.print();
        System.out.println("\nValues mode statistics:");
        valuesResult.print();

        // Tylko tymczasowo:
        compressedPackage.initialize(CompressMode.GROWTHS, growthsResult.dataCount, growthsResult.growthBits);
        compress(compressedPackage, data);
        compressedPackage.initialize(CompressMode.MIXED, mixedResult.dataCount, mixedResult.growthBits);
        compress(compressedPackage, data);
        compressedPackage.initialize(CompressMode.VALUES, valuesResult.dataCount, valuesResult.growthBits);
        compress(compressedPackage, data);

        // Potem to odkomentować:
//        if(growthsResult.dataCount >= mixedResult.dataCount
//                && growthsResult.dataCount >= valuesResult.dataCount){ // growths mode
//            compressedPackage.initialize(CompressMode.GROWTHS, growthsResult.dataCount, growthsResult.growthBits);
//        }
//        else if(mixedResult.dataCount >= valuesResult.dataCount){ // mixed mode
//            compressedPackage.initialize(CompressMode.MIXED, mixedResult.dataCount, mixedResult.growthBits);
//        }
//        else{ // values mode
//            compressedPackage.initialize(CompressMode.VALUES, valuesResult.dataCount, valuesResult.growthBits);
//        }

        return;
    }


    private analyzeDataResult analyzeDataGrowthsMode(short[] data, int maxPackageSize){
//        System.out.println("\n\n\nGROWTHS MODE CALCULATE:");

        analyzeDataResult result = new analyzeDataResult();
        int[] hist = new int[HIST_SIZE];
        int headerSize = 28;
        int dataSize = 0;
        int growthBits = 0;
        int value = data[0];
        boolean progress;

        for (int i = 1; i < data.length; i++) { // pętla od 1, bo 0 trafia do nagłówka paczki
            progress = false;
            hist[getBitsForGrowth(data[i] - value)-1]++; // -1 żeby odwzorować [1,10] na tablicę [0,9]
            growthBits = getMaxBits(hist) +1; // +1 na znak

//            System.out.print("\nhist = " + Arrays.toString(hist));
//            System.out.print("\n  growthBits = " + growthBits);

            dataSize = growthBits * i;
            if (headerSize + dataSize <= maxPackageSize) {
                result.growthBits = growthBits;
                result.dataCount = i;
                result.packageSize = headerSize + dataSize;
                progress = true;
            }
            value = data[i];
            if(!progress) break; // jeśli w tym przebiegu pętli nie udało się poprawić wyniku, to już nigdy się nie uda
        }

        return result;

    }


    private analyzeDataResult analyzeDataMixedMode(short[] data, int maxPackageSize) {
//        System.out.println("\n\n\nMIXED MODE CALCULATE:");

        analyzeDataResult result = new analyzeDataResult();
        int[] hist = new int[HIST_SIZE];
        int headerSize = 28;
        int dataSize = 0;
        int growthBits = 0;
        int value = data[0];
        boolean progress;

        for (int i = 1; i < data.length; i++) { // pętla od 1, bo 0 trafia do nagłówka paczki
            progress = false;
            hist[getBitsForGrowth(data[i] - value)-1]++; // -1 żeby odwzorować [1,10] na tablicę [0,9]
//            System.out.println("\n\ni = " + i);
//            System.out.println("\ndata[i] = " + data[i] +  "   value = " + value);
//            System.out.println("getBitsForGrowth(data[i] - value) = " + getBitsForGrowth(data[i] - value));
//            System.out.print("hist = " + Arrays.toString(hist));

            for (int j = 0; j < HIST_SIZE; j++) {
                int constantValuesNum = getValuesCountAbove(hist, j);
                int growthsValuesNum = i - constantValuesNum;

                growthBits  = j +1 +1; // +1 bo dla j=0 zajmuje 1 bit, +1 na znak

                dataSize = constantValuesNum * (10 +1) // +1 bo jeden bit na typ wartości
                        + growthsValuesNum * (growthBits +1); // +1 bo jeden bit na typ wartości

//                System.out.print("\n  j = " + j);
//                System.out.print("  constantValuesNum = " + constantValuesNum);
//                System.out.print("  growthsValuesNum = " + growthsValuesNum);
//                System.out.print("  size = " + dataSize);

                if ( (i > result.dataCount) && (dataSize + headerSize <= maxPackageSize) ) {
//                    System.out.print("\n\t\t\tNowy max = " + i);
                    result.dataCount = i;
                    result.growthBits = growthBits;
                    result.constantValuesNum = constantValuesNum;
                    result.growthsValuesNum = growthsValuesNum;
                    result.packageSize = headerSize + dataSize;
                    progress = true;
                }
            }

            value = data[i];
            if(!progress) break; // jeśli w tym przebiegu pętli nie udało się poprawić wyniku, to już nigdy się nie uda
        }

        return result;
    }

    private analyzeDataResult analyzeDataValuesMode(short[] data, int maxPackageSize) {
//        System.out.println("\n\n\nVALUES MODE CALCULATE:");

        analyzeDataResult result = new analyzeDataResult();
        int headerSize = 15;
        int dataSize = 0;

        result.dataCount = Math.min( ((maxPackageSize - headerSize) / 10), data.length);
        dataSize = result.dataCount * 10;
        result.packageSize = dataSize + headerSize;
        return result;
    }

    private class analyzeDataResult {
        public int dataCount = 0;
        public int packageSize = 0;
        public int growthBits = 0;
        public int growthsValuesNum = 0;
        public int constantValuesNum = 0;

        public void print() {
            System.out.println("dataCount = " + dataCount);
            System.out.println("packageSize = " + packageSize);
            System.out.println("growthBits = " + growthBits);
            System.out.println("growthsValuesNum = " + growthsValuesNum);
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
                return i+2;
            }
        }
//        System.out.println();
        return 1;
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

        System.out.println("\n\nMode: GROWTHS (bit 0-1) ");
        System.out.println("Bits for growth: " + compressedPackage.getGrowthBits() + " (bit 2-4)");
        System.out.println("Length: " + compressedPackage.getDataCount() + " (bit 5-17)");
        System.out.println("Initial value: " + value + " (bit 18-27)");

        short growth;
        for (int i = 1; i <= compressedPackage.getDataCount(); i++) {
            growth = (short) (data[i] - value);
            System.out.println("Growth: " + growth + "   (" + data[i] + ")");

            value = data[i];
        }

        short growths = (short) compressedPackage.getGrowthBits();
        short dataCount = (short) compressedPackage.getDataCount();

        CustomBitSet result = new CustomBitSet();
        result.clear();
        result.set(1,true);
        result.set(0,false);

        result.add(2, 4, growths);
        result.add(5, 17, dataCount);
        result.add(18, 27, data[0]);

        int dataBits = compressedPackage.getGrowthBits();
        value = data[0];
        int i;
        for (i = 1; i <= compressedPackage.getDataCount(); i++) {
            growth = (short) (data[i] - value);
            int bit = (i-1) * dataBits + 28;
            System.out.println("bit = " + bit);
            result.addConversed(bit, bit + dataBits, growth);
            value = data[i];
        }


        System.out.println("result: " + result);
        result.print();
    }



    private void compressMixedMode(CompressedPackage compressedPackage, short[] data){
        short value = data[0];
        int maxGrowthValue = (int)Math.pow(2.0, compressedPackage.getGrowthBits()-1.0)-1;
        int minGrowthValue = -(int)Math.pow(2.0, compressedPackage.getGrowthBits()-1.0);

        System.out.println("\n\nMode: MIXED (bit 0-1) ");
        System.out.println("Bits for growth: " + compressedPackage.getGrowthBits() + " (bit 2-4)   minGrowthValue = " + minGrowthValue + "   maxGrowthValue = " + maxGrowthValue);
        System.out.println("Length: " + compressedPackage.getDataCount() + " (bit 5-17)");
        System.out.println("Initial value: " + value + " (bit 18-27)");

        short growth;
        for (int i = 1; i <= compressedPackage.getDataCount(); i++) {
            growth = (short) (data[i] - value);

            if(growth<=maxGrowthValue && growth>=minGrowthValue) {
                System.out.println("Growth: " + growth + "   (" + data[i] + ")");
            }
            else {
                System.out.println("Value: " + data[i]);
            }

            value = data[i];
        }

        short growths = (short) compressedPackage.getGrowthBits();
        short dataCount = (short) compressedPackage.getDataCount();

        CustomBitSet result = new CustomBitSet();
        result.clear();
        result.set(1,true);
        result.set(1,false);

        result.add(2, 4, growths);
        result.add(5, 17, dataCount);
        result.add(18, 27, data[0]);

        int dataBits = compressedPackage.getGrowthBits() + 1;
        value = data[0];

        for (int i = 1; i <= compressedPackage.getDataCount(); i++) {
            growth = (short) (data[i] - value);
            int bit = (i-1) * dataBits + 28;
            if (getBitsForGrowth(growth) > compressedPackage.getGrowthBits()) {
                result.clear(bit); //wartosc bezwzględna
                result.addConversed(bit + 1, bit + dataBits, data[i]);
            } else {
                result.set(bit);
                result.addConversed(bit + 1, bit + dataBits + 1, growth);
                value = data[i];
            }
        }

        System.out.println("result: " + result);
        result.print();
    }


    private void compressValuesMode(CompressedPackage compressedPackage, short[] data){
        System.out.println("\n\nMode: VALUES (bit 0-1) ");
        System.out.println("Length: " + compressedPackage.getDataCount() + " (bit 2-14)");

        for (int i = 0; i < compressedPackage.getDataCount(); i++) {
            System.out.println("Value: " + data[i]);
        }

        short dataCount = (short) compressedPackage.getDataCount();

        CustomBitSet result = new CustomBitSet();
        result.clear();
        result.set(0,true);
        result.set(1,false);

        result.add(2, 14, dataCount);

        int dataBits = 10;
        for (int i = 1; i <= compressedPackage.getDataCount(); i++) {
            int bit = (i-1) * dataBits + 15;
            result.add(bit, bit + dataBits, data[i]);
        }

        System.out.println("result: " + result);
        result.print();
    }
}
