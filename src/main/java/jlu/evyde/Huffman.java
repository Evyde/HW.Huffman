package jlu.evyde;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.zip.CRC32;

public class Huffman {

    public class Node {

    }

    public class Header {
        public static final Bits MAGIC_NUMBER = new Bits(0x01711EF3, 32);
        public static final Bits VERSION = new Bits(1, 8);
        private byte type;
        private int trieLength;
        private BigInteger sourceLength;

        private int CRC32Code;

        public Header(BitsIn in) {
            // valid header, read it
        }

        // TODO: Change the signature of this method to fit the full huffman class
        public Header(String... things_of_huffman) {

        }

        public static boolean isValidHeader(BitsIn inStream) {
            // try reading first 32 bits -- an integer
            // and the version number is supported
            // TODO: Change this stupid readInt into Bits read()
            boolean result = (inStream.read(32).equals(MAGIC_NUMBER)) && (inStream.read(8).compareTo(VERSION) <= 0);
            // inStream.reset();
            return result;
        }

        public BigInteger getSourceLength() {
            return sourceLength;
        }

        public int getTrieLength() {
            return trieLength;
        }

        public byte getType() {
            return type;
        }

        public int getCRC32Code() {
            return CRC32Code;
        }

        public List<Bits> dump() {
            List<Bits> header = new ArrayList<>();
            header.add(MAGIC_NUMBER);
            header.add(VERSION);
            header.add(new Bits((byte) 1));
            header.add(new Bits(37890, 16));

            header.add(new Bits(new BigInteger("123456789101112"), 64));

            CRC32 temp = new CRC32();
            temp.update(new BigInteger("123456789101112").toByteArray());
            header.add(new Bits(temp.getValue(), 32));

            header.add(new Bits(0, 128));
            return header;
        }
    }

    private final Header header;

    private Huffman(BitsIn in) {
        if (Header.isValidHeader(in)) {
            this.header = new Header(in);
            // read Huffman trie from given file

        } else {
            this.header = new Header("");
            // create Huffman trie from file and do statistics
        }
    }

    public Huffman(String... filename) {
        this(new BitsIn(filename));
    }

    public Huffman(File file) {
        this(new BitsIn(file));
    }

    // TODO: Fill out the expand method
    private void expand() {}

    private void compress(String... filename) {
        compress(new BitsOut(filename));
    }

    private void compress(BitsOut out) {
        for (Bits b: header.dump()) {
            out.write(b);
        }
        out.flush();
        out.close();
    }

    public void auto(String... outputFilename) {
        auto(new File(Utils.join(outputFilename)));
    }

    public void auto(File outputFile) {
        compress(new BitsOut(outputFile));
    }

}
