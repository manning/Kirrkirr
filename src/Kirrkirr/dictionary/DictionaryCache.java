package Kirrkirr.dictionary;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.*;
import Kirrkirr.ui.data.KKListModel;
import Kirrkirr.ui.panel.optionPanel.XslOptionPanel;
import Kirrkirr.ui.panel.HtmlPanel;

import java.io.*;
import java.util.*;
import java.net.*;

import javax.swing.*;

import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;
import org.w3c.dom.*;
import org.apache.xpath.*;

/** The {@code DictionaryCache} object controls all the dictionary data
 * for Kirrkirr. It acts as the interface to the XML database, returning
 * everything from DictEntries to Html files.
 *
 * @author      Kevin Jansz
 * (Madhu:'00; added saveCache,loadCache,addElement,generateHTML methods,
 * added empty constructor for cacheEntry)
 */
public final class DictionaryCache implements Serializable {

    private static final long serialVersionUID = -1366973594849355336L;

    private Kirrkirr parent;

    //string constants that need to be translated
    private static final String SC_LOADING_DICT="Kirrkirr_is_loading_the_dictionary";
    private static final String SC_NO_ENTRY="There_is_no_entry_in_the_dictionary_for";
    private static final String SC_DICT_ERROR="Couldn't_access_the_dictionary!";
    private static final String SC_HTML_LIST="Kirrkirr_-_Save_Word_List_as_HTML";
    private static final String SC_SAVE_AS="Save_As";
    private static final String SC_FILE_SAVED="file_saved_in_folder";
    private static final String SC_FILE_ERROR="Error_saving_file";
    private static final String SC_OPEN_LIST="Kirrkirr_-_Open_Saved_Krr_List";
    private static final String SC_LIST_FILES_DESC="Kirrkirr_list_files";
    // private static final String SC_NEED_INDEX="You_need_to_build_an_index_file_to_use_this_dictionary";
    private static final String SC_NO_ENG_ENTRY="Lighter_colored_words_are_headings;_choose_a_sub-entry";

    /** This string contains the necessary stuff to have at the beginning
     *  of a Headword dictionary file for it to be well-formed.  This is
     *  both the standard XML stuff and necessary character entity
     *  definitions.  If these are changed, wrl-xml-new.pl needs changing too.
     */
     public static final String XML_HEADER =
    "<?xml version=\"1.0\" encoding=\"" + RelFile.ENCODING + "\"?>\n";
    /*public static final String XML_HEADER =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
        "<?xml-stylesheet type=\"text/xsl\" href=\"headword.xsl\"?>\n" +
        "<!DOCTYPE DICTIONARY [\n" +
        "  <!ENTITY aacute \"&#225;\">\n" +
        "  <!ENTITY agrave \"&#224;\">\n" +
        "  <!ENTITY copy   \"&#169;\">\n" +
        "]>\n"; */

    /** What to return when no Gloss gloss is found. */
    private static final String NO_ENG = "";
    /** Maximum number of entries in the cache */
    private static final int CACHE_SIZE = 12;

    /** If an Gloss word corresponds to more than this many Headword words,
     *  then we automatically choose an abbreviated XSL formatting style.
     *  This is maybe okay, but the real reason we did it was that huge HTML
     *  files seemed to wreak havoc in JEditorPane under JDK 1.1.8
     */
    private static final int ENGLISH_TOO_BIG = 6;

    /** Extract modes for extracting "links" from xml -
     *  everything except domain is EXTRACT_OTHER. */
    private static final int EXTRACT_OTHER = 1;
    /** Extract modes for extracting domain "links" from xml */
    private static final int EXTRACT_DOMAIN = 2;
    /** Extract media which has a file and a description from xml */
    private static final int EXTRACT_MEDIA = 3;

    /** Vector of all headword headwords in dictionary.
     *  This never changes - it is the 'original/reset sort'
     *  list. The data for the changing word list is in
     *  WordList.java.
     */
    public Vector headwordList;  //accessed by ScrollL, QuizP, & SearchP
    /** index of all words in the dictionary */
    private Hashtable index;
    /** more complete info for words in the cache
        (padded headwords -> cacheEntries) */
    private Hashtable cache;
    /** vector of strings in cache */
    private Vector      hitList;

    /** the list of Gloss glosses from H_WORDS.  Little used.  Eliminate? */
    public Vector glossList;
    /** vector of strings in cache */
    private Vector    glossHitList;
    /** index of all gloss words in the dictionary */
    private Hashtable glossIndex;
    /** gloss cache (glosses -> cacheEntries) */
    private Hashtable glossCache;
    /** the thread at the begining which loads the gloss dictionary */
    public transient Thread     glossLoader;

    /** int that keeps increasing each time a new entry is added to the cache.
        used to make xml filenames */
    private int         hit_count; // = 0;

    /** the URI for the xsl file to convert the xml to html (set in XslOptionPanel)
     */
    public  String      xslfile;
    /** the temp html subfolder */
    private String      htmlFolder;
    /** the temp xml subfolder */
    private String      xmlFolder;
    /** the xsl subfolder */
    private String      xslFolder;

    private static String entryTag, headwordXPath;

    /** Stores the name of the XML file that holds the whole dictionary */
    private static String xmlFilename;

    /** longest allowed reset - should be longer than most entries, but need
     *  not be longer than all (code will reopen if reset fails). In bytes.
     *  On 1998 Headword dict, length of 16000 means about 27 entries are too
     *  long for reset.  Could try to determine if overall performance is
     *  slower or faster by varying this size (or even by not doing this
     *  resetting at all)
     */
    private static final int MARKLIMIT = 0;  // was 16000

    /** the current file position we are reading at in the
        BufferedInputStream */
    private static long markedFpos;
    /** the BufferedInputStream for the XML dictionary.  It is maintained
     *  globally for the DictionaryCache by setup_bis (should be synchronized
     *  when manipulating bis) and generally left open.
     */
    private static BufferedInputStream bis;


    /**
     * Constructs the DictionaryCache.
     *
     * At present takes the task from Kirrkirr of opening the clkFile and reading the
     * binary Hashtable objects - this is so the clk format can be changed (to XML?)
     * and the code only changes in this constructor method. Initializes the
     * gloss index loader (but does not call it).
     *
     * @param     clkFile      the Kirrkirr Headword index filename
     *                         generated by IndexMaker
     * @param     parent       the Kirrkirr object
     * @param     htmlFolder   the temporary (writeable) html subfolder
     * @param     xmlFolder    the temporary (writeable) xml subfolder
     * @param     xslFolder    the xsl subfolder
     */
    public DictionaryCache (String clkFile, Kirrkirr parent, String htmlFolder,
                            String xmlFolder, String xslFolder) throws Exception {
        this.parent = parent;
        this.htmlFolder = htmlFolder;
        this.xmlFolder = xmlFolder;
        this.xslFolder = xslFolder;
        cache = new Hashtable(CACHE_SIZE);
        hitList = new Vector();
        glossLoader = null;
        setXslFilename(XslOptionPanel.getDefaultStyleSheet(xslFolder));
        DictionaryInfo dictInfo = parent.dictInfo;
        entryTag=dictInfo.getEntryTag();
        //dictTag=dictInfo.getDictionaryTag();
        headwordXPath=dictInfo.getHeadwordXPath();
        try {
            long time = -1;
            if (Dbg.TIMING) time = System.currentTimeMillis();
            InputStream is;
            if (parent.APPLET) {
                URL index_url = RelFile.makeURL(RelFile.dictionaryDir, clkFile); //new URL(clkFile);
                URLConnection yc = index_url.openConnection();
                is = yc.getInputStream();
            } else {
                // try {
                    is = new FileInputStream(RelFile.makeFileName(RelFile.dictionaryDir, clkFile));
                //} catch (FileNotFoundException fnfe) {
                //    JOptionPane.showMessageDialog(null,
                //        Helper.getTranslation(SC_NEED_INDEX),
                //        Helper.getTranslation(SC_NEED_INDEX),
                //        JOptionPane.ERROR_MESSAGE);
                //    parent.showIndexMakerDialog(true);
                //
                //    is = new FileInputStream(RelFile.MakeFileName(RelFile.dictionaryDir,clkFile));
                //}
            }
            // note: putting in buffering made this way faster under JDK1.3.
            // I tested it.
            ObjectInputStream ois;
            ois = new ObjectInputStream(new BufferedInputStream(new ProgressMonitorInputStream(parent.window,
                                Helper.getTranslation(SC_LOADING_DICT), is)));
            index = (Hashtable) ois.readObject();
            ois.close();
            if (Dbg.TIMING) {
                time=System.currentTimeMillis()-time;
                Dbg.print("loading dictionary took "+time +" ms");
            }

            EIndexLoader loader = new EIndexLoader();
            glossLoader = new Thread(loader);
            glossLoader.setPriority(Thread.MIN_PRIORITY);
        } catch (Exception e) {
            if (index == null) {
                throw e;
            }
            if (Dbg.ERROR) {
                e.printStackTrace();
            }
        }

        headwordList = (Vector) index.remove(DictField.H_WORDS);

        if (Kirrkirr.APPLET) {
            xmlFilename = RelFile.MakeURLString(RelFile.dictionaryDir,
                                                Kirrkirr.xmlFile);
        } else {
            xmlFilename = RelFile.makeFileName(RelFile.dictionaryDir,
                                               Kirrkirr.xmlFile);
        }
    }


    /** Constructs the DictionaryCache
     *  This constructor is useful for utils or classes that want to load the dictionary
     *  and use many of the DictionaryCache functions without loading kirrkirr.
     *  This means that it doesn't have a pointer to Kirrkirr, so it can't access
     *  all functions. (Not bugproof, since this is only for developers).
     *
     *  @param     indexFile    the kirrkirr (clk) index filename generated by IndexMaker
     *  @param     htmlFolder   the temporary (writeable) html subfolder
     *  @param     xmlFolder    the temporary (writeable) xml subfolder
     *  @param     xslFolder    the xsl subfolder
     *  @param     loadGloss   whether or not to load the gloss dictionary
     */
    public DictionaryCache (String indexFile, String htmlFolder,
                            String xmlFolder, String xslFolder,
                            boolean loadGloss) throws Exception {
        this.parent = null;
        this.htmlFolder = htmlFolder;
        this.xmlFolder = xmlFolder;
        this.xslFolder = xslFolder;
        cache = new Hashtable(CACHE_SIZE);
        hitList = new Vector();
        glossLoader=null;
        setXslFilename(XslOptionPanel.getDefaultStyleSheet(xslFolder));
        try {
            long time=-1;
            if (Dbg.TIMING) time=System.currentTimeMillis();
            InputStream is = new FileInputStream(indexFile);

            // note: putting in buffering made this way faster under JDK1.3. Tested it.
            ObjectInputStream ois =
                new ObjectInputStream(new BufferedInputStream(is));
            index = (Hashtable) ois.readObject();
            ois.close();
            if (Dbg.TIMING) {
                time=System.currentTimeMillis()-time;
                Dbg.print("loading dictionary took "+time +" ms");
            }

            if (loadGloss) {
                EIndexLoader loader=new EIndexLoader();//glossIndex,ois);
                glossLoader = new Thread(loader);
                glossLoader.setPriority(Thread.MIN_PRIORITY);
            }
        } catch(Exception e) {
            if (index==null) {
                throw e;
            }
            if (Dbg.ERROR) {
                e.printStackTrace();
            }
        }

        index.remove(DictField.H_WORDS);
        if (loadGloss) glossLoader.start();
    }

    public  Document getXmlEntry(String headword) {
        DictEntry de=(DictEntry)index.get(headword);
        if (de==null) return null;
        return getXmlEntry(de.fpos);
    }

    /** Returns a Document representing this DictEntry. This does the
     *  whole operation from fpos through parsing.
     *  This one used when one
     *  doesn't want to write out the xml mini-file to disk.
     *  @see #parseXmlEntry(long, XmlMiniDocument, Document, Writer)
     *  @param fpos the file position at which to look in
     *        the dictionary for the xml fragment
     */
    public static Document getXmlEntry(long fpos) {
        try {
            BufferedInputStream bis=setup_bis(fpos);
            XmlMiniDocument myxmlMD = new XmlMiniDocument(headwordXPath,entryTag);
            Document doc = myxmlMD.setup();
            doc = myxmlMD.parseElement(bis, doc);
            doc = myxmlMD.finish(doc);
            finishup_bis(bis);
            return doc;
        } catch (Exception e) {
            Kirrkirr.kk.setStatusBar(Helper.getTranslation(SC_DICT_ERROR));
            Helper.handleException(e);
            return(null);
        }
    }


   /** Wraps XmlMiniDocument.parseEntry() so as to handle getting from a
    *  file position to a correctly positioned input stream
    *  Returns a Document representing this DictEntry. Uses the
     * XmlMiniDocument passed in, and the global (Kirrkirr) xmlFile.
     * This one is used when one has an fpos
     * and one wants to write out the xml mini-file to disk.
     * @see #getXmlEntry(long)
     * @param fpos the file position at which to look in
     *        the dictionary for the xml fragment
     * @param xmlMD An XmlMiniDocument that hasn't been start()-ed
     *       (this is important, otherwise it will mess up the DICTIONARY tags)
     * @param xmldoc The XML document being read
     * @param outp the Writer to which the xml output should be written.
     *        Must be initialized before and closed after.  For good
     *        performance, it should be buffered beforehand.
     */
    private static Document parseXmlEntry(long fpos, XmlMiniDocument xmlMD,
                                          Document xmldoc, Writer outp) {
        try {
            BufferedInputStream bis=setup_bis(fpos);
            Document doc = xmlMD.parseElement(bis, xmldoc, outp);
            finishup_bis(bis);
            return doc;
        } catch (Exception e) {
            // can't do this as static so can call from IndexMaker
            // parent.setStatusBar(Helper.getTranslation(SC_DICT_ERROR));
            Helper.handleException(e);
            return xmldoc; // return what was there before.  Wise?
        }
    }


    /** Generates a gloss definition html file for all the entries in the
     *  list (in the
     *  list as Strings). This function is used when the xml file has already
     *  been created.
     *
     *  @param xmlFile the fully qualified filename of the already existing
     *          xml file
     *  @param htmlFile the fully qualified filename under which to put the
     *          html
     *  @param length Some limit on length?  What does this do??
     *  @return whether successfully created file
     */
    private boolean generateGlossHtml(String xmlFile, String htmlFile,
                                       int length) {
        String previousXslFilename = xslfile;
        if (length > ENGLISH_TOO_BIG) {
            // cw 2002: use XslOP constant
//          setXslFilename(RelFile.MakeURLString(xslFolder,
//                                               XslOptionPanel.fileNames[0]));
            setXslFilename(RelFile.MakeURLString(xslFolder, XslOptionPanel.fileNames[XslOptionPanel.defaultOption]));
        }

        if ( ! XslDriver.makeHtml(xmlFile, xslfile, htmlFile)) {
            if (Dbg.ERROR) {
                Dbg.print("DictionaryCache:generateGlossHtml(String, String): html file generation using " + xslfile + " didn't work");
            }
            JOptionPane.showMessageDialog(Kirrkirr.kk.window,
                        "Error making html file for \n" +
                         Helper.uniqueKeyToPrintableString(
                                   Helper.cacheFilenameToUniqueKey(htmlFile)),
                         "Kirrkirr file error",
                         JOptionPane.ERROR_MESSAGE);
            setXslFilename(previousXslFilename);
            return false;
        } else {
            if (Dbg.VERBOSE)
                Dbg.print("created "+(new File(htmlFile)).getName());
            setXslFilename(previousXslFilename);
            return true;
        }
    }


    /** Generates the gloss xml file and an gloss definition html file
     *  for all the entries in the list (in the
     *  list as Strings). The gloss as well as the filename that the gloss xml
     *  is saved under must be passed in.
     *  @param gloss the gloss gloss that one is looking up
     *  @param list list containing words as Strings to create HTML entries
     *         for
     *  @param xmlFile The (unqualified) filename under which to put the xml
     *  @param htmlFile The (unqualified) filename under which to put the html
     *  @return whether successfully created file
     */
    private boolean generateGlossHtml(String gloss, String[] list, String xmlFile, String htmlFile)
    {
        if (Dbg.VERBOSE) Dbg.print("generateGlossHtml "+gloss);
        try{
            if (list==null || list.length==0) return false;
            //make a temporary "gloss.xml" file with the gloss's entry in it
            String tempfile=RelFile.MakeWriteFileName(xmlFolder,"@gloss@.xml");
            String text = XML_HEADER + "<GLOSS>\n\t\t"+gloss+"\n\t</GLOSS>";
            FileWriter fwriter = new FileWriter(tempfile);
            fwriter.write(text);
            fwriter.close();
            //add the other xml documents to the gloss.xml file, writing out the xml file as we go along
            Document xmldoc=generateGlossDocument(list, tempfile, xmlFile);
            if (xmldoc == null) {
                if (Dbg.ERROR) Dbg.print("generateGlossHtml "+gloss+" couldnt generate gloss document");
                JOptionPane.showMessageDialog(Kirrkirr.kk.window,
                                              "Error making html doc for "+gloss,
                                              "Kirrkirr internal error",
                                              JOptionPane.ERROR_MESSAGE);
                return false;
            }
            //make html file in user's directory
            return generateGlossHtml(xmlFile, htmlFile, list.length);
        } catch (Exception e) {
            if (Dbg.ERROR) {
                JOptionPane.showMessageDialog(Kirrkirr.kk.window,
                                              "Error making html file for "+gloss,
                                              "Kirrkirr file error",
                                              JOptionPane.ERROR_MESSAGE);
                Dbg.print("dictionarycache: generateGlossHtml(list)");
                Dbg.print(e.getMessage());
                e.printStackTrace();
            }

            return false;
        }
    }


    public String generateHTML(KKListModel list) {
        if (Dbg.K) Dbg.print("generateHTML: List is " + list);
        // you can't use toArray() 'cos one wants an array of String!
        int len = list.size();
        String[] headwords = new String[len];
        for (int i = 0; i < len; i++) {
            headwords[i] = (String) list.getElementAt(i);
        }
        return generateHTML(headwords);
    }


    /**
     * Generates one html file for all the entries in the list (in the
     * list as Strings). First prompts user for filename (to which the
     * file extensions are added). Afterwards, pops up a box telling the
     * user whether the file was successfully created or not. Used
     * by Kirrkirr, when user clicks "Create HTML" on advanced search panel.
     * @param list list containing words as Strings to create HTML entries
     *        for
     * @return the (fully qualified) filename it was saved under
     */
    private String generateHTML(String[] list) {
        if (Dbg.VERBOSE) Dbg.print("generateHTML");
        //show file chooser
        JFileChooser fileChooser = new JFileChooser(new File(RelFile.WRITE_DIRECTORY));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(Helper.getTranslation(SC_HTML_LIST));
        int userChoice = fileChooser.showDialog(Kirrkirr.window, Helper.getTranslation(SC_SAVE_AS));
        if (userChoice != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        String userHtmlFile = fileChooser.getSelectedFile().getName();
        String userDir = fileChooser.getSelectedFile().getParent();

        //make sure user's file ends in .htm or .html
        if ( ! userHtmlFile.contains(".htm"))
            userHtmlFile = userHtmlFile.concat(".html");
        int filenameIndex = userHtmlFile.indexOf(".htm");
        String tempXmlFile = RelFile.MakeWriteFileName(xmlFolder,
                Helper.wordToXmlFilename(userHtmlFile.substring(0,filenameIndex)));

        try {
            //create and write merged xml file
            generateDocument(list, tempXmlFile);
            //use temp xml file to make html file in user's directory, and
            //print out status messages
            if (XslDriver.makeHtml(tempXmlFile, this.xslfile, userHtmlFile))
                {
                    parent.setStatusBar(userHtmlFile+ ' ' +Helper.getTranslation(SC_FILE_SAVED)+ ' '
                                                 +userDir);
                    if (Dbg.VERBOSE) Dbg.print("created "+
                                               ((new File(tempXmlFile)).getName()+
                                                " and "+(new File(userHtmlFile)).getName()));
                } else {
                    parent.setStatusBar(Helper.getTranslation(SC_FILE_ERROR)+ ' ' +userHtmlFile);
                    JOptionPane.showMessageDialog(Kirrkirr.kk.window,
                                              "Error making html file for list.",
                                              "Kirrkirr file error",
                                              JOptionPane.ERROR_MESSAGE);
                }

        } catch (Exception e) {
            parent.setStatusBar(Helper.getTranslation(SC_FILE_ERROR)+ ' ' +userHtmlFile);
            if (Dbg.ERROR) {
                Dbg.print("dictionarycache: generatehtml(list)");
                Dbg.print(e.getMessage());
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(Kirrkirr.kk.window,
                                          "Error making html file for list.",
                                          "Kirrkirr file error",
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return userHtmlFile;
    }


    /** Given a list and a filename, writes the contents of the list
     *  as a text file to a file named "name.krr". (Unless the name has
     *  an .htm extension on the end, in which case it is chopped off before
     *  saving).  The uniqueKey is now written as is.  It is assumed to be
     *  printable.
     *  Lines starting with # are skipped (can add comments).
     *  @param list the list to save
     *  @param name the (unqualified) filename under which to save it
     */
    public void saveAsKrr(KKListModel list, String name) {
        if (name.indexOf(".htm") != -1) {
            name=name.substring(0,name.indexOf(".htm"));
        }
        if (name.indexOf(".krr") == -1) {
            name = name + ".krr";
        }
        String krrFile = RelFile.MakeWriteFileName((new File(name)).getParent(),
                                                   name);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(krrFile)));
            String intro="# This text file stores Headword headwords, with numbered homophones\n"+
                "# represented as #'s after the word. Kirrkirr can read the words in this\n"+
                "# file and construct a word list. You can edit this file too.\n"+
                "# Any line that starts with a # will be skipped by Kirrkirr when reading the file.\n";
            bw.write(intro);
            for (Enumeration e=list.elements(); e.hasMoreElements() ; ) {
                String pword=(String)e.nextElement();
                bw.write(pword);
                bw.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (IOException ioe) {}
        }
    }

    /** Ask a user to select a .krr file. If they choose one,
     *  try opening it and reading the headwords into a vector,
     *  and return it.
     *  If they don't choose one, or there are problems opening
     *  or reading it, return null.
     *
     *  @return the Vector of headwords, or null if they don't choose
     *          a file or if there was a problem opening/reading the file.
     */
    public Vector getVectorFromKrr() {
        try {
            //show file chooser
            JFileChooser fileChooser = new JFileChooser(new File(RelFile.WRITE_DIRECTORY));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setDialogTitle(Helper.getTranslation(SC_OPEN_LIST));
            // cdm: should change this to use KirrkirrFileFilter!
            fileChooser.setFileFilter( new  /*class F extends*/ javax.swing.filechooser.FileFilter() {
                public boolean accept(File f) {
                    String s = f.getName();
                    //        Dbg.print("looking at "+s);
                    int i = s.lastIndexOf('.');
                    if (i != -1) {
                        String ext = s.substring(i+1,s.length()).toLowerCase();
                        //                     Dbg.print("ext = "+ext);
                        return (ext.equals("krr"));
                    }
                    return false;
                }
                public String getDescription() {
                    return Helper.getTranslation(SC_LIST_FILES_DESC);
                }
            });
            int userChoice = fileChooser.showOpenDialog(Kirrkirr.window);
            if (userChoice != JFileChooser.APPROVE_OPTION) return null;
            String userKrrFile= fileChooser.getSelectedFile().getName();
            // String userDir=fileChooser.getSelectedFile().getParent();
            return getVectorFromFile(userKrrFile);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     *  Does the job of parsing the file at (fully qualified) userKrrFile
     *  and reading in the names, homonym numbers, skipping comment
     *  lines, etc.
     *  @param userKrrFile the fully qualified filename to read from
     *  @return a Vector with all of the padded headwords read from the file
     */
    private Vector getVectorFromFile(String userKrrFile) {
        try {
            InputStream is;
            if (parent.APPLET) {
                URL index_url = new URL(userKrrFile);
                URLConnection yc = index_url.openConnection();
                is = yc.getInputStream();
            } else {
                is = new FileInputStream(userKrrFile);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line=null;
            Vector selections=new Vector();
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equals("") || line.startsWith("#")) continue;
                selections.addElement(line);
            }
            br.close();
            return selections;
        } catch(Exception e) {
            if (Dbg.ERROR)
                e.printStackTrace();
            return null;
        }
    }


    /** Gives user file chooser to choose a .krr file of a
     *  kirrkirr list. If the user chooses a file and it is
     *  opened successfully, the side scrolllist is limited to the
     *  items in that list.
     */
    public void openKrr()
    {
        Vector selections=getVectorFromKrr();
        if (selections!=null)
            parent.scrollPanel.resetWordsGUI(selections);
    }


    /** Adds all the xml documents of the words in the list
     *  to one big xml document, and returns it. Also writes
     *  out the one xml file with all the entries. Also ends
     *  the mini documents parse (with the final DICTIONARY tag).
     *  CDM: With a little cleverness, could this be collapsed with
     *  generateDocument(String[], String) ??
     *  @return Document with the entries from all the words
     *          in the list, or null if the list is null, empty or
     *          there is a problem getting the xml entries.
     *  @param list the list of (padded) headwords associated with
     *         this gloss
     *  @param filename the (fully qualified) filename of the gloss.xml file
     *  @param outname the (fully qualified) filename to
     *         output the merged xml file to
     */
    private Document generateGlossDocument(String[] list, String filename,
                                                             String outname) {
        if (Dbg.VERBOSE) Dbg.print("generateGlossDocument "+filename+ ' ' +outname);
        if (list==null || list.length==0) {
            if (Dbg.ERROR) Dbg.print("DictionaryCache:generateGlossDocument: Empty or null list.");
            return null;
        }
        if (filename==null) {
            if (Dbg.ERROR) Dbg.print("DictionaryCache:generateGlossDocument: null filename.");
            return null;
        }
        try {
            // cw 2002: so accented chars. get written properly.  see
            // DictionaryCache.refreshCache().
//          FileWriter fw = new FileWriter(outname);
//          BufferedWriter bw = new BufferedWriter(fw);
            FileOutputStream fos = new FileOutputStream(outname);
            OutputStreamWriter osw = new OutputStreamWriter(fos,
                                                            RelFile.ENCODING);
            BufferedWriter bw = new BufferedWriter(osw);

            //goes through all elements in list, adding them to the xmldoc
            XmlMiniDocument xmlmd = new XmlMiniDocument(headwordXPath,entryTag);
            Document xmldoc = xmlmd.setup(bw);
            for (int i = 0; i < list.length; i++) {
                String word = list[i];
                if (Dbg.CACHE) Dbg.print("adding "+word);
                if (word != null) {
                    DictEntry de = getIndexEntry(word);
                    if (de != null) {
                        xmldoc = parseXmlEntry(de.fpos, xmlmd, xmldoc, bw);
                    } else {
                        if (Dbg.ERROR) {
                            Dbg.print("Null DictEntry for |" + word + '|');
                        }
                    }
                }
            }
            BufferedInputStream bfis = new BufferedInputStream(new
                                               FileInputStream(filename));
            xmldoc = xmlmd.parseElement(bfis, xmldoc, bw);
            bfis.close();
            xmldoc = addDirectories(xmlmd, xmldoc, false, bw);
            xmldoc = xmlmd.finish(xmldoc, bw);
            bw.close();
            return xmldoc;
        } catch (Exception e) {
            if (Dbg.ERROR) e.printStackTrace();
            return null;
        }
    }


    /** Makes an XML document containing the list of uniqueKeys in list.
     *  This document is written to the file <code>outname</code>. It is
     *  also returned.
     *
     * @param list List of uniqueKeys to make XML from
     * @param outname File to which to write XML document
     * @return An Xml <code>Document</code> object containing the words
     */
    private Document generateDocument(String[] list, String outname) {
        if (Dbg.VERBOSE) Dbg.print("generateDocument "+outname);
        if (list==null || list.length==0) {
            if (Dbg.ERROR) Dbg.print("DictionaryCache:generateDocument(list): Empty or null list.");
            return null;
        }
        try {
            Writer writer = new BufferedWriter(new FileWriter(outname));
            //goes through all elements in list, adding them to the xmldoc
            XmlMiniDocument xmlmd = new XmlMiniDocument(headwordXPath,entryTag);
            Document xmldoc = xmlmd.setup(writer);
            for (int i=0; i < list.length; i++) {
                String word = list[i];
                if (word != null) {
                    DictEntry de=getIndexEntry(word);
                    xmldoc = parseXmlEntry(de.fpos, xmlmd, xmldoc, writer);
                }
            }
            xmldoc = addDirectories(xmlmd, xmldoc, true, writer);
            xmlmd.finish(xmldoc, writer);
            writer.close();
            return xmldoc;
        } catch (Exception e) {
            if (Dbg.ERROR) e.printStackTrace();
            return null;
        }
    }


    /*  NOT USED (and wouldn't work? String[] vs. Object[])
     *  Adds all the xml documents of the words in the list
     *  to one big xml document, and returns it. Also writes
     *  out the one xml file with all the entries. Also ends
     *  the mini document's parse (with the final DICTIONARY tag).
     *  @return Document with the entries from all the words
     *          in the list, or null if the list is null, empty or
     *          there is a problem getting the xml entries.
     *  @param outname the (fully qualified) filename to
     *          output the merged xml file to
    private Document generateDocument(KKListModel list, String outname)
    {
        String[] headwords=(String[])list.toArray();
        return generateDocument(headwords,outname);
    }
    */


    private String cachedHeaderDirectoryInfo = null;
    private String cachedDirFile = null;
    private boolean cachedCalledBefore = false;
    private boolean cachedMakeUserFile = false;

    /**
     *  Adds the image and sound directories' Xml entries to the xml document.
     *  Also adds an attribute USERFILE if the file being created is for
     *  use by a user - then relative links to other cache files are not
     *  written in the file.
     *  @param xmldoc the xmldoc so far
     *  @param xmlmd the xmlmd that has been used to parse so far
     *  @param makeUserFile whether or not the file being created is for
     *         use by a user - then relative links to other cache files are not
     *         written in the file.
     *  @param writer the writer to write the xml file to.
     *  @retirm Thhe augmented Document
     */
    private Document addDirectories(XmlMiniDocument xmlmd, Document xmldoc,
                                    boolean makeUserFile, Writer writer) {
        try {
            if (cachedCalledBefore && (cachedMakeUserFile == makeUserFile)) {
                // everything is the same, so nothing to do!
            } else {
                if ( ! cachedCalledBefore) {
                    // must do initial setup
                    // note: this assumes xsl is a folder in the same
                    // directory as the image and sound directories.
                    // Seems an okay assumption??
                    String imageDir=RelFile.MakeURLString(RelFile.dictionaryDir,Kirrkirr.imagesFolder,"");
                    String soundDir=RelFile.MakeURLString(RelFile.dictionaryDir,Kirrkirr.soundFolder,"");
                    cachedHeaderDirectoryInfo = /* XML_HEADER + */
                            RelFile.lineSeparator() + "<DIR><IDIR>" + imageDir + "</IDIR>";
                    cachedHeaderDirectoryInfo = cachedHeaderDirectoryInfo +
                            "<SDIR>" + soundDir + "</SDIR>";
                    cachedCalledBefore = true;
                }
                // make a temporary "@dir@.xml" file with the directory info
                // in it
                cachedDirFile = RelFile.MakeWriteFileName(xmlFolder,
                                                          "@dir@.xml");
                StringBuffer text = new StringBuffer(120);
                text.append(cachedHeaderDirectoryInfo);
                if (makeUserFile)
                    text.append("<USERFILE> </USERFILE>");
                text.append("</DIR>");
                text.append(RelFile.lineSeparator());
                cachedMakeUserFile = makeUserFile;
                FileWriter fwriter=new FileWriter(cachedDirFile);
                fwriter.write(text.toString());
                fwriter.close();

                if (Dbg.CACHE) {
                    Dbg.print("made dirfile: " + cachedDirFile);
                }
            }

            // we add the other xml documents to the dir.xml file
            InputStream bis = new BufferedInputStream(new
                                        FileInputStream(cachedDirFile));
            xmldoc = xmlmd.parseElement(bis, xmldoc, writer);
            bis.close();
            return xmldoc;
        } catch (Exception e) {
            if (Dbg.ERROR){
                e.printStackTrace();
                Dbg.print("Dictionary:addDirectories returning null");
            }
            return null;
        }
        //return xmldoc;
    }


    /** Returns the name of the stylesheet file being used to transform the
     *  dictionary.
     *  This value is set by the getDefaultStyleSheet function in
     *  XslOptionPanel, or by the style sheet specified in the profile, if
     *  there is one. The choice of stylesheet is based on the type of
     *  output the user sees (headword, basic details, full entry...)
     *  @return the name of the xsl file used by the dictionary.
     *  @see XslOptionPanel
     */
    public String getXslFilename() {
        return xslfile;
    }


    /** Just sets the current xsl filename - doesn't regenerate cache entries.
     *  @param xslfile the new xsl filename
     *  @see #xslChanged(String)
     */
    private void setXslFilename(String xslfile) {
        this.xslfile = xslfile;
    }


    /** Called by XslOptionPanel to indicate a change in the xslFile option.
     *  This regenerates html entries for any words in the cache for which
     *  an xml/html entry exists, and calls formatChanged() in Kirrkirr
     *  to update.
     *  [Note for future: we could now just delete invalidated entries]
     *  @param xslURLString The file specification for the new XSL stylesheet
     */
    public void xslChanged(String xslURLString) {
        if (Dbg.VERBOSE) Dbg.print("xslChanged("+xslURLString);
        setXslFilename(xslURLString);

        Enumeration keys = cache.keys();

        while (keys.hasMoreElements()) {
            String s_word = (String) keys.nextElement();
            CacheEntry c_entry = (CacheEntry) cache.get(s_word);
            if (c_entry == null || c_entry.xmlFile == null ||
                c_entry.htmlFile == null)
                continue;

            String htmlFile = RelFile.MakeWriteFileName(htmlFolder,
                                         Helper.wordToCacheFilename(s_word));
            if (XslDriver.makeHtml(c_entry.xmlFile, this.xslfile, htmlFile)) {
                if (Dbg.VERBOSE) Dbg.print("updated "+htmlFile);
            } else {
                if (Dbg.ERROR) Dbg.print("xslChanged("+xslURLString+
                                         "): problem making html "+htmlFile);
                c_entry.htmlFile = null;
            }
        }

        if (glossCache==null) return;

        keys = glossCache.keys();

        while(keys.hasMoreElements()) {
            String word = (String) keys.nextElement();
            GlossCacheEntry hit = (GlossCacheEntry) glossCache.get(word);
            if (hit == null || hit.xmlFile == null)
                continue;
            int size;
            if (hit.headwords != null)
                size=hit.headwords.length;
            else
                size=0;
            if (!generateGlossHtml(hit.xmlFile, hit.htmlFile, size)) {
                if (Dbg.ERROR)
                    Dbg.print("xslChanged: couldnt generate gloss html "+hit.xmlFile);
            }

        }
        parent.formatChanged();
    }


    /** Gets the plain DictEntry (no link fields filled in) from the index
     *  This now holds frequency info!
     *  @param pword The unique key to look up
     *  @return the DictEntry for the given word, or null if it cannot be
     *          found in the dictionary
     */
    public DictEntry getIndexEntry(String pword) {
        //      Dbg.print("Looking up |" + pword + "|");
        DictEntry de = (DictEntry) index.get(pword);
        if (de == null) {
            // stop multiple warnings coming up from the one look-up action by the user.
            // if (! word.equals(previous_warning)) {
            //    JOptionPane.showMessageDialog(parent.window
            //        ,"There is no entry in dictionary: "+ pword
            //        ,"Lookup error"
            //        ,JOptionPane.INFORMATION_MESSAGE);
            // }
            // previous_warning = word;
            parent.setStatusBar(Helper.getTranslation(SC_NO_ENTRY) + ' ' +
                                Helper.uniqueKeyToPrintableString(pword));
            if (Dbg.K) {
                Dbg.print("getIndexEntry: no headword entry for " +
                          Helper.uniqueKeyToPrintableString(pword) +
                          " - this may just mean it is an gloss word.");
                Exception e=new Exception();
                e.printStackTrace();
            }
            return null;
        }
        //      if (Dbg.STREE) Dbg.print(pword+"="+de);
        return(de);
    }


    /** Returns GlossDictEntry for the gloss string
     *  passed in, or null if it cant be found in the index.
     *
     *  @param  glossKey The gloss key
     *  @return The GlossDictEntry for the gloss string
     *          passed in
     */
    public GlossDictEntry getGlossIndexEntry(String glossKey)
    {
        if (glossKey==null || glossIndex==null) return null;
        return (GlossDictEntry)glossIndex.get(glossKey);
    }

    /** Adds an entry to the cache, updating the status of the 'hasNote'
     *  field.  Used by ProfileManager and NotesPanel
     *
     *  @param pword An uniqueKey that is to be added and updated
     *  @param hasNote Whether this word has a note stored for it
     *  @return true if the addition was successful, false otherwise
     */
    public boolean addEntryUpdateNote(String pword, boolean hasNote) {
        DictEntry de = getIndexEntry(pword);
        if (de == null) {
            //this is dumb... if can't find the headword
            //entry, assume its gloss. (better way?)
            GlossDictEntry ede=getGlossIndexEntry(pword);
            if (ede != null) {
                ede.hasNote = hasNote;
            } else {
                return false;
            }
        } else {
            de.hasNote = hasNote;
        }
        return true;
    }


    /** Ensures that HTML has been made for a word, by putting it into
     *  the word cache.
     *  This creates both a small XML file, and an HTML file for
     *  the word.
     *  @param uniqueKey The padded word to lookup
     *  @return true if the word is found and hence there is HTML available
     *  @see #refreshCache(String, boolean)
     */
    public boolean ensureHtmlAvailable(String uniqueKey)
    {
        return refreshCache(uniqueKey, true) != null;
    }


    /** Looks for word in dictionary cache, and refreshes it (puts it at end
     *  of queue) if it is there; otherwise it calls getIndexEntry to get a
     *  DictEntry for the word, and adds it to the cache.
     *  If needHtml is true, it calls getXmlEntry to get the real content
     *  from the disk
     *  file.  This creates both a small XML file, and an HTML file for
     *  the word. (Synchronized)
     *  @param pword The uniqueKey for a dictionary entryto lookup.
     *  @param needHtml XML and HTML for the entry will be generated if
     *         this is true
     *  @return the CacheEntry of the given word, or null
     *         if the word is not found
     */
    private synchronized CacheEntry refreshCache(String pword,
                                                boolean needHtml) {
        long time =-1;
        if (Dbg.TIMING) time = System.currentTimeMillis();
        if (Dbg.CACHE) {
            if (pword == null) {
                Dbg.print("Attempting to refresh cache for null word.");
            } else {
                Dbg.print("refreshCache for |" + pword + "|, html="+needHtml);
            }
        }
        try {
            CacheEntry hit = (CacheEntry) cache.get(pword);
            if (Dbg.VERBOSE) if (hit!=null) Dbg.print("\ti found it in the cache!");
            DictEntry de = null;
            Document doc = null;

            boolean needDoc = (hit == null) || (needHtml && hit.xmlFile==null);
            if (hit == null)
                hit_count++;

            if (needDoc) {
                de = getIndexEntry(pword);
                if (de == null) {
                    if (Dbg.ERROR) Dbg.print("DictionaryCache:refreshCache(pword, needHtml): not found: |"+pword+ '|');
                    return null;
                }
                // Create mini Document for the entry
                // cw 2002: set osw encoding so that special / accented
                // characters get written out properly!
                // (default seems to be "ASCII" - neither this nor "UTF8"
                // do what we want) - todo: potentially should change this
                // in other places (e.g., other parts of dictCache,
                // IndexMaker?)
//              BufferedWriter out = new BufferedWriter(new
//                  FileWriter(RelFile.MakeWriteFileName(xmlFolder,
//                                    Integer.toString(hit_count) + ".xml")));
                FileOutputStream fos = new FileOutputStream(RelFile.MakeWriteFileName(xmlFolder, Integer.toString(hit_count) + ".xml"));
                OutputStreamWriter osw = new OutputStreamWriter(fos, RelFile.ENCODING);
                BufferedWriter out = new BufferedWriter(osw);

                XmlMiniDocument xmlmd = new XmlMiniDocument(headwordXPath, entryTag);
                doc = xmlmd.setup(out);

                doc = parseXmlEntry(de.fpos, xmlmd, doc, out);
                doc = addDirectories(xmlmd, doc, false, out);
                doc = xmlmd.finish(doc, out);
                out.close();
            }

            if (hit == null) {
                if (Dbg.K) Dbg.print(pword+" not found in cache");

                if (doc != null) {
                    // make full DictEntry from doc (just use Kirrkirr)
                    hit = new CacheEntry(de,
                                         getLinksFromDoc(doc),
                                         getGlossFromDoc(doc),
                                         getDomainsFromDoc(doc),
                                         getSoundsFromDoc(doc),
                                         getPicturesFromDoc(doc), null, null);
                } else {
                    if (Dbg.ERROR)
                        Dbg.print("DictionaryCache doc was null! what to do?");
                    return null;
                }
            }
            if (needDoc) {
                hit.xmlFile = RelFile.MakeWriteFileName(xmlFolder,
                                        Integer.toString(hit_count) + ".xml");
            }

            if (needHtml && hit != null && hit.htmlFile == null) {
                hit.htmlFile = RelFile.MakeWriteFileName(htmlFolder,
                                        Helper.wordToCacheFilename(pword));
                boolean okay = XslDriver.makeHtml(hit.xmlFile, xslfile,
                                                  hit.htmlFile);
                if (Dbg.VERBOSE) Dbg.print("created "+hit.xmlFile+
                                   " and "+(new File(hit.htmlFile)).getName());
                if (Dbg.ERROR && !okay)
                    Dbg.print("Error making file "+hit.htmlFile);
            }
            if (hit != null) {
                checkIntoCache(pword, hit);
            }
            if (Dbg.TIMING) {
                time=System.currentTimeMillis()-time;
                    Dbg.print("refreshCache(" + pword + ", " + needHtml +
                              ") took " + time+" ms");
            }
            return(hit);
        } catch(Exception e) {
            if (Dbg.ERROR)
                e.printStackTrace();
            return null;
        }
    }


    /** Set up a word in the Gloss cache, and put its Headword equivalents
     *  into the Headword cache.  Generates XML and HTML.
     *  Note that this calls Kirrkirr.setCurrentWord (on Headword words).
     *  Is this really a good idea?  (Good for GraphPanel, and Semantic
     *  Domains bad for Html?).
     *  This method needs some rethinking: some
     *  of this functionality should be moved up into
     *  Kirrkirr.setCurrentWord().  It isn't DictionaryCache stuff. CDM.
     *  @return The GlossCacheEntry for the passed in <code>gloss/ede</code>, or
     *      <code>null</code> if the item cannot be found, etc.
     */
    public synchronized GlossCacheEntry refreshCache(String gloss,
                                                       GlossDictEntry ede) {
        if (ede == null) return null;
        if (Dbg.VERBOSE) Dbg.print("refreshCache(gloss="+gloss+", "+ede+ ')');
        //gloss=gloss.replace('@',' ').trim();
        if (ede.numMatches() == 0) {
            // it's a greyed out entry with nothing to match...
            parent.setStatusBar(Helper.getTranslation(SC_NO_ENG_ENTRY));
            return null;
        }
        GlossCacheEntry hit = (GlossCacheEntry) glossCache.get(gloss);
        if (hit != null) {
            if (Dbg.VERBOSE) Dbg.print("refreshCache: found in gloss cache "
                                       + hit.xmlFile);
        } else {
            hit = new GlossCacheEntry(ede.numMatches());
            for (int i=0; i<ede.numMatches(); i++) {
                DictEntry de = (DictEntry) index.get(ede.headwords[i]);
                if (de == null) {
                    if (Dbg.ERROR)
                        Dbg.print("refreshCache("+gloss+", "+ede+") couldnt find dict entry for "
                                  +ede.headwords[i]+ '|');
                } else {
                    hit.headwords[i] = refreshCache(de.fpos);
                }
            }
            hit.xmlFile = RelFile.MakeWriteFileName(xmlFolder,
                                             Helper.wordToXmlFilename(gloss));
            hit.htmlFile = RelFile.MakeWriteFileName(htmlFolder,
                                         Helper.wordToCacheFilename(gloss));
            if ( ! generateGlossHtml(gloss, hit.headwords,
                                       hit.xmlFile, hit.htmlFile)) {
                if (Dbg.ERROR)
                    Dbg.print("refreshCache("+gloss+", "+ede+") couldnt generate gloss html");
                return null;
            }
            checkIntoGlossCache(gloss, hit);
        }
        // we might need to add the words to panels, even if we find it
        // in the cache....
        if (Dbg.VERBOSE) {
            Dbg.print("refreshCache: doing L1 setCurrentWord for " + ede.numMatches() +
                      " words");
        }
        HtmlPanel.freeze();
        for (int i = 0; i < hit.headwords.length; i++) {
            if (hit.headwords[i] != null) {
                if (Dbg.VERBOSE) {
                    Dbg.print("  Wrlp setCurrentWord for " + hit.headwords[i]);
                }
                parent.setCurrentWord(hit.headwords[i], false, null,
                                      parent.DICTIONARYCACHE, 0);
            }
        }
        HtmlPanel.unfreeze();
        return hit;
    }


    /** For reverse lookup of headword words by their file positions, used by
     *  gloss. Given a file position, gets the xml file at that position,
     *  and retrieves the headword. Adds that headword to the cache, and
     *  returns the padded headword string. Generates xml automatically
     *  (since it has to look up the file position anyway), and also the html.
     *  (Synchronized.)
     *  @return the unique key at this file position, or null if it can't
     *          be found or the xml file can't be created
     *  @param fpos the file position at which the word is stored.
     */
    public synchronized  String refreshCache(long fpos) {
        if (Dbg.VERBOSE) Dbg.print("refreshCache("+fpos+ ')');
        try {
            hit_count++; // this is vital or can trash existing in use XML
            String xmlfilename = RelFile.MakeWriteFileName(xmlFolder,
                                         Integer.toString(hit_count) + ".xml");
            // cw 2002: for accented chars. (see refreshCache(pword, needHtml))
//          Writer fs = new BufferedWriter(new FileWriter(xmlfilename));
//          XmlMiniDocument xmlmd = new XmlMiniDocument(headwordXPath,entryTag);
//          Document doc = xmlmd.setup(fs);
//          doc = parseXmlEntry(fpos, xmlmd, doc, fs);
//          doc = addDirectories(xmlmd, doc, false, fs);
//          doc = xmlmd.finish(doc, fs);
//          fs.close();
            FileOutputStream fos = new FileOutputStream(xmlfilename);
            OutputStreamWriter osw = new OutputStreamWriter(fos, RelFile.ENCODING);
            BufferedWriter bw = new BufferedWriter(osw);
            XmlMiniDocument xmlmd = new XmlMiniDocument(headwordXPath,entryTag);
            Document doc = xmlmd.setup(bw);
            doc = parseXmlEntry(fpos, xmlmd, doc, bw);
            doc = addDirectories(xmlmd, doc, false, bw);
            doc = xmlmd.finish(doc, bw);
            bw.close();

            String pword=getHeadword(fpos, doc);
            if (doc == null) {
                if (Dbg.ERROR) Dbg.print("DictionaryCache:refreshCache(fpos) Couldn't init doc for word at "+fpos);
                return null;
            }
            if (pword==null) {
                if (Dbg.ERROR) Dbg.print("DictionaryCache:refreshCache(fpos) Couldn't find word at "+fpos);
                return null;
            }
            // make fake DictEntry, containing only the fpos
            DictEntry fakeDE=new DictEntry();
            fakeDE.fpos=fpos;
            // this is all default initialization
            // fakeDE.hasPics=false;
            // fakeDE.hasSounds=false;
            // fakeDE.hasNote=false;
            // fakeDE.freq=0;
            // fakeDE.isSubword=false;
            CacheEntry hit = new CacheEntry(fakeDE,
                                 getLinksFromDoc(doc),
                                 getGlossFromDoc(doc),
                                 getDomainsFromDoc(doc),
                                 getSoundsFromDoc(doc),
                                 getPicturesFromDoc(doc),
                                 xmlfilename,
                                 null);
            // no longer make the HtmlFile
            checkIntoCache(pword, hit);
            return pword;
        } catch (Exception e) {
            if (Dbg.ERROR)
                e.printStackTrace();
            return null;
        }
    }


    /**  If the word is in the cache, it moves it to the end of the eviction
     *   queue. If the cache doesn't have the (padded) word, it adds it,
     *   and associates it with the CacheEntry. When adding a new word,
     *   other words may be evicted to keep the cache size to
     *   CACHE_SIZE. Words which are evicted also get their temporary
     *   files deleted.
     *   @param tailWord padded tailword to add to cache
     *   @param hit cacheentry to associate with the tailword
     */
    private void checkIntoCache(/* padded */ String tailWord, CacheEntry hit) {

        if (hitList.contains(tailWord)) {
            if(hitList.removeElement(tailWord)) {
                if (Dbg.TWO) Dbg.print(tailWord+" already there, refreshing");
                hitList.addElement(tailWord);
            }
        } else {
            //remove any elements beyond CACHE_SIZE
            while (cache.size() >= CACHE_SIZE) {
                String dele = (String)hitList.firstElement();
                hitList.removeElement(dele);

                CacheEntry dEntry = (CacheEntry) cache.remove(dele);
                // Cleanup the 2 files if they were generated
                if (dEntry.htmlFile != null) {
                    File target = new File(dEntry.htmlFile);
                    if(target.canWrite() && target.isFile()) {
                        target.delete();
                        if (Dbg.VERBOSE)
                            Dbg.print("deleted "+target.getName()+" storing "+
                                      dele + " html");
                    }
                    target = new File(dEntry.xmlFile);
                    if(target.canWrite() && target.isFile()) {
                        target.delete();
                        if (Dbg.VERBOSE)
                            Dbg.print("deleted "+target.getName()+" storing "+
                                      dele + " xml");
                    }
                }
            }
            if (Dbg.TWO) Dbg.print(tailWord +" added to cache!");
            hitList.addElement(tailWord);
            cache.put(tailWord, hit);
            if (Dbg.VERBOSE)
                Dbg.print("size of cache "+cache.size());
        }
    }

    /**  If the word is in the cache, it moves it to the end of the eviction
     *   queue. If the cache doesn't have the (padded) word, it adds it,
     *   and associates it with the CacheEntry. When adding a new word,
     *   other words may be evicted to keep the cache size to
     *   CACHE_SIZE. Words which are evicted also get thier temporary
     *   files deleted.
     *   @param gloss padded tailword to add to cache
     *   @param hit cacheentry to associate with the tailword
     */
    private void checkIntoGlossCache(String gloss, GlossCacheEntry hit) {

        if(glossHitList.contains(gloss)) {
            if(glossHitList.removeElement(gloss)) {
                if (Dbg.TWO) Dbg.print(gloss+" already there, refreshing");
                glossHitList.addElement(gloss);
            }
        } else {
            //remove any elements not needed
            while (glossHitList.size() >= CACHE_SIZE) {
                String dele = (String)glossHitList.firstElement();
                glossHitList.removeElement(dele);

                GlossCacheEntry dEntry = (GlossCacheEntry) glossCache.remove(dele);
                // Cleanup the 2 files if they were generated
                if (dEntry.htmlFile != null) {
                    File target = new File(dEntry.htmlFile);
                    if(target.canWrite() && target.isFile()) {
                        target.delete();
                        if (Dbg.VERBOSE) Dbg.print("deleted "+target.getName());
                    }
                    target = new File(dEntry.xmlFile);
                    if(target.canWrite() && target.isFile()) {
                        target.delete();
                        if (Dbg.VERBOSE) Dbg.print("deleted "+target.getName());
                    }
                }
            }
            if (Dbg.TWO) Dbg.print(gloss +" added to cache!");
            glossHitList.addElement(gloss);
            glossCache.put(gloss, hit);
            if (Dbg.TWO) Dbg.print("size of cache "+glossCache.size());
        }
    }

    /** Open a buffered input stream for the xml entry of
     *  the DictEntry - opens a stream to the large xml dictionary,
     *  and skip to the part of the xml dictionary that the fpos points to.
     *  This tried to do clever stuff with resetting file pointers, but it
     *  seems inoperative in JDK1.3, so I've put in a flag.
     *
     *  @param fpos The file position of the DictEntry whose XML entry to skip to
     *  @return the BufferedInputStream, open to the place where
     *          the DictEntry's XML entry is at
     */
    private synchronized static BufferedInputStream setup_bis(long fpos)
                                                        throws IOException {
        if (Dbg.VERBOSE) Dbg.print("setup_bis go to offset " + fpos +
                                   " markedFpos is " + markedFpos);
        if (Kirrkirr.APPLET) {
            URL xml_url = new URL(xmlFilename);
            URLConnection yc = xml_url.openConnection();
            bis = new BufferedInputStream(yc.getInputStream());
            // bis.skip(fpos);
            /* assumes the current file position is at 0.  This looping
             * was necessary because of what looks like a java-bug: URL
             * streams won't skip more than 1144 bytes, but FileStreams will?
             */
            long left = fpos;
            while (left > 0) {
                long actual = bis.skip(left);
                left = left - actual;
            }
        } else {
            if (MARKLIMIT == 0) {
                // close file if open, and then open and skip.
                if (bis != null) {
                    bis.close();
                }
                long actual;
                bis = new BufferedInputStream(new FileInputStream(xmlFilename));
                actual = bis.skip(fpos);
                if (Dbg.VERBOSE)
                    Dbg.print("setup_bis always reopen wanted to skip " +
                              (fpos - markedFpos) + " skipped " + actual);
            } else {
                // try to reuse the same filehandle if scanning forward;
                boolean okay = false;
                if (bis != null && fpos >= markedFpos) {
                    // rewind to marked pos, then scan from there
                    try {
                        long actual;
                        bis.reset();
                        actual = bis.skip(fpos - markedFpos);
                        if (Dbg.VERBOSE)
                            Dbg.print("setup_bis relative wanted to skip " +
                                   (fpos - markedFpos) + " skipped " + actual);
                        okay = true;
                    } catch (IOException ioe) {
                        // entry was too long probably
                    }
                }
                if ( ! okay) {
                    // close file if open, and then open and skip.
                    if (bis != null) {
                        bis.close();
                    }
                    long actual;
                    bis = new BufferedInputStream(new FileInputStream(xmlFilename));
                    actual = bis.skip(fpos);
                    if (Dbg.VERBOSE)
                        Dbg.print("setup_bis reopen wanted to skip " +
                                  (fpos - markedFpos) + " skipped " + actual);
                }
                bis.mark(MARKLIMIT);
                markedFpos = fpos;
            }
        }
        return bis;
    }


    /** Called when done reading from buffered input stream.
     *  If applet, close the stream. Otherwise keep it open
     *  for next time we read from stream. (Synchronized).
     *  @param bis the BufferedInputStream to finish
     */
    private static synchronized void finishup_bis(BufferedInputStream bis) throws IOException {
        if (Kirrkirr.APPLET) {
            bis.close();
            bis = null;
        }
    }

    /** Called by the Search Panel to search an entry for a particular
     *  regular expression (which may be just content, or may contain a
     *  regular expression that mandates a match within a certain element
     *  Returns a string giving the match (first matching string in entry).
     *  (Synchronized).
     *  This is now trying to remember and cache file positions so as to
     *  be more efficient (if run from file and not an applet).  In the
     *  current implementation, we keep where possible the InputStream
     *  but trash and reopen each time the Readers that sit on top of it.
     *  @param uniqueKey This is the entry in which to search
     *  @param p5m The regular expression matcher engine
     *  @param reg   The regular expression to look for
     *  @param regexHasDollarOne If true, return matched partial pattern;
     *          otherwise return the whole line if there is a match
     *  @return Return a string which will be a line or part line with the
     *          match, or null if there wasn't one.
     */
    public synchronized String getSearchMatch(String uniqueKey,
                                              Perl5Matcher p5m, Pattern reg,
                                              boolean regexHasDollarOne) {
        DictEntry de = getIndexEntry(uniqueKey);
        if (de == null) {
            if (Dbg.ERROR) Dbg.print("DictionaryCache:getSearchMatch: not found "+uniqueKey);
            return(null);    //empty entry with null attributes
        }
        if (Dbg.THREE) Dbg.print("looking up entry for |" + uniqueKey +
                                 "| at offset " + de.fpos);

        try {
            // cdm: does 2 levels of buffering make sense? Try with just one?
            // But note that readLine method is only in BufferedReader
            // Note that we toss the Reader each time, since it's unclear how
            // to tell it that we've moved the underlying InputStream position

            BufferedInputStream bis = setup_bis(de.fpos);
            BufferedReader br = new BufferedReader(new InputStreamReader(bis));

            /* the xml end of entry string for the dictionary */
            final String entryEnd = "</" + parent.dictInfo.getEntryTag() + '>';
            String matchLine = null;

            for (String line; (line = br.readLine()) != null &&
                   line.indexOf(entryEnd) < 0; ) {
                if (p5m.contains(line, reg)) {
                    MatchResult matchRes = p5m.getMatch();
                    if (regexHasDollarOne) {
                        matchLine = matchRes.group(1);
                    } else {
                        matchLine = line;
                        // was this. wrong? matchLine = matchRes.toString();
                    }
                    break;
                }
            }
            finishup_bis(bis);
            return matchLine;
        } catch (Exception e) {
            Helper.handleException(e);
            return null;
        }
    }


    /**
     * Used by Profile Manager for saving the cache into profiles,
     * by serialization. Throws an exception if the objects cannot
     * be written out or their classes cannot be found,
     *  but this should never happen.
     * KP: took out - not useful to save cache since temp
     * files are gone anyway?
     * @param oos the ObjectOutputStream to write the cache to
     * @see Kirrkirr.ui.data.ProfileManager
     * @see #loadCache(ObjectInputStream)
     */
    //Madhu:'00, for saving into Profiles
    public void saveCache(ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        /*oos.writeInt(size);
        oos.writeObject(hitList);
        oos.writeObject(cache);*/
    }

    /**
     * Used by Profile Manager for loading the cache from a profile,
     * by serialization. Throws an exception if the objects cannot
     * be written out or their classes cannot be found,
     * but this should never happen.
     * KP: took out - not useful to save cache since temp
     * files are gone anyway?
     * @param ois the ObjectInputStream to read the cache from
     * @see Kirrkirr.ui.data.ProfileManager
     * @see #saveCache(ObjectOutputStream)
     */
    //Madhu:'00, for loading from Profiles
    public void loadCache(ObjectInputStream ois) throws IOException, ClassNotFoundException, InvalidClassException {
        /*size = ois.readInt();
        hitList = (Vector)ois.readObject();
        cache = (Hashtable)ois.readObject();*/
    }


    //--- functions to read stuff from XML ----

    /** Returns the glosses for the given uniqueKey
     *  as a Vector, since there may be more than one.
     *  The elements of the vector are of class DictField (an historical
     *  relic, but not yet changed - should be String).  The
     *  difference between this and getFirstGloss is that getFirstGloss
     *  returns a string (the first entry in the Vector).
     *  @param uniqueKey The unique key to look up
     *  @return A Vector of DictField containing glosses, or <code>null</code> if the word is not
     *       found
     *  @see #getFirstGloss
     */
    public Vector<DictField> getGlossEntry(String uniqueKey) {
        CacheEntry hit = refreshCache(uniqueKey, false);
        if (hit == null) {
            if (Dbg.ERROR) Dbg.print("DictionaryCache:getGlossEntry: hit is null for " + uniqueKey);
            return null;
        }
        if (Dbg.CACHE) Dbg.print("getGlossEntry: entry is: " + hit.gloss);
        return hit.gloss;
    }


    /** Gets a single gloss word for the the given unique key.
     *  Used in FunPanel to show gloss.
     *  @param uniqueKey The word uniqueKey to look up
     *  @return The gloss (gloss word) for the entry or NO_ENG if there
     *  is no glass.  It is guaranteed that this entry will never return
     *  null.  NO_ENG is also returned in error conditions.
     *  @see #NO_ENG
     *  @see #getGlossEntry
     */
    public String getFirstGloss(final String uniqueKey) {
        Vector found = getGlossEntry(uniqueKey);
        if (found != null && found.size() > 0) {
            DictField df = (DictField) found.elementAt(0);
            String ans = df.uniqueKey;
            if (ans.length() > 35) {
                ans = ans.substring(0, 32) + "...";
            }
            if (Dbg.CACHE) {
                Dbg.print("getFirstGloss: for " +
                          Helper.uniqueKeyToPrintableString(uniqueKey) +
                          " is " + ans);
            }
            return ans;
        } else {
            if (found == null && Dbg.ERROR)
                Dbg.print("getFirstGloss: word not found " +
                                Helper.uniqueKeyToPrintableString(uniqueKey));
            return NO_ENG;
        }
    }


    /** Gets the semantic domains for the primary sense of the
     *  given padded word as a
     *  DictFields. Used by semantic explorer.
     *  @param uniqueKey the padded word to look up
     *  @return the semantic domains of the entry, or null if the word
     *  cannot be found in the dictionary. A list of DictFields (which
     *  is itself a list of the domain path).
     */
    public Vector getDomainsEntry (String uniqueKey) {
        Vector allDomains = getAllDomains(uniqueKey);
        if (allDomains == null || allDomains.isEmpty()) {
            return null;
        } else {
            return (Vector) allDomains.elementAt(0);
        }
    }

    /** Gets ALL domain information for a given uniqueKey, collecting
     * DictFields in a Vector, with one DictFields per sense
     */
    public Vector getAllDomains(String uniqueKey) {
        CacheEntry hit = refreshCache(uniqueKey, false);
        if (hit == null) {
            if (Dbg.ERROR) Dbg.print("DictionaryCache:getDomainsEntry: not found "+uniqueKey);
            return null;
        }
        return hit.domains;
    }

     /** Gets the sounds for the given unique key as a DictFields. Used by
      *  MediaPanel.
      *  @param word the unique key to look up
      *  @return the sounds of the entry, or null if the word
      *  cannot be found in the dictionary
      */
    public DictFields getSounds(String word) {
        CacheEntry hit = refreshCache(word, false);
        if (hit == null){
            if (Dbg.ERROR) Dbg.print("DictionaryCache:getSounds: not found "+word);
            return null;
        }
        return hit.sounds;
    }

     /** Gets the pictures for the given padded word as a DictFields.
      *  Used by MediaPanel.
      *  @param uniqueKey the padded word to look up
      *  @return the pictures of the entry, or null if the word
      *  cannot be found in the dictionary
      */
    public DictFields getPictures(String uniqueKey) {
        CacheEntry hit = refreshCache(uniqueKey, false);
        if (hit == null){
            if (Dbg.ERROR) {
              Dbg.print("DictionaryCache:getPictures: uniqueKey not found " +
                                     uniqueKey);
            }
            return null;
        }
        return hit.pictures;
    }


    /** Returns a Vector of unique keys for words linked to by the argument
     *  word.  Used by GraphPanel to draw graph. Used also by QuizMasterPanel.
     *
     *  @param uniqueKey The unique key to look up
     *  @return The links for the entry or <code>null</code> if the word is
     *       not found in the dictionary
     */
    public Vector getLinksForWord(String uniqueKey) {
        if (uniqueKey == null) {
            if (Dbg.ERROR)
                Dbg.print("getLinksForWord called with null word");
            return null;
        }
        if (Dbg.CACHE) {
            Dbg.print("getLinksForWord for |" + uniqueKey + '|');
        }
        CacheEntry hit = refreshCache(uniqueKey, false);
        if (hit == null) {
            if (Dbg.ERROR) Dbg.print("DictionaryCache:getLinksForWord: not found " + uniqueKey);
            return null;
        } else if (Dbg.VERBOSE) {
            Dbg.print("getLinksForWord: "+uniqueKey+": "+hit.links.size()+
                      " links: "+hit.links);
        }
        return hit.links.toVector();
    }


    /** This routine gets the links from an entry.
     *  Used by GraphPanel to draw graph. Used also by QuizMasterPanel.
     *  @param uniqueKey the padded word to look up
     *  @return the links for the entry or null if the word is not found in
     *          the dictionary
     */
    public DictFields getDictEntryLinks(String uniqueKey) {
        if (uniqueKey == null) {
            if (Dbg.ERROR)
                Dbg.print("getDictEntryLinks called with null word");
            return null;
        }
        if (Dbg.CACHE) {
            Dbg.print("getDictEntryLinks for |" + uniqueKey + '|');
        }
        CacheEntry hit = refreshCache(uniqueKey, false);
        if (hit == null) {
            if (Dbg.ERROR) Dbg.print("getDictEntryLinks: not found " + uniqueKey);
            return null;
        }

        return hit.links;
    }


    /** Gets the dialects associated with the current DictEntry.
     *  Returns null if no dialects are. This is not 100%
     *  accurate, since dialects can be for other senses
     *  besides the main sense. Should we restrict it?
     *  CDM: Have this return DictFields??  Or change getDialectFromDoc
     *  to return Vector??
     *
     *  @return Vector of dialects (Strings) associated with the DictEntry,
     *  or null if there are none.
     */
    public Vector<String> getDialect(DictEntry de)
    {
        Document myxml = getXmlEntry(de.fpos);
        DictFields dial = getDialectFromDoc(myxml);
        if (dial==null || dial.size()==0) return null;
        Vector<String> v = new Vector<String>();
        for (int i=0;i<dial.size();i++)
            v.addElement(dial.get(i).uniqueKey);
        return v;
    }

    /** Gets the parts of speech (POS) associated with the current DictEntry.
     *  Returns null if no POS are. This returns all POS in the
     *  DictEntry - so if other senses have different POS,
     *  it will return those too. (Should we restrict it?)
     *
     *  @return Vector of parts of speech (Strings) associated with the DictEntry,
     *  or null if there are none.
     */
    public Vector getPOS(DictEntry de)
    {
        Document myxml = getXmlEntry(de.fpos);
        DictFields pos = getPOSFromDoc(myxml);
        if (pos==null || pos.size()==0) return null;
        Vector<String> v = new Vector<String>();
        for (int i=0;i<pos.size();i++)
            v.addElement(pos.get(i).uniqueKey);
        return v;
    }

    /** Whether or not the current DictEntry is associated
     *  with a special register (such as Baby Talk, etc).
     *  This should be used to reject words for games,
     *  since many speakers do not know these words.
     *  This returns true even if other senses have a register,
     *  but the main one doesn't. (Should we restrict it?)
     */
    public boolean hasRegister(String pword){
        DictEntry de = getIndexEntry(pword);
        return hasRegister(de);
    }

    /** Whether or not the current DictEntry is associated
     *  with a special register (such as Baby Talk, etc).
     *  This should be used to reject words for games,
     *  since many speakers do not know these words.
     *  This returns true even if other senses have a register,
     *  but the main one doesn't. (Should we restrict it?)
     *  The argument may be null, and then false is returned.
     */
    public boolean hasRegister(DictEntry de) {
        if (de==null)
            return false;
        Document myxml = getXmlEntry(de.fpos);
        return getRegisterFromDoc(myxml);
    }

    /**
     * Returns true if the entry for the padded word is a subentry rather
     * than a main word. Used by semantic explorer and Kirrkirr
     * @param pword padded word to lookup
     * @return true if the entry is a subentry, false if it is null or not
     * a subentry
     */
    public boolean isGlossSubWord(/* hnum padded */ String pword) {
        GlossDictEntry dE = getGlossIndexEntry(pword);
        if (dE == null) {
            return false;
        }
        return dE.isSubword;
    }
    /**
     * Returns true if the entry for the uniqueKey pword is a subentry rather
     * than a main word. Used by semantic explorer and Kirrkirr.
     *
     * @param pword Unique key to lookup
     * @return true if the entry is a subentry, false if it is null or not
     *     a subentry
     */
    public boolean isSubWord(String pword) {
        DictEntry dE = getIndexEntry(pword);
        if (dE == null) {
            return false;
        }
        return dE.isSubword;
    }

    /**
     * Returns the frequency of the given padded word.
     * @param uniqueKey Unique key to lookup
     * @return the frequency of the padded word or 0 if the word cannot be
     * found in the dictionary.
     */
    public int getFreq(String uniqueKey) {
        DictEntry dE = getIndexEntry(uniqueKey);
        if (dE == null) {
            return 0;
        }
        return dE.freq;
    }

    public int getGlossFreq(String uniqueKey) {
        GlossDictEntry dE = getGlossIndexEntry(uniqueKey);
        if (dE == null) {
            return 0;
        }
        return dE.freq;
    }

    public static Vector<String> getHeadwords(GlossDictEntry ede){
        if (ede==null || ede.headwords ==null) return null;
        Vector<String> v = new Vector<>();
        for (int i = 0; i < ede.headwords.length; i++) {
            //String cur=getHeadword(ede.fpos[i]);
            if (ede.headwords[i] != null) {
                v.addElement(ede.headwords[i]);
            }
        }
        return v;
    }

    /** Gets the headword from the xml entry at fpos.
     *  Used for reverse lookup by GlossDictEntries.
     *  @param fpos the fpos to look for the xml entry
     *  @return the padded headword from the document
     */
    public synchronized /* uniqueKey */ String getHeadword(long fpos)
    {
        Document doc=getXmlEntry(fpos);
        if (doc == null) {
            if (Dbg.ERROR) Dbg.print("DictionaryCache:getHeadword(fpos): null doc "+fpos);
            return(null);
        }
        return getHeadword(fpos, doc);
    }


    /** Gets the headword from the xml entry at fpos.
     *  Used for reverse lookup by GlossDictEntries.
     *  @param fpos the fpos to look for the xml entry
     *  @param doc the XmlDocument with the XmlEntry
     *  @return the uniqueKey headword from the document
     */
    private synchronized String getHeadword(long fpos, Document doc) {
        //get headword uniqueKey
        String word = getHeadWordFromDoc(doc);
        if (word == null) {
            if (Dbg.ERROR) {
                Dbg.print("getHeadword(fpos, doc): headword null "+fpos);
            }
            return null;
        }
        String polys = getHnumFromDoc(doc);
        return Helper.makeUniqueKey(word, polys);
    }


    /** Gets all the links from an xml document (ie, all synonyms,
     *  antonyms, collocations, etc). This is different from getLinksFromDoc
     *  because it does not limit the number of links returned.
     *
     *  @param doc the XmlDocument with the XmlEntry
     *  @return Vector containing DictFields for all the links in the xml document.
     *          These combine links from all senses.
     */
    public Vector getAllLinksFromDoc(Document doc) {
        Vector results = new Vector();
        DictionaryInfo dictInfo = parent.dictInfo;
        int len = dictInfo.getNumLinks();

        for (int i = 0; i < len; i++) {
            String xPathForLink = parent.dictInfo.getLinkXPath(i);
            Vector newEntry = getItemsForEachSenseFromDoc(doc, xPathForLink,
                                  DictField.NUM_LINKS_DISPLAYED, EXTRACT_OTHER,
                                  dictInfo, (short) i);
            //now have multiple sense capability; join all
            for (int j = 0, sz = newEntry.size(); j < sz; j++) {
                Vector linksForSense = (Vector) newEntry.elementAt(j);
                for (int k = 0, ksz = linksForSense.size(); k < ksz; k++) {
                    results.addElement(linksForSense.elementAt(j));
                }
            }
        }

        return results;
    }


    /** Gets some of the links from an xml document (ie, synonyms,
     *  antonyms, collocations, etc). This is different from getAllLinksFromDoc
     *  because it limits the number of links returned to
     *  DictField.NUM_LINKS_DISPLAYED,
     *  though it tries to make them diverse (see niceSelection).
     *
     *  @param doc The XmlDocument with the XmlEntry
     *  @return Vector containing DictFields for most of the links in the xml document
     *  @see DictFields#niceSelection
     */
    private DictFields getLinksFromDoc(Document doc) {
        // cdm: redo this to avoid for-loop?  Make 1 pass over DOM tree
        Vector results = new Vector();
        DictionaryInfo dictInfo = parent.dictInfo;

        int numTypes = dictInfo.getNumLinks();  // number of types of links
        if (Dbg.CACHE) {
            Dbg.print("There are " + numTypes + " defined dictionary links");
        }

        for (int i = 0; i < numTypes; i++) {
            Vector newEntry = getItemsForEachSenseFromDoc(doc, dictInfo.getLinkXPath(i),
                                  DictField.NUM_LINKS_DISPLAYED, EXTRACT_OTHER,
                                  dictInfo, (short) i);
            // getItemsForEachSenseFromDoc prints this, don't need it again
            // if (Dbg.CACHE) {
            //     Dbg.print("For link type " + i + " links are " + newEntry);
            // }
            // now have multiple sense capability; for now we'll just ignore
            // that and use all senses' results
            for (int k = 0, nSize = newEntry.size(); k < nSize; k++) {
                Vector firstSense = (Vector) newEntry.elementAt(k);
                if (firstSense != null) {
                    for (int j = 0, fSize = firstSense.size(); j < fSize; j++) {
                        results.addElement(firstSense.elementAt(j));
                    }
                }
            }
        }

        return DictFields.niceSelection(results, DictField.NUM_LINKS_DISPLAYED,
                                                this);
    }


    /** Returns all the glosses for the word in the xml document.
     *  This returns all the glosses for the first sense (only)
     *
     *  @param doc the XmlDocument with the XmlEntry
     *  @return A Vector of DictField containing the glosses in the document
     */
    private Vector getGlossFromDoc(Document doc) {
        DictionaryInfo dictInfo = parent.dictInfo;

        Vector newEntry = getItemsForEachSenseFromDoc(doc, dictInfo.getGlossXPath(),
                DictField.DEFAULT_GLOSS, EXTRACT_OTHER, dictInfo,
                (short) DictField.UNDEFINED);

        if (newEntry == null || newEntry.isEmpty()) {
            return null;
        } else {
            return (Vector) newEntry.elementAt(0);
        }
    }


    /** Returns true if there is a register in the xml document.
     *  (Can also implement to return the registers, if desired.)
     *
     *  @param doc the XmlDocument with the XmlEntry
     *  @return true if there is at least one register directly under the
     *          ENTRY (ie, not under a SENSE, PDX, etc)
     */
    private boolean getRegisterFromDoc(Document doc) {
        //look for "REGISTER"
        DictionaryInfo dictInfo = parent.dictInfo;

        NodeList fields =
            getNodeListFromRoot(doc.getDocumentElement(),
                                dictInfo.getRegisterXPath());
        return(fields != null && fields.getLength() > 0);

    }

    /** Returns the semantic domains for the word in the xml document.
     *  These are a Vector of Vectors of String unique keys.
     *
     *  @param doc the XmlDocument with the XmlEntry
     *  @return Vector containing a Vector for each sense of the semantic
     *          domains in the document. The semantic domain might be a
     *          String or a unique key.
     */
    private Vector getDomainsFromDoc(Document doc) {
        DictionaryInfo dictInfo = parent.dictInfo;
        // in EXTRACT_DOMAIN, return value is a Vector of Vector of uniqueKey (String)
        Vector doms = getItemsForEachSenseFromDoc(doc, dictInfo.getDomainComponentFullXPath(),
                DictField.DEFAULT_GLOSS, EXTRACT_DOMAIN, dictInfo,
                (short) DictField.UNDEFINED);
        if (Dbg.CACHE) Dbg.print("Domains are: " + doms);
        return doms;
    }


    /** Returns the uniquifier for the word in the xml document as a string,
     *  or null if it can't be found.
     *  @param doc the XmlDocument with the XmlEntry
     *  @return String of the homonym number, or null if one
     *          can't be found.
     */
    private String getHnumFromDoc(Document doc) {
        DictionaryInfo dictInfo = parent.dictInfo;
        return extractElementInformation(doc.getDocumentElement(),
                                         dictInfo.getUniquifierXPath());
    }


    /** Returns the headword in the xml document.
     *  @param doc the XmlDocument with the XmlEntry
     *  @return String of the headword, or {@code null} if one
     *          can't be found.
     */
    private String getHeadWordFromDoc(Document doc) {
        DictionaryInfo dictInfo = parent.dictInfo;
        return extractElementInformation(doc.getDocumentElement(),
                                         dictInfo.getHeadwordXPath());
    }


    /** Returns the dialects in the xml document.
     *  This is not 100% accurate, since dialects can be for other senses
     *  besides the main sense. Should we restrict it?
     *
     *  @param doc the XmlDocument with the XmlEntry
     *  @return DictFields containing the dialects in the document
     */
    private DictFields getDialectFromDoc(Document doc){
        //look for "POS"
        DictionaryInfo dictInfo = parent.dictInfo;
        Vector newEntry = getItemsForEachSenseFromDoc(doc, dictInfo.getDialectXPath(),
                DictField.NUM_LINKS_DISPLAYED, EXTRACT_OTHER, dictInfo,
                (short) DictField.UNDEFINED);
        if (newEntry.isEmpty()) return null;
        Vector entry = (Vector) newEntry.elementAt(0);
        if (entry == null) {
            return null;
        } else {
            return new DictFields(entry);
        }
    }


    /** Returns the POS in the xml document.
     *  This is not 100% accurate, since POS can be for other senses
     *  besides the main sense. Should we restrict it?
     *  @param doc the XmlDocument with the XmlEntry
     *  @return DictFields containing the parts of speech in the document
     */
    public DictFields getPOSFromDoc(Document doc){
        DictionaryInfo dictInfo = parent.dictInfo;

        String xpath = dictInfo.getPOSXPath();
        if (xpath == null) return null;

        Vector newEntry = getItemsForEachSenseFromDoc(doc, xpath,
                DictField.NUM_LINKS_DISPLAYED, EXTRACT_OTHER, dictInfo,
                (short) DictField.UNDEFINED);

        if (newEntry.isEmpty()) return null;
        Vector entry = (Vector) newEntry.elementAt(0);
        if (entry == null) {
            return null;
        } else {
            return new DictFields(entry);
        }
    }


    /** Returns the sound files for the word in the xml document.
     *
     *  @param doc the XmlDocument with the XmlEntry
     *  @return DictFields containing the sound files in the document
     */
    private DictFields getSoundsFromDoc(Document doc) {
        DictionaryInfo dictInfo = parent.dictInfo;

        String xpath = dictInfo.getAudioXPath();
        if (xpath == null) return null;

        Vector newEntry = getItemsForEachSenseFromDoc(doc, xpath,
                DictField.DEFAULT_GLOSS, EXTRACT_OTHER, dictInfo,
                (short) DictField.UNDEFINED);

        if (newEntry.isEmpty()) return null;
        Vector entry = (Vector) newEntry.elementAt(0);
        if (entry == null) {
            return null;
        } else {
            return new DictFields(entry);
        }
    }


    /** Returns the image files for the word in the xml document.
     *
     *  @param doc the XmlDocument with the XmlEntry
     *  @return DictFields containing the image files in the document
     */
    private DictFields getPicturesFromDoc(Document doc) {
        DictionaryInfo dictInfo = parent.dictInfo;

        String xpath = dictInfo.getImagesXPath();
        if(xpath == null) return null;

        Vector<Vector<Object>> newEntry = getItemsForEachSenseFromDoc(doc, xpath,
                DictField.DEFAULT_GLOSS, EXTRACT_OTHER, dictInfo,
                (short) DictField.UNDEFINED);

        if (newEntry.isEmpty()) return null;
        Vector entry = newEntry.elementAt(0);
        if (entry == null) {
            return null;
        } else {
            return new DictFields(entry);
        }
    }


    /** Extract a particular type of link or
     *  other information from an XML lexical entry.  This method returns
     *  a Vector of Vectors of a certain type.  The type may be uniqueKeys
     *  (Strings) or may be another type defined for the extractMode.
     *  At the top level, it constructs one
     *  vector element for each sense (the zeroth element is for stuff contained
     *  in the top-level entry, and other elements are for things under a sense
     *  node).  For each sense
     *  there is a Vector of elements.  This Vector may be empty
     *  but is never null.
     *
     *  @param doc The (XML) Document (DOM object) to extract stuff from.
     *             This document only has a 1 word dictionary in it.
     *  @param xpath The XPath from which to extract the information.
     *  @param max The maximum of this type of thing to extract.
     *  @param extractMode EXTRACT_DOMAIN for special code for domain
     *          extraction, otherwise EXTRACT_OTHER
     *  @param dictInfo The DictionaryInfo that is used to give XPaths needed
     *              for finding senses
     *  @param tagNum The tagNum to fill in in the DictFields that are returned.
     *  @return A Vector of Vectors, containing entries of the items for each
     *          sense.  For EXTRACT_DOMAIN, each item is a uniqueKey.  At
     *          present in all other cases it is a DictField
     */
    private static Vector<Vector<Object>> getItemsForEachSenseFromDoc(Document doc,
                                               String xpath,
                                               int max,
                                               int extractMode,
                                               DictionaryInfo dictInfo,
                                               short tagNum) {
        Vector newEntry = new Vector();

        if (dictInfo == null) {
            if (Dbg.ERROR) {
                Dbg.print("DictionaryCache: DictionaryInfo is null!");
            }
            return null;
        }
        Element  root = (Element) doc.getFirstChild();
        Vector<Node> senses = new Vector<Node>();
        senses.addElement(root);

        //support for multiple senses: we make a list to look up information in primary entry and each sense
        String senseXPath = dictInfo.getSenseXPath();
        if (senseXPath != null) {
            NodeList roots = getNodeListFromRoot(root, senseXPath);
            for (int r = 0, rleng = roots.getLength(); r < rleng; r++)
                senses.addElement(roots.item(r));
        }
        if (Dbg.CACHE) {
            Dbg.print("getItemsForEachSenseFromDoc for " + xpath + "; word has " + senses.size() + " senses");
        }

        //process each sense
        for (int sense = 0, ssize = senses.size(); sense < ssize; sense++) {
            root = (Element) senses.elementAt(sense);
            String xPathToUse;
            if (sense == 0) {
                xPathToUse = xpath;
            } else {
                String match = dictInfo.getEntryTag() + '/';
                int index = xpath.lastIndexOf(match) + match.length();
                // int index = dictInfo.getDictionaryTag().length() +
                //     dictInfo.getEntryTag().length() + 3; //+3 for three '/' chars
                xPathToUse = //senseXPath + // we now first of all root at the sense
                     xpath.substring(index);
            }
            if (Dbg.CACHE) {
                if (sense == 0) {
                    Dbg.print("  extracting from xpath " + xPathToUse + " (tagNum " + tagNum + ")");
                } else {
                    Dbg.print("  extracting from xpath " + xPathToUse +
                              " from within " + senseXPath + " (tagNum " + tagNum + ")");
                }
            }
            NodeList items = getNodeListFromRoot(root, xPathToUse);
            Vector fieldsForSense = new Vector();

            if (items == null) {
                newEntry.addElement(fieldsForSense); // add empty list for sense
                continue;
            }
            int itemsSize = items.getLength();
            if (itemsSize == 0) {
                newEntry.addElement(fieldsForSense); // placeholder empty list if no
                                                     // matching field for this sense
                continue;
            }

            int limiter = 0; //reset limiter for new sense
            for (int j = 0; (j < itemsSize) && (limiter < max) ; j++) {
                Node item = items.item(j);

                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element en_item = (Element) item;
                    String value;
                    String uniquifier = null;

                    // gets the XPATH that references headword within element
                    String exact = extractElementInformation(en_item,
                                             dictInfo.getLinkHeadwordXPath());
                    if (exact != null && ! exact.isEmpty() &&
                        ! exact.equals(DictField.UNKNOWN)) {
                        value = exact;

                        uniquifier = extractElementInformation(en_item,
                                        dictInfo.getLinkUniquePaddingXPath());
                         if (Dbg.CACHE) {
                            Dbg.print("  tag " + en_item.getNodeName() + " match " + j + " via " +
                                      dictInfo.getLinkHeadwordXPath() +
                                      " exact is " + exact + "; uniquifier is " + uniquifier);
                        }
                    } else {
                        // We didn't find an exact headword-resolved match
                        // Use (new) Helper fn. so Text is collated properly
                        // (possibility of multiple Text nodes under a single
                        // Element (e.g., when have escaped character
                        // entities)
                        // cw 2002
                        value = Helper.getElementText(en_item);
                       if (Dbg.CACHE) {
                            Dbg.print("  tag " + en_item.getNodeName() + " match " + j +
                                      " nonexact is " + value + "; uniquifier is " +
                                      uniquifier + ", exact is " + exact);
                        }
                        /*
                        String hnum = extractElementInformation(en_item,
                                        dictInfo.getLinkUniquePaddingXPath());
                        if (Dbg.CACHE) {
                            Dbg.print("getItemsForEachSenseFromDoc: hnum is " + hnum);
                        }
                        if (hnum!=null && ! hnum.equals("")) {
                            if (hnum.equals(DictField.UNKNOWNHNUM)) {
                                temp.hnum = DictField.UNRESOLVED;
                            } else {
                                try {
                                    temp.hnum = Short.parseShort(hnum);
                                } catch (Exception ex) {
                                    temp.hnum = (short) hnum.hashCode();
                                }
                            }
                        } else {
                            temp.hnum = DictField.BLANK;
                        }
                        */
                    }

                    if ( extractMode == EXTRACT_DOMAIN ) {
                        String uniqueKey = Helper.makeUniqueKey(value, uniquifier);
                        fieldsForSense.addElement(uniqueKey);
                    } else {  // extract_mode == EXTRACT_OTHER
                        String uniqueKey = Helper.makeUniqueKey(value, uniquifier);
                        DictField temp = new DictField(tagNum, uniqueKey);
                        fieldsForSense.addElement(temp);
                    }
                    limiter++;
                } else {
                    if (Dbg.CACHE) {
                        Dbg.print("  Error: Item for sense " + j + " is not an ELEMENT_NODE!");
                    }
                }
            }
            newEntry.addElement(fieldsForSense);
        }
        if (Dbg.CACHE) Dbg.print("  EXTRACTED: " + newEntry);
        return newEntry;
    }


    public static NodeList getNodeListFromRoot(Element root, String xpath) {
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
            if (Dbg.CACHE && Dbg.VERBOSE) {
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


    /** Class EIndexLoader loads the gloss dictionary index (clk) file
     *  as a separate thread. Until this finishes loading, the button to
     *  switch to gloss is disabled. If the gloss dictionary can't
     *  load, it deletes the button to switch to gloss entirely.
     *  The thread is initialized in DictionaryCache, but started in
     *  Kirrkirr after things are loaded (plus sleeping a bit to let
     *  Swing catch up).
     */
    class EIndexLoader implements Runnable {

        @Override
        public void run() {
            if (Dbg.CACHE) {
                Dbg.print("engIndexFile is |" + Kirrkirr.engIndexFile + '|');
            }
            if (Kirrkirr.engIndexFile == null || Kirrkirr.engIndexFile.isEmpty()) {
                parent.disableGlossList();
                glossLoader=null;
                return;
            }
            try {
                glossList = null; // allow any old glossList to be garbage collected
                glossIndex = null; // allow any old glosssIndex to be garbage collected
                long time = -1;
                if (Dbg.TIMING) time=System.currentTimeMillis();
                InputStream is;
                if (parent.APPLET) {
                    URL index_url = RelFile.makeURL(RelFile.dictionaryDir, Kirrkirr.engIndexFile); //new URL(Kirrkirr.engIndexFile);
                    URLConnection yc = index_url.openConnection();
                    is = yc.getInputStream();
                } else {
                    is = new FileInputStream(RelFile.makeFileName(RelFile.dictionaryDir,Kirrkirr.engIndexFile));
                }
                ObjectInputStream ois =
                    new ObjectInputStream(new BufferedInputStream(is));
                Thread.sleep(10);
                glossIndex = (Hashtable) ois.readObject();
                Thread.sleep(10);
                ois.close();
                if (Dbg.TIMING) {
                    time=System.currentTimeMillis()-time;
                    Dbg.print("loading gloss took "+time+" ms");
                }
                // now not used; we build it dynamically.
                // glossList = (Vector) glossIndex.remove(DictField.H_WORDS);
                parent.scrollPanel.mainwords=(Vector)glossIndex.remove(DictField.MAINWORDS);
                glossCache = new Hashtable();
                glossHitList = new Vector();
                if (Dbg.TIMING) {
                    time=System.currentTimeMillis();
                }
                // Object[] items=new Object[parent.scrollPanel.mainwords.size()];
                // parent.scrollPanel.mainwords.copyInto(items);
                glossList = createGlossList(parent.scrollPanel.mainwords);
                if (Dbg.TIMING) {
                    time=System.currentTimeMillis()-time;
                    Dbg.print("Creating gloss list took "+time+" ms");
                }
                parent.scrollPanel.setGlosses(glossList);
            } catch (Exception e) {
                if (Dbg.ERROR) e.printStackTrace();
                if (glossIndex == null) {
                    parent.disableGlossList();
                }
            } finally {
                glossLoader = null;
            }
        }



        private Vector<String> createGlossList(Vector items) {
            // Collections.sort(items, new KAlphaComparator());
            int itemsSize = items.size();
            Vector<String> v = new Vector<String>(itemsSize);
            for (int i = 0; i < itemsSize; i++) {
                String cur = (String) items.elementAt(i);
                GlossDictEntry ede = getGlossIndexEntry(cur);
                v.addElement(cur);
                if (ede != null) {
                    for (int j = 0, numSubwords = ede.numSubwords(); j < numSubwords; j++) {
                        v.addElement(ede.subwords[j]);
                    }
                }
            }
            v.trimToSize();
            return v;
       }

    } // end class EIndexLoader

} // end class DictionaryCache


class GlossCacheEntry {
    String xmlFile;
    String htmlFile;
    // GlossDictEntry ede;
    String[] headwords;

    GlossCacheEntry(int numheadwords){
        headwords=new String[numheadwords];
    }
}


/** A cache entry object. These should be immutable (but then can't
 *  externalize). Contain all the information about a headword -
 *  the links, glosses, domains, sounds, pictures, xmlfile name and html filename.
 */
class CacheEntry implements Externalizable {

    private static final long serialVersionUID = -753979224709453869L;

    DictEntry entry;
    DictFields links;
    Vector<DictField> gloss;
    Vector domains;
    DictFields sounds;
    DictFields pictures;
    String xmlFile;
    String htmlFile;

    //Madhu:'00 or else get a InvalidClassException while saving profiles
    //needed as class implements Externalizable
    public CacheEntry() {
    }

    public CacheEntry(DictEntry entry, DictFields links, Vector<DictField> gloss,
                Vector domains, DictFields sounds, DictFields pictures,
                String xmlFile, String htmlFile) {
        this.entry = entry;
        this.links = links;
        this.gloss = gloss;
        this.domains = domains;
        this.sounds = sounds;
        this.pictures = pictures;
        this.xmlFile = xmlFile;
        this.htmlFile = htmlFile;
    }

    /* ---- not used
    public CacheEntry(DictEntry entry, Vector links, Vector gloss,
                Vector domains, DictFields sounds, DictFields pictures,
                String xmlFile, String htmlFile, CacheEntry[] entries) {
        this.entry = entry;
        this.links = links;
        this.gloss = gloss;
        this.domains = domains;
        this.sounds = sounds;
        this.pictures = pictures;
        this.xmlFile = xmlFile;
        this.htmlFile = htmlFile;
    }
    ------------- */

    public String toString() {
        return "CacheEntry[" + entry+" xmlFile:"+xmlFile+
                " htmlFile:"+htmlFile +
                " links:"+links+" gloss:"+gloss+ " domains:"+domains+
                " sounds:"+sounds+" pictures:"+pictures+
                ']' + RelFile.lineSeparator();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(entry);
        out.writeObject(links);
        out.writeObject(gloss);
        out.writeObject(domains);
        out.writeObject(sounds);
        out.writeObject(pictures);
        out.writeObject(xmlFile);
        out.writeObject(htmlFile);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        entry = (DictEntry) in.readObject();
        links = (DictFields) in.readObject();
        gloss = (Vector) in.readObject();
        domains = (Vector) in.readObject();
        sounds = (DictFields) in.readObject();
        pictures = (DictFields) in.readObject();
        xmlFile = (String) in.readObject();
        htmlFile = (String) in.readObject();
    }

}

