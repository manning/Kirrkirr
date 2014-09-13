package Kirrkirr.ui.panel;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.HeadwordList;
import Kirrkirr.dictionary.GlossList;
import Kirrkirr.dictionary.WordList;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.RelFile;
import Kirrkirr.util.FontProvider;
import Kirrkirr.ui.data.KKListModel;
import Kirrkirr.ui.HWCellRenderer;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.ui.GlossCellRenderer;
import Kirrkirr.util.KAlphaComparator;

/** This class is stuff that used to be in clickText.java, but which now
 *  controls in one module everything related to the two scrollPanels
 *  (English and Warlpiri), and also controls the WordList toolbar.
 *  [Chris: I've removed all the setSelectionMode stuff.  I couldn't see
 *  what it's doing that's useful.]
 *  Scroll Panel controls the GUI; WordList contains the data, and the
 *  functionality related to the individual lists.
 */
public class ScrollPanel implements ListSelectionListener, ActionListener {

    private Kirrkirr parent;

    private JPanel scrollPanel;

    public KirrkirrButton switchListButton;
    public boolean headwordsShowing; // false if glosses showing

    private static final String SC_LOADING = "Loading_English";
    private static final String SC_DONE = "Done_Loading_English_Dictionary";
    private static final String SC_LANGCHANGE = "Switch_word_list_language";
    private static final String SC_NOT_IN_LIST="not_found_in_list";
    private static final String SC_SUB_NOT_IN_LIST="not_found_in_list_(a_subentry)";

    public static final int SEE_SUB=0, SEE_FREQ=1, SEE_POS=3, SEE_DIALECT=2,
        SEE_POLY=4, ENGLISH_ATTRIBUTES=2, WARLPIRI_ATTRIBUTES=5;

    private HeadwordList headwordList;
    private GlossList glossList;
    private WordList currentList;

    public Vector mainwords;

    //------ functions for whole scroll list ------
    public ScrollPanel(Kirrkirr cparent, int size) {
        parent = cparent;
        headwordsShowing = true;

        headwordList = new HeadwordList(new HWCellRenderer(parent), parent, this, size);
        glossList = new GlossList(new GlossCellRenderer(parent),parent,this, size);
        currentList = headwordList;

        switchListButton = new KirrkirrButton(SC_LOADING, this);
        switchListButton.setFont(FontProvider.PROMINENT_INTERFACE_FONT);
        switchListButton.setEnabled(false);
        switchListButton.setToolTipText(Helper.getTranslation(SC_LANGCHANGE));

        scrollPanel=new JPanel();
        scrollPanel.setLayout(new BoxLayout(scrollPanel,BoxLayout.Y_AXIS));
        headwordList.addToPanel(scrollPanel);
    }

    public void actionPerformed(ActionEvent e){
        if (e.getSource() == switchListButton)
            switchLists();
    }

    public void disableGlossList() {
        headwordsShowing=true;
        glossList=null;
        scrollPanel.revalidate();
    }

    public void switchLists() {
        headwordsShowing = ! headwordsShowing;
        scrollPanel.removeAll();
        if (headwordsShowing) {
            currentList=headwordList;
        } else {
            currentList=glossList;
        }
        headwordList.setEnabled(headwordsShowing,this,scrollPanel);
        glossList.setEnabled(!headwordsShowing,this,scrollPanel);

        if (headwordsShowing) {
            // switch to headwords
            parent.enableHeadwordList();
        } else {
            //switch to glosses
            parent.enableGlossList();
        }
        setScrollListIcons();
        scrollPanel.repaint();
        //necessary for some reason to get english to show up
        scrollPanel.revalidate();
    }

    public void repaint(){
        //is this neccessary?
        currentList.repaint();
        scrollPanel.repaint();
    }

    /** Called from DictionaryCache at startup (only) to set up the
     *  glosses list.
     */
    public void setGlosses(final Vector v) {
        glossList.setGlosses(v);
        setScrollListIcons();
        switchListButton.setEnabled(true);
        parent.setStatusBar(Helper.getTranslation(SC_DONE));
    }


    /** Change the icons and text in the menu bar and in the bottom
     *  status bar to reflect the current language of the ScrollPanel.
     */
    private void setScrollListIcons() {
        // cw '02: generalize a little bit.  for the icons, could pass
        // makeImageIcon (pic, true) and make the flags dictionary specific...
        // could make dictLang and glossLang member vars., but would need
        // to update them if changing dictionaries on the fly were
        // implemented...
        String dictLang = Kirrkirr.dictInfo.getDictLangName();
        String glossLang = Kirrkirr.dictInfo.getGlossLangName();
        String dictIcon = Kirrkirr.dictInfo.getDictLangIcon();
        String glossIcon = Kirrkirr.dictInfo.getGlossLangIcon();
        if (headwordsShowing) {
            String inGloss = "In_" + glossLang;
            parent.setStatusBarIconText(makeIconText(dictLang, glossLang));
            parent.setStatusBarIcon(dictIcon);
            switchListButton.setIcon(RelFile.makeImageIcon(glossIcon, false));
            switchListButton.setText(Helper.getTranslation(inGloss));
            //switchListButton.setText(Helper.getTranslation(SC_ENGLISH));
        } else {
            String inDict = "In_" + dictLang;
            switchListButton.setText(Helper.getTranslation(inDict));
            //switchListButton.setText(Helper.getTranslation(SC_WARLPIRI));
            switchListButton.setIcon(RelFile.makeImageIcon(dictIcon, false));
            parent.setStatusBarIconText(makeIconText(glossLang, dictLang));
            parent.setStatusBarIcon(glossIcon);
        }
    }


    private static String makeIconText(String lang1, String lang2) {
        StringBuilder sb = new StringBuilder(lang1);
        return sb.append('-').append(lang2).append(' ').toString();
    }


    public JPanel getPanel(){
        return scrollPanel;
    }

    public void init(){
        // this innocuous, but unnecessary looking line solves a
        // weird Swing bug, where it will generate every list cell
        // if you ask to ensureIndexIsVisible and nothing has been
        // previously selected
        headwordList.jWords.setSelectedIndex(0);
        headwordList.setEnabled(true,this,scrollPanel);
    }


   /** Sets the word in the scroll list box.
    *  Called (only) from Kirrkirr.setCurrentWord
    *  @param tailWord  the current word with space-padded hnum
    *  @param english   true if we are dealing with English
    */
    public void setWordInScrollPanel(String tailWord, boolean english,
                         final JComponent signaller, final int signallerType,
                         final int arg)
    {
        if (Dbg.K) Dbg.print("setWordInScrollPanel |" + tailWord +
                               "| english: " + english + " headwordsShowing " +
                               headwordsShowing);
        // I think the right thing is to do nothing unless headwords are
        // showing.  Doing that seems okay.  CDM.
        if ( ! headwordsShowing) {
            return;
        }

        // We don't have to do anything if a scrollPanel valueChanged caused
        // the event, but we should do it if we are handling a
        // highlightInWordList event from the SearchPanel.
        // We distinguish these two cases by arg = 1 and 0 respectively.
        if (signallerType == Kirrkirr.SCROLLPANEL && arg == 1) {
            return;
        }

        int index = headwordList.getWords().indexOf(tailWord);
        if (Dbg.TWO) Dbg.print("index is " + index);

        if (index >= 0) {
            if (Dbg.TWO) Dbg.print("removed listSelListener");
            headwordList.jWords.removeListSelectionListener(this);
            if ( ! headwordList.jWords.isSelectedIndex(index))
                headwordList.jWords.setSelectedIndex(index);
            if (Dbg.TWO) Dbg.print("setSelectedIndex to " + index);
            headwordList.jWords.ensureIndexIsVisible(index);
            headwordList.jWords.addListSelectionListener(this);
            if (Dbg.TWO)
                Dbg.print("Made it visible, and added ListSelectionListener");
        } else {
            String word = Helper.getWord(tailWord);
            if (Dbg.VERBOSE) Dbg.print("setWordInScrollPanel: " + word +
                                       " not found in list");
            if (parent.cache.isSubWord(tailWord) &&
                ( ! parent.seeSubwords())) {
                parent.setStatusBar(word + ' ' +
                                    Helper.getTranslation(SC_SUB_NOT_IN_LIST));
            } else {
                parent.setStatusBar(word + ' ' +
                                    Helper.getTranslation(SC_NOT_IN_LIST));
            }
        }
        if (Dbg.TWO) Dbg.print("Done setWordInScrollPanel " + tailWord);
    }

    /** ScrollPanel watcher: Called when the user clicks on a word in the
     *  headword list. Implements the ListSelectionListener interface.
     *  If they select multiple words, we just use the first one (that is
     *  getSelectedIndex() returns).
     *  @param e The selection event
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if ( ! (e.getValueIsAdjusting())) {
            Object obj = e.getSource();
            currentList.wordClicked(obj);
            // nice, but only works in jdk1.3
            // EventListener[] evts = jHeadWords.getListeners(ListSelectionListener.class);
            // Dbg.print("jHeadWords listeners: num = " + evts.length);
            // for (int i = 0; i < evts.length; i++) {
            //      Dbg.print("  " + evts[i]);
            // }
        }
    }

    //list specific calls
    public int headwordsSize(){
        return headwordList.size();
    }

    public int glossesSize(){
        return glossList.size();
    }

    public String glossAt(int index){
        return glossList.wordAt(index);
    }

    public String headwordAt(int index){
        return headwordList.wordAt(index);
    }

    public boolean getHeadwordAttribute(int attributeType){
        return headwordList.getAttribute(attributeType);
    }

    public boolean getGlossAttribute(int attributeType){
        return glossList.getAttribute(attributeType);
    }

    //calls to the currentList... (better way to do this?)
    public void scrollToWord(String word){
        currentList.scrollToWord(word);
    }

    public ListCellRenderer getCellRenderer(){
        return currentList.getCellRenderer();
    }

    public int size(){
        return currentList.size();
    }

    public KKListModel getWords(){
        return currentList.getWords();
    }

    public String getSelectedWord(){
        return currentList.getSelectedWord();
    }

    public void resetWordsGUI(Vector selections) {
        currentList.resetWordsGUI(selections);
    }

    public void resetWords(Vector selections){
        currentList.resetWords(selections);
    }

    public void setAttribute(int attributeType, boolean value) {
        currentList.setAttribute(attributeType,value);
        refreshWords(attributeType);
    }

    public boolean getAttribute(int attributeType){
        return currentList.getAttribute(attributeType);
    }

    public void refreshWords(int attributeType){
        currentList.refreshWords(attributeType);
    }

    public void updateSelection(String query, boolean filter){
        currentList.updateSelection(query, filter);
    }

    public void highlightInWordList(Vector selections){
        currentList.highlightInWordList(selections);
    }

    public void highlightInWordList(int[] selections){
        currentList.highlightInWordList(selections);
    }



    //---------- sorting and comparison  --------
    public void alphaSort()
    {
        if (headwordsShowing){
            String[] items = headwordList.getWords().toArray(new String[headwordList.size()]);
            Arrays.sort(items, new KAlphaComparator());
            headwordList.resetWords(items);
        } else {
            String[] items;

            if (!glossList.getAttribute(SEE_SUB)){
                items = new String[glossList.size()];
                glossList.getWords().copyInto(items);
                Arrays.sort(items, new KAlphaComparator());
                // System.err.println(items);
            } else if (glossList.size()==parent.cache.glossList.size()){
                resetWords(null);
                return;
            } else {
                items = new String[glossList.size()];
                glossList.getWords().copyInto(items);
                items=glossList.sortGlosses(items);
            }
            glossList.resetWords(items);
        }
    }

    public void freqSort()
    {
        if (headwordsShowing){
            String[] items = headwordList.getWords().toArray(new String[headwordList.size()]);
            Arrays.sort(items, new FreqComparator());
            headwordList.resetWords(items);
        } else {
            String[] items = new String[glossList.getWords().size()];
            glossList.getWords().copyInto(items);
            Arrays.sort(items, new FreqComparator());
            glossList.resetWords(items);
        }
    }

    public void rhymeSort() {
        if (headwordsShowing) {
                String[] items = headwordList.getWords().toArray(new String[headwordList.size()]);
                Arrays.sort(items, new RhymeComparator());
                headwordList.resetWords(items);
            }
    }

    /* ...still too slow... -KP

      public void POSSort()
      {
      if (headwordsShowing)
      {
      String[] items = headwordList.getWords().toArray(new String[headwordList.size()]);
      Arrays.sort(items, new POSComparator());
      headwordList.resetWords(items);
      }
      }*/


    static class RhymeComparator implements Comparator<String> {

        @Override
        public int compare(String a, String b) {
            StringBuilder sa = new StringBuilder(Helper.getWord(a));
            StringBuilder sb = new StringBuilder(Helper.getWord(b));
            sa.reverse();
            sb.reverse();
            return sa.toString().compareTo(sb.toString());
        }

    }


    class FreqComparator implements Comparator<String> {

        @Override
        public int compare(String sa, String sb) {
            int fa = parent.cache.getFreq(sa);
            int fb = parent.cache.getFreq(sb);
            // System.out.println("Comparing " + sa + "(freq " + fa + ") and "
            //  + sb + " (freq " + fb + ")");
            return fb - fa;   //ie sort by freq in descending order
        }

    }


    /* This is too slow to be practical so far -KP
     *
    class POSComparator implements Comparator, Serializable {
        public int compare(Object a, Object b)
        {
            DictEntry dea=parent.cache.getIndexEntry((String)a);
            Vector posa=parent.cache.getPOS(dea);
            DictEntry deb=parent.cache.getIndexEntry((String)b);
            Vector posb=parent.cache.getPOS(deb);
            //      System.err.print(posa+"\t"+posb+"\t");
            if (posa==null)
                {
                    if (posb==null)
                        return ScrollPanel.compareTwo((String)a,(String)b);
                    else
                        return 1;
                } else {
                    if (posb==null)
                        return -1;
                }
            String one=(String)posa.elementAt(0);
            String two=(String)posb.elementAt(0);
            //    System.err.println(one+"\t"+two);
            int i=ScrollPanel.compareTwo(one,two);
            if (i==0)
                return ScrollPanel.compareTwo((String)a,(String)b);
            return i;
        }


        public boolean equals(Object a, Object b)
        {
            //      System.err.println(a+" "+b);
            DictEntry dea=parent.cache.getIndexEntry((String)a);
            Vector posa=parent.cache.getPOS(dea);
            DictEntry deb=parent.cache.getIndexEntry((String)b);
            Vector posb=parent.cache.getPOS(deb);
            if (posa==null && posb==null) return (ScrollPanel.compareTwo((String)a,(String)b)==0);
            if (posa==null || posb==null) return false;
            String one=(String)posa.elementAt(0);
            String two=(String)posb.elementAt(0);
            if (Scrollpanel.compareTwo(one,two)==0)
                return (ScrollPanel.compareTwo((String)a,(String)b)==0);
            else return false;

        }
        }*/

 /** This could be faster if passed in a dictentry or cache entry with
     *  the word and dictentry already there?
     *
    class GlossAlphaComparator implements Comparator, Serializable {
        public String getString(Object a)
        {
            String one;
            if (a==null) Dbg.print("a is null!!!");
            EnglishDictEntry edea=parent.cache.getEnglishIndexEntry((String)a);
            if (edea==null) Dbg.print("edea is null!! "+a);
            //      if (edea.mainword !=null)
            if (edea.mainwordPosition!=-1)
                one=((String)a).substring(edea.mainwordPosition,((String)a).length());//mainword;
            else
                one=(String)a;
            return one;
        }

        public int compare(Object a, Object b)
        {
            String one, two;
            if (a==null) Dbg.print("a is null!!");
            EnglishDictEntry edea=parent.cache.getEnglishIndexEntry((String)a);
            if (edea==null) Dbg.print("edea is null!! "+a);
            if (edea.mainwordPosition!=-1)
                one=((String)a).substring(edea.mainwordPosition,((String)a).length());//edea.mainword;
            else
                one=(String)a;
            if (b==null) Dbg.print("b is null!!!");
            EnglishDictEntry edeb=parent.cache.getEnglishIndexEntry((String)b);
            if (edea==null) Dbg.print("edeb is null!! "+b);
            if (edeb.mainwordPosition!=-1)
                two=((String)b).substring(edeb.mainwordPosition,((String)b).length());//edea.mainword;
            else
                two=(String)b;
            if (one.toLowerCase().equals(((String)b).toLowerCase())) return 1;
            if (two.toLowerCase().equals(((String)a).toLowerCase())) return -1;
            return (((String) one).toLowerCase()).compareTo(((String) two).toLowerCase());
        }

        public boolean equals(Object a, Object b)
        {
            String one=getString(a);
            String two=getString(b);
            return (((String) one).toLowerCase()).equals(((String) two).toLowerCase());
        }
        }*/

}

