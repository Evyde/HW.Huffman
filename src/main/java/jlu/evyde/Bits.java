package jlu.evyde;

import java.math.BigInteger;
import java.util.*;

public class Bits implements Comparable<Bits> {
    private final Deque<Boolean> bits = new LinkedList<>(); // new ArrayDeque<>(256);
    private int length;
    private final StringBuilder bitsString = new StringBuilder();

    private String cachedBitsString = "";

    private int zeros = 0;
    private int ones = 0;

    public Bits() {
        this("", 0);
    }

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
                this.bitsString.append(ch);
                this.ones += 1;
            } else if (ch == '0') {
                this.bits.add(false);
                this.bitsString.append(ch);
                this.zeros += 1;
            } else {
                throw new NumberFormatException();
            }
        }
        this.length = bitLength;
        this.cachedBitsString = this.bitsString.toString();
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
            if (bit) {
                this.bitsString.append('1');
                this.ones += 1;
            } else {
                this.bitsString.append('0');
                this.zeros += 1;
            }
        }
        this.length = bitsLength;
        this.cachedBitsString = this.bitsString.toString();
    }

    public Bits(int bitsInInt) {
        this(bitsInInt, 32);
    }

    public Bits(int bitsInInt, int bitsLength) {
        assert bitsLength >= 0;
        for (int i = 0; i < bitsLength; i++) {
            boolean bit = ((bitsInInt >> (bitsLength - i - 1)) & 1) == 1;
            this.bits.add(bit);
            if (bit) {
                this.bitsString.append('1');
                this.ones += 1;
            } else {
                this.bitsString.append('0');
                this.zeros += 1;
            }
        }
        this.length = bitsLength;
        this.cachedBitsString = this.bitsString.toString();
    }

    public Bits(long bitsInLong) {
        this(bitsInLong, 64);
    }

    public Bits(long bitsInLong, int bitsLength) {
        assert bitsLength >= 0;
        for (int i = 0; i < bitsLength; i++) {
            boolean bit = ((bitsInLong >> (bitsLength - i - 1)) & 1) == 1;
            this.bits.add(bit);
            if (bit) {
                this.bitsString.append('1');
                this.ones += 1;
            } else {
                this.bitsString.append('0');
                this.zeros += 1;
            }
        }
        this.length = bitsLength;
        this.cachedBitsString = this.bitsString.toString();
    }

    public Bits(boolean bitInBoolean) {
        this(new boolean[]{bitInBoolean});
    }

    public Bits(boolean[] bitsInBoolean) {
        this.length = bitsInBoolean.length;
        for (boolean b: bitsInBoolean) {
            this.bits.add(b);
            if (b) {
                this.bitsString.append('1');
                this.ones += 1;
            } else {
                this.bitsString.append('0');
                this.zeros += 1;
            }
        }
        this.cachedBitsString = this.bitsString.toString();
    }

    public Bits(Boolean[] bitsInBoolean) {
        this.length = bitsInBoolean.length;
        for (boolean b: bitsInBoolean) {
            this.bits.add(b);
            if (b) {
                this.bitsString.append('1');
                this.ones += 1;
            } else {
                this.bitsString.append('0');
                this.zeros += 1;
            }
        }
        this.cachedBitsString = this.bitsString.toString();
    }

    public Bits(Bits... bits) {
        this(joinBitsString(bits));
    }

    private static String joinBitsString(Bits... bits) {
        StringBuilder sb = new StringBuilder();
        for (Bits b: bits) {
            sb.append(b.toString());
        }
        return sb.toString();
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
        if (this.cachedBitsString.length() == this.getLength()) {
            return this.cachedBitsString;
        } else {
            this.cachedBitsString = this.bitsString.toString();
            return this.bitsString.toString();
        }
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
//            int randIndex = new Random().nextInt(0, me.length);
//            if (!me.bits.toArray(new Boolean[]{})[randIndex].equals(other.bits.toArray(new Boolean[]{})[randIndex])) {
//                return false;
//            }
//            if (me.getLength() >= 2) {
//                int n = me.getLength() / 10;
//                Boolean[] meFirstN = new Boolean[n];
//                Boolean[] meLastN = new Boolean[n];
//                Boolean[] otherFirstN = new Boolean[n];
//                Boolean[] otherLastN = new Boolean[n];
//
//                for (int i = 0; i < n; i++) {
//                    meFirstN[i] = me.bits.removeFirst();
//                    meLastN[i] = me.bits.removeLast();
//                    otherFirstN[i] = other.bits.removeFirst();
//                    otherLastN[i] = other.bits.removeLast();
//                }
//
//                for (int i = 0; i < n; i++) {
//                    me.bits.addFirst(meFirstN[n - 1 - i]);
//                    me.bits.addLast(meLastN[n - 1 - i]);
//
//                    other.bits.addFirst(otherFirstN[n - 1 - i]);
//                    other.bits.addLast(otherLastN[n - 1 - i]);
//                }
//
//                if (!Arrays.equals(meFirstN, otherFirstN) || !Arrays.equals(meLastN, otherLastN)) {
//                    return false;
//                }
//            }
            if (me.ones != other.ones || me.zeros != other.zeros) {
                return false;
            }
            return me.toString().equals(other.toString());
        }
    }

    public static boolean isLooseEqual(Bits me, Bits other) {
        boolean isEqual;
        // expand with 0 in shorter thing
        if (other.getLength() != me.getLength()) {
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
        this.bitsString.append(b);
        this.length += b.getLength();
        this.zeros += b.zeros;
        this.ones += b.ones;
    }

    public static Bits model(Bits me, Bits other) {
        assert me != null && other != null;
        if (other.getLength() > me.getLength()) {
            return me;
        }
        // assert if 2^n
        assert !other.getBitsQueue().getLast();

        Bits otherCopy = other.subtract(new Bits("1"));

        Deque<Boolean> meDeque = new LinkedList<>(me.getBitsQueue());
        Deque<Boolean> otherDeque = new LinkedList<>(otherCopy.getBitsQueue());
        Deque<Boolean> returnQueue = new ArrayDeque<>(me.getLength());

        while (!otherDeque.isEmpty()) {
            boolean meBit = meDeque.removeLast();
            boolean otherBit = otherDeque.removeLast();
            if (meBit) {
                returnQueue.addFirst(otherBit);
            } else {
                returnQueue.addFirst(false);
            }
        }
        while (!meDeque.isEmpty()) {
            meDeque.removeLast();
            returnQueue.addFirst(false);
        }
        return new Bits(returnQueue.toArray(new Boolean[]{}));
    }

    public Bits add(Bits additive) {
        return new Bits(new BigInteger(this.toString(), 2).add(new BigInteger(additive.toString(), 2)));
    }

    public Bits subtract(Bits subtractive) {
        return new Bits(new BigInteger(this.toString(), 2).subtract(new BigInteger(subtractive.toString(), 2)));
    }

    public void shiftRight() {
        this.shiftRight(1);
    }
    public void shiftRight(int length) {
        for (int i = 0; i < length; i++) {
            this.bits.addFirst(false);
            this.bits.removeLast();
        }
    }
}
