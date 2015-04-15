package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
	    System.out.println(System.getProperty("user.dir"));
        try {
            short[] tab = InputParser.parseFile("data/2.in");
            CompressedPackage pckg = new CompressedPackage();
            Compressor.initializePackage(pckg, tab, 1024);

            System.out.println();

            System.out.println("GROWTHS: " + Compressor.calculateDataCount(tab, CompressMode.GROWTHS, 100));
            System.out.println("MIXED: " + Compressor.calculateDataCount(tab, CompressMode.MIXED, 100));
            System.out.println("VALUES: " + Compressor.calculateDataCount(tab, CompressMode.VALUES, 100));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
