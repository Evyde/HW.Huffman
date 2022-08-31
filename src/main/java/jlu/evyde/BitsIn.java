package jlu.evyde;

import java.io.*;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.util.NoSuchElementException;
import java.util.zip.CRC32;

public class BitsIn {
    private InputStream stream;

    private static final int EOF = -1;

    private final CRC32 CRC32Code = new CRC32();

    private int buffer;

    private int pointer = 0;

    private BigInteger totalContentLength = new BigInteger("0");

    private BigInteger tempContentLength = new BigInteger("0");
    private boolean isCRC32Generated = false;

    public BitsIn() {
        this(System.in);
    }

    public BitsIn(String filename) {
        this(new File(filename));
    }

    public BitsIn(String... filename) {
        this(Utils.join(filename));
    }

    public BitsIn(File file) {
        try {
            // this.contentLength = new BigInteger(String.valueOf(file.length() * 8));
            file.setReadable(true);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            this.totalContentLength = new BigInteger(String.valueOf(file.length())).multiply(new BigInteger("8"));
            this.stream = new Utils.RandomAccessFileStream(randomAccessFile);
            fillBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BitsIn(InputStream inputStream) {
        this.stream = new BufferedInputStream(inputStream);
        this.stream.mark(Integer.MAX_VALUE);
        fillBuffer();
    }

    public boolean readBit() {
        if (isEOF()) {
            return false;
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

    public int readInt() {
        // read an 32-bit int
        return readInt(32);
    }

    @Deprecated
    public Bits readBits(int n) {
        return new Bits(readBooleanBits(n));
    }

    public boolean[] readBooleanBits(int n) {
        boolean[] returnBits = new boolean[n];
        for (int i = 0; i < n; i++) {
            returnBits[i] = readBit();
        }
        return returnBits;
    }

    public Bits read(int n) {
        return readBits(n);
    }

    public int readInt(int n) {
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

    public boolean isEOF() {
        return buffer == EOF;
    }

    private void fillBuffer() {
        try {
            buffer = this.stream.read();
            if (buffer == EOF) {
                this.pointer = -1;
            } else {
                this.tempContentLength = this.tempContentLength.add(new BigInteger("8"));
                if (this.tempContentLength.compareTo(totalContentLength) >= 0) {
                    this.totalContentLength = this.tempContentLength.add(new BigInteger("0"));
                }
                this.pointer = 8;
                if (!isCRC32Generated) {
                    this.CRC32Code.update(buffer);
                }
            }
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

    @Deprecated
    public void reset() {
        softReset();
    }

    public void softReset() {
        try {
            this.stream.reset();
            buffer = 0;
            this.pointer = 8;
            this.tempContentLength = new BigInteger("0");
            this.isCRC32Generated = true;
            fillBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void hardReset() {
        try {
            this.stream.reset();
            buffer = 0;
            this.pointer = 8;
            this.tempContentLength = new BigInteger("0");
            this.totalContentLength = new BigInteger("0");
            this.CRC32Code.reset();
            this.isCRC32Generated = false;
            fillBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bits getContentLength() {
        return new Bits(this.totalContentLength);
    }

    public CRC32 getCRC32Code() {
        return CRC32Code;
    }
}
