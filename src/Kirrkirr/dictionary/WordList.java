package Kirrkirr.dictionary;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.data.KKListModel;
import Kirrkirr.util.*;
import Kirrkirr.ui.panel.ScrollPanel;
import Kirrkirr.ui.panel.SearchPanel;
import Kirrkirr.ui.panel.KirrkirrPanel;

import java.util.*;
import javax.swing.*;
import java.awt.Dimension;
import javax.swing.event.ListSelectionListener;

/** Decomposes shared functionality and structure between
 *  the gloss word list and the headword one. ScrollPanels
 *  have/manipulate WordLists. HeadwordList and
 *  GlossList extend WordList.
 *  @see ScrollPanel
 */
public abstract class WordList {

    protected Kirrkirr parent;

    /** String constants that need to be translated */
    private static final String SC_NOT_FOUND="Nothing_Found";
    private static final String SC_WORD_HIGHLIGHTED="word_highlighted";
    private static final String SC_WORDS_HIGHLIGHTED="words_highlighted";

    /** to set list width - Kirrkirr words are long =) */
    private static final String PROTOTYPE_CELL = "abcdefghijklmnopqrstuvwxyz";

    /** Words in Left Hand Side scrollPanel (model). This may not be all the
     *  words in the dictionary (list may have been filtered, or subwords
     *  turned off).
     */
    protected KKListModel<String> words;
    /** Swing component for list of words. */
    public JList jWords;
    /** list listener (so that we can remove and re-add when highlighting) */
    protected ListSelectionListener listener;
    /** Swing scroll pane to hold the list of words */
    protected JScrollPane sp_jWords;
    /** list of list attributes, indexed by static constants
     *  in ScrollPanel (SEE_FREQ, SEE_POS, ...). */
    protected boolean[] attributes;
    //i think menu options in kirrkirr should maybe be here too??
    protected ListCellRenderer renderer;

    /** Gives the name of which WordList this is for debug messages.
     */
    private final String debug;

    protected ScrollPanel scrollPanel;

    /** Sets up the data structures behind a word list
     *  and sets it to make subwords visible by default
     *  (and frequency and parts of speech not visible).
     */
    public WordList(ListCellRenderer renderer, Kirrkirr p, ScrollPanel sp,
                    int size, String implementationString) {
        parent = p;
        scrollPanel = sp;
        debug = implementationString;

        this.renderer = renderer;
        words = new KKListModel<String>();

        jWords = new JList(words);
        jWords.setCellRenderer(renderer);
        jWords.setPrototypeCellValue(PROTOTYPE_CELL);

        sp_jWords = new JScrollPane(jWords,
                                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp_jWords.setDoubleBuffered(true);
        sp_jWords.setMinimumSize(new Dimension(60, 100));
        // this next bit does successfully determine wordlist starting size
        if (size >= KirrkirrPanel.LARGE) {
          sp_jWords.setPreferredSize(new Dimension(180, 350));
        } else {
          sp_jWords.setPreferredSize(new Dimension(110, 350));
        }
        initAttributes();
    }

    public ListCellRenderer getCellRenderer() {
        return renderer;
    }

    /** If the <code>attributeType</code> is appropriate for this list,
     *  makes sure that it is set to <code>value</code>. If it
     *  is already set, do nothing. If it needs to be reset, call
     *  resetWordsGUI when done to refresh the list.
     */
    public void setAttribute(int attributeType, boolean value) {
        if (Dbg.LIST)
            Dbg.print(debug+"setting Attribute "+attributeType+" to "+value);
        if (attributes==null || attributeType>=attributes.length) {
            if (Dbg.LIST) Dbg.print("attribute invalid in set "+attributeType);
            return;
        }
        if (value!=attributes[attributeType]) {
            attributes[attributeType]=value;
            refreshWords(attributeType);
        }
    }

    public boolean getAttribute(int attributeType){
        if (attributes==null || attributeType>=attributes.length) {
            if (Dbg.LIST) Dbg.print("attribute invalid in get "+attributeType);
            return false;
        }
        return attributes[attributeType];
    }

    /** If <code>enabled</code> is true, makes this
     *  word list visible and adds it to the scroll panel,
     *  as well as adding <code>llistener</code> as a
     *  list listener. Otherwise, makes the word list not
     *  visible and not enabled, and removes <code>llistener</code>.
     *  Called by ScrollPanel.
     */
    public void setEnabled(boolean enabled,
                           ListSelectionListener llistener,
                           JPanel panel) {
        if (Dbg.LIST) Dbg.print(debug+"setenabled to "+enabled);
        jWords.setVisible(enabled);
        jWords.setEnabled(enabled);
        if (enabled){
            jWords.addListSelectionListener(llistener);
            listener=llistener;
            panel.add(sp_jWords);
        } else {
            jWords.removeListSelectionListener(llistener);
        }

    }

    /** Accessor for the list of words.
     */
    public KKListModel<String> getWords() {
        return words;
    }

    /** Public accessor/shortcut for elements of the word list.
     */
    public String wordAt(int index) {
        return (String)words.elementAt(index);
    }

    /** Returns size of currently showing word list.
     */
    public int size() {
        if (words==null) return 0;
        return words.size();
    }

    /** Returns word currently selected in the list.
     */
    public String getSelectedWord() {
        return (String)jWords.getSelectedValue();
    }

    /** Repaints the graphical representation of the list.
     */
    public void repaint() {
        jWords.repaint();
    }

    /** Adds the scrollPane of this list to the panel.
     */
    public void addToPanel(JPanel panel) {
        panel.add(sp_jWords);
    }

    /** Called from ValueChanged when the user clicks on a word
     *  in the word list.
     */
    public void wordClicked(Object source) {
        if (Dbg.LIST) Dbg.print(debug + "wordclicked");
        if (source!=jWords) return;
        int index=jWords.getSelectedIndex();
        if (index != -1) {
            String word=(String)words.elementAt(index);
            setWord(word);
        }
    }

    /** As a user types a word in, finds the word in the
     *  word list that is most similar, and scrolls to that
     *  word. Searches the whole list because list is not always
     *  in alphabetical order.
     *  @see #startMatch
     */
    public void scrollToWord(String word) {
        if (Dbg.LIST) Dbg.print(debug+"scrolltoword "+word);
        word = word.trim().toLowerCase();
        int closestMatch = 0;
        int prevlen = 0;

        // Warlpiri: Because the Headword dict is in its own quirky order (not
        // really alphabetical), it won't scroll as you type to match rd or ng
        // or ny beginning words -  This means we now search the whole word
        // list each time.
        for (int i = 0, sz = words.size(); i < sz; i++) {
            int len = startMatch(word, (String) words.elementAt(i));
            //the higher len is, the better a match it is.
            if (len > prevlen) {
                closestMatch = i;
                prevlen = len;
            }
        }

        if (prevlen > 0) {
            jWords.ensureIndexIsVisible(closestMatch);
            jWords.repaint();
        }
    }

    /** If element <= typed, returns number of initial matching letters,
     *  else if it starts with -, = return -1, else return -10.
     */
    private static int startMatch(String typed, String element) {
        // convert to char arrays
        int tl = typed.length();
        char[] tc = new char[tl];
        typed.getChars(0, tl, tc, 0);
        int el = element.length();
        char[] ec = new char[el];
        element.getChars(0, el, ec, 0);

        int n = Math.min(tl, el);
        int i = 0;
        while (n-- > 0) {
            char tc1 = tc[i];
            char ec1 = ec[i];
            if (tc1 == ec1) {
                i++;
            } else if (ec1 > tc1) {
                return -1;
            } else { // i.e. (ec1 < tc1)
                break;
            }
        }
        return i;
    }

    /** This is called from the SearchPanel. Given a vector
     *  of strings, highlights those strings in the word list,
     *  if they are there. NB: some words may not be in current
     *  list if the current list is reduced. (Case-sensitive).
     *  @param selections Vector of Strings to highlight in list.
     */
    public void highlightInWordList(Vector selections) {
        if (Dbg.LIST) Dbg.print(debug+"highlightinwordlist(vector) "+selections.size()+" "+selections);
        // NB: some words may not be in current words list if we have
        // searched over all words and current list is reduced
        int startSize = selections.size();
        int[] tempIndices = new int[startSize];
        int tempSize = 0;
        //looks for each element of selections in the words
        //and adds their indices to the tempIndices array.
        for (int i = 0; i < startSize; i++) {
            int k = words.indexOf(selections.elementAt(i));
            if (k >= 0) {
                tempIndices[tempSize++] = k;
            } else {
                if (Dbg.ERROR) {
                    Dbg.print(selections.elementAt(i)+" not found??");
                }
            }
        }
        int[] indices=new int[tempSize];
        System.arraycopy(tempIndices,0,indices,0,tempSize);
        jWords.removeListSelectionListener(listener);
        highlightInWordList(indices);
        jWords.addListSelectionListener(listener);
    }

    /** Highlight in list items at specified indices.
     *  NB: The ListSelectionListener should be removed before calling this!
     *  @param indices integer array of list positions to highlight.
     */
    public void highlightInWordList(int[] indices){
        jWords.setSelectedIndices(indices);
        if (indices.length > 0) {
            StringBuilder b = new StringBuilder(24);
            b.append(Integer.toString(indices.length));
            b.append(' ');
            if (indices.length == 1) {
                b.append(Helper.getTranslation(SC_WORD_HIGHLIGHTED));
            } else {
                b.append(Helper.getTranslation(SC_WORDS_HIGHLIGHTED));
            }

            parent.setStatusBar(b.toString());

            // Don't have selected right at beginning or end
            jWords.ensureIndexIsVisible(Math.max(indices[0] - 7, 0));
            jWords.ensureIndexIsVisible(Math.min(indices[0] + 7,
                                                   words.size()-1));
            jWords.ensureIndexIsVisible(indices[0]);

            //if num matches == 1 then set the current word too
            if (indices.length == 1) {
                if (Dbg.VERBOSE) Dbg.print("WordList: should click it!");
                wordClicked(jWords);
            }
        } else {
            parent.setStatusBar(Helper.getTranslation(SC_NOT_FOUND));
            // it would be nice to get it to beep here!
        }
    }

    /** Called from OneBoxPanel when user clicks "find" "filter" or
     *  presses RETURN in the search box. If filter, then filters the main
     *  list, otherwise highlights the words. Applies to all words
     *  which match word, via plain or fuzzy search.
     */
    public void updateSelection(String query, boolean filter) {
        if (Dbg.LIST) Dbg.print(debug+"updateSelection "+query+" "+filter);
        query = query.trim();
        // quote parentheses
        query = SearchPanel.checkForParentheses(query);

        jWords.removeListSelectionListener(listener);
        // try both regular exact match and fuzzy:
        // at the moment regex1 has exact full word match.
        Regex regex1 = OroRegex.newICaseRegex("^" + query + " *$");
        Regex regex2 = OroRegex.newICaseRegex("^" + getFuzzy(query));

        Vector selections1 = new Vector();
        Vector selections2 = new Vector();

        // if selections1 is non-empty, only matches there will be used,
        // so we can skip regex2.  We record whether it is non-empty in
        // a boolean for speed.
        // Else, try regex2 first, since regex1 can only match if it does
        boolean selections1Empty = true;
        for (int i = 0, sz = words.size(); i < sz ; i++) {
            String curword = words.elementAt(i);
            if (selections1Empty) {
                if (regex2.hasMatch(curword)) {
                    if (Dbg.VERBOSE) Dbg.print("Matched: "+regex2.toString()+" <with> "+words.elementAt(i));
                    Object obj = Integer.valueOf(i);
                    if (filter) {
                        obj = curword;
                    }
                    selections2.add(obj);
                    if (regex1.hasMatch(curword)) {
                        selections1.add(obj);
                        selections1Empty = false;
                    }
                }
            } else {
                if (regex1.hasMatch(curword)) {
                    if (Dbg.VERBOSE) Dbg.print("Matched: "+regex1.toString()+" <with> "+words.elementAt(i));
                    Object obj = Integer.valueOf(i);
                    if (filter) obj = curword;
                    selections1.add(obj);
                }
            }
        }

        if (selections1.isEmpty()) {
            selections1 = selections2;
        }
        if (Dbg.LIST) Dbg.print("Selections are: " + selections1);
        if ( ! filter) {
            int sz = selections1.size();
            int[] indices = new int[sz];
            for (int i=0 ; i < sz; i++) {
                indices[i] = ((Integer)selections1.get(i)).intValue();
            }
            highlightInWordList(indices);
        } else { // filter case
            resetWordsGUI(selections1);
            /* without fuzzy search:
              Regex regex = OroRegex.newRegex(query);
            String cur;
            for (int i=0 ; i < words.size() ; i++) {
                cur=(String)words.elementAt(i);
                if (! regex.hasMatch(cur)) {
                    // didn't match so remove it
                    removeWord(cur,i);
                    --i;
                }
            }
            */

            if ( ! words.isEmpty()) {
                jWords.setSelectedIndex(0);
            }
        }
        jWords.addListSelectionListener(listener);
    }


    /**
     */
    protected void removeWord(String word,int wordsIndex){
        words.removeElementAt(wordsIndex);
    }

    /** Reset the lefthandside words list to the contents of selections
     *  or the original list, if selections is null.  This wraps the
     *  basic resetWords() routine, and then prints a status
     *  bar message. <b>Only used when attributes remain the
     *  same and list contents should change - such as filtering.</b>
     *  For a change in SEE_SUB, SEE_FREQ, or sorting, call refreshWords.
     *  (refreshWords calls resetWords when necessary).
     *    @param selections Only include these words in list. If null,
     *    reset list to original list.
     */
    public void resetWordsGUI(Vector selections) {
        if (Dbg.LIST) Dbg.print(debug+"resetwordsgui" + (selections==null?"(null)":"(non-null)"));
        resetWords(selections);
        int newsize = size();
        //This is an instance of a hard sentence to translate
        //(due to the numbers). There is some support for it
        //in the java internationalization stuff, but it is
        //somewhat complicated.
        StringBuilder b = new StringBuilder(Integer.toString(newsize));
        b.append(" word");
        if(newsize != 1) {
            b.append("s");
        }
        b.append(" in list");

        parent.setStatusBar(b.toString());
        // don't do.  jWords.setSelectedIndex(0); Was wrong. Unneeded?
        if (newsize == 1) {
            String it = (String) words.firstElement();
            setWord(it);
        }
    }

    /** Reset the words lefthandside list of words.
     *  If selections is null, it resets the list to
     *  all words in the wordList. Depending on the current state
     *  of the boolean for seeing subwords, may or may
     *  not show subwords.
     *
     *  @param selections If selections is non-null, only insert the words
     *         from the wordList that are in selections
     */
    public void resetWords(String[] selections) {
        if (Dbg.LIST) Dbg.print(debug+"resetwords(object[])");
        Vector<String> newone=new Vector<String>(selections.length);
        for (String selection : selections) {
            newone.addElement(selection);
        }
        resetWords(newone);
    }

    /** Reset the words lefthandside list of words to holding
     *  either all words in selections or all the words in wordList.
     *  @param selections If selections is non-null, only insert the words
     *         from the wordList that are in selections
     */
    public void resetWords(Vector<String> selections) {
        if (Dbg.LIST) Dbg.print(debug+"resetwords(vector)");
        // tip from Java Platform Performance: it's better to trash the
        // DefaultListModel and start a new one than to manipulate it,
        // or else there are MVC internal events for all changes.
        // If doing this, don't need to removeListSelectionListener
        if (selections == null || selections.isEmpty()) {
            selections = getFullWordList();
            if (Dbg.LIST)
                Dbg.print(debug+"reset to " + selections.size() + " words");
        }
        KKListModel<String> newWords = new KKListModel<String>(selections);
        newWords.trimToSize();
        jWords.setModel(newWords);
        words = newWords;
    }

    //--- abstract functions

    /** Refreshes words in the current list when a list
     *  attribute changes. Unlike reset, this *tries* to
     *  just change the view of the list (ie, so you see
     *  the frequency or part of speech, etc).
     *  <p>
     *  However, this *may* end up
     *  resetting the list, and losing any filtering. For
     *  example, if you filter the list, hide subwords,
     *  then show subwords, the list must be reset.
     *  Other options: 1) give a warning dialog, 2) blank out
     *  see subwords option when list is filtered, 3) somehow
     *  keep track of which subwords are attached to which
     *  mainwords (more overhead - like gloss).
     */
    public abstract void refreshWords(int attributeChanged);

    /** Set the default size and value of the attributes.
     *  Subclasses MUST have a SEE_SUB attribute.
     */
    protected abstract void initAttributes();

    /** Get a regex for the fuzzy spelling of the word.
     *  @see SearchPanel#headwordFuzzy(String)
     *  @see SearchPanel#glossFuzzy(String)
     */
    public abstract String getFuzzy(String word);

    /** Return the whole list of words that Kirrkirr started
     *  with (either the gloss gloss list or the headword
     *  headwords).
     */
    public abstract Vector<String> getFullWordList();

    /** Calls parent.setCurrentWord. (Differs for each list).
     */
    protected abstract void setWord(String word);

}
