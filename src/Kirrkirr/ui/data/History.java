package Kirrkirr.ui.data;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.KirrkirrButton;
// import Kirrkirr.ui.panel.FunPanel;
// import Kirrkirr.ui.panel.GraphPanel;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.RelFile;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.Serializable;

/**
 * The <code>History</code> object controls the history toolbar and the
 * History Menu.
 *
 * There are two lists, backwardList and forwardList. The ends of these
 * vectors (high index) are the ones you move to as move from current word.
 *
 * @version     1.0, 27/03/99
 * @author      Kevin Jansz
 * @author      Madhu (2000 added in methods used by the ProfileManager for saving the history)
 * @author      Lim Hong Lee  21/8/00
 * @author      Christopher Manning (2004: had to rewrite as had become disfunctional)
 * @see         Kirrkirr.ui.panel.KirrkirrPanel
 */
public class History extends JComponent implements ActionListener, Serializable
{
    //String constants
    private static final String SC_BACK = "Back";
    private static final String SC_BACK_DESC = "Go_backward_in_the_History";
    private static final String SC_FORWARD = "Forward";
    private static final String SC_FORWARD_DESC = "Go_forward_in_the_history";
    private static final String SC_HISTORY = "History";
    private static final String SC_CLEAR_HISTORY = "Clear_History";
    private static final String SC_HISTORY_DESC = "Look_at_the_history_of_your_browsing";

    /** Max words to display in forward or backward list */
    private static final int MAX_LIST = 15;

    private static Kirrkirr parent;

    /** A list of the uniqueKey's of words maintained in the history */
    private Vector historyList;
    /** The index in the historyList of the current word */
    private int currentEntryIndex = -1;

    //Swing variables
    /** the history toolbar (with buttons) on the kirrkirr toolbar */
    private JToolBar tb_history;
    private JButton b_back;
    private JButton b_forward;
    //buttons which activate the backlist/forelist menus to popup

    /** the history drop-down menu on the kirrkirr menu bar */
    private JMenu  m_history;
    //items for that menu
    private JMenuItem   mi_back, mi_forward, mi_clear;
    private static Icon bullet=RelFile.makeImageIcon("bullet.gif",false);
    private int menuOffset;


    /**
     * Constructs the History.
     * @param     parent   the Kirrkirr object
     */
    public History (Kirrkirr parent) {
        super();
        this.parent = parent;

        historyList= new Vector();

        //back button
        b_back = new KirrkirrButton(Helper.getTranslation(SC_BACK), "Back16.gif", this);
        // b_back.setPressedIcon(RelFile.makeImageIcon("leftDown.gif"));
        // b_back.setRolloverIcon(RelFile.makeImageIcon("leftRollover.gif"));
        b_back.setRolloverEnabled(true);
        b_back.setName(Helper.getTranslation(SC_BACK));
        b_back.setToolTipText(Helper.getTranslation(SC_BACK_DESC));
        b_back.setEnabled(false);

        b_forward = new KirrkirrButton(Helper.getTranslation(SC_FORWARD), "Forward16.gif", this);
        //b_forward.addMouseListener(new PopupListener(m_backlist));
        // b_forward.setPressedIcon(RelFile.makeImageIcon("rightDown.gif"));
        // b_forward.setRolloverIcon(RelFile.makeImageIcon("rightRollover.gif"));
        b_forward.setRolloverEnabled(true);
        b_forward.setName(Helper.getTranslation(SC_FORWARD));
        b_forward.setToolTipText(Helper.getTranslation(SC_FORWARD_DESC));
        b_forward.setEnabled(false);

        //for the main menu bar at the top of kirrkirr
        m_history = new JMenu(Helper.getTranslation(SC_HISTORY));
        //m_history.setDisplayedMnemonic('h');

        mi_back = new JMenuItem(Helper.getTranslation(SC_BACK));
        mi_back.setName(Helper.getTranslation(SC_BACK));
        mi_back.addActionListener(this);
        mi_back.setEnabled(false);
        m_history.add(mi_back);

        mi_forward = new JMenuItem(Helper.getTranslation(SC_FORWARD));
        mi_forward.setName(Helper.getTranslation(SC_FORWARD));
        mi_forward.addActionListener(this);
        mi_forward.setEnabled(false);
        m_history.add(mi_forward);

        mi_clear = new JMenuItem(Helper.getTranslation(SC_CLEAR_HISTORY));
        mi_clear.setName(Helper.getTranslation(SC_CLEAR_HISTORY));
        mi_clear.addActionListener(this);
        mi_clear.setEnabled(true);
        m_history.add(mi_clear);

        m_history.insertSeparator(m_history.getItemCount());
        menuOffset = m_history.getItemCount();

        tb_history = new JToolBar();
        /* The following lines are used, instead of
           tb_history.add(this);
           because for some reason under CDE/Motif L&F,
           the forelist button is pushed off the toolbar
           if we dont do it this way. (kp) */
        tb_history.addSeparator(new Dimension(4,3));
        tb_history.add(b_back);
        //      tb_history.add(b_backlist);
        tb_history.addSeparator(new Dimension(2,3));
        tb_history.add(b_forward);
        // kp: these color settings are necessary (otherwise it turns up
        // grey under CDE/Motif L&F)
        tb_history.setBackground(Kirrkirr.toolbarColor);

        // m_backlist.setMenuLocation(b_backlist.getX(), b_backlist.getY() + b_backlist.getHeight());
        tb_history.setFloatable(false);

        //System.out.println(b_back.getX() + " : " + b_back.getY() + " : " + b_back.getHeight());
    }

    /** Returns the String that is suitable rollover text for a tabbed
     *  pane containing this panel.
     *  @return the string to be used as rollover text
     */
    public String getTabRollover() {
        return Helper.getTranslation(SC_HISTORY_DESC);
    }

    /** Services all history actions.  The actions can be either from the
     *  menu bar (), the back and forward buttons (), the back and forward
     *  list buttons, or from the menus that they pop up
     */
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        int i;

        // Dbg.print("History: Source is " + source);
        if (source == mi_back || source == b_back) {
            i = currentEntryIndex - 1;
        } else if (source == mi_forward || source == b_forward) {
            i = currentEntryIndex + 1;
        } else if (source == mi_clear) {
            clearHistory();
            i = 0;
        } else {
            int sz = m_history.getItemCount();
            for (i = menuOffset; i < sz; i++) {
                // Dbg.print("History: trying menu item " + i);
                JMenuItem mi = m_history.getItem(i);
                if (source == mi) {
                    break;
                }
            }
            if (Dbg.ERROR && i >= sz) {
                Dbg.print("History: event not found. Meltdown");
            }
            i = i - menuOffset;
        }
        moveCurrentEntry(i);
        String uniqueKey = (String) historyList.elementAt(currentEntryIndex);
        if (Dbg.TWO) {
            Dbg.print("History: Current = " + uniqueKey);
        }

        // Lim Hong Lee:
        // using setCurrentWord() here instead of in goBackward() and goForward()
        // will cause it to be executed once thus eliminating the RuntimeException
        parent.setCurrentWord(uniqueKey, false, this, Kirrkirr.HISTORY, 0);
    }


    private void clearHistory()
    {
        for (int i = historyList.size() - 1; i >= 0; i--) {
            if (i != currentEntryIndex) {
                m_history.remove(menuOffset + i);
                historyList.removeElementAt(i);
            }
        }
        currentEntryIndex = -1;
    }


    /** Returns the history menu for the menubar at the
     *  top of the kirrkirr window.
     *  Called only by Kirrkirr.
     */
    public JMenu getHistoryMenu() {
        return m_history;
    }


    /** Returns the toolbar to be added to the main kirrkirr
     *  toolbar. Called only by Kirrkirr.
     */
    public JToolBar getHistoryToolBar() {
        return tb_history;
    }


    /** If the caller is someone other than History,
     *  updates the history lists and menus appropriately.
     *  Gloss words do not get added to the history,
     *  because all of their headword equivalents do. This
     *  mirrors the treatment of gloss words by the
     *  graph panel - there are no gloss words floating around,
     *  but all headword equivalents are.
     */
    public void setCurrentWord(String uniqueKey, boolean gloss,
                        final JComponent signaller, final int signallerType,
                        final int arg)
    {
        if (Dbg.TWO) Dbg.print("History: calling Set word " + uniqueKey);
        //headword equivalents of gloss word will get added instead
        if (gloss) return;
        if (signallerType == Kirrkirr.HISTORY) return;
        // Check for need to update backwardList and forwardList
        if (currentEntryIndex >= 0 &&
                historyList.elementAt(currentEntryIndex).equals(uniqueKey)) {
            return;
        }

        for (int j = historyList.size() - 1; j > currentEntryIndex; j--) {
            historyList.removeElementAt(j);
            m_history.remove(j + menuOffset);
        }
        while (historyList.size() >= MAX_LIST) {
            historyList.removeElementAt(0);
            m_history.remove(menuOffset);
            currentEntryIndex--;
        }
        historyList.addElement(uniqueKey);
        JMenuItem mi = new JMenuItem(Helper.uniqueKeyToPrintableString(uniqueKey));
        mi.addActionListener(this);
        mi.setEnabled(true);
        m_history.add(mi);
        moveCurrentEntry(historyList.size() - 1);

        if (Dbg.TWO) Dbg.print("Done set word in history");
    }


    private void moveCurrentEntry(int newIndex) {
      // Dbg.print("History: moving to index " + newIndex + " menuOffset=" +menuOffset);
      if (currentEntryIndex >= 0) {
          m_history.getItem(currentEntryIndex+menuOffset).setIcon(null);
      }
      JMenuItem mi = m_history.getItem(newIndex+menuOffset);
      mi.setHorizontalTextPosition(SwingConstants.LEFT);
      mi.setIcon(bullet);
      mi_back.setEnabled(newIndex != 0);
      b_back.setEnabled(newIndex != 0);
      int max = historyList.size() - 1;
      mi_forward.setEnabled(newIndex != max);
      b_forward.setEnabled(newIndex != max);
      currentEntryIndex = newIndex;
    }


    /* Implement this later to put the ListModels in a Dialog window on View command
       ??
       public void openOptionsDialog()
       {
       }
    */

} // end class History

