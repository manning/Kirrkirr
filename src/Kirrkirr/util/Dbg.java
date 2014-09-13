package Kirrkirr.util;

import java.io.*;

/** Centralize the debugging print-outs, and make them more
 *  efficient when turned off at runtime.  Always use
 *      if (Dbg.WHATEVER) Dbg.print(str);
 *  Then no work is done (string isn't constructed) if debugging is turned off.
 */
public class Dbg {

    /** Stores the time of the current operation start. */
    private static long startTime = System.currentTimeMillis();

    /** This class cannot be instantiated. */
    private Dbg() {}

    public static final boolean ERROR = true; // An error has occured. Leave true.

    public static final boolean TIMING = false;
    public static final boolean VERBOSE = false;
    public static final boolean PARSE = false; // _very_ output intensive
    public static final boolean PROGRESS = false;
    public static final boolean HTML_PANEL = false;
    public static final boolean TWO = false;
    public static final boolean THREE = false;
    public static final boolean INDEX = false; // running IndexMaker
    public static final boolean LIST = false; // HeadwordList/WordList
    public static final boolean KEVIN = false;
    public static final boolean KRISTEN = false;
    public static final boolean K = false;
    public static final boolean MEMORY = false;
    public static final boolean STREE = false;
    public static final boolean LATTICE = false;
    public static final boolean FILE = false;
    public static final boolean CUTPASTE = false; // debug cut-and-paste
    public static final boolean GLOSSES = false;  // GlossList.java
    public static final boolean NETWORK = false;
    public static final boolean NOTES = false;
    public static final boolean CELLRENDERING = false;
    public static final boolean DICT_INFO = false; // DictionaryInfo
    public static final boolean CACHE = false;
    public static final boolean NEWFUN = false;
    public static final boolean SEARCH = false;
    public static final boolean DOMAINS = false; // fairly output intensive...
    public static final boolean DOMAINS2 = false; // more verbose still
    public static final boolean FONTS = false;
    public static final boolean DOMAIN_CONVERT = false;
    public static final boolean IMPORT_WIZARD = false;

    private static final boolean DEBUGTOFILE = false;
    private static PrintWriter debugFile;

    {
        // initialization block
        try {
            if (DEBUGTOFILE) {
                debugFile = new PrintWriter(new
                             BufferedWriter(new FileWriter("kdebug.log")));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


    /** Print the debug message to a file or stderr
     *  @param s The string to print
     */
    public static void print(String s) {
        if (DEBUGTOFILE && debugFile != null) {
            debugFile.println(s);
            // flush it to get as much as possible if it dies.
            debugFile.flush();
        } else {
            System.err.println(s);
        }
    }

    /** Print the string message to a file or stderr
     *  along with the line number. (However, note that
     *  the line number is likely to change as the file
     *  gets edited...).
     */
    public static void linePrint(int line, String s) {
        print(line + ": " + s);
    }

    /** Closes the debug file, if there is one.
     */
    public static void close() {
        if (debugFile != null) {
            debugFile.close();
        }
        debugFile = null;
    }


    /** Start the timing operation.
     */
    public static void startTime() {
        startTime = System.currentTimeMillis();
    }

    /** Print how long the timed operation took.
     *  @param str Additional string to be printed out at end of timing
     *  @return Number of elapsed milliseconds
     */
    public static long endTime(String str) {
        long elapsed = System.currentTimeMillis() - startTime;
        print(str + ": " + elapsed + " ms");
        return elapsed;
    }

    /** Print how much memory is in use.
     *  @param str Additional string to be printed out
     *  @return Number of bytes of memory in use.
     */
    public static long memoryUsage(String str) {
        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long mem = rt.totalMemory();
        print("Memory in use " + str + " " + (mem/1000) + " kb");
        return mem;
    }

}
