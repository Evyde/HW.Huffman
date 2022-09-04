package jlu.evyde;

import java.math.BigInteger;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Bits implements Comparable<Bits> {
    private final Deque<Boolean> bits = new LinkedList<>();
    private int length;

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
                throw new NumberFormatException();
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
        assert bitsLength >= 0;
        for (int i = 0; i < bitsLength; i++) {
            boolean bit = ((bitsInInt >> (bitsLength - i - 1)) & 1) == 1;
            this.bits.add(bit);
        }
        this.length = bitsLength;
    }

    public Bits(long bitsInLong) {
        this(bitsInLong, 64);
    }

    public Bits(long bitsInLong, int bitsLength) {
        assert bitsLength >= 0;
        for (int i = 0; i < bitsLength; i++) {
            boolean bit = ((bitsInLong >> (bitsLength - i - 1)) & 1) == 1;
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

    public Deque<Boolean> getBitsQueue() {
        return new LinkedList<>(bits);
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
        if (o instanceof Bits) {
            return isEqual(this, (Bits) o);
        } else if (o instanceof BigInteger) {
            return isEqual(this, new Bits((BigInteger) o));
        } else if (o instanceof String) {
            return isEqual(this, new Bits((String) o));
        } else if (o instanceof Integer) {
            return isEqual(this, new Bits((Integer) o));
        } else if (o instanceof Byte) {
            return isEqual(this, new Bits((Byte) o));
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    private static boolean isEqual(Bits me, Bits other) {
        if (other.getLength() != me.getLength()) {
            return false;
        } else {
            return me.toString().equals(other.toString());
        }
    }

    private static boolean isLooseEqual(Bits me, Bits other) {
        boolean isEqual;
        // expand with 0 in shorter thing
        if ((other.getLength() != me.getLength())) {
            Bits shorter = other.getLength() > me.getLength() ? me : other;
            int diff = Math.abs(other.getLength() - me.getLength());
            for (int i = 0; i < diff; i++) {
                shorter.bits.addFirst(false);
            }
            isEqual = me.bits.equals(shorter.bits);
            for (int i = 0; i < diff; i++) {
                shorter.bits.removeFirst();
            }
        } else {
            isEqual = me.bits.equals(other.bits);
        }
        return isEqual;
    }

    private static int compare(Bits me, Bits other) {
        if (isEqual(me, other)) {
            return 0;
        }

        int highestBit = 0, tempBits = 0;
        Deque<Boolean> meDeque = me.getBitsQueue();
        while (!meDeque.isEmpty()) {
            if (meDeque.removeLast()) {
                highestBit += tempBits;
                tempBits = 0;
            }
            tempBits++;
        }

        tempBits = 0;

        // check other
        Deque<Boolean> otherDeque = other.getBitsQueue();
        while (!otherDeque.isEmpty()) {
            if (otherDeque.removeLast()) {
                highestBit -= tempBits;
                tempBits = 0;
            }
            tempBits++;
        }

        return highestBit > 0? 1: -1;
    }

    @Override
    public int compareTo(Bits o) {
        return compare(this, o);
    }

    public void expand(Bits b) {
        this.bits.addAll(b.bits);
        this.length += b.getLength();
    }
}
