package jlu.evyde;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Huffman {
    public class Header {
        public static final int MAGIC_NUMBER = 0x01711EF3;
        public static final byte VERSION = 1;
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
            boolean result = (inStream.read() == MAGIC_NUMBER) && (inStream.readByte() <= VERSION);
            inStream.reset();
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

        public Byte[] dump() {
            List<Byte> header = new ArrayList<>();
            for (Byte b: Utils.intToByteArray(MAGIC_NUMBER)) {
                header.add(b);
            }
            header.add(VERSION);
            header.add(getType());
            for (Byte b: Utils.intToByteArray(getTrieLength(), 2)) {
                header.add(b);
            }

            for (Byte b: Utils.longToByteArray(getSourceLength().longValue())) {
                header.add(b);
            }

            for (Byte b: Utils.intToByteArray(getCRC32Code())) {
                header.add(b);
            }

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    header.add((byte) 0);
                }
            }
            return (Byte[]) header.toArray();
        }
    }

    private Header header;

    private Huffman(BitsIn in) {
        if (Header.isValidHeader(in)) {
            this.header = new Header(in);
            // create Huffman trie from file
        } else {
            this.header = new Header("");
            // create Huffman trie and do statistics
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
        for (byte h: header.dump()) {
            out.write(h);
        }
    }

    public void auto(String... outputFilename) {
        auto(new File(Utils.join(outputFilename)));
    }

    public void auto(File outputFile) {

    }

}
