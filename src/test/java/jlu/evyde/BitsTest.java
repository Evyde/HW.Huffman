package jlu.evyde;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

public class BitsTest {
    @Test
    public void normalTest() {
        Bits bits48 = new Bits(new BigInteger("123401711EF3", 16), 48);
        assertEquals(48, bits48.getLength());

        Bits bits64 = new Bits(new BigInteger("123401711EF3", 16), 64);
        assertEquals(64, bits64.getLength());

        Bits bits3 = new Bits("001");
        assertEquals("001", bits3.toString());
        assertEquals(3, bits3.getLength());

        Bits bits45 = new Bits(new BigInteger("123401711EF3", 16));
        String expectString = new BigInteger("123401711EF3", 16).toString(2);
        StringBuilder actualString = new StringBuilder();

        for (boolean b: bits45.getBitsArray()) {
            if (b) {
                actualString.append('1');
            } else {
                actualString.append('0');
            }
        }

        assertEquals(expectString, actualString.toString());

        assertEquals(new Bits("1011100010001111011110011"), new BigInteger("01711EF3", 16).toString(2));

        assertNotEquals(bits64, bits45);
        assertEquals(45, bits45.getLength());
        assertEquals(64, bits64.getLength());
        assertEquals(new Bits(3), 3);
    }

    @Test
    public void equalTest() {
        Bits bits45 = new Bits(new BigInteger("123401711EF3", 16));
        Bits bits48 = new Bits(new BigInteger("123401711EF3", 16), 48);
        Bits bits64 = new Bits(new BigInteger("123401711EF3", 16), 64);

        assertNotEquals(bits45, bits48);
        assertNotEquals(bits45, bits64);
        assertNotEquals(bits48, bits64);

        assertTrue(new Bits("01100100101011").compareTo(new Bits("11100100101011")) < 0);
        assertTrue(new Bits("101100100101011").compareTo(new Bits("11100100101011")) > 0);
        assertEquals(0, new Bits("11100100101011").compareTo(new Bits("11100100101011")));

    }

    @Test
    public void model() {
        // TODO: Fix this model error and infinite loop of isLooseEqual()
        Bits bits64 = new Bits(new BigInteger("512000", 10), 64);
        Bits bitsFileLength = new Bits(new BigInteger("43840", 10), 64);

        assertEquals(64, bits64.getLength());
        assertEquals(64, bitsFileLength.getLength());

        System.out.println(new BigInteger(Bits.model(bitsFileLength, bits64).toString(), 2).toString(10));

        assertTrue(Bits.isLooseEqual(Bits.model(bitsFileLength, bits64),
                (new Bits(new BigInteger("43840", 10), 64))));
        assertFalse(Bits.isLooseEqual(Bits.model(bitsFileLength, bits64), new Bits(0, 1)));
    }
}