package jlu.evyde;

import java.io.*;
import java.math.BigInteger;
import java.util.List;
import java.util.zip.CRC32;


public class BitsOut {
    private BufferedOutputStream stream;

    private final BigInteger minusOne = new BigInteger("-1");

    private BigInteger writeLimit = minusOne;

    private BigInteger alreadyWrite = new BigInteger("0");

    private final CRC32 CRC32Code = new CRC32();

    private int buffer;

    private int pointer = 0;

    public BitsOut() {
        this(System.out);
    }

    public BitsOut(String filename) {
        this(new File(filename));
    }

    public BitsOut(String... filename) {
        this(Utils.join(filename));
    }

    public BitsOut(File file) {
        try {
            this.stream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
            this.stream = new BufferedOutputStream(System.out);
        }
    }

    public BitsOut(OutputStream outputStream) {
        this.stream = new BufferedOutputStream(outputStream);
    }

    private void writeBit(boolean b) {
        buffer <<= 1;
        if (b) {
            buffer |= 1;
        }
        this.pointer++;
        if (this.pointer == 8) {
            clearBuffer();
        }
    }

    private void writeByte(byte b) {
        try {
            // empty buffer just write it
            if (this.pointer == 0) {
                this.stream.write(b);
                return;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        // write signal bit 8 times
        for (int i = 0; i < 8; i++) {
            this.writeBit((b >>> (8 - i - 1) & 1) == 1);
        }
    }

    private void clearBuffer() {
        if (this.isReachWriteLimit()) {
            return;
        }

        if (this.pointer == 0) {
            return;
        }
        buffer <<= (8 - this.pointer);
        try {
            this.stream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.CRC32Code.update(buffer);
        this.alreadyWrite = this.alreadyWrite.add(new BigInteger("8"));
        this.pointer = 0;
        this.buffer = 0;
    }

    public void write(byte b) {
        this.writeByte((byte) (b & 0xff));
    }

    public void write(int b) {
        this.writeByte((byte) ((b >>> 24) & 0xff));
        this.writeByte((byte) ((b >>> 16) & 0xff));
        this.writeByte((byte) ((b >>> 8) & 0xff));
        this.writeByte((byte) ((b >>> 0) & 0xff));
    }

    public void write(boolean b) {
        this.writeBit(b);
    }

    public void write(int b, int n) {
        while (n > 0) {
            this.writeBit(((b >>> (n - 1)) & 1) == 1);
            n--;
        }
    }

    public void write(Boolean[] bits) {
        for (boolean b: bits) {
            this.write(b);
        }
    }

    public void write(Bits bits) {
        this.write(bits.getBitsArray());
    }

    public void flush() {
        this.clearBuffer();
        try {
            this.stream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        flush();
        try {
            this.stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(List<Bits> bitsList) {
        for (Bits b: bitsList) {
            this.write(b);
        }
    }

    public CRC32 getCRC32Code() {
        return CRC32Code;
    }

    public void setWriteLimit(BigInteger writeLimit) {
        this.writeLimit = writeLimit;
    }

    public boolean isReachWriteLimit() {
        return this.writeLimit.compareTo(minusOne) != 0 && this.alreadyWrite.compareTo(writeLimit) >= 0;
    }

    public BigInteger getAlreadyWrite() {
        return alreadyWrite;
    }
}
