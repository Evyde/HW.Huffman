package jlu.evyde;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class Main {
    private static final ResourceBundle rb = ResourceBundle.getBundle("MainManPage", Locale.getDefault());

    static final Map<String, String> options = new HashMap<>();

    static {
        options.put("OptionHuffman", "huffman");
        options.put("OptionLZ77", "lz77");
    }

    private static void printHelpMessage() {
        System.out.println(rb.getString("DESCRIPTION"));
        System.out.println(rb.getString("USAGE"));
        System.out.println("java -jar zip.jar huffman/lz77 [submodule command]");
        System.out.println(options.get("OptionHuffman") + "\t\t" + rb.getString("HUFFMAN"));
        System.out.println(options.get("OptionLZ77") + "\t\t" + rb.getString("LZ77"));
    }


    public static void main(String[] argv) {
        if (argv.length < 1) {
            printHelpMessage();
            return;
        }

        String[] subArgv = new String[argv.length - 1];

        System.arraycopy(argv, 1, subArgv, 0, subArgv.length);

        if (argv[0].equals(options.get("OptionHuffman"))) {
            HuffmanLoader.main(subArgv);
        } else if (argv[0].equals(options.get("OptionLZ77"))) {
            LZ77Loader.main(subArgv);
        }
    }
}
