package jlu.evyde;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.zip.CRC32;

public class Huffman {

    public class Node implements Comparable<Node> {
        private BigInteger appearTimes;
        private final Bits feature;
        private final Node left;
        private final Node right;

        public Node(Bits feature, Node left, Node right) {
            this.appearTimes = new BigInteger("0");
            this.feature = feature;
            this.left = left;
            this.right = right;
        }

        public void timesPlusOne() {
            this.updateTimes(this.appearTimes.add(new BigInteger("1")));
        }

        public void updateTimes(BigInteger newTimes) {
            this.appearTimes = newTimes;
        }

        public boolean isLeaf() {
            assert ((left == null) && (right == null)) || ((left != null) && (right != null));
            return (left == null) && (right == null);
        }

        @Override
        public int compareTo(Node node) {
            return  this.appearTimes.subtract(node.appearTimes).compareTo(new BigInteger("0"));
        }
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
            return (inStream.read(32).equals(MAGIC_NUMBER)) && (inStream.read(8).compareTo(VERSION) <= 0);
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
            header.add(new Bits(getType()));
            header.add(new Bits(getTrieLength(), 16));

            header.add(new Bits(getSourceLength(), 64));

            CRC32 temp = new CRC32();
            temp.update(new BigInteger("123456789101112").toByteArray());
            header.add(new Bits(temp.getValue(), 32));

            header.add(new Bits(0, 128));
            return header;
        }
    }

    private final Header header;

    private final boolean detail;

    private Huffman(BitsIn in, BitsOut out, boolean details) {
        this.detail = details;
        if (Header.isValidHeader(in)) {
            this.header = new Header(in);
            // read Huffman trie from given file

        } else {
            this.header = new Header("");
            // create Huffman trie from file and do statistics

            compress(out);
        }
    }

    public Huffman(String inputFilename, String outputFilename, boolean details) {
        this(inputFilename == null? new BitsIn(): new BitsIn(inputFilename),
                outputFilename == null? new BitsOut(): new BitsOut(outputFilename), details);
    }

    // TODO: Fill out the expand method
    private void expand() {}

    private void compress(BitsOut out) {
        for (Bits b: header.dump()) {
            out.write(b);
        }
        out.flush();
        out.close();
    }

    private void println(String things) {
        if (this.detail) {
            System.out.println(things);
        }
    }

    private void print(String things) {
        if (this.detail) {
            System.out.print(things);
        }
    }

}
