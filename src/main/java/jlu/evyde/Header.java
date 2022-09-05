package jlu.evyde;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Header {
    public static final Bits MAGIC_NUMBER = new Bits(0x01711EF3, 32);
    public static final Bits VERSION = new Bits(1, 8);
    public static final long LENGTH = 288;
    private final int type;
    private final int trieLength;
    private final Bits sourceLength;

    private final int CRC32Code;

    public Header(BitsIn in) {
        // valid header, read it
        this.type = in.readUnsignedByte();
        int tempTrieLength = in.readInt(16);
        if (tempTrieLength == 0xFFFF) {
            this.trieLength = 0xFFFFFFFF;
        } else {
            this.trieLength = tempTrieLength;
        }
        this.sourceLength = in.read(64);
        this.CRC32Code = in.readInt();
        in.read(128);
        // 8 + 16 + 64 + 32 + 128 = 248 + (32 + 8) = 288
    }

    public Header(int type, int trieLength, Bits sourceLength, int CRC32Code) {
        this.type = type;
        if (trieLength >= 0xFFFF) {
            this.trieLength = 0xFFFFFFFF;
        } else {
            this.trieLength = trieLength;
        }
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

    public int getType() {
        return type;
    }

    public int getCRC32Code() {
        return CRC32Code;
    }

    public List<Bits> dump() {
        List<Bits> header = new ArrayList<>();
        header.add(MAGIC_NUMBER);
        header.add(VERSION);
        header.add(new Bits(getType(), 8));
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
        sb.append("Trie length: ").append(trieLength).append(" bits" +
                ".\n");
        sb.append("Source length: ").append(new BigInteger(sourceLength.toString(), 2).toString(10))
                .append(" bits.\n");
        sb.append("CRC32 code: ").append(new BigInteger(new Bits(CRC32Code).toString(), 2).toString(16)).append(".\n");
        sb.append("====================\n");
        return sb.toString();
    }
}
