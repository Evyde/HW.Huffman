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
    private static final long OFFSET_LIMIT = (long) Math.pow(2, OFFSET_LEN);

    private List<Bits> window = new ArrayList<>((int) WINDOW_SIZE * 2);
    private final Deque<Bits> lookAheadBuffer = new LinkedList<>();
    private Map<Bits, List<Position>> windowMap = new HashMap<>();
    private Deque<Bits> windowKeyList = new LinkedList<>();
    private final ProgressBarWrapper pb;
    private final Bits ZERO_BIT = new Bits("0", 1);
    private final Bits ONE_BIT = new Bits("1", 1);
    private final BigInteger FRAGMENT_THRESHOLD = new BigInteger("64000", 10); // 64KB
    private BigInteger alreadyRead = new BigInteger("0", 10);
    private BigInteger lastReset = new BigInteger("0", 10);

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
            lookAheadBuffer.addLast(in.read(WORD));
            pb.stepBy(WORD);
        }
    }

    private boolean match(BitsIn in, BitsOut out) {
        boolean isMatched = false;

        // boundary situation
        if (lookAheadBuffer.size() < 3) {
            while (!lookAheadBuffer.isEmpty()) {
                out.write(ZERO_BIT);
                out.write(lookAheadBuffer.removeFirst());
                this.alreadyRead = this.alreadyRead.add(new BigInteger("1", 10));
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
        List<Position> tempFirstPositionList = windowMap.get(combined);
        Deque<Bits> comparisonBuffer = new LinkedList<>();
        comparisonBuffer.add(_2);
        comparisonBuffer.add(_3);
        long slideLength = 1L;

        if (tempFirstPositionList != null && !tempFirstPositionList.isEmpty()) {
            Position tempFirstPosition = tempFirstPositionList.get(0);
            if (tempFirstPosition != null && window.size() - tempFirstPosition.get() > 3) {
                Bits existsCombination = null;
                Long firstPosition = null;
                int index = 1;
                List<Position> matchedPositions = null;
                do {
                    if (isMatched) {
                        // prevent hash collision
                        if (matchedPositions == null) {
                            matchedPositions = tempFirstPositionList;
                        }

                        if (index >= matchedPositions.size()) {
                            isMatched = false;
                            break;
                        }

                        tempFirstPosition = matchedPositions.get(index++);
                        if (window.size() - tempFirstPosition.get() <= 3) {
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

                assert window.size() <= WINDOW_SIZE;

                if (isMatched) {
                    // at least 3 byte
                    slideLength = slideLength + 3;
                    while (true) {
//                        pb2.setExtraMessage(this.alreadyRead.toString(10));
//                        pb2.step();
//                        pb2.refresh();
                        if (slideLength >= OFFSET_LIMIT) {
                            slideLength -= 1;
                            break;
                        }
                        if (firstPosition + slideLength - 1 >= window.size()) {
                            slideLength -= 1;
                            break;
                        }
                        Bits tempBit;
                        try {
                            tempBit = lookAheadBuffer.removeFirst();
                        } catch (Exception e) {
                            slideLength -= 1;
                            break;
                        }
                        combined.expand(tempBit);
                        comparisonBuffer.addLast(tempBit);
                        existsCombination.expand(window.get((int) (firstPosition + slideLength - 1)));
                        if (combined.equals(existsCombination)) {
                            slideLength += 1;
                        } else {
                            slideLength -= 1;
                            break;
                        }
                    }
                    // Deal with this slideLength stuff.

                    if (slideLength < 3) {
                        isMatched = false;
                        slideLength = 1L;
                    } else {
                        assert firstPosition + slideLength < WINDOW_SIZE;
                        // Write start position and offset in out
                        out.write(ONE_BIT);
                        out.write(firstPosition, POSITION_LEN);
                        out.write(slideLength, OFFSET_LEN);
                    }
                }
            }
        }

        if (!isMatched) {
            // not matched, just write one
            out.write(ZERO_BIT);
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
            if (window.size() >= WINDOW_SIZE) {
                Bits key = windowKeyList.removeFirst();
                if (windowMap.get(key) != null && !windowMap.get(key).isEmpty()) {
                    windowMap.get(key).remove(0);
                } else {
                    // seems impossible
                    windowMap.remove(key);
                }
                window.remove(0); // remove first
                // Reset NEXT_RELATIVE_POSITION
                Position.NEXT_RELATIVE_POSITION = WINDOW_SIZE;
            }
            // make hashes
            if (lookAheadBuffer.size() < 2) {
                // no new hashes, fill the look ahead buffer
                fillLookAheadBuffer(in);
                if (lookAheadBuffer.size() < 2) {
                    return isMatched;
                }
            }
            _2 = lookAheadBuffer.removeFirst();
            _3 = lookAheadBuffer.getFirst();
            Bits newKey = new Bits(_1, _2, _3);
            if (windowMap.get(newKey) == null) {
                List<Position> tempDeque = new ArrayList<>((int) WINDOW_SIZE);
                tempDeque.add(new Position());
                windowMap.put(newKey, tempDeque);
            } else {
                windowMap.get(newKey).add(new Position());
            }
            windowKeyList.addLast(newKey);
            lookAheadBuffer.addFirst(_2);
        }

        this.alreadyRead = this.alreadyRead.add(new BigInteger(String.valueOf(slideLength), 10));
        if (this.alreadyRead.subtract(this.lastReset).compareTo(this.FRAGMENT_THRESHOLD) >= 0) {
            // reset all variable
            pb.setExtraMessage(this.alreadyRead.toString(10));
            pb.refresh();
            windowMap = new HashMap<>();
            windowKeyList = new LinkedList<>();
            window = new ArrayList<>((int) WINDOW_SIZE * 2);
            System.gc();
            Position.NOW_START_POSITION = Long.MIN_VALUE;
            Position.NEXT_RELATIVE_POSITION = 0L;
            this.lastReset = this.alreadyRead;
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
                    out.write(ZERO_BIT);
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
        // Deque<Bits> window = new ArrayDeque<>((int) WINDOW_SIZE * 2);
        window = new ArrayList<>((int) WINDOW_SIZE * 2);
        pb.refresh();
        while (!in.isEOF()) {
            if (in.readBit()) {
                // slide window thing
//                System.out.println("!");
                // read start
                int start = in.readInt(POSITION_LEN);
                int offset = in.readInt(OFFSET_LEN);

                //System.out.println(this.alreadyRead.toString() + " : " + offset);

                List<Bits> buffer = new ArrayList<>((int) WINDOW_SIZE);
//                System.out.println(start + " + " + offset + " = " + windowArray.length);
                this.alreadyRead = this.alreadyRead.add(new BigInteger(String.valueOf(offset)));
                for (int i = 0; i < offset; i++) {
                    //mif (start + i < windowArray.length) {
                    Bits element = window.get(start + i);
                    out.write(element);
                    buffer.add(element);
                    pb.stepBy(WORD);
                }
                while (!buffer.isEmpty()) {
                    window.add(buffer.remove(0));
                    if (window.size() >= WINDOW_SIZE) {
                        window.remove(0);
                    }
                }
//                System.out.println();
            } else {
//                System.out.println("?");
                Bits normalWord = in.read(WORD);
//                System.out.print(normalWord + ": ");
//                System.out.println((char) new BigInteger(normalWord.toString(), 2).longValue());
                pb.stepBy(WORD);
                window.add(normalWord);
                this.alreadyRead = this.alreadyRead.add(new BigInteger("1"));
                if (window.size() >= WINDOW_SIZE) {
                    window.remove(0);
                }
                out.write(normalWord);
            }
            out.flush();
            if (this.alreadyRead.subtract(this.lastReset).compareTo(this.FRAGMENT_THRESHOLD) >= 0) {
//                System.out.println(out.getAlreadyWrite().toString(10));
                window = new ArrayList<>((int) WINDOW_SIZE * 2);
                this.lastReset = this.alreadyRead;
            }
        }
        pb.refresh();
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
