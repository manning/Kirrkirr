package Kirrkirr.util;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.*;

import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.DictField;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/** This class is a utility class that contains static methods and objects
 *  that are used to perform routine tasks.
 *
 *  @author Wee Jim Sng
 *  @author Christopher Manning
 *  @author Conrad Wai 2002
 */
public class Helper {

    /** Limit to length of filenames generated for auto-generated
     *  HTML/XML/etc. files.
     */
    public static final int MAX_FILE_NAME = 100;

    private Helper() {}


    /** Make a uniqueKey for a word from the word and a uniquifier.
     *
     *  @param word The display value string for the word.  May not be null
     *  @param uniquifier The string used to make the word unique, if any
     *  @return The uniqueKey
     */
    public static String makeUniqueKey(String word, String uniquifier) {
        if (word == null || uniquifier == null || uniquifier.isEmpty()) {
            return word;
        } else {
            return word + '@' + uniquifier;
        }
    }


    /** Makes a printable representation of a uniqueKey.  This isn't used
     *  internally in data structures, but is sometimes shown to the user.
     *  @param uniqueKey The String unique key
     *  @return The printable String representation of the unique key
     */
    public static String uniqueKeyToPrintableString(String uniqueKey) {
        StringBuilder sb = new StringBuilder(Helper.getWord(uniqueKey));
        String uniquifier = Helper.getUniquifier(uniqueKey);
        if (uniquifier != null && ! uniquifier.isEmpty() &&
                ! uniquifier.equals("#") && ! uniquifier.equals("?")) {
            sb.append(" (").append(uniquifier).append(')');
        }
        return sb.toString();
    }


    public static String printableStringToUniqueKey(String printable) {
        int pos = printable.lastIndexOf('(');

        //if no '(' or if '(' is part of word, printable form is unique
        if((pos == -1) || (printable.charAt(pos - 1) != ' ')) return printable;
        else {
            String uKey = printable.substring(0, pos-1);
            int pos2 = printable.lastIndexOf(')');
            return makeUniqueKey(uKey, printable.substring(pos+1, pos2));
        }
    }




    /** Use this when a unique key lookup has failed - even though we didn't
     *  know this before, we now know that this isn't a valid dictionary
     *  key.
     *
     *  @param uniqueKey A uniqueKey which normally assumes that the word exists
     *  @return The special uniqueKey suffix for unfindable words
     */
    public static String makeKeyUnknown(String uniqueKey) {
        return makeUniqueKey(getWord(uniqueKey), DictField.UNKNOWN);
    }


    /** The new way to display words.
     *
     *  @param uniqueKey A uniqueKey handle for a dictionary entry
     *  @return The part representing the word (i.e., the part before the '@')
     */
    public static String getWord(String uniqueKey) {
        if (uniqueKey == null) return null;
        int i = uniqueKey.lastIndexOf('@');
        if (i < 0) return uniqueKey;
        return uniqueKey.substring(0,i);
    }


    /** Return the 'padding' part of a <code>uniqueKey</code> that is added
     *  to a word to make it unique.  If the parameter is <code>null</code>,
     *  then <code>null</code> is returned, otherwise the stuff after the
     *  <code>'@'</code> in the unique key.  (Note that it is strictly stuff
     *  <i>after</i> the <code>'@'</code> now.
     *  @param uniqueKey The <code>String</code> unique key
     *  @return The uniquifying part, as above, or <code>null</code>
     */
    public static String getUniquifier(String uniqueKey) {
        if (uniqueKey == null) {
            return null;
        }
        final int i = uniqueKey.indexOf('@');
        if (i < 0) {
            return null;
        } else {
            // this is okay: argument can be uniqueKey.length() and "" returned
            return uniqueKey.substring(i + 1);
        }
    }


    /** Sometimes unique keys represent things that may not be or are
     *  definitely not in the dictionary.  This is done via two special
     *  uniquifier values, # and ?.  This one asks whether the word is
     *  unknown (#).
     */
    public static boolean unknownWord(String uniqueKey) {
        return "#".equals(getUniquifier(uniqueKey));
    }


    /** Sometimes unique keys represent things that may not be or are
     *  definitely not in the dictionary.  This is done via two special
     *  uniquifier values, # and ?.  This one asks whether the word is
     *  unknown (#), or known but the unquifier is not resolved (?).  In
     *  most circumstances, either of these make the word effectively
     *  "unknown".
     *  "?" means unknown word; "#" means unknown uniquifier (hnum)
     */
    public static boolean unresolvableWord(String uniqueKey) {
        String uniquifier = getUniquifier(uniqueKey);
        return "#".equals(uniquifier) || "?".equals(uniquifier);
    }


    /** Sometimes unique keys represent things that may not be or are
     *  definitely not in the dictionary.  This is done via two special
     *  uniquifier values, # and ?.  This one asks whether the word is
     *  resolvable to a dictionary headword.
     */
    public static boolean isResolved(String uniqueKey) {
        return ! unresolvableWord(uniqueKey);
    }


    /*static public String glossFileToWord(String file){
      if(file == null || ! file.startsWith("@")) {
      return null;
      }
      int a = file.lastIndexOf('@');
      return file.substring(1, a).replace('@',' ');
      }*/


    /**
     * Convert from URL form of filename convention (@word@uniquifier.html)
     * to uniqueKey.
     * WARNING: can't be used with gloss (because of
     * glosses with / and \
     * @param url the URL of the cached html filename
     * @return a uniqueKey String containing the word and polysemy in the
     *         filename,
     *         or null if the URL was null or the filename did not start
     *         with @
     * @see #wordToCacheFilename(String)
     * @see #cacheFilenameToUniqueKey(String)
     */
    public static String cacheFileURLToUniqueKey(URL url) {
        return cacheFilenameToUniqueKey(RelFile.getFile(url.toString()));
    }

    /**
     * Convert from filename convention (@word@uniquifier.html) to
     * uniqueKey containing the word and uniquifier.
     * @param file the filename to convert from
     * @return the unique key corresponding to this filename,
     *        or null if the file is null or does not begin with @
     * @see #cacheFileURLToUniqueKey(URL)
     * @see #wordToCacheFilename(String)
     */
    public static String cacheFilenameToUniqueKey(String file) {
        if (file == null)
            return null;

        file =  URLHelper.decode(file);

        //check format of start of decoded filename
        if (!file.startsWith("@")) return null;

        int a = file.lastIndexOf('@');
        String value = file.substring(1,a).replace('@', ' ');
        int dotIndex = file.lastIndexOf('.');
        String uniquifier;

        if (dotIndex == a+1) {  //no uniquifier - default to zero
            uniquifier = "";
        } else {
            uniquifier = file.substring(a + 1, dotIndex);
        }

        return makeUniqueKey(value, uniquifier);
    }


    /**
     * Convert from word (or maybe help file name) to filename:
     * '@word@uniqifier.html' for a word or 'word' for help.
     * @param uniqueKey The headword headword, or help filename
     * @return the filename of the cached html file of this word,
     *         or the html help file (for now, just 'word')
     *         or null if word is null.
     * @see #cacheFilenameToUniqueKey(String)
     * @see #cacheFileURLToUniqueKey(URL)
     */
    public static String wordToCacheFilename(String uniqueKey) {
        return wordToFilename(uniqueKey, "html");
    }


    public static String wordToXmlFilename(String uniqueKey) {
        return wordToFilename(uniqueKey, "xml");
    }


    /** Make sure html entries are legal filenames in Windows and Mac.
     *  update 6/27/2003: now using UTF-8 encoding of the following
     *  concatenation:'@WORD@POLY.extension'
     */
    private static String wordToFilename(String uniqueKey, String extension) {
        String file;
        String encIn;

        if (Dbg.FILE) Dbg.print("wordToFilename " + uniqueKey);
        if (uniqueKey.endsWith(Kirrkirr.HELP_FILE_END)) {
            encIn = uniqueKey;
        } else {
            encIn = "@" + uniqueKey + "." + extension;
        }
        file = URLHelper.encode(encIn);
        if (Dbg.FILE) Dbg.print("wordToFilename: encoded file is "+ file);

        if (file.length() > MAX_FILE_NAME)
            file = file.substring(0, MAX_FILE_NAME);

        return file;
    }


    public static void setCursor(Component c, boolean busy) {
        if (busy)
            c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        else
            c.setCursor(Cursor.getDefaultCursor());
    }

    /** Centre the frame on the screen */
    public static void centerFrame(Frame f) {
        Dimension screen  = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension d = f.getSize();

        int x = (screen.width - d.width) >> 1 ;         // divide by 2
        int y = (screen.height - d.height) >> 1;
        f.setLocation(x, y);
    }


    // Stuff for recognizing a Mac.  They keep changing it....
    // String lcOSName = System.getProperty("os.name").toLowerCase();
    // boolean MAC_OS_X = lcOSName.startsWith("mac os x");
    // String javaVersion = System.getProperty("java.version");
    // if (javaVersion.startsWith("1.4")) {
    //   New features for 1.4
    // }
    // mrj.version is still present but disrecommended.
    // It became just "OS X" at some point

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf('.');
            if (dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }

    /** Returns onAMactrue if on JDK 8 or earlier, so old macOS setup */
    public static boolean onOlderJdk() {
        return getJavaVersion() <= 8;
    }

    public static boolean onAMac() {
        return onMacOSX() || onAMacLegacyTest();
    }

    private static boolean onAMacLegacyTest() {
        return System.getProperty("mrj.version") != null;
    }

    public static boolean onClassicMac() {
        String os = System.getProperty("os.name");
        // MRJ
        String version = System.getProperty("mrj.version");
        return (os != null) && os.startsWith("Mac OS") &&
               ! os.startsWith("Mac OS X") &&
               (version != null) && (version.startsWith("2."));
    }

    public static boolean onMacOSX() {
        String osName = System.getProperty("os.name");
        return (osName != null) && (osName.startsWith("Mac OS X") ||
                osName.startsWith("OS X"));
    }


    /** Gets the translation of the word.
     *  All translations to call this method. It provides exception
     *  handling, eliminating numerous try and catch in the rest of the code.
     *  @param word The string to be translated
     *  @return The string translated into the current language
     */
    public static String getTranslation(String word) {
        if (word == null) {
            return null;
        }
        if (Kirrkirr.lang != null) {
            try {
                String s = Kirrkirr.lang.getString(word);
                if ( ! word.equals(s)) {
                    return s;
                }
            } catch (MissingResourceException e) {
                if (Dbg.THREE) {
                    Dbg.print("Missing resource translating " + word);
                }
            }
        }
        // return original word, if translation cannot be found, except
        // make underscores into spaces (spaces not allowed in Properties key)
        return word.replace('_', ' ');
    }


    /**
     * XML / DOM allow multiple Text nodes under a single Element.  This
     * function concatenates the Text children associated with an element
     * and returns the String.  Particularly, this is useful, for example,
     * when a "word" is broken up into multiple Text nodes because of the
     * existence of escaped character entities (e.g., as is sometimes the
     * case for accented characters).
     * This should replace elem.getFirstChild().getNodeValue() as the
     * "incantation" invoked when an Element's Text is desired.
     * @param   elem    the Element whose Text is sought
     * @return  a String that is the concatenation of all Text nodes under elem
     */
    public static String getElementText(Node elem) {
        // cw: could omit this...
        // cdm: actually maybe this is not just omittable but sometimes wrong
        //  for IndexMaker, it appears: value may be in attribute
        if (Dbg.FILE && elem.getNodeType() != Node.ELEMENT_NODE) {
            System.err.println("Helper:getElementText: nodeType is " +
                               elem.getNodeType() + " not Node.ELEMENT_NODE ("
                               + Node.ELEMENT_NODE + ")");
            System.err.println("  (2=attribute, 3=text, 4=cdata, 5=entityReference, ...)");
            // return ("");
        }

        NodeList children = elem.getChildNodes();
        int numKids = 0;
        if (children != null) {
            numKids = children.getLength();
        }
        if (numKids == 0) {
            if (Dbg.ERROR) {
                Dbg.print("Error: No children for " + elem + " (dict bug?)");
            }
        }
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < numKids; i++) {
            Node node = children.item(i);
            if (node != null && node.getNodeType() == Node.TEXT_NODE) {
                String value = node.getNodeValue();
                // was: if ((value != null) && (value.trim().length() > 0))
                // but using trim is presumably wrong? -- want any embedded
                // spaces. cdm Aug 2002
                if (value != null) {
                    sb.append(value);
                }
            }
        }
        return sb.toString();
    }

    public static void copySingleFile(File oldFile, File newFile)
    	throws IOException {

        InputStream in = new FileInputStream(oldFile);
        OutputStream out = new FileOutputStream(newFile);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void copyDirectory(File oldDir, String newParent)
    	throws IOException {
    	String oldDirName = oldDir.getName();
    	String newDirName = newParent + "\\" + oldDirName;
    	File newDir = new File(newDirName);
    	if (!newDir.exists())
    		newDir.mkdir();

    	File[] children = oldDir.listFiles();
        if (children == null) {
            return;
        }
        for (File curr : children) {
            if (curr.isDirectory()) { copyDirectory(curr, newDirName); } else {
                copySingleFile(curr, new File(newDirName + '\\' + curr.getName()));
            }
        }
    }

    /* ---------------------------
     * Convert Hashtable to an XML document and print it to the given stream.
     * Not actually currently used.
     * @param     outfilename   the filename to print the Document to
     * @param     valueTag      XML tag for the values entry (Key has tag "HW" by default)
     *
    public static int writeAsXML(Hashtable ht, String outfilename,
                                 String valueTag) {
        Document doc = new CoreDocumentImpl();//Document();
        Element root = (Element) doc.createElement("kirrkirr_"+valueTag);

        doc.appendChild (root);
        synchronized (ht) {
            Enumeration keys = ht.keys();

            while (keys.hasMoreElements()) {
                String s_word = (String) keys.nextElement();
                String s_value = (String) ht.get(s_word);
                String uniq = Helper.getUniquifier(s_word);
                s_word = Helper.getWord(s_word);

                if (s_value == null)
                    continue;

                Element entry = (Element) doc.createElement(DictField.ENTRY);
                Element e_word = (Element) doc.createElement(DictField.WORD);

                e_word.appendChild(doc.createTextNode(s_word));
                e_word.setAttribute(DictField.HNUM, uniq);
                entry.appendChild(e_word);

                Element e_value = (Element) doc.createElement(valueTag);
                e_value.appendChild(doc.createTextNode(s_value));
                entry.appendChild(e_value);

                root.appendChild(entry);
            }
        }
        try {
            //print the XML document
            // writeDocToFile(doc, outfilename);
            return 0;
        } catch (Throwable exception) {
            if (Dbg.ERROR)
                {
                    Dbg.print("Helper WriteAsXML");
                    exception.printStackTrace();
                }
            return -1;
        }
    }
  */

    /*
     * Create a Hashtable from an XML document.  Somewhat rotted.
     * @param     instream      the input stream containing the Document
     * @param     valueTag      XML tag for the values entry (Key has tag "HW" by default)
     *
     *
    public static int readFromXML(Hashtable ht, InputStream instream, String valueTag) {
        InputSource     input;
        Document     doc;

        try {

            input = new InputSource (instream);
            doc = doc.createDocumentFragment(input, false);

            NodeList entries = doc.getElementsByTagName(DictField.ENTRY);
            int i_entries = entries.getLength();

            synchronized (ht) {
                for (int i = 0 ; i < i_entries ; i++ ) {
                    Element     en_entry = (Element    )entries.item( i );

                    NodeList one_words = en_entry.getElementsByTagName(DictField.WORD);
                    Element     en_word = (Element    )one_words.item(0);
                    String word = en_word.getFirstChild().toString().trim();

                    String hnum = en_word.getAttribute(DictField.HNUM);

                    NodeList one_values = en_entry.getElementsByTagName(valueTag);
                    Element     en_value = (Element    )one_values.item(0);
                    String value = en_value.getFirstChild().toString().trim();

                    ht.put(Helper.makeUniqueKey(word, hnum), value);
                }
            }

            return(i_entries);

            /*} catch (SAXParseException err) {
            if (Dbg.ERROR) {
                Dbg.print("** Parsing error"
                                + ", line " + err.getLineNumber ()
                                + ", uri " + err.getSystemId ());
                Dbg.print("   " + err.getMessage ());
            }
            return(-1);
        } catch (SAXException e) {
            Exception   x = e.getException ();

            ((x == null) ? e : x).printStackTrace (System.err);
            return(-1);
        } catch (Throwable t) {
            if (Dbg.ERROR)
                {
                    Dbg.print("helper.java: readfromxml");
                    t.printStackTrace (System.err);
                }
            return(-1);
        }

    }*/


    /** Print a message for an exception */
    public static void handleException(Throwable exception) {
        // if(exception instanceof sun.applet.AppletSecurityException) {
        //     ;
        // }
        System.err.println("--------- UNCAUGHT EXCEPTION ---------");
        if (Dbg.ERROR) exception.printStackTrace();
    }

    /**static void writeDocToFile(Document minidoc, String filename) {
        System.err.println("got call to print "+filename+" but cant");
        /**try{
            FileWriter fw=new FileWriter(filename);
            minidoc.write(fw);
            fw.close();
            }
        catch(Exception e){e.printStackTrace();}
        /*try {
            FileWriter fw = new FileWriter(filename);

            doc.write(fw);
            fw.write(RelFile.lineSeparator());
            // [cdm: not necessary if about to close: fw.flush();
            fw.close();
        } catch(Exception e) {
            Helper.handleException(e);
            }
            }*/

} //end of class Helper

