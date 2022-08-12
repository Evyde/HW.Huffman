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

        // assertEquals(bits48, bits64);
    }

}