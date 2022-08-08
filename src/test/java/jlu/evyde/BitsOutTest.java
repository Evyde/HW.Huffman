package jlu.evyde;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class BitsOutTest {
    private final String CWD = System.getProperty("user.dir") + "/";

    @Test
    public void writeBytes() {
        byte[] expectByteArray = new byte[] {(byte) 0xff, (byte) 0xee, (byte) 0xabc, (byte) 0xaaaaa, (byte) 0xa,
                (byte) 0x01711EF3};
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        BitsOut bo = new BitsOut(actual);

        for (byte b : expectByteArray) {
            bo.write(b);
        }

        bo.close();

        assertArrayEquals(expectByteArray, actual.toByteArray());
    }

    @Test
    public void writeBits() {
        boolean[] expect = new boolean[]{ true, false, true, true };
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        BitsOut bo = new BitsOut(actual);

        for (boolean b: expect) {
            bo.write(b);
        }

        bo.close();

        assertArrayEquals(new byte[] {(byte) 0b10110000}, actual.toByteArray());
    }

    @Test
    public void writeInt() {
        int[] expectArray = new int[]{129, 127, 0, 4, 3, 1, 1, 1, 0};
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        byte[] expect = new byte[]{0, 0, 0, (byte) 129, 0, 0, 0, 127, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 3, 0, 0, 0, 1,
                0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0};
        BitsOut bo = new BitsOut(actual);

        for (int i: expectArray) {
            bo.write(i);
        }

        bo.close();

        assertArrayEquals(expect, actual.toByteArray());
    }

    @Test
    public void write3BitsInt() {
        int[] expectArray = new int[]{0b111, 0b110, 0b101, 0b011, 0b000, 0b010, 0b001, 0b001, 0b000, 0b111};
        ByteArrayOutputStream actual = new ByteArrayOutputStream();
        String expect = "11111010101100001000100100011100";
        BitsOut bo = new BitsOut(actual);

        for (int i: expectArray) {
            bo.write(i, 3);
        }

        bo.close();

        assertEquals(expect, new BigInteger(1, actual.toByteArray()).toString(2));
    }
}