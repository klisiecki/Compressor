package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
	    System.out.println(System.getProperty("user.dir"));
        try {
            short[] tab = InputParser.parseFile("data/0.in");
            CompressedPackage pckg = new CompressedPackage();
            Compressor.initializePackage(pckg, tab, 1024);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
