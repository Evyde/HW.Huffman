package jlu.evyde;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

public class LZ77 {
    private final boolean detail;
    private static final long WORD = 8; // using 8 bit for a word
    private static final long WINDOW_SIZE = 4096; // use 4096 Byte as sliding window size
    private static final long LOOK_AHEAD_BUFFER_SIZE = 100;
    public static final Bits VERSION = new Bits(2, 8);
    private static final int OFFSET_LEN = 5;
    private static final int POSITION_LEN = 12;

    private final List<Bits> window = new ArrayList<>((int) WINDOW_SIZE);
    private final Deque<Bits> lookAheadBuffer = new LinkedList<>();
    private final Map<Bits, Deque<Position>> windowMap = new HashMap<>();
    private final Deque<Bits> windowKeyList = new LinkedList<>();
    private final ProgressBarWrapper pb;
    private final Bits ZERO_BITS = new Bits("0", 64);
    private final Bits FRAGMENT_THRESHOLD = new Bits(new BigInteger("512000", 10), 64);

    public static class Position {
        private static Long NOW_START_POSITION = Long.MIN_VALUE;
        private static Long NEXT_RELATIVE_POSITION = 0L;
        private final Long position;

        public Position() {
            if (NEXT_RELATIVE_POSITION == WINDOW_SIZE) {
                NEXT_RELATIVE_POSITION -= 1;
                NOW_START_POSITION += 1;
            }
            this.position = NOW_START_POSITION + NEXT_RELATIVE_POSITION++;
        }

        public Position(long relative) {
            this.position = NOW_START_POSITION + relative;
        }

        public static void reset() {
            NEXT_RELATIVE_POSITION = 0L;
            NOW_START_POSITION = Long.MIN_VALUE;
        }

        public Long get() {
            return this.position - NOW_START_POSITION;
        }

        public static Position removeLast() {
            if (NEXT_RELATIVE_POSITION == 0) {
                return new Position(-1);
            }
            NEXT_RELATIVE_POSITION -= 1;
            return new Position(NEXT_RELATIVE_POSITION);
        }

        public static Position removeFirst() {
            if (NEXT_RELATIVE_POSITION == 0) {
                return new Position(-1);
            }
            NOW_START_POSITION += 1;
            NEXT_RELATIVE_POSITION -= 1;
            return new Position(NEXT_RELATIVE_POSITION);
        }

        @Override
        public int hashCode() {
            return this.get().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Long) {
                return this.get().equals(obj);
            } else if (obj instanceof Position) {
                return this.get().equals(((Position) obj).get());
            }
            return false;
        }

        @Override
        public String toString() {
            return this.get().toString();
        }
    }

    private LZ77(BitsIn in, BitsOut out, boolean details) {
        this.detail = details;
        if (jlu.evyde.Header.isValidHeader(in, VERSION)) {
            Header header = new Header(in);
            pb = new ProgressBarWrapper("Expanding.", header.getSourceLength(), detail);
            expand(header, in, out);
        } else {
            pb = new ProgressBarWrapper("Compressing.", in.getContentLength(), detail);
            Header contentHeader = new Header(VERSION, in.getContentLength());
            in.hardReset();
            compress(in, out, contentHeader);
        }
    }

    private void fillLookAheadBuffer(BitsIn in) {
        while (!in.isEOF() && lookAheadBuffer.size() < LOOK_AHEAD_BUFFER_SIZE) {
            if (Bits.isLooseEqual(Bits.model(in.getContentLength(), this.FRAGMENT_THRESHOLD), this.ZERO_BITS)) {
                // reset all variable
                windowMap.clear();
                windowKeyList.clear();
                window.clear();
                System.gc();
                Position.NOW_START_POSITION = Long.MIN_VALUE;
                Position.NEXT_RELATIVE_POSITION = 0L;
            }
            lookAheadBuffer.addLast(in.read(WORD));
            pb.stepBy(WORD);
        }
    }

    private boolean match(BitsIn in, BitsOut out) {
        boolean isMatched = false;

        // boundary situation
        if (lookAheadBuffer.size() < 3) {
            while (!lookAheadBuffer.isEmpty()) {
                out.write(new Bits("0", 1));
                out.write(lookAheadBuffer.removeFirst());
            }
            return false;
        }

        // fetch 3 word from look ahead buffer
        Bits _1 = lookAheadBuffer.removeFirst();
        Bits _2 = lookAheadBuffer.removeFirst();
        Bits _3 = lookAheadBuffer.removeFirst();

        // combine them to a complete 24 bits
        Bits combined = new Bits(_1, _2, _3);

        // try to match
        Deque<Position> tempFirstPositionDeque = windowMap.get(combined);
        Deque<Bits> comparisonBuffer = new LinkedList<>();
        comparisonBuffer.add(_2);
        comparisonBuffer.add(_3);
        long slideLength = 1L;

        if (tempFirstPositionDeque != null && !tempFirstPositionDeque.isEmpty()) {
            Position tempFirstPosition = tempFirstPositionDeque.getFirst();
            if (tempFirstPosition != null && window.size() - tempFirstPosition.get() >= 3) {
                Bits existsCombination = null;
                Long firstPosition = null;
                int index = 1;
                Position[] matchedPositions = null;
                do {
                    if (isMatched) {
                        // prevent hash collision
                        if (matchedPositions == null) {
                            matchedPositions = tempFirstPositionDeque.toArray(new Position[0]);
                        }

                        if (index >= matchedPositions.length) {
                            isMatched = false;
                            break;
                        }
                        tempFirstPosition = matchedPositions[index++];
                        if (window.size() - tempFirstPosition.get() < 3) {
                            isMatched = false;
                            break;
                        }
                    }
                    firstPosition = tempFirstPosition.get();
                    // tempFirstPositionDeque.removeFirst();
                    // matched
                    isMatched = true;
                    // find max match
                    existsCombination = new Bits(window.get(Math.toIntExact(firstPosition)));
                    existsCombination.expand(window.get((int) (firstPosition + 1)));
                    existsCombination.expand(window.get((int) (firstPosition + 2)));
                } while (!combined.equals(existsCombination));

                if (isMatched) {
                    slideLength = slideLength + 2;
                    while (combined.equals(existsCombination)) {
                        slideLength += 1;
                        if (firstPosition + slideLength - 1 > window.size()) {
                            break;
                        }
                        Bits tempBit;
                        try {
                            tempBit = lookAheadBuffer.removeFirst();
                        } catch (Exception e) {
                            break;
                        }
                        combined.expand(tempBit);
                        comparisonBuffer.addLast(tempBit);
                        existsCombination.expand(window.get((int) (firstPosition + slideLength - 2)));
                    }
                    // Deal with this slideLength stuff.
                    slideLength -= 1;
                    // Write start position and offset in out
                    out.write(new Bits("1", 1));
                    out.write(new Bits(new BigInteger(String.valueOf(firstPosition), 10), POSITION_LEN));
                    out.write(new Bits(new BigInteger(String.valueOf(slideLength), 10), OFFSET_LEN));
                }
            } else {
                // not matched, just write one
                out.write(new Bits("0", 1));
                out.write(_1);
            }
        } else {
            // not matched, just write one
            out.write(new Bits("0", 1));
            out.write(_1);
        }
        out.flush();

        // restore lookahead buffer
        while (!comparisonBuffer.isEmpty()) {
            lookAheadBuffer.addFirst(comparisonBuffer.removeLast());
        }
        lookAheadBuffer.addFirst(_1);

        // slide window and make new hashes
        // Update hashes.
        for (long i = 0L; i < slideLength; i++) {
            _1 = lookAheadBuffer.removeFirst();
            window.add(_1);
            if (Position.NEXT_RELATIVE_POSITION >= WINDOW_SIZE) {
                Bits key = windowKeyList.removeFirst();
                if (windowMap.get(key) != null && !windowMap.get(key).isEmpty()) {
                    windowMap.get(key).removeFirst();
                } else {
                    // seems impossible
                    windowMap.remove(key);
                }
                window.remove(0); // remove first
                // Reset NEXT_RELATIVE_POSITION
                Position.NEXT_RELATIVE_POSITION = WINDOW_SIZE;
            }
            // make hashes
            if (lookAheadBuffer.size() < 3) {
                // no new hashes, fill the look ahead buffer
                fillLookAheadBuffer(in);
                if (lookAheadBuffer.size() < 3) {
                    return isMatched;
                }
            }
            _2 = lookAheadBuffer.removeFirst();
            _3 = lookAheadBuffer.removeFirst();
            Bits newKey = new Bits(_1, _2, _3);
            if (windowMap.get(newKey) == null) {
                Deque<Position> tempDeque = new LinkedList<>();
                tempDeque.add(new Position());
                windowMap.put(newKey, tempDeque);
            } else {
                windowMap.get(newKey).addLast(new Position());
            }
            windowKeyList.addLast(newKey);
            lookAheadBuffer.addFirst(_3);
            lookAheadBuffer.addFirst(_2);
        }

        return isMatched;
    }

    private void compress(BitsIn in, BitsOut out, Header header) {
        out.write(header.dump());
        for (int i = 0; i < 3; i++) {
            lookAheadBuffer.addLast(in.read(WORD));
        }
        pb.stepBy(3 * WORD);
        if (in.isEOF()) {
            println("Too small to compress.");
            return;
        }
        fillLookAheadBuffer(in);
        while (!lookAheadBuffer.isEmpty()) {
            // do slide window stuff
            while (lookAheadBuffer.size() >= 3) {
                match(in, out);
            }
            System.gc();
            if (in.isEOF()) {
                while (!lookAheadBuffer.isEmpty()) {
                    out.write(new Bits("0", 1));
                    out.write(lookAheadBuffer.removeFirst());
                }
            }
            // fill look ahead buffer
            fillLookAheadBuffer(in);
        }
        out.close();
        pb.refresh();
        pb.close();
        println(Utils.getCompressionRate(
                Long.parseLong(new BigInteger(in.getContentLength().toString(), 2).toString(10)),
                Long.parseLong(out.getAlreadyWrite().toString(10))
        ));

    }

    private void expand(Header header, BitsIn in, BitsOut out) {
        println(header.toString());
        out.setWriteLimit(new BigInteger(header.getSourceLength().toString(), 2));
        Deque<Bits> window = new ArrayDeque<>((int) WINDOW_SIZE * 2);
        pb.refresh();
        while (!in.isEOF()) {
            if (Bits.isLooseEqual(Bits.model(new Bits(out.getAlreadyWrite()), this.FRAGMENT_THRESHOLD), this.ZERO_BITS)) {
//                System.out.println(out.getAlreadyWrite().toString(10));
                window.clear();
            }
            if (in.readBit()) {
                // slide window thing
//                System.out.println("!");
                // read start
                int start = in.readInt(POSITION_LEN);
                int offset = in.readInt(OFFSET_LEN);

                Bits[] windowArray = window.toArray(new Bits[]{});
//                System.out.println(start + " + " + offset + " = " + windowArray.length);
                Deque<Bits> buffer = new LinkedList<>();
                for (int i = 0; i < offset; i++) {
                    if (start + i < windowArray.length) {
                        out.write(windowArray[start + i]);
                        buffer.addLast(windowArray[start + i]);
                        pb.stepBy(WORD);
//                        System.out.print((char) new BigInteger(windowArray[start + i].toString(), 2).longValue());
                    } else {
                        Bits temp;
                        try {
                            temp = buffer.removeFirst();
                        } catch (Exception e) {
                            break;
                        }
                        out.write(temp);
                        pb.stepBy(WORD);
                        window.addLast(temp);
                        if (window.size() > WINDOW_SIZE) {
                            window.removeFirst();
                        }

//                        System.out.print((char) new BigInteger(temp.toString(), 2).longValue());
                    }
                }
                while (!buffer.isEmpty()) {
                    Bits temp = buffer.removeFirst();
                    window.addLast(temp);
                    // System.out.print((char) new BigInteger(temp.toString(), 2).longValue());
                    if (window.size() > WINDOW_SIZE) {
                        window.removeFirst();
                    }
                }
//                System.out.println();
            } else {
//                System.out.println("?");
                Bits normalWord = in.read(WORD);
//                System.out.print(normalWord + ": ");
//                System.out.println((char) new BigInteger(normalWord.toString(), 2).longValue());
                pb.stepBy(WORD);
                window.addLast(normalWord);
                if (window.size() > WINDOW_SIZE) {
                    window.removeFirst();
                }
                out.write(normalWord);
            }
            out.flush();
        }
        pb.close();
        out.close();
    }

    public LZ77(String inputFilename, String outputFilename, boolean details) {
        this(inputFilename == null? new BitsIn(): new BitsIn(inputFilename),
                outputFilename == null? new BitsOut(): new BitsOut(outputFilename), details);
    }

    private void println(String... things) {
        if (this.detail) {
            System.out.println(Utils.join(things));
        }
    }

    private void print(String... things) {
        if (this.detail) {
            System.out.print(Utils.join(things));
        }
    }
}
