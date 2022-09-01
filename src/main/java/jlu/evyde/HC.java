package jlu.evyde;

import java.util.*;

public class HC {
    private static final ResourceBundle rb = ResourceBundle.getBundle("ManPage", Locale.US);

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
        System.out.println("java Huffman/jlu.evyde.HC [-I-O-v-h] [Input File] [Output File]");
        System.out.println(options.get("OptionI") + "\t\t" + rb.getString("DASH_I"));
        System.out.println(options.get("OptionO") + "\t\t" + rb.getString("DASH_O"));
        System.out.println(options.get("OptionV") + "\t\t" + rb.getString("DASH_V"));
        System.out.println(options.get("OptionH") + "/" + options.get("OptionHelp") + "\t" + rb.getString(
                "DASH_H"));
    }

    public static void main(String[] args) {

        Map<String, Integer> argSet = new HashMap<>();
        Map<String, Integer> switchSet = new HashMap<>();

        String inputFilename = null;
        String outputFilename = null;
        String allOptions;

        {
            StringBuilder sb = new StringBuilder();

            for (String s: options.values()) {
                sb.append(s.replaceAll("-", ""));
            }

            allOptions = sb.toString();
        }

        boolean optionDashOOn = false;
        boolean optionDashIOn = false;
        boolean optionDashVOn = false;
        int type = 0;
        // TODO: Add type detective

        // TODO: Replace this command tool method with better choice

        // TODO: Fix -I give but no output file name give so out of index issue

        int tempLoopVariable = 0;
        for (String a: args) {
            argSet.put(a, tempLoopVariable++);
        }
        tempLoopVariable = 0;
        for (String a: args) {
            if (a.startsWith("-")) {
                switchSet.put(a, tempLoopVariable++);
            }
        }

        // System.out.println(Arrays.toString(switchSet.keySet().toArray(new String[0])));

        inputFilename = "-h--help-I-O-v".contains(args[args.length - 2])? null: args[args.length - 2];
        outputFilename = "-h--help-I-O-v".contains(args[args.length - 1])? null: args[args.length - 1];

        if (argSet.containsKey("-h") || argSet.containsKey("--help")) {
            printHelpMessage();
            return;
        } else {
            if (argSet.containsKey("-O")) {
                outputFilename = null;
                if (argSet.containsKey("-v")) {
                    System.err.println(rb.getString("CONFLICT"));
                    return;
                } else {
                    optionDashOOn = true;
                }
            }

            if (argSet.containsKey("-I")) {
                inputFilename = null;
                optionDashIOn = true;
            }

            if (argSet.containsKey("-v")) {
                optionDashVOn = true;
            }
        }


        if (!optionDashIOn && inputFilename == null) {
            System.err.println(rb.getString("ERROR_PLEASE_SPECIFY_INPUT_FILENAME"));
        }

        if (!optionDashOOn && outputFilename == null) {
            System.err.println(rb.getString("ERROR_PLEASE_SPECIFY_OUTPUT_FILENAME"));
        }

        if (!optionDashIOn && !optionDashOOn) {
            // use last two arguments as input/output filename
            if (args.length - switchSet.keySet().size() < 2) {
                printHelpMessage();
                return;
            }
        }

        // TODO: Remove this test stuff.

        new Huffman(inputFilename, outputFilename, optionDashVOn, 0);

//        ProgressBar.initialize("1000");
//        for (int i = 0; i <= 1000; i+= 10) {
//            for (int j = 0; j <= 1000000; j++) {
//                System.out.println("?");
//            }
//            ProgressBar.add(i);
//
//        }
    }
}