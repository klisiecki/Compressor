package com.company;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class Main {

    public static void main(String[] args) {
	    System.out.println(System.getProperty("user.dir"));
        try {
            short[] tab = InputParser.parseFile("data/4bit.in");

            Compressor compressor = new Compressor();

            PackageHeader header = compressor.initializePackage(tab, 80);
            CustomBitSet result = compressor.compress(header, tab);
            FileOutputStream out = new FileOutputStream("out.out");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
            objectOutputStream.write(result.toByteArray());
            System.out.println();
            byte[] tab2 = result.toByteArray();
            System.out.println("result.toByteArray() = " + Arrays.toString(tab2));
            CustomBitSet bitSet =  new CustomBitSet(BitSet.valueOf(tab2));
            System.out.println("result = " + result);
            System.out.println("bitSet = " + bitSet);
            result.print();
            System.out.println();
            bitSet.print();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
