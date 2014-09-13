package Kirrkirr.ui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.dom.*;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.xerces.parsers.SAXParser;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javax.swing.*;

import Kirrkirr.*;
import Kirrkirr.util.*;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.dictionary.DictionaryInfo;


/** A wizard for creating a DictionaryInfo file for a dictionary.
 *
 *  @author Chloe Kiddon
 */
public class ImportDialog extends JDialog implements ActionListener, Runnable {

    public static final String SC_PROCESS_DESC = "Kirrkirr_XML_Dictionary_Preprocessor";
    public static final String SC_DICT_HEADING = "Step_1:_Enter_the_dictionary_file";
    public static final String SC_DICT_FILE = "XML_Dictionary_File";
    public static final String SC_SPEC_HEADING =
            "Step_2:_Enter_the_current_Kirrkirr_specification_XML_file_(if_one_exists)";
    public static final String SC_DICT_SPEC_FILE = "XML_Dictionary_Spec_File";
    public static final String SC_SPEC_FILE_DESC =
            "This_file_will_be_used_to_fill_in_some_of_the_information\n_for_the_processed_" +
                    "dictionary._Leaving_the_box_empty_will\n_assume_no_prior_specification_file_knowledge.";
    public static final String SC_BASIC_INFO_HEADING =
            "Step_3:_Enter_basic,_critical_dictionary_info";
    public static final String SC_DICT_NAME = "Dictionary_Name: ";
    public static final String SC_GLOSS_DICT_LANG = "Gloss_Language: ";
    public static final String SC_ENTRY_PATH = "Dictionary_Entry_XPath: ";
    public static final String SC_HAS_SUBENTRIES = "Dictionary_has_subwords";
    public static final String SC_HEADWORD_PATH = "Headword_XPath: ";
    public static final String SC_UNIQUIFIER_PATH = "Optional_Uniquifier_XPath: ";
    public static final String SC_GLOSS_PATH = "Gloss_Dictionary__Entry_XPath: ";
    public static final String SC_ATTRIBUTE_PATH = "Has_attribute_in_path: ";
    public static final String SC_SUBENTRY_HEADING =
            "Step_3_(cont):_Enter_information_about_subentry_structure";
    public static final String SC_SUBENTRY_ATTR = "Subentries_are_separate_entries";
    public static final String SC_SUBENTRY_XPATH = "Subentry_are_nested_inside_other_entries";
    public static final String SC_SUBENTRY_DESC =
            "You_have_indicated_that_your_dictionary_has_some_\n" +
                    "dictionary_entries_inside_other_ones_using_subentry_\n" +
                    "tags._In_order_for_Kirrkirr_to_work_correctly,_these_\n" +
                    "will_have_to_be_removed_in_preprocessing_and_replaced_\n" +
                    "by_separate_entries_in_the_dictionary_with_a_subentry_\n" +
                    "attribute_or_with_a_new_tag_linking_back_to_the_mainentry_\n" +
                    "for_linking._(You_will_have_the_option_to_save_the_new_\n" +
                    "dictionary_as_a_new_file_so_as_to_save_your_original.)";
    public static final String SC_SUBENTRY_PATH =
            "XPath_that_indicates_a_word_is_a_subentry: ";
    public static final String SC_SUBENTRY_NEST = "Nested_subentry_path: ";
    public static final String SC_USE_ATTR = "Only_use_attribute_in_reformatting";
    public static final String SC_USE_LINK =
            "Additionally_create_new_linking_tags_in_reformatting";
    public static final String SC_CREATE_SUB_LINK = "Create_link_from_mainentry_to_subwords";
    public static final String SC_CREATE_MAIN_LINK = "Create_link_from_subword_to_mainentry";
    public static final String SC_SEARCH_HEADING =
            "Step_4:_Enter_some_optional_search_information";
    public static final String SC_HEADWORD_REGEX =
            "Headword_regular_expression_for_fast_searches";
    public static final String SC_UNIQUIFIER_REGEX =
            "Uniquifier_regular_expression_for_fast_searches";
    public static final String SC_SEARCH_NAME = "Searchable_field_name: ";
    public static final String SC_REGEX = "Regular_expression: ";
    public static final String SC_SEARCH_REGEX = "Search_Regular_Expressions";
    public static final String SC_ADD = "ADD";
    public static final String SC_EDIT = "EDIT";
    public static final String SC_DELETE = "DELETE";
    public static final String SC_DOMAIN_HEADING =
            "Step_5:_Enter_information_about_the_domain_xpaths";
    public static final String SC_DOMAIN_PATH = "Entry's_Domain_XPath: ";
    public static final String SC_SUBDOMAIN_PATH = "Has_domain_components";
    public static final String SC_DOMAIN_CONV =
            "Use_domain_converter_to_normalize_domain_names: ";
    public static final String SC_BROWSE = "Browse";
    public static final String SC_CREATE_NEW = "Create_new";
    public static final String SC_LINKS_HEADING =
            "Step_6:_Enter_information_that_describes_links_between_words";
    public static final String SC_REF_HEADWORD_XPATH =
            "Headword_Reference_XPath_(opt): ";
    public static final String SC_REF_UNIQUIFIER_XPATH =
            "Uniquifier_Reference_XPath_(opt): ";
    public static final String SC_LINK_NAME = "Link_name: ";
    public static final String SC_LINK_PATH = "Link_XPath: ";
    public static final String SC_LINK_COLOR = "Link_color: ";
    public static final String SC_SELECT_COLOR = "Select_color";
    public static final String SC_RED = "RED: ";
    public static final String SC_GREEN = "GREEN: ";
    public static final String SC_BLUE = "BLUE: ";
    public static final String SC_SUB_LINK_MESSAGE =
            "Mainentry_and/or_subentry_links_will_be_added_in_processing";
    public static final String SC_FUZZY_HEADING =
            "Step_7:_Enter_any_fuzzy_spelling_replacements_to_allow_for_better_searching";
    public static final String SC_FUZZY_HWORD_SUBS =
            "Fuzzy_Headword_Regex_Substitutions: ";
    public static final String SC_FUZZY_GLOSS_SUBS =
            "Fuzzy_Gloss_Regex_Substitutions";
    public static final String SC_FUZZY_ORIG_REGEX = "Original_regular_expression: ";
    public static final String SC_FUZZY_NEW_REGEX = "Replacement_regular_expression: ";
    public static final String SC_STOP_HEADING =
            "Step_8:_Enter_stop_strings_to_be_disregarded_in_gloss_dictionary_processing";
    public static final String SC_STOPWORD = "Stopword: ";
    public static final String SC_STOPCHAR = "Stopchar: ";
    public static final String SC_OTHER_INFO_HEADING1 =
            "Step_9a:_Enter_some_other_optional_information";
    public static final String SC_OTHER_INFO_HEADING2 =
            "Step_9b:_Enter_some_other_optional_information";
    public static final String SC_FREQ = "Word_Frequency_XPath: ";
    public static final String SC_POS = "Word_Part_of_Speech_XPath: ";
    public static final String SC_SENSE = "Word_Sense_XPath: ";
    public static final String SC_DIALECT = "Word_Dialects_XPath: ";
    public static final String SC_REGISTER = "Word_Registers_XPath: ";
    public static final String SC_DICTERROR_HEADING =
            "Step_10:_Enter_opt_error_strings_to_be_removed_from_hword_and_gloss_processing";
    public static final String SC_DICTERROR = "Dictionary_Error: ";
    public static final String SC_MEDIA_HEADING =
            "Step_11:_Enter_information_about_sounds_and_images";
    public static final String SC_IN_IMAGE_FOLDER = "In \'images\' directory...";
    public static final String SC_IMAGE_PATH = "Word_Images_XPath: ";
    public static final String SC_AUDIO_PATH = "Word_Audio_XPath: ";
    public static final String SC_DICT_ICON = "Dictionary_Icon: ";
    public static final String SC_GLOSS_ICON = "Gloss_Dictionary_Icon: ";
    public static final String SC_XSL_HEADING =
            "Step_12:_Enter_the_xsl_files_to_be_used_to_generate_html_entries";
    public static final String SC_IN_XSL_FOLDER = "In \'xsl\' directory...";
    public static final String SC_USE_UNDERSCORE =
            "For_internationalization,_please_use_underscores_instead_of_spaces.";
    public static final String SC_XSL_FILE = "XSL_Filename: ";
    public static final String SC_XSL_NAME = "Short_Name: ";
    public static final String SC_XSL_DESC = "XSL_Description: ";

    public static final String SC_PROCESS_HEADING =
            "Step_13:_Pre-Processing!";
    public static final String SC_NEED_PREPROCESS =
            "You_have_indicated_structure_elements_of_your_dictionary_that_must_be_processed:";
    public static final String SC_SUBENTRY_R = "Subentry_structure_must_be_revised";
    public static final String SC_LINK_R = "Link ";
    public static final String SC_MUST_SPLIT = " must_be_parsed";
    public static final String SC_SENSE_R = "Senses_must_be_parsed";
    public static final String SC_DIALECT_R = "Dialects_must_be_parsed";
    public static final String SC_REGISTER_R = "Registers_must_be_parsed";
    public static final String SC_DOMAIN_R = "Domains_must_be_parsed";
    public static final String SC_DOMAIN_CONV_R = "Domains_must_be_converted";
    public static final String SC_IMAGE_R = "Images_must_be_parsed";
    public static final String SC_AUDIO_R = "Audio_must_be_parsed";
    public static final String SC_NEW_DICT_DESC =
            "A_new_dictionary_must_be_constructed_for_use_with_Kirrkirr:";
    public static final String SC_NO_TYPE =
            "(Please_do_not_include_file_extensions_in_you_names)";
    public static final String SC_OLD_DICT_DESC =
            "No_preprocessing_will_have_to_be_done_on_your_dictionary.";
    public static final String SC_SPEC_SAVE_DESC =
            "Please_give_the_name_of_the_file_you_would_like_these_specs_to_be_saved_in.";
    public static final String SC_NEW_DICT_DIR = "New_Dictionary_Directory: ";
    public static final String SC_NEW_DICT_FILE = "New_Dictionary_XML_Filename: ";
    public static final String SC_NEW_SPEC_FILE = "New_Dictionary_Spec_Filename: ";
    public static final String SC_RUN_PREPROCESS = "Click_\'Next\'_to_begin_preprocessing...";

    public static final String SC_DICT_FILES_HEADING = "Step_14:_Final_processing!";
    public static final String SC_DICT_FILES_DESC =
            "Enter_names_of_these_Kirrkirr_dictionary_files_that_will_be_created_or_rewritten.";
    public static final String SC_FORWARD_INDEX = "Forward_index_file: ";
    public static final String SC_REVERSE_INDEX = "Reverse_index_file: ";
    public static final String SC_DOMAIN_FILE = "Domain_file";
    public static final String SC_RUN_PROCESS = "Click_\'Next\'_to_begin_processing...";

    public static final String SC_FINAL_HEADING = "Dictionary_preparation_completed!";
    public static final String SC_LOAD_NEW_DICT = " Load_new_dictionary_now ";
    public static final String SC_FINISH = "Finish";

    public static final String SC_SUBELEM = "_I";
    public static final String SC_EXTRASUBELEM = "I";


    public static final String SC_CANCEL = "Cancel";
    public static final String SC_PREVIOUS = "<_Previous";
    public static final String SC_NEXT = "Next_>";

    private JPanel currPage;

    private JPanel mainPanel;
    private JPanel instructionsPanel;

    private HashMap tagNames;
    private HashMap subTags;
    private Vector possSenseTags;

    private KirrkirrButton cancel;
    private KirrkirrButton previous;
    private KirrkirrButton next;

    private String filename;
    private String specFilename;
    private DictionaryInfo oldSpecs;

    private boolean newDictOrSpec;

    //private Document newSpecs;

    //dict panel
    private JPanel dictFilePanel;
    private AuxFilePanel fileGetterPanel;

    //spec panel
    private JPanel specFilePanel;
    private AuxFilePanel specFileGetterPanel;

    //basic info panel
    private JPanel basicInfoPanel;
    private JTextField dictName;
    private JTextField glossLang;
    private SpecEntryPanel dictEntryXPath;
    private JCheckBox hasSubEntries;
    private SpecEntryPanel headwordPath;
    private SpecEntryPanel uniquifierPath;
    private SpecEntryPanel glossXPath;

    //subentry panel
    private JPanel subEntryFixPanel;
    private JPanel subFixCardPanel;
    private JPanel subCardsPanel;
    private JRadioButton subIsSeparate;
    private SpecEntryPanel subEntryXPath;
    private JRadioButton subIsNested;
    private SpecSubTagPanel subPath;
    private JRadioButton subFixWAttr;
    private JRadioButton subFixWLink;
    private JCheckBox createSubLink;
    private JCheckBox createMainLink;
    private KirrkirrButton subColorChooser, mainColorChooser;
    private JTextField subLinkRed, subLinkBlue, subLinkGreen;
    private JTextField mainLinkRed, mainLinkBlue, mainLinkGreen;

    //search info panel
    private JPanel searchPanel;
    private JTextField headwordRegex;
    private JTextField uniquifierRegex;
    private JList searchRegexList;
    private DefaultListModel searchRegexListModel;
    private JTextField searchName;
    private JTextField searchRegex;
    private Hashtable regexTable;
    private KirrkirrButton addRegex;
    private KirrkirrButton deleteRegex;
    private KirrkirrButton editRegex;


    //domain panel
    private JPanel domainInfoPanel;
    private SpecWithSubsPanel domainXPath;
    private JTextField converter;
    private KirrkirrButton browseConv;
    private KirrkirrButton createNewConv;

    //links panel
    private JPanel linksPanel;
    private JTextField refHeadwordXPath;
    private JTextField refUniquifierXPath;
    private JTextField linkName;
    private SpecWithSubsPanel linkXPath;
    private JTextField linkRed, linkBlue, linkGreen;
    private KirrkirrButton colorChooser;
    private JList linkList;
    private DefaultListModel linkListModel;
    private Hashtable linkTable;
    private KirrkirrButton addLink;
    private KirrkirrButton deleteLink;
    private KirrkirrButton editLink;

    //fuzzy spelling replacements panel
    private JPanel fuzzyPanel;
    private JTextField fuzzyHwordOrigRegex;
    private JTextField fuzzyHwordReplacementRegex;
    private JList fuzzyHwordList;
    private DefaultListModel fuzzyHwordListModel;
    private Hashtable fuzzyHwordTable;
    private KirrkirrButton addFuzzyHword, deleteFuzzyHword, editFuzzyHword;
    private JTextField fuzzyGlossOrigRegex;
    private JTextField fuzzyGlossReplacementRegex;
    private JList fuzzyGlossList;
    private DefaultListModel fuzzyGlossListModel;
    private Hashtable fuzzyGlossTable;
    private KirrkirrButton addFuzzyGloss, deleteFuzzyGloss, editFuzzyGloss;

    //stopwords panel
    private JPanel stopPanel;
    private JTextField stopword;
    private JList stopwordList;
    private DefaultListModel stopwordListModel;
    private KirrkirrButton addStopword, editStopword, deleteStopword;
    private JTextField stopchar;
    private JList stopcharList;
    private DefaultListModel stopcharListModel;
    private KirrkirrButton addStopchar, editStopchar, deleteStopchar;

    //optional info panels
    private JPanel optInfoPanel1;
    private JPanel optInfoPanel2;
    private SpecEntryPanel freqPath;
    private SpecEntryPanel posPath;
    private SpecEntryPanel sensePath;
    private SpecWithSubsPanel dialectPath;
    private SpecWithSubsPanel registerPath;
    private boolean useSense;

    //dicterror panel
    private JPanel dictErrorPanel;
    private KirrkirrButton editDictError;
    private JTextField dictError;
    private JList dictErrorList;
    private DefaultListModel dictErrorListModel;
    private KirrkirrButton addDictError, deleteDictError;

    //media panel
    private JPanel mediaPanel;
    private SpecWithSubsPanel imagePath;
    private SpecWithSubsPanel audioPath;
    private AuxFilePanel dictIcon;
    private AuxFilePanel glossIcon;

    //xsl files panel
    private JPanel xslPanel;
    private AuxFilePanel xslFile;
    private JTextField shortName;
    private JTextField xslDesc;
    private JList xslList;
    private DefaultListModel xslListModel;
    private Hashtable xslTable;
    private KirrkirrButton addXSL, deleteXSL, editXSL;

    //processing panel
    private JPanel processPanel;
    private JTextField newDirectory;
    private JTextField newDictFile;
    private JTextField newSpecFile;

    //index creator panel
    private JPanel indexPanel;
    private JTextField forwardIndex;
    private JTextField reverseIndex;
    private JTextField domainFile;

    //final panel
    private JPanel finalPanel;
    private JCheckBox loadNewDict;

    private Object[] theNames;

    /** A set of reasons (as text strings) why the dictionary needs
     *  to be preprocessed
     */
    private Hashtable reasons;
    private Hashtable parseRegexps;

    private String newSubentryXPath;
    private int subFixType;

    private static final Dimension minimumSize = new Dimension(500, 520);

    public ImportDialog(Kirrkirr parent, boolean modal) {
        this(parent, modal, null);
    }


    public ImportDialog(Kirrkirr parent, boolean modal, String fname) {
        //false = non-modal; i.e., other windows can be active
        super(parent.window, Helper.getTranslation(SC_PROCESS_DESC), modal);

        // place and size the dialog
        int xLoc = parent.getWidth()/2-250;
        int yLoc = parent.getHeight()/2-200;
        if (xLoc >= 0 && yLoc >= 0) {
            setLocation(xLoc, yLoc);
        }
        setSize(minimumSize);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        tagNames = new HashMap();
        subTags = new HashMap();
        possSenseTags = new Vector();
        //newSpecs = (new DOMImplementationImpl()).createDocument(null,
        //	DictionaryInfo.SPEC_ROOT, null);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());


        instructionsPanel = new JPanel();
        mainPanel.add(instructionsPanel, BorderLayout.NORTH);

        setupActionPanel();

        setupLoadFilePanel();
        setupGetSpecFilePanel();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                dispose();
            }
        });

        getContentPane().add(mainPanel);
        switchToPage(dictFilePanel, SC_DICT_HEADING);
        newDictOrSpec = false;
        setVisible(true);
    }

    public void  setupLoadFilePanel() {
        dictFilePanel = new JPanel();
        dictFilePanel.setLayout(new BorderLayout());
        fileGetterPanel = new AuxFilePanel(Helper.getTranslation(SC_DICT_FILE),
                KirrkirrFileFilter.XML_ENTRY);
        fileGetterPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        fileGetterPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        dictFilePanel.add(fileGetterPanel, BorderLayout.CENTER);
        dictFilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dictFilePanel.setAlignmentY(Component.CENTER_ALIGNMENT);
    }

    public void setupGetSpecFilePanel() {
        specFilePanel = new JPanel();
        specFilePanel.setLayout(new BorderLayout());
        specFileGetterPanel = new AuxFilePanel(Helper.getTranslation(SC_DICT_SPEC_FILE),
                KirrkirrFileFilter.XML_ENTRY);
        specFileGetterPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        specFileGetterPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        specFilePanel.add(specFileGetterPanel, BorderLayout.CENTER);
        specFilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        specFilePanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        JTextArea specificationFileLabel =
                new JTextArea(Helper.getTranslation(SC_SPEC_FILE_DESC));
        specificationFileLabel.setEditable(false);
        specificationFileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        specificationFileLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        specFilePanel.add(specificationFileLabel, BorderLayout.SOUTH);
    }

    public static boolean isSinglePath(String path) {
        int len = path.length();
        for (int i = 0; i < len; i++) {
            char curr = path.charAt(i);
            if (!Character.isLetterOrDigit(curr) && !Character.isWhitespace(curr)
                    && ((new Character(curr)).compareTo(new Character('/')) != 0)
                    && ((new Character(curr)).compareTo(new Character('@')) != 0))
                return false;
        }
        return true;
    }

    public SpecEntryPanel addSpecPanel(JPanel parent,
                                       String name, String old, boolean centered) {
        return addSpecPanel(parent, name, old, centered, true, true);
    }

    public SpecEntryPanel addSpecPanel(JPanel parent,
                                       String name, String old, boolean centered, boolean useAttr,
                                       boolean useCustom) {
        return addSpecPanel(parent, name, old, centered, useAttr, useCustom, null);
    }

    public SpecEntryPanel addSpecPanel(JPanel parent,
                                       String name, String old, boolean centered, boolean useAttr,
                                       boolean useCustom, Object[] names) {

        SpecEntryPanel panel;
        if (old == null) {
            panel = new SpecEntryPanel(Helper.getTranslation(name),
                            null, null, false, useAttr, useCustom, names);
        } else {
            if (isSinglePath(old)) {
                int atIndex = old.indexOf("@");
                if (atIndex < 0) {
                    panel = new SpecEntryPanel(Helper.getTranslation(name),
                                    old, null, false, useAttr, useCustom, names);
                } else {
                    panel = new SpecEntryPanel(Helper.getTranslation(name),
                                    old.substring(0, atIndex-1),
                                    old.substring(atIndex+1), false, useAttr, useCustom, names);
                }
            } else {
                panel = new SpecEntryPanel(Helper.getTranslation(name),
                                old, null, false, useAttr, useCustom, names);
            }
        }
        if (centered) {
            panel.add(Box.createHorizontalGlue());
        }
        panel.setAlignmentX(0);
        parent.add(panel);
        panel.setAlignmentX(0);
        if (centered) {
            panel.add(Box.createHorizontalGlue());
        }
        return panel;
    }

    public SpecWithSubsPanel addSpecWithSubsPanel(JPanel parent,
                                                  String name, String old, String subName, String sub,
                                                  boolean centered, boolean hasSubtags) {

        SpecWithSubsPanel panel;
        /*public SpecWithSubsPanel(String entryName, String entryPath, String attr,
                                 int subType, String subName, String sub, String additionalAttr,
                                 boolean subtagCard, boolean centered) {*/
        if (old == null) {
            panel = new SpecWithSubsPanel(Helper.getTranslation(name), null, null,
                            0, Helper.getTranslation(subName), null, null, hasSubtags, centered);
        } else {
            String entryPath, attr;
            int atIndex = old.indexOf("@");
            if (!isSinglePath(old) || atIndex < 0) {
                entryPath = old;
                attr = null;
            } else {
                entryPath = old.substring(0, atIndex-1);
                attr = old.substring(atIndex+1);
            }

            String subPath, subAttr;
            int subType;

            if (sub == null) {
                subType = 0;
                subPath = null;
                subAttr = null;
            } else {
                if (isSinglePath(sub)) {
                    subType = 1;
                    if (Arrays.binarySearch(theNames, old+"/"+sub) < 0) {
                        subType = 0;
                        subPath = null;
                        subAttr = null;
                    } else {
                        atIndex = sub.indexOf("@");
                        if (atIndex < 0) {
                            subPath = sub;
                            subAttr = null;
                        } else {
                            subPath = sub.substring(0, atIndex-1);
                            subAttr = sub.substring(atIndex+1);
                        }
                    }
                } else {
                    /*
                                          try {
                                                  Element root = oldSpecs.getDocument().getDocumentElement();
                                                  XPathAPI.eval(root,sub);

                                                  subType = 1;
                                          } catch (Exception ex) {
                                                  subType = 3;
                                          }*/
                    subType = 3;
                    subPath = sub;
                    subAttr = null;
                }
            }
            panel = new SpecWithSubsPanel(Helper.getTranslation(name),
                            entryPath, attr, subType, Helper.getTranslation(subName),
                            subPath, subAttr, hasSubtags, centered);
        }

        if (centered)
            panel.add(Box.createHorizontalGlue());

        panel.setAlignmentX(0);
        parent.add(panel);
        panel.setAlignmentX(0);
        if (centered)
            panel.add(Box.createHorizontalGlue());

        return panel;
    }

    public JTextField addTextInput(JPanel parent, String name, String old,
                                   boolean centered) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        if (centered)
            panel.add(Box.createHorizontalGlue());

        panel.add(Box.createHorizontalStrut(15));

        JLabel nameLabel = new JLabel(Helper.getTranslation(name));
        panel.add(nameLabel);

        JTextField field;
        if (old == null) {
            field = new JTextField("", 30);
        } else {
            field = new JTextField(old, 30);
        }
        field.setMaximumSize(new Dimension(200, 30));

        panel.setAlignmentX(0);
        panel.add(field);
        panel.add(Box.createHorizontalGlue());
        parent.add(panel);

        panel.add(Box.createHorizontalStrut(15));
        if (centered)
            panel.add(Box.createHorizontalGlue());

        return field;
    }

    public void addLabel(JPanel parent, String text, boolean centered) {
        addLabel(parent, text, centered, false);
    }
    public void addLabel(JPanel parent, String text, boolean centered, boolean red) {
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        if (centered)
            labelPanel.add(Box.createHorizontalGlue());
        else
            labelPanel.add(Box.createHorizontalStrut(15));
        JLabel label = new JLabel(Helper.getTranslation(text));
        labelPanel.add(label);
        labelPanel.setAlignmentX(0);
        if (red) label.setForeground(Color.red);
        parent.add(labelPanel);

        if (centered)
            labelPanel.add(Box.createHorizontalGlue());
    }

    public void addSpecLister(JPanel parent, KirrkirrButton add, KirrkirrButton edit,
                              KirrkirrButton delete, JList list) {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalGlue());

        JPanel addPanel = new JPanel();
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.X_AXIS));
        addPanel.add(Box.createHorizontalGlue());
        addPanel.add(add);
        addPanel.add(Box.createHorizontalGlue());
        addPanel.setAlignmentX(0);
        parent.add(addPanel);

        list.setMaximumSize(new Dimension(500, 80));
        list.setMinimumSize(new Dimension(200, 80));
        //list.setPreferredSize(new Dimension(200, 80));
        list.setVisibleRowCount(4);

        JScrollPane scroller = new JScrollPane(list);
        scroller.setMaximumSize(new Dimension(520, 100));
        scroller.setMinimumSize(new Dimension(220, 100));
        //scroller.setPreferredSize(new Dimension(200, 80));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(scroller);
        panel.add(Box.createHorizontalGlue());
        panel.setAlignmentX(0);
        parent.add(panel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(edit);
        buttonPanel.add(delete);
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.setAlignmentX(0);
        parent.add(buttonPanel);
    }

    public void setupBasicInfoPanel() {
        basicInfoPanel = new JPanel();
        basicInfoPanel.setLayout(new BoxLayout(basicInfoPanel, BoxLayout.PAGE_AXIS));
        //basicInfoPanel.setLayout(new SpringLayout());
        theNames = (tagNames.keySet().toArray());
        Arrays.sort(theNames);

        basicInfoPanel.add(Box.createVerticalStrut(5));

        if (oldSpecs == null)
            dictName = addTextInput(basicInfoPanel, SC_DICT_NAME, null, false);
        else
            dictName = addTextInput(basicInfoPanel,
                            SC_DICT_NAME, oldSpecs.getDictLangName(), false);

        if (oldSpecs == null)
            glossLang = addTextInput(basicInfoPanel, SC_GLOSS_DICT_LANG, null, false);
        else
            glossLang = addTextInput(basicInfoPanel,
                    SC_GLOSS_DICT_LANG, oldSpecs.getGlossLangName(), false);

        basicInfoPanel.add(Box.createVerticalStrut(5));

        if (oldSpecs == null)
            dictEntryXPath = addSpecPanel(basicInfoPanel, SC_ENTRY_PATH, null, false,
                    false, false);
        else
            dictEntryXPath = addSpecPanel(basicInfoPanel, SC_ENTRY_PATH,
                    oldSpecs.getDictionaryEntryXPath(), false, false, false);

        JPanel hasSE = new JPanel();
        hasSubEntries = new JCheckBox(Helper.getTranslation(SC_HAS_SUBENTRIES));
        if (oldSpecs != null && oldSpecs.getSubwordXPath() != null)
            hasSubEntries.setSelected(true);
        hasSubEntries.setAlignmentX(Component.CENTER_ALIGNMENT);
        hasSE.add(hasSubEntries);
        hasSE.setAlignmentX(0);
        basicInfoPanel.add(hasSE);

        if (oldSpecs == null)
            headwordPath = addSpecPanel(basicInfoPanel, SC_HEADWORD_PATH, null, false);
        else
            headwordPath = addSpecPanel(basicInfoPanel, SC_HEADWORD_PATH,
                    oldSpecs.getHeadwordXPath(), false);

        if (oldSpecs == null)
            uniquifierPath = addSpecPanel(basicInfoPanel, SC_UNIQUIFIER_PATH, null, false);
        else
            uniquifierPath = addSpecPanel(basicInfoPanel, SC_UNIQUIFIER_PATH,
                    oldSpecs.getUniquifierXPath(), false);

        if (oldSpecs == null)
            glossXPath = addSpecPanel(basicInfoPanel, SC_GLOSS_PATH, null, false);
        else
            glossXPath = addSpecPanel(basicInfoPanel, SC_GLOSS_PATH,
                    oldSpecs.getGlossXPath(), false);

        basicInfoPanel.add(Box.createVerticalGlue());
        basicInfoPanel.add(Box.createHorizontalGlue());
    }

    public void setupSubentryFixPanel() {
        subEntryFixPanel = new JPanel();
        subEntryFixPanel.setLayout(new BoxLayout(subEntryFixPanel, BoxLayout.PAGE_AXIS));
        subEntryFixPanel.add(Box.createVerticalStrut(15));
        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
        subIsSeparate = new JRadioButton(Helper.getTranslation(SC_SUBENTRY_ATTR));
        subIsSeparate.addActionListener(this);
        subIsSeparate.setSelected(true);
        subIsNested = new JRadioButton(Helper.getTranslation(SC_SUBENTRY_XPATH));
        subIsNested.addActionListener(this);
        ButtonGroup first = new ButtonGroup();
        first.add(subIsSeparate);
        first.add(subIsNested);
        radioPanel.add(Box.createHorizontalGlue());
        radioPanel.add(subIsSeparate);
        radioPanel.add(subIsNested);
        radioPanel.add(Box.createHorizontalGlue());
        radioPanel.setAlignmentX(0);
        subEntryFixPanel.add(radioPanel);

        subCardsPanel = new JPanel(new CardLayout());
        if (oldSpecs == null) {
            subEntryXPath = new SpecEntryPanel(Helper.getTranslation(SC_SUBENTRY_ATTR),
                    null, null, false, true, true);
        } else {
            String old = oldSpecs.getSubwordXPath();
            if (old == null)
                subEntryXPath =
                        new SpecEntryPanel(Helper.getTranslation(SC_SUBENTRY_ATTR),
                                null, null, false, true, true);
            else {
                if (isSinglePath(old)) {
                    int atIndex = old.indexOf("@");
                    if (atIndex < 0)
                        subEntryXPath =
                                new SpecEntryPanel(Helper.getTranslation(SC_SUBENTRY_ATTR),
                                        old, null, false, true, true);
                    else
                        subEntryXPath =
                                new SpecEntryPanel(Helper.getTranslation(SC_SUBENTRY_ATTR),
                                        old.substring(0, atIndex-1),
                                        old.substring(atIndex+1), false, true, true);
                } else {
                    subEntryXPath =
                            new SpecEntryPanel(Helper.getTranslation(SC_SUBENTRY_ATTR),
                                    old, null, false, true, true);
                }
            }
        }
        subCardsPanel.add(subEntryXPath, Helper.getTranslation(SC_SUBENTRY_ATTR));

        subFixCardPanel = new JPanel();
        subFixCardPanel.setLayout(new BoxLayout(subFixCardPanel, BoxLayout.PAGE_AXIS));
        subFixCardPanel.add(Box.createVerticalStrut(30));
        subPath = new SpecSubTagPanel(Helper.getTranslation(SC_SUBENTRY_NEST),
                null, null, dictEntryXPath.getText());
        subPath.setAlignmentX(0);
        subFixCardPanel.add(subPath);
        //subFixCardPanel.add(Box.createVerticalStrut(10));
        subFixWAttr = new JRadioButton(Helper.getTranslation(SC_USE_ATTR));
        subFixWAttr.setSelected(true);
        subFixWAttr.addActionListener(this);
        subFixWAttr.setAlignmentX(0);
        subFixCardPanel.add(subFixWAttr);
        subFixCardPanel.add(Box.createVerticalStrut(15));
        subFixWLink = new JRadioButton(Helper.getTranslation(SC_USE_LINK));
        subFixWLink.addActionListener(this);
        subFixWLink.setAlignmentX(0);
        subFixCardPanel.add(subFixWLink);
        ButtonGroup second = new ButtonGroup();
        second.add(subFixWAttr);
        second.add(subFixWLink);

        JPanel createSubLinkPanel = new JPanel();
        createSubLinkPanel.setLayout(new BoxLayout(createSubLinkPanel, BoxLayout.X_AXIS));
        createSubLink = new JCheckBox(Helper.getTranslation(SC_CREATE_SUB_LINK));
        createSubLink.setEnabled(false);
        createSubLink.addActionListener(this);
        createSubLinkPanel.add(Box.createHorizontalGlue());
        createSubLinkPanel.add(createSubLink);
        createSubLinkPanel.add(Box.createHorizontalGlue());
        createSubLinkPanel.setAlignmentX(0);
        subFixCardPanel.add(createSubLinkPanel);

        JPanel subLink = new JPanel();
        subLink.setLayout(new BoxLayout(subLink, BoxLayout.X_AXIS));
        subLink.setMaximumSize(new Dimension(600, 40));
        subLink.add(Box.createHorizontalGlue());
        subLinkRed = new JTextField(3);
        subLinkGreen = new JTextField(3);
        subLinkBlue = new JTextField(3);
        JLabel colorLabel = new JLabel(Helper.getTranslation(SC_LINK_COLOR));
        JLabel redLabel = new JLabel(Helper.getTranslation(SC_RED));
        redLabel.setMaximumSize(new Dimension(20, 15));
        JLabel greenLabel = new JLabel(Helper.getTranslation(SC_GREEN));
        greenLabel.setMaximumSize(new Dimension(20, 15));
        JLabel blueLabel = new JLabel(Helper.getTranslation(SC_BLUE));
        blueLabel.setMaximumSize(new Dimension(20, 15));
        subLink.add(colorLabel);
        subLink.add(Box.createHorizontalStrut(20));
        subLink.add(redLabel);
        subLink.add(subLinkRed);
        subLink.add(Box.createHorizontalStrut(5));
        subLink.add(greenLabel);
        subLink.add(subLinkGreen);
        subLink.add(Box.createHorizontalStrut(5));
        subLink.add(blueLabel);
        subLink.add(subLinkBlue);
        subLink.add(Box.createHorizontalStrut(10));
        subColorChooser = new KirrkirrButton(Helper.getTranslation(SC_SELECT_COLOR), this);
        subLink.add(subColorChooser);
        subLink.add(Box.createHorizontalGlue());
        subLink.setAlignmentX(0);
        subLinkRed.setEnabled(false);
        subLinkGreen.setEnabled(false);
        subLinkBlue.setEnabled(false);
        subColorChooser.setEnabled(false);
        subFixCardPanel.add(subLink);

        subEntryFixPanel.add(Box.createVerticalGlue());

        JPanel createMainLinkPanel = new JPanel();
        createMainLinkPanel.setLayout(new BoxLayout(createMainLinkPanel, BoxLayout.X_AXIS));
        createMainLink = new JCheckBox(Helper.getTranslation(SC_CREATE_MAIN_LINK));
        createMainLink.setEnabled(false);
        createMainLink.addActionListener(this);
        createMainLinkPanel.add(Box.createHorizontalGlue());
        createMainLinkPanel.add(createMainLink);
        createMainLinkPanel.add(Box.createHorizontalGlue());
        createMainLinkPanel.setAlignmentX(0);
        subFixCardPanel.add(createMainLinkPanel);

        JPanel mainLink = new JPanel();
        mainLink.setLayout(new BoxLayout(mainLink, BoxLayout.X_AXIS));
        mainLink.setMaximumSize(new Dimension(600, 40));
        mainLink.add(Box.createHorizontalGlue());
        mainLinkRed = new JTextField(3);
        mainLinkGreen = new JTextField(3);
        mainLinkBlue = new JTextField(3);
        JLabel colorLabel2 = new JLabel(Helper.getTranslation(SC_LINK_COLOR));
        JLabel redLabel2 = new JLabel(Helper.getTranslation(SC_RED));
        redLabel2.setMaximumSize(new Dimension(15, 15));
        JLabel greenLabel2 = new JLabel(Helper.getTranslation(SC_GREEN));
        greenLabel.setMaximumSize(new Dimension(15, 15));
        JLabel blueLabel2 = new JLabel(Helper.getTranslation(SC_BLUE));
        blueLabel2.setMaximumSize(new Dimension(15, 15));
        mainLink.add(colorLabel2);
        mainLink.add(Box.createHorizontalStrut(20));
        mainLink.add(redLabel2);
        mainLink.add(mainLinkRed);
        mainLink.add(Box.createHorizontalStrut(5));
        mainLink.add(greenLabel2);
        mainLink.add(mainLinkGreen);
        mainLink.add(Box.createHorizontalStrut(5));
        mainLink.add(blueLabel2);
        mainLink.add(mainLinkBlue);
        mainLink.add(Box.createHorizontalStrut(10));
        mainColorChooser = new KirrkirrButton(Helper.getTranslation(SC_SELECT_COLOR), this);
        mainLink.add(mainColorChooser);
        mainLink.add(Box.createHorizontalGlue());
        mainLink.setAlignmentX(0);
        mainLinkRed.setEnabled(false);
        mainLinkGreen.setEnabled(false);
        mainLinkBlue.setEnabled(false);
        mainColorChooser.setEnabled(false);
        subFixCardPanel.add(mainLink);
        subFixCardPanel.add(Box.createVerticalGlue());

        subCardsPanel.add(subFixCardPanel, Helper.getTranslation(SC_SUBENTRY_XPATH));
        subCardsPanel.setAlignmentX(0);
        subEntryFixPanel.add(subCardsPanel);
        /*
                 JPanel help = new JPanel();
                 JTextArea subentryHelpLabel =
                         new JTextArea(Helper.getTranslation(SC_SUBENTRY_DESC));
                 subentryHelpLabel.setEditable(false);
                 subentryHelpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                 subentryHelpLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
                 help.add(subentryHelpLabel);
                 help.setAlignmentX(0);
                 subEntryFixPanel.add(help);
                 subEntryFixPanel.add(Box.createVerticalStrut(15));

                 subEntryXPath = addSpecPanel(subEntryFixPanel, SC_SUBENTRY_PATH, null, false);
                 */
        subEntryFixPanel.add(Box.createVerticalGlue());
    }

    public void setupSearchPanel() {
        searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.PAGE_AXIS));
        searchPanel.add(Box.createVerticalStrut(20));

        if (oldSpecs == null)
            headwordRegex = addTextInput(searchPanel, SC_HEADWORD_REGEX, null, false);
        else
            headwordRegex = addTextInput(searchPanel,
                    SC_HEADWORD_REGEX, oldSpecs.getHeadwordRegexp(), false);

        searchPanel.add(Box.createVerticalStrut(10));

        if (oldSpecs == null)
            uniquifierRegex = addTextInput(searchPanel, SC_UNIQUIFIER_REGEX, null, false);
        else
            uniquifierRegex = addTextInput(searchPanel,
                    SC_UNIQUIFIER_REGEX, oldSpecs.getUniquifierRegexp(), false);

        searchPanel.add(Box.createVerticalStrut(20));

        addLabel(searchPanel, SC_SEARCH_REGEX, false);
        searchPanel.add(Box.createVerticalStrut(10));
        searchName = addTextInput(searchPanel, SC_SEARCH_NAME, null, true);
        searchRegex = addTextInput(searchPanel, SC_REGEX, null, true);

        addRegex = new KirrkirrButton(Helper.getTranslation(SC_ADD), this);
        editRegex = new KirrkirrButton(Helper.getTranslation(SC_EDIT), this);
        deleteRegex = new KirrkirrButton(Helper.getTranslation(SC_DELETE), this);
        searchRegexListModel = new DefaultListModel();
        if (oldSpecs != null) {
            regexTable = oldSpecs.getSearchRegexps();
            int size = regexTable.size();
            Object[] regexps = regexTable.keySet().toArray();
            for (int i = 0; i < size; i++)
                searchRegexListModel.addElement(regexps[i]);
        }
        else
            regexTable = new Hashtable();
        searchRegexList = new JList(searchRegexListModel);
        addSpecLister(searchPanel, addRegex, editRegex, deleteRegex, searchRegexList);
    }

    public void setConverter(String conv) {
        converter.setText(conv);
    }

    public void setupDomainPanel() {
        domainInfoPanel = new JPanel();
        domainInfoPanel.setLayout(new BoxLayout(domainInfoPanel, BoxLayout.PAGE_AXIS));
        /*public SpecWithSubsPanel addSpecWithSubsPanel(JPanel parent,
                         String name, String old, String subName, String sub, boolean centered) {*/
        if (oldSpecs == null)
            domainXPath =
                    addSpecWithSubsPanel(domainInfoPanel, SC_DOMAIN_PATH, null,
                            SC_SUBDOMAIN_PATH, null, false, true);
        else
            domainXPath = addSpecWithSubsPanel(domainInfoPanel, SC_DOMAIN_PATH,
                    oldSpecs.getDomainXPath(),
                    SC_SUBDOMAIN_PATH, oldSpecs.getDomainComponentXPath(), false, true);

        JPanel domainConvPanel = new JPanel();
        domainConvPanel.setLayout(new BoxLayout(domainConvPanel, BoxLayout.X_AXIS));

        //set up the label
        addLabel(domainInfoPanel, SC_DOMAIN_CONV, false);
        //fileTypeLabel.setMaximumSize(new Dimension(160,30));
        //fileTypeLabel.setPreferredSize(new Dimension(160,30));
        //fileTypeLabel.setAlignmentX(0);
        domainConvPanel.add(Box.createHorizontalStrut(15));

        domainConvPanel.add(Box.createHorizontalStrut(5));

        //set up the text field
        converter = new JTextField(40);
        converter.setMaximumSize(new Dimension(200,30));
        domainConvPanel.add(converter);
        domainConvPanel.add(Box.createHorizontalStrut(5));

        //set up the browse button
        browseConv = new KirrkirrButton(Helper.getTranslation(SC_BROWSE), null, this);
        browseConv.setMaximumSize(new Dimension(60, 30));
        domainConvPanel.add(browseConv);
        domainConvPanel.add(Box.createHorizontalStrut(5));

        //set up create button
        createNewConv = new KirrkirrButton(Helper.getTranslation(SC_CREATE_NEW), null, this);
        domainConvPanel.add(createNewConv);

        domainConvPanel.setAlignmentX(0);
        domainInfoPanel.add(domainConvPanel);
        //domainConvPanel.add(Box.createHorizontalGlue());
        domainInfoPanel.add(Box.createVerticalStrut(15));
        domainInfoPanel.add(Box.createVerticalGlue());
    }

    public void setupLinkPanel() {
        linksPanel = new JPanel();
        linksPanel.setLayout(new BoxLayout(linksPanel, BoxLayout.PAGE_AXIS));

        searchPanel.add(Box.createVerticalStrut(20));

        if (oldSpecs == null)
            refHeadwordXPath = addTextInput(linksPanel,
                    SC_REF_HEADWORD_XPATH, null, false);
        else
            refHeadwordXPath = addTextInput(linksPanel,
                    SC_REF_HEADWORD_XPATH, oldSpecs.getRefHeadwordXPath(), false);

        searchPanel.add(Box.createVerticalStrut(10));

        if (oldSpecs == null)
            refUniquifierXPath = addTextInput(linksPanel, SC_REF_UNIQUIFIER_XPATH, null, false);
        else
            refUniquifierXPath = addTextInput(linksPanel,
                    SC_REF_UNIQUIFIER_XPATH, oldSpecs.getRefUniquePaddingXPath(), false);

        linksPanel.add(Box.createVerticalStrut(20));

        linkName = addTextInput(linksPanel, SC_LINK_NAME, null, false);

        linksPanel.add(Box.createVerticalStrut(5));

        linkXPath =
                addSpecWithSubsPanel(linksPanel, SC_LINK_PATH, null,
                        "", null, true, false);

        JPanel colorPanel = new JPanel();
        colorPanel.setLayout(new BoxLayout(colorPanel, BoxLayout.X_AXIS));
        colorPanel.add(Box.createHorizontalGlue());
        colorPanel.add(Box.createHorizontalStrut(15));
        linkRed = new JTextField(3);
        linkGreen = new JTextField(3);
        linkBlue = new JTextField(3);
        JLabel colorLabel = new JLabel(Helper.getTranslation(SC_LINK_COLOR));
        JLabel redLabel = new JLabel(Helper.getTranslation(SC_RED));
        redLabel.setMaximumSize(new Dimension(20, 15));
        JLabel greenLabel = new JLabel(Helper.getTranslation(SC_GREEN));
        greenLabel.setMaximumSize(new Dimension(20, 15));
        JLabel blueLabel = new JLabel(Helper.getTranslation(SC_BLUE));
        blueLabel.setMaximumSize(new Dimension(20, 15));
        colorPanel.add(colorLabel);
        colorPanel.add(Box.createHorizontalStrut(20));
        colorPanel.add(redLabel);
        colorPanel.add(linkRed);
        colorPanel.add(Box.createHorizontalStrut(5));
        colorPanel.add(greenLabel);
        colorPanel.add(linkGreen);
        colorPanel.add(Box.createHorizontalStrut(5));
        colorPanel.add(blueLabel);
        colorPanel.add(linkBlue);
        colorPanel.add(Box.createHorizontalStrut(10));
        colorChooser = new KirrkirrButton(Helper.getTranslation(SC_SELECT_COLOR), this);
        colorPanel.add(colorChooser);
        colorPanel.add(Box.createHorizontalStrut(15));
        colorPanel.add(Box.createHorizontalGlue());
        colorPanel.setAlignmentX(0);
        linksPanel.add(colorPanel);
        linksPanel.add(Box.createVerticalStrut(5));

        JPanel linkListPanel = new JPanel();
        linkListPanel.setLayout(new BoxLayout(linkListPanel, BoxLayout.X_AXIS));
        linkListPanel.add(Box.createHorizontalGlue());

        addLink = new KirrkirrButton(Helper.getTranslation(SC_ADD), this);
        editLink = new KirrkirrButton(Helper.getTranslation(SC_EDIT), this);
        deleteLink = new KirrkirrButton(Helper.getTranslation(SC_DELETE), this);
        linkListModel = new DefaultListModel();
        linkTable = new Hashtable();
        if (oldSpecs != null) {
            int size = oldSpecs.getNumLinks();
            for (int i = 0; i < size; i++) {
                String name = oldSpecs.getLinkName(i);
                String xpath = oldSpecs.getLinkXPath(i);
                ///will need to add function to dictionaryinfo!!!!1
                String sub = null;
                Color color = oldSpecs.getLinkColor(i);
                Link newLink = new Link(name, xpath, sub, 0, color.getRed(), color.getGreen(),
                        color.getBlue());
                linkTable.put(name, newLink);
                linkListModel.addElement(name);
            }
        }
        linkList = new JList(linkListModel);
        linkList.setCellRenderer(new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list,
                                                          Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel item = new JLabel(value.toString());
                item.setOpaque(true);
                Link itemLink = (Link)linkTable.get(value.toString());
                Color itemColor = new Color(itemLink.r, itemLink.g, itemLink.b);
                item.setForeground(itemColor);
                if (isSelected || cellHasFocus)
                    item.setBackground(Color.white);
                return item;
            }
        });
        addSpecLister(linksPanel, addLink, editLink, deleteLink, linkList);

        if (hasSubEntries.isSelected() &&
                (createSubLink.isSelected() || createMainLink.isSelected()))
            addLabel(linksPanel, SC_SUB_LINK_MESSAGE, true, true);
    }

    public void setupFuzzyPanel() {
        fuzzyPanel = new JPanel();
        fuzzyPanel.setLayout(new BoxLayout(fuzzyPanel, BoxLayout.PAGE_AXIS));
        fuzzyPanel.add(Box.createVerticalStrut(20));

        addLabel(fuzzyPanel, SC_FUZZY_HWORD_SUBS, false);
        fuzzyPanel.add(Box.createVerticalStrut(10));

        fuzzyHwordOrigRegex = addTextInput(fuzzyPanel, SC_FUZZY_ORIG_REGEX, null, true);
        fuzzyHwordReplacementRegex =
                addTextInput(fuzzyPanel, SC_FUZZY_NEW_REGEX, null, true);

        addFuzzyHword = new KirrkirrButton(Helper.getTranslation(SC_ADD), this);
        editFuzzyHword = new KirrkirrButton(Helper.getTranslation(SC_EDIT), this);
        deleteFuzzyHword = new KirrkirrButton(Helper.getTranslation(SC_DELETE), this);
        fuzzyHwordListModel = new DefaultListModel();
        fuzzyHwordTable = new Hashtable();
        if (oldSpecs != null) {
            Regex[] oldFuzzyHwords = oldSpecs.getHeadwordLanguageSubs();
            for (int i = 0; i < oldFuzzyHwords.length; i++)
            {
                String pattern = ((OroRegex)oldFuzzyHwords[i]).getOriginalPattern();
                String sub = ((OroRegex)oldFuzzyHwords[i]).getOriginalSub();
                fuzzyHwordListModel.addElement(pattern);
                fuzzyHwordTable.put(pattern, sub);
            }
        }
        fuzzyHwordList = new JList(fuzzyHwordListModel);
        addSpecLister(fuzzyPanel, addFuzzyHword, editFuzzyHword, deleteFuzzyHword,
                fuzzyHwordList);
        //
        fuzzyPanel.add(Box.createVerticalStrut(10));

        addLabel(fuzzyPanel, SC_FUZZY_GLOSS_SUBS, false);
        fuzzyPanel.add(Box.createVerticalStrut(10));
        fuzzyGlossOrigRegex = addTextInput(fuzzyPanel, SC_FUZZY_ORIG_REGEX, null, true);
        fuzzyGlossReplacementRegex =
                addTextInput(fuzzyPanel, SC_FUZZY_NEW_REGEX, null, true);

        addFuzzyGloss = new KirrkirrButton(Helper.getTranslation(SC_ADD), this);
        editFuzzyGloss = new KirrkirrButton(Helper.getTranslation(SC_EDIT), this);
        deleteFuzzyGloss = new KirrkirrButton(Helper.getTranslation(SC_DELETE), this);
        fuzzyGlossListModel = new DefaultListModel();
        fuzzyGlossTable = new Hashtable();
        if (oldSpecs != null) {
            Regex[] oldFuzzyGloss = oldSpecs.getGlossLanguageSubs();
            for (int i = 0; i < oldFuzzyGloss.length; i++)
            {
                String pattern = ((OroRegex)oldFuzzyGloss[i]).getOriginalPattern();
                String sub = ((OroRegex)oldFuzzyGloss[i]).getOriginalSub();
                fuzzyGlossListModel.addElement(pattern);
                fuzzyGlossTable.put(pattern, sub);
            }
        }
        fuzzyGlossList = new JList(fuzzyGlossListModel);
        addSpecLister(fuzzyPanel, addFuzzyGloss, editFuzzyGloss, deleteFuzzyGloss,
                fuzzyGlossList);
    }

    public void setupStopPanel() {
        stopPanel = new JPanel();
        stopPanel.setLayout(new BoxLayout(stopPanel, BoxLayout.PAGE_AXIS));
        stopPanel.add(Box.createVerticalStrut(30));

        stopword = addTextInput(stopPanel, SC_STOPWORD, null, true);
        addStopword = new KirrkirrButton(Helper.getTranslation(SC_ADD), this);
        editStopword = new KirrkirrButton(Helper.getTranslation(SC_EDIT), this);
        deleteStopword = new KirrkirrButton(Helper.getTranslation(SC_DELETE), this);
        stopwordListModel = new DefaultListModel();
        if (oldSpecs != null) {
            Vector stopwords = oldSpecs.getStopWords();
            for (int i = 0; i < stopwords.size(); i++)
            {
                String word = (String)stopwords.elementAt(i);
                stopwordListModel.addElement(word);
            }
        }
        stopwordList = new JList(stopwordListModel);
        addSpecLister(stopPanel, addStopword, editStopword, deleteStopword,
                stopwordList);
        stopPanel.add(Box.createVerticalStrut(20));

        stopchar = addTextInput(stopPanel, SC_STOPCHAR, null, true);
        stopchar.setColumns(1);
        addStopchar = new KirrkirrButton(Helper.getTranslation(SC_ADD), this);
        editStopchar = new KirrkirrButton(Helper.getTranslation(SC_EDIT), this);
        deleteStopchar = new KirrkirrButton(Helper.getTranslation(SC_DELETE), this);
        stopcharListModel = new DefaultListModel();
        if (oldSpecs != null) {
            Vector stopchars = oldSpecs.getStopChars();
            for (int i = 0; i < stopchars.size(); i++)
            {
                String character = (String)stopchars.elementAt(i);
                stopcharListModel.addElement(character);
            }
        }
        stopcharList = new JList(stopcharListModel);
        stopcharList.setPreferredSize(new Dimension(30, 300));
        addSpecLister(stopPanel, addStopchar, editStopchar, deleteStopchar,
                stopcharList);
    }

    public void getSenseChoices() {
        String entryPath = dictEntryXPath.getText()+"/";
        possSenseTags.clear();
        for (int i = 0; i < theNames.length; i++) {
            String name = (String)theNames[i];
            int eIndex = name.lastIndexOf(entryPath);
            if (eIndex != -1) {
                eIndex+=entryPath.length();
                String end = name.substring(eIndex);
                if (end.indexOf("/") == -1)
                    possSenseTags.add(name);
            }
        }
    }

    public void setupOptInfoPanel() {
        optInfoPanel1 = new JPanel();
        //optInfoPanel1.setLayout(new BoxLayout(optInfoPanel1, BoxLayout.PAGE_AXIS));
        optInfoPanel1.setLayout(new GridLayout(3, 1));
        //optInfoPanel1.add(Box.createVerticalStrut(10));

        getSenseChoices();
        useSense = false;
        if (oldSpecs != null) {
            sensePath = addSpecPanel(optInfoPanel1, SC_SENSE,
                    oldSpecs.getSenseXPath(), false, false, false,
                    possSenseTags.toArray());
            if (oldSpecs.getSenseXPath() != null) useSense = true;
        }
        else
            sensePath = addSpecPanel(optInfoPanel1, SC_SENSE, null, false, false, false,
                    possSenseTags.toArray());

        if (oldSpecs != null)
            freqPath =
                    addSpecPanel(optInfoPanel1, SC_FREQ, oldSpecs.getFrequencyXPath(), false);
        else
            freqPath = addSpecPanel(optInfoPanel1, SC_FREQ, null, false);

        if (oldSpecs != null)
            posPath =
                    addSpecPanel(optInfoPanel1, SC_POS, oldSpecs.getPOSXPath(), false);
        else
            posPath = addSpecPanel(optInfoPanel1, SC_POS, null, false);

    }

    public void setupOptInfoPanel2() {
        optInfoPanel2 = new JPanel();
        optInfoPanel2.setLayout(new BoxLayout(optInfoPanel2, BoxLayout.PAGE_AXIS));
        optInfoPanel2.add(Box.createVerticalStrut(10));

        if (oldSpecs != null)
            dialectPath = addSpecWithSubsPanel(optInfoPanel2, SC_DIALECT,
                    oldSpecs.getDialectXPath(), "", null, false, false);
        else
            dialectPath = addSpecWithSubsPanel(optInfoPanel2, SC_DIALECT,
                    null, "", null, false, false);

        if (oldSpecs != null)
            registerPath = addSpecWithSubsPanel(optInfoPanel2, SC_REGISTER,
                    oldSpecs.getRegisterXPath(), "", null, false, false);
        else
            registerPath = addSpecWithSubsPanel(optInfoPanel2, SC_REGISTER,
                    null, "", null, false, false);
    }

    public void setupDictErrorPanel() {
        dictErrorPanel = new JPanel();
        dictErrorPanel.setLayout(new BoxLayout(dictErrorPanel, BoxLayout.PAGE_AXIS));
        dictErrorPanel.add(Box.createVerticalGlue());

        dictError = addTextInput(dictErrorPanel, SC_DICTERROR, null, true);
        addDictError = new KirrkirrButton(Helper.getTranslation(SC_ADD), this);
        editDictError = new KirrkirrButton(Helper.getTranslation(SC_EDIT), this);
        deleteDictError = new KirrkirrButton(Helper.getTranslation(SC_DELETE), this);
        dictErrorListModel = new DefaultListModel();
        if (oldSpecs != null) {
            Vector dicterrors = oldSpecs.getDictErrors();
            for (int i = 0; i < dicterrors.size(); i++)
            {
                String error = (String)dicterrors.elementAt(i);
                dictErrorListModel.addElement(error);
            }
        }
        dictErrorList = new JList(dictErrorListModel);
        addSpecLister(dictErrorPanel, addDictError, editDictError, deleteDictError,
                dictErrorList);
        dictErrorPanel.add(Box.createVerticalGlue());
    }

    public void setupMediaPanel() {
        mediaPanel = new JPanel();
        mediaPanel.setLayout(new BoxLayout(mediaPanel, BoxLayout.PAGE_AXIS));
        mediaPanel.add(Box.createVerticalStrut(20));

        if (oldSpecs != null)
            imagePath = addSpecWithSubsPanel(mediaPanel, SC_IMAGE_PATH,
                    oldSpecs.getImagesXPath(), "", null, false, false);
        else
            imagePath = addSpecWithSubsPanel(mediaPanel, SC_IMAGE_PATH,
                    null, "", null, false, false);

        if (oldSpecs != null)
            audioPath = addSpecWithSubsPanel(mediaPanel, SC_AUDIO_PATH,
                    oldSpecs.getAudioXPath(), "", null, false, false);
        else
            audioPath = addSpecWithSubsPanel(mediaPanel, SC_AUDIO_PATH,
                    null, "", null, false, false);

        addLabel(mediaPanel, SC_IN_IMAGE_FOLDER, false);
        mediaPanel.add(Box.createVerticalStrut(15));

        dictIcon = new AuxFilePanel(Helper.getTranslation(SC_DICT_ICON),
                KirrkirrFileFilter.IMAGE_ENTRY, true);
        if (oldSpecs != null) dictIcon.setText(oldSpecs.getDictLangIcon());
        dictIcon.setAlignmentX(0);
        dictIcon.setInnerFolder("images");
        mediaPanel.add(dictIcon);

        glossIcon = new AuxFilePanel(Helper.getTranslation(SC_GLOSS_ICON),
                KirrkirrFileFilter.IMAGE_ENTRY, true);
        if (oldSpecs != null) glossIcon.setText(oldSpecs.getGlossLangIcon());
        glossIcon.setAlignmentX(0);
        glossIcon.setInnerFolder("images");
        mediaPanel.add(glossIcon);

        mediaPanel.add(Box.createVerticalGlue());
    }

    public void setupXslPanel() {
        xslPanel = new JPanel();
        xslPanel.setLayout(new BoxLayout(xslPanel, BoxLayout.PAGE_AXIS));
        xslPanel.add(Box.createVerticalStrut(10));

        addLabel(xslPanel, SC_IN_XSL_FOLDER, false);
        xslPanel.add(Box.createVerticalStrut(15));

        addLabel(xslPanel, SC_USE_UNDERSCORE, false);
        xslPanel.add(Box.createVerticalStrut(15));

        xslFile = new AuxFilePanel(Helper.getTranslation(SC_XSL_FILE),
                KirrkirrFileFilter.XSL_ENTRY, true);
        xslFile.setInnerFolder("xsl");
        xslFile.setAlignmentX(0);
        xslPanel.add(xslFile);
        shortName = addTextInput(xslPanel, SC_XSL_NAME, null, false);
        xslDesc = addTextInput(xslPanel, SC_XSL_DESC, null, false);


        JPanel xslListPanel = new JPanel();
        xslListPanel.setLayout(new BoxLayout(xslListPanel, BoxLayout.X_AXIS));
        xslListPanel.add(Box.createHorizontalGlue());

        addXSL = new KirrkirrButton(Helper.getTranslation(SC_ADD), this);
        editXSL = new KirrkirrButton(Helper.getTranslation(SC_EDIT), this);
        deleteXSL = new KirrkirrButton(Helper.getTranslation(SC_DELETE), this);
        xslListModel = new DefaultListModel();
        xslTable = new Hashtable();
        if (oldSpecs != null) {
            int size = oldSpecs.getNumXslFiles();
            for (int i = 0; i < size; i++) {
                String file = oldSpecs.getXslFilename(i);
                String name = oldSpecs.getXslShortname(i);
                String desc = oldSpecs.getXslDescription(i);
                XslFile newXsl = new XslFile(file, name, desc);

                xslTable.put(name, newXsl);
                xslListModel.addElement(name);
            }
        }
        xslList = new JList(xslListModel);

        addSpecLister(xslPanel, addXSL, editXSL, deleteXSL, xslList);

        xslPanel.add(Box.createVerticalGlue());
    }

    public void setupProcessPanel() {
        processPanel = new JPanel();
        processPanel.setLayout(new BoxLayout(processPanel, BoxLayout.PAGE_AXIS));
        processPanel.add(Box.createVerticalStrut(10));

        if ( ! reasons.isEmpty()) {
            Dbg.print("setupProcessPanel: reasons isn't empty: " + reasons);
            addLabel(processPanel, SC_NEED_PREPROCESS, false);
            processPanel.add(Box.createVerticalStrut(2));

            for (Enumeration e = reasons.elements() ; e.hasMoreElements() ;) {
                Object reason = e.nextElement();
                if (reason instanceof Vector) {
                    Vector linkVector = (Vector)reason;
                    for (int j = 0; j < linkVector.size(); j++) {
                        JPanel linkPanel = new JPanel();
                        linkPanel.setLayout(new BoxLayout(linkPanel, BoxLayout.X_AXIS));
                        linkPanel.add(Box.createHorizontalGlue());
                        JLabel link = new JLabel((String)linkVector.elementAt(j));
                        link.setForeground(Color.red);
                        linkPanel.add(link);
                        linkPanel.add(Box.createHorizontalGlue());
                        linkPanel.setAlignmentX(0);
                        processPanel.add(linkPanel);
                    }
                } else {
                    JPanel reasonPanel = new JPanel();
                    reasonPanel.setLayout(new BoxLayout(reasonPanel, BoxLayout.X_AXIS));
                    reasonPanel.add(Box.createHorizontalGlue());
                    JLabel label = new JLabel((String)reason);
                    label.setForeground(Color.red);
                    reasonPanel.add(label);
                    reasonPanel.add(Box.createHorizontalGlue());
                    reasonPanel.setAlignmentX(0);
                    processPanel.add(reasonPanel);
                }
            }
            processPanel.add(Box.createVerticalStrut(2));
            addLabel(processPanel, SC_NEW_DICT_DESC, false);
            addLabel(processPanel, SC_NO_TYPE, false);
            processPanel.add(Box.createVerticalStrut(6));
            newDirectory = addTextInput(processPanel, SC_NEW_DICT_DIR, null, false);
            newDictFile = addTextInput(processPanel, SC_NEW_DICT_FILE, null, false);
            newSpecFile = addTextInput(processPanel, SC_NEW_SPEC_FILE, null, false);
        } else {
            addLabel(processPanel, SC_OLD_DICT_DESC, false);
            processPanel.add(Box.createVerticalStrut(6));
            addLabel(processPanel, SC_SPEC_SAVE_DESC, false);
            addLabel(processPanel, SC_NO_TYPE, false);
            //++ cdm July 2006: I added this because it seemed we needed these fields - but no, just if creating
            // newDirectory = addTextInput(processPanel, SC_NEW_DICT_DIR, null, false);
            // newDictFile = addTextInput(processPanel, SC_NEW_DICT_FILE, null, false);
            //--

            newSpecFile = addTextInput(processPanel, SC_NEW_SPEC_FILE, null, false);
        }
        processPanel.add(Box.createVerticalGlue());
        JLabel run = new JLabel(Helper.getTranslation(SC_RUN_PREPROCESS));
        JPanel runPanel = new JPanel();
        runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.X_AXIS));
        runPanel.add(Box.createHorizontalGlue());
        runPanel.add(run);
        runPanel.setAlignmentX(0);
        processPanel.add(runPanel);
    }

    public void setupIndexPanel() {
        indexPanel = new JPanel();
        indexPanel.setLayout(new BoxLayout(indexPanel, BoxLayout.PAGE_AXIS));
        indexPanel.add(Box.createVerticalStrut(10));

        addLabel(indexPanel, SC_DICT_FILES_DESC, false);
        indexPanel.add(Box.createVerticalStrut(20));

        forwardIndex = addTextInput(indexPanel, SC_FORWARD_INDEX, null, false);
        reverseIndex = addTextInput(indexPanel, SC_REVERSE_INDEX, null, false);
        domainFile = addTextInput(indexPanel, SC_DOMAIN_FILE, null, false);

        indexPanel.add(Box.createVerticalGlue());
        JLabel run = new JLabel(Helper.getTranslation(SC_RUN_PROCESS));
        JPanel runPanel = new JPanel();
        runPanel.setLayout(new BoxLayout(runPanel, BoxLayout.X_AXIS));
        runPanel.add(Box.createHorizontalGlue());
        runPanel.add(run);
        runPanel.setAlignmentX(0);
        indexPanel.add(runPanel);

        /*
                 if (reasons.size() > 0)
                         readForPropFile(newDirectory.getText());
                 else {*/
        int second = filename.lastIndexOf("\\");
        int first = filename.lastIndexOf("\\", second-1)+1;
        readForPropFile(filename.substring(first, second));
        //}
    }

    public void setupFinalPanel() {
        finalPanel = new JPanel();
        finalPanel.setLayout(new BoxLayout(finalPanel, BoxLayout.PAGE_AXIS));
        finalPanel.add(Box.createVerticalGlue());

        JPanel loadPanel = new JPanel();
        loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.X_AXIS));
        loadPanel.add(Box.createHorizontalGlue());
        loadNewDict = new JCheckBox(Helper.getTranslation(SC_LOAD_NEW_DICT));
        loadPanel.add(loadNewDict);
        loadPanel.add(Box.createHorizontalGlue());
        loadPanel.setAlignmentX(0);
        finalPanel.add(loadPanel);

        finalPanel.add(Box.createVerticalGlue());

        next.setText(Helper.getTranslation(SC_FINISH));
    }

    public boolean process() {
        //ProcessProgressDialog tracker = new ProcessProgressDialog(Kirrkirr.window,
        //      reasons.size() > 0);

        if ( ! reasons.isEmpty()) {
            String directory = newDirectory.getText();
            File dirFile = new File(directory);
            Dbg.print("process: directory is " + dirFile);
            if (!dirFile.exists() || !dirFile.isDirectory()) {
                dirFile.mkdir();
                Dbg.print("  making it.");
            }
            // make initial pass through dict file
            /*String dictFilename, String
                                            entryTag,
                                                                                    String subEntry,
                                                                                    Vector errorWords,
                                                                                    IndexMakerTracker progressTracker*/
            /*
                 if(!Preprocessor.getFilePositions(dictEntryXPath.getText(),
                                         tracker)) {
                     //error! couldn't parse dict file on first pass
                         tracker.dispose();
                     return;
                 }
                 */
        }
        writeSpecDocument();
        if ( ! reasons.isEmpty()) {
            createFixDict();
        }
        return true;
    }

    public void switchToPage(JPanel page, String label) {
        if (page == null) return;
        if (page == currPage) return;

        if (currPage != null) {
            mainPanel.remove(currPage);
        }

        mainPanel.add(page, BorderLayout.CENTER);
        instructionsPanel.removeAll();
        instructionsPanel.add(new JLabel(Helper.getTranslation(label)));
        currPage = page;
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void setupActionPanel() {
        cancel = new KirrkirrButton(Helper.getTranslation(SC_CANCEL), this);
        previous = new KirrkirrButton(Helper.getTranslation(SC_PREVIOUS), this);
        next = new KirrkirrButton(Helper.getTranslation(SC_NEXT), this);

        JPanel actionPanel = new JPanel();
        actionPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
        actionPanel.add(Box.createHorizontalGlue());
        actionPanel.add(cancel);
        actionPanel.add(Box.createRigidArea(new Dimension(10,0)));
        actionPanel.add(previous);
        actionPanel.add(Box.createRigidArea(new Dimension(3,0)));
        actionPanel.add(next);

        previous.setEnabled(false);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);
    }


    /** gets a filename to use and puts it in the filename string variable */
    public void getFilename() {
        //open up a file chooser dialog
        JFileChooser chooser = new JFileChooser(RelFile.dictionaryDir);
        chooser.setMultiSelectionEnabled(false);
        KirrkirrFileFilter filter;
        filter = new KirrkirrFileFilter(KirrkirrFileFilter.XML_ENTRY);
        chooser.setFileFilter(filter);
        int returnVal = chooser.showDialog(this, ToolsDialog.SC_OK);

        //if a file is chosen
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            //fill in text field with chosen file name
            filename = chooser.getSelectedFile().getAbsolutePath();
        }
    }

    private boolean scanForTagNames() {
        SAXParser parser = new SAXParser();
        TagFinderHandler rfh = new TagFinderHandler();
        try {
            InputStream bis = new BufferedInputStream(
                    new FileInputStream(filename));
            parser.setContentHandler(rfh);
            parser.parse(new InputSource(bis));
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void nextButtonClicked() {
        if (currPage == dictFilePanel) {
            String old = filename;
            filename = fileGetterPanel.getText();
            if (old == null && filename != null) newDictOrSpec = true;
            else if (old != null && filename == null) newDictOrSpec = true;
            else if (old != null && filename != null && !old.equals(filename))
                newDictOrSpec = true;
            if (filename == null || filename.equalsIgnoreCase("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a filename.");
            } else {
                scanForTagNames();
                switchToPage(specFilePanel, SC_SPEC_HEADING);
                previous.setEnabled(true);
            }
        } else if (currPage == specFilePanel) {
            String old = specFilename;
            specFilename = specFileGetterPanel.getText();
            if (old == null && specFilename != null) newDictOrSpec = true;
            else if (old != null && specFilename == null) newDictOrSpec = true;
            else if (old != null && specFilename != null && !old.equals(specFilename))
                newDictOrSpec = true;

            try {
                if (newDictOrSpec) {
                    //set up panels
                    if (specFilename != null && ! specFilename.trim().equals(""))
                        oldSpecs = new DictionaryInfo(specFilename);
                    reasons = new Hashtable();
                    setupBasicInfoPanel();
                    setupSearchPanel();
                    setupDomainPanel();
                    setupFuzzyPanel();
                    setupStopPanel();
                    setupOptInfoPanel2();
                    setupDictErrorPanel();
                    setupMediaPanel();
                    setupXslPanel();
                    newDictOrSpec = false;
                }
                switchToPage(basicInfoPanel, SC_BASIC_INFO_HEADING);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Problem with file. " +
                        "\nPlease enter another filename.");
            }
        } else if (currPage == basicInfoPanel) {
            if (!dictEntryXPath.isValidEntry() || !headwordPath.isValidEntry() ||
                    !glossXPath.isValidEntry())
                return;
            if (hasSubEntries.isSelected()) {
                setupSubentryFixPanel();
                switchToPage(subEntryFixPanel, SC_SUBENTRY_HEADING);
            } else {
                switchToPage(searchPanel, SC_SEARCH_HEADING);
            }
        } else if (currPage == subEntryFixPanel) {
            if ((subEntryXPath.isValidEntry() && subIsSeparate.isSelected()) ||
                    (subIsNested.isSelected() && subPath.isValidEntry())) {
                if (subIsNested.isSelected()) {
                    if (createSubLink.isSelected()) {
                        try {
                            int r = Integer.parseInt(subLinkRed.getText());
                            if (r < 0 || r > 255) throw new NumberFormatException();
                            int g = Integer.parseInt(subLinkGreen.getText());
                            if (g < 0 || g > 255) throw new NumberFormatException();
                            int b = Integer.parseInt(subLinkBlue.getText());
                            if (b < 0 || b > 255) throw new NumberFormatException();
                        } catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(
                                    Kirrkirr.kk.window, Helper.getTranslation(
                                    "Please_enter_a_number_from_0_to_255_for_each_color_of_the_sub_link"));
                            return;
                        }
                    }
                    if (createMainLink.isSelected()) {
                        try {
                            int r = Integer.parseInt(mainLinkRed.getText());
                            if (r < 0 || r > 255) throw new NumberFormatException();
                            int g = Integer.parseInt(mainLinkGreen.getText());
                            if (g < 0 || g > 255) throw new NumberFormatException();
                            int b = Integer.parseInt(mainLinkBlue.getText());
                            if (b < 0 || b > 255) throw new NumberFormatException();
                        } catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(
                                    Kirrkirr.kk.window, Helper.getTranslation(
                                    "Please_enter_a_number_from_0_to_255_for_each_color_of_the_main_link"));
                            return;
                        }
                    }
                    reasons.put(subEntryFixPanel, Helper.getTranslation(SC_SUBENTRY_R));
                }
                else
                    reasons.remove(subEntryFixPanel);
                switchToPage(searchPanel, SC_SEARCH_HEADING);
            }
        } else if (currPage == searchPanel) {
            switchToPage(domainInfoPanel, SC_DOMAIN_HEADING);
        } else if (currPage == domainInfoPanel) {
            if (domainXPath.isValidEntry()) {
                if (domainXPath.willNeedToBeParsed()) {
                    reasons.put(domainXPath, Helper.getTranslation(SC_DOMAIN_R));
                } else {
                    reasons.remove(domainXPath);
                }
                if (converter.getText() != null && !converter.getText().trim().equals("")) {
                    reasons.put(converter, Helper.getTranslation(SC_DOMAIN_CONV_R));
                } else {
                    reasons.remove(converter);
                }
                setupLinkPanel();
                switchToPage(linksPanel, SC_LINKS_HEADING);
            }
        } else if (currPage == linksPanel) {
            Vector linkReasons = new Vector();
            for (Enumeration e = linkTable.elements() ; e.hasMoreElements() ;) {
                Link currLink = (Link)e.nextElement();
                if (currLink.sub != null && !currLink.sub.equals("")
                        && !isSinglePath(currLink.sub))
                    linkReasons.addElement(
                            Helper.getTranslation(SC_LINK_R)+currLink.name+
                                    Helper.getTranslation(SC_MUST_SPLIT));
            }
            if (linkReasons.isEmpty()) {
                reasons.remove(linksPanel);
            } else {
                reasons.put(linksPanel, linkReasons);
            }
            switchToPage(fuzzyPanel, SC_FUZZY_HEADING);
        } else if (currPage == fuzzyPanel) {
            switchToPage(stopPanel, SC_STOP_HEADING);
        } else if (currPage == stopPanel) {
            setupOptInfoPanel();
            switchToPage(optInfoPanel1, SC_OTHER_INFO_HEADING1);
        } else if (currPage == optInfoPanel1) {
            useSense = ! sensePath.getText().trim().equals("");
            switchToPage(optInfoPanel2, SC_OTHER_INFO_HEADING2);
        } else if (currPage == optInfoPanel2) {
            String base = dialectPath.getBase();
            if (base==null || base.trim().equals("")
                    || (base != null && dialectPath.isValidEntry())) {
                String base2 = registerPath.getBase();
                if (base2==null || base2.trim().equals("")
                        || (base2 != null && registerPath.isValidEntry())){
                    if (base != null && !base.trim().equals("")) {
                        if (dialectPath.willNeedToBeParsed())
                            reasons.put(dialectPath, Helper.getTranslation(SC_DIALECT_R));
                        else
                            reasons.remove(dialectPath);
                    }
                    if (base2 != null && !base2.trim().equals("")) {
                        if (registerPath.willNeedToBeParsed())
                            reasons.put(registerPath, Helper.getTranslation(SC_REGISTER_R));
                        else
                            reasons.remove(registerPath);
                    }
                    switchToPage(dictErrorPanel, SC_DICTERROR_HEADING);
                }
            }
        } else if (currPage == dictErrorPanel) {
            switchToPage(mediaPanel, SC_MEDIA_HEADING);
        } else if (currPage == mediaPanel) {
            String base = imagePath.getBase();
            if (base==null || base.trim().equals("")
                    || (base != null && imagePath.isValidEntry())) {
                String base2 = audioPath.getBase();
                if (base2==null || base2.trim().equals("")
                        || (base2 != null && audioPath.isValidEntry())){
                    if (base != null && !base.trim().equals("")) {
                        if (imagePath.willNeedToBeParsed())
                            reasons.put(imagePath, Helper.getTranslation(SC_IMAGE_R));
                        else
                            reasons.remove(imagePath);
                    }
                    if (base2 != null && !base2.trim().equals("")) {
                        if (audioPath.willNeedToBeParsed())
                            reasons.put(audioPath, Helper.getTranslation(SC_AUDIO_R));
                        else
                            reasons.remove(audioPath);
                    }
                    switchToPage(xslPanel, SC_XSL_HEADING);
                }
            }
        } else if (currPage == xslPanel) {
            setupProcessPanel();
            switchToPage(processPanel, SC_PROCESS_HEADING);
        } else if (currPage == processPanel) {
            if ( ! reasons.isEmpty() && newSpecFile.getText().trim().equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window,
                        "Please enter a name for your spec file.");
                return;
            } else {
                if ( ! reasons.isEmpty()) {
                    if (notFilledWarn(newDirectory,
                            "Please enter a name for your directory.")) {
                        return;
                    }
                    if (notFilledWarn(newDictFile,
                            "Please enter a name for your dictionary file.")) {
                        return;
                    }
                }
                if (notFilledWarn(newSpecFile, "Please enter a name for your spec file.")) {
                    return;
                }
            }
            process();
            setupIndexPanel();
            switchToPage(indexPanel, SC_DICT_FILES_HEADING);
        } else if (currPage == indexPanel) {
            if (notFilled(forwardIndex)) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window,
                        "Please enter a name for your forward index file.");
                return;
            }
            if (notFilled(reverseIndex)) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window,
                        "Please enter a name for your reverse index file.");
                return;
            }
            if (notFilled(domainFile)) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window,
                        "Please enter a name for your domain file.");
                return;
            }
            makeFiles();
        } else if (currPage == finalPanel) {
            dispose();
            if (loadNewDict.isSelected())
                Kirrkirr.kk.loadNewDict(getCurrDictDir());
        }
    }

    private boolean notFilledWarn(JTextField tf, String msg) {
        if (notFilled(tf)) {
            JOptionPane.showMessageDialog(
                    Kirrkirr.kk.window, msg);
            return true;
        }
        return false;
    }

    private static boolean notFilled(JTextField tf) {
        if (tf == null) {
            if (Dbg.ERROR) {
                Dbg.print("Warning: ImportDialog.notFilled: text field is null!");
                Exception e = new NullPointerException();
                e.printStackTrace();
            }
            return true;
        } else {
            String str = tf.getText();
            if (str == null) {
                return true;
            } else {
                str = str.trim();
                return "".equals(str);
            }
        }
    }

    private void prevButtonClicked() {
        if (currPage == specFilePanel) {
            switchToPage(dictFilePanel, SC_DICT_HEADING);
            previous.setEnabled(false);
        }
        else if (currPage == basicInfoPanel) {
            switchToPage(specFilePanel, SC_SPEC_HEADING);
        }
        else if (currPage == subEntryFixPanel) {
            switchToPage(basicInfoPanel, SC_BASIC_INFO_HEADING);
        }
        else if (currPage == searchPanel) {
            if (hasSubEntries.isSelected())
                switchToPage(subEntryFixPanel, SC_SUBENTRY_HEADING);
            else
                switchToPage(basicInfoPanel, SC_BASIC_INFO_HEADING);
        } else if (currPage == domainInfoPanel) {
            switchToPage(searchPanel, SC_SEARCH_HEADING);
        } else if (currPage == linksPanel) {
            switchToPage(domainInfoPanel, SC_DOMAIN_HEADING);
        } else if (currPage == fuzzyPanel) {
            switchToPage(linksPanel, SC_LINKS_HEADING);
        } else if (currPage == stopPanel) {
            switchToPage(fuzzyPanel, SC_FUZZY_HEADING);
        } else if (currPage == optInfoPanel1) {
            switchToPage(stopPanel, SC_STOP_HEADING);
        } else if (currPage == optInfoPanel2) {
            switchToPage(optInfoPanel1, SC_OTHER_INFO_HEADING1);
        } else if (currPage == dictErrorPanel) {
            switchToPage(optInfoPanel2, SC_OTHER_INFO_HEADING2);
        } else if (currPage == mediaPanel) {
            switchToPage(dictErrorPanel, SC_DICTERROR_HEADING);
        } else if (currPage == xslPanel) {
            switchToPage(mediaPanel, SC_MEDIA_HEADING);
        } else if (currPage == processPanel) {
            switchToPage(xslPanel, SC_XSL_HEADING);
        }
    }

    public void changeElement(Document doc, String tagName, String newSpec) {
        NodeList found = doc.getElementsByTagName(tagName);
        if (found == null || found.getLength() == 0) {
            try {
                Element newElem = doc.createElement(tagName);
                Element root = doc.getDocumentElement();
                Text newText = doc.createTextNode(newSpec);
                newElem.appendChild(newText);
                root.appendChild(newElem);
            } catch (DOMException domex) {
                domex.printStackTrace();
            }
        }
        else {
            try {
                Node oldNode = found.item(0);
                Text newText = doc.createTextNode(newSpec);
                oldNode.replaceChild(newText, oldNode.getFirstChild());
            } catch (DOMException domex) {
                domex.printStackTrace();
            }
        }
    }

    public void addTextElement(Document doc, String tagName, String spec) {
        String newSpec = spec.trim();
        if (newSpec == null || newSpec.equals("")) return;
        try {
            Element newElem = doc.createElement(tagName);
            Element root = doc.getDocumentElement();
            Text newText = doc.createTextNode(newSpec);
            newElem.appendChild(newText);
            root.appendChild(newElem);
        } catch (DOMException domex) {
            domex.printStackTrace();
        }
    }

    private String findSubElem(String xpath) {
        String name = xpath.substring(xpath.lastIndexOf("/")+1);
        String subtag = xpath + "/" + name+SC_SUBELEM;
        while(tagNames.containsKey(subtag))
            subtag+=SC_EXTRASUBELEM;

        return subtag;
    }

    public static String makeRegex(String chars) {
        return "[" + chars.trim()+ "]+";
    }

    public String getCurrDictDir() {
        if ( ! reasons.isEmpty()) {
            return newDirectory.getText();
        } else {
            int second = filename.lastIndexOf("\\");
            int first = filename.lastIndexOf("\\", second-1)+1;
            if (Dbg.IMPORT_WIZARD) {
                Dbg.print("getCurrDictDir: filename is " + filename);
                Dbg.print("getCurrDictDir: returning" + filename.substring(first, second));
            }
            return filename.substring(first, second);
        }
    }

    public boolean writeSpecToFile(Document specDoc) {
        try {
            // Prepare the DOM document for writing
            Source source = new DOMSource(specDoc);

            // Prepare the output file
            String specFile;

            specFile = getCurrDictDir() +
                    "\\" + newSpecFile.getText() + ".xml";

            File file = new File(specFile);
            file.createNewFile();
            Result result = new StreamResult(specFile);

            // Write the DOM document to the file
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(source, result);

            return true;
        } catch (TransformerConfigurationException e) {
            System.out.println("TCE");
            return false;
        } catch (TransformerException e) {
            e.printStackTrace();
            System.out.println("TE");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void addParserForSense(String oldPath, String[] parse) {
        if (!isSinglePath(oldPath)) return;
        String entryPath = dictEntryXPath.getText()+"/";
        String sense = sensePath.getText();

        int oIndex = oldPath.lastIndexOf(entryPath)+entryPath.length();
        String newPath = sense + "/" + oldPath.substring(oIndex);

        parseRegexps.put(newPath, parse);
    }

    public boolean writeSpecDocument() {
        try {
            //ADD DTD later instead of NULL
            Document doc = (new DOMImplementationImpl()).createDocument(null,
                    DictionaryInfo.SPEC_ROOT, null);

            parseRegexps = new Hashtable();

            //basic Info
            addTextElement(doc, DictionaryInfo.DICTIONARY_ENTRY_XPATH,
                    dictEntryXPath.getText());
            addTextElement(doc, DictionaryInfo.HEADWORD, headwordPath.getText());
            addTextElement(doc, DictionaryInfo.UNIQUIFIER, uniquifierPath.getText());
            addTextElement(doc, DictionaryInfo.HEADWORD_REGEXP, headwordRegex.getText());
            addTextElement(doc, DictionaryInfo.UNIQUIFIER_REGEXP, uniquifierRegex.getText());

            //search regexs
            if ( ! regexTable.isEmpty()) {
                Element regexpsElem = doc.createElement(DictionaryInfo.SEARCH_REGEXPS);
                for (Enumeration e = regexTable.keys() ; e.hasMoreElements() ;) {
                    String key = (String)e.nextElement();
                    Element searchElem = doc.createElement(DictionaryInfo.SEARCH_REGEXP);
                    Element menuNameElem = doc.createElement(DictionaryInfo.MENU_NAME);
                    Text menuName = doc.createTextNode(key);
                    menuNameElem.appendChild(menuName);
                    searchElem.appendChild(menuNameElem);

                    Element regexpElem = doc.createElement(DictionaryInfo.REGEXP);
                    Text regexp = doc.createTextNode((String)regexTable.get(key));
                    regexpElem.appendChild(regexp);
                    searchElem.appendChild(regexpElem);

                    regexpsElem.appendChild(searchElem);
                }
                Element root = doc.getDocumentElement();
                root.appendChild(regexpsElem);
            }

            //xsl files
            if ( ! xslTable.isEmpty()) {
                Element xslsElem = doc.createElement(DictionaryInfo.XSL_FILES);
                for (Enumeration e = xslTable.keys() ; e.hasMoreElements() ;) {
                    String key = (String)e.nextElement();
                    XslFile value = (XslFile)xslTable.get(key);
                    Element xslElem = doc.createElement(DictionaryInfo.XSL_FILE);

                    Element filenameElem = doc.createElement(DictionaryInfo.XSL_FILENAME);
                    Text filename = doc.createTextNode(value.filename);
                    filenameElem.appendChild(filename);
                    xslElem.appendChild(filenameElem);

                    Element shortNameElem = doc.createElement(DictionaryInfo.XSL_SHORT);
                    Text shortName = doc.createTextNode(key);
                    shortNameElem.appendChild(shortName);
                    xslElem.appendChild(shortNameElem);

                    Element descElem = doc.createElement(DictionaryInfo.XSL_DESC);
                    Text desc = doc.createTextNode(value.description);
                    descElem.appendChild(desc);
                    xslElem.appendChild(descElem);

                    xslsElem.appendChild(xslElem);
                }
                Element root = doc.getDocumentElement();
                root.appendChild(xslsElem);
            }

            //ref xpaths
            addTextElement(doc, DictionaryInfo.REF_HEADWORD_XPATH,
                    refHeadwordXPath.getText());
            addTextElement(doc, DictionaryInfo.REF_UNIQUIFIER_XPATH,
                    refUniquifierXPath.getText());

            //links
            if ( ! linkTable.isEmpty()) {
                Element linksElem = doc.createElement(DictionaryInfo.LINKS);
                for (Enumeration e = linkTable.keys() ; e.hasMoreElements() ;) {
                    String key = (String)e.nextElement();
                    Link value = (Link)linkTable.get(key);
                    Element linkElem = doc.createElement(DictionaryInfo.LINK_NODE);

                    Element nameElem = doc.createElement(DictionaryInfo.LINK_NAME);
                    Text name = doc.createTextNode(key);
                    nameElem.appendChild(name);
                    linkElem.appendChild(nameElem);

                    Element colorElem = doc.createElement(DictionaryInfo.LINK_COLOR);
                    Element rElem = doc.createElement(DictionaryInfo.LINK_COLOR_R);
                    Text r = doc.createTextNode((new Integer(value.r)).toString());
                    rElem.appendChild(r);
                    colorElem.appendChild(rElem);
                    Element gElem = doc.createElement(DictionaryInfo.LINK_COLOR_G);
                    Text g = doc.createTextNode((new Integer(value.g)).toString());
                    gElem.appendChild(g);
                    colorElem.appendChild(gElem);
                    Element bElem = doc.createElement(DictionaryInfo.LINK_COLOR_B);
                    Text b = doc.createTextNode((new Integer(value.b)).toString());
                    bElem.appendChild(b);
                    colorElem.appendChild(bElem);
                    linkElem.appendChild(colorElem);

                    Element xpathElem = doc.createElement(DictionaryInfo.LINK_XPATH);
                    Text xpath;
                    if (value.subType == 0) {
                        xpath = doc.createTextNode(value.xpath);
                    } else {
                        String parser;
                        if (value.subType == 2)
                            parser = makeRegex(value.sub);
                        else
                            parser = value.sub;

                        String sub = findSubElem(value.xpath);
                        xpath = doc.createTextNode(sub);
                        String[] parse = new String[2];
                        parse[0] = parser;
                        parse[1] = sub.substring(sub.lastIndexOf("/")+1);
                        parseRegexps.put(value.xpath, parse);
                        if (useSense)
                            addParserForSense(value.xpath, parse);
                    }
                    xpathElem.appendChild(xpath);
                    linkElem.appendChild(xpathElem);

                    linksElem.appendChild(linkElem);
                }
                Element root = doc.getDocumentElement();
                root.appendChild(linksElem);
            }

            //sense path
            addTextElement(doc, DictionaryInfo.SENSE_XPATH, sensePath.getText());

            //images & audio
            if (imagePath.willNeedToBeParsed()) {
                String parser;
                if (imagePath.getSubType() == 2)
                    parser = makeRegex(imagePath.getSub());
                else
                    parser = imagePath.getSub();

                String base = imagePath.getBase();
                String sub = findSubElem(base);
                Text path = doc.createTextNode(sub);
                String[] parse = new String[2];
                parse[0] = parser;
                parse[1] = sub.substring(sub.lastIndexOf("/")+1);
                parseRegexps.put(base, parse);
                if (useSense)
                    addParserForSense(base, parse);
                addTextElement(doc, DictionaryInfo.IMAGES_XPATH, sub);
            } else {
                addTextElement(doc, DictionaryInfo.IMAGES_XPATH, imagePath.getText());
            }
            if (audioPath.willNeedToBeParsed()) {
                String parser;
                if (audioPath.getSubType() == 2)
                    parser = makeRegex(audioPath.getSub());
                else
                    parser = audioPath.getSub();

                String base = audioPath.getBase();
                String sub = findSubElem(base);
                Text path = doc.createTextNode(sub);
                String[] parse = new String[2];
                parse[0] = parser;
                parse[1] = sub.substring(sub.lastIndexOf("/")+1);
                parseRegexps.put(base, parse);
                if (useSense)
                    addParserForSense(base, parse);
                addTextElement(doc, DictionaryInfo.AUDIO_XPATH, sub);
            } else {
                addTextElement(doc, DictionaryInfo.AUDIO_XPATH, audioPath.getText());
            }

            //domain stuff
            addTextElement(doc, DictionaryInfo.DOMAIN_XPATH, domainXPath.getBase());
            if (domainXPath.willNeedToBeParsed()) {
                String parser;
                if (domainXPath.getSubType() == 2)
                    parser = makeRegex(domainXPath.getSub());
                else
                    parser = domainXPath.getSub();

                String base = domainXPath.getBase();
                String sub = findSubElem(base);
                Text path = doc.createTextNode(sub);
                String[] parse = new String[2];
                parse[0] = parser;
                parse[1] = sub.substring(sub.lastIndexOf("/")+1);
                parseRegexps.put(base, parse);
                if (useSense)
                    addParserForSense(base, parse);
                addTextElement(doc, DictionaryInfo.DOMAIN_COMPONENT_XPATH,
                        sub.substring(sub.lastIndexOf("/")+1));
            } else {
                String subTag = domainXPath.getSub();
                if (subTag != null)
                    addTextElement(doc, DictionaryInfo.DOMAIN_COMPONENT_XPATH, subTag);
            }

            //freq
            addTextElement(doc, DictionaryInfo.FREQUENCY_XPATH, freqPath.getText());

            //subword
            if (subIsSeparate != null && subIsSeparate.isSelected()) {
                newSubentryXPath = null;
                addTextElement(doc, DictionaryInfo.SUBWORD_XPATH, subEntryXPath.getText());
            } else {
                if (subFixWAttr != null && subFixWAttr.isSelected()) {
                    subFixType = Preprocessor.NO_LINKS;
                } else {
                    if (createSubLink != null && createSubLink.isSelected() && createMainLink != null && createMainLink.isSelected()) {
                        subFixType = Preprocessor.BOTH_LINKS;
                    } else if (createSubLink != null && createSubLink.isSelected()) {
                        subFixType = Preprocessor.SUB_LINK;
                    } else if (createMainLink != null && createMainLink.isSelected()) {
                        subFixType = Preprocessor.MAIN_LINK;
                    } else {
                        subFixType = Preprocessor.NO_LINKS;
                    }
                }
                String entryPath = dictEntryXPath.getText();
                int atIndex = entryPath.indexOf("@");
                String newSubPath;
                if (atIndex != -1) {
                    newSubPath = entryPath.substring(0, atIndex);
                } else {
                    newSubPath = entryPath;
                }
                String subTag = "TYPE";
                while (((HashSet)tagNames.get(entryPath)).contains(subTag)) {
                    subTag+=SC_EXTRASUBELEM;
                }
                // cdm: July 2006: avoiding crash
                if (subPath != null) {
                    parseRegexps.put(subPath.getFullPath(), new String[]{"", ""});
                } else {
                    Dbg.print("subPath is null");
                }
                newSubentryXPath = subTag;
                addTextElement(doc, DictionaryInfo.SUBWORD_XPATH,
                        newSubPath+"/@"+subTag);
            }

            //other info
            if (dialectPath.willNeedToBeParsed()) {
                String parser;
                if (dialectPath.getSubType() == 2)
                    parser = makeRegex(dialectPath.getSub());
                else
                    parser = dialectPath.getSub();

                String base = dialectPath.getBase();
                String sub = findSubElem(base);
                Text path = doc.createTextNode(sub);
                String[] parse = new String[2];
                parse[0] = parser;
                parse[1] = sub.substring(sub.lastIndexOf("/")+1);
                parseRegexps.put(base, parse);
                if (useSense)
                    addParserForSense(base, parse);
                addTextElement(doc, DictionaryInfo.DIALECT_XPATH, sub);
            } else {
                addTextElement(doc, DictionaryInfo.DIALECT_XPATH, dialectPath.getText());
            }
            if (registerPath.willNeedToBeParsed()) {
                String parser;
                if (registerPath.getSubType() == 2)
                    parser = makeRegex(registerPath.getSub());
                else
                    parser = registerPath.getSub();

                String base = registerPath.getBase();
                String sub = findSubElem(base);
                Text path = doc.createTextNode(sub);
                String[] parse = new String[2];
                parse[0] = parser;
                parse[1] = sub.substring(sub.lastIndexOf("/")+1);
                parseRegexps.put(base, parse);
                if (useSense)
                    addParserForSense(base, parse);
                addTextElement(doc, DictionaryInfo.REGISTER_XPATH, sub);
            } else {
                addTextElement(doc, DictionaryInfo.REGISTER_XPATH, registerPath.getText());
            }
            addTextElement(doc, DictionaryInfo.POS_XPATH, posPath.getText());
            addTextElement(doc, DictionaryInfo.GLOSS_XPATH, glossXPath.getText());

            //fuzzy spelling
            if (fuzzyHwordTable.size() != 0 || fuzzyGlossTable.size() != 0) {
                Element fuzzyElem = doc.createElement(DictionaryInfo.FUZZY_SPELLING);
                if (fuzzyHwordTable.size() != 0) {
                    Element fuzzyHwords =
                            doc.createElement(DictionaryInfo.HEADWORD_LANGUAGE);
                    for (Enumeration e = fuzzyHwordTable.keys() ; e.hasMoreElements() ;) {
                        Element substitution =
                                doc.createElement(DictionaryInfo.SUBSTITUTION);
                        String key = (String)e.nextElement();
                        Element originalElem = doc.createElement(DictionaryInfo.SUBS_ORIGINAL);
                        Text original = doc.createTextNode(key);
                        originalElem.appendChild(original);
                        substitution.appendChild(originalElem);

                        Element replacementElem =
                                doc.createElement(DictionaryInfo.SUBS_REPLACEMENT);
                        Text replacement =
                                doc.createTextNode((String)fuzzyHwordTable.get(key));
                        replacementElem.appendChild(replacement);
                        substitution.appendChild(replacementElem);

                        fuzzyHwords.appendChild(substitution);
                    }
                    fuzzyElem.appendChild(fuzzyHwords);
                }
                if (fuzzyGlossTable.size() != 0) {
                    Element fuzzyGlosses =
                            doc.createElement(DictionaryInfo.GLOSS_LANGUAGE);
                    for (Enumeration e = fuzzyGlossTable.keys() ; e.hasMoreElements() ;) {
                        Element substitution =
                                doc.createElement(DictionaryInfo.SUBSTITUTION);
                        String key = (String)e.nextElement();
                        Element originalElem = doc.createElement(DictionaryInfo.SUBS_ORIGINAL);
                        Text original = doc.createTextNode(key);
                        originalElem.appendChild(original);
                        substitution.appendChild(originalElem);

                        Element replacementElem =
                                doc.createElement(DictionaryInfo.SUBS_REPLACEMENT);
                        Text replacement =
                                doc.createTextNode((String)fuzzyGlossTable.get(key));
                        replacementElem.appendChild(replacement);
                        substitution.appendChild(replacementElem);

                        fuzzyGlosses.appendChild(substitution);
                    }
                    fuzzyElem.appendChild(fuzzyGlosses);
                }
                Element root = doc.getDocumentElement();
                root.appendChild(fuzzyElem);
            }

            //stopwords & stopchars
            int stopwordListSize = stopwordListModel.size();
            if (stopwordListSize != 0) {
                Element stopwords = doc.createElement(DictionaryInfo.STOPWORDS);
                for (int i = 0; i < stopwordListSize; i++) {
                    Element stopwordElem = doc.createElement(DictionaryInfo.STOPWORD);
                    Text stopword = doc.createTextNode((String)stopwordListModel.elementAt(i));
                    stopwordElem.appendChild(stopword);
                    stopwords.appendChild(stopwordElem);
                }
                Element root = doc.getDocumentElement();
                root.appendChild(stopwords);
            }
            int stopcharListSize = stopcharListModel.size();
            if (stopcharListSize != 0) {
                Element stopchars = doc.createElement(DictionaryInfo.STOPCHARS);
                for (int i = 0; i < stopcharListSize; i++) {
                    Element stopcharElem = doc.createElement(DictionaryInfo.STOPCHAR);
                    Text stopchar = doc.createTextNode((String)stopcharListModel.elementAt(i));
                    stopcharElem.appendChild(stopchar);
                    stopchars.appendChild(stopcharElem);
                }
                Element root = doc.getDocumentElement();
                root.appendChild(stopchars);
            }

            //dicterrors
            int dictErrorListSize = dictErrorListModel.size();
            if (dictErrorListSize != 0) {
                Element dicterrors = doc.createElement(DictionaryInfo.DICTERRORS);
                for (int i = 0; i < dictErrorListSize; i++) {
                    Element dicterrorElem = doc.createElement(DictionaryInfo.DICTERROR);
                    Text dicterror = doc.createTextNode((String)dictErrorListModel.elementAt(i));
                    dicterrorElem.appendChild(dicterror);
                    dicterrors.appendChild(dicterrorElem);
                }
                Element root = doc.getDocumentElement();
                root.appendChild(dicterrors);
            }

            //lang stuff
            addTextElement(doc, DictionaryInfo.GLOSS_LANGUAGE_NAME, glossLang.getText());
            addTextElement(doc, DictionaryInfo.DICTIONARY_LANGUAGE_NAME, dictName.getText());
            addTextElement(doc, DictionaryInfo.GLOSS_LANGUAGE_ICON, glossIcon.getText());
            addTextElement(doc, DictionaryInfo.DICTIONARY_LANGUAGE_ICON, dictIcon.getText());


            return writeSpecToFile(doc);
        } catch (DOMException domex) {
            domex.printStackTrace();
            return false;
        }
    }

    public boolean createFixDict() {
        String entryPath = dictEntryXPath.getText();
        Preprocessor p =
                new Preprocessor(newDirectory.getText()+"\\"+newDictFile.getText()+".xml",
                        parseRegexps, newSubentryXPath, entryPath);
        if (subFixType == Preprocessor.MAIN_LINK ||
                subFixType == Preprocessor.BOTH_LINKS) {
            String main = Preprocessor.SC_MAINENTRY;
            while (tagNames.containsKey(entryPath + "/" + main))
                main+=SC_EXTRASUBELEM;
            p.setMainLinkTag(main);
        }
        if (subFixType == Preprocessor.SUB_LINK ||
                subFixType == Preprocessor.BOTH_LINKS) {
            String subs = Preprocessor.SC_SUBENTRIES;
            while (tagNames.containsKey(entryPath + "/" + subs))
                subs+=SC_EXTRASUBELEM;
            String sub = Preprocessor.SC_SUBENTRY;
            while (tagNames.containsKey(entryPath + "/" + sub))
                sub+=SC_EXTRASUBELEM;
            p.setSubLinkTags(subs, sub);
        }
        String domainConv = converter.getText();
        if (domainConv != null && !domainConv.equals("")) {
            if (domainXPath.getSubType() == 1)
                p.setDomainConv(domainConv,
                        domainXPath.getBase() + "/" + domainXPath.getSub());
            else
                p.setDomainConv(domainConv, domainXPath.getBase());
        }
        SAXParser parser = new SAXParser();
        try {
            InputStream bis = new BufferedInputStream(
                    new FileInputStream(filename));
            parser.setErrorHandler(p);
            parser.setContentHandler(p);
            parser.setEntityResolver(p);
            parser.setDTDHandler(p);
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", p);
            //reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            InputSource is = new InputSource(bis);
            String encoding = is.getEncoding();
            p.setEncoding(encoding);
            parser.parse(is);
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        //return true;
    }

    public boolean readForPropFile(String dictionaryDir) {
        File propFile = new File(dictionaryDir + "\\" + Kirrkirr.DICT_PROPERTIES_FILE);
        if (propFile.exists() && propFile.isFile()) {
            Properties oldProps = new Properties();

            try {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propFile));
                PropertiesUtils.load(oldProps, bis);
                bis.close();

                String fI = oldProps.getProperty("dictionary.index");
                if (fI != null) {
                    forwardIndex.setText(fI.substring(0, fI.lastIndexOf(".")));
                }
                String rI = oldProps.getProperty("dictionary.reverseIndex");
                if (rI != null) {
                    reverseIndex.setText(rI.substring(0, rI.lastIndexOf(".")));
                }
                String dF = oldProps.getProperty("dictionary.domainFile");
                if (dF != null) {
                    domainFile.setText(dF.substring(0, dF.lastIndexOf(".")));
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    private boolean makeNewPropFile() {
        try {
            String fname = newDirectory.getText()+
                            "\\" + Kirrkirr.DICT_PROPERTIES_FILE;
            BufferedWriter propOut =
                    new BufferedWriter(new FileWriter(fname));
            Dbg.print("Writing new dictionary.properties: " + fname);
            propOut.write("#dictionary.properties file");
            propOut.newLine();
            propOut.write("dictionary.index="+forwardIndex.getText()+".clk");
            propOut.newLine();
            propOut.write("dictionary.dictionary="+newDictFile.getText()+".xml");
            propOut.newLine();
            propOut.write("dictionary.reverseIndex="+reverseIndex.getText()+".clk");
            propOut.newLine();
            propOut.write("dictionary.dictSpecFile="+newSpecFile.getText()+".xml");
            propOut.newLine();
            propOut.write("dictionary.domainFile="+domainFile.getText()+".xml");

            propOut.close();
            return true;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
    }

    private boolean fixOldPropFile() {
        int second = filename.lastIndexOf("\\");
        int first = filename.lastIndexOf("\\", second-1)+1;

        File propFile = new File(filename.substring(first, second)
                + "\\" + Kirrkirr.DICT_PROPERTIES_FILE);
        if (Dbg.IMPORT_WIZARD) {
            Dbg.print("fixOldPropFile: filename=" + filename);
            Dbg.print("fixOldPropFile: propFile=" + propFile);
            Dbg.print("fixOldPropFile: i.e., writing " + propFile.getAbsolutePath());
        }
        BufferedWriter propOut = null;

        try {
            Properties oldProps = null;
            if (propFile.exists() && propFile.isFile()) {
                oldProps = new Properties();
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(propFile));
                PropertiesUtils.load(oldProps, bis);
                bis.close();
            }

            propOut = new BufferedWriter(new FileWriter(propFile));
            propOut.write("#dictionary.properties file");
            propOut.newLine();
            propOut.write("dictionary.index="+forwardIndex.getText()+".clk");
            propOut.newLine();

            String dict = null;
            if (oldProps != null) {
                dict = oldProps.getProperty("dictionary.dictionary");
            } else {
                int ind = filename.lastIndexOf("\\");
                dict =  filename.substring(ind + 1);
                Dbg.print("fixOldPropFile: dictionary file is " + dict);
            }
            if (dict != null) {
                propOut.write("dictionary.dictionary="+dict);
                propOut.newLine();
            }
            propOut.write("dictionary.reverseIndex="+reverseIndex.getText()+".clk");
            propOut.newLine();
            propOut.write("dictionary.dictSpecFile="+newSpecFile.getText()+".xml");
            propOut.newLine();
            propOut.write("dictionary.domainFile="+domainFile.getText()+".xml");
            propOut.close();
            return true;
        } catch (Exception e) {
            try {
                if (propOut != null) { propOut.close(); }
            } catch (Exception ee) {}
            return false;
        }
    }


    private boolean makeFiles() {
        //make requested files
        Thread maker = new Thread(this);
        maker.start();

        return true;  //tests were attempted
    }

    /** Actually make auxiliary files based on user's requests.
     *  Note that this is done on a background thread.
     */

    /** The extension should include the period! */
    private String addExtensionIfMissing(String file, String ext) {
       if (file.endsWith(ext)) {
           return file;
       }
       return file + ext;
    }

    public void run() {
        String dictionaryDir;
        String xmlDictionaryFile;
        String xmlSpecFile;
        String xs = ".xml";
        String clk = ".clk";
        if (reasons.isEmpty()) {
            int second = filename.lastIndexOf("\\");
            int first = filename.lastIndexOf("\\", second-1)+1;
            dictionaryDir = filename.substring(first, second) + "\\";
            xmlDictionaryFile = dictionaryDir + filename.substring(second + 1);
            xmlSpecFile = dictionaryDir + addExtensionIfMissing(newSpecFile.getText(), xs);
        } else {
            dictionaryDir = newDirectory.getText() + "\\";
            xmlDictionaryFile = dictionaryDir + addExtensionIfMissing(newDictFile.getText(), xs);
            xmlSpecFile = dictionaryDir + addExtensionIfMissing(newSpecFile.getText(), xs);
        }

        ToolsDialog.IndexProgressDialog tracker =
                new ToolsDialog.IndexProgressDialog(Kirrkirr.window,
                        true, true, true);
        //make initial pass through dict file
        Dbg.print("run: running indexmaker on " + xmlDictionaryFile +
                "  " + xmlSpecFile);

        //forward and/or reverse index

        //this is a no-op if both index filenames are null
        try {
            if ( ! IndexMaker.makeIndexFiles(xmlSpecFile, xmlDictionaryFile,
                    dictionaryDir + addExtensionIfMissing(forwardIndex.getText(), clk),
                    dictionaryDir + addExtensionIfMissing(reverseIndex.getText(), clk),
                    dictionaryDir + addExtensionIfMissing(domainFile.getText(), xs),
                    null, tracker)) {
                //error! couldn't parse dict file on first pass
                if (Dbg.ERROR) Dbg.print("Running IndexMaker failed.");
                tracker.dispose();
                return;
            }
            if ( ! reasons.isEmpty()) {
                if (makeNewPropFile()) {
                    File newDir = new File(dictionaryDir);
                    int second = filename.lastIndexOf("\\");
                    int first = filename.lastIndexOf("\\", second-1)+1;

                    File oldDir = new File(filename.substring(first, second));
                    File[] children = oldDir.listFiles();
                    for (int i = 0; i < children.length; i++) {
                        File curr = children[i];
                        if (curr.isDirectory())
                            Helper.copyDirectory(curr, newDir.getName());
                    }
                } else {
                    JOptionPane.showMessageDialog(
                            Kirrkirr.kk.window, "Error creating prop file.");
                    return;
                }
            } else {
                fixOldPropFile();
            }
            setupFinalPanel();
            switchToPage(finalPanel, SC_FINAL_HEADING);
        } catch (Throwable exception) {
            exception.printStackTrace();
            tracker.dispose();
        }
    }


    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == next) {
            nextButtonClicked();
        } else if (src == cancel) {
            dispose();
        } else if (src == previous) {
            prevButtonClicked();
        } else if (src == createSubLink) {
            if (createSubLink.isSelected()) {
                subLinkRed.setEnabled(true);
                subLinkGreen.setEnabled(true);
                subLinkBlue.setEnabled(true);
                subColorChooser.setEnabled(true);
            } else {
                subLinkRed.setEnabled(false);
                subLinkGreen.setEnabled(false);
                subLinkBlue.setEnabled(false);
                subColorChooser.setEnabled(false);
            }
        } else if (src == createMainLink) {
            if (createMainLink.isSelected()) {
                mainLinkRed.setEnabled(true);
                mainLinkGreen.setEnabled(true);
                mainLinkBlue.setEnabled(true);
                mainColorChooser.setEnabled(true);
            } else {
                mainLinkRed.setEnabled(false);
                mainLinkGreen.setEnabled(false);
                mainLinkBlue.setEnabled(false);
                mainColorChooser.setEnabled(false);
            }
        } else if (src == addRegex) {
            String name = searchName.getText();
            if (name == null || name.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_SEARCH_NAME.substring(0, SC_SEARCH_NAME.indexOf(":"))) + ".");
                return;
            }
            String regex = searchRegex.getText();
            if (regex == null || regex.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_SEARCH_REGEX.substring(0, SC_SEARCH_REGEX.indexOf(":"))) + ".");
                return;
            }
            if (searchRegexListModel.indexOf(name) == -1)
                searchRegexListModel.addElement(name);
            regexTable.put(name, regex);

            searchName.setText("");
            searchRegex.setText("");
        } else if (src == addLink) {
            String name = linkName.getText();
            if (name == null || name.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_LINK_NAME.substring(0, SC_LINK_NAME.indexOf(":"))) + ".");
                return;
            }
            String base = linkXPath.getBase();
            if (base == null || base.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_LINK_PATH.substring(0, SC_LINK_PATH.indexOf(":"))) + ".");
                return;
            }
            String sub = linkXPath.getSub();
            int subType = linkXPath.getSubType();
            try {
                int r = Integer.parseInt(linkRed.getText());
                if (r < 0 || r > 255) throw new NumberFormatException();
                int g = Integer.parseInt(linkGreen.getText());
                if (g < 0 || g > 255) throw new NumberFormatException();
                int b = Integer.parseInt(linkBlue.getText());
                if (b < 0 || b > 255) throw new NumberFormatException();
                Link newLink = new Link(name, base, sub, subType, r, g, b);
                linkTable.put(name, newLink);
                if (linkListModel.indexOf(name) == -1)
                    linkListModel.addElement(name);
                else {
                    linkListModel.removeElement(name);
                    linkListModel.addElement(name);
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, Helper.getTranslation(
                        "Please_enter_a_number_from_0_to_255_for_each_of_the_color_values"));
                return;
            }
        } else if (src == addFuzzyHword) {
            String pattern = fuzzyHwordOrigRegex.getText();
            if (pattern == null || pattern.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_FUZZY_ORIG_REGEX.substring(0,
                                        SC_FUZZY_ORIG_REGEX.indexOf(":"))) + ".");
                return;
            }
            String sub = fuzzyHwordReplacementRegex.getText();
            if (sub == null || sub.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_FUZZY_NEW_REGEX.substring(0,
                                        SC_FUZZY_NEW_REGEX.indexOf(":"))) + ".");
                return;
            }
            fuzzyHwordTable.put(pattern, sub);
            if (fuzzyHwordListModel.indexOf(pattern) == -1)
                fuzzyHwordListModel.addElement(pattern);
            else {
                fuzzyHwordListModel.removeElement(pattern);
                fuzzyHwordListModel.addElement(pattern);
            }
        } else if (src == addFuzzyGloss) {
            String pattern = fuzzyGlossOrigRegex.getText();
            if (pattern == null || pattern.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_FUZZY_ORIG_REGEX.substring(0,
                                        SC_FUZZY_ORIG_REGEX.indexOf(":"))) + ".");
                return;
            }
            String sub = fuzzyGlossReplacementRegex.getText();
            if (sub == null || sub.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_FUZZY_NEW_REGEX.substring(0,
                                        SC_FUZZY_NEW_REGEX.indexOf(":"))) + ".");
                return;
            }
            fuzzyGlossTable.put(pattern, sub);
            if (fuzzyGlossListModel.indexOf(pattern) == -1)
                fuzzyGlossListModel.addElement(pattern);
            else {
                fuzzyGlossListModel.removeElement(pattern);
                fuzzyGlossListModel.addElement(pattern);
            }
        } else if (src == addStopword) {
            String word = stopword.getText();
            if (word == null || word.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_STOPWORD.substring(0,
                                        SC_STOPWORD.indexOf(":"))) + ".");
                return;
            }
            if (stopwordListModel.indexOf(word) == -1)
                stopwordListModel.addElement(word);
        } else if (src == addStopchar) {
            String character = stopchar.getText();
            if (character == null || character.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_STOPCHAR.substring(0,
                                        SC_STOPCHAR.indexOf(":"))) + ".");
                return;
            }
            if (character.length() > 1) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, Helper.getTranslation(
                        "Please_enter_just_one_character"));
                return;
            }
            if (stopcharListModel.indexOf(character) == -1)
                stopcharListModel.addElement(character);
        } else if (src == addDictError) {
            String error = dictError.getText();
            if (error == null || error.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_DICTERROR.substring(0,
                                        SC_DICTERROR.indexOf(":"))) + ".");
                return;
            }
            if (dictErrorListModel.indexOf(error) == -1)
                dictErrorListModel.addElement(error);
        } else if (src == addXSL) {
            String filename = xslFile.getText();
            if (filename == null || filename.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, Helper.getTranslation(
                        "Please_pick_a_filename."));
                return;
            }
            String name = shortName.getText();
            if (name == null || name.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_XSL_NAME.substring(0,
                                        SC_XSL_NAME.indexOf(":"))) + ".");
                return;
            }
            String desc = xslDesc.getText();
            if (desc == null || desc.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        Helper.getTranslation(
                                SC_XSL_DESC.substring(0,
                                        SC_XSL_DESC.indexOf(":"))) + ".");
                return;
            }
            XslFile newFile = new XslFile(filename, name, desc);
            xslTable.put(name, newFile);
            if (xslListModel.indexOf(name) == -1)
                xslListModel.addElement(name);
            else {
                xslListModel.removeElement(name);
                xslListModel.addElement(name);
            }
        } else if (src == editRegex) {
            String name = (String)searchRegexList.getSelectedValue();
            if (name == null) return;
            searchName.setText(name);
            searchRegex.setText((String)regexTable.get(name));
        } else if (src == deleteRegex) {
            String name = (String)searchRegexList.getSelectedValue();
            if (name == null) return;
            int index = searchRegexListModel.indexOf(name);
            searchRegexListModel.remove(index);
            regexTable.remove(name);
        } else if (src == editLink) {
            String name = (String)linkList.getSelectedValue();
            if (name == null) return;
            linkName.setText(name);
            Link selected = (Link)linkTable.get(name);
            linkXPath.setPath(selected.xpath, selected.sub, selected.subType);
            linkRed.setText((new Integer(selected.r)).toString());
            linkGreen.setText((new Integer(selected.g)).toString());
            linkBlue.setText((new Integer(selected.b)).toString());
        } else if (src == deleteLink) {
            String name = (String)linkList.getSelectedValue();
            if (name == null) return;
            int index = linkListModel.indexOf(name);
            linkListModel.remove(index);
            linkTable.remove(name);
        } else if (src == editFuzzyHword) {
            String pattern = (String)fuzzyHwordList.getSelectedValue();
            if (pattern == null) return;
            fuzzyHwordOrigRegex.setText(pattern);
            fuzzyHwordReplacementRegex.setText((String)fuzzyHwordTable.get(pattern));
        } else if (src == deleteFuzzyHword) {
            String pattern = (String)fuzzyHwordList.getSelectedValue();
            if (pattern == null) return;
            int index = fuzzyHwordListModel.indexOf(pattern);
            fuzzyHwordListModel.remove(index);
            fuzzyHwordTable.remove(pattern);
        } else if (src == editFuzzyGloss) {
            String pattern = (String)fuzzyGlossList.getSelectedValue();
            if (pattern == null) return;
            fuzzyGlossOrigRegex.setText(pattern);
            fuzzyGlossReplacementRegex.setText((String)fuzzyGlossTable.get(pattern));
        } else if (src == deleteFuzzyGloss) {
            String pattern = (String)fuzzyGlossList.getSelectedValue();
            if (pattern == null) return;
            int index = fuzzyGlossListModel.indexOf(pattern);
            fuzzyGlossListModel.remove(index);
            fuzzyGlossTable.remove(pattern);
        } else if (src == editStopword) {
            String word = (String)stopwordList.getSelectedValue();
            if (word == null) return;
            stopword.setText(word);
        } else if (src == deleteStopword) {
            String word = (String)stopwordList.getSelectedValue();
            if (word == null) return;
            int index = stopwordListModel.indexOf(word);
            stopwordListModel.remove(index);
        } else if (src == editStopchar) {
            String word = (String)stopcharList.getSelectedValue();
            if (word == null) return;
            stopchar.setText(word);
        } else if (src == deleteStopchar) {
            String word = (String)stopcharList.getSelectedValue();
            if (word == null) return;
            int index = stopcharListModel.indexOf(word);
            stopcharListModel.remove(index);
        } else if (src == editDictError) {
            String error = (String)dictErrorList.getSelectedValue();
            if (error == null) return;
            dictError.setText(error);
        } else if (src == deleteDictError) {
            String error = (String)dictErrorList.getSelectedValue();
            if (error == null) return;
            int index = dictErrorListModel.indexOf(error);
            dictErrorListModel.remove(index);
        } else if (src == editXSL) {
            String name = (String)xslList.getSelectedValue();
            if (name == null) return;
            shortName.setText(name);
            XslFile selected = (XslFile)xslTable.get(name);
            xslFile.setText(selected.filename);
            xslDesc.setText(selected.description);
        } else if (src == deleteXSL) {
            String name = (String)xslList.getSelectedValue();
            if (name == null) return;
            int index = xslListModel.indexOf(name);
            xslListModel.remove(index);
            xslTable.remove(name);
        } else if (src == browseConv) {
            // open up a file chooser dialog
            JFileChooser chooser = new JFileChooser(RelFile.dictionaryDir);
            chooser.setMultiSelectionEnabled(false);
            KirrkirrFileFilter filter;
            filter = new KirrkirrFileFilter(KirrkirrFileFilter.XML_ENTRY);
            chooser.setFileFilter(filter);
            int returnVal = chooser.showDialog(this, ToolsDialog.SC_OK);

            //if a file is chosen
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                //fill in text field with chosen file name
                converter.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        } else if (src == createNewConv) {
            new DomainConverterEditor(true, this);
        } else if (src == colorChooser) {
            Color startColor;
            try {
                int r, g, b;
                r = Integer.parseInt(linkRed.getText());
                g = Integer.parseInt(linkGreen.getText());
                b = Integer.parseInt(linkBlue.getText());
                startColor = new Color(r, g, b);
            } catch (NumberFormatException nfe) {
                startColor = Color.black;
            }
            Color newColor = JColorChooser.showDialog(
                                this, "Choose Link Color", startColor);
            linkRed.setText((new Integer(newColor.getRed())).toString());
            linkGreen.setText((new Integer(newColor.getGreen())).toString());
            linkBlue.setText((new Integer(newColor.getBlue())).toString());
        } else if (src == mainColorChooser) {
            Color startColor;
            try {
                int r, g, b;
                r = Integer.parseInt(mainLinkRed.getText());
                g = Integer.parseInt(mainLinkGreen.getText());
                b = Integer.parseInt(mainLinkBlue.getText());
                startColor = new Color(r, g, b);
            } catch (NumberFormatException nfe) {
                startColor = Color.black;
            }
            Color newColor = JColorChooser.showDialog(
                                this, "Choose Link Color", startColor);
            mainLinkRed.setText((new Integer(newColor.getRed())).toString());
            mainLinkGreen.setText((new Integer(newColor.getGreen())).toString());
            mainLinkBlue.setText((new Integer(newColor.getBlue())).toString());
        } else if (src == subColorChooser) {
            Color startColor;
            try {
                int r, g, b;
                r = Integer.parseInt(subLinkRed.getText());
                g = Integer.parseInt(subLinkGreen.getText());
                b = Integer.parseInt(subLinkBlue.getText());
                startColor = new Color(r, g, b);
            } catch (NumberFormatException nfe) {
                startColor = Color.black;
            }
            Color newColor = JColorChooser.showDialog(
                                 this, "Choose Link Color", startColor);
            subLinkRed.setText((new Integer(newColor.getRed())).toString());
            subLinkGreen.setText((new Integer(newColor.getGreen())).toString());
            subLinkBlue.setText((new Integer(newColor.getBlue())).toString());
        } else if (src == subIsSeparate) {
            CardLayout cl = (CardLayout)(subCardsPanel.getLayout());
            cl.show(subCardsPanel, Helper.getTranslation(SC_SUBENTRY_ATTR));
        } else if (src == subIsNested) {
            CardLayout cl = (CardLayout)(subCardsPanel.getLayout());
            cl.show(subCardsPanel, Helper.getTranslation(SC_SUBENTRY_XPATH));
        } else if (src == subFixWAttr) {
            createSubLink.setEnabled(false);
            createMainLink.setEnabled(false);
            subLinkRed.setEnabled(false);
            subLinkGreen.setEnabled(false);
            subLinkBlue.setEnabled(false);
            subColorChooser.setEnabled(false);
            createSubLink.setSelected(false);
            createMainLink.setSelected(false);
        } else if (src == subFixWLink) {
            createSubLink.setEnabled(true);
            createMainLink.setEnabled(true);
        }
    }

    private class TagFinderHandler extends DefaultHandler {

        public String tagPath;

        public TagFinderHandler() {
            super();
            tagPath = "";
            tagNames.clear();
            subTags.clear();
        }

        private void addSubtags(String tag) {
            int slash = tag.indexOf("/", 1);
            while (slash != -1) {
                String topName = tag.substring(0, slash);
                String subPath = tag.substring(slash+1);
                if (!subTags.containsKey(topName))
                    subTags.put(topName, new HashSet());
                else {
                    HashSet subPaths = (HashSet)(subTags.get(topName));
                    subPaths.add(subPath);
                }
                slash = tag.indexOf("/", slash+1);
            }
        }

        public void startElement(String uri, String name, String raw,
                                 Attributes attrs) throws SAXException {
            super.startElement(uri, name, raw, attrs);
            tagPath+=("/"+name);
            addSubtags(tagPath);
            if (!tagNames.containsKey(tagPath))
                tagNames.put(tagPath, new HashSet());
            else {
                HashSet attributes = (HashSet)(tagNames.get(tagPath));
                for (int i = 0; i < attrs.getLength(); i++) {
                    attributes.add(attrs.getQName(i));
                    tagNames.put(tagPath, attributes);
                }
            }
        }

        public void endElement(String uri, String name, String raw)
                throws SAXException {
            super.endElement(uri, name, raw);
            tagPath=tagPath.substring(0, tagPath.lastIndexOf(name)-1);
        }

        public void processingInstruction(String target, String data)
                throws SAXException {
            System.out.println("THE PI!");
        }
    }

    private class SpecEntryPanel extends JPanel implements ItemListener {

        private static final String SC_ATTR = "attribute";
        static final String SC_XPATH = "Custom_XPath: ";
        static final String CUSTOM_XPATH = "CUSTOM_XPATH";
        private static final String SC_EMPTY = "";

        private JLabel entryLabel;
        JComboBox dictTagComboBox;
        private JPanel attrPanel;
        private JPanel attrCard;
        private JPanel xpathCard;
        private JPanel emptyCard;
        JCheckBox usingAttr;
        JComboBox attribute;
        private Vector empty;
        private JTextArea xpath;
        private boolean useAttr;
        private boolean useCustom;

        /*
                 public SpecEntryPanel(String entryName, String entryPath, String attr) {
                         this(entryName, entryPath, attr, false, true);
                 }
                 public SpecEntryPanel(String entryName, String entryPath, String attr,
                                 boolean centered) {
                         this(entryName, entryPath, attr, centered, true);
                 }
                 */
        public SpecEntryPanel(String entryName, String entryPath, String attr,
                              boolean centered, boolean useAttr, boolean useCustom) {
            this(entryName, entryPath, attr, centered, useAttr, useCustom, null);
        }

        public SpecEntryPanel(String entryName, String entryPath, String attr,
                              boolean centered, boolean useAttr, boolean useCustom, Object[] names) {
            super();
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            //setLayout(new GridLayout(3, 1));
            //setSize(new Dimension(500, 100));

            this.useAttr = useAttr;
            this.useCustom = useCustom;

            JPanel labelPanel = new JPanel();
            entryLabel = new JLabel(entryName);
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
            labelPanel.add(Box.createHorizontalStrut(15));
            labelPanel.add(entryLabel);
            entryLabel.setAlignmentX(0);
            labelPanel.add(Box.createHorizontalGlue());
            labelPanel.setAlignmentX(0);
            add(labelPanel);
            labelPanel.setAlignmentX(0);

            JPanel comboPanel = new JPanel();
            comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
            comboPanel.add(Box.createHorizontalGlue());

            if (names == null)
                dictTagComboBox = new JComboBox(theNames);
            else
                dictTagComboBox = new JComboBox(names);

            dictTagComboBox.insertItemAt("", 0);
            if (useCustom)
                dictTagComboBox.insertItemAt(Helper.getTranslation(CUSTOM_XPATH), 1);
            if (entryPath != null && (Arrays.binarySearch(theNames, entryPath) >= 0))
                dictTagComboBox.setSelectedItem(entryPath);
            else if (entryPath == null)
                dictTagComboBox.setSelectedItem("");
            else {
                if (useCustom)
                    dictTagComboBox.setSelectedItem(Helper.getTranslation(CUSTOM_XPATH));
                else
                    dictTagComboBox.setSelectedItem("");
            }

            dictTagComboBox.setMaximumSize(new Dimension(460, 40));
            dictTagComboBox.addItemListener(this);
            comboPanel.add(dictTagComboBox);
            if (centered)
                comboPanel.add(Box.createHorizontalGlue());
            comboPanel.setAlignmentX(0);
            add(comboPanel);
            comboPanel.setAlignmentX(0);

            attrPanel = new JPanel(new CardLayout());

            emptyCard = new JPanel();
            attrPanel.add(emptyCard, SC_EMPTY);
            boolean hasAttrs = false;
            if (useAttr) {
                attrCard = new JPanel();
                attrCard.setLayout(new BoxLayout(attrCard, BoxLayout.X_AXIS));
                attrCard.add(Box.createHorizontalGlue());
                attrCard.setAlignmentX(0);
                JLabel attrLabel = new JLabel(Helper.getTranslation(SC_ATTRIBUTE_PATH));
                attrCard.add(attrLabel);
                usingAttr = new JCheckBox();
                usingAttr.addItemListener(this);
                attrCard.add(usingAttr);
                if (centered)
                    attrCard.add(Box.createHorizontalGlue());

                empty = new Vector();
                empty.add("			");
                if (entryPath != null && (Arrays.binarySearch(theNames, entryPath) >= 0)) {
                    HashSet attrHS = (HashSet)(tagNames.get(entryPath));
                    if (attrHS.isEmpty()) {
                        attribute = new JComboBox(empty);
                        attribute.setEnabled(false);
                        usingAttr.setSelected(false);
                        usingAttr.setEnabled(false);
                        //attrPanel.setVisible(false);
                        CardLayout cl = (CardLayout)(attrPanel.getLayout());
                        cl.show(attrPanel, SC_EMPTY);
                    } else {
                        Object[] attrArray = attrHS.toArray();
                        Arrays.sort(attrArray);
                        attribute = new JComboBox(attrArray);
                        hasAttrs = true;
                        if (attr != null && (Arrays.binarySearch(attrArray, attr) >= 0)) {
                            usingAttr.setSelected(true);
                            usingAttr.setEnabled(true);
                            attribute.setEnabled(true);
                            attribute.setSelectedItem(attr);
                            attrPanel.setVisible(true);
                        }
                        else {
                            attribute.setEnabled(false);
                            usingAttr.setSelected(false);
                            usingAttr.setEnabled(true);
                            attrPanel.setVisible(true);
                        }
                    }
                } else if (entryPath == null ){
                    attribute = new JComboBox(empty);
                    attribute.setEnabled(false);
                    usingAttr.setSelected(false);
                    usingAttr.setEnabled(false);
                    //attrPanel.setVisible(false);
                    CardLayout cl = (CardLayout)(attrPanel.getLayout());
                    cl.show(attrPanel, SC_EMPTY);
                } else {
                    attribute = new JComboBox(empty);
                }

                attribute.setMaximumSize(new Dimension(100, 30));
                attrCard.add(attribute);
                attrCard.setAlignmentX(0);
                attrPanel.add(attrCard, SC_ATTR);
            }

            if (useCustom) {
                xpathCard = new JPanel();
                xpathCard.setLayout(new BoxLayout(xpathCard, BoxLayout.X_AXIS));
                xpathCard.setAlignmentX(0);
                xpathCard.add(Box.createHorizontalGlue());
                if (entryPath != null && (Arrays.binarySearch(theNames, entryPath) < 0))
                    xpath = new JTextArea(entryPath, 2, 30);
                else
                    xpath = new JTextArea(2, 30);
                xpath.setLineWrap(true);
                xpath.setMaximumSize(new Dimension(500, 60));
                JScrollPane xpathScroller = new JScrollPane(xpath);
                xpathScroller.setMaximumSize(new Dimension(500, 60));
                xpathScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                xpathScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                JLabel xpathLabel = new JLabel(Helper.getTranslation(SC_XPATH));
                xpathCard.add(xpathLabel);
                xpathCard.add(xpathScroller);
                if (centered)
                    xpathCard.add(Box.createHorizontalGlue());
                attrPanel.add(xpathCard, SC_XPATH);

            }

            attrPanel.setAlignmentX(0);
            add(attrPanel);

            CardLayout cl = (CardLayout)(attrPanel.getLayout());
            if (entryPath != null && (Arrays.binarySearch(theNames, entryPath) < 0)) {
                if (useCustom) {
                    cl.show(attrPanel, SC_XPATH);
                }
                else
                    cl.show(attrPanel, SC_EMPTY);
            } else if (hasAttrs) {
                cl.show(attrPanel, SC_ATTR);
            } else if (!useAttr) {
                cl.show(attrPanel, SC_EMPTY);
            }
            /*
                         JPanel empty = new JPanel();
                         JTextField tf = new JTextField(1);
                         tf.setVisible(false);
                         empty.add(tf);
                         //add(new Label(""));
                         add(empty);
                         */
            add(Box.createVerticalGlue());
            add(Box.createVerticalGlue());
            //add(Box.createVerticalStrut(10));
        }

        public void itemStateChanged(ItemEvent e) {
            Object src = e.getSource();
            if (src == usingAttr) {
                if (usingAttr.isSelected())
                    attribute.setEnabled(true);
                else
                    attribute.setEnabled(false);
            } else if (src == dictTagComboBox) {
                String newTag = (String)(dictTagComboBox.getSelectedItem());
                if (tagNames.containsKey(newTag)) {
                    if (useAttr) {
                        CardLayout cl = (CardLayout)(attrPanel.getLayout());
                        cl.show(attrPanel, SC_ATTR);
                        HashSet newAttrHS = (HashSet)(tagNames.get(newTag));
                        attribute.removeAllItems();
                        if (newAttrHS.isEmpty()) {
                            attribute.addItem("			");
                            attribute.setEnabled(false);
                            usingAttr.setSelected(false);
                            usingAttr.setEnabled(false);
                            //attrPanel.setVisible(false);
                            cl.show(attrPanel, SC_EMPTY);
                        } else {
                            Object[] newAttrs = newAttrHS.toArray();
                            Arrays.sort(newAttrs);
                            for (int i = 0; i < newAttrHS.size(); i++)
                                attribute.addItem(newAttrs[i]);
                            attribute.setEnabled(false);
                            usingAttr.setSelected(false);
                            usingAttr.setEnabled(true);
                            attrPanel.setVisible(true);
                        }
                    }
                    else
                        attrPanel.setVisible(false);
                } else if (newTag.equals("")) {
                    if (useAttr) {
                        attribute.removeAllItems();
                        attribute.setEnabled(false);
                        usingAttr.setSelected(false);
                        usingAttr.setEnabled(false);
                    }
                    //attrPanel.setVisible(false);
                    CardLayout cl = (CardLayout)(attrPanel.getLayout());
                    cl.show(attrPanel, SC_EMPTY);
                } else {
                    CardLayout cl = (CardLayout)(attrPanel.getLayout());
                    if (useCustom) {
                        cl.show(attrPanel, SC_XPATH);
                        xpath.setText("");
                        attrPanel.setVisible(true);
                    }
                    else
                        cl.show(attrPanel, SC_EMPTY);
                }
            }
            setAlignmentX(0);
        }

        public void setText(String path) {
            CardLayout cl = (CardLayout)(attrPanel.getLayout());
            if (path == null || path.equals("")) {
                dictTagComboBox.setSelectedItem("");
                //attrPanel.setVisible(false);
                cl.show(attrPanel, SC_EMPTY);
                return;
            }
            if (isSinglePath(path)) {
                int atIndex = path.indexOf("@");
                if ((atIndex < 0) && (Arrays.binarySearch(theNames,	path) >= 0)) {
                    dictTagComboBox.setSelectedItem(path);
                    //attrPanel.setVisible(false);
                    cl.show(attrPanel, SC_EMPTY);
                    return;
                }
                else if (atIndex >= 0){
                    String base = path.substring(0, atIndex-1);
                    String attr = path.substring(atIndex+1);
                    if (Arrays.binarySearch(theNames, base) >= 0) {
                        cl.show(attrPanel, SC_ATTR);
                        HashSet attrHS = (HashSet)(tagNames.get(base));
                        Object[] newAttrs = attrHS.toArray();
                        Arrays.sort(newAttrs);
                        if (Arrays.binarySearch(newAttrs, attr) >= 0){
                            dictTagComboBox.setSelectedItem(base);
                            Arrays.sort(newAttrs);
                            for (int i = 0; i < attrHS.size(); i++)
                                attribute.addItem(newAttrs[i]);
                            attribute.setEnabled(true);
                            usingAttr.setSelected(true);
                            usingAttr.setEnabled(true);
                            attrPanel.setVisible(true);
                            attribute.setSelectedItem(attr);
                        }
                        return;
                    }
                }
                dictTagComboBox.setSelectedItem(Helper.getTranslation(CUSTOM_XPATH));
                cl.show(attrPanel, SC_XPATH);
                xpath.setText(path);
                attrPanel.setVisible(true);
            }
        }

        public String getText() {
            if (((String)dictTagComboBox.getSelectedItem()).equals(CUSTOM_XPATH))
                return xpath.getText();
            else {
                if (useAttr && usingAttr.isSelected())
                    return ((String)(dictTagComboBox.getSelectedItem()) +
                            "/@" + (String)attribute.getSelectedItem());
                else
                    return (String)(dictTagComboBox.getSelectedItem());
            }
        }

        public boolean isValidEntry() {
            String path = (String)(dictTagComboBox.getSelectedItem());
            if (path == null || path.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        entryLabel.getText() + ".");
                return false;
            } else if (path.equalsIgnoreCase(Helper.getTranslation(CUSTOM_XPATH))){
                String customPath = xpath.getText().trim();
                if (customPath == null || customPath.equals("")) {
                    JOptionPane.showMessageDialog(
                            Kirrkirr.kk.window, "Please enter a " +
                            Helper.getTranslation(CUSTOM_XPATH) + ".");
                    return false;
                }
            }
            else if (useAttr && usingAttr.isSelected()) {
                String attr = (String)attribute.getSelectedItem();
                if (attr == null || attr.equals("")) {
                    JOptionPane.showMessageDialog(
                            Kirrkirr.kk.window,
                            "Please enter a attribute name for the " +
                                    entryLabel.getText() + "or uncheck the box.");
                    return false;
                }
            }
            return true;
        }
    }

    private class SpecSubTagPanel extends JPanel implements ItemListener {

        private static final String SC_ATTR = "attribute";
        private static final String NO_TAGS = "No_Subtags_were_found_in_scan";

        private JLabel entryLabel;
        private JComboBox dictTagComboBox;
        private JPanel attrPanel;
        private JPanel attrCard;
        private JPanel xpathCard;
        private JPanel comboPanel;
        private JCheckBox usingAttr;
        private JComboBox attribute;
        private Vector empty;
        private JTextArea xpath;
        private boolean isEmpty;

        private String parent;
        //private JTextField attribute;

        public SpecSubTagPanel(String entryName, String entryPath, String attr,
                               String parentPath) {
            super();
            setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            //setLayout(new GridLayout(3, 1));
            //setSize(new Dimension(500, 100));

            parent = parentPath;

            JPanel labelPanel = new JPanel();
            entryLabel = new JLabel(entryName);
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
            labelPanel.add(Box.createHorizontalStrut(15));
            labelPanel.add(entryLabel);
            entryLabel.setAlignmentX(0);
            labelPanel.add(Box.createHorizontalGlue());
            labelPanel.setAlignmentX(0);
            add(labelPanel);
            labelPanel.setAlignmentX(0);

            comboPanel = new JPanel();
            comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.X_AXIS));
            comboPanel.add(Box.createHorizontalGlue());

            if (parentPath != null && (Arrays.binarySearch(theNames, parentPath) >= 0)) {
                HashSet subTagHS = (HashSet)(subTags.get(parentPath));
                if (subTagHS.size() == 0) {
                    dictTagComboBox = new JComboBox();
                    dictTagComboBox.setMaximumSize(new Dimension(460, 40));
                    dictTagComboBox.addItemListener(this);
                    JLabel nothing = new JLabel(Helper.getTranslation(NO_TAGS));
                    comboPanel.add(nothing);
                    isEmpty = true;
                }
                else {
                    Object[] subTags = subTagHS.toArray();
                    Arrays.sort(subTags);
                    dictTagComboBox = new JComboBox(subTags);
                    dictTagComboBox.setSelectedItem(entryPath);
                    dictTagComboBox.setMaximumSize(new Dimension(460, 40));
                    dictTagComboBox.addItemListener(this);
                    comboPanel.add(dictTagComboBox);
                    isEmpty = false;
                }

            }
            else {
                //shouldnt ever occur
                dictTagComboBox = new JComboBox();
                dictTagComboBox.setMaximumSize(new Dimension(460, 40));
                dictTagComboBox.addItemListener(this);
                JLabel nothing = new JLabel(Helper.getTranslation(NO_TAGS));
                comboPanel.add(nothing);
                isEmpty = true;
            }
            comboPanel.add(Box.createHorizontalGlue());
            comboPanel.setAlignmentX(0);
            add(comboPanel);

            //attrPanel = new JPanel(new CardLayout());
            attrCard = new JPanel();
            attrCard.setLayout(new BoxLayout(attrCard, BoxLayout.X_AXIS));
            attrCard.add(Box.createHorizontalGlue());
            attrCard.setAlignmentX(0);
            JLabel attrLabel = new JLabel(Helper.getTranslation(SC_ATTRIBUTE_PATH));
            attrCard.add(attrLabel);
            usingAttr = new JCheckBox();
            usingAttr.addItemListener(this);
            attrCard.add(usingAttr);

            empty = new Vector();
            empty.add("			");
            if (!isEmpty && entryPath != null &&
                    (Arrays.binarySearch(theNames, parentPath+"/"+entryPath) >= 0)) {
                HashSet attrHS = (HashSet)(tagNames.get(parentPath+"/"+entryPath));
                if (attrHS.size() == 0) {
                    attribute = new JComboBox(empty);
                    attribute.setEnabled(false);
                    usingAttr.setSelected(false);
                    usingAttr.setEnabled(false);
                    attrCard.setVisible(false);
                }
                else {
                    Object[] attrArray = attrHS.toArray();
                    Arrays.sort(attrArray);
                    attribute = new JComboBox(attrArray);
                    if (attr != null && (Arrays.binarySearch(attrArray, attr) >= 0)) {
                        usingAttr.setSelected(true);
                        usingAttr.setEnabled(true);
                        attribute.setEnabled(true);
                        attribute.setSelectedItem(attr);
                        attrCard.setVisible(true);
                    }
                    else {
                        attribute.setEnabled(false);
                        usingAttr.setSelected(false);
                        usingAttr.setEnabled(true);
                        attrCard.setVisible(true);
                    }
                }
            }
            else if (entryPath == null ){
                attribute = new JComboBox(empty);
                attribute.setEnabled(false);
                usingAttr.setSelected(false);
                usingAttr.setEnabled(false);
                attrCard.setVisible(false);
            } else {
                attribute = new JComboBox(empty);
                attribute.setEnabled(false);
                usingAttr.setSelected(false);
                usingAttr.setEnabled(false);
                attrCard.setVisible(false);
            }

            attribute.setMaximumSize(new Dimension(100, 30));
            attrCard.add(attribute);
            attrCard.add(Box.createHorizontalGlue());
            attrCard.setAlignmentX(0);
            //attrPanel.add(attrCard, SC_ATTR);
            //attrPanel.setAlignmentX(0);
            add(attrCard);
            add(Box.createVerticalGlue());

            //add(new Label(""));

            add(Box.createVerticalGlue());
            add(Box.createVerticalGlue());
            //add(Box.createVerticalStrut(10));
        }

        public void setNewParent(String newParent) {
            parent = newParent;
            HashSet subTagHS = (HashSet)(subTags.get(parent));
            if (subTagHS == null || subTagHS.size() == 0) {
                if (!isEmpty) {
                    JLabel nothing = new JLabel(Helper.getTranslation(NO_TAGS));
                    comboPanel.removeAll();
                    comboPanel.add(Box.createHorizontalGlue());
                    comboPanel.add(nothing);
                    comboPanel.add(Box.createHorizontalGlue());
                    isEmpty = true;
                    attribute.setEnabled(false);
                    usingAttr.setSelected(false);
                    usingAttr.setEnabled(false);
                    attrCard.setVisible(false);
                }
            }
            else {
                if (isEmpty) {
                    comboPanel.removeAll();
                    comboPanel.add(Box.createHorizontalGlue());
                    comboPanel.add(dictTagComboBox);
                    dictTagComboBox.setAlignmentX(0);
                    comboPanel.add(Box.createHorizontalGlue());
                    comboPanel.setAlignmentX(0);
                    isEmpty = false;
                }
                dictTagComboBox.removeAllItems();
                Object[] subTagArray = subTagHS.toArray();
                Arrays.sort(subTagArray);
                for (int i = 0; i < subTagHS.size(); i++)
                    dictTagComboBox.addItem(subTagArray[i]);
                //comboPanel.add(dictTagComboBox);

                String firstChild = (String)subTagArray[0];
                if ( ! isEmpty && tagNames.containsKey(parent+"/"+firstChild)) {
                    HashSet newAttrHS = (HashSet)(tagNames.get(parent+"/"+firstChild));
                    attribute.removeAllItems();
                    if (newAttrHS.isEmpty()) {
                        attribute.addItem("			");
                        attribute.setEnabled(false);
                        usingAttr.setSelected(false);
                        usingAttr.setEnabled(false);
                        attrCard.setVisible(false);
                    } else {
                        Object[] newAttrs = newAttrHS.toArray();
                        Arrays.sort(newAttrs);
                        for (int i = 0; i < newAttrHS.size(); i++)
                            attribute.addItem(newAttrs[i]);
                        attribute.setEnabled(false);
                        usingAttr.setSelected(false);
                        usingAttr.setEnabled(true);
                        attrCard.setVisible(true);
                    }
                } else {
                    attribute.removeAllItems();
                    attribute.setEnabled(false);
                    usingAttr.setSelected(false);
                    usingAttr.setEnabled(false);
                    attrCard.setVisible(false);
                }
            }
        }

        public void itemStateChanged(ItemEvent e) {
            Object src = e.getSource();
            if (src == usingAttr) {
                if (usingAttr.isSelected())
                    attribute.setEnabled(true);
                else
                    attribute.setEnabled(false);
            } else if (src == dictTagComboBox) {
                String newTag = (String)(dictTagComboBox.getSelectedItem());
                if (tagNames.containsKey(parent+"/"+newTag)) {
                    HashSet newAttrHS = (HashSet)(tagNames.get(parent+"/"+newTag));
                    attribute.removeAllItems();
                    if (newAttrHS.size() == 0) {
                        attribute.addItem("			");
                        attribute.setEnabled(false);
                        usingAttr.setSelected(false);
                        usingAttr.setEnabled(false);
                        attrCard.setVisible(false);
                    }
                    else {
                        Object[] newAttrs = newAttrHS.toArray();
                        Arrays.sort(newAttrs);
                        for (int i = 0; i < newAttrHS.size(); i++)
                            attribute.addItem(newAttrs[i]);
                        attribute.setEnabled(false);
                        usingAttr.setSelected(false);
                        usingAttr.setEnabled(true);
                        attrCard.setVisible(true);
                    }
                } else {
                    attribute.removeAllItems();
                    attribute.setEnabled(false);
                    usingAttr.setSelected(false);
                    usingAttr.setEnabled(false);
                    attrCard.setVisible(false);
                }
            }
            setAlignmentX(0);
        }

        public void setText(String newSub) {
            if (newSub != null)
                dictTagComboBox.setSelectedItem(newSub);
        }

        public String getText() {
            if (usingAttr.isSelected()) {
                return dictTagComboBox.getSelectedItem() +
                        "/@" + attribute.getSelectedItem();
            } else {
                return (String) dictTagComboBox.getSelectedItem();
            }
        }

        public String getFullPath() {
            if (usingAttr.isSelected()) {
                return (parent + "/" + (String)(dictTagComboBox.getSelectedItem()) +
                        "/@" + (String)attribute.getSelectedItem());
            } else {
                return parent + "/" + (String)(dictTagComboBox.getSelectedItem());
            }
        }

        public boolean isValidEntry() {
            String path = (String)(dictTagComboBox.getSelectedItem());
            if (path == null || path.equals("")) {
                JOptionPane.showMessageDialog(
                        Kirrkirr.kk.window, "Please enter a " +
                        entryLabel.getText() + ".");
                return false;
            }
            if (usingAttr.isSelected()) {
                String attr = (String)attribute.getSelectedItem();
                if (attr == null || attr.equals(""))
                    JOptionPane.showMessageDialog(
                            Kirrkirr.kk.window,
                            "Please enter a attribute name for the " +
                                    entryLabel.getText() + "or uncheck the box.");
                return false;
            }
            return true;
        }
    }


    private class SpecWithSubsPanel extends SpecEntryPanel implements ItemListener {
        public static final String SC_WITH_TAG = "split_by_subtags";
        public static final String SC_WITH_CHAR = "split_by_characters";
        public static final String SC_WITH_REGEX = "split_by_regular_expression";
        public static final String SC_NONE = "";
        public static final String SC_CHAR_DESC = "Enter_the_splitting_chars_\n(" +
                "Whitespace_will_be_trimmed.\nRemember_to_escape_backslashes)";
        public static final String SC_REGEX_DESC = "Enter_the_regular_expression_\n" +
                "to_be_used_in the splitting._(Remember_\n_to_escape_backslashes)";

        // private JPanel cards;

        private SpecSubTagPanel subtagPanel;
        private JTextField characters;
        private JTextField regex;

        //private JCheckBox usesSubComponents;
        private JComboBox subComponentsType;
        private JPanel subCards;
        private int subType;

        private boolean hasSubtag;

        public SpecWithSubsPanel(String entryName, String entryPath, String attr,
                                 int subType, String subName, String sub, String additionalAttr) {
            this(entryName, entryPath, attr, subType, subName, sub,
                    additionalAttr, true, false);
        }

        public SpecWithSubsPanel(String entryName, String entryPath, String attr,
                                 int subType, String subName, String sub, String additionalAttr,
                                 boolean subtagCard, boolean centered) {
            super(entryName, entryPath, attr, centered, true, true);

            this.subType = subType;
            hasSubtag = subtagCard;

            JPanel subPanel = new JPanel();
            subPanel.setLayout(new BorderLayout());

            JPanel subTop = new JPanel();
            //usesSubComponents = new JCheckBox(subName);
            //usesSubComponents.addItemListener(this);
            //subTop.add(usesSubComponents);
            String[] comboBoxItems;
            if (hasSubtag)
                comboBoxItems = new String[]{ Helper.getTranslation(SC_NONE),
                        Helper.getTranslation(SC_WITH_TAG),
                        Helper.getTranslation(SC_WITH_CHAR),
                        Helper.getTranslation(SC_WITH_REGEX) };
            else
                comboBoxItems = new String[]{ Helper.getTranslation(SC_NONE),
                        Helper.getTranslation(SC_WITH_CHAR),
                        Helper.getTranslation(SC_WITH_REGEX) };
            subComponentsType = new JComboBox(comboBoxItems);
            subComponentsType.setEditable(false);
            subComponentsType.addItemListener(this);
            subTop.add(subComponentsType);
            subPanel.add(subTop, BorderLayout.NORTH);

            if (super.usingAttr.isSelected() && subType != 1)
                subComponentsType.setEnabled(false);

            subCards = new JPanel(new CardLayout());

            /*public SpecSubTagPanel(String entryName, String entryPath, String attr,
                                 boolean isSubtag, String parentPath) {*/

            if (subtagCard) {
                if (sub != null && subType == 1 && (Arrays.binarySearch(theNames,
                        entryPath) >= 0))
                    subtagPanel = new SpecSubTagPanel("", sub, additionalAttr, entryPath);
                else
                    subtagPanel = new SpecSubTagPanel("", null, null, null);
                subCards.add(subtagPanel, Helper.getTranslation(SC_WITH_TAG));
            }
            JPanel charPanel = new JPanel();
            JTextArea charArea = new JTextArea(Helper.getTranslation(SC_CHAR_DESC));
            charArea.setEditable(false);
            charArea.setBackground(charPanel.getBackground());
            charPanel.add(charArea);
            if (sub == null || (sub != null && subType != 2))
                characters = new JTextField("", 15);
            else
                characters = new JTextField(sub, 15);
            charPanel.add(characters);
            subCards.add(charPanel, Helper.getTranslation(SC_WITH_CHAR));

            JPanel regexPanel = new JPanel();
            JTextArea regexArea = new JTextArea(Helper.getTranslation(SC_REGEX_DESC));
            regexArea.setEditable(false);
            regexArea.setBackground(regexPanel.getBackground());
            regexPanel.add(regexArea);
            if (sub == null || (sub != null && subType != 3))
                regex = new JTextField("", 15);
            else
                regex = new JTextField(sub, 15);
            regexPanel.add(regex);
            subCards.add(regexPanel, Helper.getTranslation(SC_WITH_REGEX));

            JPanel noneUsed = new JPanel();
            subCards.add(noneUsed, Helper.getTranslation(SC_NONE));

            subPanel.add(subCards, BorderLayout.CENTER);
            subPanel.setAlignmentX(0);
            add(subPanel);

            switchLayout();

        }

        public void setPath(String base, String sub, int sT) {
            CardLayout cl = (CardLayout)(subCards.getLayout());
            subType = sT;
            if (sub != null) {
                if (subType == 1) {
                    subtagPanel.setText(sub);
                    cl.show(subCards, Helper.getTranslation(SC_WITH_TAG));
                } else if (subType == 2) {
                    characters.setText(sub);
                    cl.show(subCards, Helper.getTranslation(SC_WITH_CHAR));
                } else if (subType == 3) {
                    regex.setText(sub);
                    cl.show(subCards, Helper.getTranslation(SC_WITH_REGEX));
                }
                else
                    cl.show(subCards, Helper.getTranslation(SC_NONE));
            }
            else
                cl.show(subCards, Helper.getTranslation(SC_NONE));
            super.setText(base);
        }

        public String getBase() {
            return super.getText();
        }

        public String getSub() {
            switch (subType) {
                case 0: return null;
                case 1: return (subtagPanel.getText());
                case 2: return characters.getText();
                    //case 2: return "["+characters.getText().trim()+"]+";
                case 3: return regex.getText();
                default: return null;
            }
        }

        public int getSubType() { return subType; }

        public boolean willNeedToBeParsed() {
            if (subType == 2 || subType == 3) return true;
            return false;
        }

        private void switchLayout() {
            CardLayout cl = (CardLayout)(subCards.getLayout());
            switch (subType) {
                case 0: subComponentsType.setSelectedItem(Helper.getTranslation(SC_NONE));
                    cl.show(subCards, Helper.getTranslation(SC_NONE));
                    break;
                case 1: subComponentsType.setSelectedItem(Helper.getTranslation(SC_WITH_TAG));
                    cl.show(subCards, Helper.getTranslation(SC_WITH_TAG));
                    break;
                case 2: subComponentsType.setSelectedItem(Helper.getTranslation(SC_WITH_CHAR));
                    cl.show(subCards, Helper.getTranslation(SC_WITH_CHAR));
                    break;
                case 3: subComponentsType.setSelectedItem(Helper.getTranslation(SC_WITH_REGEX));
                    cl.show(subCards, Helper.getTranslation(SC_WITH_REGEX));
                    break;
                default:
                    subComponentsType.setSelectedItem(Helper.getTranslation(SC_NONE));
                    cl.show(subCards, Helper.getTranslation(SC_NONE));
                    break;
            }
            if (subType != 0) {
                super.usingAttr.setSelected(false);
                super.usingAttr.setEnabled(false);
                super.attribute.setEnabled(false);
            } else {
                super.usingAttr.setEnabled(true);
                super.attribute.setEnabled(true);
            }
        }

        public boolean isValidEntry() {
            if (!super.isValidEntry()) return false;

            if (subType == 0) return true;
            if (subType == 1) return subtagPanel.isValidEntry();
            if (subType == 2) {
                String split = characters.getText();
                if (split == null || split.equals("")) {
                    JOptionPane.showMessageDialog(
                            Kirrkirr.kk.window, Helper.getTranslation(
                            "Please_enter_splitting_characters."));
                    return false;
                }
                return true;
            }
            if (subType == 3) {
                String split = regex.getText();
                if (split == null || split.equals("")) {
                    JOptionPane.showMessageDialog(
                            Kirrkirr.kk.window, Helper.getTranslation(
                            "Please_enter_a_regular_expression."));
                    return false;
                }
            }
            return true;
        }

        public void itemStateChanged(ItemEvent e) {
            super.itemStateChanged(e);
            Object src = e.getSource();
            if (src == subComponentsType) {
                String newType = (String)subComponentsType.getSelectedItem();
                if (newType.equals(Helper.getTranslation(SC_NONE)))
                    subType = 0;
                else if (newType.equals(Helper.getTranslation(SC_WITH_TAG)))
                    subType = 1;
                else if (newType.equals(Helper.getTranslation(SC_WITH_CHAR)))
                    subType = 2;
                else if (newType.equals(Helper.getTranslation(SC_WITH_REGEX)))
                    subType = 3;
                else
                    subType = 0;

                CardLayout cl = (CardLayout)(subCards.getLayout());
                cl.show(subCards, newType);
                /*
                                  if (subType != 0) {
                                          super.usingAttr.setSelected(false);
                                          super.usingAttr.setEnabled(false);
                                          super.attribute.setEnabled(false);
                                  } else {
                                          super.usingAttr.setEnabled(true);
                                          super.attribute.setEnabled(true);
                                  }*/
            } else if (src == super.dictTagComboBox) {
                subType = 0;
                if (hasSubtag)
                    subtagPanel.setNewParent((String)super.dictTagComboBox.getSelectedItem());
                characters.setText("");
                regex.setText("");
                switchLayout();
                if ((super.dictTagComboBox.getSelectedItem())
                        .equals(Helper.getTranslation(super.CUSTOM_XPATH)))
                    subComponentsType.setEnabled(false);
                else
                    subComponentsType.setEnabled(true);
            } else if (src == super.usingAttr) {
                if (super.usingAttr.isSelected()) {
                    subType = 0;
                    CardLayout cl = (CardLayout)(subCards.getLayout());
                    cl.show(subCards, SC_NONE);
                    if (hasSubtag)
                        subComponentsType.removeItemAt(1);
                    //subComponentsType.setEnabled(false);
                } else {
                    if (hasSubtag)
                        subComponentsType.insertItemAt(
                                new String(Helper.getTranslation(SC_WITH_TAG)), 1);
                    //subComponentsType.setEnabled(true);
                }
            }
        }
    }

    private class Link {
        public String name;
        public String xpath;
        public String sub;
        public int r, g, b;
        public int subType;

        public Link(String n, String x, String s, int st, int red, int green, int blue) {
            name = n;
            xpath = x;
            sub = s;
            subType = st;
            r = red;
            g = green;
            b = blue;
        }
    }

    private class XslFile {
        public String filename;
        public String shortName;
        public String description;

        public XslFile(String f, String s, String d) {
            filename = f;
            shortName = s;
            description = d;
        }
    }

    static class ProcessProgressDialog extends JDialog implements IndexMakerTracker
    {
        private boolean doDict; //whether each type of file
        //is being built
        private boolean doneDispose; //=false;

        private int stages, curStage;  //number of passes to complete

        private JLabel stageName;
        private JProgressBar progressBar;

        private static final String SC_PROCESS_PROG = "Pre-Processing Progress";

        private static final String SC_GET_FPOS = "Getting file positions";
        private static final String SC_DICT = "Creating New Dictionary";
        private static final String SC_SPEC = "Creating New Spec File";

        public ProcessProgressDialog(JFrame owner, boolean dict) {
            super(owner, Helper.getTranslation(SC_PROCESS_PROG), false);

            doDict = dict;


            stages = 1; //for gathering fpos indices
            if(doDict) stages+=2;

            curStage = 1;

            JPanel progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel,
                    BoxLayout.Y_AXIS));
            progressPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            stageName = new JLabel("Step " + curStage + " of " + stages +
                    ": "  + SC_SPEC);

            stageName.setAlignmentX(CENTER_ALIGNMENT);

            //set up progress bar - max is irrelevant until we get
            //nextPassTotal notification
            progressBar = new JProgressBar(0, 100);  // initialized to min value
            stageName.setLabelFor(progressBar);
            progressBar.setAlignmentX(CENTER_ALIGNMENT);

            progressPanel.add(stageName);
            progressPanel.add(Box.createVerticalStrut(12));
            progressPanel.add(progressBar);
            getContentPane().add(progressPanel);
            Rectangle bnds = owner.getBounds();

            setLocation(bnds.x + bnds.width/2 - 125,
                    bnds.y + bnds.height/2 - 50);
            setSize(new Dimension(250,100));
            setVisible(true);
        }


        /* For the IndexMakerTracker interface
         *
         */

        public void totalStepsForPass(int nSteps) {
            progressBar.setMaximum(nSteps);
        }

        public void stepsDone(int nStepsDone) {
            progressBar.setValue(nStepsDone);
        }

        public void passDone() {
            ++curStage;  //move to next Stage

            //set up name for next stage
            if (doDict && curStage == 2) {
                stageName.setText("Step " + curStage + " of " + stages +
                        ": " + SC_GET_FPOS);
            }
            else if (doDict && curStage == 3) {
                stageName.setText("Step " + curStage + " of " + stages +
                        ": " + SC_DICT);
            } else {
                maybeDispose();
            }
        }

        public void maybeDispose() {
            if (!doneDispose) {
                dispose(); //all done
                doneDispose = true;
            }
        }

    } // end class ProcessProgressDialog

}
