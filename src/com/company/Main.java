package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("Compressing: inputFile outputFile packageSize");
            System.out.println("Decompressing: -d outputFile");
            return;
        }
        try {
            Compressor compressor = new Compressor();
            if ("-d".equals(args[0])) {
                decompress(compressor, args[1]);
            } else {
                compress(compressor, args[0], args[1], Integer.parseInt(args[2]), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void compress(Compressor compressor, String inFile, String outFile, int packageSize, Boolean isClearInput) throws IOException {
        System.out.println("Compressing " + inFile + " to " + outFile + " with " + packageSize + " package sieze");
        short[] inputData = InputParser.parseFile(inFile, isClearInput);
        int compressedIndex = 0;
        int iteration = 0;
        while (compressedIndex < inputData.length - 1) {
            short[] packageData = Arrays.copyOfRange(inputData, compressedIndex, inputData.length);
            int countInPackage = compressor.initializePackage(packageData, packageSize);
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
        System.out.println("Decompressing " + file);
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
