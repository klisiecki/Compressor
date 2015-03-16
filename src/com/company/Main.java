package com.company;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
	    System.out.println(System.getProperty("user.dir"));
        try {
            short[] tab = InputParser.parseFile("data/0.in");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
