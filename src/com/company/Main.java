package com.company;

import java.io.*;
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
            System.out.println("result = " + result);
            result.print();
            System.out.println();
            objectOutputStream.close();
            out.close();

            FileInputStream in = new FileInputStream("out.out");
            ObjectInputStream objectInputStream = new ObjectInputStream(in);

            ArrayList<Byte> data = new ArrayList<Byte>();
            Byte b;
            try {
                while ((b = objectInputStream.readByte()) != null) {
                    data.add(b);
                }
            } catch (EOFException e) {
                objectInputStream.close();
                in.close();
            }
            byte[] readTab = new byte[data.size()];
            for (int i = 0; i < data.size(); i++) {
                readTab[i] = data.get(i);
            }

            System.out.println();
            System.out.println("Arrays.toString(readTab) = " + Arrays.toString(readTab));
            CustomBitSet bitSet =  new CustomBitSet(BitSet.valueOf(readTab));
            compressor.decompress(bitSet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
