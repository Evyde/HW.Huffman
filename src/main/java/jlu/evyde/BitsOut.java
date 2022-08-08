package jlu.evyde;

import java.io.*;


public class BitsOut {
    private BufferedOutputStream stream;

    private int buffer;

    private int pointer = 0;

    public BitsOut() {
        this(System.out);
    }

    public BitsOut(String filename) {
        this(new File(filename));
    }

    public BitsOut(String workingDirectory, String filename) {
        this(workingDirectory + filename);
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
            buffer += 1;
        }
        this.pointer++;
        if (this.pointer == 8) {
            clearBuffer();
        }
    }

    private void writeByte(int b) {
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
        if (this.pointer == 0) {
            return;
        }
        buffer <<= (8 - this.pointer);
        try {
            this.stream.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.pointer = 0;
        this.buffer = 0;
    }

    public void write(byte b) {
        this.writeByte(b & 0xff);
    }

    public void write(int b) {
        this.writeByte((b >>> 24) & 0xff);
        this.writeByte((b >>> 16) & 0xff);
        this.writeByte((b >>> 8) & 0xff);
        this.writeByte((b >>> 0) & 0xff);
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
}
