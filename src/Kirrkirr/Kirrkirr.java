/** Kirrkirr is an interactive multimedia browsing
 *  environment for dictionaries (particularly targeted at indigenous
 *  languages).
 *
 *  For further information see: http://nlp.stanford.edu/kirrkirr/
 *  or contact: manning@cs.stanford.edu
 *
 *  Original versions (1.0, 1.1, 2.0) (c) Kevin Jansz 1998, 1999
 *  kjansz@hotmail.com
 *
 *  Later version 2 releases code (2.1, 2.1.x) (c) 1999, 2000, 2001
 *  Christopher Manning, Jessica Halida, Lim Hong Lee Andrew, Madhu Pawar
 *  Sng Wee Jim
 *
 *  Version 3 releases code (c) 2001, 2002
 *  Christopher Manning, Kristen Parton, Kevin Lim
 *
 *  Version 4 release code (c) 2003, 2004, 2005
 *  Christopher Manning, Chloe Kiddon
 *
 *  java Kirrkirr.Kirrkirr
 *
 *  Older versions were:
 *  Wrl.xml Wrl.clk
 *  appletviewer -J-Djava.security.policy=file:/C:/tmp/kirrkirr/kirrkirr.policy file:/C:/UNIV/kirr2/applet.html
 */
package Kirrkirr;

import Kirrkirr.util.*;
import Kirrkirr.ui.*;
import Kirrkirr.ui.data.*;
import Kirrkirr.ui.dialog.*;
import Kirrkirr.ui.panel.*;
import Kirrkirr.ui.panel.optionPanel.*;
import Kirrkirr.dictionary.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;
import java.net.*;
import java.lang.reflect.Method;

import javax.swing.*;
import javax.swing.event.*;

// added by Jessica Halida
// the following library is added to support the new help button
import javax.help.*;


/** The main class for the graphical interface Kirrkirr.  A main method
 *  is included in this class.  Many variables in it are static --
 *  it isn't built to handle multiple Kirrkirrs running.
 *
 *  @version 4.1
 */
public class Kirrkirr extends JPanel
    implements ActionListener, ChangeListener, HtmlListener {

    /////
    //  Static constants
    /////
    private static final String VERSION = "4.1";
    private static final String PROPERTIES_FILE = "kirrkirr.properties";
    public static final String DICT_PROPERTIES_FILE = "dictionary.properties";

    public static String[] args;

    /** folder names to find/save files in */
    public static final String xmlFolder = "xml";
    public static final String xslFolder = "xsl";
    /** the name of the folder for cached html files */
    public static final String htmlFolder = "html";
    public static final String usersFolder = "users";
    // public static final String tutorFolder = "tutor";
    public static final String imagesFolder="images";
    public static final String soundFolder="audio";
    public static final String iconsFolder="icons";

    /** the dictionary to read from */
    public static String xmlFile;       // accessed in dictionary cache, ProfileManager, demo, SearchPanel
    /** the .clk file to read from */
    public static String indexFile;  // referenced in ProfileManager, demo, & DictionaryCache
    /** the .clk file with the gloss/gloss index */
    public static String engIndexFile;
    /** the .xml domain file to read from*/
    public static String domainFile;
    /** the .xml domain converter file if there is one */
    public static String domainConverter;
    public static DomainConverter dc;
    public static boolean usingDomainConversion = false;

    //string constants that need to be translated
    private static final String SC_DOTS="...";
    private static final String SC_STARTING="Starting_Kirrkirr";
    private static final String SC_LOAD_GRAPH="Loading_graphical_network";
    private static final String SC_LOAD_HTML="Loading_definitions";
    private static final String SC_LOAD_NOTES="Loading_note_taking";
    private static final String SC_LOAD_MEDIA="Loading_multimedia";
    private static final String SC_LOAD_SEARCH="Loading_search_panel";
    private static final String SC_LOAD_EXPLORER="Loading_semantic_domains";
    private static final String SC_LOAD_GAME="Loading_games";
    private static final String SC_SETTING_UP="Setting_up_word_list";
    private static final String SC_COMPLETE="Startup_complete!";
    private static final String SC_KEEP="Keep";
    private static final String SC_KEEP_DESC="Keep_the_current_word_in_a_new_window";
    private static final String SC_COPY="Copy";
    private static final String SC_COPY_DESC="Copy_selected_text";
    private static final String SC_RESET="Reset";
    private static final String SC_WORD_LIST="Word_List";
    private static final String SC_RESET_LIST="Reset_Word_List";
    private static final String SC_RESET_DESC="Reset_word_list_to_original_contents";
    private static final String SC_FILE="File";
    private static final String SC_OPEN_LIST="Open_Word_List";
    private static final String SC_SAVE_LIST="Save_Word_List";
    private static final String SC_EXPORT_HTML="Export_Word_List_to_HTML";
    private static final String SC_EXIT="Exit";
    private static final String SC_MAC_EXIT="Quit";
    private static final String SC_EDIT="Edit";
    private static final String SC_CUT="Cut";
    private static final String SC_PASTE="Paste";
    private static final String SC_TOOLS="Tools";
    private static final String SC_INDEX_FILES = "Make_index_files";
    private static final String SC_XSL_TRANSFORM = "XSL_transformer";
    private static final String SC_CHARSET_REENCODE = "File_character_set_reencode";
    private static final String SC_DOMAIN_CONVERSION = "Create/edit_domain_conversion_file...";
    private static final String SC_VIEW ="View";
    private static final String SC_PREFERENCES="Preferences...";
    private static final String SC_SORT_LIST="Sort_word_list";
    private static final String SC_ALPHA="Alphabetical";
    private static final String SC_RHYME="Rhyme";
    private static final String SC_FREQUENCY="Frequency";
    private static final String SC_ORIGINAL="Original";
    private static final String SC_LIST_OPTIONS="Show_in_word_list";
    private static final String SC_NUMBER_MULTIPLES="Homograph_numbering";
    private static final String SC_SEE_SUBS="Subentries";
    private static final String SC_SEE_FREQ="Frequency";
    private static final String SC_SEE_POS="Part_of_speech";
    private static final String SC_SEE_DIALECT="Dialect";
    private static final String SC_KEEP_WORD="Keep_Current_Word";
    private static final String SC_HELP="Help";
    private static final String SC_CONTENTS="Contents...";
    private static final String SC_MAC_KIRRKIRR_HELP = "Kirrkirr_Help";
    private static final String SC_ABOUT="About";
    private static final String SC_SURE="I_am_sure";
    private static final String SC_CANCEL="Cancel";
    private static final String SC_MANY_WORDS="There_are_many_words_in_the_list";
    private static final String SC_LONG_TIME="Saving_the_list_will_take_a_long_time_and_create_a_large_file.";
    private static final String SC_SURE_Q="Are you sure you want to do this?";
    private static final String SC_WARNING="Warning";
    private static final String SC_CLIPBOARD_ERROR="Can't_access_clipboard";
    private static final String SC_SAVE_BEFORE_EXIT="Do_you_want_to_save_notes_and_settings_before_exiting?";
    private static final String SC_CONFIRM_EXIT="Confirm_Exit";
    private static final String SC_NEW_DICT="Please_choose_the_directory_containing_your_dictionary";
    private static final String SC_SET_DICT="Select";
    private static final String SC_NO_DICTINFO="There_was_a_problem_opening_or_loading_the_dictionary_specification_file";
    private static final String SC_NO_DICT="There_was_a_problem_opening_or_loading_the_dictionary";
    private static final String SC_NO_DICT_INDEX="No_dictionary.index file_was_specified_in_the_properties_file";
    private static final String SC_DICTINFO_ERROR = "Dictionary_specification_file_error";
    private static final String SC_DICT_ERROR = "Dictionary_file_error";
    private static final String SC_LOAD_NEW_DICT="Open_Dictionary";
    private static final String SC_DICTIONARY_PREPROCESSOR = "Dictionary_preprocessor";

    private static final boolean CONFIRM_EXIT = true;

    /** when saving list to html, max words allowed without an "are you sure" prompt */
    private static final int MAX_HTML_WORDS=100;

    /** macintosh classic extended look and feel class name */
    public static final String MACINTOSH_LOOK_AND_FEEL="it.unitn.ing.swing.plaf.macos.MacOSLookAndFeel";

    //for the tabbed panes
    public static final int TOPPANE = 0;
    public static final int BOTPANE = 1;

    //for 1 or 2 rhs panes
    public static int NUMPANES = 2;

    public static final int HISTORY = -1;
    public static final int SCROLLPANEL = -2;
    public static final int DICTIONARYCACHE = -3;

    public static final int GRAPH = 0;
    static final int NOTHING = 0;
    public static final int HTML = 1;
    public static final int NOTES = 2;
    static final int MEDIA = 3;
    static final int SEARCH = 4;
    public static final int EXPLORER = 5;
    static final int QUIZ = 6;
    static final int PICBROWSE = 6;
    public static final int KKPANES = 7; //num panes

    /////
    //  Help variables - added by Jessica Halida
    /////
    /** to call help.html and about.html*/
    public static final String HELP_FILE_END = ".html";
    /** private static final String help_main = "help.html";*/
    private static final String help_about = "about.html";
    /** define helpSet for Java Help File */
    //    protected static HelpSet m_hs;
    /** define Broker and the location of HelpSet file is in Help folder*/
    private static HelpBroker helpBroker;
    private static final String HELPSETNAME = "Help/Kirrkirr.hs";

    /////
    //  Global variables
    /////
    public static boolean APPLET; // = false;
    public static KirrkirrApplet demo;
    private static Dimension screenSize;

    public static DictionaryInfo dictInfo;

    /** (User/Kirrkirr) Properties for this invocation of Kirrkirr. */
    private static Properties props;

    /** left hand side scroll list - headword/gloss */
    public ScrollPanel scrollPanel;

    /** History archive for back-forward navigation */
    public static History history;

    public static DictionaryCache cache; // = null
    public static Kirrkirr kk;
    public static JFrame window;

    /** Use setStatusBar(String) to access it */
    /** cw '02: and occasionally setStatusBarIconText(String)... */
    private static StatusBar statusBar;

    //for the progress bar:
    private static ProgressDialog progress;
    private static final int PROGRESS_MAX_VAL = 30;

    //for the KirrkirrOptionPanels, each one has an index,
    //and they are all managed by the KirrkirrOptionsDialog
    public static KirrkirrOptionsDialog kirrkirrOptions;

    //for profile managing
    public static ProfileManager profileManager;  // accessed in NotesPanel
    //read in from properties file, accessed in profilemanager
    private static boolean autoLoadDefault;
    private static String defaultProfile;

    //for internationalization - used in Helper
    public static ResourceBundle lang;
    /** What language we should work in.  Set in parseArguments */
    public static String langCode;
    /** What country we should work in.  Set in parseArguments */
    public static String countryCode;

    ////
    //  Swing components
    ////
    public static JTabbedPane[] tabbedPanes;
    private static JSplitPane splitPane;
    public static KirrkirrPanel[][] KKTabs;
    private static KirrkirrPanel[] currentTabs;

                         //keep track of current tab to notify when deselected

    /* the top searchBox */
    private OneBoxPanel obp;
    public static JSplitPane centreSplit;

    public static final Color toolbarColor = (new Color(250, 220, 100)).darker();
    /* For Kirrkirr toolbar (keep, copy, switch, reset), but switch is in
       scroll list class.*/
    private static JButton keepButton, copyButton, reset;

    /* For File menu */
    private static JMenuItem openWordlist, saveWordlist, resetWordlist,
        createHTML, quit;
    /* For Edit menu */
    private static JMenuItem cut, copy, paste;
    /* For old Profile menu
       Taken out, as profile settings moved to kkoptionpanel
      private static JMenuItem loadProfile, saveProfile,
      profile_settings;*/
    /* For Options menu (sortlist, scrolloptions are in initializemenubar()).*/
    private static JMenuItem preferences;

    /* For Tools menu(indexFiles, xsltransform)*/
    private static JMenuItem indexFiles, xsltransform, charsetReencode,
		createDomainConv, preprocessor;

    /* For History menu (rest of menu created in History). */
    private static JMenuItem menuKeep;
    /* For help menu */
    private static JMenuItem contents, about;
    /* No longer a menu item, taken out.
      private static JMenuItem   write_dir;*/
    /** For the scroll options menu (Options->Word List Options).
        seeSubwords and showPoly both have accessor functions below.*/
    private static JCheckBoxMenuItem seeSubwords, polysemy, seeFreq,
        seeSpeech, seeDialect;
    /** mirrors whether polysemy is selected. Accessor fn below. */
    private boolean showPoly;
    /** For Options->Sort Word List menu */
    private static JRadioButtonMenuItem freqSort, rhymeSort, origSort,
        curHeadwordSort, alphSort, curGlossSort;
    /* disabled - too slow. posSort */
    /** Use old or new visualization -- also reffed in KirrkirrOptionsDialog */
    public static boolean oldNetwork = true;
    public static boolean oldSemanticDomains = true;

    //one rhs pane
    public static boolean oneRHSPane = false;

    //in game mode - one pane playing
    public static boolean inGamePane = false;
    public static boolean fromBottomPane = false;
    public static KirrkirrPanel oldBottomPane = null;
    public static JPanel hintPanel = new JPanel();
    public static HtmlPanel hints;
    public static int dividerSize;
    public static int dividerLoc;
    public static boolean hintsOn = false;

    private static final int TINYHEIGHT = 480;
    private static final int TINYWIDTH = 640;
    private static final int REGULARHEIGHT = 600;
    private static final int REGULARWIDTH = 800;
    private static final int REGULARMACWIDTH = 1024;
    private static final int LARGE_HEIGHT = 1024;
    // you seem to have to delete this much height on Windows.  Really.
    private static final int ALLOWHEIGHT = 80;
    private static final int ALLOWWIDTH = 20;


    /** This is the sole constructor that sets up the whole of Kirrkirr.
     */
    public Kirrkirr() {
        super();

        // For Internationalization
        Locale currentLocale = new Locale(langCode, countryCode);
        //set the language for the session. if property file is not found,
        //the default one, i.e. lang.properties, will be searched and used.
        try {
            // Rather than using ResourceBundle.getBundle, we roll our own
            // way of making a PropertyBundle, so that we can use RelFile
            // to search for the properties file, rather than using the Java
            // ClassLoader (via the CLASSPATH mechanism), which may not be
            // set up right.  This seemed easier than writing a custom
            // ClassLoader.  If our own way fails, we revert to trying
            // the standard mechanism.  Our version only looks for an
            // exact language/country match; it doesn't do backoff like the
            // standard version.
            String fname = "lang_" + currentLocale.getLanguage() + "_" +
                currentLocale.getCountry() + ".properties";
            if (Dbg.PROGRESS) Dbg.print("Loading properties " + fname);
            lang = new PropertyResourceBundle(RelFile.makeURL(fname).openConnection().getInputStream());
        } catch (IOException ioe) {
            try {
                lang = ResourceBundle.getBundle("lang", currentLocale);
            } catch(MissingResourceException mre) {
                lang = null;
                if (Dbg.ERROR)
                    Dbg.print("Missing lang resource, using default settings");
            }
        }
        progress.incrementValue();

        // Deal with screen sizing options
        int kirrkirrSize = KirrkirrPanel.NORMAL;
        String interfaceSize = props.getProperty("kirrkirr.interfaceSize");
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (interfaceSize != null) {
            String lcSize = interfaceSize.toUpperCase();
            if (lcSize.equals("TINY")) {
                kirrkirrSize = KirrkirrPanel.TINY;
            } else if (lcSize.equals("SMALL")) {
                kirrkirrSize = KirrkirrPanel.SMALL;
            } else if (lcSize.equals("LARGE")) {
                kirrkirrSize = KirrkirrPanel.LARGE;
            } else {
                kirrkirrSize = KirrkirrPanel.NORMAL;
            }
        } else {
            if (screenSize.height <= 500) {
                kirrkirrSize = KirrkirrPanel.TINY;
            } else if (screenSize.height <= 640) {
                kirrkirrSize = KirrkirrPanel.SMALL;
            } else if (screenSize.height >= 1200) {
                kirrkirrSize = KirrkirrPanel.LARGE;
            }
        }

        if (Helper.onMacOSX()) {
            KirrkirrButton.setInsetSize(KirrkirrButton.LARGE);
        } else if (kirrkirrSize <= KirrkirrPanel.SMALL) {
            KirrkirrButton.setInsetSize(KirrkirrButton.SMALL);
        }

        // init left hand side scroll list
        scrollPanel = new ScrollPanel(this, kirrkirrSize);

        if (Dbg.MEMORY){
           Dbg.memoryUsage("in Kirrkirr() before GUI initialization");
        }

        // Create the History archive (ie for back-forward storage and gui)
        history = new History(this);

        String str = props.getProperty(DirsOptionPanel.PROP_ONERHSPANE);
        if ("true".equalsIgnoreCase(str)) {
            oneRHSPane = true;
        } else if ("false".equalsIgnoreCase(str)) {
            // then believe it is what they want
        } else {
            // make oneRHSPane on tiny displays (also less mem!)
            oneRHSPane = kirrkirrSize < KirrkirrPanel.SMALL;
        }

        // init each tabbed pane
        initializeTabbedPanes(kirrkirrSize);

        /** splitPane contains the top/bottom tabbed panes,
         *  obp is the OneBoxPanel (the search toolbar).
         *  rhsPane holds both of these.
         *  centreSplit holds the headwords on the left and
         *  the rhsPane on the right.
         *  The toolbar is on top, then centreSplit, then statusBar.
         */
        if (!oneRHSPane) {
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                    tabbedPanes[TOPPANE],
                    tabbedPanes[BOTPANE]);
            //in jdk1.3 gives the top pane most of the extra space; doesnt
            //work for 1.1
            //splitPane.setResizeWeight(.9);
            // this caused problems with JDK1.1 (divider is set at 0.0!!)
            // splitPane.setDividerLocation(0.6F);
            splitPane.setOneTouchExpandable(true);
            splitPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            splitPane.setDoubleBuffered(true);
        } else {
            splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            splitPane.setTopComponent(tabbedPanes[TOPPANE]);
            splitPane.setAlignmentX(Component.LEFT_ALIGNMENT);
            splitPane.setDoubleBuffered(true);
            //tabbedPanes[TOPPANE].setAlignmentX(Component.LEFT_ALIGNMENT);
            //tabbedPanes[TOPPANE].setDoubleBuffered(true);
        }

        obp = new OneBoxPanel(this, kirrkirrSize);
        obp.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (oneRHSPane) {
        	obp.setSize(obp.getMinimumSize());
        }
        progress.incrementValue();

        JPanel rhsPane = new JPanel();
        rhsPane.setLayout(new BoxLayout(rhsPane, BoxLayout.Y_AXIS));
        rhsPane.add(Box.createVerticalStrut(2));
        rhsPane.add(obp);
        rhsPane.add(Box.createVerticalStrut(2));
        /*
        if (oneRHSPane) {
        	rhsPane.add(tabbedPanes[TOPPANE]);
        }
        else*/
        	rhsPane.add(splitPane);

        centreSplit =
            new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                           scrollPanel.getPanel(), rhsPane);
        centreSplit.setOneTouchExpandable(true);

        statusBar = new StatusBar("dictionary.gif");
        // create the KirrkirrOptionsDialog (don't setup till user opens it)
        kirrkirrOptions = new KirrkirrOptionsDialog(this.window);
        for (int i = 0; i < KKPANES; i++) {
            KirrkirrOptionPanel kkop = KKTabs[TOPPANE][i].getOptionPanel();
            if( kkop != null) {
                kirrkirrOptions.addOptionPanel(kkop);
            }
        }
        progress.incrementValue();

        //-- The default option panels -- (Note kk not set yet!)
        kirrkirrOptions.addOptionPanel(new LookAndFeelOptionPanel(this));
        // if (oldNetwork) {
        //   kirrkirrOptions.addOptionPanel(new FunPanelOptionPanel(this,
        //               (OldGraphPanel) KKTabs[TOPPANE][GRAPH]));
        // }
        kirrkirrOptions.addOptionPanel(new DirsOptionPanel());

        // This is the whole upper toolbar, with the history toolbar,
        // kirrkirr toolbar, and mainlist toolbar.
        JPanel toolPanel = initializeToolPanel();

        // put the components in the top JPanel Kirrkirr
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // cdm june 2000: unless I left aligned these, otherwise the word
        // scrolled list displayed wierdly with lots of blank space to left
        toolPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centreSplit.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(toolPanel);
        add(centreSplit);
        add(statusBar);
        if (kirrkirrSize <= KirrkirrPanel.TINY) {
            if (false) {
              if (screenSize.width < TINYWIDTH + ALLOWWIDTH) {
                screenSize.width = screenSize.width - ALLOWWIDTH;
              } else {
                screenSize.width = TINYWIDTH;
              }
            } else {
              screenSize.width = TINYWIDTH - ALLOWWIDTH;
            }
            if (false) {
              if (screenSize.height < TINYHEIGHT + ALLOWHEIGHT) {
                  screenSize.height = screenSize.height - ALLOWHEIGHT;
              } else {
                screenSize.height = TINYHEIGHT;
              }
            } else {
              screenSize.height = TINYHEIGHT - ALLOWHEIGHT;
            }
        } else {
            if (Helper.onMacOSX()) {
                if (screenSize.width < REGULARMACWIDTH + ALLOWWIDTH) {
                    screenSize.width = screenSize.width - ALLOWWIDTH;
                } else {
                    screenSize.width = REGULARMACWIDTH;
                }
            } else {
                if (screenSize.width < REGULARWIDTH + ALLOWWIDTH) {
                    screenSize.width = screenSize.width - ALLOWWIDTH;
                } else {
                    screenSize.width = REGULARWIDTH;
               }
            }
            if (screenSize.height < REGULARHEIGHT + ALLOWHEIGHT) {
                screenSize.height = screenSize.height - ALLOWHEIGHT;
            } else if (kirrkirrSize >= KirrkirrPanel.LARGE) {
                screenSize.height = LARGE_HEIGHT;
            } else {
                screenSize.height = REGULARHEIGHT;
            }
        }
        setPreferredSize(screenSize);
        validate();
        progress.incrementValue();

    }


    // --------- swing initialization functions --------------------


    public boolean isOneRHSPane() { return oneRHSPane; }

    /**
     * Initializes the tabbed panes, which
     * form the main part of Kirrkirr.  Depending on oneRHSPane,
     * either both top and bottom tabbed panes or one set (top) are set up.
     * Effective size of top pane is at least SMALL if only one set.
     * Effective size of bottom pane is maximally SMALL.
     *
     * @param kirrkirrSize The rough size of the UI
     */
    private void initializeTabbedPanes(int kirrkirrSize) {
        int onePaneSize = (oneRHSPane && kirrkirrSize < KirrkirrPanel.SMALL) ?
                KirrkirrPanel.SMALL : kirrkirrSize;
        int bottomSize;
        if (kirrkirrSize >= KirrkirrPanel.LARGE) {
            bottomSize = KirrkirrPanel.NORMAL;
        } else if (kirrkirrSize > KirrkirrPanel.SMALL) {
            bottomSize = KirrkirrPanel.SMALL;
        } else {
            bottomSize = kirrkirrSize;
        }
        if (Dbg.PROGRESS) {
            Dbg.print("KK size is " + kirrkirrSize);
            if (oneRHSPane) {
                Dbg.print("Because oneRHSPane, effective size is " +
                        onePaneSize);
            } else {
                Dbg.print("(Shrunk) bottom panel size is " + bottomSize);
            }
        }

    	if (oneRHSPane) {
    		NUMPANES = 1;
    		tabbedPanes = new JTabbedPane[] {new JTabbedPane()};
    		KKTabs = new KirrkirrPanel[][] {new KirrkirrPanel[KKPANES]};
    		currentTabs = new KirrkirrPanel[] {null};
    	} else {
    		NUMPANES = 2;
    		tabbedPanes = new JTabbedPane[] { new JTabbedPane(),
                    new JTabbedPane(/* no! JTabbedPane.BOTTOM */) };
    				//referenced in History
    		KKTabs = new KirrkirrPanel[][] { new KirrkirrPanel[KKPANES],
                    new KirrkirrPanel[KKPANES] };
    		currentTabs = new KirrkirrPanel[] {null, null};
    	}

    	progress.incrementValue(Helper.getTranslation(SC_LOAD_GRAPH));

        if (oldNetwork) {
            KKTabs[TOPPANE][GRAPH] = new OldGraphPanel(this, window, true,
                                                    onePaneSize);
            if (!oneRHSPane) {
                KKTabs[BOTPANE][GRAPH] = new OldGraphPanel(this, window, false,
                                                    bottomSize);
            }
     	} else {
            KKTabs[TOPPANE][GRAPH] = new GraphPanel(this, window, true,
                                                    onePaneSize);
            if (!oneRHSPane)
            	KKTabs[BOTPANE][GRAPH] = new GraphPanel(this, window, false,
                                                    bottomSize);
        }
        progress.incrementValue();

        progress.incrementValue(Helper.getTranslation(SC_LOAD_HTML));
        KKTabs[TOPPANE][HTML] = new HtmlPanel(this,this, htmlFolder);
        progress.incrementValue();
        if (!oneRHSPane) KKTabs[BOTPANE][HTML] = new HtmlPanel(this,this, htmlFolder);
        progress.incrementValue();

        progress.incrementValue(Helper.getTranslation(SC_LOAD_NOTES));
        KKTabs[TOPPANE][NOTES] = new NotesPanel(this);
        progress.incrementValue();
        if (!oneRHSPane) KKTabs[BOTPANE][NOTES] = new NotesPanel(this);
        progress.incrementValue();

        progress.incrementValue(Helper.getTranslation(SC_LOAD_MEDIA));
        KKTabs[TOPPANE][MEDIA] = new MediaPanel(this, window, onePaneSize);
        progress.incrementValue();
        if (!oneRHSPane) {
            KKTabs[BOTPANE][MEDIA] = new MediaPanel(this, window, bottomSize);
        }

        progress.incrementValue(Helper.getTranslation(SC_LOAD_SEARCH));
        KKTabs[TOPPANE][SEARCH] = new SearchPanel(this, onePaneSize);
        progress.incrementValue();
        if (!oneRHSPane) {
            KKTabs[BOTPANE][SEARCH] = new SearchPanel(this,
                                                      bottomSize);
        }
        progress.incrementValue();

        progress.incrementValue(Helper.getTranslation(SC_LOAD_EXPLORER));
        boolean disableDomains = false;
        if (oldSemanticDomains) {
            KKTabs[TOPPANE][EXPLORER] = new SemanticExplorerPanel(this, window,
                                                      onePaneSize);
            progress.incrementValue();
            if (!oneRHSPane) KKTabs[BOTPANE][EXPLORER] = new SemanticExplorerPanel(this,window,
                                                        bottomSize);
        } else {
            NewSemanticPanel nsp = new NewSemanticPanel(this, window,
                                                  onePaneSize, domainFile);
            KKTabs[TOPPANE][EXPLORER] = nsp;
            if (nsp.hasNoContent()) {
                disableDomains = true;
            }
            progress.incrementValue();

            if ( ! oneRHSPane) {
                KKTabs[BOTPANE][EXPLORER] = new NewSemanticPanel(this, window,
                                                  bottomSize, domainFile);
            }
        }
        progress.incrementValue();

        progress.incrementValue(Helper.getTranslation(SC_LOAD_GAME));
        // will get shorter form of name for small screen
        KKTabs[TOPPANE][QUIZ] = new GameSelectPanel(this, onePaneSize);
        progress.incrementValue();
        if (!oneRHSPane) {
            KKTabs[BOTPANE][QUIZ] = new GameSelectPanel(this, bottomSize);
        }
        progress.incrementValue();

        for (int j = TOPPANE; j < NUMPANES; j++) {
            for (int k = 0; k < KKPANES; k++) {
                tabbedPanes[j].insertTab(KKTabs[j][k].getName(), null, KKTabs[j][k], KKTabs[j][k].getTabRollover(), k);
            }
            if (disableDomains) {
                tabbedPanes[j].setEnabledAt(EXPLORER, false);
            }
            tabbedPanes[j].setBorder(null);
            tabbedPanes[j].addChangeListener(this);
            tabbedPanes[j].setPreferredSize(new Dimension(400, 200));
        }

        if ( ! oneRHSPane) {
        	tabbedPanes[BOTPANE].setSelectedIndex(HTML);
        	currentTabs[BOTPANE] = KKTabs[BOTPANE][HTML];
        }
        tabbedPanes[TOPPANE].setSelectedIndex(GRAPH);
        currentTabs[TOPPANE] = KKTabs[TOPPANE][GRAPH];

        hints = new HtmlPanel(this, this, htmlFolder);

        progress.incrementValue();
    }


    /**
     *  The toolPanel contains three toolbars. The history toolbar
     *  has back and forward buttons. The Kirrkirr toolbar has
     *  Keep and Copy buttons. The list toolbar has reset main list
     *  and save list as html buttons. <p>
     *  Note at present that the "toolbar" is actually a JPanel containing
     *  toolbars: it would give a more standard UI if these different
     *  toolbar parts were incorporated into one JToolBar, with separators,
     *  etc. (see the Java Look and Feel guidelines).  Though see the
     *  comment below claiming a Swing bug.
     *
     *  @return The toolbar panel
     */
    private JPanel initializeToolPanel()
    {
        JToolBar tb_history = history.getHistoryToolBar();
        tb_history.setAlignmentX(Component.LEFT_ALIGNMENT);
        tb_history.setBorderPainted(false);
        tb_history.setBackground(toolbarColor);

        JToolBar tb_kirrkirr = initializeKeepCopyToolBar();
        tb_kirrkirr.setAlignmentX(Component.LEFT_ALIGNMENT);
        tb_kirrkirr.setBorderPainted(false);
        tb_kirrkirr.setBackground(toolbarColor);

        JToolBar tb_listfn=initializeMainListToolbar();
        tb_listfn.setAlignmentX(Component.LEFT_ALIGNMENT);
        tb_listfn.setBorderPainted(false);
        tb_listfn.setBackground(toolbarColor);

        // Any toolbars will need to be added to this JPanel so that they
        // are aligned properly - LEFT_ALIGNMENT is ignored (swing bug) and
        // the toolbar will be centre aligned in the window if added directly
        // to the main Kirrkirr Panel.
        JPanel toolPanel = new JPanel();
        toolPanel.setOpaque(true);
        toolPanel.setLayout(new BoxLayout(toolPanel, BoxLayout.X_AXIS));
        toolPanel.setBackground(toolbarColor);
        toolPanel.add(Box.createHorizontalStrut(2));
        toolPanel.add(tb_history);
        toolPanel.add(Box.createHorizontalStrut(6));
        toolPanel.add(tb_kirrkirr);
        toolPanel.add(Box.createHorizontalStrut(6));
        toolPanel.add(tb_listfn);
        toolPanel.add(Box.createGlue());
        return toolPanel;
    }

    /**
     *  initializes the toolbar with the Keep and Copy buttons.
     *  @return a Keep and Copy toolbar
     */
    private JToolBar initializeKeepCopyToolBar() {
        JPanel p_toolbar = new JPanel();
        p_toolbar.setLayout(new BoxLayout(p_toolbar, BoxLayout.X_AXIS));

        keepButton = new KirrkirrButton(Helper.getTranslation(SC_KEEP), "keep.gif",this);
        keepButton.setPressedIcon(RelFile.makeImageIcon("keepDown.gif",false));
        keepButton.setRolloverIcon(RelFile.makeImageIcon("keepRollover.gif",false));
        keepButton.setRolloverEnabled(true);
        keepButton.setName(Helper.getTranslation(SC_KEEP_WORD));
        keepButton.setToolTipText(Helper.getTranslation(SC_KEEP_DESC));
        // keepButton.setEnabled(true);
        p_toolbar.add(keepButton);

        copyButton = new KirrkirrButton(SC_COPY, "copy.gif", this);
        copyButton.setPressedIcon(RelFile.makeImageIcon("copyDown.gif",false));
        copyButton.setRolloverIcon(RelFile.makeImageIcon("copyRollover.gif",false));
        copyButton.setRolloverEnabled(true);
        copyButton.setName(Helper.getTranslation(SC_COPY));
        copyButton.setToolTipText(Helper.getTranslation(SC_COPY_DESC));
        // copyButton.setEnabled(true);
        p_toolbar.add(copyButton);
        p_toolbar.setBackground(toolbarColor);

        JToolBar tb_kirrkirr = new JToolBar();
        tb_kirrkirr.setFloatable(false);
        tb_kirrkirr.add(p_toolbar);
        return(tb_kirrkirr);
    }

    /** Initializes the toolbar with the "Reset Word List"
     *  and "List to Gloss/Headword" buttons.
     */
    private JToolBar initializeMainListToolbar()
    {
        // This is the toolbar with "word list:" "switch" and "reset"
        JPanel mainListToolbar = new JPanel();
        mainListToolbar.setLayout(new BoxLayout(mainListToolbar,
                                                BoxLayout.X_AXIS));
        mainListToolbar.setBackground(toolbarColor);

        JLabel wl = new JLabel(Helper.getTranslation(SC_WORD_LIST) + ": ");
        wl.setForeground(Color.black);
        reset=new KirrkirrButton(SC_RESET,this);
        reset.setToolTipText(Helper.getTranslation(SC_RESET_DESC));


        mainListToolbar.add(wl);
        mainListToolbar.add(scrollPanel.switchListButton);
        mainListToolbar.add(Box.createHorizontalStrut(2));
        mainListToolbar.add(reset);


        JToolBar tb_kirrkirr = new JToolBar();
        tb_kirrkirr.setFloatable(false);
        tb_kirrkirr.add(mainListToolbar);
        return tb_kirrkirr;
    }


    /**
     * Initializes the menu bar at the top of kirrkirr (file/edit/view/etc).
     *
     * @param f Frame that menu is attached to
     */
    public void initializeMenuBar(JFrame f) {
        JMenuBar menubar = new JMenuBar();
        int keyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();


        menubar.setAlignmentX(LEFT_ALIGNMENT);
        menubar.setAlignmentY(TOP_ALIGNMENT);

        //////////FILE MENU: //////////////////////////////
        JMenu file = new JMenu(Helper.getTranslation(SC_FILE));
        menubar.add(file);

        JMenuItem loadNewDict = new JMenuItem(Helper.getTranslation(SC_LOAD_NEW_DICT) +
                                    SC_DOTS);
        loadNewDict.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, keyMask));
        file.add(loadNewDict);
        file.add(new JSeparator());

        openWordlist = new JMenuItem(Helper.getTranslation(SC_OPEN_LIST) +
                                     SC_DOTS);
        file.add(openWordlist);
        saveWordlist = new JMenuItem(Helper.getTranslation(SC_SAVE_LIST) +
                                     SC_DOTS);
        file.add(saveWordlist);
        resetWordlist = new JMenuItem(Helper.getTranslation(SC_RESET_LIST));
        file.add(resetWordlist);
        file.add(new JSeparator());

        createHTML = new JMenuItem(Helper.getTranslation(SC_EXPORT_HTML));
        file.add(createHTML);

        if (Helper.onMacOSX()) {
            // Initialize handling special OS X menus
            macOSXRegistration();
        } else {
            // Add Quit menu item.
            // There's no quit item on the file menu in Mac OS X because it
            // is on the separate application menu
            file.add(new JSeparator());
            String quitter = (Helper.onAMac()) ? SC_MAC_EXIT: SC_EXIT;
            quit = new JMenuItem(Helper.getTranslation(quitter));
            file.add(quit);
        }

        openWordlist.addActionListener(this);
        saveWordlist.addActionListener(this);
        resetWordlist.addActionListener(this);
        createHTML.addActionListener(this);
        loadNewDict.addActionListener(new LoadDictDir());
        if (quit != null) {
          // it's null on Mac OS X
          quit.addActionListener(this);
        }


        /////////////Edit Menu/////////////////////////////////////////////////
        JMenu edit = new JMenu(Helper.getTranslation(SC_EDIT));
        menubar.add(edit);
        cut = new JMenuItem(Helper.getTranslation(SC_CUT));
        edit.add(cut);
        copy = new JMenuItem(Helper.getTranslation(SC_COPY));
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, keyMask));

        edit.add(copy);
        paste = new JMenuItem(Helper.getTranslation(SC_PASTE));
        paste.setEnabled(false);
        edit.add(paste);
        menuKeep = new JMenuItem(Helper.getTranslation(SC_KEEP_WORD));
        edit.add(menuKeep);

        cut.addActionListener(this);
        copy.addActionListener(this);
        paste.addActionListener(this);

        ///////////View Menu/////////////////////////////////////////////////
        JMenu view = new JMenu(Helper.getTranslation(SC_VIEW));
        menubar.add(view);

        JMenu sortList = new JMenu(Helper.getTranslation(SC_SORT_LIST));
        ButtonGroup sort_g = new ButtonGroup();
        sortList.add(alphSort = new JRadioButtonMenuItem(Helper.getTranslation(SC_ALPHA)));
        alphSort.addActionListener(this);
        sort_g.add(alphSort);
        /* disabled - too slow
        sortList.add(posSort = new JRadioButtonMenuItem(Helper.getTranslation(SC_POS)));
          posSort.addActionListener(this);
          sort_g.add(posSort);*/
        sortList.add(rhymeSort = new JRadioButtonMenuItem(Helper.getTranslation(SC_RHYME)));
        rhymeSort.addActionListener(this);
        sort_g.add(rhymeSort);
        sortList.add(freqSort = new JRadioButtonMenuItem(Helper.getTranslation(SC_FREQUENCY)));
        freqSort.addActionListener(this);
        sort_g.add(freqSort);
        sortList.add(origSort = new JRadioButtonMenuItem(Helper.getTranslation(SC_ORIGINAL)+"/"
                                                          +Helper.getTranslation(SC_RESET)));

        origSort.addActionListener(this);
        sort_g.add(origSort);
        origSort.setSelected(true);

        view.add(sortList);
        curHeadwordSort=origSort;
        curGlossSort=origSort;

        JMenu wordListOptions = new JMenu(Helper.getTranslation(SC_LIST_OPTIONS));
        wordListOptions.add(polysemy = new JCheckBoxMenuItem(Helper.getTranslation(SC_NUMBER_MULTIPLES)));
        wordListOptions.add(seeSubwords = new JCheckBoxMenuItem(Helper.getTranslation(SC_SEE_SUBS)));
        wordListOptions.add(seeFreq = new JCheckBoxMenuItem(Helper.getTranslation(SC_SEE_FREQ)));
        wordListOptions.add(seeSpeech = new JCheckBoxMenuItem(Helper.getTranslation(SC_SEE_POS)));
        wordListOptions.add(seeDialect = new JCheckBoxMenuItem(Helper.getTranslation(SC_SEE_DIALECT)));
        seeSpeech.setState(false);
        seeSubwords.setState(true);
        seeFreq.setState(false);
        seeDialect.setState(false);
        polysemy.addActionListener(this);
        seeSubwords.addActionListener(this);
        seeFreq.addActionListener(this);
        seeSpeech.addActionListener(this);
        seeDialect.addActionListener(this);
        view.add(wordListOptions);

        ////////////////Tools Menu////////////////////////////////////////////

        JMenu tools = new JMenu(Helper.getTranslation(SC_TOOLS));
        menubar.add(tools);

        indexFiles = new JMenuItem(Helper.getTranslation(SC_INDEX_FILES));
        tools.add(indexFiles);

        xsltransform = new JMenuItem(Helper.getTranslation(SC_XSL_TRANSFORM));
        tools.add(xsltransform);

        charsetReencode = new JMenuItem(Helper.getTranslation(SC_CHARSET_REENCODE));
        tools.add(charsetReencode);

        createDomainConv = new JMenuItem(Helper.getTranslation(SC_DOMAIN_CONVERSION));
        tools.add(createDomainConv);

        preprocessor = new JMenuItem(Helper.getTranslation(SC_DICTIONARY_PREPROCESSOR));
        tools.add(preprocessor);

        indexFiles.addActionListener(this);
        xsltransform.addActionListener(this);
        charsetReencode.addActionListener(this);
        createDomainConv.addActionListener(this);
        preprocessor.addActionListener(this);

        if ( ! Helper.onMacOSX()) {
            // The preferences item appears on the application menu in OS X
            // For Windows Vista, the standard place to put options is on the Tools menu.

            tools.add(new JSeparator());

            //profile view
            /*saveProfile = new JMenuItem(Helper.getTranslation(SC_SAVE_PROFILE));
              view.add(saveProfile);
              loadProfile = new JMenuItem(Helper.getTranslation(SC_LOAD_PROFILE));
              view.add(loadProfile);
              profile_settings = new JMenuItem(Helper.getTranslation(SC_PROFILE_SETTINGS));
              view.add(profile_settings);
              saveProfile.addActionListener(this);
             loadProfile.addActionListener(this);
             profile_settings.addActionListener(this);
             view.add(new JSeparator());*/

            preferences = new JMenuItem(Helper.getTranslation(SC_PREFERENCES));
            tools.add(preferences);
            preferences.addActionListener(this);
        }




        ////////////////History Menu//////////////////////////////////////////
        //the History object has control over it's menu
        JMenu historyMenu = history.getHistoryMenu();
        //menuKeep = new JMenuItem(Helper.getTranslation(SC_KEEP_WORD));
        //historyMenu.add(menuKeep);
        //menuKeep.addActionListener(this);

        menubar.add(historyMenu);
        // menubar.add(Box.createHorizontalGlue());
        menubar.add(Box.createHorizontalStrut(30));

        // added by Jessica Halida
        // add new button
        JMenu help = new JMenu(Helper.getTranslation(SC_HELP));
        menubar.add(help);
        // menubar.add(Box.createHorizontalStrut(50));
        if (Helper.onMacOSX()) {
            help.add(contents = new JMenuItem(Helper.getTranslation(SC_MAC_KIRRKIRR_HELP)));
            // The about item appears on the application menu in OS X
        } else {
            help.add(contents = new JMenuItem(Helper.getTranslation(SC_CONTENTS)));
            help.add(about = new JMenuItem(Helper.getTranslation(SC_ABOUT)));
            about.addActionListener(this);
        }
        // added by Jessica Halida
        // enable help button, for "contents" option by calling the HelpBroker
        if (createHelp()) {
            helpBroker.enableHelpOnButton(contents,"help",null);
            contents.addActionListener(this);
        } else {
            contents.setEnabled(false);
            if (Dbg.ERROR) Dbg.print("Couldn't create JavaHelp");
        }
        if ( ! Helper.onMacOSX()) {
          // so it doesn't stick outside the Kirrkirr window
          // breaks and unnecessary for MacOSX when in menu bar
          help.setAlignmentX(Component.RIGHT_ALIGNMENT);
          contents.setAlignmentX(Component.RIGHT_ALIGNMENT);
          about.setAlignmentX(Component.RIGHT_ALIGNMENT);
        }

        // show it!
        window.setJMenuBar(menubar);
    }

    // =============== Methods - in approximate alphabetical order ============

    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        if (obj == quit) {
            quitKirrkirr();
            /* No longer menu items, taken out.
               } else if (obj == saveProfile) {
                 profileManager.saveProfile();
               } else if (obj == loadProfile) {
                 profileManager.loadProfile();
               } else if (obj == profile_settings) {
                 profileManager.changeSetting();
               } else if (obj == write_dir) {
                 RelFile.editWriteDirectory();   */
        } else if(obj == indexFiles) {
            showIndexMakerDialog(false);
        } else if(obj == xsltransform) {
            showXSLTransformDialog();
        } else if (obj == charsetReencode) {
            showEncodingDialog();
        } else if (obj == createDomainConv) {
        	showDomainConversionDialog(true);
        } else if (obj == preprocessor) {
        	showPreprocessor(false);
        } else if(obj == createHTML) {
            convertListToHtml();
        } else if (obj==openWordlist) {
            cache.openKrr();
        } else if (obj==saveWordlist) {
            saveToKrr();
        } else if (obj == preferences) {
            displayPreferences();
        } else if (obj == polysemy) {
            showPoly = polysemy.isSelected();
            scrollPanel.setAttribute(ScrollPanel.SEE_POLY,showPoly);
            for (int j = TOPPANE; j < NUMPANES; j++) {
                KirrkirrPanel currentPanel =  (KirrkirrPanel) tabbedPanes[j].getSelectedComponent();
                currentPanel.polysemyUpdate();
            }
            scrollPanel.repaint();
        } else if (obj==seeSpeech) {
            scrollPanel.setAttribute(ScrollPanel.SEE_POS,seeSpeech.isSelected());
            scrollPanel.repaint();
        } else if (obj==seeDialect) {
            scrollPanel.setAttribute(ScrollPanel.SEE_DIALECT,seeDialect.isSelected());
            scrollPanel.repaint();
        } else if (obj==seeFreq) {
            scrollPanel.setAttribute(ScrollPanel.SEE_FREQ,seeFreq.isSelected());
            scrollPanel.repaint();
        } else if (obj==seeSubwords) {
            scrollPanel.setAttribute(ScrollPanel.SEE_SUB,seeSubwords.isSelected());
            scrollPanel.refreshWords(ScrollPanel.SEE_SUB);
        } else if (obj == keepButton || obj == menuKeep) {
            openHtmlDialog();
        } else if (obj == copyButton || obj == copy) {
            copyTextToClipboard(false);
        } else if (obj == cut) {
            copyTextToClipboard(true);
        } else if (obj == reset || obj == resetWordlist) {
          scrollPanel.resetWordsGUI(null);
          turnOnSubWordsDisplay();
            /*} else if (obj==htmlconvert) {
            convertListToHtml();*/
        } else if (obj == contents) {
            // added by Jessica Halida
            // since openHelp function is replaced by calling the HelpSet
            // repaint it to refresh the applet after the button being clicked
            // openHelp(help_main);
            scrollPanel.repaint();
        } else if (obj == about) {
            displayAbout();
        } else if ((obj == alphSort) || (obj == freqSort) || (obj == rhymeSort)){
                   //disabled || (obj==posSort)) {
            Helper.setCursor(window, true);
            // this was Arrays.sort() but the Mac didn't like it...
            if (obj == alphSort) {
                scrollPanel.alphaSort();
            } else if (obj == freqSort) {
                scrollPanel.freqSort();
            } else if (obj == rhymeSort) {
                scrollPanel.rhymeSort();
            }
            if (scrollPanel.headwordsShowing)
                curHeadwordSort=(JRadioButtonMenuItem)obj;
            else
                curGlossSort=(JRadioButtonMenuItem)obj;
            // had System.gc(), but unnecessary?
            Helper.setCursor(window, false);
        } else if (obj == origSort) {
            scrollPanel.resetWordsGUI(null);
            if (scrollPanel.headwordsShowing)
                curHeadwordSort=(JRadioButtonMenuItem)obj;
            else
                curGlossSort=(JRadioButtonMenuItem)obj;
        }
    }

    public void displayAbout() {
        new HtmlDialog(this, htmlFolder, HtmlDialog.NO_NOTES, help_about);
        //openHelp(help_about);
        // cdm July 2007: I'm not sure if/why the below line is needed....
        scrollPanel.repaint();
    }

    public void displayPreferences() {
        kirrkirrOptions.setup();
        kirrkirrOptions.pack();
        kirrkirrOptions.setVisible(true);
    }

    /** Use to turn on display of subwords.
     *  This can happen not under user control for options that effectively
     * turn them on (like searches on whole dictionary).
     */
    public void turnOnSubWordsDisplay() {
        seeSubwords.setState(true);
        scrollPanel.setAttribute(ScrollPanel.SEE_SUB,seeSubwords.isSelected());
    }

    /** Says whether the Headword headwords showing.
     *  @return true = Headword headwords are showing; false = gloss glosses
     */
    public boolean headwordsShowing() {
        return scrollPanel.headwordsShowing;
    }

    public int wordListSize(){
        return scrollPanel.size();
    }

    public int glossListSize(){
        return scrollPanel.glossesSize();
    }

    public int headwordsListSize(){
        return scrollPanel.headwordsSize();
    }


    /** Writes the value into the key of the Properties file.
     *  This instantaneously updates the file that holds properties.
     *  2003 change: This now doesn't try to interact with the
     *  System.getProperties properties, but maintains a separate set, in
     *  line with Properties in JDK1.3+.
     *
     *  @param key the key in the propertiePs file to update
     *  @param value the new value for the key
     */
    public void changeProperty(String key, String value) {
        try {
            PropertiesUtils.changeProperty(props,
                    RelFile.MakeWriteFileName(null, PROPERTIES_FILE),
                    key, value);
            if (Dbg.VERBOSE) Dbg.print("Properties: changed " + key + " to "
                                       + value);
        } catch (Exception e) {
            if (Dbg.ERROR)
                Helper.handleException(e);
            setStatusBar("Couldn't save properties file.");
        }
    }


    /** Writes the value into the key of the Properties file.
     *  This is just a simple helper for setting boolean-valued properties.
     *  This instantaneously updates the file that holds properties.
     *  2003 change: This now doesn't try to interact with the
     *  System.getProperties properties, but maintains a separate set, in
     *  line with Properties in JDK1.3+.
     *
     *  @param key the key in the propertiePs file to update
     *  @param val the new value for the key
     */
    public void changeProperty(String key, boolean val) {
        changeProperty(key, val ? "true": "false");
    }


    public String getProperty(String key) {
        return props.getProperty(key);
    }


    private void saveToKrr() {
        int size=scrollPanel.size();
        if (size <= 0) { return; }
        JFileChooser fileChooser = new JFileChooser(new File(RelFile.WRITE_DIRECTORY));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle(Helper.getTranslation(SC_SAVE_LIST));
        int userChoice = fileChooser.showDialog(Kirrkirr.window, Helper.getTranslation(SC_SAVE_LIST));
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            String userFile = fileChooser.getSelectedFile().getName();
            // String userDir=fileChooser.getSelectedFile().getParent();
            cache.saveAsKrr(scrollPanel.getWords(),userFile);
        }
    }

    /** Convert the current contents
     *  of the headwords list to an html file. Prompts
     *  if there are more than MAX_HTML_WORDS in list.
     *  Called by a button in advanced search panel.
     */
    private void convertListToHtml() {
        int size=scrollPanel.size();
        if (size<=0) return;
        int choice=0;
        if (size > MAX_HTML_WORDS) {
            String[] options = {Helper.getTranslation(SC_SURE),
                              Helper.getTranslation(SC_CANCEL)};
            String msg = Helper.getTranslation(SC_MANY_WORDS) +
                                  " ("+size+").\n" +
                                  Helper.getTranslation(SC_LONG_TIME)+"\n"+
                                  Helper.getTranslation(SC_SURE_Q);
            choice=JOptionPane.showOptionDialog(null, msg,
                                        SC_WARNING, JOptionPane.DEFAULT_OPTION,
                                        JOptionPane.WARNING_MESSAGE, null,
                                        options, options[1]);
        }
        if (choice == 0) {
            String name = cache.generateHTML(scrollPanel.getWords());
            //if (name!=null) cache.saveAsKrr(scrollPanel.getWords(),name);
        }
    }


    /** "Copy" method - try to copy text from the children KirrkirrPanel's
     *  If neither of the components had text selected that could be copied,
     *  and there isn't anything selected in the OneBoxPanel,
     *  then copy the current word to the clipboard.
     *  Problem: disprefer GraphPanel to text panel (since there's always a
     *  selected word in the GraphPanel).  For the moment, I've just removed
     *  the GraphPanel copyText().
     *  @param isCut true if this is a cut operation (selected text is deleted)
     */
    private void copyTextToClipboard(boolean isCut) {
        int copied;

        if (Dbg.CUTPASTE) Dbg.print("Doing cut/copy; isCut: " + isCut);
        for (int j = TOPPANE; j < NUMPANES; j++) {
            KirrkirrPanel currentPanel =
                (KirrkirrPanel) tabbedPanes[j].getSelectedComponent();
            copied = currentPanel.copyText(isCut);
            if (copied != 0) {
                return;
            }
        }

        copied = obp.copyText(isCut);
        if (copied != 0) {
            return;
        }

        String word = scrollPanel.getSelectedWord();
        if (Dbg.CUTPASTE)
            if (word != null)
                Dbg.print("ScrollPanel cut/copy; selected is " +
                          word.length() + " chars\n  " + word);
            else
                Dbg.print("ScrollPanel cut/copy; word is null");
        if (word != null) {
            putStringInClipboard(word);
        }
    }


    // added by Jessica Halida
    private boolean createHelp() {
        //      ClassLoader loader = this.getClass().getClassLoader();
        //      use default class loader = null
        try {
            // This assumes the helpset is in a .jar file with Kirrkirr...
            URL url = HelpSet.findHelpSet(null, HELPSETNAME);
            if (url == null) {
                return false;
            } else {
                HelpSet m_hs = new HelpSet(null, url);
                helpBroker = m_hs.createHelpBroker();
                helpBroker.enableHelpKey(window.getRootPane(),"help", m_hs);
                return true;
            }
        } catch (Exception ex) {
            if (Dbg.ERROR)
                {
                    Dbg.print("Kirrkirr: createHelp");
                    ex.printStackTrace();
                }
            return false;
        }
    }

    public void disableGlossList()
    {
        scrollPanel.disableGlossList();
        //also remove any buttons/menu items having to do with gloss
    }

    public void enableGlossList()
    {
        for (int i = TOPPANE; i < NUMPANES; i++) {
            for (int j=0;j<KKPANES;j++) {
                if(KKTabs[i][j]!=null)
                    KKTabs[i][j].scrollPanelChanged(true);
            }
        }
        rhymeSort.setEnabled(false);
        rhymeSort.setSelected(false);
        //possort disabled
        //posSort.setEnabled(false);
        //posSort.setSelected(false);
        //freqsort is enabled
        freqSort.setSelected(false);
        //alphasort is enabled
        alphSort.setSelected(false);
        //origsort is enabled
        origSort.setSelected(false);
        //whichever one is currently in effect is selected
        curGlossSort.setSelected(true);

        polysemy.setEnabled(false);
        polysemy.setSelected(false);
        seeFreq.setSelected(scrollPanel.getAttribute(ScrollPanel.SEE_FREQ));
        seeSubwords.setSelected(scrollPanel.getAttribute(ScrollPanel.SEE_SUB));
        seeDialect.setEnabled(false);
        seeDialect.setSelected(false);
        seeSpeech.setEnabled(false);
        seeSpeech.setSelected(false);
        createHTML.setEnabled(false);
    }

    public void enableHeadwordList()
    {
        for (int i = TOPPANE; i < NUMPANES; i++) {
            for (int j=0;j<KKPANES;j++){
                if(KKTabs[i][j]!=null)
                    KKTabs[i][j].scrollPanelChanged(false);
            }
        }
        rhymeSort.setEnabled(true);
        //pos sort disabled
        //posSort.setEnabled(true);
        freqSort.setSelected(false);
        alphSort.setSelected(false);
        origSort.setEnabled(true);
        origSort.setSelected(false);
        curHeadwordSort.setSelected(true);

        polysemy.setEnabled(true);
        polysemy.setSelected(scrollPanel.getAttribute(ScrollPanel.SEE_POLY));
        seeFreq.setSelected(scrollPanel.getAttribute(ScrollPanel.SEE_FREQ));
        seeSubwords.setSelected(scrollPanel.getAttribute(ScrollPanel.SEE_SUB));
        seeDialect.setEnabled(true);
        seeDialect.setSelected(scrollPanel.getAttribute(ScrollPanel.SEE_DIALECT));
        seeSpeech.setEnabled(true);
        seeSpeech.setSelected(scrollPanel.getAttribute(ScrollPanel.SEE_POS));

        createHTML.setEnabled(true);
    }

    /**
     * Called by the DictionaryCache when the Xsl format has been changed and it has
     * complete the regeneration of the Html entries with the new format. Refreshes all
     * html panels.
     */
    public void formatChanged() {
        //      ((HtmlPanel)KKTabs[TOPPANE][HTML]).refreshFormat();
        //      ((HtmlPanel)KKTabs[BOTPANE][HTML]).refreshFormat();
        for (int j = TOPPANE; j < NUMPANES; j++) {
            KirrkirrPanel currentPanel = (KirrkirrPanel)tabbedPanes[j].getSelectedComponent();
            if (currentPanel instanceof HtmlPanel) {
                ((HtmlPanel)currentPanel).refreshFormat();
            }
        }
    }


    /** "Keep" method of main toolbar.
     *  Opens the Html-Notes Dialog on the current word.
     */
    private void openHtmlDialog() {
        String word = scrollPanel.getSelectedWord();
        if (word != null) {
            if (Dbg.VERBOSE) {
                Dbg.print("openHtmlDialog for |" + word + "|");
            }
            // i.e., a word has been selected
            // should change HtmlDialog constructor to take uniqueKey!
            new HtmlDialog(this, htmlFolder, HtmlDialog.NOTES,
                           word, ! scrollPanel.headwordsShowing);
        }
    }


    /** Copies the text into the system clipboard.
     *  @param text the string to copy
     */
    public void putStringInClipboard (String text) {
        try {
            // copy the word to the transferable object
            StringSelection s = new StringSelection(text);
            // get the system clipboard
            Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
            // put text in clipboard
            // StringSelection implements the ClipboardOwner interface
            c.setContents(s, s);
        } catch(SecurityException se) {
            setStatusBar(Helper.getTranslation(SC_CLIPBOARD_ERROR));
            getToolkit().beep();
        }
    }


    /** Exits kirrkirr. If there are changes, asks user whether to save profile
     *  (user can cancel, and we then return to running the program).
     *  If user wants to save profile, the profile manager is
     *  invoked, otherwise the program just exits.
     *  CDM Nov 2001: I changed this to just YES/NO option, since I can't see
     *  how to get CANCEL option to work right: on my Windows machine, it
     *  deletes the Kirrkirr window, but javaw.exe is left running in the
     *  background, which is gruesome (you need to use Task Manager to
     *  clean it up
     */
    public static void quitKirrkirr() {
        int option;

        if (CONFIRM_EXIT &&
            profileManager != null && profileManager.isUnsaved()) {
            option = JOptionPane.showConfirmDialog(window,
                                Helper.getTranslation(SC_SAVE_BEFORE_EXIT),
                                Helper.getTranslation(SC_CONFIRM_EXIT),
                                // JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
        } else {
            option = JOptionPane.NO_OPTION;
        }
        if (Dbg.VERBOSE) {
            Dbg.print("Quit: option is " + option);
        }
        // if (option == JOptionPane.CANCEL_OPTION) {
        //     return;
        // }

        if (option == JOptionPane.YES_OPTION) {
            if (!profileManager.saveProfile())
                return;
        }

        String cleanup = props.getProperty("kirrkirr.cleanup");
        if ((cleanup == null) || (cleanup.equals("true"))) {
            RelFile.cleanupWriteDirectories();
        }

        // exit if the user selected YES or NO to save
        Dbg.close();
        window.dispose();
        if (APPLET) {
            demo.showStatus("");
        } else {
            System.exit(0);
        }
    }

    /** Accessor for menu item Options|Word List|See Subwords.
     *  @return Whether the user wants to see subwords
     */
    public boolean seeSubwords(){
        return seeSubwords != null && seeSubwords.isSelected();
    }

    public boolean showPoly(){
        return showPoly;
    }

    public void showIndexMakerDialog(boolean modal) {
        new ToolsDialog(this, modal);
    }

    private void showXSLTransformDialog() {
        new XSLDialog(this);
    }

    private void showEncodingDialog() {
        new EncodingDialog(this);
    }

    private void showDomainConversionDialog(boolean modal) {
    	new DomainConverterEditor(this, modal);
    }

    private void showPreprocessor(boolean modal) {
    	new ImportDialog(this, modal);
    }

    /** Called by JComponents (History, ScrollPanel, KirrkirrPanels) to
     *  update the current word in all other components (and perhaps also
     *  themselves).  Also refreshes the word in the cache.
     *  Currently we only signal displayed components, but
     *  this might change (could then have audio even when not visible).
     *  If it's Gloss, we put up a wait cursor, since Gloss is slow.
     *  @param uniqueKey A unique key for a L1 headword.  This is the case
     *         even when Gloss is showing:
     *         setCurrentWord will be called on each Headword word that
     *         matches the Gloss gloss
     *  @param gloss true if the scrollPanel is showing Glosses
     *  @param signaller A reference to the component which sent this
     *         setCurrentWord request (implementations can use it to detect
     *         whether they themselves sent the request which is sent back
     *         to them if visible
     *  @param signallerType
     *  @param arg
     */
    public void setCurrentWord(String uniqueKey, boolean gloss,
                           final JComponent signaller, final int signallerType,
                           final int arg)
    {
        long starttime = 0;
        if (Dbg.TIMING) {
            starttime= System.currentTimeMillis();
        }
        if (Dbg.MEMORY) {
            Dbg.memoryUsage("during setCurrentWord");
        }
        if (gloss) Helper.setCursor(this, true);
        if (uniqueKey == null) {
            // shouldn't happen?? -- but we guarantee this for things called!
            return;
        }
        if (Dbg.PROGRESS || Dbg.TWO)
            Dbg.print("Calling setCurrentWord: |" + uniqueKey +
                "| signaller type " + signallerType + " gloss: " + gloss);
        if (gloss) {
            Object ret =
                    cache.refreshCache(uniqueKey, cache.getGlossIndexEntry(uniqueKey));
            if (ret == null) {
                // there was some problem, like clicking on subtitle in English mode
                Helper.setCursor(this, false);
                return;
            }
        }
        // else {
            // this was regenerating Html always.  Why?  Done in HtmlPanel.
            // if (signallerType == DICTIONARYCACHE)
            // cache.refreshCache(uniqueKey, false);
            // else
            // cache.refreshCache(uniqueKey, true);
        // }
        // Most panel setCurrentWord's, setWordInScrollPanel, and
        // History.setCurrentWord() are no-ops for gloss.
        // the real work is done above in refreshCache(word, GlossDictEntry)
        // However, the HtmlPanel does do work on Gloss words.
        history.setCurrentWord(uniqueKey, gloss, signaller, signallerType,
                               arg);

        scrollPanel.setWordInScrollPanel(uniqueKey, gloss, signaller,
                                       signallerType, arg);
        if (Dbg.TWO) Dbg.print("Done history and scroll-list");
        if (Dbg.TIMING) {
            long endtime = System.currentTimeMillis();
            endtime = endtime - starttime;
            Dbg.print("Kirrkirr.setCurrentWord common part took " +
                      endtime + "ms");
        }

        for (int j = TOPPANE; j < NUMPANES; j++) {
            KirrkirrPanel currentPanel = (KirrkirrPanel)tabbedPanes[j].getSelectedComponent();
            currentPanel.setCurrentWord(uniqueKey, gloss, signaller, signallerType, arg);
            if (Dbg.TWO) Dbg.print("Done pane " + j);

        }
        if (gloss) Helper.setCursor(this, false);
    }


    public boolean setStatusBar(String status)
    {
        if (statusBar==null) return false;
        statusBar.setText(status);
        return true;
    }

    // cw '02: sometimes want to do this (change from in head to in gloss, for
    // example), since the status bar has a timer/delay associated with
    // it, while this does not.
    public boolean setStatusBarIconText(String txt)
    {
        if (statusBar==null) return false;
        statusBar.setIconText(txt);
        return true;
    }

    // cw '02: sometimes want to do this (change from in head to in gloss, for
    // example), since the status bar has a timer/delay associated with
    // it, while this does not.
    public boolean setStatusBarIcon(String filename)
    {
        if (statusBar==null) return false;
        statusBar.setIcon(filename);
        return true;
    }


    /** implementation of the ChangeListener listening to the tabbed panes
     *  This is registered for each tabbed pane, and called when they are
     *  selected - hence they auto-activate the first word. [If we
     *  signalled even non-displaying panes, this might not be needed.]
     *  Changed 5/01 by Kristen - implemented as a KirrkirrPanel optional
     *  function rather than calling "getClass().getName()"
     */
    public void stateChanged(ChangeEvent e) {
    	String tailWord = scrollPanel.getSelectedWord();
        KirrkirrPanel panel = (KirrkirrPanel)((JTabbedPane) e.getSource()).getSelectedComponent();


        int whichPane = getPane(panel);


        //dont need to set current word since gameselectpanels dont use that
        //   function - it is empty
        if (panel instanceof GameSelectPanel) {

        	if(!oneRHSPane && !inGamePane) {

	        	if (whichPane == BOTPANE) {
	        		oldBottomPane = currentTabs[whichPane];
	        		fromBottomPane = true;
	        		KirrkirrPanel gamePane = KKTabs[TOPPANE][QUIZ];
	        		tabbedPanes[TOPPANE].setSelectedComponent(gamePane);

	        		if(currentTabs[TOPPANE]!=null)
	            		currentTabs[TOPPANE].tabDeselected();
	        		gamePane.tabSelected();
	        		currentTabs[TOPPANE] = gamePane;
	        		oldBottomPane.tabSelected();
	            	whichPane = BOTPANE;
	            	tabbedPanes[BOTPANE].setSelectedComponent(oldBottomPane);

	            	if(currentTabs[BOTPANE]!=null)
	            		currentTabs[BOTPANE].tabDeselected();
	        		currentTabs[BOTPANE] = oldBottomPane;

	        	}
	        	dividerSize = splitPane.getDividerSize();
        		dividerLoc = splitPane.getDividerLocation();
	            splitPane.remove(tabbedPanes[BOTPANE]);
        	}
           	inGamePane = true;
           	if (hintsOn) addHintPanel();
        }
        else
        {
            panel.tabSelected();
            if (!(panel instanceof GameSelectPanel)) {

            	if (!oneRHSPane && inGamePane) {
	            	if (fromBottomPane) {
	            		fromBottomPane = false;
	            		/*
		            	oldBottomPane.tabSelected();
		            	whichPane = BOTPANE;
		            	tabbedPanes[BOTPANE].setSelectedComponent(oldBottomPane);

		            	if(currentTabs[BOTPANE]!=null)
		            		currentTabs[BOTPANE].tabDeselected();
		        		currentTabs[BOTPANE] = oldBottomPane;
		        		*/
	            	}
	            	splitPane.setBottomComponent(tabbedPanes[BOTPANE]);
	            	splitPane.setDividerSize(dividerSize);
	            	splitPane.setDividerLocation(dividerLoc);
            	}
            	inGamePane = false;
            }

            if (currentTabs[whichPane] != null) {
                currentTabs[whichPane].tabDeselected();
            }
            currentTabs[whichPane] = panel;

            if (scrollPanel.headwordsShowing) {
                if (tailWord!=null) {
                    panel.setCurrentWord(tailWord, false, null, 0, 0);
                }
                currentTabs[whichPane].start();
            } else {
                if (tailWord!=null) {
                    panel.setCurrentWord(tailWord, true, null, 0, 0);
                }
                currentTabs[whichPane].start();
            }
        }
    }


    public void addHintPanel() {
    	splitPane.setBottomComponent(hints);
    	splitPane.setDividerSize(0);
    	splitPane.setDividerLocation(0.8);
    }

    public void removeHintPanel() {
    	if (splitPane.getBottomComponent() != null)
    		splitPane.remove(2);
    }

    public static void setHintWord(String word) {

        hints.setCurrentWord(word);

    }

    /**Helper to return the pane in which the given panel resides.  Would
     * be faster to just ask panel, but that also requires panel knowing
     * location - not the best object oriented design.
     */
    private int getPane(KirrkirrPanel panel) {
        for(int whichPane = TOPPANE; whichPane < NUMPANES; whichPane++) {
            for(int tab = GRAPH; tab < KKPANES; tab++) {
                if(KKTabs[whichPane][tab] == panel) {
                    return whichPane;
                }
            }
        }
        return TOPPANE; //default?
    }


    /** Listener to the HtmlPanel -- this is here because the HtmlPanel's
     *  parent could also be an HtmlDialog frame (but reorganize this?)
     */
    public void wordClicked(String uniqueKey, JComponent signaller) {
        setCurrentWord(uniqueKey, false, signaller, HTML, 0);
    }


    public static boolean readInDictionaryInfo(String dictionaryDir,
                                               Properties props) {
        String dictInfoFile = null;
        usingDomainConversion = false;
        try {
            RelFile.setDictionaryDir(dictionaryDir);
            if (Dbg.PROGRESS) Dbg.print("dictionaryDirectory is |" + dictionaryDir + "|");
            props.load(new BufferedInputStream(RelFile.makeURL(dictionaryDir, DICT_PROPERTIES_FILE).openConnection().getInputStream()));

            indexFile = props.getProperty("dictionary.index");
            engIndexFile=props.getProperty("dictionary.reverseIndex");
            xmlFile = props.getProperty("dictionary.dictionary");
            domainFile = props.getProperty("dictionary.domainFile");
            dictInfoFile = props.getProperty("dictionary.dictSpecFile");
            domainConverter = props.getProperty("dictionary.domainConverter");
            if ((domainConverter != null) && (!domainConverter.equalsIgnoreCase("null"))) {
            	dc = new DomainConverter(RelFile.MakeFileName(RelFile.dictionaryDir, domainConverter));
            	usingDomainConversion = true;
            } else {
            	usingDomainConversion = false;
            }
            dictInfo=new DictionaryInfo(RelFile.MakeFileName(RelFile.dictionaryDir, dictInfoFile));
            return true;
        } catch (Exception e) {
            if (Dbg.ERROR) {
                Dbg.print("readInDictionaryInfo: failed");
                e.printStackTrace();
            }
            if (dictInfoFile == null) dictInfoFile = "";
            JOptionPane.showMessageDialog(null,
                    Helper.getTranslation(SC_NO_DICTINFO) + "\n" +
                      RelFile.MakeFileName(RelFile.dictionaryDir, dictInfoFile) +
                      "\nError: " + e.getMessage(),
                    Helper.getTranslation(SC_DICTINFO_ERROR),
                    JOptionPane.ERROR_MESSAGE);

            //if can't load dict, set dictdir to null
            //so that it will prompt user to try again
            RelFile.dictionaryDir = null;
            dictInfo = null;
            return false;
        }
    }


    public static boolean promptForNewDictionary() {
        JFileChooser fileChooser = new JFileChooser(new File(RelFile.WRITE_DIRECTORY));
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle(Helper.getTranslation(SC_NEW_DICT));
        int userChoice = fileChooser.showDialog(Kirrkirr.window, Helper.getTranslation(SC_SET_DICT));
        // Dbg.print("userChoice was " + userChoice + " [APPROVE=" +
        //        JFileChooser.APPROVE_OPTION + ", CANCEL=" +
        //        JFileChooser.CANCEL_OPTION + "]");
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            String directoryDir= fileChooser.getSelectedFile().getName();
            Dbg.print(directoryDir);

            return readInDictionaryInfo(directoryDir, props);
        } else { // if (userChoice==JFileChooser.CANCEL_OPTION) {
            quitKirrkirr();
            return false;
        }
    }


    /** Reads command line arguments, and sets various variables.
     *  @param argv Command line arguments
     *  @return Whether or not a dictionary was found
     */
    public static boolean parseArguments(String[] argv) {
        boolean foundDict=false;
        String autoLoadString = null;

        if (argv.length == 0) {
            autoLoadString = props.getProperty("kirrkirr.AutoLoadDefault");
            defaultProfile =  props.getProperty("kirrkirr.defaultProfile");
            // Madhu:'00,for I18n
            langCode = props.getProperty("kirrkirr.langCode");
            countryCode = props.getProperty("kirrkirr.countryCode");
            foundDict = RelFile.setDictionaryDir(props.getProperty("kirrkirr.dictionaryDirectory"));
            if (Dbg.VERBOSE) Dbg.print("setDictionary to " + props.getProperty("kirrkirr.dictionaryDirectory") + " foundDict=" + foundDict);
            String oldN = props.getProperty(DirsOptionPanel.PROP_OLDNETWORK);
            oldNetwork = (oldN != null && oldN.equalsIgnoreCase("true"));
            String oldD = props.getProperty(DirsOptionPanel.PROP_OLDDOMAINS);
            oldSemanticDomains = (oldD != null && oldD.equalsIgnoreCase("true"));
        } else if (argv[0].equals("-web")) {
            APPLET = true;
            foundDict=RelFile.setDictionaryDir(argv[1]);
            if (Dbg.K) Dbg.print("dictionaryDirectory is |" + argv[1] + "|");
        } else if ( (argv.length < 6) || (argv[0].equals("-help"))) {
            System.out.println("Kirrkirr Usage: java Kirrkirr.Kirrkirr");
            System.out.println("OR: java Kirrkirr xmlDocument clkIndexFile glossClkFile autoLoadBoolean defaultProfile languageCode");
            System.exit(0);
        } else {
            xmlFile = argv[0];
            indexFile = argv[1];
            engIndexFile =argv[2];
            autoLoadString  = argv[3];  //Madhu:'00
            defaultProfile = argv[4];
            langCode = argv[5];
        }

        if (autoLoadString!=null && autoLoadString.equalsIgnoreCase("true")) {
            autoLoadDefault = true;
        }
        if (langCode == null || langCode.equals("")) {
            langCode = "en";
        }
        if (countryCode == null || countryCode.equals("")) {
            countryCode = "AU";
        }
        if (foundDict) {
            foundDict = readInDictionaryInfo(RelFile.dictionaryDir, props);
        }
        if (Dbg.ERROR && ! foundDict) {
            Dbg.print("Dictionary spec file not found: " + RelFile.dictionaryDir);
        }
        return foundDict;
    }


    /** The main program.
     *  @param argv Command line arguments
     */
    public static void main(String[] argv) {
    	args = argv;
        RelFile.Init("");
        mainInit(argv);
    }


    /** To be merged with mainInit() and deleted!!! */
    static void reload(String[] argv) {
    	try {
            if (Dbg.TIMING) {
                Dbg.startTime();
            }
            if (Dbg.MEMORY) {
                Dbg.memoryUsage("at start up");
            }

              if (Helper.onMacOSX()) {
                  try {
                      if (Dbg.PROGRESS) Dbg.print("On MacOSX doing menu setup");
                      // So menu appears on top of screen for Mac
                      // for Mac Java 1.4
                      System.setProperty("apple.laf.useScreenMenuBar", "true");
                      // for Mac Java 1.3
                      System.setProperty("com.apple.macos.useScreenMenuBar", "true");
                      System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Kirrkirr");

                      // System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
                      // use java.awt.FileChooser rather than javax.swing.JFileChooser.
                      // See http://developer.apple.com/samplecode/OSXAdapter/OSXAdapter.html for handling Apple application menu
                      // apple.awt.showGrowBox
                      // apple.awt.brushMetalLook
                  } catch (Exception e) {
                    Dbg.print("Error in MacOS setup");
                    e.printStackTrace();
                  }
              }

            // Get properties for Kirrkirr
            props = new Properties();
            try {
                BufferedInputStream bis = new BufferedInputStream(RelFile.makeURL(PROPERTIES_FILE).openConnection().getInputStream());
                PropertiesUtils.load(props, bis);
                bis.close();
            } catch (IOException ioe) {
                // Probably file not found. Do nothing.
            }

            // guarantees kirrkirr.properties arguments to be non-null
            // and non-empty (except glossIndex)
            if ( ! parseArguments(argv)) {
                Kirrkirr.dictInfo = null;
            }

            WindowListener winlin = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    quitKirrkirr();
                }
                public void windowActivated(WindowEvent e) {
                    // start any panel threads
                    for (int j = TOPPANE; j < NUMPANES; j++) {
                        if(currentTabs[j]!=null)
                            currentTabs[j].start();
                    }
                }
                public void windowDeactivated(WindowEvent e) {
                    // stop any panel threads
                    for (int j = TOPPANE; j < NUMPANES; j++) {
                        if(currentTabs[j] !=null)
                            currentTabs[j].stop();
                    }
                }
            };

            // set up fonts
            FontProvider.initializeFonts(props);

            //Title of window
            //now taken care of by ProfileManager.
            /*if (autoLoadDefault.equals("True")) {
                window = new JFrame("Kirrkirr: " + defaultProfile +" Profile");
            } else {
                window = new JFrame("Kirrkirr");
                }*/
            window = new JFrame("Kirrkirr");
            window.addWindowListener(winlin);
            progress.incrementValue();

            // if they cancel this dialog, program exits.
            while (Kirrkirr.dictInfo == null) {
                promptForNewDictionary();
            }

            kk = new Kirrkirr();

            if (Dbg.MEMORY) {
                Dbg.memoryUsage("after Kirrkirr() initialization");
            }

            // BorderLayout is default anyway
            // window.getContentPane().setLayout(new BorderLayout());
            kk.initializeMenuBar(window);
            // window.getContentPane().add(kk.menubar, BorderLayout.NORTH);
            window.getContentPane().add(kk, BorderLayout.CENTER);
            if (Dbg.VERBOSE) {
                Dbg.print("Screensize: " + kk.screenSize);
            }
            window.getContentPane().setSize(kk.screenSize);

            progress.setMessage(Helper.getTranslation(SC_SETTING_UP));
            // TODO: If you do a reload and the indexFile isn't named or doesn't exist, it crashes here. This one isn't protected by any checks unlike in mainInit.  Unify the two!
            cache = new DictionaryCache(indexFile, kk, htmlFolder, xmlFolder,
                      RelFile.dictionaryDir+RelFile.fileSeparator()+xslFolder);
            progress.incrementValue();

            // Chris: this was resetWordsGUI, but that's wrong: there is
            // no statusBar yet, and it caused an XML parse, even though we
            // haven't done anything yet....  In particular it caused a
            // crash if you had only 1 word in your .clk file.  Unusual, but
            // there you go.
            kk.scrollPanel.resetWords(null);
            progress.incrementValue();

            kk.scrollPanel.init();  // start listening here

            progress.incrementValue(Helper.getTranslation(SC_COMPLETE));
            //Madhu:'00, autoloading of Default Profile
            ProfileManager prof_manager = new ProfileManager(kk,
                                                             autoLoadDefault,
                                                             defaultProfile);
            kk.profileManager = prof_manager;
            kk.kirrkirrOptions.addOptionPanel(prof_manager.getOptionPanel());
            /*if (autoLoadDefault.equals("True"))
              profileManager.loadProfile(kk.defaultProfile);*/
            window.pack();
            progress.setValue(PROGRESS_MAX_VAL);

            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        window.setVisible(true);
                        // this needs to be done after Kirrkirr is initialized
                        // this should be done on EventQueue thread
                        float split = 0.6F;
                        String str = props.getProperty("kirrkirr.singlePanelStartup");
                        if ("true".equalsIgnoreCase(str)) {
                            split = 1.0F;
                        }

                        if (!oneRHSPane) kk.splitPane.setDividerLocation(split);
                    }
                });

            if (Dbg.VERBOSE) {
                Dbg.print("W:"+window.getPreferredSize()+" "+window.getSize());
            }
            if (Dbg.MEMORY) {
                Dbg.memoryUsage("at end of main()");
            }

            if (APPLET) {
                demo.showStatus(Helper.getTranslation(SC_COMPLETE));
            }
            if (Dbg.VERBOSE) Dbg.print("hello from main");
            Thread.sleep(1200); //let the swing threads catch up
            cache.glossLoader.start();


            if (Dbg.TIMING) {
                Dbg.endTime("Kirrkirr total startup time");
            }
        } catch (Throwable exception) {
            if (exception instanceof InterruptedIOException) {
                System.exit(0);
            }
            exception.printStackTrace();
        }
    }

    static void mainInit(String[] argv) {
        mainInit(argv, false);
    }

    private static void mainInit(String[] argv, boolean reloading) {
        try {
            if (Dbg.TIMING) {
                Dbg.startTime();
            }
            if (Dbg.MEMORY) {
                Dbg.memoryUsage("at start up");
            }

            if (Helper.onMacOSX()) {
              try {
                  if (Dbg.PROGRESS) Dbg.print("On MacOSX doing menu setup");
                  // So menu appears on top of screen for Mac
                  // for Mac Java 1.4
                  System.setProperty("apple.laf.useScreenMenuBar", "true");
                  // for Mac Java 1.3
                  System.setProperty("com.apple.macos.useScreenMenuBar", "true");
                  System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Kirrkirr");
				  System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Stanford RTE");

                  System.setProperty("com.apple.macos.use-file-dialog-packages", "true");
                  // use java.awt.FileChooser rather than javax.swing.JFileChooser.
                  // See http://developer.apple.com/samplecode/OSXAdapter/OSXAdapter.html for handling Apple application menu
                  // apple.awt.showGrowBox
                  // apple.awt.brushMetalLook
              } catch (Exception e) {
                Dbg.print("Error in Mac OS X setup");
                e.printStackTrace();
              }
            }

            if (! reloading) {
                // put up loading dialog box quickly
                progress = new ProgressDialog(Helper.getTranslation(SC_STARTING),
                                              PROGRESS_MAX_VAL);
            }

            // Get properties for Kirrkirr
            props = new Properties();
            try {
                BufferedInputStream bis = new BufferedInputStream(RelFile.makeURL(PROPERTIES_FILE).openConnection().getInputStream());
                PropertiesUtils.load(props, bis);
                bis.close();
            } catch (IOException ioe) {
                // Probably file not found. Do nothing.
            }

            // guarantees kirrkirr.properties arguments to be non-null
            // and non-empty (except glossIndex)
            if ( ! parseArguments(argv)) {
                Kirrkirr.dictInfo = null;
            }

            WindowListener winlin = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    quitKirrkirr();
                }
                public void windowActivated(WindowEvent e) {
                    // start any panel threads
                    for (int j = TOPPANE; j < NUMPANES; j++) {
                        if(currentTabs[j] != null)
                            currentTabs[j].start();
                    }
                }
                public void windowDeactivated(WindowEvent e) {
                    // stop any panel threads
                    for (int j = TOPPANE; j < NUMPANES; j++) {
                       if(currentTabs[j] != null) {
                            currentTabs[j].stop();
                       }
                    }
                }
            };

            // set up fonts
            FontProvider.initializeFonts(props);

            //Title of window
            //now taken care of by ProfileManager.
            /*if (autoLoadDefault.equals("True")) {
                window = new JFrame("Kirrkirr: " + defaultProfile +" Profile");
            } else {
                window = new JFrame("Kirrkirr");
                }*/
            window = new JFrame("Kirrkirr");
            window.addWindowListener(winlin);
            progress.incrementValue();

            kk = new Kirrkirr();

            if (Dbg.MEMORY) {
                Dbg.memoryUsage("after Kirrkirr() initialization");
            }

            // if they cancel this dialog, program exits.
            progress.setMessage(Helper.getTranslation(SC_SETTING_UP));
            while (dictInfo == null || cache == null) {
                boolean doingOkay = true;
                if (dictInfo == null) {
                    doingOkay = promptForNewDictionary();
                }
                if (doingOkay) {
                    if (indexFile == null || "".equals(indexFile)) {
                        JOptionPane.showMessageDialog(null,
                                Helper.getTranslation(SC_NO_DICT_INDEX) + "\n" +
                                RelFile.MakeFileName(RelFile.dictionaryDir, indexFile),
                                Helper.getTranslation(SC_DICT_ERROR),
                                JOptionPane.ERROR_MESSAGE);
                        if (Dbg.ERROR) {
                            Dbg.print("Error: No dictionary index specified!");
                        }
                        new ToolsDialog(Kirrkirr.kk, true);
                        cache = null;
                        dictInfo = null;
                        doingOkay = false;
                    }
                }
                if (doingOkay) {
                    try {
                        // this next call can fail if file is invalid (e.g., truncated)
                        cache = new DictionaryCache(indexFile, kk, htmlFolder, xmlFolder,
                              RelFile.dictionaryDir+RelFile.fileSeparator()+xslFolder);
                    } catch (IOException ioe) {
                        JOptionPane.showMessageDialog(null,
                                Helper.getTranslation(SC_NO_DICT) + "\n" +
                                RelFile.MakeFileName(RelFile.dictionaryDir, indexFile) + "\n" +
                                ioe.toString(),
                                Helper.getTranslation(SC_DICT_ERROR),
                                JOptionPane.ERROR_MESSAGE);
                        if (Dbg.ERROR) {
                            Dbg.print("Couldn't open the dictionary!");
                            Helper.handleException(ioe);
                        }
                        cache = null;
                        dictInfo = null;
                    }
                }
            } // end while (dictInfo == null || cache == null)
            progress.incrementValue();

	    // BorderLayout is default anyway
            // window.getContentPane().setLayout(new BorderLayout());
            kk.initializeMenuBar(window);
            // window.getContentPane().add(kk.menubar, BorderLayout.NORTH);
            window.getContentPane().add(kk, BorderLayout.CENTER);
            if (Dbg.VERBOSE) {
                Dbg.print("Screensize: " + kk.screenSize);
            }
            window.getContentPane().setSize(kk.screenSize);

            // put in an icon
            window.setIconImage(RelFile.makeImageIcon("kirr32.gif", false).getImage());

            // Chris: this was resetWordsGUI, but that's wrong: there is
            // no statusBar yet, and it caused an XML parse, even though we
            // haven't done anything yet....  In particular it caused a
            // crash if you had only 1 word in your .clk file.  Unusual, but
            // there you go.
            kk.scrollPanel.resetWords(null);
            progress.incrementValue();

            kk.scrollPanel.init();  // start listening here

            progress.incrementValue(Helper.getTranslation(SC_COMPLETE));
            //Madhu:'00, autoloading of Default Profile
            ProfileManager prof_manager = new ProfileManager(kk,
                                                             autoLoadDefault,
                                                             defaultProfile);
            kk.profileManager = prof_manager;
            kk.kirrkirrOptions.addOptionPanel(prof_manager.getOptionPanel());
            /*if (autoLoadDefault.equals("True"))
              profileManager.loadProfile(kk.defaultProfile);*/
            window.pack();
            progress.setValue(PROGRESS_MAX_VAL);

            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        window.setVisible(true);
                        // this needs to be done after Kirrkirr is initialized
                        // this should be done on EventQueue thread
                        float split = 0.6F;
                        String str = props.getProperty("kirrkirr.singlePanelStartup");
                        if ("true".equalsIgnoreCase(str)) {
                            split = 1.0F;
                        }

                        if (!oneRHSPane) kk.splitPane.setDividerLocation(split);
                    }
                });

            if (Dbg.VERBOSE) {
                Dbg.print("W:"+window.getPreferredSize()+" "+window.getSize());
            }
            if (Dbg.MEMORY) {
                Dbg.memoryUsage("at end of main()");
            }

            if (APPLET && ! reloading) {
                demo.showStatus(Helper.getTranslation(SC_COMPLETE));
            }
            if (Dbg.VERBOSE) Dbg.print("hello from main");
            Thread.sleep(1200); //let the swing threads catch up
            if (cache.glossLoader != null) {
                cache.glossLoader.start();
            }

            if (Dbg.TIMING) {
                Dbg.endTime("Kirrkirr total startup time");
            }
        } catch (Throwable exception) {
            if (exception instanceof InterruptedIOException) {
                System.exit(0);
            }
            exception.printStackTrace();
        }
    }

    public void loadNewDict(String dir) {
    	LoadDictDir ldd = new LoadDictDir();
    	ldd.setDir(dir);
    	Thread loader = new Thread(ldd);
    	loader.start();
    }


    public class LoadDictDir implements ActionListener, Runnable {

    	private String dictDir;

        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(new File(RelFile.WRITE_DIRECTORY)); // change? base dir (RelFile.codeBase)? - they actually start off the same (RelFile.Init)
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setDialogTitle(Helper.getTranslation(SC_LOAD_NEW_DICT));
            int userChoice = fileChooser.showDialog(Kirrkirr.window, Helper.getTranslation(RelFile.SELECT));
            if (userChoice == JFileChooser.APPROVE_OPTION) {
                // this is how promptForNewDictionary() does it, but
                // it doesn't seem to be the best way...  Only works if subdir
                dictDir = fileChooser.getSelectedFile().getName();

                Thread loader = new Thread(this);
                loader.start();
            }
        }

        public void setDir(String dir) {
        	dictDir = dir;
        }

        public void run() {
            progress = new ProgressDialog(Helper.getTranslation(SC_STARTING),
                                          PROGRESS_MAX_VAL);
            for (int j = TOPPANE; j < NUMPANES; j++) {
                if (currentTabs[j] !=null) {
                    currentTabs[j].stop();
                }
            }
            int option;

            if (CONFIRM_EXIT &&
                profileManager != null && profileManager.isUnsaved()) {
                option = JOptionPane.showConfirmDialog(window,
                                    Helper.getTranslation(SC_SAVE_BEFORE_EXIT),
                                    Helper.getTranslation(SC_CONFIRM_EXIT),
                                    // JOptionPane.YES_NO_CANCEL_OPTION,
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
            } else {
                option = JOptionPane.NO_OPTION;
            }
            if (Dbg.VERBOSE) {
                Dbg.print("Quit: option is " + option);
            }

            if (option == JOptionPane.YES_OPTION) {
                if (!profileManager.saveProfile())
                    return;
            }

            String cleanup = props.getProperty("kirrkirr.cleanup");
            if ((cleanup == null) || (cleanup.equals("true"))) {
                RelFile.cleanupWriteDirectories();
            }

            try {

            Kirrkirr.kk.changeProperty("kirrkirr.dictionaryDirectory",
                    dictDir);
            Kirrkirr.kk.changeProperty("dictionary.domainConverter",
                    "null");


			PropertiesUtils.changeProperty(props,
			     RelFile.MakeWriteFileName(null, PROPERTIES_FILE),
					"kirrkirr.dictionaryDirectory",
			     dictDir);
			if (Dbg.VERBOSE) Dbg.print(
					"Properties: changed kirrkirr.dictionaryDirectory to "
			                        + dictDir);
            } catch (Exception ex) {
                if (Dbg.ERROR)
                    Helper.handleException(ex);
                setStatusBar("Couldn't save properties file.");
            }

            // exit if the user selected YES or NO to save
            Dbg.close();
            window.dispose();
            RelFile.Init("");
            // mainInit(args, true);
            reload(args);
        }
    } // end class LoadDictDir


    /** Generic registration with the Mac OS X application menu.  Attempts
     *  to register with the Apple EAWT.  This code assumes you're running this
     *  inside a test for being on a Mac OS X machine.
     *  This method calls OSXAdapter.registerMacOSXApplication() and OSXAdapter.enablePrefs().
     *  See OSXAdapter.java for the signatures of these methods.
     */
    public void macOSXRegistration() {
        try {
            Class osxAdapter = ClassLoader.getSystemClassLoader().loadClass("Kirrkirr.util.OSXAdapter");

            Class[] defArgs = {Kirrkirr.class};
            Method registerMethod = osxAdapter.getDeclaredMethod("registerMacOSXApplication", defArgs);
            if (registerMethod != null) {
                Object[] args = { this };
                registerMethod.invoke(osxAdapter, args);
            }
            // This is slightly gross.  To reflectively access methods with boolean args,
            // use "boolean.class", then pass a Boolean object in as the arg, which apparently
            // gets converted for you by the reflection system.
            defArgs[0] = boolean.class;
            Method prefsEnableMethod = osxAdapter.getDeclaredMethod("enablePrefs", defArgs);
            if (prefsEnableMethod != null) {
                Object[] args = { Boolean.TRUE };
                prefsEnableMethod.invoke(osxAdapter, args);
            }
        } catch (NoClassDefFoundError e) {
            // This will be thrown first if the OSXAdapter is loaded on a system without the EAWT
            // because OSXAdapter extends ApplicationAdapter in its def
            System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
        } catch (ClassNotFoundException e) {
            // This shouldn't be reached; if there's a problem with the OSXAdapter we should get the
            // above NoClassDefFoundError first.
            System.err.println("This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled (" + e + ")");
        } catch (Exception e) {
            System.err.println("Exception while loading the OSXAdapter:");
            e.printStackTrace();
        }
    }

}

