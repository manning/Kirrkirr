package Kirrkirr;

import Kirrkirr.util.*;
import Kirrkirr.dictionary.*;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.apache.xpath.XPathAPI;


/** IndexMaker makes an index file for an XML dictionary.
 *  java IndexMaker existingXmlFile wrlIndexFile engIndexFile <br>
 *            - will create a new index file with the give name <br>
 *  java IndexMaker existingIndexFile        <br>
 *            - will print out the entries contained in the index.<br>
 *  java IndexMaker -i prompt for arguments.
 *  @author Kevin Jansz
 *  @author Kristen Parton
 *  @author Christopher Manning
 */
public class IndexMaker {

    // currently all static
    private IndexMaker() {}


    /** index has either gloss String:GlossDictEntry or uniqueKey headWord String:DictEntry
     *  So value is not currently single type.
     */
    private static Hashtable<String,Object> index;

    //headwords has list of gloss glosses or headword headwords
    private static Vector<String> headWords;

    //dictinfo provides access to dictionary specification information
    private static DictionaryInfo dictinfo;

    // fpos is a list of the fpos for the headwords in the file. good for both
    // headword and gloss production
    private static Vector<Long> fpos;

    // how many entries to read (neg = infinite)
    private static int readLimit = -1;

    private static final int MARKLIMIT = 0;

    private static DomainConverter dc;
    private static boolean usingDomainConverter = false;

    public static String lastError;


    /** These are actual gloss or headword items in the dictionary, but
     *  shouldn't be part of the headword/gloss list.
     *  (They aren't STOPCHARS because some of them may be
     *  okay within a word, just not as a word by themselves).
     *
     *  These are kinda of doing "double duty" right now, being checked in
     *  both gloss and headwords.  Perhaps the two should be distinct?
     *  - cw 2002
     */
    private static Vector dictErrors;


    public static void main(String[] argv) {
        if (argv.length == 1) {
            if (argv[0].equals("-i")) { //if interactive flag set, go
                //into interactive mode
                enterInteractiveMode();
            } else {
                printClickFile(argv[0]);
            }
        } else if (argv.length < 3) {
            System.out.println("IndexMaker usage error:");
            System.out.println("java Kirrkirr.IndexMaker xmlDictionarySpecFile xmlDictionary clkIndexFile [reverseClkIndexFile [domainFile]] [-Llimit]");
            System.out.println("   java Kirrkirr.IndexMaker clkIndexFile");
            System.out.println("or java Kirrkirr.IndexMaker -i  for interactive mode");
        } else {
            String reverse = null;
            String dom = null;
            String domConv = null;
            int max = argv.length - 1;
            if (argv[max].startsWith("-L")) {
                readLimit = Integer.parseInt(argv[max].substring(2));
                max--;
            }
            if (max >= 3) {
                reverse = argv[3];
                if (max >= 4) {
                    dom = argv[4];
                    if (max >= 5) {
                        domConv = argv[5];
                    }
                }
            }
            makeIndexFiles(argv[0], argv[1], argv[2], reverse, dom, domConv, null);
        }
    }


    private static void enterInteractiveMode() {
        boolean done = false;
      BufferedReader reader;

        try {
            reader = new BufferedReader(new InputStreamReader(System.in));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RelFile.init("");
        System.out.println("Welcome to IndexMaker");
        while ( ! done) {
            String reverseIndex = null;
            String domainFile = null;
            String domainConvFile = null;
            String forwardIndex = null;
            System.out.print("Enter XML dictionary filename: ");
            String dictFile = safeReadLine(reader);

            System.out.print("Enter XML dictionary specification filename: " );
            String specFile = safeReadLine(reader);

            if (request("forward index", reader)) {
                System.out.print("Enter forward index filename: ");
                forwardIndex = safeReadLine(reader);
            }

            if (request("reverse index", reader)) {
                System.out.print("Enter reverse index filename: ");
                reverseIndex = safeReadLine(reader);
            }

            if (request("domain file", reader)) {
                System.out.print("Enter domain index filename: ");
                domainFile = safeReadLine(reader);
            }

            if (request("use domain conversion file", reader)) {
            	System.out.println("Enter domain conversion filename: ");
            	domainConvFile = safeReadLine(reader);
            }

            if (forwardIndex!=null || reverseIndex!=null ||
                    domainFile != null) {
                if ( ! makeIndexFiles(specFile, dictFile, forwardIndex,
                                   reverseIndex, domainFile, domainConvFile, null)) {
                    Dbg.print("Couldn't make index files");
                    Dbg.print(lastError);
                }
            }
            done = ! getYesOrNo("Would you like to make more indices? ",
                                reader);
        }
        System.out.println("Goodbye.");
    }


    private static String safeReadLine(BufferedReader reader) {
        String resp = null;
        try {
            resp = reader.readLine();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return resp;
    }

    private static boolean request(String type, BufferedReader reader) {
        return getYesOrNo("Would you like to create a " + type + "? ", reader);
    }

    private static boolean getYesOrNo(String prompt, BufferedReader reader) {
        System.out.print(prompt);
        String response = safeReadLine(reader);
        char ch = response.charAt(0);
        return (ch == 'Y' || ch == 'y');
    }


    /** Build in the class variable fpos a list of dictionary entries and
     *  byte offsets of their starts.
     *  Dictionary entry start positions are discovered by a regex based on
     *  the DictionaryInfo getEntryTag() which is calculated from the
     *  DICTIONARY_ENTRY_XPATH.  That is, it just looks for the element name
     *  of an entry and ignores any of the enclosing XML structure. Beware!!
     */
    private static boolean getFilePositions(String dictFilename, String
                                           dictSpecFile, IndexMakerTracker
                                           progressTracker) {
        try {
            dictinfo = new DictionaryInfo(dictSpecFile);
        } catch (Exception e) {
            Dbg.print("Couldn't load DictionaryInfo!");
            e.printStackTrace();
            lastError = e.toString();
            return false;
        }
        String entryTag = dictinfo.getEntryTag();
        Regex startentryreg = OroRegex.newRegex("<"+entryTag+"(?: [^>]*)?>");
        Dbg.print("getFilePositions: entry regex is " + startentryreg);
        fpos = new Vector<Long>(9000, 1000);
        int success = readFilePositions(fpos, dictFilename, startentryreg,
                                      progressTracker);
        if (Dbg.INDEX && success < 40) {
            Dbg.print("Dumping fpos index");
            for (int k = 0; k < success; k++) {
                Dbg.print(k + ": " + fpos.elementAt(k));
            }
        }

        //additionally, we only want to set up dictErrors once
        dictErrors = dictinfo.getDictErrors();

        return success != -1;
    }


    /** This routine builds the index files for an XML file.
     *  It needs the dictionary specification xml file. It needs
     *  full filenames for parameters.
     *  <p>
     *  makeIndexFiles catches all internal errors
     *  (usually file IOExceptions). If there is one, it returns false,
     *  and the public variable <code>lastError</code> is set to the
     *  toString form of the error.
     *
     *  @param dictInfoFile the dictionary specification filename
     *  @param dictFile the XML dictionary filename
     *  @param srcLangIndex the output file for the dictionary index
     *  @param glsLangIndex the output file for the reverse dictionary index
     *                      (ie the index from L2-&gt;L1).
     *  @param domainFile The output file for the semantic domains index
     *  @param domainConvFile A file of translations (name expansions)
     *                        for domains
     *  @param progressTracker A progress meter for index building
     *  @return true if it built file positions okay, else false
     */
    public static boolean makeIndexFiles(String dictInfoFile,
                                      String dictFile,
                                      String srcLangIndex,
                                      String glsLangIndex,
                                      String domainFile,
                                      String domainConvFile,
                                      IndexMakerTracker progressTracker) {

        //set up fpos indices for entries
        // if (fpos == null) {  // cdm aug 2006: we always want to rebuild
        if ( ! getFilePositions(dictFile, dictInfoFile, progressTracker)) {
            if (Dbg.ERROR) Dbg.print("IndexMaker: Unable to build fpos vector");
            return false;
        }
        // }

        //set up dictinfo (should be set up by getFilePositions)
        if (dictinfo == null) {
            try {
                dictinfo = new DictionaryInfo(dictInfoFile);
            } catch (Exception e) {
                Dbg.print("Couldn't create DictionaryInfo!");
                e.printStackTrace();
                lastError = e.toString();
                return false;
            }
        }
        if (srcLangIndex != null) {
            ObjectOutputStream oos = null;
            try {
                oos = new ObjectOutputStream(new FileOutputStream(srcLangIndex));
                convertSourceLang(oos, dictFile, srcLangIndex, progressTracker);
                oos.close();
            } catch (Exception e) {
                // abandon hope if try to build source lang part and fail!
                e.printStackTrace();
                lastError = e.toString();
                try {
                    progressTracker.maybeDispose();
                    if (oos != null) oos.close();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                return false;
            }
        }

        String glossLangXPath = dictinfo.getGlossXPath();
        if (glossLangXPath != null && glsLangIndex != null) {
            ObjectOutputStream ous = null;
            try {
                ous = new ObjectOutputStream(new FileOutputStream(glsLangIndex));
                convertGlossLang(ous, dictFile, glsLangIndex, progressTracker);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                lastError = ioe.toString();
                return false;
            } finally {
                try {
                    if (ous != null) ous.close();
                } catch (IOException ioe2) {
                }
            }
        }
        if (domainFile != null) {
            if (domainConvFile != null) {
                try {
                    dc = new DomainConverter(domainConvFile);
                    usingDomainConverter = true;
                } catch (Exception e) {
                    System.err.println("Couldn't create DomainConverter!");
                    e.printStackTrace();
                    lastError = e.toString();
                    return false;
                }
            }
            try {
                buildDomainFile(dictFile, domainFile, progressTracker);
            } catch (Exception e) {
                if (progressTracker != null) {
                    progressTracker.passDone();
                }
                System.err.println("Couldn't process domain info");
                e.printStackTrace();
                lastError = e.toString();
                return false;
            }
        }
        Dbg.print("IndexMaker: have finished making all index files.");
        return true;
    }


    private static int numgli;
    private static int totalgli;

    /** Builds the reverse dictionary index. Does two passes through
     *  the dictionary because you need to access the file through a
     *  RandomAccessFile to get the file positions, and you need an
     *  InputStream to use XmlMiniDocument.
     *  @param oos The object output stream corresponding to the
     *             outputted index file (.clk)
     *  @param xmlfilename The filename for the XML dictionary
     *  @param clkfilename The output index filename (only used
     *             for debugging printouts)
     *  @param progressTracker Thing tracking progress or null
     */
    public static void convertGlossLang(ObjectOutputStream oos,
                                        String xmlfilename,
                                        String clkfilename,
                                        IndexMakerTracker progressTracker) {
        numgli = 0;
        totalgli = 0;
        try {
            index = new Hashtable<String, Object>(12000);
            headWords = new Vector<String>(9000, 1000);
	    // Vector noGloss = new Vector();
            //String glossXPath=dictinfo.getBidirectionalXPath();
	    String glossXPath=dictinfo.getGlossXPath();
            String imagesXPath=dictinfo.getImagesXPath();
            String uniquekeyXPath=dictinfo.getUniquifierXPath();
            if (uniquekeyXPath == null) {
                System.err.println("No uniquifier defined; continuing");
            }
            Vector stopwords = dictinfo.getStopWords();
            Vector stopchars = dictinfo.getStopChars();
            // dictErrors was set up in getFilePositions earlier
            String headwordXPath=dictinfo.getHeadwordXPath();
            // String dictTag=dictinfo.getDictionaryTag();
            String entryTag=dictinfo.getEntryTag();
            //      Regex endentryreg=OroRegex.newRegex("</"+entryTag+">");

            String firstimage = null;
            long curFPos = 0;
            int wordcount = 0;

            int i=0;
            InputStream bis = new BufferedInputStream(
                                        new FileInputStream(xmlfilename));

	    int fposSize = fpos.size();
            if (progressTracker != null) {
                progressTracker.totalStepsForPass(fposSize);
            }

            boolean givenWarning = false;
            while (i<fposSize && headWords.size() != readLimit) {
                long lastFPos = curFPos;
                curFPos=((Long)fpos.elementAt(i)).longValue();
                i++;

                try {
                    bis.reset();
                    bis.skip(curFPos-lastFPos);
                } catch(Exception ee) {
                    bis.close();
                    bis = new BufferedInputStream(new FileInputStream(xmlfilename));
                    bis.skip(curFPos);
                }
                bis.mark(MARKLIMIT);
                XmlMiniDocument minidoc = new XmlMiniDocument(headwordXPath, entryTag);
                Document doc = minidoc.setup();
                doc = minidoc.parseElement(bis, doc);
                doc = minidoc.finish(doc);

                // cw 2002: this may be a little misleading, as one only
                // gets told that it's skipping entries/hws (which is
                // fine) if it happens to be on a wordcount divisible by
                // 100 - such as 0.
                if (wordcount % 100 == 0) {
                    if(progressTracker!=null)
                        progressTracker.stepsDone(wordcount);
                    System.out.println("Second pass (backward): Have parsed " +
                                       wordcount +
                                       " words so far, am at fpos "+curFPos);
                }

                // check for restricted XPath, and if so exclude entry!!
                NodeList nl=getElementsFromDoc(dictinfo.getDictionaryEntryXPath(), doc);
                if (nl != null && nl.getLength() == 0) {
                    nl = null;
                }
                String headword = getElementFromDoc(headwordXPath, doc);
                if (nl == null || headword == null || headword.isEmpty() ||
                        dictErrors.contains(headword)) {
                    if ( ! givenWarning) {
                        givenWarning = true;

                        Dbg.print("IndexMaker: null or zero length headword okay?  Word num = " +
                                  wordcount);
                        Dbg.print("  It may be okay if restrictions on XPath");
                        Dbg.print("  headwordXPath is " + headwordXPath);
                        Dbg.print("  dictionaryEntryXPath is " + dictinfo.getDictionaryEntryXPath());
                    }
                    continue;
                }

                NodeList glossNodes = getElementsFromDoc(glossXPath, doc);

                if (glossNodes == null) continue;
                if (imagesXPath != null)
                    firstimage = getElementFromDoc(imagesXPath, doc);
                String uniquekey = createUniqueKey(headwordXPath, uniquekeyXPath, doc);

                doOneGloss(glossNodes, firstimage != null, uniquekey,
                           stopwords, stopchars);

                wordcount++;
                if (Dbg.INDEX){
                    Dbg.print("-----------------");
                }
            }
            bis.close();

	    System.err.println("Read " + wordcount + " dictionary entries");
	    System.err.println("STOP_CHARS was " + stopchars);
            System.err.println("Total num unique gli "+numgli+
                               " nonunique "+totalgli+
                               " total mainwords "+headWords.size());
            System.err.println("Saving gloss index to file: "+clkfilename);

            //NB: It's important to save a sorted vector, for
            //quick resetting of the word list!
            sortGlosses();

            // put the headWords vector into the hashtable
            index.put(DictField.MAINWORDS, headWords);

            oos.writeObject(index);
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if (progressTracker != null) {
                progressTracker.passDone();
            }
        }
    }


    /** Creates the unique key for a headword by
     *  concatenating the headword with the unique key
     *  (if one exists).
     *  @see Kirrkirr.util.Helper#makeUniqueKey(String, String)
     *  @param headwordXPath the XPath for the headword
     *  @param uniquekeyXPath the XPath for the unique key padding
     *  @param doc The Document from which to extract the headword
     *             and padding; ie the XML dictionary/minidoc representation
     */
    private static String createUniqueKey(String headwordXPath,
                                      String uniquekeyXPath, Document doc) {

        String id = getElementFromDoc(headwordXPath, doc);
        String uniquekey = null;
        if (uniquekeyXPath != null) {
            uniquekey = getElementFromDoc(uniquekeyXPath, doc);
        }
        if (Dbg.INDEX) {
            Dbg.print("createUniqueKey from " + id + " and " + uniquekey +
                      " gives " + Helper.makeUniqueKey(id, uniquekey) + ".");
        }
        return Helper.makeUniqueKey(id, uniquekey);
    }


    /** Sorts the glosses alphabetically,
     *  using regular AlphaComparator. Necessary since
     *  we want to save the words in alphabetical
     *  order so that "reset words" is fast.
     */
    private static void sortGlosses() {
        Collections.sort(headWords, new KAlphaComparator());
    }


    /** Creates a new GlossDictEntry for the gloss and
     *  fills it with info, then adds it to headWords and
     *  index. First filters the gloss phrase for specific
     *  Kirrkirr errors: 1) having quotes or other punctuation
     *  that would make the wordlist alphabetize strangely (esp
     *  the {??} problems) and 2) dictionary errors; see the
     *  array above. This step will probably have to be removed
     *  for making it general. The filters each word in the phrase
     *  for indexing, based on the stopwords and stopchars passed in.
     *  Each phrase is stored under each one of the words in the phrase,
     *  except for stopwords, and without punctuation (according to
     *  the vectors passed in). See filterStopWords for further details.
     *  @param glossNodes The NodeList of glosses for this headword
     *  @param hasPics    Whether this headword has pictures
     *  @param uniquekey  The actual unique key for this headword
     *  @param stopwords  The Vector of stopwords for this language
     *  @param stopchars  The Vector of stopchars for this language
     */
    private static void doOneGloss(NodeList glossNodes, boolean hasPics,
                                   String uniquekey, Vector stopwords,
                                   Vector stopchars) {
        for (int i = 0, gnl = glossNodes.getLength(); i < gnl; i++) {
            // gloss = Helper.getElementText(glossNodes.item(i));
            String gloss = Helper.getElementText(glossNodes.item(i));

            if (gloss == null) continue;
            gloss = gloss.trim();
            if (gloss.equals("")) continue;

            if (Dbg.INDEX) {
                Dbg.print(gloss);
            }
            //takes care of some punctuation/dict errors
            //in the way the subword phrases/words appear
            //which may be headword specific
            gloss = filterPhrase(gloss, stopchars);
            if (gloss == null) continue;

            // breaks a subword/phrase into words, or returns null
            // if this is a main word (a single word gloss)
            Vector glossWords = filterStopWords(gloss, stopwords, stopchars);
            totalgli++;

            GlossDictEntry newEntry;
            if (index.containsKey(gloss)) {
                newEntry=(GlossDictEntry)index.get(gloss);
                if (!newEntry.hasPics && hasPics) {
                    newEntry.hasPics = true;
                }
            } else {
                numgli++;
                newEntry = new GlossDictEntry();
                newEntry.hasPics=hasPics;
            }
            newEntry.freq++;
            newEntry.addHeadword(uniquekey);
            index.put(gloss,newEntry);

            // For listing subwords under their mainwords
            if (glossWords!=null){
                newEntry.isSubword=true;
                for (Enumeration e=glossWords.elements();e.hasMoreElements();){
                    String word=(String)e.nextElement();
		    word = word.toLowerCase();
                    totalgli++;
                    //For phrases/subwords, capitalization doesn't matter
                    //But for indexing main words, convert to lower case
                    //so subword phrases match up appropriately
                    GlossDictEntry ede=(GlossDictEntry)index.get(word);
                    //if the main word for a gloss isn't in
                    //the hashtable yet, create it (may end up
                    //being without a definition)
                    if (ede==null){
                        headWords.addElement(word);
                        numgli++;
                        ede = new GlossDictEntry();
                        index.put(word, ede);
                    }
                    ede.freq++;
                    ede.addSubword(gloss);
                }
            }

            if (!newEntry.isSubword && !headWords.contains(gloss))
                headWords.addElement(gloss);

            if (Dbg.INDEX) {
                System.out.print(gloss+" put under [");
                for (int j=0;j<newEntry.headwords.length;j++)
                    System.out.print(newEntry.headwords[j]+" ");
                System.out.println("] mainwords "+glossWords+
                                   " issub? "+newEntry.isSubword+" subword "+Arrays.toString(newEntry.subwords)+
                                   " image? "+newEntry.hasPics+" freq "+newEntry.freq);
            }
        }
    }


    /** Go through all the words in the gloss' phrase,
     *  removing stopwords in the order they appear in the stopWords
     *  array until 1) there is one word left in the phrase, or
     *  2) all stopwords are removed.
     *  <P>Examples: "to be" removes "to", realizes that "be" is the only one
     *  left, and returns a Vector with "be"
     *  "abandoned camp of deceased person" returns a Vector with
     *  "abandoned", "camp", "deceased", "person" </P>
     *  @param gloss the String gloss
     *  @param stopwords An ordered list of stopwords
     *  @param stopchars The stopchars are stripped again from each wor
     *  @return null if the gloss is not a phrase (ie has no spaces, so is
     *          a mainword), or a Vector of at least one String mainword
     *          to index the gloss phrase under
     */
    private static Vector filterStopWords(String gloss, Vector stopwords,
                                          Vector<String> stopchars) {
        int startWord = 0;
        int endWord = gloss.indexOf(" ");
        if (endWord == -1) {
            return null;
        }
        Vector<String> glossWords = new Vector<String>();
        String word = gloss.substring(startWord,endWord);
        while (word != null) {
            addWord(word, glossWords, stopchars);
            startWord = endWord + 1;
            if ((endWord=gloss.indexOf(" ", startWord))==-1) {
                word = null;
            } else {
                word = gloss.substring(startWord,endWord);
            }
        }
        //get the last word remaining (After the last space).
        if (startWord < (gloss.length()-1)) {
            word=gloss.substring(startWord,gloss.length());
            addWord(word,glossWords,stopchars);
        }
        for (Enumeration e = stopwords.elements();
             e.hasMoreElements() && glossWords.size() > 1; ) {
            word=(String)e.nextElement();
            glossWords.removeElement(word);
        }
        if (Dbg.INDEX) {
            System.err.print("putting "+gloss+" under: ");
            for (int i=0;i<glossWords.size();i++){
                System.err.print("|"+glossWords.elementAt(i)+"| ");
            }
            System.err.println();
        }
        if (glossWords.isEmpty()) {
            return null;
        }
        return glossWords;
    }


    /** Short helper function which filters the stopchars
     *  from a word, handles the String/stringbuffer conversion
     *  and adds the new word to glosswords if it isn't
     *  null or already there.
     *  @param word a single word of a gloss phrase
     *  @param glossWords a vector containing all the filtered
     *                    words in a gloss phrase
     *  @param stopChars a vector of characters to filter in the word
     */
    private static void addWord(String word, Vector<String> glossWords,
                                Vector<String> stopChars) {
        StringBuffer sb=filterStopChars(word, stopChars);
        if (Dbg.INDEX) {
            Dbg.print(word+" -> "+sb);
        }
        if (sb==null) return;
        String w=sb.toString().trim();
        if ( ! w.equals("")) {
            if (!glossWords.contains(w)) {
                glossWords.addElement(w);
            }
        }
    }


    /** Filters a word of all instances of "stopchars."
     *  The only subtlety is that if an apostrophe is
     *  mid-word, it remains. This may be headword specific?
     *
     *  @param gloss the single gloss word to be filtered
     *  @param stopchars the characters to filter out of the gloss
     */
    private static StringBuffer filterStopChars(String gloss,
                                                Vector<String> stopchars) {
        if (gloss==null) return null;
        StringBuffer temp = new StringBuffer(gloss);
        boolean changed = false;
        int stopsize = stopchars.size();
        if (Dbg.INDEX) {
            Dbg.print("Gloss is |" + gloss + "|; filtering " + stopsize +
                      " stopChars: " + stopchars);
        }
        // note that the length of temp shrinks as chars are deleted...
        for (int i=0; i<temp.length(); ) {
            //check for STOPCHARS
            char ch = temp.charAt(i);
            for (int j=0; j<stopsize && !changed; j++) {
                if (stopchars.elementAt(j).charAt(0) == ch) {
                    //ie, it's okay for a ' to be mid-word
                    if (ch != '\'' ||
                            i == 0 ||
                            i == (temp.length() - 1) ||
                            ! Character.isLetter(temp.charAt(i - 1)) ||
                            ! Character.isLetter(temp.charAt(i + 1))) {
                        temp = deleteCharAt(i, temp);
                        changed = true;
                    }
                }
            }
            if (! changed) {
                i++;
            } else {
                changed = false;
            }
        }
        if (temp.length() == 0)
            return null;
        if (Dbg.INDEX) Dbg.print("  result is |" + temp + "|");
        return temp;
    }


    /** Standard function for StringBuffers, but not available
     *  for StringBuffers in jdk1.1
     */
    private static StringBuffer deleteCharAt(int i, StringBuffer sb) {
        String temp=sb.toString();
        //substring(int, int) also not for StringBuffers in jdk1.1
        StringBuffer toreturn=new StringBuffer(temp.substring(0,i)).append(temp.substring(i+1,temp.length()));
        if (Dbg.INDEX) {
            Dbg.print(temp + "\t-->\t" + toreturn.toString());
        }
        return toreturn;
    }


    /** Removes what are regarded as 'stop characters' from the gloss
     *  string.
     *  First, <code>filterStopChars(String, Vector)</code> is used to
     *  remove the stop chars specified in the Vector (except that there
     *  is currently special hardwired handling of "'" so that it is
     *  only removed at beginning, and end of string and before white space
     *  even if it is specified in the stop chars vector.
     *  Second, a gloss-final comma is removed.
     *  Third, leading and trailing whitespace is removed.
     *  Fourth, if the string is of zero length or it is contained in the
     *  hardcoded <code>dictErrors</code> vector, then <code>null</code>
     *  is returned.
     *  So: <code>don't</code> stays <code>don't</code>
     *  but <code>'choo-choo'</code> goes to <code>choo-choo</code>
     *  This is probably headword specific and should be removed.
     *  @param gloss An unfiltered gloss phrase or word.
     *  @return A string filtered for apostrophes or <code>null</code>
     */
    private static String filterPhrase(String gloss, Vector stopchars) {
        StringBuffer temp = filterStopChars(gloss, stopchars);
        if (temp == null) return null;
        if (Dbg.INDEX) Dbg.print(gloss+"\t-->\t"+temp);
        gloss=temp.toString().trim();
        if (gloss.length() ==0 || dictErrors.contains(gloss)) {
            return null;
        }
        return gloss;
    }


    /** Builds the dictionary index. Does two passes through
     *  the dictionary because you need to access the file through a
     *  RandomAccessFile to get the file positions, and you need an
     *  InputStream to use XmlMiniDocument.
     *  <p>
     *  This method now throws exceptions rather than catching them.
     *  That way the calling code can abandon further processing.
     *
     *  @param oos The object output stream corresponding to the
     *             outputted index file (.clk)
     *  @param xmlfilename The filename for the XML dictionary
     *  @param clkfilename The output index filename (only used
     *             for debugging printouts)
     *  @param progressTracker For showing progress through the dictionary in GUI
     */
    private static void convertSourceLang(ObjectOutputStream oos, String xmlfilename,
                                          String clkfilename,
                                          IndexMakerTracker progressTracker) throws Exception {
        int numEntries = fpos.size();
        index = new Hashtable<String, Object>(numEntries);
        headWords = new Vector<String>(numEntries, 200);

        String headwordXPath=dictinfo.getHeadwordXPath();
        String freqXPath=dictinfo.getFrequencyXPath();
        String soundXPath=dictinfo.getAudioXPath();
        String imagesXPath=dictinfo.getImagesXPath();
        String subwordXPath=dictinfo.getSubwordXPath();
        // String dictTag=dictinfo.getDictionaryTag();
        String uniquekeyXPath=dictinfo.getUniquifierXPath();
        String entryTag=dictinfo.getEntryTag();

        String firstsound = null, firstimage = null;
        long curFPos = 0;
        int wordcount = 0;
        boolean givenWarning = false;

        InputStream bis = new BufferedInputStream(new FileInputStream(xmlfilename));

        //for testing
        //OutputStreamWriter ow = new OutputStreamWriter(ne

        //set up progress tracker for this pass
        if (progressTracker != null) {
            progressTracker.totalStepsForPass(numEntries);
        }

        int modulo = numEntries < 300 ? numEntries / 10: 100;

        for (int i = 0; (i<numEntries) && headWords.size() != readLimit; i++) {
            long lastFPos = curFPos;
            curFPos = fpos.elementAt(i).longValue();

            try {
                bis.reset();
                bis.skip(curFPos-lastFPos);
            } catch (Exception ee) {
                bis.close();
                bis=new BufferedInputStream(new FileInputStream(xmlfilename));
                bis.skip(curFPos);
            }
            bis.mark(MARKLIMIT);
            XmlMiniDocument minidoc = new XmlMiniDocument(headwordXPath, entryTag);

            Document doc = minidoc.setup();
            doc = minidoc.parseElement(bis, doc);
            doc = minidoc.finish(doc);

            //for testing
            /*doc = minidoc.setup(ow);
              doc = minidoc.parseElement(bis, doc, ow);
              doc = minidoc.finish(doc, ow);*/

            if (wordcount % modulo == 0) {
                if (progressTracker!=null) {
                    progressTracker.stepsDone(wordcount);
                }
                System.out.println("Second pass (forward): Have parsed "+wordcount+
                        " words so far, am at fpos "+curFPos);
            }
            String freqString = null;
            boolean subword = false;

            NodeList nl=getElementsFromDoc(dictinfo.getDictionaryEntryXPath(), doc);
            if (nl != null && nl.getLength() == 0) {
                nl = null;
            }
            String headword = getElementFromDoc(headwordXPath, doc);
            if (nl == null || headword == null || headword.isEmpty() ||
                    dictErrors.contains(headword)) {
                if ( ! givenWarning) {
                    givenWarning = true;

                    Dbg.print("IndexMaker: null or zero length headword okay?  Word num = " +
                            wordcount);
                    Dbg.print("  It may be okay if restrictions on XPath");
                    Dbg.print("  headwordXPath is " + headwordXPath);
                    Dbg.print("  dictionaryEntryXPath is " + dictinfo.getDictionaryEntryXPath());
                }
                continue;
            }

            if (freqXPath != null)
                freqString = getElementFromDoc(freqXPath, doc);
            if (soundXPath != null)
                firstsound = getElementFromDoc(soundXPath, doc);
            if (imagesXPath != null)
                firstimage = getElementFromDoc(imagesXPath, doc);
            if (subwordXPath != null) {
                String subwordString = getElementFromDoc(subwordXPath, doc);
                if (subwordString != null)
                    subword=true;
            }

            String uniquekey = createUniqueKey(headwordXPath, uniquekeyXPath, doc);
            if (uniquekey==null) {
                Dbg.print("IndexMaker: null uniquekey okay?  hw = " +
                        headword + " word num = " + wordcount);
                continue;
            }

            if (headword == null || headword.isEmpty() || dictErrors.contains(headword)) {
                continue;
            }

            doOneWord(freqString,curFPos,subword,firstimage!=null,
                    firstsound!=null,headword,uniquekey);

            wordcount++;
        }
        bis.close();

        System.err.println("Read " + wordcount + " dictionary entries");
        System.err.println("Saving headword index to file: " + clkfilename);

        // sort headWords vector alphabetically, since for some
        // dictionaries (e.g., for the Nahuatl), the default order is
        // not as meaningful (use these calls for Java 1.1 compatibility)
        // see KKListModel.toArray, ScrollPanel.alphaSort
        // - cw 2002

        // Sort by order in index file by default.  Alphabetical
        // sorting is provided as a user option in Kirrkirr.

//          String[] items = new String[headWords.size()];
//          headWords.copyInto(items);
//          Arrays.sort(items, new KAlphaComparator());
//          Vector hws = new Vector();
//          for (int v = 0; v < items.length; v++){
//              hws.addElement(items[v]);
//          }
        // put the sorted headWords vector hws into the hashtable
        index.put(DictField.H_WORDS, headWords);

//          // put the headWords vector into the hashtable
//             index.put(DictField.H_WORDS, headWords);

        oos.writeObject(index);

        //tell the progress tracker we're done
        if (progressTracker != null) {
            progressTracker.passDone();
        }
    }


    /** Creates a new DictEntry for one headword, fills
     *  it with info, and adds it to index and headWords.
     *  @param freqString The string for this headword's frequency
     *                    (may be null)
     *  @param lastFPos   This headword's file position
     *  @param isSubword  Whether this headword is a subword
     *  @param hasPics    Whether this headword has pictures
     *  @param hasSounds  Whether this headword has sounds
     *  @param headword   The headword
     *  @param uniquekey  The actual unique key for this headword
     */
    private static void doOneWord(String freqString, long lastFPos,
                                  boolean isSubword, boolean hasPics,
                                  boolean hasSounds, String headword,
                                  String uniquekey) {
        DictEntry newEntry = new DictEntry();
        newEntry.freq = 0;
        try {
            if (freqString != null) {
                newEntry.freq = Integer.parseInt(freqString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        newEntry.fpos = lastFPos;
        newEntry.isSubword = isSubword;
        newEntry.hasPics = hasPics;
        newEntry.hasSounds = hasSounds;
        // newEntry.display = headword;

        if (Dbg.INDEX){
            Dbg.print("new entry with uniqueid "+uniquekey+" headword "+headword+
                      " sounds? "+newEntry.hasSounds+" pics? "+newEntry.hasPics+
                      " fpos "+lastFPos+" isSub? "+isSubword+"\n-----------------");

        }

        headWords.addElement(uniquekey);
        index.put(uniquekey, newEntry);
    }


    /** Makes one pass through the dictionary file,
     *  greping for "startreg" and recording the
     *  filepositions at which it occurs into the
     *  fpositions vector.
     *  @param fpositions Vector to put Long file positions into
     *  @param filename the filename of the XML dictionary
     *  @param startreg the regular expression to grep for;
     *                  should match the start tag for ENTRY.
     *  @param progressTracker A GUI component for tracking progress
     *  @return The number of words found in the dictionary or -1 if there
     *          was an error
     */
    private static int readFilePositions(Vector<Long> fpositions, String filename,
                        Regex startreg, IndexMakerTracker progressTracker) {
        int total = 0;
        try {
            RandomAccessFile raf = new RandomAccessFile(filename, "r");
            if (progressTracker != null)
                progressTracker.totalStepsForPass((int) raf.length());
            long fpos = 0;
            String line = raf.readLine();
            while (line != null) {
                // read until you reach the current startentry
                // fpos is set _before_ reading line: points to beginning of it
                if (startreg.hasMatch(line)) {
                    fpositions.addElement(Long.valueOf(fpos));
                    total++;
                    if (progressTracker != null && total % 100 == 0) {
                        //every hundred entries, update tracker
                        progressTracker.stepsDone((int)fpos);
                    }
                    if (total % 1000 == 0) {
                        Dbg.print("First pass at fpos "+fpos+".");
                    }
                }
                fpos=raf.getFilePointer();
                line=raf.readLine();
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
            lastError = e.toString();
            total = -1;
        }
        Dbg.print("First pass (gathering headwords) found " + total +
                  " words in dictionary");
        if (progressTracker != null) {    // tell tracker we're done
            progressTracker.passDone();
        }
        return total;
    }


    /** Given an xpath and a document, this extracts
     *  the first matching node value from the document,
     *  or returns null if it couldn't find one.
     *  @param xpath The xpath to match
     *  @param doc The document to look in
     *  @return The value of the first node in doc that matches
     *  xpath, or null if none match.
     *  XXXX REVISE
     */
    public static String getElementFromDoc(String xpath, Document doc) {

        NodeList nl=getElementsFromDoc(xpath, doc);
        if (nl==null) return null;
        Node node=nl.item(0);
        if (node!=null) {
            if (Dbg.INDEX){
                Dbg.print("Node value: " + Helper.getElementText(node));
            }
            //since the dict now uses attributes as well (for subwords),
            //might not be element node - should check for type gracefully
            //rather then error when it's not an Element node.
            if(node.getNodeType() == Node.ELEMENT_NODE)
                return Helper.getElementText(node);
            else if(node.getNodeType() == Node.ATTRIBUTE_NODE)
                return node.getNodeValue();
            else
                return null;
        } else {
            if (Dbg.INDEX)
                Dbg.print("node was null for "+xpath);
            return null;
        }
    }


    /** Given an xpath and a document, this extracts
     *  all the matching nodes from the document,
     *  or returns null if it couldn't find any.
     *  @param xpath The xpath to match
     *  @param doc The document to look in
     *  @return The value of the matching nodes,
     *  or null if none match.
     */
    public static NodeList getElementsFromDoc(String xpath, Document doc) {
        if (xpath == null || doc == null) {
            Dbg.print("IndexMaker: null argument; xpath is " + xpath + "; doc is " + doc);
            return null;
        }
        try {
            // Element root = doc.getDocumentElement();
            // XObject obj=XPathAPI.eval(root,xpath);
            // NodeList nl= obj.nodelist();
            NodeList nl = XPathAPI.selectNodeList(doc, xpath);
            if (nl == null || nl.getLength() == 0) {
                return null;
            }
            return nl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /** Print the contents of an existing .clk file
     *  This works for either a Headword or an Gloss .clk file
     *  @param filename The .clk filename
     */
    private static void printClickFile(String filename) {
        try {
            RelFile.dumpSystemInfo();
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename));
            Hashtable<String,Object> garb = (Hashtable<String,Object>) ois.readObject();
            int j = garb.size();
            Vector<String> hws = (Vector<String>) garb.remove(DictField.H_WORDS);
            if (hws != null) {
                System.out.println("ClickFile Hashtable started with " + j +
                        " entries; H_WORDS vector is length " + hws.size());
                for (int i = 0; i < hws.size(); i++) {
                    System.out.print("hws[" + i + "]: |" + hws.elementAt(i) + "|");
                    Object thisOne = garb.get(hws.elementAt(i));
                    if (thisOne == null) {
                        System.out.println("  MISSING");
                    } else {
                        System.out.println("  " + thisOne);
                    }
                }
            } else {
                System.out.println("ClickFile Hashtable started with " + j +
                        " entries; no H_WORDS (i.e., Gloss)");
                Vector v = (Vector) garb.remove(DictField.MAINWORDS);
                if (v != null) {
                    System.out.println("mainwords is " + v);
                } else {
                    System.out.println("Mainwords is MISSING!");
                }
                int i = 0;
                for (String key : garb.keySet()) {
                    System.out.print(i + ": |" + key + "|");
                    Object thisOne = garb.get(key);
                    if (thisOne == null) {
                        System.out.println("  MISSING");
                    } else {
                        System.out.println("  " + thisOne);
                    }
                    i++;
                }
            }
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Builds the domain file from the specified dictionary file
     *  and places the results in the specified domain file.  Currently,
     *  a dummy root node is constructed on behalf of the tree as a
     *  top-level node with the empty string as a tag.
     *  @param dictFile the XML dictionary filename
     *  @param domFile the filename of the XML file where domain info will
     *  be written
     */
    public static void buildDomainFile(String dictFile,
                                       String domFile,
                                       IndexMakerTracker progressTracker) throws Exception {
        if (Dbg.INDEX) Dbg.print("Calling buildDomainFile on " + domFile);
        if (domFile == null) return; //shuld be no-op if domFile is null
            // we have those file positions, process the dictionary
        processDomains(dictFile, domFile, fpos, progressTracker);
        Dbg.print("Saved domain index file to " + domFile);
    }


    /*
     * Helper for building domain file.  Takes dict filename, domain
     * filename, and vector of file positions of entries in dictionary
     * file, then creates domain file.
     * FIX: ought to deal with uniquifiers where necessary.  Will likely
     * be distinguished by domain membership here, but if linked in
     * Kirrkirr, we'll want to know which entry to link to for setting
     * current word in other panels.
     */

    private static void processDomains(String dictFile, String domFile,
                                       Vector<Long> fpos,
                                       IndexMakerTracker progressTracker) throws Exception {
        //create a single dummy root since there may not be one in the dictionary.
        TreeNode root = new TreeNode("");
        String entryTag = dictinfo.getEntryTag();
        // String dictTag = dictinfo.getDictionaryTag();
        String hwXPath = dictinfo.getHeadwordXPath();
        String domainXPath = dictinfo.getDomainXPath();
        // String entryXPath = dictinfo.getDictionaryEntryXPath();
        String uniquekeyXPath = dictinfo.getUniquifierXPath();

        if (domainXPath == null) {
            Dbg.print("Dictionary does not have domain information!");

            if (progressTracker!=null) {
                progressTracker.passDone();
            }
            return;
        }

        //initialize anyone tracking our progress
        if (progressTracker!= null) {
            progressTracker.totalStepsForPass(fpos.size());
        }

        InputStream bis = new BufferedInputStream(new FileInputStream(dictFile));

        //For multiple word senses
        String senseXPath = dictinfo.getSenseXPath();
        String relDomXPath = domainXPath.substring(domainXPath.indexOf(entryTag)+entryTag.length()+1);

        long curFPos = 0;
        //loop through, processing each dictionary entry
        for (int i = 0, fpossize = fpos.size(); i < fpossize; ) {
            long lastFPos = curFPos;
            curFPos = fpos.elementAt(i).longValue();
            i++;

            try {
                bis.reset();
                bis.skip(curFPos-lastFPos);
            } catch(Exception ee) {
                bis.close();
                bis=new BufferedInputStream(new FileInputStream(dictFile));
                bis.skip(curFPos);
            }
            bis.mark(0);

            XmlMiniDocument minidoc = new XmlMiniDocument(hwXPath, entryTag);

            //parse the document.
            Document doc = minidoc.setup();
            doc = minidoc.parseElement(bis, doc);
            doc = minidoc.finish(doc);

            //update anyone tracking our progress
            if (i%100 == 0) {
                if (progressTracker!=null) {
                    progressTracker.stepsDone(i);
                }
                System.out.println("Second pass (domains) : " + i + " words done");
            }

            //get (and check) the word for this entry
            // check for restricted XPath, and if so exclude entry!!
            NodeList nl=getElementsFromDoc(dictinfo.getDictionaryEntryXPath(), doc);
            if (nl != null && nl.getLength() == 0) {
                nl = null;
            }
            String headword = IndexMaker.getElementFromDoc(hwXPath, doc);
            String uniquekey = createUniqueKey(hwXPath, uniquekeyXPath, doc);
            if (nl == null || ! validWord(headword, uniquekey, dictErrors)) continue;

            //get the sense info for this entry
            Vector senseVec = getWordSenses(doc, senseXPath);  //for storing the senses

            //process domain data for each word sense
            for (int sense = 0; sense < senseVec.size(); sense++) {
                // what to use as the actual xpath to domains (value depends on whether this is main sense or additional sense)
                String usableDomXPath;
                if (sense == 0) {
                    usableDomXPath = domainXPath;
                } else {
                    usableDomXPath = relDomXPath;
                }

                if (!processDomainsForSense((Element) senseVec.elementAt(sense),
                        usableDomXPath, doc,
                        uniquekey, root)) {
                    if (progressTracker != null) {
                        progressTracker.passDone();
                    }
                    return;
                }
            }
        }
        writeDomainFile(root, domFile);
        if (progressTracker != null) {
            progressTracker.passDone();
        }
    }


    /** Given the semantic domain info for a single sense of a word,
     *  and adds that info to the tree structure representing all domains.
     *  @return false iff there is an unrecoverable error in processing
     */

    private static boolean processDomainsForSense(Element parent, String domainXPath,
                                                  Document doc, String uniquekey,
                                                  TreeNode root) {

        NodeList nl = getNodeListFromRoot(parent, domainXPath);
        if (nl == null) {
            Dbg.print("processDomainsForSense: null node list!!!");
            return false;
        }
        int nlLeng = nl.getLength();
        // System.out.println("XXXX Got a node list of length " + nlLeng);
        if (nlLeng == 0) {
            // no domain info for sense
            return true;
        }

        // decide style of domains (single pcdata or nested list)
        String domainComponentXPath = dictinfo.getDomainComponentXPath();
        boolean containsElements = domainComponentXPath != null;

        // Process possibly multiple domains for word sense
        if (containsElements) {
            for (int k = 0; k < nlLeng; k++) {
                Node node = nl.item(k);
                Element enode = (Element) node;
                NodeList tnl = enode.getElementsByTagName(domainComponentXPath);
                addHeadwordToTree(doc, tnl, uniquekey, root, -1);
            }
        } else {
            // all are just one item
            for (int entry = 0; entry < nlLeng; entry++) {
                addHeadwordToTree(doc, nl, uniquekey, root, entry);
            }
        }
        return true;
    }


    /** Collects Nodes for each of the senses of a word and returns them
     * in a Vector.  Elements of vector are Nodes, and the first entry is
     * the primary sense (that directly under the ENTRY tag).  Any
     * additional senses (possibly none) are the SENSE nodes directly under the
     * ENTRY tag.
     */

    private static Vector<Node> getWordSenses(Document doc, String senseXPath) {
        //gather additional senses
        Vector<Node> senseVec = new Vector<Node>();
        senseVec.addElement(doc.getFirstChild()); //add primary sense node
        NodeList senses = getNodeListFromRoot((Element) doc.getFirstChild(), senseXPath);
        if (senses != null) {
            for (int sense = 0; sense < senses.getLength(); sense++)
                senseVec.addElement(senses.item(sense));
        }
        return senseVec;
    }


    /** Returns true iff both hw and unique key are non-null and pass
     *  dictErrors tests.
     */

    private static boolean validWord(String headword, String uniquekey,
                              Vector dictErrors) {
        return (!(uniquekey==null || headword == null || headword.isEmpty()
                  || dictErrors.contains(headword)));
    }


    /** Borrowed from DictionaryCache - really should be unified in one place.
     *  Can't be in DictionaryCache, since we are running outside the
     *  context of Kirrkirr (in stand-alone runs), and so
     *  DictionaryCache's static kirrkirr pointer will cause a crash.
     *  Perhaps this functionality should go in Helper?  It deals only
     *  with xml data, not with .clk files or any dictionary-specific info.
     */

    private static NodeList getNodeListFromRoot(Element root, String xpath) {
        if (xpath == null || root == null)
            return null;

        try {
            return XPathAPI.eval(root, xpath).nodelist();
        } catch (Exception ex) {
            if(Dbg.PARSE)
                Dbg.print("Xpath evaluation exception: " + ex.toString());
        }
        return null;
    }

    public static String extractElementInformation(Element item,
                                                    String xpath) {
        if (xpath == null || item == null)
            return null;
        try {
            String ans = XPathAPI.eval(item, xpath).str();
            if (Dbg.CACHE) {
                Dbg.print("extractElementInformation for " + xpath + " got " +
                          ans);
            }
            return ans;
        } catch (Exception ex) {
            if (Dbg.ERROR) {
                Dbg.print("Xpath evaluation exception: " + ex.toString());
            }
        }
        return null;
    }


    /** Helper method to add a single headword to the domain tree.  Requires
     *  the listing of domain information (in the NodeList), the headword, and
     *  the root of the domain tree.
     */
    private static void addHeadwordToTree(Document doc, NodeList dmiList,
                                          String uniquekey,
                                          TreeNode root, int index) {
        // System.out.print("Adding domain for headword " + uniquekey + ": ");
        for (int idx = 0; idx < dmiList.getLength(); idx++) {
            Node n = dmiList.item(idx);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;
            // String dom = Helper.getElementText(n);
            // System.out.print(dom + "; ");
        }
        // System.out.println("index " + index);

        TreeNode curNode = root;
        String paddedHeadword;
       	paddedHeadword = Helper.uniqueKeyToPrintableString(uniquekey);
        //determine start/stop indices
        int start = index, end = index+1;
        if (index == -1) {
            start = 0;
            end = dmiList.getLength();
        }

        //process domain path for headword
        Node conversionNode = null;
        for (int domEntry = start; domEntry < end; domEntry++) {
            Node n = dmiList.item(domEntry);
            if (n.getNodeType() != Node.ELEMENT_NODE) continue;

            String domLevel;
            if (usingDomainConverter) {
            	domLevel = dc.getConversion(Helper.getElementText(n), conversionNode);
            	conversionNode = dc.getConversionNode(Helper.getElementText(n), conversionNode);
            } else {
            	domLevel = Helper.getElementText(n);
            }
            //take this level of the word's domain hierarchy and
            //find its place in tree
            if (domLevel!=null) {
                TreeNode child = curNode.findChild(domLevel);
                if(child!=null) {
                    curNode = child;  //move down a level
                }
                else {
                    //add this child
                    curNode =
                        curNode.addChild(domLevel);
                    // add the rest to that subtree
                    if(domEntry+1 < end)
                        curNode = addBranch(curNode, dmiList,
                                            domEntry+1);
                    break;
                }
            }
        }
        if(curNode.findChild(paddedHeadword) == null)
            //don't want duplicates
            curNode.addChild(paddedHeadword);
    }



    /** Writes out the domain tree referred to by root to the domain file
     *  specified as the second parameter.
     */

    private static void writeDomainFile(TreeNode root, String domainFile) throws IOException {
        BufferedWriter outWriter = null;
        try {
            outWriter = new BufferedWriter(new FileWriter(domainFile));
            outWriter.write("<?xml version=\"1.0\" encoding=\"" +
                            RelFile.ENCODING + "\"?>");
            outWriter.newLine();
            outWriter.write("<DTREE>");
            outWriter.newLine();

            root.recWrite(outWriter, 0);  //ask tree to write self, given
                                          //writer and initial level of 0

            outWriter.write("</DTREE>");
            outWriter.newLine();
            outWriter.close();
        } finally {
            try {
                if (outWriter != null) {
                    outWriter.close();
                }
            } catch (IOException ioe) {
            }
        }
    }


    /** Helper method that adds the remainder of an entry's domain
     *  information to the sub-tree denoted by curNode.
     */

    private static TreeNode addBranch(TreeNode curNode, NodeList dmiList, int listPos) {
        for(int i = listPos; i < dmiList.getLength(); i++) {
            Node n = dmiList.item(i);
            if(n.getNodeType() != Node.ELEMENT_NODE) continue;
            String domLevel = Helper.getElementText(n);
            if(domLevel!=null) {
                curNode = curNode.addChild(domLevel);
            }
        }
        return curNode;
    }


    /** TreeNode is a helper class to define the building block of the
     *  domain structure.  One TreeNode exists for each level of the
     *  domain tree.
     */
    static class TreeNode {
        //Ivars: name of domain and vector of domain's sub-domain children.
        public String name;
        public Vector<TreeNode> children;  //vector of TreeNode children

        /** Constructor for a tree node.  Takes only the name of the
         *  domain to be constructed.
         */
        public TreeNode(String tag) {
            name = tag;
            children = new Vector<TreeNode>();
        }

        /** Searches for a domain by the given name at the current
         *  level of the domain tree.  If found, returns that node,
         *  otherwise, returns null.  Right now, children are stored
         *  unordered, so search must be linear - could be made faster.
         */
        public TreeNode findChild(String childName) {
            for (TreeNode child : children) {
                if (childName.equals(child.name)) {
                    return child;
                }
            }
            return null;
        }

        /** Adds a child node for the given domain name as a child of
         *  the current node.
         */
        public TreeNode addChild(String childName) {
            TreeNode newChild = new TreeNode(childName);
            children.addElement(newChild);
            return newChild;
        }


        /** Recursively writes out the subtree that has the current node as root.
         *  Takes a BufferedWriter to write with, as well as an integer
         *  level to append to the XML tag to distinguish between levels
         *  of the tree.  This level is incremented at each level of the
         *  recursion.
         */
        public boolean recWrite(BufferedWriter writer, int level) {
            try {
                //convert any nested quotes into spaces so as to avoid
                //attribute parse errors
                writer.write("<DOM"+ level + " NAME=\"");
                writer.write(URLHelper.encode(name));
                writer.write("\"");

                if (children.isEmpty()) { //no children - use shorthand
                    writer.write("/>");
                    writer.newLine();
                } else {  //children - write out self, ask children to
                        //write, then add ending tag for self
                    writer.write(">");
                    writer.newLine();

                    for (TreeNode child : children) {
                        if ( ! (child.recWrite(writer, level+1))) {
                            //failure!  quickly fail out and propagate
                            //notification of failure up to top level
                            return false;
                        }
                    }
                    writer.write("</DOM" + level + ">");
                    writer.newLine();
                }
            }
            catch (IOException ioe) {
                Dbg.print("Error while writing xml file");
                return false;
            }
            return true;
        }

    } // end static class TreeNode

    /*
    public static char ConvertEntityToChar(String html){
        try{
            int first=html.indexOf('#');
            int second=html.indexOf(';');
            String enc=new String(html.substring(first+1,second));
            return (char)Integer.parseInt(enc);
        }catch(Exception e){
            return '/0';
        }
        }*/

    /** This extracts the contents of one entry, delimited
     *  by the startreg/endreg, from the randomaccessfile
     *  and writes them out to the tempfile. This is so
     *  that XmlMiniDocument can parse one entry without
     *  messing up the RandomAccessFile file pointer (since
     *  we can't access one file in two separate ways).
     *  But it seems like there is probably a more efficient
     *  way to do this (pipestreams?).
     *  Returns a fpos.
     *  @param raf The RandomAccessFile to read from (ie, the xml
     *  dictionary
     *  @param startreg The regular expression to match when we are
     *  looking for the start of a dictionary entry
     *  @param endreg The regular expression to match when we are
     *  looking for the end of a dictionary entry
     *  @param tempfile The file to save the dictionary entry to.
     *  @return The file position for the just-saved entry.

    private static long GetNextEntry(RandomAccessFile raf, Regex startreg,
                                     Regex endreg, String tempfile) {
        try {
            long fpos=raf.getFilePointer(), returnfpos=-1;
            DataOutputStream fos=new DataOutputStream(new FileOutputStream(tempfile));
            String line=raf.readLine();
            //read until you reach the current startentry (should normally be immediate)
            while(line!=null && !startreg.hasMatch(line)){
                fpos=raf.getFilePointer();
                line=raf.readLine();
            }
            if (line==null) return -1;
            //write the current startentry
            fos.writeBytes(line+"\n");
            //set the fpos to return
            returnfpos=fpos;
            line=raf.readLine();
            if (line==null) return -1;
            //read/write until you get to the endentry
            while(line!=null && !endreg.hasMatch(line)){
                fos.writeBytes(line+"\n");
                fpos=raf.getFilePointer();
                line=raf.readLine();
            }
            if (line==null) return -1;
            //write the current endentry
            fos.writeBytes(line+"\n");
            //rewind to the last fpos - not this way
            //raf.seek(fpos);
            fos.close();
            return returnfpos;
        }catch(Exception e){
            e.printStackTrace();
        }
        return -1;
        }  */

  /*
    private static Document GetNextDocument(XmlMiniDocument minidoc, String tempfile){
        try{
            InputStream bis=new BufferedInputStream(new FileInputStream(tempfile));
            Document doc = minidoc.setup();
            doc = minidoc.parseElement(bis, doc);
            doc = minidoc.finish(doc);
            bis.close();
            return doc;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
        }*/
    /*    private static String filterWord(String gloss, Vector stopChars){
        StringBuffer sb=filterStopChars(gloss,stopChars);
        if (sb==null) return null;
        String toreturn=sb.toString().replace('\\',' ').replace('/',' ').trim();
        if (toreturn.length()==0) return null;
        return toreturn;
        }*/

}

