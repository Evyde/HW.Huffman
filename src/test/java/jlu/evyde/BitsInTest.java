package jlu.evyde;

import org.junit.Test;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class BitsInTest {

    @Test
    public void readByte() {
        byte[] expectArray = new byte[]{0b001, 0b101, 0b111, 0b101};

        BitsIn bi1 = new BitsIn(new ByteArrayInputStream(expectArray));
        BitsIn bi2 = new BitsIn(new ByteArrayInputStream(expectArray));

        assertEquals(bi1.readByte(), bi2.readByte());
        assertEquals(bi1.readByte(), bi2.readByte());
        assertEquals(bi1.readByte(), bi2.readByte());
        assertEquals(bi1.readByte(), bi2.readByte());
    }

    @Test
    public void read32Int() throws IOException {
        int[] exceptArray = new int[]{0x01711EF3, 1, 3, 5, 7, 9};
        int[] actualArray = new int[6];
        PipedInputStream sharedInStream = new PipedInputStream();
        PipedOutputStream sharedOutStream = new PipedOutputStream();
        sharedOutStream.connect(sharedInStream);
        BitsOut bo = new BitsOut(sharedOutStream);

        for (int i = 0; i < 6; i++) {
            bo.write(exceptArray[i]);

        }

        bo.flush();

        BitsIn bi = new BitsIn(sharedInStream);

        bo.close();

        for (int i = 0; i < 6; i++) {
            actualArray[i] = bi.read();
        }

        assertArrayEquals(exceptArray, actualArray);
    }

    @Test
    public void readBit() {
        byte[] expectArray = new byte[]{0b001, 0b101, 0b111, 0b101};

        BitsIn bi1 = new BitsIn(new ByteArrayInputStream(expectArray));
        BitsIn bi2 = new BitsIn(new ByteArrayInputStream(expectArray));

        assertArrayEquals(bi2.readBits(8), bi1.readBits(8));
        assertArrayEquals(bi2.readBits(17), bi1.readBits(17));
        assertArrayEquals(bi2.readBits(6), bi1.readBits(6));
    }

    @Test
    public void resetTest() {
        byte[] expectArray = new byte[]{0b001, 0b101, 0b111, 0b101};

        BitsIn bi1 = new BitsIn(new ByteArrayInputStream(expectArray));
        BitsIn bi2 = new BitsIn(new ByteArrayInputStream(expectArray));

        assertArrayEquals(bi2.readBits(8), bi1.readBits(8));
        assertArrayEquals(bi2.readBits(17), bi1.readBits(17));
        assertArrayEquals(bi2.readBits(6), bi1.readBits(6));

        bi1.reset();
        bi2.reset();
        assertArrayEquals(bi2.readBits(8), bi1.readBits(8));
        assertArrayEquals(bi2.readBits(17), bi1.readBits(17));
        assertArrayEquals(bi2.readBits(6), bi1.readBits(6));
    }
}