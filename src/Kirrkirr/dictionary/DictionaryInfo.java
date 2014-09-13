package Kirrkirr.dictionary;

import Kirrkirr.util.Regex;
import Kirrkirr.util.OroRegex;
import Kirrkirr.util.Dbg;

import java.awt.Color;
import java.util.*;
import java.io.*;
import javax.swing.JOptionPane;

import org.w3c.dom.*;
import org.xml.sax.*;
import org.apache.xerces.parsers.*;
import org.xml.sax.helpers.DefaultHandler;


public class DictionaryInfo extends DefaultHandler {

    /** DictionaryEntryXPath is an xpath, but it is limited to a
     *  restricted subspace of xpaths that can be easily manipulated by
     *  the program.  It must start with a slash (be rooted) and then be
     *  a sequence of elements along the child axis.  This sequence of
     *  elements may have restrictions hanging off them
     *  (e.g., [@lang="Biao-jiao Mien"]), and these are respected.
     */
    private String dictionaryEntryXPath;
    private String dictionaryTag;
    private String entryTag;
    private String headwordXPath;
    private String uniquifierXPath;
    private String senseXPath;

    private Vector links; //holds link name, color, xpath (all needed)
    private String linkHeadwordXPath;
    private String linkUniquePaddingXPath;
    private Vector xslData; //xsl data -- fname, short name, description
    private Vector customData; //holds name and xpath for each custom field

    private Document doc;
    private DOMParser parser;
    private String filename;

    //lazy init
    private Regex[] GlossLanguageSubs, HeadwordLanguageSubs;

    //public static final int IMAGES=0, AUDIO=1,
    //    DOMAIN=2, FREQ=3, SUBWORD=4, DIALECT=5, REGISTER=6;

    public static final String SPEC_ROOT = "KIRRKIRR_SPEC";
    public static final String DICTIONARY_ENTRY_XPATH =
        "DICTIONARY_ENTRY_XPATH";
    public static final String HEADWORD = "HEADWORD_XPATH";
    public static final String UNIQUIFIER = "UNIQUIFIER_XPATH";
    public static final String HEADWORD_REGEXP = "HEADWORD_REGEXP";
    public static final String UNIQUIFIER_REGEXP = "UNIQUIFIER_REGEXP";
    public static final int NUM_LINK_PROPERTIES = 3;
    public static final String LINKS = "LINKS";
    public static final String REF_HEADWORD_XPATH = "REF_HEADWORD_XPATH";
    public static final String REF_UNIQUIFIER_XPATH = "REF_UNIQUIFIER_XPATH";
    public static final String LINK_NODE = "LINK";
    public static final String LINK_NAME = "LINK_NAME";
    public static final String LINK_COLOR = "LINK_COLOR";
    public static final String LINK_COLOR_NAME = "COLOR_NAME";
    public static final String LINK_COLOR_R = "COLOR_R";
    public static final String LINK_COLOR_G = "COLOR_G";
    public static final String LINK_COLOR_B = "COLOR_B";
    public static final String LINK_XPATH = "LINK_XPATH";
    public static final String XSL_FILES = "XSL_FILES";
    public static final int NUM_XSL_PROPERTIES = 3;
    public static final String XSL_FILE = "XSL_FILE";
    public static final String XSL_FILENAME = "XSL_FILENAME";
    public static final String XSL_SHORT = "SHORT_NAME";
    public static final String XSL_DESC = "XSL_DESCRIPTION";
    public static final String SEARCH_REGEXPS = "SEARCH_REGEXPS";
    public static final String SEARCH_REGEXP = "SEARCH_REGEXP";
    public static final String MENU_NAME = "MENU_NAME";
    public static final String REGEXP = "REGEXP";
    public static final String POS_XPATH = "POS_XPATH";
    public static final String SENSE_XPATH = "SENSE_XPATH";
    public static final String IMAGES_XPATH = "IMAGES_XPATH";
    public static final String AUDIO_XPATH = "AUDIO_XPATH";
    public static final String DOMAIN_XPATH = "DOMAIN_XPATH";
    public static final String DOMAIN_COMPONENT_XPATH = "DOMAIN_COMPONENT_XPATH";
    public static final String FREQUENCY_XPATH = "FREQUENCY_XPATH";
    public static final String SUBWORD_XPATH = "SUBWORD_XPATH";
    public static final String DIALECT_XPATH = "DIALECT_XPATH";
    public static final String REGISTER_XPATH = "REGISTER_XPATH";
    public static final String HEADWORD_LANGUAGE = "HEADWORD_LANGUAGE_SUBS";
    public static final String GLOSS_LANGUAGE = "GLOSS_LANGUAGE_SUBS";
    public static final String SUBSTITUTION = "SUBSTITUTION";
    public static final String SUBS_ORIGINAL = "ORIGINAL_REGEXP";
    public static final String SUBS_REPLACEMENT = "REPLACEMENT_REGEXP";
    public static final String FUZZY_SPELLING = "FUZZY_SPELLING_SUBSTITUTIONS";
    public static final String STOPWORDS = "STOPWORDS";
    public static final String STOPWORD = "STOPWORD";
    public static final String STOPCHARS = "STOPCHARS";
    public static final String STOPCHAR = "STOPCHAR";
    public static final String DICTERRORS = "DICTERRORS";
    public static final String DICTERROR = "DICTERROR";
    public static final String BIDIRECTIONAL_XPATH = "REVERSE_DICTIONARY_XPATH";
    public static final String GLOSS_XPATH = "GLOSS_XPATH";
    public static final String GLOSS_LANGUAGE_NAME = "GLOSS_LANGUAGE_NAME";
    public static final String DICTIONARY_LANGUAGE_NAME = "DICTIONARY_LANGUAGE_NAME";
    public static final String GLOSS_LANGUAGE_ICON = "GLOSS_LANGUAGE_ICON";
    public static final String DICTIONARY_LANGUAGE_ICON = "DICTIONARY_LANGUAGE_ICON";


    //much ugliness here. We need to be able to convert a color name to a
    //string, and this beats reflection.
    private static final Object[] COLOR_TABLE = {
        "black", Color.black,
        "blue", Color.blue,
        "cyan", Color.cyan,
        "darkGray", Color.darkGray,
        "gray", Color.gray,
        "green", Color.green,
        "lightGray", Color.lightGray,
        "magenta", Color.magenta,
        "orange", Color.orange,
        "pink", Color.pink,
        "red", Color.red,
        "white", Color.white,
        "yellow", Color.yellow
    };

    /**
     * opens the given filename and initializes based on what it
     * finds.
     */
    public DictionaryInfo(String filename) throws Exception {
        this.filename = filename;
        parser = new DOMParser();

        try {
            parser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
            parser.setErrorHandler(this);
        } catch (Exception ex) {
            if (Dbg.DICT_INFO) {
                Dbg.print("DictionaryInfo Constructor error");
                ex.printStackTrace();
            }
        }
        initDictionary();
    }


    /** Initialize the dictionary info.
     *  We'll do most things lazily, but DictionaryEntryXPath,
     *  HeadwordXPath, and UniquePaddingXPath need to be present, so we'll
     *  find those here.
     */
    private void initDictionary() throws Exception {
    	InputStream fis = new FileInputStream(filename);
    	parser.parse(new InputSource(fis));

        doc = parser.getDocument();

        Element root = doc.getDocumentElement();
        if (root.getTagName().equalsIgnoreCase("parseError")) {
          throw new Exception("XML Parsing Error of Specification File.");
        }
        if ( ! root.getTagName().equals(SPEC_ROOT)) {
            throw new Exception("Invalid Document; expecting " + SPEC_ROOT +
            ", found " + root.getTagName());
        }
        dictionaryEntryXPath =
                getElementFromName(DICTIONARY_ENTRY_XPATH);
        if (Dbg.DICT_INFO) {
            Dbg.print("DictionaryEntryXPath is " + dictionaryEntryXPath);
        }
        if ( ! okayRestrictedXPath()) {
            throw new Exception("Invalid " + DICTIONARY_ENTRY_XPATH
                                + ": " + dictionaryEntryXPath);
        }
        headwordXPath = getElementFromName(HEADWORD);
        uniquifierXPath =getValueFromName(UNIQUIFIER);
        senseXPath = getValueFromName(SENSE_XPATH);
        initLinks();
        fis.close();
    }


    /** Checks that this is a simple XPath from the root, from which we can
     *  easily extract individual elements.
     *
     *  @return false if the path isn't okay
     */
    private boolean okayRestrictedXPath() {
        try {
            dictionaryTag = initializeDictionaryTag();
            entryTag = initializeEntryTag();
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    private String getElementFromName(String nodeName) throws Exception {
        String value = getValueFromName(nodeName);
        if (value == null) {
            throw new Exception("Node " + nodeName + " not found");
        }
        return value;
    }

    /** This gets the name of the outermost element of the entire dictionary.
     */
    private String initializeDictionaryTag() {
        String xp = dictionaryEntryXPath;
        Regex restrict = new OroRegex("\\[[^[]*\\]", "");
        xp = restrict.doReplace(xp);
        if (xp.charAt(0) != '/') {
            throw new RuntimeException("Bad DictionaryEntryXPath: doesn't start with slash");
        }
        int nextSlash = xp.indexOf('/', 1);
        if (nextSlash == -1) {
            throw new RuntimeException("Bad DictionaryEntryXPath: no non-initial slash");
        }
        if (nextSlash == 1) {
            throw new RuntimeException("Bad DictionaryEntryXPath: begins with 2 slashes");
        }
        xp = xp.substring(1, nextSlash);
        xp = handleChildAxisIfPresent(xp);
        if (Dbg.DICT_INFO) {
            Dbg.print("dictionaryTag is: " + xp);
        }
        return xp;
    }


    public String getDictionaryTag() {
        return dictionaryTag;
    }

    /** Returns the name of the element that contains entries.
     *
     *  @return Name of element for each dictionary entry, e.g., "WORD" or "ENTRY"
     */
    private String initializeEntryTag() {
        String xp = dictionaryEntryXPath;
        Regex restrict = new OroRegex("\\[[^[]*\\]", "");
        xp = restrict.doReplace(xp);
        int lastSlash = xp.lastIndexOf('/');
        if (lastSlash == -1) {
            throw new RuntimeException("Bad DictionaryEntryXPath: no slash");
        }
        if (lastSlash == xp.length() - 1) {
            throw new RuntimeException("Bad DictionaryEntryXPath: ends in slash");
        }
        xp = xp.substring(lastSlash + 1);
        xp = handleChildAxisIfPresent(xp);
        if (Dbg.DICT_INFO) {
            Dbg.print("entryTag is: " + xp);
        }
        return xp;
    }


    public String getEntryTag() {
        return entryTag;
    }


    /** Deletes a "child::" beginning from an xpath and complains if any other
     *  axis is present.
     *
     *  @param xPathElement A component of an XPath (between slashes)
     *  @return The component with any "child::" axis deleted
     *  @throws RuntimeException If any other axis is present
     */
    public static String handleChildAxisIfPresent(String xPathElement) {
        int colons = xPathElement.lastIndexOf("::");
        if (colons != -1) {
            String axis = xPathElement.substring(0, colons);
            if ( ! axis.equals("child")) {
                throw new RuntimeException("Bad XPath axis: " + xPathElement);
            }
            // start after the "::" (of length 2)
            xPathElement = xPathElement.substring(colons + 2);
        }
        return xPathElement;
    }


    public String getDictionaryEntryXPath() {
        return dictionaryEntryXPath;
    }


    public String getUniquifierXPath(){
        return uniquifierXPath;
    }

    public String getHeadwordXPath() {
        return headwordXPath;
    }

    /** Get regexp for isolating headword, or retun null */
    public String getHeadwordRegexp() {
        return getValueFromName(HEADWORD_REGEXP);
    }

    /** Get regexp for isolating uniquifier, or retun null */
    public String getUniquifierRegexp() {
        return getValueFromName(UNIQUIFIER_REGEXP);
    }


    public String getSenseXPath() {
        return senseXPath;
    }

    public String getImagesXPath() {
        return(getValueFromName(IMAGES_XPATH));
    }

    public String getAudioXPath() {
        return(getValueFromName(AUDIO_XPATH));
    }

    public String getRefHeadwordXPath() {
        return getValueFromName(REF_HEADWORD_XPATH);
    }

    public String getRefUniquePaddingXPath() {
        return getValueFromName(REF_UNIQUIFIER_XPATH);
    }

    public String getDomainXPath() {
        return(getValueFromName(DOMAIN_XPATH));
    }

    public String getDomainComponentXPath() {
        return(getValueFromName(DOMAIN_COMPONENT_XPATH));
    }

    public String getDomainComponentFullXPath() {
        String comp = getValueFromName(DOMAIN_COMPONENT_XPATH);
        if (comp == null) {
            return getValueFromName(DOMAIN_XPATH);
        } else {
            return getValueFromName(DOMAIN_XPATH) + "/" + comp;
        }
    }

    public String getFrequencyXPath() {
        return(getValueFromName(FREQUENCY_XPATH));
    }

    public String getPOSXPath() {
        return(getValueFromName(POS_XPATH));
    }

    public String getSubwordXPath() {
        return(getValueFromName(SUBWORD_XPATH));
    }

    public String getDialectXPath() {
        return(getValueFromName(DIALECT_XPATH));
    }

    public String getRegisterXPath() {
        return(getValueFromName(REGISTER_XPATH));
    }

    //unnecessary
    /*
    public String getBidirectionalXPath(){
        return(getValueFromName(BIDIRECTIONAL_XPATH));
    }
    */

    public String getGlossXPath(){
        return(getValueFromName(GLOSS_XPATH));
    }

    public String getGlossLangName() {
        String str = getValueFromName(GLOSS_LANGUAGE_NAME);
        if (str == null) {
            str = "";
        }
        return str;
    }

    public String getDictLangName() {
        String str = getValueFromName(DICTIONARY_LANGUAGE_NAME);
        if (str == null) {
            str = "";
        }
        return str;
    }

    public String getGlossLangIcon() {
        return(getValueFromName(GLOSS_LANGUAGE_ICON));
    }

    public String getDictLangIcon() {
        return(getValueFromName(DICTIONARY_LANGUAGE_ICON));
    }

    public String getValueFromName(String nodeName) {
        Element root = doc.getDocumentElement();
        NodeList nodes = root.getElementsByTagName(nodeName);
        if (nodes.getLength() < 1) {
            return null;
        }
        return nodes.item(0).getFirstChild().getNodeValue();
    }

    /****************************************/
    /********Link Information****************/
    /****************************************/

    /** How many types of links are there?
     *
     * @return The number of different link types
     */
    public int getNumLinks() {
        return links.size();
    }

    public String getLinkHeadwordXPath() {
        return(linkHeadwordXPath);
    }

    public String getLinkUniquePaddingXPath() {
        return(linkUniquePaddingXPath);
    }

    public String getLinkName(int i){
        return ((String) ((Object[]) links.elementAt(i))[0]);
    }

    public Color getLinkColor(int i){
        return ((Color) ((Object[]) links.elementAt(i))[1]);
    }

    public String getLinkXPath(int i){
        return ((String) ((Object[]) links.elementAt(i))[2]);
    }

    private void initLinks() throws Exception{
        links = new Vector();

        linkHeadwordXPath = getValueFromName(REF_HEADWORD_XPATH);
        // a default of current node if not set
        if (linkHeadwordXPath == null || linkHeadwordXPath.equals("")) {
            linkHeadwordXPath = ".";
        }
        linkUniquePaddingXPath = getValueFromName(REF_UNIQUIFIER_XPATH);

        Node linksNode = doc.getDocumentElement().getElementsByTagName(LINKS).item(0);
        if(linksNode == null) return; //no links in dictionary

        NodeList nodes = linksNode.getChildNodes();
        int len = nodes.getLength();
        for(int i=0; i<len; i++) {
            linksNode = nodes.item(i); //link node

            if(!linksNode.getNodeName().equals(LINK_NODE))
                continue;

            Object[] linkDefinition = new Object[NUM_LINK_PROPERTIES];
            NodeList linkProperties = linksNode.getChildNodes();
            int propLen = linkProperties.getLength();
            for(int j=0; j<propLen; j++) {
                Node property = linkProperties.item(j);

                if(property.getNodeName().equals(LINK_NAME)) {
                    linkDefinition[0] =
                        property.getFirstChild().getNodeValue();
                } else if(property.getNodeName().equals(LINK_COLOR)) {
                    linkDefinition[1] =
                        createColorFromNode(property);
                } else if(property.getNodeName().equals(LINK_XPATH)) {
                    linkDefinition[2] =
                        property.getFirstChild().getNodeValue();
                }
            }

            if(linkDefinition[0] == null ||
               linkDefinition[1] == null ||
               linkDefinition[2] == null)
                throw new Exception("Link not fully defined");

            links.addElement(linkDefinition);
        }
    }

    private static Color createColorFromNode(Node colorNode) throws Exception {
        NodeList list = colorNode.getChildNodes();

        int len = list.getLength();
        int r = -1, g = -1, b = -1;
        for (int i = 0; i < len; i++) {
            Node color = list.item(i);
            if (color.getNodeName().equals(LINK_COLOR_NAME)) {
                return(colorFromString(color.getFirstChild().getNodeValue()));
            } else if (color.getNodeName().equals(LINK_COLOR_R)) {
                //could switch on 7th char of node name, but not as flexible
                r = Integer.parseInt(color.getFirstChild().getNodeValue());
            } else if(color.getNodeName().equals(LINK_COLOR_G)) {
                g = Integer.parseInt(color.getFirstChild().getNodeValue());
            } else if(color.getNodeName().equals(LINK_COLOR_B)) {
                b = Integer.parseInt(color.getFirstChild().getNodeValue());
            }
        }

        if (r == -1 || g == -1 || b == -1) {
            throw new Exception("Link Color not fully defined");
        }

        return new Color(r, g, b);
    }

    private static Color colorFromString(String colorName) throws Exception{
        for (int i=0; i<COLOR_TABLE.length; i+=2)
            if(colorName.equalsIgnoreCase((String) COLOR_TABLE[i]))
                return((Color) COLOR_TABLE[i+1]);

        throw new Exception("Unknown Color name " + colorName);
    }

    /*****************************************/
    /*****XSL fields**************************/
    /*****************************************/

    public int getNumXslFiles(){
        if(xslData == null)
            initXsl();

        return(xslData.size());
    }

    public String getXslFilename(int i){
        if(xslData == null)
            initXsl();

        return ((String[]) xslData.elementAt(i))[0];
    }

    public String getXslShortname(int i){
        if(xslData == null)
            initXsl();

        return ((String[]) xslData.elementAt(i))[1];
    }

    public String getXslDescription(int i){
        if(xslData == null)
            initXsl();

        return ((String[]) xslData.elementAt(i))[2];
    }

    //this one isn't in the dtd...should it be?
    public String getXslXPath(int i){
        if(xslData == null)
            initXsl();
        return null;
    }

    private void initXsl() {
        xslData = new Vector();

        Node xslFilesNode =
            doc.getDocumentElement().getElementsByTagName(XSL_FILES).item(0);
        if(xslFilesNode == null) //maybe this is a little harsh...
            throw new RuntimeException("No Xsl Files (they are required by dtd)");

        NodeList nodes = xslFilesNode.getChildNodes();
        int len = nodes.getLength();
        for(int i=0; i<len; i++) {
            xslFilesNode = nodes.item(i); //xsl file node

            if(!xslFilesNode.getNodeName().equals(XSL_FILE))
                continue;

            String[] xslProperties = new String[NUM_XSL_PROPERTIES];
            NodeList xslNodeList = xslFilesNode.getChildNodes();
            int propLen = xslNodeList.getLength();
            for(int j=0; j<propLen; j++) {
                Node property = xslNodeList.item(j);

                if(property.getNodeName().equals(XSL_FILENAME))
                    xslProperties[0] =
                        property.getFirstChild().getNodeValue();
                else if(property.getNodeName().equals(XSL_SHORT))
                    xslProperties[1] =
                        property.getFirstChild().getNodeValue();
                else if(property.getNodeName().equals(XSL_DESC))
                    xslProperties[2] =
                        property.getFirstChild().getNodeValue();
            }

            if(xslProperties[0] == null ||
               xslProperties[1] == null ||
               xslProperties[2] == null)
                throw new RuntimeException("Xsl file not fully defined");

            xslData.addElement(xslProperties);
        }
    }


    public Hashtable getSearchRegexps() {
        Hashtable searches = new Hashtable();

        Node searchesNode =
         doc.getDocumentElement().getElementsByTagName(SEARCH_REGEXPS).item(0);
        if (searchesNode != null) {
            NodeList nodes = searchesNode.getChildNodes();
            int len = nodes.getLength();
            for (int i = 0; i < len; i++) {
                searchesNode = nodes.item(i); //search_regexp node, hopefully

                if (! searchesNode.getNodeName().equals(SEARCH_REGEXP)) {
                    // for some reason you get these. Whitespace?
                    continue;
                }
                String menuName = null;
                String regexp = null;
                NodeList searchNodeList = searchesNode.getChildNodes();
                int propLen = searchNodeList.getLength();
                for (int j = 0; j < propLen; j++) {
                    Node property = searchNodeList.item(j);
                    if (property.getNodeName().equals(MENU_NAME))
                        menuName = property.getFirstChild().getNodeValue();
                    else if(property.getNodeName().equals(REGEXP))
                        regexp = property.getFirstChild().getNodeValue();
                }

                if (menuName == null || regexp == null) {
                    throw new RuntimeException("Xsl file not fully defined");
                }
                searches.put(menuName, regexp);
            }
        }
        if (Dbg.SEARCH) {
            Dbg.print("Search regexps read in: " + searches);
        }
        return searches;
    }


    /****************************************/
    /****Custom Fields***********************/
    /****************************************/

    public int getNumCustomFields(){
        if(customData == null)
            initCustomFields();

        return customData.size();
    }

    public String getCustomFieldName(int i){
        if(customData == null)
            initCustomFields();
        return(((String[]) customData.elementAt(i))[0]);
    }

    public String getCustomFieldXPath(int i){
        if(customData == null)
            initCustomFields();
        return(((String[]) customData.elementAt(i))[1]);
    }

    public static final String CUSTOM_FIELDS = "CUSTOM_FIELDS";
    public static final String CUSTOM_FIELD = "CUSTOM_FIELD";
    public static final String FIELD_NAME = "FIELD_NAME";
    public static final String FIELD_XPATH = "FIELD_XPATH";
    public static final int NUM_CUSTOM_PROPERTIES = 2;

    private void initCustomFields() {
        customData = new Vector();

        Node customFieldNode =
            doc.getDocumentElement().getElementsByTagName(CUSTOM_FIELDS).item(0);
        if(customFieldNode == null) //maybe this is a little harsh...
            throw new RuntimeException("No Xsl Files (they are required by dtd)");

        NodeList nodes = customFieldNode.getChildNodes();
        int len = nodes.getLength();
        for(int i=0; i<len; i++) {
            customFieldNode = nodes.item(i); //custom node

            if(!customFieldNode.getNodeName().equals(CUSTOM_FIELD))
                continue;

            String[] customFields = new String[NUM_CUSTOM_PROPERTIES];
            NodeList customNodeList = customFieldNode.getChildNodes();
            int propLen = customNodeList.getLength();
            for(int j=0; j<propLen; j++) {
                Node property = customNodeList.item(j);

                if(property.getNodeName().equals(FIELD_NAME))
                    customFields[0] =
                        property.getFirstChild().getNodeValue();
                else if(property.getNodeName().equals(FIELD_XPATH))
                    customFields[1] =
                        property.getFirstChild().getNodeValue();
            }

            if(customFields[0] == null ||
               customFields[1] == null)
                throw new RuntimeException("Custom file not fully defined");

            customData.addElement(customFields);
        }
    }


    //  ****************************************
    //  *****Language Substitutions*************
    //  ****************************************

    /** Returns an array of regex to apply to gloss words.
     *  The returned value may be an empty array but is never null.
     */
    public Regex[] getGlossLanguageSubs() {
        if (GlossLanguageSubs == null) {
            GlossLanguageSubs = createLanguageSubs(GLOSS_LANGUAGE);
        }
        return GlossLanguageSubs;
    }

    /** Returns an array of regex to apply to headwords.
     *  The returned value may be an empty array but is never null.
     */
    public Regex[] getHeadwordLanguageSubs() {
        if (HeadwordLanguageSubs == null) {
            HeadwordLanguageSubs = createLanguageSubs(HEADWORD_LANGUAGE);
        }
        return HeadwordLanguageSubs;
    }

    private Regex[] createLanguageSubs(String language) {
        Node regexpNode = doc.getDocumentElement().getElementsByTagName(FUZZY_SPELLING).item(0);
        if (regexpNode == null) {
            // don't make it null - no need and this makes code easier
            return new Regex[0]; //no links in dictionary
        }

        Regex[] regexps = null;
        NodeList nodes = regexpNode.getChildNodes();
        int len = nodes.getLength();
        for (int i=0; i<len; i++) {
            regexpNode = nodes.item(i); //language node

            if (!regexpNode.getNodeName().equals(language))
                continue;

            NodeList substitutions = regexpNode.getChildNodes();
            int subLen = substitutions.getLength();

            int currSub = 0;
            regexps = new Regex[nodeListLength(substitutions, SUBSTITUTION)];
            for(int j=0; j<subLen; j++) {
                Node property = substitutions.item(j);

                if(!property.getNodeName().equals(SUBSTITUTION))
                    continue;

                NodeList subNodes = property.getChildNodes();
                int nodesLen = subNodes.getLength();
                String orig = null, replace = null;
                for (int k=0; k<nodesLen; k++) {
                    Node subsString = subNodes.item(k);

                    if(subsString.getNodeName().equals(SUBS_ORIGINAL))
                        orig = subsString.getFirstChild().getNodeValue();

                    if(subsString.getNodeName().equals(SUBS_REPLACEMENT))
                        replace = subsString.getFirstChild().getNodeValue();
                }

                if(orig == null ||
                   replace == null)
                    throw new RuntimeException("Fuzzy Regexp not fully defined");
                regexps[currSub++] = OroRegex.newSubRegex(orig, replace);
            }
        }
        if (regexps == null) {
            // it'll still be null if some kind of substitution is defined
            // but not the kind we were looking for
            // don't make it null - no need and this makes code easier
            regexps = new Regex[0]; //no links in dictionary
        }
        return regexps;
    }


    private int nodeListLength(NodeList list, String name) {
        int len = list.getLength();
        int realLen = 0;

        for(int i=0; i<len; i++)
            if(list.item(i).getNodeName().equals(name))
                realLen++;

        return realLen;
    }

    //   ****************************************
    //   ***** Stop Words, Stopchars ************
    //   ****************************************

    /** Return a vector of words to stop out of indexing in the glosses.
     *  This may be a zero length Vector, but will not be <code>null</code>.
     */
    public Vector getStopWords() {
        return getChildrenVector(STOPWORDS, STOPWORD);
    }

    /** Return a vector of characters to stop out of indexing in the glosses.
     *  What is returned is actually a Vector of <code>String</code>, but
     *  the rest of the code assumes that it's only a 1 character String, and
     *  anything beyond this is ignored.
     *  The full gloss string has these characters deleted like tr -d.
     *  This may be a zero length Vector, but will not be <code>null</code>.
     */
    public Vector getStopChars() {
        return getChildrenVector(STOPCHARS, STOPCHAR);
    }

    /** Return a vector of words that should be omitted from either the
     *  headwords list or the gloss list if they appear.
     *  This may be a zero length Vector, but will not be <code>null</code>.
     */
    public Vector getDictErrors() {
        return getChildrenVector(DICTERRORS, DICTERROR);
    }

    private Vector getChildrenVector(String tagname, String childtagname) {
        Vector children = new Vector();
        Node childrenNode =
            doc.getDocumentElement().getElementsByTagName(tagname).item(0);
        if (childrenNode == null)
            return children;

        NodeList nodes = childrenNode.getChildNodes();
        int len = nodes.getLength();
        for (int i=0; i<len; i++) {
            Node childNode = nodes.item(i); //child node

            if (!childNode.getNodeName().equals(childtagname)) {
                continue;
            }
            children.addElement(childNode.getFirstChild().getNodeValue());
        }
        return children;
    }


    //---- Functions to implement the ErrorHandler interface ---

    /** Returns a string of the location. */
    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();

    }

    public Document getDocument() { return doc; }

    public void setDocument(Document d) {
    	try {
    	doc = d;
    	Element root = doc.getDocumentElement();
        if (root.getTagName().equalsIgnoreCase("parseError")) {
          throw new Exception("XML Parsing Error of Specification File.");
        }
        if ( ! root.getTagName().equals(SPEC_ROOT)) {
            throw new Exception("Invalid Document; expecting " + SPEC_ROOT +
            ", found " + root.getTagName());
        }
        dictionaryEntryXPath =
                getElementFromName(DICTIONARY_ENTRY_XPATH);
        if ( ! okayRestrictedXPath()) {
            throw new Exception("Invalid " + DICTIONARY_ENTRY_XPATH
                                + ": " + dictionaryEntryXPath);
        }
        headwordXPath = getElementFromName(HEADWORD);
        uniquifierXPath =getValueFromName(UNIQUIFIER);
        senseXPath = getValueFromName(SENSE_XPATH);
        initLinks();
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

    /** Warning. */
    public void warning(SAXParseException ex) {

    	JOptionPane.showMessageDialog(null,
    			"[Warning] "+
                getLocationString(ex)+": "+
                ex.getMessage(),
    	    "XML Validation Warning",
    	    JOptionPane.WARNING_MESSAGE);

        if (Dbg.ERROR)
            System.err.println("[Warning] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
    }

    /** Error. */
    public void error(SAXParseException ex) throws SAXException {
    	JOptionPane.showMessageDialog(null,
    			"[Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage(),
    	    "XML Validation Error",
    	    JOptionPane.ERROR_MESSAGE);
        if (Dbg.ERROR)
            System.err.println("[Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
        //if (Dbg.PARSE) Dbg.linePrint(entryDepth, "FATAL ERROR: " + message);
        //if (Dbg.PARSE) Dbg.linePrint(entryDepth, "  at " + url + ": line " + line + " column " + column);
        try {
        	parser.reset();
        } catch (Exception e) {}
        throw new SAXException(ex);
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException {
    	JOptionPane.showMessageDialog(null,
    			"[Fatal Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage(),
    	    "XML Validation Fatal Error",
    	    JOptionPane.ERROR_MESSAGE);
    	if (Dbg.ERROR)
            System.err.println("[Fatal Error] "+
                               getLocationString(ex)+": "+
                               ex.getMessage());
    	try {
        	parser.reset();
        } catch (Exception e) {}
        throw new SAXException(ex);

    }



    /** This just tests whether getting info from the XML spec file works.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java Kirrkirr.DictionaryInfo xml_spec");
            System.exit(-1);
        }

        DictionaryInfo info = new DictionaryInfo(args[0]);

        System.out.println(info.getDictionaryEntryXPath());
        System.out.println(info.getDictionaryTag());
        System.out.println(info.getEntryTag());
        System.out.println(info.getHeadwordXPath());
        System.out.println(info.getUniquifierXPath());
        System.out.println(info.getImagesXPath());
        System.out.println(info.getAudioXPath());
        System.out.println(info.getDomainXPath());
        System.out.println(info.getFrequencyXPath());
        System.out.println(info.getSubwordXPath());
        System.out.println(info.getDialectXPath());
        System.out.println(info.getRegisterXPath());

        for (int i=0; i<info.getNumLinks(); i++) {
            System.out.println(info.getLinkName(i));
            System.out.println("\t"+info.getLinkColor(i));
            System.out.println("\t"+info.getLinkXPath(i));
        }

        for (int i=0; i<info.getNumXslFiles(); i++) {
            System.out.println(info.getXslFilename(i));
            System.out.println("\t"+info.getXslShortname(i));
            System.out.println("\t"+info.getXslDescription(i));
        }

        for (int i=0; i<info.getNumCustomFields(); i++) {
            System.out.println(info.getCustomFieldName(i));
            System.out.println("\t"+info.getCustomFieldXPath(i));
        }

        Regex[] reg = info.getGlossLanguageSubs();
        if(reg != null)
            for(int i=0; i<reg.length; i++)
                System.out.println(reg[i]);

        System.out.println("\n");

        reg = info.getHeadwordLanguageSubs();
        if (reg != null) {
            for(int i=0; i<reg.length; i++) {
                System.out.println(reg[i]);
            }
        }

        //added by kristen
        Vector stopwords=info.getStopWords();
        if (stopwords!=null) {
            for (int i=0;i<stopwords.size();i++){
                System.out.print(stopwords.elementAt(i)+"\t");
            }
        }
    }
}

