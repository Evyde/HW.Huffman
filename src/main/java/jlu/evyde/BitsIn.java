package jlu.evyde;

import java.io.*;
import java.util.NoSuchElementException;

public class BitsIn {
    private BufferedInputStream stream;

    private static final int EOF = -1;

    private int buffer;

    private int pointer = 0;

    public BitsIn() {
        this(System.in);
    }

    public BitsIn(String filename) {
        this(new File(filename));
    }

    public BitsIn(String workingDirectory, String filename) {
        this(workingDirectory + filename);
    }

    public BitsIn(File file) {
        try {
            this.stream = new BufferedInputStream(new FileInputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            this.stream = new BufferedInputStream(System.in);
        }
    }

    public BitsIn(InputStream inputStream) {
        this.stream = new BufferedInputStream(inputStream);
        fillBuffer();
    }

    private boolean readBit() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        this.pointer--;
        boolean bit = ((buffer >> this.pointer) & 1) == 1;
        if (this.pointer == 0) {
            fillBuffer();
        }
        return bit;
    }

    public byte readByte() {
        byte returnByte = 0;
        for (int i = 0; i < 8; i++) {
            returnByte <<= 1;
            if (readBit()) {
                returnByte |= 1;
            }
        }
        return returnByte;
    }

    public int read() {
        // read an 32-bit int
        return read(32);
    }

    public boolean[] readBits(int n) {
        boolean[] returnBits = new boolean[n];
        for (int i = 0; i < n; i++) {
            returnBits[i] = readBit();
        }
        return returnBits;
    }

    public int read(int n) {
        int returnInt = 0;
        for (int i = 0; i < n; i++) {
            returnInt <<= 1;
            boolean bit = readBit();
            if (bit) {
                returnInt |= 1;
            }
        }
        return returnInt;
    }

    private boolean isEmpty() {
        return buffer == EOF;
    }

    private void fillBuffer() {
        try {
            buffer = this.stream.read();
            this.pointer = 8;
        } catch (IOException ie) {
            ie.printStackTrace();
            buffer = EOF;
            this.pointer = -1;
        }
    }

    public void close() {
        try {
            this.stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            buffer = EOF;
            this.pointer = EOF;
        }
    }

}
