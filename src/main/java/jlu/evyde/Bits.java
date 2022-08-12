package jlu.evyde;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

public class Bits {
    private final List<Boolean> bits = new LinkedList<>();
    private final int length;

    public Bits(String bitsInString) {
        this(bitsInString, bitsInString.length());
    }

    public Bits(String bitsInString, int bitLength) {
        int l = bitsInString.length();

        // appending 0
        for (int i = 0; i < (bitLength - l); i++) {
            this.bits.add(false);
        }

        for (char ch: bitsInString.toCharArray()) {
            if (ch == '1') {
                this.bits.add(true);
            } else if (ch == '0') {
                this.bits.add(false);
            } else {
                throw new ArrayStoreException();
            }
        }
        this.length = bitLength;
    }

    public Bits(BigInteger bitsInBigInteger) {
        this(bitsInBigInteger.toString(2));
    }

    public Bits(BigInteger bitsInBigInteger, int bitsLength) {
        this(bitsInBigInteger.toString(2), bitsLength);
    }

    public Bits(byte bitsInByte) {
        this(bitsInByte, 8);
    }

    public Bits(byte bitsInByte, int bitsLength) {
        assert bitsLength <= 8 && bitsLength >= 0;
        for (int i = 0; i < bitsLength; i++) {
            boolean bit = ((bitsInByte >> (bitsLength - i - 1)) & 1) == 1;
            this.bits.add(bit);
        }
        this.length = bitsLength;
    }

    public Bits(int bitsInInt) {
        this(bitsInInt, 32);
    }

    public Bits(int bitsInInt, int bitsLength) {
        assert bitsLength <= 32 && bitsLength >= 0;
        for (int i = 0; i < bitsLength; i++) {
            boolean bit = ((bitsInInt >> (bitsLength - i - 1)) & 1) == 1;
            this.bits.add(bit);
        }
        this.length = bitsLength;
    }

    public Bits(boolean bitInBoolean) {
        this(new boolean[]{bitInBoolean});
    }

    public Bits(boolean[] bitsInBoolean) {
        this.length = bitsInBoolean.length;
        for (boolean b: bitsInBoolean) {
            this.bits.add(b);
        }
    }

    public int getLength() {
        return length;
    }

    public List<Boolean> getBitsList() {
        return bits;
    }

    public Boolean[] getBitsArray() {
        return bits.toArray(new Boolean[0]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (boolean b: this.bits) {
            if (b) {
                sb.append('1');
            } else {
                sb.append('0');
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bits bits1)) return false;
        // TODO: Fix this equal problem (or not)
        return this.bits.equals(bits1.bits);
    }

    @Override
    public int hashCode() {
        return this.bits.hashCode();
    }
}
