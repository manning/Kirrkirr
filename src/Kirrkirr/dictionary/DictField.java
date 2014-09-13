package Kirrkirr.dictionary;

import java.io.*;

import Kirrkirr.util.Helper;


/** DictField :
 *  basically just a struct for an element of data: tag, uniqueKey
 *  (this is mainly used for link data, but the value can also store
 *  a gloss or semantic field).  The class also contains constant
 *  definitions and methods for xml entities to save them
 *  being defined in multiple classes
 */

public class DictField implements Externalizable {

    public short tag;
    public String uniqueKey;        // usually word linked to (or gloss, etc.)

/*
    //these need to be translated
    public static final String descriptions[] = {
        "Same_meaning",
        "Same_as",
        "Alternate_Form",
        "Opposite",
        "See_also",
        "Preverb",
        "Sub-entry",
        "Main_Entry",
        "Collocation",

        "Gloss",
        "Semantic_Domain"//POS, register, dialect
    };

    public static final String DICTIONARY = "DICTIONARY";
    public static final String ENTRY = "ENTRY";
    public static final String WORD = "HW";
    public static final String HNUM = "HNUM";           //headword number (homophone distinction)
    public static final String EXACT = "HENTRY";
*/

    public static final String UNKNOWN = "?";
    public static final String UNKNOWNHNUM = "#";

    public static final int BLANK = 0;
    public static final int UNRESOLVED = -555; // hw exists but hnum unknown
    public static final int UNDEFINED = -777; // hw does not exist
    // public static final int BEAST = -666; //hahaha whoever wrote this is clever.

/*
    public static final short SYNONYM = 0;
    public static final short XROSSREF = 1;
    public static final short ALTERN = 2;
    public static final short ANTONYM = 3;
    public static final short CROSSREF = 4;
    public static final short PREVERB = 5;
    public static final short SUBENT = 6;
    public static final short MAINENT = 7;
    public static final short COLLOCATION = 8;
    public static final short GLOSS  = 9;
    public static final short DOMAIN  = 10;
    public static final short SOUND = 11;
    public static final short IMAGE = 12;
    public static final short HW = 13;
    public static final short POLY = 14;
    public static final short POS = 15;
    public static final short DIALECT = 16;
    public static final short DEF=17;

    public static final short TAGTYPES = 9;
                // to stop gloss, domain, etc. being treated as the other links
    public static final short ACTUAL_TYPES = 11;
                // for translating code <=> txt for searching purposes

    public static final String tags[] = {
        "SYN",
        "XME",
        "ALT",
        "ANT",
        "CF",
        "PVL",
        "SE",
        "CME",
        "COLLOC",

        "GL",
        "DOMAIN",

        "SOUND",
        "IMAGE",
        "ENTRY","ENTRY","ENTRY","DIALECTS","ENTRY"
        };

    public static final String items[] = {
        "SYNI",
        "XMEI",
        "ALTI",
        "ANTI",
        "CFI",
        "PVLI",
        "SEI",
        "CMEI",
        "COLLI",

        "GLI",
        "DMI",

        "SNDI",
        "IMGI",
        "HW","HW","POS","DLI","DEF"
    };

    private static final Color subentryColor = new Color(185, 170, 90);
*/
    /** Rationale for colors: avoid the colors of the nodes (yellow, green)
        Same Meaning/Same As/Alternate Form very similar - all shades of blue
        Opposite - red
        Collocation (not used) - green
        Sub/Main Entry - brown
        See Also - grey (neutral)
        Preverb - magenta
     */
/*
    public static final Color colors[] = {
        Color.blue,                      //same meaning
        new Color(31, 163, 255),          //same as - pastel blue
        new Color(64, 224, 255),          //alternate form - cyan-blue
        Color.red,                       //opposite
        Color.gray,                      //see also
        Color.magenta,                   //preverb
        subentryColor,                      //subentry
        subentryColor,                      //mainentry
        Color.green,                     //collocation

        Color.white,                    //gloss not used
        Color.white                     //domain not used
    };
*/
    // these don't really belong here, but it's a convenient place
    public static final String H_WORDS = "$headwords$";
    public static final String MAINWORDS = "$mainwords$";

    public static final int NUM_LINKS_DISPLAYED = 10;  // max links to show
    public static final int DEFAULT_GLOSS = 10; // allowed number of glosses to extract
    public static final int MAXOFITEM = 8;
    // maximum number of items of one crossreference type (syn, alt, etc.)

/*
    public static String getTag(final short tagId) {
        return(tags[tagId]);
    }

    public static String getItem(final short tagId) {
        return(items[tagId]);
    }

    public static Color getColor(final short tagId) {
        return(colors[tagId]);
    }

    public static String getDescription(final short tagId) {
        return(Helper.getTranslation(descriptions[tagId]));
    }

    public static short countTags() {
        return(TAGTYPES);
    }

    public static short getCodeForItem(final String tag) {
        for (short i = 0 ; i < ACTUAL_TYPES ; i++) {
            if ( items[i].equals(tag) ) {
               return(i);
            }
        }
        return((short)UNDEFINED);
    }
*/

    public DictField() {
        tag = 0;
        uniqueKey = null;
    }

    public DictField(short tag, String uniqueKey) {
        this.tag = tag;
        this.uniqueKey = uniqueKey;
    }

    public DictField(String uniqueKey) {
        this.tag = 0;
        this.uniqueKey = uniqueKey;
    }


    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof DictField) {
            DictField df = (DictField) obj;
            return equals(df);
        } else {
            return false;
        }
    }

    public boolean equals(DictField d) {
        if (d==null) {
            return false;
        }
        return this.uniqueKey.equals(d.uniqueKey);
    }

    public int hashCode() {
        return uniqueKey.hashCode();
    }


    public boolean hasExact() {
        return Helper.isResolved(uniqueKey);
    }

    public String toString() {
        return "tag: " + tag + " uniqueKey: " + uniqueKey;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeShort(tag);
        out.writeObject(uniqueKey);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        tag = in.readShort();
        uniqueKey = (String)in.readObject();
    }

    private static final long serialVersionUID = 3L;
}



