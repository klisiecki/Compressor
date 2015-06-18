package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
	    System.out.println(System.getProperty("user.dir"));
        try {
            short[] tab = InputParser.parseFile("data/4bit.in");

            PackageHeader pckg = new PackageHeader();
            Compressor compressor = new Compressor();

            compressor.initializePackage(pckg, tab, 80);
            //compressor.compress(pckg, tab);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
