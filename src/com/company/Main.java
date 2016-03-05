package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class Main {

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        try {
            Compressor compressor = new Compressor();
            compress(compressor, "data/m7mini.txt", "data/out", false);
            decompress(compressor, "data/out_1");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void compress(Compressor compressor, String inFile, String outFile, Boolean isClearInput) throws IOException {
        short[] inputData = InputParser.parseFile(inFile, isClearInput);
        int compressedIndex = 0;
        int iteration = 0;
        while (compressedIndex < inputData.length - 1) {
            short[] packageData = Arrays.copyOfRange(inputData, compressedIndex, inputData.length);
            int countInPackage = compressor.initializePackage(packageData, 200);
            compressedIndex += countInPackage;
            System.out.println("compressing " + countInPackage + " data from " + compressedIndex +" index");
            CustomBitSet result = compressor.compress();
            saveData(outFile + "_"+iteration, result);
            printResult(result);
            iteration++;
        }
    }

    private static void saveData(String outFile, CustomBitSet result) throws IOException {
        FileOutputStream out = new FileOutputStream(outFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        objectOutputStream.write(result.toByteArray());
        objectOutputStream.close();
        out.close();
    }

    private static void printResult(CustomBitSet result) {
        System.out.println();
        System.out.println("result.toByteArray() = " + Arrays.toString(result.toByteArray()));
        System.out.println("result = " + result);
        result.print();
        System.out.println();
    }

    private static void decompress(Compressor compressor, String file) throws IOException {
        CustomBitSet bitSet = readCompressedFile(file);
        compressor.decompress(bitSet);
    }

    private static CustomBitSet readCompressedFile(String file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        ObjectInputStream objectInputStream = new ObjectInputStream(in);

        ArrayList<Byte> data = new ArrayList<Byte>();
        Byte b;
        while (objectInputStream.available() > 0) {
            b = objectInputStream.readByte();
            data.add(b);
        }
        objectInputStream.close();
        in.close();

        byte[] readTab = new byte[data.size()];
        for (int i = 0; i < data.size(); i++) {
            readTab[i] = data.get(i);
        }

        System.out.println();
        System.out.println("Arrays.toString(readTab) = " + Arrays.toString(readTab));
        return new CustomBitSet(BitSet.valueOf(readTab));
    }
}
