package jlu.evyde;

import java.io.*;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.util.NoSuchElementException;
import java.util.zip.CRC32;

public class BitsIn {
    private InputStream stream;

    private static final int EOF = -1;

    private boolean isEOF = false;

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

    public int readUnsignedByte() {
        return this.readByte() & 0xFF;
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
        return isEOF;
    }

    private void fillBuffer() {
        try {
            if (isEOF()) {
                buffer = 0;
                this.pointer = 8;
                return;
            }
            buffer = this.stream.read();
            if (buffer == EOF) {
                buffer = 0;
                isEOF = true;
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
            isEOF = true;
            buffer = 0;
            this.pointer = 8;
        }
    }

    public void close() {
        try {
            this.stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            buffer = EOF;
            isEOF = true;
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
            isEOF = false;
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
            isEOF = false;
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

    public boolean isCRC32Generated() {
        return isCRC32Generated;
    }
}
