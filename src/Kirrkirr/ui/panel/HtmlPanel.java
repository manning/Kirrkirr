package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.GlossDictEntry;
import Kirrkirr.ui.HtmlListener;
import Kirrkirr.ui.panel.optionPanel.KirrkirrOptionPanel;
import Kirrkirr.ui.panel.optionPanel.XslOptionPanel;
import Kirrkirr.ui.dialog.PictureDialog;
import Kirrkirr.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.text.html.HTMLDocument;

import java.applet.*;

import java.awt.*;
import java.net.*;
import java.io.*;

/**
 * HtmlPanel is the class which displays the definitions of the words. The HTML
 * content is generated from the xml dictionary file, and the format is
 * generated from the xsl file. HtmlPanel uses the JEditorPane to display
 * html definitions, including hyperlinks to other html files.
 *
 * Adapted from the HtmlPanel.java class from the SwingSet example in the
 * Swing 1.03 release
 * Copyright (c) 1997 Sun Microsystems, Inc. All Rights Reserved.

 * @author Kevin Jansz
 * @author Christopher Manning
 */
public class HtmlPanel extends KirrkirrPanel implements HyperlinkListener {

    /* String constants that need to be translated for HtmlPanel.java
     */
    public  static final String SC_HTML_TITLE = "Definition";
    private static final String SC_HTML_NAME  = "Definition_Panel";
    private static final String SC_HTML_DESC  = "Panel_for_viewing_definition_text_ HTML_documents_and_following_their_links";
    private static final String SC_HTML_ROLLOVER = "Displays_formatted_dictionary_entries";

    /** Pane which shows the html */
    private final JEditorPane htmlPane;
    /** Html parent so we can repaint everytime the page changes (unnecessary?) */
    private Container htmlparent;

    /** Whether or not to update the page (used by gloss scroll list).
        Static so that both htmlpanels are frozen
        (see freeze and unfreeze methods). */
    private static boolean frozen; // = false;

    // these next 2 vars always set on object creation
    // -- should be "blank final" but JDK1.2.2 has problems with them
    /** Where the html files are (mirrors Kirrkirr.htmlDirectory */
    private final String htmlDirectory;
    /** Listener to the html link clicks (Kirrkirr) */
    private final HtmlListener listener;

    // /** The current url being displayed by the html pane */
    // private URL currentURL;

    private String curPword; // = null;

    /** What to show when the word is not found */
    private static final String blankhtml = "blank.html";
    /** Before opening any html file */
    private static final String starthtml = "start.html";

    private static final Dimension minimumSize = new Dimension(300, 100);

    /**
     * Initializes a new Html Panel and loads the default start page.
     * @param kparent The parent Kirrkirr
     * @param listener the HtmlListener (Kirrkirr)
     * @param folder the directory where the cached html files are
     */
    public HtmlPanel(Kirrkirr kparent, HtmlListener listener,
                      String folder) {
        this(kparent, listener, folder, null);
    }

    /**
     * Initializes a new Html Panel and loads the page with
     * word (hnum), or the default start page if word is null.
     * @param kparent the Kirrkirr parent
     * @param listener the HtmlListener (Kirrkirr)
     * @param folder the directory where the cached html files are
     * @param uniqueKey The word to load at startup, or null to load
     *             the default startfile (starthtml)
     */
    public HtmlPanel(Kirrkirr kparent, HtmlListener listener,
                      String folder, String uniqueKey) {
        super(kparent);
        this.listener = listener;
        setName(Helper.getTranslation(SC_HTML_TITLE));
        htmlDirectory = folder;
        htmlPane = new JEditorPane();
        init(uniqueKey);
    }

    /**
     * Opens new page, either the page for word(hnum) or the default
     * start page.
     * @param uniqueKey The word to be loaded at startup, or null to load
     *             the default start page
     */
    private void init (String uniqueKey) {
        setBackground(Color.white);
        setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleName(Helper.getTranslation(SC_HTML_NAME));
        getAccessibleContext().setAccessibleDescription(Helper.getTranslation(SC_HTML_DESC));

        JScrollPane scroller = new JScrollPane();
        scroller.setBackground(Color.white);
        scroller.setForeground(Color.white);
        scroller.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));
        /* Scroller's viewport so we can control scrolling */
        JViewport viewport = scroller.getViewport();
        viewport.add(htmlPane);
        viewport.setBackground(Color.white);
        viewport.setForeground(Color.white);
        htmlparent=htmlPane.getParent();
        add(scroller, BorderLayout.CENTER);

        htmlPane.setBackground(Color.white);
        htmlPane.setForeground(Color.white);
        htmlPane.setEditable(false);
        htmlPane.addHyperlinkListener(this);

        // for the following to work, we would need JDK 1.5
        // htmlPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        // htmlPane.setFont(FontProvider.TEXT_FONT);
        // this next one doesn't seem to work....
        // StyleSheet myStyleSheet = new StyleSheet();
        // myStyleSheet.addRule("body {font: \"" + FontProvider.TEXT_FONT_NAME + "\" }");
        // myStyleSheet.addRule("h1 {font: \"" + FontProvider.TEXT_FONT_NAME + "\" }");
        // HTMLEditorKit htmlKit = (HTMLEditorKit)(htmlPane.getEditorKitForContentType("text/html"));
        // htmlKit.setStyleSheet(myStyleSheet);
        // htmlPane.setEditorKit(htmlKit);

        if (Dbg.HTML_PANEL) Dbg.print("HtmlPanel.init: about to load first Html page");

        setCurrentWord(uniqueKey, true);
    }

    @Override
    public Dimension getMinimumSize() {
        return minimumSize;
    }

    @Override
    public Dimension getPreferredSize() {
        return minimumSize;
    }

    /**
     * This is a workaround for the Gloss words. Since each
     * gloss word may have more than one headword word, calling
     * setCurrentWord on one Gloss word will set the html panel
     * multiple times. Instead, "freeze" the htmlpanel while
     * calling setcurrentword for the headword words, then "unfreeze"
     * it and display the gloss html (which lists all of the headword
     * equivalents). Static so it freezes both HtmlPanels.
     */
    public static void freeze(){ frozen=true;}

    /**
     * This is a workaround for the Gloss words. Since each
     * gloss word may have more than one headword word, calling
     * setCurrentWord on one Gloss word will set the html panel
     * multiple times. Instead, "freeze" the htmlpanel while
     * calling setcurrentword for the headword words, then "unfreeze"
     * it and display the gloss html (which lists all of the headword
     * equivalents). Static so it freezes both HtmlPanels.
     */
    public static void unfreeze(){ frozen=false;}

    /**
     * For tooltips.
     * @return a description of the panel for tooltip rollovers
     */
    @Override
    public String getTabRollover() {
        return Helper.getTranslation(SC_HTML_ROLLOVER);
    }

    /**
     * Called when the Xsl style sheet is changed - all Html files are regenerated.
     * This is called by formatChanged() in Kirrkirr, which is called by
     * XslUpdated in DictionaryCache, which has already done the cache update.
     * So it doesn't need to call refreshCache.
     * # Because of an "optimisation" in the JEditorPane source code it will not read the document
     *   if it's the one currently being displayed.
     * # There is a bug in JEditorPane, if you open 2 files too close to eachother, it
     *   will paste the first document about 5 times above the second document.
     *   Hence, the use of the empty document - the pasting still occurs, but you cant see it.
     */
    public void refreshFormat()
    {
        /* Changed this as it was causing strange (untraceable)
           null pointer exceptions in the swing event queue (only
           for jdk1.3). Instead, reset document in pageloader (see below).

           URL empty = RelFile.makeURL(htmlDirectory, emptyhtml);
           Dbg.print("set to empty");
           linkActivated(null,empty, 0);*/
        if (curPword != null) { // can be null at beginning
            setCurrentWord(curPword);
        }
    }

    /** Called by Kirrkirr when JComponents (History, ScrollPanel,
     *  KirrkirrPanels)
     *  update the current word. Refreshes the cache and updates the currently
     *  displayed word.
     *  Note: Want:
     *  don't display word for an HTML panel other than oneself (so can
     *  have two distinct html displays). Otherwise show it.
     *  But can't easily currently do this as html event signalling is in
     *  Kirrkirr, and so doesn't distinguish frames...
     *  @see Kirrkirr#setCurrentWord(String,boolean,JComponent,int,int)
     */
    @Override
    public void setCurrentWord(String uniqueKey, boolean gloss,
                               final JComponent signaller, final int signallerType,
                               final int arg) {
        if (Dbg.HTML_PANEL) Dbg.print("HtmlPanel.setCurrentWord(" + uniqueKey +
                ',' + gloss + ") frozen is " + frozen);
        // if ( ! (signallerType == parent.HTML && signaller != this)) {
        if (!frozen) {
            boolean gotHtml = false;
            GlossDictEntry ede = null;
            if ( ! gloss)
                gotHtml = parent.cache.ensureHtmlAvailable(uniqueKey);
            //this is a bad hack... if a user clicks on a headword
            //link from an gloss page, we need the headword word.
            //so if gloss index entry not found, look for headword one
            //(and vice versa)
            if (gloss || ! gotHtml) { //ce == null means headword failed
                ede = parent.cache.getGlossIndexEntry(uniqueKey);
                // Chris: I think this unnecessarily created a lot of work!
                // we can assume that Gloss html creation has been done...
                // if (ede!=null)
                //    ce=parent.cache.refreshCache(uniqueKey, ede);
                // else
                //    //gloss wasnt there, try headword
                //    ce=parent.cache.refreshCache(uniqueKey, true);
                if (ede == null)
                    // gloss wasnt there, try headword
                    gotHtml = parent.cache.ensureHtmlAvailable(uniqueKey);
            }
            if ( ! gotHtml && ede == null) {
                // both still null; possibly gloss head word with no entry
                if (Dbg.ERROR) Dbg.print("HtmlPanel.setCurrentWord: warning, couldn't find word "+uniqueKey);
                return;
            }
            setCurrentWord(uniqueKey);
        }
    }

    public void setCurrentWord(String uniqueKey) {
        setCurrentWord(uniqueKey, false);
    }
    /**
     * Displays the html file for the word chosen in the html panel.
     * If word is a headword word, goes to the cached html file.
     * If it is a help file, goes to the html help file. In case of
     * error, displays blank.
     */
    public void setCurrentWord(String uniqueKey, boolean defaultToStart) {
        if (Dbg.HTML_PANEL) Dbg.print("HTML.setCurrentWord(" + uniqueKey + ")");
        URL url = null;

        String strfile = null;
        if (uniqueKey != null) {
            strfile = Helper.wordToCacheFilename(uniqueKey);
        }

        if (strfile != null) {
            if (uniqueKey.endsWith(Kirrkirr.HELP_FILE_END) ) {
                url = RelFile.makeURL(htmlDirectory, strfile);
            } else {
                url = RelFile.MakeWriteURL(htmlDirectory, strfile);
            }
        } else {
            if (defaultToStart) {
                url = RelFile.makeURL(htmlDirectory, starthtml);
            } else {
                if (Dbg.ERROR)
                    Dbg.print("HtmlPanel:setCurrentWord(String): Couldn't make filename! url: "+url+", word: "+uniqueKey);
                url = RelFile.makeURL(htmlDirectory, blankhtml);
            }
        }
        linkActivated(uniqueKey, url, 0);
    }


    /** Follows the reference in a
     *  link.  The given url is the requested reference.
     *  By default this calls <a href="#setPage">setPage</a>,
     *  and if an exception is thrown the original previous
     *  document is restored and a beep sounded.  If an
     *  attempt was made to follow a link, but it represented
     *  a malformed url, this method will be called with a
     *  null argument. If the same page as is there already is
     *  activated, it just scrolls up. Before it loads the page,
     *  the cursor is changed to a waiting cursor, then the PageLoader
     *  class is used to load the page and then return the cursor
     *  to normal.
     *  @param word A uniqueKey
     *  @param url the URL to follow
     *  @param height the height of the viewport (where to scroll to)
     */
    public void linkActivated(String word, URL url, int height) {
        if (Dbg.HTML_PANEL) Dbg.print("linkActivated("+word+" "+url+" "+height);
        // cdm 2009: I'm not sure why this code was here, but it won't work with new init/linkActivated merger
        // if (currentURL == null) {
        //    PageLoader pageloader = new PageLoader();
        //     try {
        //         SwingUtilities.invokeLater(pageloader);
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        //     return;
        // }
        // currentURL = url;
        if (word != null) {
            curPword = word;
        }
        Cursor c = htmlPane.getCursor();
        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        htmlPane.setCursor(waitCursor);

        PageLoader pageloader = new PageLoader(url, c, height);

        try {
            SwingUtilities.invokeLater(pageloader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Returns an XslChoicePanel. Overrides KirrkirrPanel.
     */
    @Override
    public KirrkirrOptionPanel getOptionPanel () {
        return(new XslOptionPanel(parent));
    }


    /** Copies the selected text in the html panel to the system
     *  clipboard and returns the length of the selection. If no
     *  text is selected, does nothing and returns 0.
     *  Overrides KirrkirrPanel.
     *  At present this does a copy even if you ask for a cut.  Should beep?
     *  @param isCut true if this should be a cut operation
     *  @return how many characters were copy (0 if no selection)
     */
    public int copyText(boolean isCut) {
        String selected = htmlPane.getSelectedText();
        if (Dbg.CUTPASTE) {
            if (selected != null)
                Dbg.print("HtmlPanel cut/copy; selected is " +
                          selected.length() + " chars\n  " + selected);
            else
                Dbg.print("HtmlPanel cut/copy; selected is null");
        }
        if (selected != null) {
            htmlPane.copy();
            return(selected.length());
        } else {
            return(0);
        }
    }


    /** Notification of a change relative to a
     *  hyperlink, for example when a link is selected and entered.
     *  Called by the JEditorPane. When notified that a link
     *  has been activated, calls listener (ie the Kirrkirr parent)
     *  with wordClicked, which calls setCurrentWord and updates
     *  all the panels.
     */
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            URL url = e.getURL();
            if (Dbg.HTML_PANEL) Dbg.print("html link clicked: " + url);
            // int index;
            String urlString = url.toString();
            if (urlString.startsWith("http://") || urlString.startsWith("https://")) {
                // quote spaces in URL.  Really this should do proper URL encoding....
                Regex reg = new OroRegex(" ", "%20");
                urlString = reg.doReplace(urlString);
                BrowserLaunch.openURL(urlString);
                return;
            }

            String extension = KirrkirrFileFilter.getExtension(urlString);
            if (urlString.indexOf('#') != -1) {
                // when the link is a relative link on the same page
                // ie, for gloss html pages
                //urlString=urlString.substring(index+1,urlString.length());
                //htmlPane.scrollToReference(urlString);
                try {
                    setHtmlPage(url);
                } catch (Exception we) {
                    we.printStackTrace();
                }
            } else if (KirrkirrFileFilter.isAudioExtension(extension)) {
                playAudio((new File(url.getFile())).getName());
            } else if (KirrkirrFileFilter.isImageExtension(extension)) {
                openPicture((new File(url.getFile())).getName());
            } else {
                //when the link is a new word
                //!!!First encode (links in plain text, not encoded, while
                //filenames are encoded using URLHelper).  Then get the word.
                //urlString=URLHelper.encode(RelFile.getFile(url.toString()));
                // ABOVE APPEARS WRONG!  Links (e.g. with space) are encoded!
                String fileString = (new File(url.getFile())).getName();
                if (Dbg.HTML_PANEL) Dbg.print("HTML: fileString is " + fileString);
                String uniqueKey = Helper.cacheFilenameToUniqueKey(fileString);
                if (Dbg.HTML_PANEL) Dbg.print("HTML: uniqueKey is " + uniqueKey);
                if (uniqueKey != null) {
                    curPword = uniqueKey;
                    if (Dbg.HTML_PANEL) Dbg.print("word clicked: |" + curPword + "|");
                    if (parent != null) {
                        listener.wordClicked(curPword, this);
                    }
                }
            }
        }
    }


    private void openPicture(String filename) {
        new PictureDialog(parent, filename, curPword);
    }


    private static void playAudio(String filename) {
        if (Dbg.HTML_PANEL) Dbg.print("clicked on audio file: "+filename);
        if (RelFile.canMakeAudioClip()) {
            AudioClip currentClip = RelFile.makeAudioClip(filename, true);
            if (Dbg.HTML_PANEL) Dbg.print("trying to play "+currentClip+" "+filename);
            try { Thread.sleep(200); } catch (Exception ee) {
                // just do nothing
            }
            currentClip.play();
        } else if (Dbg.ERROR)
            Dbg.print("Couldn't make audio for "+filename);
    }


    /** Set htmlPane to a given URL, and set the default font of that URL */
    private void setHtmlPage(URL url) throws IOException {
        htmlPane.setPage(url);
        try {
            // add a CSS rule to force body tags to use the correct font
            // instead of the value in javax.swing.text.html.default.css
            // You need to do this because CSS overrides the setFont method
            HTMLDocument document = (HTMLDocument) htmlPane.getDocument();
            Font htmlFont = FontProvider.HTML_FONT;
            String bodyRule = "body { font-family: " + htmlFont.getFamily() + "; " +
                "font-size: " + htmlFont.getSize() + "pt; }";
            document.getStyleSheet().addRule(bodyRule);
        } catch (ClassCastException cce) {
            if (Dbg.ERROR) {
                Dbg.print("HTMLPanel.setHtmlPage: JEditorPane didn't use HTMLDocument editor kit");
                cce.printStackTrace();
            }
        }
    }


    /** Class that loads the page synchronously (although
     *  later than the request so that a cursor change
     *  can be done). Called after the cursor changes to a wait cursor,
     *  then loads the page, then is called again to change the cursor
     *   back to a normal cursor.
     */
    class PageLoader implements Runnable {

        private URL url;
        private final Cursor cursor;

        PageLoader(URL u, Cursor c, int h) {
            url = u;
            cursor = c;
        }

        public void start() {
            run();
        }

        @Override
        public synchronized void run() {
            if (url == null) {
                // restore the original cursor
                // cdm: I changed this as it seemed like cursor could
                // sometimes be null, though I'm not sure why...
                if (cursor != null && cursor.getType() != Cursor.WAIT_CURSOR) {
                    htmlPane.setCursor(cursor);
                } else {
                    htmlPane.setCursor(Cursor.getDefaultCursor());
                }
                //Container parent = htmlPane.getParent();
            } else {

                Document doc = htmlPane.getDocument();

                // do this so that if we are loading the same document again,
                // it will re-read it from file. See java bug 4412125
                // and the workaround described there.
                doc.putProperty(Document.StreamDescriptionProperty, null);
                try {
                    try {
                        //nastiness: since JEditorPane takes url for
                        //setpage(), will automatically decode UTF-8
                        //format.  So we strip the relative filename (ex:
                        //for /html/jaala.html we would select jaala.html) and
                        //encode that again (2 encodings total), and then stick
                        //it back on to create a new URL.  This way, when
                        //setPage decodes, it will still find the singly
                        //encoded file within the fs and all will be well.
                        File f = new File(url.getFile());
                        url = new URL("file", "", f.getParent() + File.separator +
                                      URLHelper.encode(f.getName()));
                    } catch (Exception e) {
                        if (Dbg.ERROR) Dbg.print(e.toString());
                    }
                    setHtmlPage(url);

                    //vp.setViewPosition(new Point(0, height));
                } catch (NullPointerException npe) {
                    if (Dbg.ERROR) {
                        Dbg.print("Broken HTML load: Url was: |" + url + '|');
                        npe.printStackTrace();
                    }
                    try {
                        Thread.sleep(10);
                        htmlPane.setPage(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (Dbg.ERROR) Dbg.print("HTML reload also broke");
                    }
                } catch (IOException ioe) {
                    try {
                        setHtmlPage(RelFile.makeURL(htmlDirectory, blankhtml));
                        Dbg.print(ioe.toString());
                    } catch (IOException ioexc){
                        htmlPane.setDocument(doc);
                        getToolkit().beep();
                    } catch (Exception m) {
                        if (Dbg.ERROR)
                            m.printStackTrace();
                    }
                } catch (Exception n) {
                    if (Dbg.ERROR)
                        n.printStackTrace();
                } finally {
                    // schedule the cursor to revert after
                    // the paint has happened.
                    url = null;
                    SwingUtilities.invokeLater(this);
                }
            } //end of else
            htmlparent.repaint();
        } //end of run

     } //end class PageLoader

 }
