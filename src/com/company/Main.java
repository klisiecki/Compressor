package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class Main {

    private static void decompress(Compressor compressor, String file) throws IOException {
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
        CustomBitSet bitSet =  new CustomBitSet(BitSet.valueOf(readTab));
        compressor.decompress(bitSet);
    }

    private static void compress(Compressor compressor, String inFile, String outFile) throws IOException {
        short[] tab = InputParser.parseFile(inFile);
        PackageHeader header = compressor.initializePackage(tab, 80);
        CustomBitSet result = compressor.compress(header, tab);
        FileOutputStream out = new FileOutputStream(outFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        objectOutputStream.write(result.toByteArray());
        printResult(result);
        objectOutputStream.close();
        out.close();
    }

    private static void compressRaw(Compressor compressor, String inFile, String outFile) throws IOException {
        short[] tab = InputParser.parseRawFile(inFile);
        PackageHeader header = compressor.initializePackage(tab, 80);
        CustomBitSet result = compressor.compress(header, tab);
        FileOutputStream out = new FileOutputStream(outFile);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        objectOutputStream.write(result.toByteArray());
        printResult(result);
        objectOutputStream.close();
        out.close();
    }

    private static void printResult(CustomBitSet result) {
        System.out.println();
        byte[] resultArray = result.toByteArray();
        System.out.println("result.toByteArray() = " + Arrays.toString(resultArray));
        System.out.println("result = " + result);
        result.print();
        System.out.println();
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        try {
            Compressor compressor = new Compressor();
//            compress(compressor, "data/4bit.in", "out.out");
//            decompress(compressor, "out.out");
            compressRaw(compressor, "data/m7.txt", "m7.out");
            decompress(compressor, "m7.out");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
