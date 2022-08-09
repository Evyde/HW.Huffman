package jlu.evyde;

import java.util.*;

public class Main {
    private static final ResourceBundle rb = ResourceBundle.getBundle("ManPage", Locale.US);

    private static void printHelpMessage() {
        System.out.println(rb.getString("DESCRIPTION"));
        System.out.println(rb.getString("USAGE"));
        System.out.println("-I\t\t" + rb.getString("DASH_I"));
        System.out.println("-O\t\t" + rb.getString("DASH_O"));
        System.out.println("-v\t\t" + rb.getString("DASH_V"));
        System.out.println("-h/--help\t" + rb.getString("DASH_H"));
    }

    public static void main(String[] args) {

        Map<String, Integer> argSet = new HashMap<>();
        String inputFilename = null;
        String outputFilename = null;
        boolean optionDashO = false;
        boolean optionDashI = false;

        int tempLoopVariable = 0;
        for (String a: args) {
            argSet.put(a, tempLoopVariable++);
        }

        // use last two arguments as input/output filename
        if (args.length < 2) {
            printHelpMessage();
            return;
        }
        inputFilename = "-h--help-V-I-O-v".contains(args[args.length - 2])? null: args[args.length - 2];
        outputFilename = "-h--help-V-I-O-v".contains(args[args.length - 1])? null: args[args.length - 1];

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
                    optionDashO = true;
                }
            }

            if (argSet.containsKey("-I")) {
                inputFilename = null;
                optionDashI = true;
            }

            if (argSet.containsKey("-v")) {

            }
        }


        if (!optionDashI && inputFilename == null) {
            System.err.println(rb.getString("ERROR_PLEASE_SPECIFY_INPUT_FILENAME"));
        }

        if (!optionDashO && outputFilename == null) {
            System.err.println(rb.getString("ERROR_PLEASE_SPECIFY_OUTPUT_FILENAME"));
        }

        System.out.println("Would read from " + (optionDashI? "standard input": inputFilename)
                + " and write out to " + (optionDashO? "standard output": outputFilename) + ".");


        // TODO: Turn these stackoverflow-like code to a independent class named "ProgressBar"
        char[] animationChars = new char[]{'|', '/', '-', '\\'};

        for (int i = 0; i <= 100; i++) {
            System.out.print("Processing: [");
            for (int j = 0; j < i; j++) {
                System.out.print("#");
            }

            for (int j = i; j < 100; j++) {
                System.out.print(" ");
            }

            System.out.print("] " + i + "% " + animationChars[i % 4] + "\r");

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Processing: Done!");
    }
}