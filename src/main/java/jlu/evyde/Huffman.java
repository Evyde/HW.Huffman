package jlu.evyde;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

public class Huffman {

    public class Node implements Comparable<Node> {
        private final BigInteger appearTimes;
        private final Bits feature;
        private final Node left;
        private final Node right;

        public Node(Bits feature, BigInteger appearTimes, Node left, Node right) {
            this.feature = feature;
            this.appearTimes = appearTimes;
            this.left = left;
            this.right = right;
        }

        public boolean isLeaf() {
            return this.feature != null;
        }

        @Override
        public int compareTo(Node node) {
            return  this.appearTimes.subtract(node.appearTimes).compareTo(new BigInteger("0"));
        }
    }

    public class Header {
        public static final Bits MAGIC_NUMBER = new Bits(0x01711EF3, 32);
        public static final Bits VERSION = new Bits(1, 8);
        private final byte type;
        private final int trieLength;
        private final Bits sourceLength;

        private final int CRC32Code;

        public Header(BitsIn in) {
            // valid header, read it
            this.type = in.readByte();
            this.trieLength = in.readInt(16);
            this.sourceLength = in.read(64);
            this.CRC32Code = in.readInt();
            in.read(128);
            // 8 + 16 + 64 + 32 + 128 = 248 + (32 + 8) = 288
        }

        public Header(byte type, int trieLength, Bits sourceLength, int CRC32Code) {
            this.type = type;
            this.trieLength = trieLength;
            this.sourceLength = sourceLength;
            this.CRC32Code = CRC32Code;
        }

        public static boolean isValidHeader(BitsIn inStream) {
            // try reading first 32 bits -- an integer
            // and the version number is supported
            return (inStream.read(32).equals(MAGIC_NUMBER)) && (inStream.read(8).compareTo(VERSION) <= 0);
        }

        public Bits getSourceLength() {
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

            header.add(new Bits(getSourceLength().toString(), 64));

            header.add(new Bits(getCRC32Code(), 32));

            header.add(new Bits(0, 128));
            return header;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("\tHeader\n====================\n");

            sb.append("Magic number: ").append(new BigInteger(MAGIC_NUMBER.toString(), 2).toString(16)).append(".\n");
            sb.append("Version: ").append(new BigInteger(VERSION.toString(), 2).longValue()).append(".\n");
            sb.append("Type: ").append(type).append(".\n");
            sb.append("Trie length: ").append(trieLength).append(" bits.\n");
            sb.append("Source length: ").append(new BigInteger(sourceLength.toString(), 2).toString(10))
                    .append(" bits.\n");
            sb.append("CRC32 code: ").append(new BigInteger(new Bits(CRC32Code).toString(), 2).toString(16)).append(".\n");
            sb.append("====================\n");
            return sb.toString();
        }
    }

    private final boolean detail;

    private Huffman(BitsIn in, BitsOut out, boolean details, int type) {
        this.detail = details;

        if (Header.isValidHeader(in)) {
            println("Expanding.");
            Header header = new Header(in);
            // read Huffman trie from given file
            expand(header, in, out);
        } else {
            println("Compress with type " + type + ".");
            // create Huffman trie from file and do statistics
            compress(in, out, type);
        }
    }

    public Huffman(String inputFilename, String outputFilename, boolean details, int type) {
        this(inputFilename == null? new BitsIn(): new BitsIn(inputFilename),
                outputFilename == null? new BitsOut(): new BitsOut(outputFilename), details, type);
    }

    private Node buildTrie(PriorityQueue<Node> frequency) {
        while (frequency.size() > 1) {
            Node left = frequency.poll();
            Node right = frequency.poll();
            Node parent = new Node(null, left.appearTimes.add(right != null? right.appearTimes:
                    new BigInteger("0")),
                    left, right);
            frequency.add(parent);
        }

        return frequency.poll();
    }

    private List<Node> walkTrie(Node node, List<Node> list, BigInteger hierarchy) {
        if (node.isLeaf()) {
            list.add(new Node(node.feature, node.appearTimes.multiply(hierarchy), null, null));
        } else {
            walkTrie(node.left, list, hierarchy.add(new BigInteger("1")));
            walkTrie(node.right, list, hierarchy.add(new BigInteger("1")));
        }
        return list;
    }

    private int generateCode(HashMap<Bits, Bits> map, Node parent, String nowBits, boolean isRead) {
        int sum = 0;
        if (parent == null) {
            return sum;
        }else if (!parent.isLeaf()) {
            sum += (generateCode(map, parent.left, nowBits + '0', isRead));
            sum += (generateCode(map, parent.right, nowBits + '1', isRead));
        } else {
            if (isRead) {
                map.put(new Bits(nowBits), parent.feature);
            } else {
                map.put(parent.feature, new Bits(nowBits));

            }
            sum += (parent.feature.getLength());
        }
        return sum;
    }

    // TODO: Fill out the expand method
    private void expand(Header header, BitsIn in, BitsOut out) {
        println(header.toString());
        HashMap<Bits, Bits> codeMap = new HashMap<>();
        Node head = readTrie(header, in);
        if (head.isLeaf()) {
            generateCode(codeMap, head, "0", true);
        } else {
            generateCode(codeMap, head, "", true);
        }
        out.setWriteLimit(new BigInteger(header.getSourceLength().toString(), 2));
        while (!in.isEOF() && !out.isReachWriteLimit()) {
            Bits b = in.read(1);
            while (!codeMap.containsKey(b)) {
                b.expand(in.read(1));
            }
            out.write(codeMap.get(b));
        }
        if (new Bits(out.getCRC32Code().getValue(), 32).equals(new Bits(header.getCRC32Code(), 32))) {
            println("Success!!!!!!");
        }
        out.flush();
        out.close();
    }

    private Node readTrie(Header header, BitsIn in) {
        boolean isLeaf = in.readBit();
        if (isLeaf) {
            return new Node(in.read(header.getType()), null, null, null);
        } else {
            return new Node(null, null, readTrie(header, in), readTrie(header, in));
        }
    }

    private void compress(BitsIn in, BitsOut out, int type) {
        // statistic word(binary) frequency

        Node first = null;

        if (type <= 0) {
            class BitLengthMapElement implements Comparable<BitLengthMapElement> {
                public final int key;
                public final BigInteger length;
                public final Node parent;

                public BitLengthMapElement(int k, BigInteger l, Node parent) {
                    this.key = k;
                    this.length = l;
                    this.parent = parent;
                }

                @Override
                public int compareTo(BitLengthMapElement o) {
                    return this.length.compareTo(o.length);
                }
            }

            PriorityQueue<BitLengthMapElement> autoDetectType = new PriorityQueue<>();

            for (int i = 2; i <= 8; i *= 2) {
                try {
                    println("Try " + i + " bits.");
                    HashMap<Bits, BigInteger> frequency = new HashMap<>();
                    PriorityQueue<Node> tempPQ = getFrequency(in, frequency, i,
                            (a, b) -> Integer.compare(a.compareTo(b), 0));

                    BigInteger sum = new BigInteger("0");

                    Node parent = buildTrie(tempPQ);

                    List<Node> tempList = walkTrie(parent, new ArrayList<>(), new BigInteger("1"));

                    for (Node n : tempList) {
                        sum = sum.add(n.appearTimes.add(new BigInteger(String.valueOf(i))));
                    }

                    autoDetectType.add(new BitLengthMapElement(i, sum, parent));

                    println("Est will be " + sum + " bits.");
                } catch (Error e) {
                    if (this.detail) {
                        e.printStackTrace();
                    }

                    // TODO: Complete type detection algorithm.

                    println("Error detected, back to last tried.");
                    autoDetectType.poll();
                    break;
                }
            }

            BitLengthMapElement temp = autoDetectType.poll();
            type = temp == null? 8: temp.key;
            first = temp == null? null: temp.parent;
        }

        if (first == null) {
            first = buildTrie(getFrequency(in, new HashMap<>(), type, (a, b) -> Integer.compare(a.compareTo(b), 0)));
        }

        HashMap<Bits, Bits> codeMap = new HashMap<>();

        // Utils.mapVisualize(codeMap);
        Header header;
        if (first.isLeaf()) {
            header  = new Header((byte) type, generateCode(codeMap, first, "0", false), in.getContentLength(),
                    (int) in.getCRC32Code().getValue());
        } else {
            header = new Header((byte) type, generateCode(codeMap, first, "", false), in.getContentLength(),
                    (int) in.getCRC32Code().getValue());
        }

        // write header
        out.write(header.dump());

        // write trie
        writeTrie(first, out, type);

        // write content
        while (!in.isEOF()) {
            out.write(codeMap.get(in.read(type)));
        }
        out.flush();
        out.close();
        println("Complete!\n" + header);
        long before = Long.parseLong(new BigInteger(header.sourceLength.toString(), 2).toString(10));
        long after = Long.parseLong(out.getAlreadyWrite().toString(10));
        // minus header
        after -= 288;
        println("Compression rate: " + after + "/" + before + " = " + new BigDecimal(after * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .divide(new BigDecimal(before)
                        .setScale(2, RoundingMode.HALF_UP), RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue() + "%.");
    }

    private void writeTrie(Node first, BitsOut out, int type) {
        if (first == null) {
            return;
        }

        if (first.isLeaf()) {
            out.write(true);
            out.write(new Bits(first.feature.toString(), type));
        } else {
            out.write(false);
            writeTrie(first.left, out, type);
            writeTrie(first.right, out, type);
        }
    }

    private PriorityQueue<Node> getFrequency(BitsIn in, HashMap<Bits, BigInteger> frequency, int type,
                                             Comparator<Node> pqComparator) {
        while (!in.isEOF()) {
            Bits tempByte = in.read(type);
            assert tempByte.getLength() == type;
            if (frequency.containsKey(tempByte)) {
                frequency.put(tempByte, frequency.get(tempByte).add(new BigInteger("1")));
            } else {
                frequency.put(tempByte, new BigInteger("1"));
            }
        }

        PriorityQueue<Node> pq = new PriorityQueue<>(pqComparator);

        for (Bits keySet: frequency.keySet()) {
            pq.add(new Node(keySet, frequency.get(keySet), null, null));
        }

        in.reset();
        return pq;
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
