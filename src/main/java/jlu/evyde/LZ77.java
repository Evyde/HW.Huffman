package jlu.evyde;

import java.math.BigInteger;
import java.util.*;

public class LZ77 {
    private final boolean detail;
    private static final long WORD = 8; // using 8 bit for a word
    private static final long WINDOW_SIZE = 4096 * WORD; // use 4096 Byte as sliding window size
    private static final long LOOK_AHEAD_BUFFER_SIZE = 100 * WORD;

    private final Map<Long, Bits> window = new HashMap<>();
    private final Deque<Bits> lookAheadBuffer = new LinkedList<>();
    private final Map<Bits, Long> windowMap = new HashMap<>();
    private final Deque<Bits> windowKeyList = new LinkedList<>();
    private final ProgressBarWrapper pb;
    private final Bits ZERO_BITS = new Bits("0", 64);
    private final Bits FRAGMENT_THRESHOLD = new Bits(new BigInteger("64", 10), 8);
    private LZ77(BitsIn in, BitsOut out, boolean details) {
        this.detail = details;
        if (jlu.evyde.Header.isValidHeader(in)) {
            Header header = new Header(in);
            pb = new ProgressBarWrapper("Expanding.", header.getSourceLength(), detail);
            expand(header, in, out);
        } else {
            pb = new ProgressBarWrapper("Compressing.", -1, detail);
            compress(in, out);
        }
    }

    private void fillLookAheadBuffer(BitsIn in) {
        while (in.isEOF() && lookAheadBuffer.size() < LOOK_AHEAD_BUFFER_SIZE) {
            lookAheadBuffer.addLast(in.read(WORD));
            pb.stepBy(WORD);
        }
    }

    private void compress(BitsIn in, BitsOut out) {
        lookAheadBuffer.addLast(in.read(3 * WORD));
        pb.stepBy(3 * WORD);
        if (in.isEOF()) {
            println("Too small to compress.");
            return;
        }
        fillLookAheadBuffer(in);
        while (!lookAheadBuffer.isEmpty()) {
            if (Bits.isLooseEqual(Bits.model(in.getContentLength(), this.FRAGMENT_THRESHOLD), this.ZERO_BITS)) {
                // do last check in look ahead buffer
                match(out);
                // reset all variable
                windowMap.clear();
                lookAheadBuffer.clear();
                window.clear();

                // fill look ahead buffer
                fillLookAheadBuffer(in);
            } else {
                // do slide window stuff
                match(out);
                fillLookAheadBuffer(in);
            }
        }
    }

    private boolean match(BitsOut out) {
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
        Long firstPosition = windowMap.get(combined);
        long slideLength = 1L;
        if (firstPosition != null) {
            // matched
            isMatched = true;
            slideLength = slideLength + 1;
            // find max match
            Bits existsCombination = window.get(firstPosition);
            while (combined.equals(existsCombination)) {
                slideLength += 1;
                combined.expand(lookAheadBuffer.removeFirst());
                existsCombination.expand(window.get(firstPosition + slideLength));
            }
            // TODO: Deal with this slideLength stuff.
            slideLength -= 1;
            // TODO: Write start position and offset in out
        } else {
            // not matched, just write one
            lookAheadBuffer.addFirst(_2);
            lookAheadBuffer.addFirst(_3);
            out.write(new Bits("0", 1));
            out.write(_1);
        }

        // slide window and make new hashes
        // TODO: Update hashes.
        for (long i = 0L; i < slideLength; i++) {
            windowMap.remove(windowKeyList.removeFirst());
        }

        return isMatched;
    }

    private void expand(Header header, BitsIn in, BitsOut out) {

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
