package Kirrkirr.ui;

import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;
import Kirrkirr.util.FontProvider;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * The <code>KirrkirrButton</code> object is a very simple subclass of
 * a JButton.  I wrote it so that Kirrkirr would use more compact buttons
 * if the screen size is small (the default buttons have _huge_ insets).
 * But it is also convenient for simplifying slightly the tedious
 * initialization of buttons. Specifically:
 * (1) it does Helper.getTranslation of the button text
 * (2) it loads an Icon from a file in the images folder
 * (3) it adds an ActionListener
 * In 2007, if running on MacOSX, it was changed to not muck with button size.
 *
 * @version     1.0, 00/06/22
 * @author      Christopher Manning
 */
public class KirrkirrButton extends JButton {

    /** Make a button with the given text (internationalized) and
     *  icon, and an action listener
     *
     *  @param text The text for the button
     *  @param iconfile The icon filename for the button
     *  @param al ActionListener for button.
     */
    public KirrkirrButton(String text, String iconfile, ActionListener al) {
        super(Helper.getTranslation(text),
                RelFile.makeImageIcon(iconfile, false));
        setFont(FontProvider.REGULAR_INTERFACE_FONT);
        if (insetSize != LARGE) {
            setMargin((insetSize == SMALL) ? smallInsets: regularInsets);
        }
        addActionListener(al);
    }


    /** Make a button with the given text (internationalized) and
     *  an action listener and a tool tip (translated).
     *
     *  @param text The text for the button. Can be null.
     *  @param al ActionListener for button.
     */
    public KirrkirrButton(String text, ActionListener al) {
        super(Helper.getTranslation(text));
        setFont(FontProvider.REGULAR_INTERFACE_FONT);
        if (insetSize != LARGE) {
            setMargin((insetSize == SMALL) ? smallInsets: regularInsets);
        }
        addActionListener(al);
    }


    /** The documentation suggests users now should manipulate a Border
     *  not the Insets directly, but if you just want to inherit a standard
     *  JButton border, but muck with the insets, this seems the simplest
     *  way to do it -- and to work. Insets are (t,l,b,r)
     */
    private static final Insets smallInsets = new Insets(1,2,1,2);
    private static final Insets regularInsets = new Insets(1,3,1,3);

    public static final int SMALL = 1;
    public static final int MEDIUM = 2;
    public static final int LARGE = 3;

    public static void setInsetSize(int size) {
        insetSize = size;
    }

    private static int insetSize = MEDIUM;

}

