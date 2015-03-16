package com.company;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class InputParser {

    public static short[] parseFile(String fileName) throws IOException {
        BufferedReader reader = null;
        try {
            InputStream inputStream = new FileInputStream(fileName);
            InputStreamReader isr = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            reader = new BufferedReader(isr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ArrayList<Short> result = new ArrayList<Short>();

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line + " (" + parseNumber(line) +")");
            result.add(parseNumber(line));
        }

        return toShortArray(result);
    }

    private static short[] toShortArray(List<Short> list){
        short[] ret = new short[list.size()];
        for(int i = 0;i < ret.length;i++)
            ret[i] = list.get(i);
        return ret;
    }

    public static short parseNumber(String number) {
        if (number.length() != 10) {
            System.err.println("Data error, line length = " + number.length());
        }
        if (number.charAt(0) == '1') {
            number = "111111" + number;
        }
        return (short)Integer.parseInt(number, 2);
    }
}
