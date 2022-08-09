package jlu.evyde;

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
}