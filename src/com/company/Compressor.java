package com.company;

public class Compressor {
    private static final int HIST_SIZE = 10;


    public PackageHeader initializePackage(short[] data, int maxPackageSize) {
//        System.out.println(Arrays.toString(createStats(data)) + "");
//        System.out.println();
        PackageHeader packageHeader = new PackageHeader();

        AnalyzeDataResult growthsResult = analyzeDataGrowthsMode(data, maxPackageSize);
        AnalyzeDataResult mixedResult = analyzeDataMixedMode(data, maxPackageSize);
        AnalyzeDataResult valuesResult = analyzeDataValuesMode(data, maxPackageSize);

        System.out.println("\n\nGrowths mode statistics:");
        growthsResult.print();
        System.out.println("\nMixed mode statistics:");
        mixedResult.print();
        System.out.println("\nValues mode statistics:");
        valuesResult.print();

        // Tylko tymczasowo:
//        packageHeader.initialize(CompressMode.GROWTHS, growthsResult.dataCount, growthsResult.growthBits);
//        compress(packageHeader, data);
        //       packageHeader.initialize(CompressMode.MIXED, mixedResult.dataCount, mixedResult.growthBits);
//        compress(packageHeader, data);
//        packageHeader.initialize(CompressMode.VALUES, valuesResult.dataCount, valuesResult.growthBits);
//        compress(packageHeader, data);

        // Potem to odkomentować:
        if (growthsResult.dataCount + 1 >= mixedResult.dataCount + 1
                && growthsResult.dataCount + 1 >= valuesResult.dataCount) { // growths mode
            packageHeader.initialize(CompressMode.GROWTHS, growthsResult.dataCount, growthsResult.growthBits);
        } else if (mixedResult.dataCount + 1 >= valuesResult.dataCount) { // mixed mode
            packageHeader.initialize(CompressMode.MIXED, mixedResult.dataCount, mixedResult.growthBits);
        } else { // values mode
            packageHeader.initialize(CompressMode.VALUES, valuesResult.dataCount, valuesResult.growthBits);
        }

        return packageHeader;
    }


    private AnalyzeDataResult analyzeDataGrowthsMode(short[] data, int maxPackageSize) {
//        System.out.println("\n\n\nGROWTHS MODE CALCULATE:");

        AnalyzeDataResult result = new AnalyzeDataResult();
        int[] hist = new int[HIST_SIZE];
        int headerSize = 28;
        int dataSize = 0;
        int growthBits = 0;
        int value = data[0];
        boolean progress;

        for (int i = 1; i < data.length; i++) { // pętla od 1, bo 0 trafia do nagłówka paczki
            progress = false;
            hist[getBitsForGrowth(data[i] - value) - 1]++; // -1 żeby odwzorować [1,10] na tablicę [0,9]
            growthBits = getMaxBits(hist) + 1; // +1 na znak

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
            if (!progress) break; // jeśli w tym przebiegu pętli nie udało się poprawić wyniku, to już nigdy się nie uda
        }

        return result;

    }


    private AnalyzeDataResult analyzeDataMixedMode(short[] data, int maxPackageSize) {
//        System.out.println("\n\n\nMIXED MODE CALCULATE:");

        AnalyzeDataResult result = new AnalyzeDataResult();
        int[] hist = new int[HIST_SIZE];
        int headerSize = 28;
        int dataSize = 0;
        int growthBits = 0;
        int value = data[0];
        boolean progress;

        for (int i = 1; i < data.length; i++) { // pętla od 1, bo 0 trafia do nagłówka paczki
            progress = false;
            hist[getBitsForGrowth(data[i] - value) - 1]++; // -1 żeby odwzorować [1,10] na tablicę [0,9]
//            System.out.println("\n\ni = " + i);
//            System.out.println("\ndata[i] = " + data[i] +  "   value = " + value);
//            System.out.println("getBitsForGrowth(data[i] - value) = " + getBitsForGrowth(data[i] - value));
//            System.out.print("hist = " + Arrays.toString(hist));

            for (int j = 0; j < HIST_SIZE; j++) {
                int constantValuesNum = getValuesCountAbove(hist, j);
                int growthsValuesNum = i - constantValuesNum;

                growthBits = j + 1 + 1; // +1 bo dla j=0 zajmuje 1 bit, +1 na znak

                dataSize = constantValuesNum * (10 + 1) // +1 bo jeden bit na typ wartości
                        + growthsValuesNum * (growthBits + 1); // +1 bo jeden bit na typ wartości

//                System.out.print("\n  j = " + j);
//                System.out.print("  constantValuesNum = " + constantValuesNum);
//                System.out.print("  growthsValuesNum = " + growthsValuesNum);
//                System.out.print("  size = " + dataSize);

                if ((i > result.dataCount) && (dataSize + headerSize <= maxPackageSize)) {
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
            if (!progress) break; // jeśli w tym przebiegu pętli nie udało się poprawić wyniku, to już nigdy się nie uda
        }

        return result;
    }

    private AnalyzeDataResult analyzeDataValuesMode(short[] data, int maxPackageSize) {
//        System.out.println("\n\n\nVALUES MODE CALCULATE:");

        AnalyzeDataResult result = new AnalyzeDataResult();
        int headerSize = 15;
        int dataSize = 0;

        result.dataCount = Math.min(((maxPackageSize - headerSize) / 10), data.length);
        dataSize = result.dataCount * 10;
        result.packageSize = dataSize + headerSize;
        return result;
    }

    private class AnalyzeDataResult {
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
        for (int i = HIST_SIZE - 1; i >= 0; i--) {
            if (hist[i] > 0) {
                return i + 1; // +1 bo dla hist[0] potrzeba już jednego bitu
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
            int growth = data[i] - data[i - 1];
            hist[getBitsForGrowth(growth)]++;
        }

        return hist;
    }

    private static int getBitsForGrowth(int growth) {
//        System.out.print("calculating bits for " + growth + " ");
        if (growth > Short.MAX_VALUE || growth < Short.MIN_VALUE) {
            throw new RuntimeException("Growth too big");
        }
        short growthShort = (short) growth;
        if (growthShort < 0) {
            growthShort = (short) ~growth;
        }
        for (int i = 9; i >= 0; i--) {
            int bit = (growthShort & (1 << i));
            //System.out.print(bit > 0 ? "1" : "0");
            //System.out.println("bit " + i + " = " + (growth & (1 << i)));
            if (bit > 0) { //if ((growthShort & (1 << i)) > 0) {
//                System.out.println();
//                return growthShort < 0 ? i + 1 : i;
                return i + 2;
            }
        }
//        System.out.println();
        return 1;
    }

    public CustomBitSet compress(PackageHeader packageHeader, short[] data) {

        if (packageHeader.getMode() == CompressMode.GROWTHS) {
            return compressGrowthsMode(packageHeader, data);
        } else if (packageHeader.getMode() == CompressMode.MIXED) {
            return compressMixedMode(packageHeader, data);
        } else if (packageHeader.getMode() == CompressMode.VALUES) {
            return compressValuesMode(packageHeader, data);
        }
        return null;
    }

    private CustomBitSet compressGrowthsMode(PackageHeader packageHeader, short[] data) {
        short value = data[0];

        System.out.println("\n\nMode: GROWTHS (bit 0-1) ");
        System.out.println("Bits for growth: " + packageHeader.getGrowthBits() + " (bit 2-5)");
        System.out.println("Length: " + packageHeader.getDataCount() + " (bit 6-18)");
        System.out.println("Initial value: " + value + " (bit 19-28)");

        short growth;
        for (int i = 1; i <= packageHeader.getDataCount(); i++) {
            growth = (short) (data[i] - value);
            System.out.println("Growth: " + growth + "   (" + data[i] + ")");

            value = data[i];
        }

        short growths = (short) packageHeader.getGrowthBits();
        short dataCount = (short) packageHeader.getDataCount();

        CustomBitSet result = new CustomBitSet();
        result.clear();
        result.set(0, true);
        result.set(1, false);

        result.add(2, 5, growths);
        result.add(6, 18, dataCount);
        result.add(19, 28, data[0]);

        int dataBits = packageHeader.getGrowthBits();
        value = data[0];
        int i;
        for (i = 1; i <= packageHeader.getDataCount(); i++) {
            growth = (short) (data[i] - value);
            int bit = (i - 1) * dataBits + 29;
            System.out.println("bit = " + bit);
            result.addConversed(bit, bit + dataBits, growth);
            value = data[i];
        }

        result.print();
        return result;
    }


    private CustomBitSet compressMixedMode(PackageHeader packageHeader, short[] data) {
        short value = data[0];
        int maxGrowthValue = (int) Math.pow(2.0, packageHeader.getGrowthBits() - 1.0) - 1;
        int minGrowthValue = -(int) Math.pow(2.0, packageHeader.getGrowthBits() - 1.0);

        System.out.println("\n\nMode: MIXED (bit 0-1) ");
        System.out.println("Bits for growth: " + packageHeader.getGrowthBits() + " (bit 2-5)   minGrowthValue = " + minGrowthValue + "   maxGrowthValue = " + maxGrowthValue);
        System.out.println("Length: " + packageHeader.getDataCount() + " (bit 6-18)");
        System.out.println("Initial value: " + value + " (bit 19-28)");

        short growth;
        for (int i = 1; i <= packageHeader.getDataCount(); i++) {
            growth = (short) (data[i] - value);

            if (growth <= maxGrowthValue && growth >= minGrowthValue) {
                System.out.println("Growth: " + growth + "   (" + data[i] + ")");
            } else {
                System.out.println("Value: " + data[i]);
            }

            value = data[i];
        }

        short growths = (short) packageHeader.getGrowthBits();
        short dataCount = (short) packageHeader.getDataCount();

        CustomBitSet result = new CustomBitSet();
        result.clear();
        result.set(0, true);
        result.set(1, true);

        result.add(2, 5, growths);
        result.add(6, 18, dataCount);
        result.add(19, 28, data[0]);

        int dataBits = packageHeader.getGrowthBits() + 1;
        value = data[0];
        int bit = 29;
        for (int i = 1; i <= packageHeader.getDataCount(); i++) {
            growth = (short) (data[i] - value);
            if (getBitsForGrowth(growth) > packageHeader.getGrowthBits()) {
                result.clear(bit); //wartosc bezwzględna
                result.add(bit + 1, bit + 10, data[i]);
                bit += 10 + 1;
            } else {
                result.set(bit);
                result.addConversed(bit + 1, bit + dataBits + 1, growth);
                bit += dataBits;
            }
            value = data[i];
        }

        result.print();
        return result;
    }


    private CustomBitSet compressValuesMode(PackageHeader packageHeader, short[] data) {
        System.out.println("\n\nMode: VALUES (bit 0-1) ");
        System.out.println("Length: " + packageHeader.getDataCount() + " (bit 2-14)");

        for (int i = 0; i < packageHeader.getDataCount(); i++) {
            System.out.println("Value: " + data[i]);
        }

        short dataCount = (short) packageHeader.getDataCount();

        CustomBitSet result = new CustomBitSet();
        result.clear();
        result.set(0, false);
        result.set(1, true);

        result.add(2, 14, dataCount);

        int dataBits = 10;
        for (int i = 0; i < packageHeader.getDataCount(); i++) {
            int bit = (i) * dataBits + 15;
            result.add(bit, bit + dataBits - 1, data[i]);
        }

        result.print();
        return result;
    }

    public short[] decompress(CustomBitSet bitSet) {
        boolean b0 = bitSet.get(0);
        boolean b1 = bitSet.get(1);
        System.out.println("decompressing: ");
        bitSet.print();
        if (b0 && !b1) {
            System.out.println("growths");
            return decompressGrowthsMode(bitSet);
        } else if (b0 && b1) {
            System.out.println("mixed");
            decompressMixedMode(bitSet);
        } else if (!b0 && b1) {
            System.out.println("values");
            return decompressValuesMode(bitSet);
        } else {
            throw new RuntimeException("Data header error");
        }
        return null;
    }

    private short[] decompressGrowthsMode(CustomBitSet bitSet) {
        short bitsForGrowth = bitSet.getShort(2, 5);
        System.out.println("bitsForGrowth = " + bitsForGrowth);
        short dataCount = bitSet.getShort(6, 18);
        System.out.println("dataCount = " + dataCount);
        short value = bitSet.getShort(19, 28);
        System.out.println("value = " + value);
        short[] tab = new short[dataCount + 1];
        tab[0] = value;
        for (int i = 1; i <= dataCount; i++) {
            short growth = bitSet.getConversed(29 + (i - 1) * bitsForGrowth, 29 + i * bitsForGrowth - 1);
            System.out.println("growth = " + growth);
            tab[i] = (short) (tab[i - 1] + growth);
            System.out.println("tab[i] = " + tab[i]);
        }
        return tab;
    }

    private short[] decompressMixedMode(CustomBitSet bitSet) {
        short bitsForGrowth = bitSet.getShort(2, 5);
        System.out.println("bitsForGrowth = " + bitsForGrowth);
        short dataCount = bitSet.getShort(6, 18);
        System.out.println("dataCount = " + dataCount);
        short value = bitSet.getShort(19, 28);
        System.out.println("value = " + value);
        short[] tab = new short[dataCount + 1];
        tab[0] = value;
        int bit = 29;
        for (int i = 1; i <= dataCount; i++) {

            boolean type = bitSet.get(bit);
            if (!type) {
                //wartosc bezwzgledna
                tab[i] = bitSet.getShort(bit + 1, bit + 10);
                System.out.println("tab[i] = " + tab[i]);
                bit += 10 + 1;

            } else {
                //przyrost
                tab[i] = (short) (tab[i - 1] + bitSet.getConversed(bit + 1, bit + bitsForGrowth));
                new CustomBitSet(bitSet.get(bit + 1, bit + bitsForGrowth)).print();
                System.out.println();
                System.out.println("g = " + bitSet.getConversed(bit + 1, bit + bitsForGrowth));
                System.out.println("tab[i[ = " + tab[i]);
                bit += bitsForGrowth + 1;
            }
        }
        return tab;
    }

    private short[] decompressValuesMode(CustomBitSet bitSet) {
        short dataCount = bitSet.getShort(2, 14);
        System.out.println("dataCount = " + dataCount);
        short[] tab = new short[dataCount];
        for (int i = 0; i < dataCount; i++) {
            tab[i] = bitSet.getShort(15 + i * 10, 15 + i * 10 + 9);
            System.out.println("tab[i] = " + tab[i]);
        }

        return tab;
    }

}
