package jlu.evyde;

import java.util.*;

public class LZ77Loader {
    private static final ResourceBundle rb = ResourceBundle.getBundle("LZ77ManPage", Locale.getDefault());

    static final Map<String, String> options = new HashMap<>();

    static {
        options.put("OptionH", "-h");
        options.put("OptionHelp", "--help");
        options.put("OptionV", "-v");
        options.put("OptionI", "-I");
        options.put("OptionO", "-O");
    }

    private static void printHelpMessage() {
        System.out.println(rb.getString("DESCRIPTION"));
        System.out.println(rb.getString("USAGE"));
        System.out.println("java -jar zip.jar lz77 [-I-O-v-h] [Input File] [Output File]");
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

        List<String> argList = new ArrayList<>();
        Deque<String> filenameList = new ArrayDeque<>(2);

        for (String a: args) {
            if (a.startsWith("-")) {
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

        new LZ77(inputFilename, outputFilename, optionDashVOn);
    }
}
