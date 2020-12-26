package Kirrkirr.util;

import java.util.Properties;
import java.awt.*;


/** This class provides fonts that the rest of Kirrkirr uses. */
public class FontProvider {

    private FontProvider() {} // not an instantiable class

    private static final String SANS_SERIF = "SansSerif";  // These only appear in JDK awt.Font in 1.6 !
    private static final String SERIF = "Serif";
    private static final String DIALOG = "Dialog";

    private static final int SMALL_TEXT_SIZE = 10;
    private static final int WORD_LIST_FONT_SIZE = 12;
    private static final int REGULAR_TEXT_SIZE = 12;
    private static final int LARGE_TEXT_SIZE = 14;
    private static final int VERY_LARGE_TEXT_SIZE = 16;
    private static final int HUGE_TEXT_SIZE = 18;
    private static final int GIGANTIC_TEXT_SIZE = 24;

    public static Font SMALL_WORD_FONT;
    public static Font WORD_LIST_FONT;
    public static Font WORD_LIST_SUBWORD_FONT;
    public static Font LARGE_WORD_FONT;
    public static Font LARGE_WORD_SUBWORD_FONT;
    public static Font PROMINENT_LARGE_WORD_FONT;
    public static Font PROMINENT_VERY_LARGE_WORD_FONT;
    public static Font HUGE_WORD_FONT;
    public static Font PROMINENT_HUGE_WORD_FONT;
    public static Font GIGANTIC_WORD_FONT;
    public static Color WORD_LIST_COLOR;

    public static Font GLOSS_LIST_FONT;
    public static Font GLOSS_LIST_SUBWORD_FONT;
    public static Color GLOSS_LIST_COLOR;
    public static Color GLOSS_LIST_LIGHTER_COLOR;

    public static Font SMALL_TEXT_FONT;
    public static Font TEXT_FONT;
    public static Font VERY_LARGE_TEXT_FONT;
    public static Font VERY_LARGE_ITALIC_TEXT_FONT;

    public static Font REGULAR_INTERFACE_FONT;
    public static Font PROMINENT_INTERFACE_FONT;
    public static Font PROMINENT_LARGE_INTERFACE_FONT;
    public static Font PROMINENT_VERY_LARGE_INTERFACE_FONT;

    public static Font HTML_FONT;

    public static void initializeFonts(Properties pr) {

        String str = pr.getProperty("dictionary.wordFont");
        if (isEmpty(str)) {
            str = pr.getProperty("kirrkirr.wordFont");
            if (isEmpty(str)) {
               str = SANS_SERIF;
            }
        }
        if (Dbg.FONTS && ! str.equals(SANS_SERIF)) {
            Dbg.print("Using user-defined kirrkirr.wordFont: " + str);
        }
        WORD_LIST_FONT = new Font(str, Font.PLAIN, WORD_LIST_FONT_SIZE);
        WORD_LIST_SUBWORD_FONT = new Font(str, Font.ITALIC, WORD_LIST_FONT_SIZE);
        SMALL_WORD_FONT = new Font(str, Font.PLAIN, SMALL_TEXT_SIZE);
        LARGE_WORD_FONT = new Font(str, Font.PLAIN, LARGE_TEXT_SIZE);
        LARGE_WORD_SUBWORD_FONT = new Font(str, Font.ITALIC, LARGE_TEXT_SIZE);
        PROMINENT_LARGE_WORD_FONT = new Font(str, Font.BOLD, LARGE_TEXT_SIZE);
        PROMINENT_VERY_LARGE_WORD_FONT = new Font(str, Font.BOLD, VERY_LARGE_TEXT_SIZE);
        HUGE_WORD_FONT = new Font(str, Font.PLAIN, HUGE_TEXT_SIZE);
        PROMINENT_HUGE_WORD_FONT = new Font(str, Font.BOLD, HUGE_TEXT_SIZE);
        GIGANTIC_WORD_FONT = new Font(str, Font.PLAIN, GIGANTIC_TEXT_SIZE);

        WORD_LIST_COLOR = Color.black;
        String col = pr.getProperty("dictionary.wordColor");
        if ( ! isEmpty(col)) {
            try {
                WORD_LIST_COLOR = Color.decode(col);
            } catch (NumberFormatException nfe) {
                // don't worry about it
            }
        }

        String gStr = pr.getProperty("dictionary.glossFont");
        if (isEmpty(gStr)) {
            gStr = str;
        }
        GLOSS_LIST_FONT = new Font(gStr, Font.PLAIN, WORD_LIST_FONT_SIZE);
        GLOSS_LIST_SUBWORD_FONT = new Font(gStr, Font.ITALIC, LARGE_TEXT_SIZE);
        GLOSS_LIST_COLOR = Color.black;
        col = pr.getProperty("dictionary.glossColor");
        if ( ! isEmpty(col)) {
            try {
                GLOSS_LIST_COLOR = Color.decode(col);
            } catch (NumberFormatException nfe) {
                // don't worry
            }
        }
        GLOSS_LIST_LIGHTER_COLOR = GLOSS_LIST_COLOR.brighter();

        str = pr.getProperty("dictionary.textFont");
        if (isEmpty(str)) {
          str = pr.getProperty("kirrkirr.textFont");
            if (isEmpty(str)) {
                str = SERIF;
            }
        }
        SMALL_TEXT_FONT = new Font(str, Font.PLAIN, SMALL_TEXT_SIZE);
        TEXT_FONT = new Font(str, Font.PLAIN, REGULAR_TEXT_SIZE);
        VERY_LARGE_TEXT_FONT = new Font(str, Font.PLAIN, VERY_LARGE_TEXT_SIZE);
        VERY_LARGE_ITALIC_TEXT_FONT = new Font(str, Font.ITALIC, VERY_LARGE_TEXT_SIZE);

        String str2 = pr.getProperty("dictionary.defaultHtmlFont");
        if (isEmpty(str2)) {
            str2 = str;
        }
        // Doing this would only be effective if we also redefined H1, H2, etc.'s size in CSS.
        // String str3 = pr.getProperty("kirrkirr.defaultHtmlSize");
        // int sz = REGULAR_TEXT_SIZE;
        // try {
        //    sz = Integer.parseInt(str3);
        // } catch (NumberFormatException nfe) {
        //    if (Dbg.ERROR) {
        //        nfe.printStackTrace();
        //     }
        // }
        HTML_FONT = new Font(str2, Font.PLAIN, REGULAR_TEXT_SIZE);

        str = pr.getProperty("dictionary.interfaceFont");
        if (isEmpty(str)) {
            str = pr.getProperty("kirrkirr.interfaceFont");
            if (isEmpty(str)) {
                str = DIALOG;
            }
        }
        REGULAR_INTERFACE_FONT = new Font(str, Font.PLAIN, REGULAR_TEXT_SIZE);
        PROMINENT_INTERFACE_FONT = new Font(str, Font.BOLD, REGULAR_TEXT_SIZE);
        PROMINENT_LARGE_INTERFACE_FONT = new Font(str, Font.BOLD, LARGE_TEXT_SIZE);
        PROMINENT_VERY_LARGE_INTERFACE_FONT = new Font(str, Font.BOLD, VERY_LARGE_TEXT_SIZE);
    }


    private static boolean isEmpty(String prop) {
        return prop == null || prop.isEmpty();
    }

}

