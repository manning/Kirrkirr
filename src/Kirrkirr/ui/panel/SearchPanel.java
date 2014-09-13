package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.*;
import Kirrkirr.ui.data.TableSorter;
import Kirrkirr.ui.data.KKListModel;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.ui.JTextFieldLimiter;
import Kirrkirr.dictionary.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

import org.apache.oro.text.regex.*;

/** SearchPanel: Advanced search capabilities.
 *  Parts adapted from the examples from the Sun java tutorial for using
 *  the table API.
 */
public class SearchPanel extends KirrkirrPanel
    implements ActionListener, ListSelectionListener, Runnable {

    /////
    //   Static constants
    ////
// private static final Color darkBrown = new Color(184, 106, 69);
    private static final Color lightBrown = new Color(255, 220, 180);

    /** enable fast searching */
    private static boolean fastSearch = true;

    /** Number of columns in the search results table */
    protected static final int COLS = 2;        // accessed in MatchItem
    /** Word goes in the first column of the search results */
    private static final int WORD = 0;
    /** 'Matched' goes in the second column of the search results */
    private static final int MATCHED = 1;

    public static final int FILTER = 1, HIGHLIGHT = 2, ADD = 3,
        AUTOHIGHLIGHT = 4, DELETE = 5;

    //search words from where? results list, headwords list, whole dictionary, glosses
    private static final int RESULTS_LIST=0, HEADWORDS=1, DICTIONARY=2,
                                GLOSSES=3;

    //search for what? headwords or glosses or whole entry
    private static final int FIND_HEADWORD = 0;
    private static final int FIND_GLOSS = 1;
    private static final int FIND_WHOLE_ENTRY = 2;
    // higher numbers are custom and in Array

    /////
    //   Regular expression components
    /////
    private Perl5Compiler p5c = new Perl5Compiler();
    private Perl5Matcher p5m = new Perl5Matcher();

    private Thread searcherThread; // = null;

    private Hashtable searchRegexps;   // stores name -> regexp
    String[] searchDescriptions;

    /** array of headword fuzzy spelling regexs, init'd first time
        search using fuzzy is performed.
        note that ordering of things in this array is important:
        lowest numbered = preferred expansion */
    private static Regex[] headwordMistake, glossMistake; // = null

    /** for timing searches */
    private long starttime;

    //variables for fast search
    private static Pattern entryreg; // = null;
    private static Pattern uniqreg; // = null;

    /** required because of deprecation of Thread.stop() in java1.2 */
    private volatile boolean b_searcher = false;

    /////
    //    String constants
    /////
    private final static String SC_SEARCH_NAME    = "Advanced_Search";
    private final static String SC_SEARCH_NAME_SHORT = "Search";
    private final static String SC_SEARCH_TITLE   = "Type_your_query_and_press_enter!";
    private final static String SC_SEARCHING      = "Searching...";
    private final static String SC_SEARCH_STOPPED = "Search_stopped";
    private final static String SC_BAD_REGEXP = "Search_expression_error";
    private final static String SC_WORD           = "Word";
    private final static String SC_MATCHED        = "Matched_with";
    private final static String SC_SEARCH_ROLLOVER= "Search_and_highlight_or_extract_the_words_you_want";
    private final static String SC_SEARCH         = "Search";
    private final static String SC_FIND           = "Find!";
    private final static String SC_STOP           = "Stop";
    private final static String SC_LOOK_IN        = "Look_in";
    private final static String SC_WITH           = "with";
    private final static String SC_PICTURE        = "Picture";
    private final static String SC_SOUND          = "Sound";
    private final static String SC_NOTE           = "Note";
    private final static String SC_WHOLE_WORD     = "Whole_Word";
    private final static String SC_METHOD         = "Method";
    private final static String SC_FUZZY          = "Fuzzy_Spelling";
    private final static String SC_REGEX          = "Regular_Expression";
    private final static String SC_PLAIN          = "Plain";
    private final static String SC_RESULTS_FROM   = "Search_Within";
    private final static String SC_RESULT_LIST    = "Search_Results";
    private final static String SC_RESULT_LIST_BORDER
        = "Search_Results_to_Word_List";
    private final static String SC_CURRENT_LIST   = "Word_List";
    private final static String SC_ALL_WORDS      = "Whole_Dictionary";
    private final static String SC_AUTO_SELECT    = "Auto_Select";
    private final static String SC_SELECTED_RESULTS="Selected_Results";
    private final static String SC_ALL_RESULTS    = "All_Results";
    private static final String SC_HIGHLIGHT      = "Highlight_in_Word_List";
    private final static String SC_FILTER_MAIN    = "Filter_Word_List";
    private final static String SC_ADD_MAIN       = "Add_to_Word_List";
    private final static String SC_DEL_MAIN       = "Delete_from_Word_List";
    private final static String SC_HIGHLIGHT_S      = "Highlight";
    private final static String SC_FILTER_MAIN_S    = "Filter";
    private final static String SC_ADD_MAIN_S       = "Add";
    private final static String SC_DEL_MAIN_S       = "Delete";
    private final static String SC_ITEM    = "item";
    private final static String SC_ITEMS   = "items";
    private final static String SC_MATCHED_TOTAL = "matched_in_total";
    // ap -- added to advanced search combo box
    private final static String SC_HEADWORD = "Headword";
    private final static String SC_GLOSS = "Gloss";
    private final static String SC_FULL_ENTRY = "Full_Entry";

    /////
    //   Swing variables
    /////
    /** model for results table */
    private MyTableModel myModel;
    /** table sorter for results */
    private TableSorter sorter;
    /** results table */
    private JTable table;

    /** panel on left containing the search option widgets
        (but not the results table) */
    private JPanel leftPanel;

    /** Where the search query is entered */
    private JTextField    searchBox;
    /** start the search */
    private JButton       start;
    /** stop the search */
    private JButton       stop;
    /** search progress bar */
    private JProgressBar  progressBar;

    /** search in (headword, gloss, domain...) */
    private JComboBox taglist;

    /** search using fuzzy spelling */
    private JRadioButton fuzzy;
    /** search using regular expression */
    private JRadioButton regex;
    /** search using plain word */
    private JRadioButton plain;
    /** search methods combobox for when SMALL */
    private JComboBox methodlist;

    /** search for words with sounds */
    private JCheckBox withSound;
    /** search for words with images */
    private JCheckBox withPic;
    /** search for words with notes */
    private JCheckBox withNote;

    /** get results from results box */
    // private JRadioButton results;
    /** get results from main list */
    // private JRadioButton list;
    /** get results from whole dictionary */
    // private JRadioButton dictionary;
    /** get results from combobox for when SMALL */
    private JComboBox resultsfromlist;

    /** search for words with sounds */
    private JCheckBox wholeWord;


    /** select results clicked on in main list? */
    private JCheckBox autoselect;

    /** apply buttons to selected results */
    private JRadioButton highlighted;
    /** apply buttons to all results in table */
    private JRadioButton all_field;

    /** highlight results */
    private JButton highlight;
    /** filter main list */
    private JButton copy;
    /** add selected to main list (without deleting what is already there) */
    private JButton add;
    /** delete selected from main list */
    private JButton del;

    /** Panel for searching functionality.
     *
     *  Used to have buttons (in size=LARGE) for 'results from' and 'search
     *  methods' but combined them into a combo box. But left the code
     *  commented out - maybe better UI?
     *  This is the structure of the Search panel interface (large size):
     *  <pre>
     *    SearchPanel (KirrkirrPanel, GridBagLayout))
     *      leftemp (JPanel, BoxLayout(Y))
     *        leftPanel (JPanel, BoxLayout(Y))
     *          progressPanel (JPanel, BoxLayout(X))
     *            progressBar (JProgressBar)
     *          verticalStrut
     *          verticalglue
     *          searchPanel (JPanel, BoxLayout(X))
     *            sl (JLabel)
     *            searchBox (JTextField)
     *          verticalglue
     *          findinPanel (JPanel, BoxLayout(X))
     *            lookinPanel (JPanel, BoxLayout(Y))
     *              tag (JPanel, BoxLayout(X)
     *                lookin (JLabel, "Look in")
     *                taglist (JComboBox)
     *              p051 (JPanel, BoxLayout(X))
     *                l051 (JLabel, "with:")
     *                withPic, withSound, withNote buttons
     *            horizontalstrut
     *            searchButtPanel (JPanel, BoxLayout(Y))
     *              start
     *              stop
     *          verticalglue
     *          upperbottom (JPanel, BoxLayout(Y))
     *            methodPanel (JPanel, BoxLayout(X))
     *              method (JLabel, "Search Method")
     *              methodlist (JComboBox)
     *              wholeWord (JCheckBox)
     *            verticalglue
     *            resultsFromPanel (JPanel, BoxLayout(X))
     *              from (JLabel, "Search within:")
     *              resultsfromList (JComboBox)
     *          verticalglue
     *        bottom (JPanel, BoxLayout(X))
     *          resultSelectPanel (JPanel, BoxLayout(Y))
     *            autoselect
     *            highlighted
     *            all_field
     *          horizontalglue
     *          resultApplyPanel (JPanel, BoxLayout(Y))
     *            highlight, copy, add buttons
     *          horizontalglue
     *      rightPanel (JPanel, BoxLayout(Y))
     *        scrollPane (JScrollPane)
     *          table (JTable)
     *  </pre>
     */
    public SearchPanel(Kirrkirr kparent, int size) {
        super(kparent);
        if (size <= KirrkirrPanel.TINY) {
            setName(Helper.getTranslation(SC_SEARCH_NAME_SHORT));
        } else {
            setName(Helper.getTranslation(SC_SEARCH_NAME));
        }
        boolean small = (size <= KirrkirrPanel.SMALL);
        boolean onMacOSX = Helper.onMacOSX();

        JPanel progressPanel=null;
        if (! small) {
            progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
            progressPanel.setBackground(lightBrown);
            // JLabel pl = new JLabel(Helper.getTranslation(SC_PROGRESS)+": ");
            // pl.setBackground(darkBrown);
            // pl.setForeground(Color.black);
            // pl.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
            // progressPanel.add(pl);
            progressBar = new JProgressBar(0, 100);
            progressBar.setValue(0);
            progressPanel.add(progressBar);
        }

        JPanel searchPanel=new JPanel();
        searchPanel.setBackground(lightBrown);
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        JLabel sl = new JLabel(Helper.getTranslation(SC_SEARCH)+": ");
        sl.setOpaque(true);
        //        sl.setBackground(new Color(128, 64, 64));
        sl.setForeground(Color.black);
        sl.setBackground(lightBrown);
        if (size <= KirrkirrPanel.TINY) {
            sl.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
        } else {
            sl.setFont(FontProvider.PROMINENT_VERY_LARGE_INTERFACE_FONT);
        }
        searchPanel.add(sl);

        int len = 18;
        if (size <= KirrkirrPanel.TINY) {
            len = 15;
        }
        searchBox = new JTextField(len);
        searchBox.setDocument(new JTextFieldLimiter());
        if (size <= KirrkirrPanel.TINY) {
            searchBox.setFont(FontProvider.PROMINENT_LARGE_WORD_FONT);
        } else {
            searchBox.setFont(FontProvider.PROMINENT_VERY_LARGE_WORD_FONT);
        }
        searchBox.setMaximumSize(new Dimension(300, 24));
        searchBox.addActionListener(this);
        searchBox.requestFocus();
        searchPanel.add(searchBox);

        JPanel searchButtPanel=new JPanel();
        searchButtPanel.setPreferredSize(new Dimension(120, 60));
        searchButtPanel.setLayout(new BoxLayout(searchButtPanel,BoxLayout.Y_AXIS));
        searchButtPanel.setBackground(lightBrown);
        start = new KirrkirrButton(Helper.getTranslation(SC_FIND), this);
        start.setAlignmentY(Component.CENTER_ALIGNMENT);
        start.setBackground(Color.green);
        start.setForeground(Color.black);
        start.setVisible(true);

        stop = new KirrkirrButton(Helper.getTranslation(SC_STOP), this);
        stop.setAlignmentY(Component.CENTER_ALIGNMENT);
        stop.setVisible(true);
        stop.setBackground(Color.red);
        if ( ! onMacOSX) {
            stop.setForeground(Color.white);
        }

        searchButtPanel.add(start);
        searchButtPanel.add(stop);

        JPanel lookinPanel = new JPanel();
        lookinPanel.setLayout(new BoxLayout(lookinPanel,BoxLayout.Y_AXIS));
        // cdm Jul 2007: currently neither of below methods seems to work!!
        lookinPanel.setBackground(lightBrown);
        // lookinPanel.setOpaque(false);

        if (Kirrkirr.dictInfo != null) {
            searchRegexps = Kirrkirr.dictInfo.getSearchRegexps();
        } else {
            searchRegexps = new Hashtable();
        }
        int j = 3;
        searchDescriptions= new String[j + searchRegexps.size()];
        searchDescriptions[0] = Helper.getTranslation(SC_HEADWORD);
        searchDescriptions[1] = Helper.getTranslation(SC_GLOSS);
        searchDescriptions[2] = Helper.getTranslation(SC_FULL_ENTRY);
        for (Enumeration en = searchRegexps.keys(); en.hasMoreElements(); ) {
            searchDescriptions[j++] = (String) en.nextElement();
        }
        taglist = new JComboBox(searchDescriptions);

        taglist.setSelectedIndex(0);
        taglist.setAlignmentX(Component.CENTER_ALIGNMENT);
        taglist.addActionListener(this);

        JLabel lookin = new JLabel(Helper.getTranslation(SC_LOOK_IN)+": ");
        lookin.setForeground(Color.black);
        lookin.setBackground(lightBrown);
        lookin.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);

        JPanel tag = new JPanel();
        tag.setLayout(new BoxLayout(tag,BoxLayout.X_AXIS));
        tag.add(lookin);
        tag.add(taglist);
        tag.setBackground(lightBrown);

        // unless I had space before With, edge of W was chopped.  Wacky
        JLabel l051 = new JLabel(' ' +Helper.getTranslation(SC_WITH)+":  ");
        l051.setForeground(Color.black);
        l051.setBackground(lightBrown);
        withSound = new JCheckBox(Helper.getTranslation(SC_SOUND));
        withPic = new JCheckBox(Helper.getTranslation(SC_PICTURE));
        withNote=new JCheckBox(Helper.getTranslation(SC_NOTE));

        JPanel p051 = new JPanel();
        p051.setBackground(lightBrown);
        withSound.setBackground(lightBrown);
        withPic.setBackground(lightBrown);
        withNote.setBackground(lightBrown);
        p051.setAlignmentX(Component.CENTER_ALIGNMENT);
        p051.setLayout(new BoxLayout(p051, BoxLayout.X_AXIS));
        p051.add(l051);
        p051.add(withPic);
        p051.add(withSound);
        p051.add(withNote);

        lookinPanel.add(tag);
        lookinPanel.add(p051);

        JPanel findinPanel = new JPanel();
        findinPanel.setBackground(lightBrown);
        findinPanel.setLayout(new BoxLayout(findinPanel,BoxLayout.X_AXIS));
        findinPanel.add(lookinPanel);
        findinPanel.add(Box.createHorizontalStrut(6));
        findinPanel.add(searchButtPanel);

        JPanel methodPanel = new JPanel();
        methodPanel.setBackground(lightBrown);
        JLabel method=new JLabel(Helper.getTranslation(SC_METHOD)+": ");
        method.setForeground(Color.black);
        method.setBackground(lightBrown);
        Vector methods = new Vector(3);
        methods.addElement(Helper.getTranslation(SC_FUZZY));
        methods.addElement(Helper.getTranslation(SC_REGEX));
        methods.addElement(Helper.getTranslation(SC_PLAIN));

        if (false) {          // was (!small)
            methodPanel.setLayout(new BoxLayout(methodPanel, BoxLayout.Y_AXIS));
            methodPanel.add(method);

            methodPanel.add(fuzzy = new JRadioButton((String)methods.elementAt(0)));
            methodPanel.add(regex = new JRadioButton((String)methods.elementAt(1)));
            methodPanel.add(plain = new JRadioButton((String)methods.elementAt(2)));
            fuzzy.setBackground(lightBrown);
            regex.setBackground(lightBrown);
            plain.setBackground(lightBrown);
            fuzzy.setSelected(true);
            ButtonGroup g10= new ButtonGroup();
            g10.add(fuzzy);
            g10.add(regex);
            g10.add(plain);
            fuzzy.addActionListener(this);
            regex.addActionListener(this);
            plain.addActionListener(this);
        } else {
            methodPanel.setLayout(new BoxLayout(methodPanel, BoxLayout.X_AXIS));
            methodlist=new JComboBox(methods);
            methodlist.setSelectedIndex(0);
            methodlist.setAlignmentX(Component.CENTER_ALIGNMENT);

            wholeWord = new JCheckBox(Helper.getTranslation(SC_WHOLE_WORD));
            // I've turned off having wholeWord selected by default since
            // it has a number of non-intuitive behaviors (parentheses don't
            // count as ends of words, but hyphens do, etc.). cdm Nov 2001
            // wholeWord.setSelected(true);
            wholeWord.setBackground(lightBrown);

            methodPanel.add(method);
            methodPanel.add(methodlist);
            methodPanel.add(Box.createHorizontalStrut(4));
            methodPanel.add(wholeWord);
        }

        JPanel resultsFromPanel=new JPanel();
        resultsFromPanel.setBackground(lightBrown);
        JLabel from=new JLabel(Helper.getTranslation(SC_RESULTS_FROM)+": ");
        from.setForeground(Color.black);
        from.setBackground(lightBrown);
        Vector resultsfrom=new Vector(3);
        resultsfrom.addElement(Helper.getTranslation(SC_RESULT_LIST));
        resultsfrom.addElement(Helper.getTranslation(SC_CURRENT_LIST));
        resultsfrom.addElement(Helper.getTranslation(SC_ALL_WORDS));

        /*      if (!small){
            resultsFromPanel.setLayout(new BoxLayout(resultsFromPanel, BoxLayout.Y_AXIS));
            resultsFromPanel.add(from);
            resultsFromPanel.add(results = new JRadioButton((String)resultsfrom.elementAt(0)));
            resultsFromPanel.add(list = new JRadioButton((String)resultsfrom.elementAt(1)));
            resultsFromPanel.add(dictionary = new JRadioButton((String)resultsfrom.elementAt(2)));

            results.setBackground(lightBrown);
            list.setBackground(lightBrown);
            dictionary.setBackground(lightBrown);

            list.setSelected(true);

            ButtonGroup g11= new ButtonGroup();
            g11.add(results);
            g11.add(list);
            g11.add(dictionary);

            results.addActionListener(this);
            results.setToolTipText(Helper.getTranslation("Search_only_in_entries_from_the_results_table_(below)"));
            list.addActionListener(this);
            list.setToolTipText(Helper.getTranslation("Search_only_in_entries_from_the_headwords_list_(on_the_left)"));
            dictionary.addActionListener(this);
            dictionary.setToolTipText(Helper.getTranslation("Search_all_entries_in_the_dictionary"));
        }else{
        */  resultsFromPanel.setLayout(new BoxLayout(resultsFromPanel, BoxLayout.X_AXIS));
            resultsfromlist = new JComboBox(resultsfrom);
            resultsfromlist.setSelectedIndex(DICTIONARY);  // 2 = whole dictionary
            resultsfromlist.setAlignmentX(Component.CENTER_ALIGNMENT);
            resultsFromPanel.add(from);
            resultsFromPanel.add(resultsfromlist);
            //  }
        JPanel upperbottom=new JPanel();
        // if (small) {
        upperbottom.setLayout(new BoxLayout(upperbottom,BoxLayout.Y_AXIS));
        // } else {
        //     upperbottom.setLayout(new BoxLayout(upperbottom,BoxLayout.X_AXIS));
        // }
        upperbottom.add(methodPanel);
        upperbottom.add(Box.createVerticalGlue());
        upperbottom.add(resultsFromPanel);
        upperbottom.setBackground(lightBrown);

        leftPanel=new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(lightBrown);
        // leftPanel.setForeground(Color.red.darker());
        printSearchStatus(leftPanel, Helper.getTranslation(SC_SEARCH_TITLE));

        if (! small) {
            leftPanel.add(progressPanel);
            leftPanel.add(Box.createVerticalStrut(3));
        }
        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(searchPanel);
        leftPanel.add(Box.createVerticalGlue());

        leftPanel.add(findinPanel);
        leftPanel.add(Box.createVerticalGlue());

        // suppress upperbottom stuff if size is TINY
        if (size > KirrkirrPanel.TINY) {
            leftPanel.add(upperbottom);
            leftPanel.add(Box.createVerticalGlue());
        }

        myModel = new MyTableModel(this);
        sorter = new TableSorter(myModel);
        table = new JTable(sorter);
        sorter.addMouseListenerToHeaderInTable(table);
        table.setBackground(Color.white);
        table.setFont(FontProvider.WORD_LIST_FONT);

        table.getSelectionModel().addListSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(table);
        table.setBackground(Color.white);

        JPanel bottom=new JPanel();
        bottom.setBackground(lightBrown);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(SC_RESULT_LIST_BORDER)));

        JPanel resultSelectPanel=new JPanel();
        resultSelectPanel.setLayout(new BoxLayout(resultSelectPanel,BoxLayout.Y_AXIS));
        resultSelectPanel.setBackground(lightBrown);

        autoselect = new JCheckBox(Helper.getTranslation(SC_AUTO_SELECT));
        autoselect.setSelected(true);
        autoselect.setBackground(lightBrown);
        resultSelectPanel.add(autoselect);

        highlighted= new JRadioButton(Helper.getTranslation(SC_SELECTED_RESULTS));
        all_field=new JRadioButton(Helper.getTranslation(SC_ALL_RESULTS));
        all_field.setSelected(true);
        highlighted.setBackground(lightBrown);
        all_field.setBackground(lightBrown);

        ButtonGroup whichresults= new ButtonGroup();
        whichresults.add(highlighted);
        whichresults.add(all_field);

        resultSelectPanel.add(highlighted);
        resultSelectPanel.add(all_field);

        JPanel resultApplyPanel=new JPanel();
        // resultApplyPanel.setLayout(new BoxLayout(resultApplyPanel,BoxLayout.Y_AXIS));
        resultApplyPanel.setLayout(new GridLayout(2, 2, 2, 2));
        resultApplyPanel.setBackground(lightBrown);

        if (true) {  // was if (small)
            highlight=new KirrkirrButton(Helper.getTranslation(SC_HIGHLIGHT_S),
                                         this);
            copy=new KirrkirrButton(Helper.getTranslation(SC_FILTER_MAIN_S),
                                    this);
            add=new KirrkirrButton(Helper.getTranslation(SC_ADD_MAIN_S),
                                   this);
            del=new KirrkirrButton(Helper.getTranslation(SC_DEL_MAIN_S),
                                   this);
        } else {
            highlight=new KirrkirrButton(Helper.getTranslation(SC_HIGHLIGHT),
                                         this);
            copy=new KirrkirrButton(Helper.getTranslation(SC_FILTER_MAIN),
                                    this);
            add=new KirrkirrButton(Helper.getTranslation(SC_ADD_MAIN),
                                   this);
            del=new KirrkirrButton(Helper.getTranslation(SC_DEL_MAIN),
                                   this);
        }
        highlight.setBackground(Color.yellow);
        highlight.setForeground(Color.black);
        if (! onMacOSX) {
            copy.setBackground(Color.black);
            copy.setForeground(Color.white);
        }
        add.setBackground(Color.black);
        add.setBackground(Color.white);
        del.setBackground(Color.red);
        if (! onMacOSX) {
            del.setForeground(Color.white);
        }

        resultApplyPanel.add(highlight);
        resultApplyPanel.add(copy);
        resultApplyPanel.add(add);
        resultApplyPanel.add(del);

        bottom.add(resultSelectPanel);
        //bottom.add(Box.createVerticalStrut(4));
        bottom.add(Box.createHorizontalGlue());
        bottom.add(resultApplyPanel);
        bottom.add(Box.createHorizontalGlue());

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(350, 350));
        // X part of this does good in starting rightPanel big
        rightPanel.add(scrollPane);

        JPanel leftemp=new JPanel();
        leftemp.setLayout(new BoxLayout(leftemp,BoxLayout.Y_AXIS));
        leftemp.add(leftPanel);

        // positioning or non-positioning of Results to Word List
        // if we're tiny, we suppress it altogether
        if ( ! small) {
            leftemp.add(bottom);
        } else if (size > KirrkirrPanel.TINY) {
            rightPanel.add(bottom);
        }

        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        setLayout(gbl);
        gbc.fill = GridBagConstraints.BOTH;

        gbc.weightx = 0.01;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbl.setConstraints(leftemp, gbc);
        //setLayout(new GridLayout(1,2));//BoxLayout(this,BoxLayout.X_AXIS));
        add(leftemp);
        gbc.weightx = 0.9;
        gbc.weighty = 1.0;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbl.setConstraints(rightPanel, gbc);
        add(rightPanel);
    } //end constructor (swing initialization)


    public void clearSearchResults() {
        myModel.clear();
        repaint();
    }


    /** Called when scroll list changes. */
    public void scrollPanelChanged(boolean toGloss) {
        clearSearchResults();
        withNote.setEnabled( ! toGloss);
        withSound.setEnabled( ! toGloss);
    }


    /** Returns the String that is suitable rollover text for a tabbed
     *  pane containing this panel.
     *  @return the string to be used as rollover text
     */
    public String getTabRollover() {
        return Helper.getTranslation(SC_SEARCH_ROLLOVER);
    }


    /** Implemented as part of listSelection interface.
    *  When something in the list is selected and autoselect
    *  is true, selects the word in the main list.
    */
    public void valueChanged(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        if (!lsm.isSelectionEmpty() && autoselect.isSelected()) {
            updateWordList(AUTOHIGHLIGHT);
        }
    }


    /** Listens to search box and start, stop,
     *  highlight and filter main list (copy) buttons.
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == searchBox || src == start)
            startSearch();
        else if (src == stop)
            stopSearch();
        else if (src == highlight)
            updateWordList(HIGHLIGHT);
        else if (src==copy)
            updateWordList(FILTER);
        else if (src==add)
            updateWordList(ADD);
        else if (src==del)
            updateWordList(DELETE);
    }


    /** Must override method from abstract class KirrkirrPanel. Here a no-op.
     */
    public void setCurrentWord(/* padded */ String tailWord, boolean gloss,
                        final JComponent signaller, final int signallerType,
                        final int arg) {
        // do nothing
    }


    /** Overrides KirrkirrPanel. Copies the information from the results
     *  table to the system clipboard:
     *  list of word+ tab + matched information + endline.
     *  Returns the length of the string copied, or 0 if nothing was.
     *  At present always does a copy regardless of parameter
     *  @param isCut true if this should be a cut operation
     *  @return how many characters were copied (0 if no selection)
     */
    public int copyText(boolean isCut) {
        int count = table.getSelectedRowCount();
        if (count > 0) {
            StringBuffer sb = new StringBuffer("");
            int[] high = table.getSelectedRows();
            for (int i=0 ; i<count ; i++) {
                sb.append(sorter.getValueAt(high[i], WORD));
                sb.append('\t');
                sb.append(sorter.getValueAt(high[i], MATCHED));
                sb.append(RelFile.lineSeparator());
            }
            String text = sb.toString();
            parent.putStringInClipboard(text);
            return(text.length());
        } else {
            return(0);
        }
    }


    /** Returns true if the "search in" combobox is
     *  set to the headwords index.
     *  @return true if we are currently looking in Headword headwords
     */
    private boolean searchingHeadwords()
    {
        int index=taglist.getSelectedIndex();
        return (index==FIND_HEADWORD);
    }


    /** Returns true if the "look in" combobox is
     *  set to a field that is written in Gloss.
     *  @return true if we are currently looking in an Gloss language field
     */
    private boolean searchingGloss()
    {
        int index = taglist.getSelectedIndex();
        return (index == FIND_GLOSS);
    }


    /** Called when highlight or filter main list is clicked.
     *  Highlights words in main list, or clears main list
     *  except for these words. Which words it affects depends
     *  on whether the user has clicked "selected results"
     *  or "all results."
     *  @param arg what to do: HIGHLIGHT or FILTER or ADD or AUTOHIGHLIGHT
     *     or DELETE
     */
    private void updateWordList(int arg) {
        Vector selections;  // always initialized below

        //apply to just those results that are selected
        if (highlighted.isSelected() || arg == AUTOHIGHLIGHT) {
            int[] high = table.getSelectedRows();
            selections = new Vector(high.length);
            for (int i=0 ; i<high.length; i++) {
                selections.addElement(Helper.printableStringToUniqueKey(
                                 (String) sorter.getValueAt(high[i], WORD)));
            }
        /*---
        } else if (arg == DELETE) {
            selections = new Vector();
            selections = combineLists(parent.scrollPanel.getWords(),
                                      selections);
        } else if (arg == ADD) {
            int high[] = table.getSelectedRows();
            selections = new Vector(high.length);
            Vector list = new Vector();
            list = combineLists(parent.scrollPanel.getWords(), list);
            for (int i=0 ; i<high.length ; i++) {
                if(!list.contains(sorter.getValueAt(high[i], WORD)))
                   selections.addElement(sorter.getValueAt(high[i], WORD));
            }
        ---*/
        } else {
            // it's not an AUTOHIGHLIGHT request and all_field is selected
            // apply to all results, not just those selected
            int count = myModel.getRowCount();
            selections = new Vector(count);
            for (int i = 0 ; i<count ; i++) {
                selections.addElement(Helper.printableStringToUniqueKey(
                                 (String) sorter.getValueAt(i, WORD)));
            }
        }

        if (arg == FILTER) {
            turnOnSubWordsDisplayIfWholeDictionary();
            makeSet(selections);
            parent.scrollPanel.resetWordsGUI(selections);
        } else if (arg == ADD) {
            selections = combineLists(parent.scrollPanel.getWords(),
                                      selections);
          turnOnSubWordsDisplayIfWholeDictionary();
          parent.scrollPanel.resetWordsGUI(selections);
        } else if (arg == DELETE) {
            selections = subtractFrom(parent.scrollPanel.getWords(),
                                      selections);
            parent.scrollPanel.resetWordsGUI(selections);
        } else { // HIGHLIGHT
            parent.scrollPanel.highlightInWordList(selections);
        }
    }

    private static void makeSet(Vector vector) {
        int sz = vector.size();
        for (int k = sz - 1; k >= 0; k--) {
            int f = vector.indexOf(vector.elementAt(k));
            if (f < k) {
                vector.removeElementAt(k);
            }
        }
    }

    private void turnOnSubWordsDisplayIfWholeDictionary() {
    int listToLookIn = DICTIONARY;
    if (resultsfromlist != null) {
        listToLookIn = resultsfromlist.getSelectedIndex();
    }
    if (listToLookIn == DICTIONARY) {
      parent.turnOnSubWordsDisplay();
    }
  }


  /** For "add to main list": creates a new vector with the
     *  intersection of the passed in list and vector (ie no
     *  duplication).
     *  Chris: if there are a lot of elements in sel, this might be
     *  inefficient, and we may wish to trash list, and build a new
     *  ListModel
     *  @param list the headwords or glosses from the scrolllist
     *  @param sel the selections to add to the list
     *  @return a vector with the intersection of list and sel
     */
    private static Vector combineLists(KKListModel list, Vector sel) {
        for (Enumeration e=list.elements(); e.hasMoreElements(); ) {
            Object o=e.nextElement();
            if (!sel.contains(o))
                sel.addElement(o);
        }
        return sel;
    }


    private static Vector subtractFrom(KKListModel list, Vector sel) {
        Vector v = new Vector(list.size());
        for (Enumeration e=list.elements(); e.hasMoreElements(); ) {
            Object o=e.nextElement();
            if (! sel.contains(o)) {
                v.addElement(o);
            }
        }
        return v;
    }


    /** Searches the entry for a word for a regexp (which may limit the
     *  search to some element). Used for all searches except headwords
     *  and gloss. Uses the DictionaryCache to look in the xml file
     *  (whereas gloss and headwords just search indices). If it
     *  matches, the word/match string are used to make a MatchItem,
     *  which is returned so it can be put in the results table.
     *  @param pword padded headword
     *  @param reg regular expression to match
     *  @param regexHasDollarOne true if limited to a certain field,
     *  false if searching the entire entry
     *  @return MatchItem containing result/word or null if nothing matched.
     *  @see DictionaryCache#getSearchMatch
     */
    private Vector searchWholeEntry(String pword, Pattern reg,
                                    boolean regexHasDollarOne) {
        Vector v = null;  // lazily initialized

        String matchStr = parent.cache.getSearchMatch(pword, p5m, reg, regexHasDollarOne);
        if (matchStr != null) {
            if (parent.scrollPanel.headwordsShowing) { //showing headword
                MatchItem m = new MatchItem();
                m.col[WORD] =  Helper.uniqueKeyToPrintableString(pword);
                m.col[MATCHED] =  matchStr;
                if (Dbg.SEARCH) {
                    Dbg.print("Searching " + pword +
                              " regexpHasDollarOne is " + regexHasDollarOne);
                    Dbg.print("Matched With"+" |" + matchStr + '|');
                }
                v = new Vector();
                v.addElement(m);
            } else { //showing gloss, so put gloss in WORD
                Vector gl = parent.cache.getGlossEntry(pword);
                // System.err.println(gl +" found for "+pword+" "+reg);
                for (int i=0; i < gl.size(); i++) {
                    String ukey = (String) gl.elementAt(i);
                    if (ukey == null) continue;
                    MatchItem m = new MatchItem();
                    m.col[WORD] =  ukey;
                    m.col[MATCHED] = matchStr;
                    if (v == null) v = new Vector();
                    v.addElement(m);
                }
            }
        }
        return v;
    }


    /** Checks whether the gloss word matches the regular expression. If it
     *  matches, the word/match string are used to make a MatchItem,
     *  which is returned so it can be put in the results table.
     *  Right now this just adds the gloss as "matched"
     *  and "result" but maybe it should enumerate the headword words
     *  for "result"?
     *  @param word gloss
     *  @param reg regular expression to match
     *  @param isGloss Already have the gloss entry passed in as word
     *  @return MatchItem containing result/word or null if nothing matched.
     */
    private Vector searchGlosses(String word, Pattern reg, boolean isGloss) {
        Vector v = new Vector();

        if (isGloss) {
            matchOneGloss(word, reg, v, null);
        } else {
            Vector gl = parent.cache.getGlossEntry(word);
            for (int i = 0, glsize = gl.size(); i < glsize; i++)
                matchOneGloss((String) gl.elementAt(i), reg, v, word);
        }
        return v;
    }

    private void matchOneGloss(String word, Pattern reg, Vector v, String hw) {

        if (p5m.contains(word, reg)) {
            MatchItem m; // always initialized if used
            if ( ! parent.scrollPanel.headwordsShowing) { //showing gloss
                m = new MatchItem();
                m.col[WORD] =  word;
                m.col[MATCHED] =  word;
                v.addElement(m);
            } else { //showing headword
                if (hw != null) {
                    m = new MatchItem();
                    m.col[WORD] = Helper.uniqueKeyToPrintableString(hw);
                    m.col[MATCHED] =  word;
                    v.addElement(m);
                } else {
                    GlossDictEntry ede=parent.cache.getGlossIndexEntry(word);
                    if (ede==null || ede.headwords==null || ede.headwords.length==0) return;
                    for (int i=0; i<ede.headwords.length; i++) {
                        //String headword=parent.cache.getHeadword(ede.headwords[i]);
                        m = new MatchItem();
                        m.col[WORD] = Helper.uniqueKeyToPrintableString(ede.headwords[i]);
                        m.col[MATCHED] =  word;
                        v.addElement(m);
                    }
                }
            }
        }
    }

    /** Checks whether the headword matches the regular expression. If it
     *  matches, the word/match string are used to make a MatchItem,
     *  which is returned so it can be put in the results table.
     *  Right now this just adds the headword as "matched"
     *  and "result". Maybe should give other information??
     *  @param word headword
     *  @param reg regular expression to match
     *  @return MatchItem containing result/word or null if nothing matched.
     */
    private Vector searchHeadWords(String word, Pattern reg, boolean isHeadword) {
        Vector v = new Vector();

        if (isHeadword) {
            MatchOneHeadword(word, reg, v, null);
        } else {
            GlossDictEntry ede = parent.cache.getGlossIndexEntry(word);
            Vector vec = parent.cache.getHeadwords(ede);
            if (vec==null) return null;
            for (int i=0, vsize = vec.size(); i < vsize; i++) {
                MatchOneHeadword((String)vec.elementAt(i), reg, v, word);
            }
        }
        return v;
    }


    private void MatchOneHeadword(String word, Pattern reg, Vector v,
                                  String gloss) {
        if (p5m.contains(word, reg)) {
            MatchItem m;
            if (parent.scrollPanel.headwordsShowing) { //showing headword
                    m = new MatchItem();
                    m.col[WORD] =  Helper.uniqueKeyToPrintableString(word);
                    m.col[MATCHED] = Helper.uniqueKeyToPrintableString(word);
                    v.addElement(m);
            } else { //showing gloss
                if (gloss != null) {
                    m = new MatchItem();
                    m.col[WORD] =  gloss;
                    m.col[MATCHED] = word;
                    v.addElement(m);
                } else {
                    Vector gl = parent.cache.getGlossEntry(word);
                    for (int i=0, glsize = gl.size(); i < glsize; i++) {
                        String gls = (String) gl.elementAt(i);
                        if (gls==null) continue;
                        m = new MatchItem();
                        m.col[WORD] =  gls;
                        m.col[MATCHED] = word;
                        v.addElement(m);
                    }
                }
            }
        }
    }


    /** Initialize the regular expressions for fuzzy spelling. This is
     *  done the first time a fuzzy spelling search is attempted.
     *
     *  cw 2002: made it so that these are no longer hardcoded in, but
     *  rather taken from the dictionary spec xml file.
     */
    private static void glossFuzzyInit() {
        glossMistake = Kirrkirr.dictInfo.getGlossLanguageSubs();
    }

    /** Initialize the regular expressions for fuzzy spelling. This is
     *  done the first time a fuzzy spelling search is attempted.
     *
     *  cw 2002: made it so that these are no longer hardcoded in, but
     *  rather taken from the dictionary spec xml file.
     */
    private static void fuzzyInit() {
        headwordMistake = Kirrkirr.dictInfo.getHeadwordLanguageSubs();
    }


    /** Convert a string to a regular spelling which accepts words that
     *  are kind of near the original string in spelling, following a
     *  categorical model of possible spelling errors.  At the moment,
     *  this function is Headword-specific. <p>
     *  Implementation notes: to avoid unfortunate interaction between
     *  rewritings, we lowercase the string, substitute lowercase for
     *  uppercase when we want to avoid recursive rewriting, and then
     *  lowercase the result.
     *
     *  @param q The input string
     *  @return The fuzzy spelling regular expression string.
     */
    public static String headwordFuzzy(final String q) {
        if (headwordMistake == null) {
            fuzzyInit();
        }
        String result = q.toLowerCase();
        for (int i = 0; i < headwordMistake.length; i++) {
            result = headwordMistake[i].doReplace(result);
        }
        return result.toLowerCase(); // this last toLower is not really needed!
    }

    public static String glossFuzzy(final String q) {
        if (glossMistake == null) {
            glossFuzzyInit();
        }
        String result = q.toLowerCase();
        for (int i = 0; i < glossMistake.length; i++) {
            result = glossMistake[i].doReplace(result);
        }
        return result.toLowerCase(); // this last toLower is not really needed!
    }


    /** If a search query has parentheses, the regex
     *  won't compile (if they are unmatched) or will
     *  give odd results (if they are treated as regex
     *  parentheses). So, unless the user is searching by regex,
     *  make the parentheses literal parentheses by adding
     *  backslashes before them. That way can search for l(pa), etc.
     *  @param regexString search query
     *  @return search query with parentheses turned into \( and \)
     */
    public static String checkForParentheses(String regexString)
    {
        int upto;
        if (Dbg.SEARCH) Dbg.print("parenthesis check old regex: " + regexString);

        upto = 0;
        while ((upto = regexString.indexOf('(', upto)) >= 0) {
            // cdm: I didn't understand what this was doing
            // if (index==regexString.length()-3) {
            //  regexString=regexString.substring(0,index+1);
            // }
            regexString=regexString.substring(0, upto) + '\\' +
                regexString.substring(upto, regexString.length());
            // add one to go past parenthesis, and one to go past backslash
            upto += 2;
        }

        upto = 0;
        while ((upto = regexString.indexOf(')', upto)) >= 0) {
            // cdm: I didn't understand what this was doing
            // if (index==regexString.length()-3) {
            //  regexString=regexString.substring(0,index+1);
            // }
            regexString=regexString.substring(0, upto) + '\\' +
                regexString.substring(upto, regexString.length());
            // add one to go past parenthesis, and one to go past backslash
            upto += 2;
        }

        if (Dbg.SEARCH) Dbg.print("  new regex: " + regexString);
        return regexString;
    }


    /** Do the search through the words.
     *  This method isn't safe against the word lists changing while
     *  it operates -- we should implement the necessary synchronization
     *  at some point [or just let it fail quietly?]
     */
    private void searcher() throws InterruptedException,
                                        MalformedPatternException {
        if ( ! running())
            return;

        int listToLookIn; // initialized below
        int numWords;     // initialized below
        String[] selections=null;
        int elementToLookIn=taglist.getSelectedIndex();
        int methodId=-1;
        if (methodlist!=null) {
            methodId=methodlist.getSelectedIndex();
        }

        /* Checks which list they want to search and
           gets how many words are in that list:
           results list, current main list, whole dictionary
           or gloss glosses list.

           If searching whole dictionary, can do fast search.
           So, if searching results list or headwords, check whether
           size == whole dictionary; if so, set list to whole dictionary
        */
        if (resultsfromlist != null) {
            listToLookIn =resultsfromlist.getSelectedIndex();
            switch(listToLookIn) {
            case(RESULTS_LIST):
                numWords=myModel.getRowCount();
                if (numWords==parent.cache.headwordList.size() &&
                    elementToLookIn!=FIND_GLOSS) {
                        listToLookIn=DICTIONARY;
                        break;
                    } else if (numWords==parent.cache.glossList.size() &&
                               elementToLookIn==FIND_GLOSS) {
                        listToLookIn=DICTIONARY;
                        break;
                    }
                selections=new String[numWords];
                for (int i=0;i<numWords;i++) {
                    selections[i] = Helper.printableStringToUniqueKey(
                                       (String)sorter.getValueAt(i,WORD));
                }
                break;
            case(HEADWORDS):                    // current word list
                numWords=parent.wordListSize();
                /*if (numWords==parent.cache.headwordList.size() &&
                    parent.headwordsShowing())
                    {
                        listToLookIn=DICTIONARY;
                        break;
                        }*/
                if ((numWords==parent.cache.headwordList.size()
                     && parent.headwordsShowing())
                     ||
                    (numWords==parent.cache.glossList.size() &&
                     !parent.headwordsShowing()))
                    {
                        listToLookIn=DICTIONARY;
                        break;
                    }
                if (!parent.headwordsShowing())
                    listToLookIn=GLOSSES;
                break;
            default: // WHOLE_DICTIONARY
                if (elementToLookIn!=FIND_GLOSS)
                    numWords=parent.cache.headwordList.size();
                else
                    numWords=parent.cache.glossList.size();
            }
        } else { // if SMALL part is commented in in constructor
            listToLookIn=HEADWORDS;
            numWords=parent.headwordsListSize();
        }

        // set up which kind of CellRenderer to use based on Word List
        try {
            table.setDefaultRenderer(Class.forName("javax.swing.JLabel"),
                                     (TableCellRenderer)parent.scrollPanel.getCellRenderer());
        } catch (Exception eee) {
            if (Dbg.ERROR) {
                eee.printStackTrace();
            }
        }

        //clear the results table
        myModel.clear();

        if (progressBar != null) {
            progressBar.setValue(0);
            progressBar.setMaximum(numWords);
        }

        /* search method:
              fuzzy - if headword headwords, use fuzzy.
                    - if gloss use plain (for now)
                    - if anything else, use fuzzy (ie synonyms, etc)
              fuzzy and plain - add \\b word boundary characters. This
                      matches when the query is a word by itself
                      or if it has nonalphanumeric characters on either side.
                      So doesn't match words that just begin with or
                      contain query (that would be bad for 'ear' for example).
                    - currently added on both sides. but possibly would
                      be okay to look for words begining with string (?)
                    - also check string for parentheses, so they
                      don't mess up the regular expression - convert them to
                      literals, ie \) and \(
              regex - assume they know what they're doing,
                      just pass it on.
            */

        String regexString = searchBox.getText();
        if (Dbg.SEARCH) Dbg.print("Search text entered: " + regexString);
        if (wholeWord.isSelected()) {
            regexString="\\b"+searchBox.getText()+"\\b";
        }
        if (methodId==1 || (regex!=null && regex.isSelected())){ //regex
            // do nothin'
        } else { //plain or fuzzy
            regexString=checkForParentheses(regexString);
        }

        if (methodId==0 || (fuzzy!=null && fuzzy.isSelected())) { //fuzzy
            if (searchingGloss()) {
                regexString=glossFuzzy(regexString);
            } else {
                regexString=headwordFuzzy(regexString);
            }
            if (Dbg.SEARCH) Dbg.print("Fuzzified: " + regexString);
        }

        /* searching a particular field, but not gloss or
               headwords, and not whole entry. Adds the
               xml tags for the field to the regular expression.
            */
        if (elementToLookIn != FIND_GLOSS &&
                elementToLookIn != FIND_HEADWORD &&
                elementToLookIn != FIND_WHOLE_ENTRY) {
            String searchKey = searchDescriptions[elementToLookIn];
            String restrictRegex = (String) searchRegexps.get(searchKey);
            // we assume that the restrictRegex has a single ( and ) that
            // we can key off of
            int start = restrictRegex.indexOf('(');
            int end = restrictRegex.indexOf(')');
            regexString = restrictRegex.substring(0, end) +
                regexString + restrictRegex.substring(start + 1);
            if (Dbg.SEARCH) Dbg.print("With restriction: " + regexString);
        }

        Pattern regexP;
        if (methodId==1 || (regex!=null && regex.isSelected())){ //regex
            regexP = p5c.compile(regexString);
        } else { //plain or fuzzy
            regexP = p5c.compile(regexString,
                                 Perl5Compiler.CASE_INSENSITIVE_MASK);
        }

        // lazily learn regexps if can
        if (fastSearch && entryreg == null) {
            String hwStr = Kirrkirr.dictInfo.getHeadwordRegexp();
            String uniqStr = Kirrkirr.dictInfo.getUniquifierRegexp();
            if (hwStr != null) {
                entryreg = p5c.compile(hwStr);
                if (uniqStr != null) {
                    uniqreg = p5c.compile(uniqStr);
                }
            } else {
                fastSearch = false;
            }
        }

        if (Dbg.SEARCH) {
            Dbg.print("Final regex is |" + regexString + "|, "
                             + " regex is "+regexP);
            Dbg.print("fastSearch flag: "+fastSearch+ " subject: " +
                             listToLookIn + " tagId: " +
                             elementToLookIn + " APPLET: " + parent.APPLET
                             + " fastSearch-able: " +
                             (fastSearch && listToLookIn==DICTIONARY &&
                              elementToLookIn != FIND_HEADWORD &&
                              elementToLookIn != FIND_GLOSS &&
                              ! parent.APPLET));
        }

        if (Dbg.TIMING) {
            starttime= System.currentTimeMillis();
        }

        /* can only do fast search if fastsearch is on, searching entire
               dictionary, not looking in headwords or glosses, and not
               running as an applet */
        if (fastSearch && listToLookIn==DICTIONARY &&
             elementToLookIn != FIND_HEADWORD &&
               elementToLookIn != FIND_GLOSS &&  !parent.APPLET) {
            fastSearch(regexP, elementToLookIn != FIND_WHOLE_ENTRY);
        } else {
            standardSearch(regexP, numWords, listToLookIn, selections,
                           elementToLookIn);
        }
    }


    /** Iterates through list we're searching, checking each
     *  item against the regular expression, and adding each
     *  match to the result table, as well as updating
     *  the progress bar. Since this isn't threadsafe, if
     *  the wordlist changes, just stops the search.
     *  @param reg regular expression to check against
     *  @param count the number of items we are searching
     *  @param subject which list to search: DICTIONARY, HEADWORDS,
     *  GLOSSES or RESULT_LIST
     *  @param selections if searching result list, the strings
     *  that used to be in the result list, otherwise null
     *  @param tagId which field we are searching (gloss, headwords,
     *  synonym, etc)
     */
    private void standardSearch(final Pattern reg, final int count,
                                final int subject, final String[] selections,
                                final int tagId) throws InterruptedException {

        try {
            int matched = 0;
            int i = 0;
            for ( ; i < count; i++) {
                if( !running()) {
                    break;
                }

                String current;
                if (subject == DICTIONARY) {
                    if (tagId==FIND_GLOSS) {
                        current = (String) parent.cache.glossList.elementAt(i);
                    } else {
                        current = (String) parent.cache.headwordList.elementAt(i);
                    }
                } else if (subject == HEADWORDS) {
                    current = parent.scrollPanel.headwordAt(i);
                } else if (subject == GLOSSES) {
                    current = parent.scrollPanel.glossAt(i);
                } else /* if(subject == RESULTS_LIST) */ {
                    current =  selections[i];
                }

                Vector v;  // always initialized below
                if (tagId == FIND_HEADWORD) {
                    boolean headword = (subject==HEADWORDS)
                        || (subject==DICTIONARY)
                        || (subject==RESULTS_LIST &&
                            parent.headwordsShowing());
                    v = searchHeadWords(current, reg, headword);
                } else if (tagId == FIND_GLOSS) {
                    boolean gloss = (subject==GLOSSES) ||
                        (subject==DICTIONARY) ||
                        (subject==RESULTS_LIST && ! parent.headwordsShowing());
                    v = searchGlosses(current, reg, gloss);
                } else {
                    v = searchWholeEntry(current, reg,
                                         tagId != FIND_WHOLE_ENTRY);
                }

                // the above fill in m.col[WORD] and m.col[MATCHED]
                if (v != null) {
                    for (int j = 0, vSize = v.size(); j < vSize; j++) {
                        if (addMatchItem((MatchItem)(v.elementAt(j)), tagId,
                                         subject, current)) {
                             matched++;
                        }
                    }
                }

                if (i % 20 == 0) {
                    if (progressBar != null) {
                        progressBar.setValue(i);
                    }
                    // sleep a little occasionally (in case nonpreemptive threads)
                    if (i % 1000 == 900) {
                        Thread.sleep(10);
                    }
                }
            } // end for

            if (Dbg.TIMING) {
                long endtime=System.currentTimeMillis();
                endtime=endtime-starttime;
                Dbg.print("search took " + endtime + "ms");
            }
            finishUpSearch(i, matched);
        } catch (ArrayIndexOutOfBoundsException aobe) {
            aobe.printStackTrace();
            // assume search list changed under us
            stopSearch();
            // should probably give a more specific message.
        }
    }


    private void finishUpSearch(int progress, int matched) {
        if (progressBar != null) {
            progressBar.setValue(progress);
            progressBar.setValue(0);
        }
        printSearchStatus(leftPanel, matched + " " +
                Helper.getTranslation(matched == 1 ? SC_ITEM : SC_ITEMS) +
                ' ' + Helper.getTranslation(SC_MATCHED_TOTAL));
        validate();
        stopHelper();
    }


    /** Add a single matching item to the set of search results.
     *  @param m A match that was found. This must be non-null.
     *  @param current The unique key of the word that we are currently
     *            searching.
     */
    private boolean addMatchItem(MatchItem m, int tagId, int subject,
                                 String current)
    {
        // cdm: NB! We now put uniqueKey in myModel hash key
        if (myModel.contains((String)m.col[WORD],(String)m.col[MATCHED])) {
            return false;
        }
        DictEntry de;  // always initialized below
        if (subject==GLOSSES || (subject==DICTIONARY && tagId==FIND_GLOSS)
                || (subject==RESULTS_LIST && ! parent.headwordsShowing())) {
            GlossDictEntry ede = parent.cache.getGlossIndexEntry(current);
            de=new DictEntry();
            de.hasPics=ede.hasPics;
            // de.hasSounds=false;//ede.hasSounds;
            de.hasNote=false;//ede.hasNote;
        } else {
            de = parent.cache.getIndexEntry(current);
        }
        if (withSound.isSelected() && ! de.hasSounds) {
            return false;
        } else if (withPic.isSelected() && ! de.hasPics) {
            return false;
        } else if (withNote.isSelected() && ! de.hasNote) {
            return false;
        }
        myModel.addRow(m);
        printSearchStatus(leftPanel, Helper.getTranslation(SC_MATCHED) +
                ' ' + Helper.uniqueKeyToPrintableString(current));
        return true;
    }


    /** Do a fast search through the entire dictionary in XML for
     *  a regular expression. Matches once per entry (no duplicates).
     *  @param reg regular expression to match
     *  @param regexHasDollarOne true if limited to a certain field,
     *          false if searching the entire xml entry
     */
    public void fastSearch(final Pattern reg, final boolean regexHasDollarOne)
                        throws InterruptedException {
        if (Dbg.SEARCH) Dbg.print("Using fast search");
        try {
            FileInputStream fis = new
            FileInputStream(RelFile.MakeFileName(RelFile.dictionaryDir,
                                                 Kirrkirr.xmlFile));
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String line;
            String headWord = null;
            String headUniquifier = null;
            int i = 0;
            boolean foundInEntry = true;
            // this variable implements the current semantics of matching
            // only once per entry.  It is initially set to "true" so one
            // cannot get a match in header material before the first
            // dictionary entry is seen.
            boolean soundRestr = withSound.isSelected();
            boolean picRestr = withPic.isSelected();
            boolean noteRestr = withNote.isSelected();
            boolean restr = soundRestr || picRestr || noteRestr;
            int matched = 0;

            while ((line = br.readLine()) != null && running()) {
                MatchResult matchRes;
                if (p5m.contains(line, entryreg)) {
                    matchRes = p5m.getMatch();
                    headWord = matchRes.group(1);
                    i++;
                    foundInEntry = false;
                    headUniquifier = null; // reset it
                }
                if (uniqreg != null && p5m.contains(line, uniqreg)) {
                    matchRes = p5m.getMatch();
                    headUniquifier = matchRes.group(1);
                }
                if ( ! foundInEntry && p5m.contains(line, reg)) {
                    matchRes = p5m.getMatch();
                    String matchLine;
                    if (regexHasDollarOne) {
                        matchLine = matchRes.group(1);
                    } else {
                        matchLine = line;
                    }
                    foundInEntry = true;
                    String pWord = Helper.makeUniqueKey(headWord, headUniquifier);
                    if (restr) {
                        DictEntry de = parent.cache.getIndexEntry(pWord);
                        if ((soundRestr && ! de.hasSounds) ||
                            (picRestr && ! de.hasPics) ||
                            (noteRestr && ! de.hasNote)) {
                            continue;   // match is suppressed
                        }
                    }
                    // changed to avoid making a vector each time. cdm.
                    MatchItem m;  // always initialized below
                    if (parent.scrollPanel.headwordsShowing) {
                        //showing headword
                        m = new MatchItem();
                        //modified to show printable version
                        m.col[WORD] = Helper.uniqueKeyToPrintableString(pWord);
                        m.col[MATCHED] =  matchLine;
                        /* m.col[NOTE] = new Boolean(parent.notesMade.containsKey(pWord));
                           m.col[SOUND] =  new Boolean(de.hasSounds);
                           m.col[PICTURE] =  new Boolean(de.hasPics);*/
                        myModel.addRow(m);
                        printSearchStatus(leftPanel,
                                          Helper.getTranslation(SC_MATCHED) +
                                                  ' ' + m.col[WORD]);
                        matched++;
                    } else {
                        Vector df=parent.cache.getGlossEntry(pWord);
                        if (Dbg.SEARCH) {
                            Dbg.print(df+" found for "+pWord+ ' ' +reg);
                        }
                        for (int idx = 0, dfsize = df.size(); idx < dfsize; idx++) {
                            String gloss = ((DictField) df.elementAt(idx)).uniqueKey;  // todo: ClassCastException; it's not a String!!!!
                            if (gloss == null) continue;
                            m = new MatchItem();
                            m.col[WORD] = gloss;
                            m.col[MATCHED] = matchLine;
                            myModel.addRow(m);
                            printSearchStatus(leftPanel,
                                          Helper.getTranslation(SC_MATCHED) +
                                                  ' ' + m.col[WORD]);
                            matched++;
                        }
                    }
                } // if matched
                if (i % 200 == 0) {
                    if (progressBar != null) {
                        progressBar.setValue(i);
                    }
                    // sleep a little occasionally (in case nonpreemptive threads)
                    if (i % 1000 == 800) {
                        Thread.sleep(5);
                    }
                }
            } // while
            br.close();
            if (Dbg.TIMING) {
                long endtime=System.currentTimeMillis();
                endtime=endtime-starttime;
                Dbg.print("search took " + endtime + "ms");
            }
            finishUpSearch(i, matched);
        } catch (Exception e) {
           if (Dbg.ERROR)
               e.printStackTrace();
        }
    }


    /** Called when the user presses the "start" button.
     *  If there is already a search going, it stops that
     *  search and starts a new one. Makes a new
     *  Searcher thread and starts it.
     */
    private void startSearch() {
        printSearchStatus(leftPanel,Helper.getTranslation(SC_SEARCHING));
        if (running()) {
            stopHelper();
        }
        searcherThread = new Thread(this, "Search Panel searcher");
        searcherThread.setPriority((Thread.NORM_PRIORITY +
                        Thread.MIN_PRIORITY) / 2);
        try {
            searcherThread.setDaemon(true);     // stops on main exit
        } catch (SecurityException se) {
        }
        b_searcher = true;
        searcherThread.start();
    }

    /** Called when a search finishes or the user presses stop.
     *  (Note: doesnt stop search thread, just sets it to null,
     *  since stop is deprecated).
     */
    private void stopHelper() {
        b_searcher = false;
        searcherThread = null;
    }

    /** Whether or not a search thread is running.
     */
    private boolean running() {
        return b_searcher;
    }

    /** Runs the search thread until stopped or done,
     *  sleeping occasionally in case needs to be preempted.
     *  CDM Nov 2001: is the loop in this method actually doing anything
     *  (sensible)???
     */
    public void run() {
        Thread myThread = Thread.currentThread();
        while (searcherThread == myThread) {
            try {
                searcher();
                Thread.sleep(400);
            } catch (InterruptedException e) {
                break;
            } catch (MalformedPatternException mpe) {
                printSearchStatus(leftPanel,
                                  Helper.getTranslation(SC_BAD_REGEXP));
                parent.setStatusBar(Helper.getTranslation(SC_BAD_REGEXP));
                break;
            } catch (Exception e) {
                if (Dbg.ERROR)
                    e.printStackTrace();
            }
        }
    }


    /** Called by Stop button - provides feedback via progressLabel.
     */
    private void stopSearch() {
        stopHelper();
        printSearchStatus(leftPanel, Helper.getTranslation(SC_SEARCH_STOPPED));
        if (progressBar != null) {
            // there's only one in the big window
            progressBar.setValue(0);
        }
    }

    /** Updates the search panel label saying how
     *  many matches it has found so far, or when
     *  search is complete, etc.
     *  @param jp the JPanel to put the text border
     *  with the message on
     *  @param text the message to put
     */
    private static void printSearchStatus(final JPanel jp, final String text)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                jp.setBorder(BorderFactory.createTitledBorder(
                                Helper.getTranslation(SC_SEARCH)+": " + text));
            }
        });
    }


    /** Defines the functions for the search table in SearchPane
     */
    static class MyTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 4182394675447119244L;

        private MatchItem columnNames;
        private Vector data;
        private SearchPanel sp;


        MyTableModel(SearchPanel sp) {
            super();
            this.sp = sp;
            columnNames = new MatchItem();
            columnNames.col[SearchPanel.WORD] = Helper.getTranslation(SearchPanel.SC_WORD);
            columnNames.col[SearchPanel.MATCHED] = Helper.getTranslation(SearchPanel.SC_MATCHED);
            data = new Vector();
        }

        public int getColumnCount() {
            return SearchPanel.COLS;
        }

        public int getRowCount() {
            return data.size();
        }

        public String getColumnName(int col) {
            return (String) columnNames.col[col];
        }

        public Object getValueAt(int row, int col) {
            return ((MatchItem)data.elementAt(row)).col[col];
        }

        public boolean contains(String word, String matched){
            for (Enumeration e=data.elements(); e.hasMoreElements(); ) {
                MatchItem mi = (MatchItem) e.nextElement();
                if (mi.col[SearchPanel.WORD].equals(word)){
                    if (mi.col[SearchPanel.MATCHED].equals(matched))
                        return true;
                }
            }
            return false;
        }

        /* If the column is called uneditable, its contents can not be
         * scrolled to.  It's important that the *matched text* column can
         * be double clicked, and then allow the user to scroll thru the
         * text in the cell & even copy it.
         * This is not possible if you only return false!
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return (col==sp.MATCHED);
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            if (Dbg.SEARCH) {
                Dbg.print("Setting value at " + row + ',' + col
                                   + " to " + value + " (an instance of "
                                   + value.getClass() + ')');
            }

            /*
           if (((MatchItem)data.elementAt(0)).col[col] instanceof Integer) {
                //If we don't do something like this, the column
                //switches to contain Strings.
                try {
                    ((MatchItem)data.elementAt(row)).col[col] = new Integer((String)value);
                } catch (NumberFormatException e) {
                    if (SwingUtilities.isEventDispatchThread()) {
                        JOptionPane.showMessageDialog(sp.parent.window,
                            "The \"" + getColumnName(col)
                            + "\" column accepts only integer values.");
                    } else {
                        System.err.println("User attempted to enter non-integer"
                                       + " value (" + value
                                       + ") into an integer-only column.");
                    }
                }
            } else {
                ((MatchItem)data.elementAt(row)).col[col] = value;
            }
            */
            if (Dbg.SEARCH) {
                Dbg.print("New value of data:");
                printDebugData();
            }
        }

        public void addRow(MatchItem row) {
            data.addElement(row);
            int index = data.size() - 1;
            fireTableRowsInserted(index, index);
        }

        public synchronized void clear() {
            int oldNumRows = data.size();

            if (oldNumRows > 0) {
                data.removeAllElements();
                fireTableRowsDeleted(0, oldNumRows - 1);
            }
            if (oldNumRows != data.size()) {
                fireTableDataChanged();
            }
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        /*      public Class getColumnClass(int c) {
                  Class cl= getValueAt(0, c).getClass();
            //  System.err.println("at "+c+" class is "+cl);
            try{
            if (c==0) return Class.forName("javax.swing.JLabel");
            }catch(Exception e){}
            return cl;
            }*/

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows ; i++) {
                System.out.print("    row " + i + ':');
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + getValueAt(i,j));
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }

    } // end class MyTableModel

} // end class SearchPanel


/** MatchItems are used in the SearchPanel scrolled table to store
 *  results.  The first column holds a printable version of the word or gloss,
 *  and the second column holds the part of the entry that matched.
 */
class MatchItem {

    Object[] col;

    MatchItem() {
        col = new Object[SearchPanel.COLS];
    }

}

