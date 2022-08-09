package jlu.evyde;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void fillIntByteArray() {
        byte[] except = new byte[] {0x01, 0x71, 0x1E, (byte) 0xF3};
        byte[] actual = new byte[except.length];
        Utils.fillIntByteArray(0x01711EF3, actual, 0);
        assertArrayEquals(except, actual);
    }

    @Test
    public void intToByteArray() {
        byte[] except = new byte[] {0x1E, (byte) 0xF3};
        assertArrayEquals(except, Utils.intToByteArray(0x01711EF3, 2));
    }

    @Test
    public void fillLongByteArray() {
        byte[] except = new byte[] {0x01, 0x71, 0x1E, (byte) 0xF3, 0x01, 0x71, 0x1E, (byte) 0xF3};
        byte[] actual = new byte[except.length];
        BigInteger e = new BigInteger("01711EF301711EF3", 16);
        Utils.fillLongByteArray(e.longValue(), actual, 0);
        assertArrayEquals(except, actual);
    }

    @Test
    public void longToByteArray() {
        byte[] except = new byte[] {0x1E, (byte) 0xF3};
        assertArrayEquals(except, Utils.intToByteArray(0x01711EF3, 2));
    }
}