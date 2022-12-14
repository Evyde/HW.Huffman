package jlu.evyde;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

/**
 * This class's thoughts are basically from
 * <a href="https://algs4.cs.princeton.edu/55compression/Huffman.java.html">Princeton ALGS</a>
 */
public class Huffman {

    public static class Node implements Comparable<Node> {
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

    private final boolean detail;

    private Huffman(BitsIn in, BitsOut out, boolean details, int type) {
        this.detail = details;

        if (jlu.evyde.Header.isValidHeader(in)) {
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
            Node parent = new Node(null, left.appearTimes.add(right != null ? right.appearTimes :
                    new BigInteger("0")),
                    left, right);
            frequency.add(parent);
        }

        return frequency.poll();
    }

    private List<Node> getAllLeafWithActualAppearTimes(Node node, List<Node> list, BigInteger hierarchy) {
        if (node.isLeaf()) {
            list.add(new Node(node.feature, node.appearTimes.multiply(hierarchy).add(hierarchy), null, null));
        } else {
            getAllLeafWithActualAppearTimes(node.left, list, hierarchy.add(new BigInteger("1")));
            getAllLeafWithActualAppearTimes(node.right, list, hierarchy.add(new BigInteger("1")));
        }
        return list;
    }

    private int generateCode(HashMap<Bits, Bits> map, Node parent, String nowBits, boolean isRead) {
        int sum = 0;
        if (parent == null) {
            return sum;
        } else if (!parent.isLeaf()) {
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

    private void expand(Header header, BitsIn in, BitsOut out) {
        println(header.toString());
        ProgressBarWrapper pb = new ProgressBarWrapper("Expanding.", header.getSourceLength(), detail);
        HashMap<Bits, Bits> codeMap = new HashMap<>();
        Node head = readTrie(header, in);
        if (head.isLeaf()) {
            generateCode(codeMap, head, "0", true);
        } else {
            generateCode(codeMap, head, "", true);
        }
        out.setWriteLimit(new BigInteger(header.getSourceLength().toString(), 2));
        while (!in.isEOF()) {
            Bits b = in.read(1);
            Bits value = codeMap.get(b);
            while (value == null) {
                b.expand(in.read(1));
                value = codeMap.get(b);
            }
            out.write(codeMap.get(b));
            pb.stepBy(header.getType());
        }
        pb.maxHint(new BigInteger(header.getSourceLength().toString(), 2).longValue());
        pb.step(new BigInteger(header.getSourceLength().toString(), 2).longValue());
        pb.refresh();

        if (new Bits(out.getCRC32Code().getValue(), 32).equals(new Bits(header.getCRC32Code(), 32))) {
            pb.setExtraMessage("CRC32 equals.");
            pb.refresh();
            println("Success!!!!!!");
        }

        pb.close();
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
        int STAGE = 3;
        ProgressBarWrapper pbs = new ProgressBarWrapper("Compress Stage", STAGE, detail);
        Node first = null;

        pbs.setExtraMessage("Detecting type.");
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
                    if (o == null) {
                        return 1;
                    }
                    return this.length.compareTo(o.length);
                }
            }

            PriorityQueue<BitLengthMapElement> autoDetectType = new PriorityQueue<>();
            ProgressBarWrapper pbts = new ProgressBarWrapper("Auto detecting", 7, detail);
            for (int i = 128; i >= 2; i /= 2) {
                try {
                    System.gc();
                    pbts.setExtraMessage("Trying " + i + " bits");
                    pbts.refresh();
                    HashMap<Bits, BigInteger> frequency = new HashMap<>();
                    PriorityQueue<Node> tempPQ = getFrequency(in, frequency, i,
                            (a, b) -> Integer.compare(a.compareTo(b), 0));

                    BigInteger sum = new BigInteger("0");

                    Node parent = buildTrie(tempPQ);

                    List<Node> tempList = getAllLeafWithActualAppearTimes(parent, new ArrayList<>(), new BigInteger("1"));

                    for (Node n : tempList) {
                        sum = sum.add(n.appearTimes.add(new BigInteger(String.valueOf(i))));
                    }

                    BitLengthMapElement nowBitsLength = new BitLengthMapElement(i, sum, parent);

                    autoDetectType.add(nowBitsLength);
                    pbts.step();
                    pbts.setExtraMessage("Estimate will be " + sum + " bits.");
                    pbts.refresh();
                } catch (Error e) {
                    pbts.setExtraMessage("Error detected, try next.");
                    pbts.step(8 - (long) (Math.log(i) / Math.log(2)));
                    pbts.refresh();
                    in.softReset();
                    if (autoDetectType.peek() != null && autoDetectType.peek().key == i) {
                        autoDetectType.poll();
                        i /= 2;
                    }
                }
            }
            pbts.step(7);

            BitLengthMapElement temp = autoDetectType.poll();
            type = temp == null? 8: temp.key;
            first = temp == null? null: temp.parent;
            pbts.setExtraMessage("Using type " + type + ".");
            pbts.refresh();
            pbts.close();
            pbs.setExtraMessage("Select type " + type + ".");
            pbs.refresh();
        }
        pbs.step();
        pbs.refresh();

        if (first == null) {
            first = buildTrie(getFrequency(in, new HashMap<>(), type, (a, b) -> Integer.compare(a.compareTo(b), 0)));
        }

        HashMap<Bits, Bits> codeMap = new HashMap<>();

        pbs.setExtraMessage("Generating Header.");
        pbs.refresh();

        Header header;
        if (first.isLeaf()) {
            header  = new Header(type, generateCode(codeMap, first, "0", false), in.getContentLength(),
                    (int) in.getCRC32Code().getValue());
        } else {
            header = new Header(type, generateCode(codeMap, first, "", false), in.getContentLength(),
                    (int) in.getCRC32Code().getValue());
        }

        // Utils.mapVisualize(codeMap);

        pbs.step();
        pbs.setExtraMessage("Writing.");
        pbs.refresh();

        ProgressBarWrapper pbWrite = new ProgressBarWrapper("Writing", in.getContentLength(), detail);

        // write header
        out.write(header.dump());

        // write trie
        writeTrie(first, out, type);

        // write content
        while (!in.isEOF()) {
            try {
                out.write(codeMap.get(in.read(type)));
                pbWrite.stepBy(type);
            } catch (Exception e) {
                System.err.println("Failed!");
                System.exit(1);
                break;
            }
        }
        pbWrite.refresh();
        pbWrite.close();
        pbs.step();
        pbs.setExtraMessage("Complete!");
        pbs.close();
        out.flush();
        out.close();
        println("Complete!\n" + header);
        println(Utils.getCompressionRate(
                Long.parseLong(new BigInteger(in.getContentLength().toString(), 2).toString(10)),
                Long.parseLong(out.getAlreadyWrite().toString(10))
        ));
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
        ProgressBarWrapper pb;
        if (in.isCRC32Generated()) {
            pb = new ProgressBarWrapper("Getting frequency", in.getContentLength(), detail);
            in.softReset();
        } else {
            pb = new ProgressBarWrapper("Getting frequency", -1, detail);
            in.hardReset();
        }

        while (!in.isEOF()) {
            Bits tempByte = in.read(type);
            pb.stepBy(type);
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

        in.softReset();
        pb.refresh();
        pb.close();
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
