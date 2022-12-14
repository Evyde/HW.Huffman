package jlu.evyde;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class Utils {
    public static String join(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String s: strings) {
            sb.append(s);
        }
        return sb.toString();
    }

    public static void fillIntByteArray(int source, byte[] target, int targetBegin) {
        int i = 0;
        for (byte b: intToByteArray(source)) {
            target[targetBegin + i] = b;
            i++;
        }
    }

    public static byte[] intToByteArray(int i, int n) {
        if (n > 4) {
            return null;
        }
        byte[] target = new byte[n];
        for (int j = 0; j < n; j++) {
            target[j] = (byte) ((i >>> ((n - j - 1) * 8)) & 0xff);
        }
        return target;
    }

    public static byte[] intToByteArray(int i) {
        return intToByteArray(i, 4);
    }

    public static void fillLongByteArray(long source, byte[] target, int targetBegin) {
        int i = 0;
        for (byte b: longToByteArray(source)) {
            target[targetBegin + i] = b;
            i++;
        }
    }

    public static byte[] longToByteArray(long i, int n) {
        if (n > 8) {
            return null;
        }
        byte[] target = new byte[n];
        for (int j = 0; j < n; j++) {
            target[j] = (byte) ((i >>> ((n - j - 1) * 8)) & 0xff);
        }
        return target;
    }

    public static byte[] longToByteArray(long i) {
        return longToByteArray(i, 8);
    }

    public static class RandomAccessFileOutputStream extends InputStream {

        private final RandomAccessFile raf;

        public RandomAccessFileOutputStream(RandomAccessFile raf) {
            this.raf = raf;
        }

        @Override
        public int read() throws IOException {
            return raf.read();
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void close() throws IOException {
            raf.close();
        }

        @Override
        public synchronized void reset() throws IOException {
            raf.seek(0);
        }
    }

    public static void mapVisualize(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder("Map: [");

        for (Map.Entry<?, ?> e: map.entrySet()) {
            if (sb.length() % 80 == 0) {
                sb.append('\n');
            }
            sb.append(e.getKey()).append(": ");
            sb.append(e.getValue());
            sb.append(", ");
        }

        sb.append("]");
        System.out.println(sb);
    }

    public static String getCompressionRate(long before, long after) {
        // minus header
        after -= jlu.evyde.Header.LENGTH;
        return ("Compression rate: " + after + "/" + before + " = " + new BigDecimal(after * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .divide(new BigDecimal(before)
                        .setScale(2, RoundingMode.HALF_UP), RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue() + "%.");
    }
}
