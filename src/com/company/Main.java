package com.company;

import com.company.external.arithmetic.ArithmeticCompress;
import com.company.external.arithmetic.BitOutputStream;
import com.company.external.arithmetic.FrequencyTable;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage:");
            System.out.println("Compressing: inputFile outputFile packageSize [-v]");
            System.out.println("Decompressing: -d outputFile [-v]");
            return;
        }
        try {
            Compressor compressor = new Compressor(args[args.length-1].equals("-v"));
            if ("-d".equals(args[0])) {
                decompress(compressor, args[1]);
            } else {
                compress(compressor, args[0], args[1], Integer.parseInt(args[2]), false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CompressorException e) {
            e.printStackTrace();
        }
    }

    private static void compress(Compressor compressor, String inFile, String outFile, int packageSize, Boolean isClearInput) throws IOException, CompressorException {
        System.out.println("Compressing " + inFile + " to " + outFile + " with " + packageSize + " package size");
        short[] inputData = InputParser.parseFile(inFile, isClearInput);
        System.out.println(inputData.length + " numbers in file");
        int compressedIndex = 0;
        int iteration = 0;
        int resultSize = 0;
        long time1 = System.currentTimeMillis();
        while (compressedIndex < inputData.length - 1) {
            short[] packageData = Arrays.copyOfRange(inputData, compressedIndex, inputData.length);
            int countInPackage = compressor.initializePackage(packageData, packageSize, null);
            compressedIndex += countInPackage;
            CustomBitSet result = compressor.compress();
            resultSize += saveData(outFile + "_" + iteration, result);
//            printResult(result);
            iteration++;
        }
        System.out.println("Compressed file bytes: " + resultSize + " in " + (System.currentTimeMillis() - time1) + "ms");
        long time2 = System.currentTimeMillis();
        long zipBytes = getZipBytes(inputData);
        System.out.println("Zip file bytes: " + zipBytes + " in " + (System.currentTimeMillis() - time2) + "ms");
        long time3 = System.currentTimeMillis();
        long bZip2Bytes = getBZip2Bytes(inputData);
        System.out.println("BZip2 file bytes: " + bZip2Bytes + " in " + (System.currentTimeMillis() - time3) + "ms");
        long time4 = System.currentTimeMillis();
        long rawBytes = getRawBytes(inputData);
        System.out.println("Raw file bytes: " + rawBytes + " in " + (System.currentTimeMillis() - time4) + "ms");
        long time5 = System.currentTimeMillis();
        long arithmeticBytes = getArithmeticBytes(inputData);
        System.out.println("Arythmetic file bytes: " + arithmeticBytes + " in " + (System.currentTimeMillis() - time5) + "ms");




    }

    private static long getRawBytes(short[] data) throws IOException {
        File testRaw = new File("testRaw");
        FileOutputStream fs = new FileOutputStream(testRaw);
        ObjectOutputStream oos = new ObjectOutputStream(fs);

        for (short i : data) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.putShort(i);
            oos.write(buffer.array());
        }

        oos.close();
        fs.close();

        long result = testRaw.length();
//        testRaw.delete();
        return result;
    }

    private static long getZipBytes(short[] data) throws IOException {
        File testFile = new File("test.zip");
        FileOutputStream fs = new FileOutputStream(testFile);
        ZipOutputStream oos = new ZipOutputStream(fs);
        ZipEntry zipEntry = new ZipEntry("test");
        oos.putNextEntry(zipEntry);

        for (short i : data) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.putShort(i);
            oos.write(buffer.array());
        }

        oos.close();
        fs.close();

        long result = testFile.length();
//        testFile.delete();
        return result;
    }

    public static long getArithmeticBytes(short[] data) throws IOException {
        Compressor compressor = new Compressor(false);
        compressor.initializePackage(data, -1, CompressMode.VALUES);
        saveData("savedValuesMode", compressor.compress());
        // Otherwise, compress
        File inputFile = new File("savedValuesMode");
        File outputFile = new File("testArithmetic");

        // Read input file once to compute symbol frequencies
        FrequencyTable freq = ArithmeticCompress.getFrequencies(inputFile);
        freq.increment(256);  // EOF symbol gets a frequency of 1

        // Read input file again, compress with arithmetic coding, and write output file
        InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
        BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        try {
            ArithmeticCompress.writeFrequencies(out, freq);
            ArithmeticCompress.compress(freq, in, out);
        } finally {
            out.close();
            in.close();
        }
        return outputFile.length();
    }

    private static long getBZip2Bytes(short[] data) throws IOException, CompressorException {
        File testFile = new File("test.bzip2");
        FileOutputStream fs = new FileOutputStream(testFile);
        CompressorOutputStream oos = new CompressorStreamFactory()
                .createCompressorOutputStream(CompressorStreamFactory.GZIP, fs);

        for (short i : data) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.putShort(i);
            oos.write(buffer.array());
        }

        oos.close();
        fs.close();

        long result = testFile.length();
        testFile.delete();
        return result;
    }

    private static long saveData(String outFile, CustomBitSet result) throws IOException {
        File f = new File(outFile);
        FileOutputStream out = new FileOutputStream(f);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(out);
        objectOutputStream.write(result.toByteArray());
        objectOutputStream.close();
        out.close();
        return f.length();
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
