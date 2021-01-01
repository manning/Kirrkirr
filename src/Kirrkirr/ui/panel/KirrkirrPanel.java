package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.panel.optionPanel.KirrkirrOptionPanel;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** {@code KirrkirrPanel} is the abstract superclass for all of
 *  the panels in the tabbed panes. It provides a useful template
 *  for communication between the Kirrkirr class and the Panels.
 *  The only required functions are {@code setCurrentWord}, which
 *  tells the panel that the user has picked a new word, and
 *  {@code getTabRollover}, to give tooltip descriptions of the tabbed panes.
 */
public abstract class KirrkirrPanel extends JPanel {

    // For a 2016 MacbookPro 15.4" with screen size 2880 x 1800, it reports:
    // Screen size is java.awt.Dimension[width=1680,height=1050]
    // I'm not sure why....

    /** A constant to define tiny tabbed panes.
     *  A tabbed pane is tiny, small, normal or large.
     *  Search panels are tiny if the display is <= 500 pixels high.
     *  The canonical TINY display is 640x480.
     */
    public static final int TINY = 0;

    /** A constant to define small tabbed panes.
     *  Lower panes on a normal screen are small. Upper panes on a small screen
     *  (height <= 640 - see 
     *  Kirrkirr.main) are also small, otherwise they are normal.
     *  The canonical SMALL display is 800x600.
     */
    public static final int SMALL = 1;

    /** A constant to define normal-sized (ie non-small) tabbed panes.
     *  The typical NORMAL display is 1024x768 or 1280x1024.
     *  A bottom pane on a large monitor can be normal.
     */
    public static final int NORMAL = 2;

    /** A constant to define large screens on which you can use big
     *  stuff.  Let's say 1920x1080 or 2560x1440 or 2880x1800
     */
    public static final int LARGE = 3;

    /** A constant to define very large screens.
     *  Let's say that's 3500x2000 and up. Commonly it's 4K monitors at 3840x2160.
     */
    public static final int HUGE = 4;

    /** a pointer to Kirrkirr */
    public Kirrkirr parent;

    /** window needed for the opening of dialogs for KirrkirrPanel's */
    public JFrame window;

    /** Just sets the parent.
     *  @param kkparent The parent panel
     */
    public KirrkirrPanel(Kirrkirr kkparent) {
        this.parent = kkparent;
    }

    /** Just sets the main Kirrkirr JFrame (for opening dialogs).
     */
    public KirrkirrPanel(JFrame window) {
        this.window = window;
    }

    /** Sets the parent and main Kirrkirr JFrame (for opening dialogs).
     */
    public KirrkirrPanel(Kirrkirr kkparent, JFrame window) {
        this.window = window;
        this.parent = kkparent;
    }

    /** Returns the main Kirrkirr frame.
     */
    public JFrame getMainFrame() {
        return window;
    }


    /** String form of size */
    public static String panelSize(int size) {
        switch(size) {
            case 0: return "TINY";
            case 1: return "SMALL";
            case 2: return "NORMAL";
            case 3: return "LARGE";
            case 4: return "HUGE";
            default: return "UNDEFINED";
        }
    }


    /** Called by Kirrkirr to set the current word in this component.
     *  All KirrkirrPanels must implement this method. They will often want to
     *  do nothing if it was themselves that changed the word (tested by
     *  checking the signaller)
     */
    public abstract void setCurrentWord(/* padded */ String tailWord,
                                                     boolean gloss,
                                                     final JComponent signaller, final int signallerType,
                                                     final int arg);


    /** Called by Kirrkirr when the user clicks "switch to headword"
     *  or "switch to gloss." Most panels will probably implement this
     *  to disable or enable certain features.
     *  @param toGloss true when the scroll list was switched to gloss
     */
    public void scrollPanelChanged(boolean toGloss){}


    /** This lets the panel knows that the user has checked the "Number Multiples" option
     *  hence the program is in "polysemy/homophone" mode. This method is not made abstact
     *  because KirrkirrPanels can choose to ignore it. If they want to know about it, they just
     *  override this method.
     */
    public void polysemyUpdate() {
    }

    /** Indicates whether the Panel implements the makeSmall() and makeNormal() methods.
     *  By default, a panel doesn't, but it can override this method.
     */
    public boolean isResizable() {
        return(false);
    }

    /** Converts a panel from KirrkirrPanel.NORMAL to KirrkirrPanel.SMALL,
     *  if overridden. A panel only overrides this function if it returns
     *  true in isResizable().
     */
    public void makeSmall() {
    }

    /** Converts a panel from KirrkirrPanel.SMALL to KirrkirrPanel.NORMAL,
     *  if overridden. A panel only overrides this function if it returns
     *  true in isResizable().
     */
    public void makeNormal() {
    }

    /** Called by the profile manager. For many panels,
     *  this is a no-op, but any that wish to save/load state
     *  may override it and should also implement loadState.
     *  @see #loadState
     */
    public void saveState(ObjectOutputStream oos) throws IOException {
    }

    /** Called by the profile manager. For many panels,
     *  this is a no-op, but any that wish to save/load state
     *  may override it and should also implement saveState.
     *  @see #saveState
     */
    public void loadState(ObjectInputStream ois) throws IOException, ClassNotFoundException {
    }

    /** If a panel has an options panel, it returns
     *  it here. By default this returns null.
     *
     *  @return Returns the options panel or null if none.
     */
    public KirrkirrOptionPanel getOptionPanel() {
        return null;
    }

    /** Get the (text based) panel to copy selected text into the clipboard
     *  returns the number of characters copied. By default, does
     *  nothing and returns 0.
     *  @param isCut true if this should be a cut operation
     *  @return how many characters were copied (0 if no selection)
     */
    public int copyText(boolean isCut) {
        return(0);
    }

    /** Returns the String that is suitable rollover text for a tabbed
     *  pane containing this panel. Panels must implement this.
     *  The panel should return localized text.
     *  @return the string to be used as rollover text
     */
    public abstract String getTabRollover();

    /** Called when the current pane's tab is clicked on;
     *  used for semantic explorer, graph panel and quiz master
     *  panel. Used to have a param - toppane -
     *  If the pane called is the top pane in Kirrkirr, toppane
     *  is true, but is no longer there. Should we reimplement?
     */
    public void tabSelected() {}

    /** Called when the current pane's tab is deselected;
     *  used for semantic explorer and graph panel
     */
    public void tabDeselected() {}

    /** Called when pane needs to be restarted.  This only
     *  needs to be implemented by panels that use auxiliary threads.
     */
    public void start() {}

    /** Called when pane needs to be stopped.  This only
     *  needs to be implemented by panels that use auxiliary threads.
     */
    public void stop() {}

}

