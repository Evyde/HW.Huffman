package jlu.evyde;

import java.util.*;

public class HuffmanLoader {
    private static final ResourceBundle rb = ResourceBundle.getBundle("ManPage", Locale.US);

    static final Map<String, String> options = new HashMap<>();

    static {
        options.put("OptionH", "-h");
        options.put("OptionHelp", "--help");
        options.put("OptionV", "-v");
        options.put("OptionI", "-I");
        options.put("OptionO", "-O");
        options.put("OptionT", "-t");
    }

    private static void printHelpMessage() {
        System.out.println(rb.getString("DESCRIPTION"));
        System.out.println(rb.getString("USAGE"));
        System.out.println("java -jar huffman.jar [-I-O-v-h] [-t] [Type] [Input File] [Output File]");
        System.out.println(options.get("OptionI") + "\t\t" + rb.getString("DASH_I"));
        System.out.println(options.get("OptionO") + "\t\t" + rb.getString("DASH_O"));
        System.out.println(options.get("OptionV") + "\t\t" + rb.getString("DASH_V"));
        System.out.println(options.get("OptionH") + "/" + options.get("OptionHelp") + "\t" + rb.getString(
                "DASH_H"));
    }

    public static void main(String[] args) {

        Set<String> switchSet = new HashSet<>();

        String inputFilename = null;
        String outputFilename = null;

        if (args.length < 2) {
            printHelpMessage();
            return;
        }

        boolean optionDashOOn = false;
        boolean optionDashIOn = false;
        boolean optionDashVOn = false;
        boolean optionDashTOn = false;

        int type = 0;
        List<String> argList = new ArrayList<>();
        Deque<String> filenameList = new ArrayDeque<>(2);

        for (String a: args) {
            if (optionDashTOn) {
                optionDashTOn = false;
                try {
                    type = Integer.parseInt(a);
                    if (type <= 0 || type % 2 != 0 || (type % 8 != 0 || 8 % type != 0) || type > 128) {
                        throw new NumberFormatException();
                    }
                    continue;
                } catch (NumberFormatException nfe) {
                    type = 0;
                }
            }

            if (a.startsWith("-")) {
                if (a.equals(options.get("OptionT"))) {
                    optionDashTOn = true;
                    continue;
                }
                switchSet.add(a);
            } else {
                filenameList.add(a);
            }
            argList.add(a);
        }

        args = argList.toArray(new String[]{});

        if (args.length < 2) {
            printHelpMessage();
            return;
        }

        if (switchSet.contains(options.get("OptionH")) || switchSet.contains(options.get("OptionHelp"))) {
            printHelpMessage();
            return;
        } else {
            if (switchSet.contains(options.get("OptionO"))) {
                if (switchSet.contains(options.get("OptionV"))) {
                    System.err.println(rb.getString("CONFLICT"));
                    return;
                } else {
                    optionDashOOn = true;
                }
            }

            if (switchSet.contains(options.get("OptionI"))) {
                optionDashIOn = true;
            }

            if (switchSet.contains(options.get("OptionV"))) {
                optionDashVOn = true;
            }
        }

        if (!optionDashIOn) {
            try {
                inputFilename = filenameList.removeFirst();
            } catch (NoSuchElementException ignored) {

            }
        }

        if (!optionDashOOn) {
            try {
                outputFilename = filenameList.removeFirst();
            } catch (NoSuchElementException ignored) {

            }
        }


        if (!optionDashIOn && inputFilename == null) {
            System.err.println(rb.getString("ERROR_PLEASE_SPECIFY_INPUT_FILENAME"));
            return;
        }

        if (!optionDashOOn && outputFilename == null) {
            System.err.println(rb.getString("ERROR_PLEASE_SPECIFY_OUTPUT_FILENAME"));
            return;
        }

        if (!optionDashIOn && !optionDashOOn) {
            // check if input/output filename exist
            if (args.length - switchSet.size() < 2) {
                printHelpMessage();
                return;
            }
        }

        new Huffman(inputFilename, outputFilename, optionDashVOn, type);
    }
}