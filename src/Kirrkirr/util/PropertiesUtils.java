package Kirrkirr.util;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;


/** This class had to be written to bridge the new (since jdk1.3) Properties
 *  API with the old Macintosh MRJ (jdk1.1), to avoid deprecation errors,
 *  and pick up the improvements.  Most of this is basically copied directly
 *  from Sun's java.util.Properties.java source, jdk1.4beta, with methods
 *  changed to be static, but a couple of extra convenience methods are
 *  added at the end.
 */
public final class PropertiesUtils {

    private static final String specialSaveChars = "=: \t\r\n\f#!";

    private PropertiesUtils() {
    }

    public static synchronized void load(Properties p,InputStream inStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream, "8859_1"));
        String line;
        // Get next line
        while ((line = in.readLine()) != null) {
            if (line.length() > 0) {
                // Continue lines that end in slashes if they are not comments
                char firstChar = line.charAt(0);
                if ((firstChar != '#') && (firstChar != '!')) {
                    while (continueLine(line)) {
                        String nextLine = in.readLine();
                        if(nextLine == null)
                            nextLine = "";
                        String loppedLine = line.substring(0, line.length()-1);
                        // Advance beyond whitespace on new line
                        int startIndex=0;
                        for( ; startIndex<nextLine.length(); startIndex++)
                            if (whiteSpaceChars.indexOf(nextLine.charAt(startIndex)) == -1)
                                break;
                        nextLine = nextLine.substring(startIndex,nextLine.length());
                        line = loppedLine+nextLine;
                    }

                    // Find start of key
                    int len = line.length();
                    int keyStart;
                    for(keyStart=0; keyStart<len; keyStart++) {
                        if(whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
                            break;
                    }

                    // Blank lines are ignored
                    if (keyStart == len)
                        continue;

                    // Find separation between key and value
                    int separatorIndex;
                    for(separatorIndex=keyStart; separatorIndex<len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);
                        if (currentChar == '\\')
                            separatorIndex++;
                        else if(keyValueSeparators.indexOf(currentChar) != -1)
                            break;
                    }

                    // Skip over whitespace after key if any
                    int valueIndex;
                    for (valueIndex=separatorIndex; valueIndex<len; valueIndex++)
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                            break;

                    // Skip over one non whitespace key value separators if any
                    if (valueIndex < len)
                        if (strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
                            valueIndex++;

                    // Skip over white space after other separators if any
                    while (valueIndex < len) {
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                            break;
                        valueIndex++;
                    }
                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

                    // Convert then store key and value
                    key = loadConvert(key);
                    value = loadConvert(value);
                    p.put(key, value);
                }
            }
        }
    }

    /**
     * Convert a nibble to a hex character
     * @param   nibble  the nibble to convert.
     */
    private static char toHex(int nibble) {
        return hexDigit[(nibble & 0xF)];
    }

    /*
     * Returns true if the given line is a line that must
     * be appended to the next line
     */
    private static boolean continueLine (String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while((index >= 0) && (line.charAt(index--) == '\\'))
            slashCount++;
        return (slashCount % 2 == 1);
    }
    private static final String whiteSpaceChars = " \t\r\n\f";
    private static final String keyValueSeparators = "=: \t\r\n\f";

   /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    private static String loadConvert (String theString) {
       int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len);

        for(int x=0; x<len; ) {
            char aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if(aChar == 'u') {
                    // Read the xxxx
                    int value=0;
                    for (int i=0; i<4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                          case '0': case '1': case '2': case '3': case '4':
                          case '5': case '6': case '7': case '8': case '9':
                             value = (value << 4) + aChar - '0';
                             break;
                          case 'a': case 'b': case 'c':
                          case 'd': case 'e': case 'f':
                             value = (value << 4) + 10 + aChar - 'a';
                             break;
                          case 'A': case 'B': case 'C':
                          case 'D': case 'E': case 'F':
                             value = (value << 4) + 10 + aChar - 'A';
                             break;
                          default:
                              throw new IllegalArgumentException(
                                           "Malformed \\uxxxx encoding.");
                        }
                    }
                    outBuffer.append((char)value);
                } else {
                    if (aChar == 't') aChar = '\t';
                    else if (aChar == 'r') aChar = '\r';
                    else if (aChar == 'n') aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    private static final String strictKeyValueSeparators = "=:";
    /** A table of hex digits */
    private static final char[] hexDigit = {
        '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
    };

    /** Copied from jdk1.4's java.util.Properties store (since
     *  jdk1.1 doesnt support store, and jdk1.1's save is
     *  deprecated).
     */
    public static void store(Properties p, OutputStream out, String header)
        throws IOException
    {
        BufferedWriter awriter;
        awriter = new BufferedWriter(new OutputStreamWriter(out, "8859_1"));
        if (header != null)
            writeln(awriter, "#" + header);
        writeln(awriter, "#" + new Date().toString());
        for (Enumeration e = p.keys(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            String val = (String)p.get(key);
            key = saveConvert(key, true);

            /* No need to escape embedded and trailing spaces for value, hence
             * pass false to flag.
             */
            val = saveConvert(val, false);
            writeln(awriter, key + "=" + val);
        }
        awriter.flush();
    }

    /** again, copied from sun's java.util.Properties
     */
    private static String saveConvert(String theString, boolean escapeSpace) {
        int len = theString.length();
        StringBuffer outBuffer = new StringBuffer(len*2);

        for(int x=0; x<len; x++) {
            char aChar = theString.charAt(x);
            switch(aChar) {
                case ' ':
                    if (x == 0 || escapeSpace)
                        outBuffer.append('\\');

                    outBuffer.append(' ');
                    break;
                case '\\':outBuffer.append('\\'); outBuffer.append('\\');
                          break;
                case '\t':outBuffer.append('\\'); outBuffer.append('t');
                          break;
                case '\n':outBuffer.append('\\'); outBuffer.append('n');
                          break;
                case '\r':outBuffer.append('\\'); outBuffer.append('r');
                          break;
                case '\f':outBuffer.append('\\'); outBuffer.append('f');
                          break;
                default:
                    if ((aChar < 0x0020) || (aChar > 0x007e)) {
                        outBuffer.append('\\');
                        outBuffer.append('u');
                        outBuffer.append(toHex((aChar >> 12) & 0xF));
                        outBuffer.append(toHex((aChar >>  8) & 0xF));
                        outBuffer.append(toHex((aChar >>  4) & 0xF));
                        outBuffer.append(toHex( aChar        & 0xF));
                    } else {
                        if (specialSaveChars.indexOf(aChar) != -1)
                            outBuffer.append('\\');
                        outBuffer.append(aChar);
                    }
            }
        }
        return outBuffer.toString();
    }

    /** again, copied from sun's java.util.Properties
     */
    private static void writeln(BufferedWriter bw, String s)
            throws IOException {
        bw.write(s);
        bw.newLine();
    }


    /* -- below here is new stuff -- */


    /** Writes the value into the key of the Properties file.
     *  This instantaneously updates the file that holds properties.
     *  2003 change: This now doesn't try to interact with the
     *  System.getProperties properties, but maintains a separate set, in
     *  line with Properties in JDK1.3+.
     *
     *  @param prop A set of Properties.  Assumed non-<code>null</code>.
     *  @param outputPropertiesFileName Writes Properties to here on every call
     *  @param key The key in the propertiePs file to update
     *  @param value The new value for the key
     */
    public static void changeProperty(Properties prop,
                                      String outputPropertiesFileName,
                                      String key,
                                      String value) {
        prop.put(key, value);
        // do this properly closing the file afterwards now.  Not closing
        // the file seemed to cause an error on the Mac.
        // this will only work for File, but otherwise just not saved
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputPropertiesFileName);
            // only store kirrkirr ones not dictionary ones!
            Properties p2 = new Properties();
            for (Enumeration e = prop.keys(); e.hasMoreElements(); ) {
                String k = (String) e.nextElement();
                if ( ! k.startsWith("dictionary.")) {
                    p2.setProperty(k, prop.getProperty(k));
                }
            }
            PropertiesUtils.store(p2, fos, "Kirrkirr.properties file");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }
    }

    /*  HISTORICAL NOTE FROM WHEN WE MANIPULATED System Properties:
     *  This method wraps the System.getProperty method.  Crucially,
     *  it only calls System.getProperty if we're not running as an
     *  applet, as some applet security managers (e.g., MRJ 2.1)
     *  don't allow applets to access any but default properties.
     *  You should call this method only for non-default properties,
     *  as it doesn't check for them by string equality.
     *  @param key the system property to lookup
     *  @return null if running as an applet, or the system property stored
     *  under the key.
     */

}

