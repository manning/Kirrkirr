//Graphical Semantic explorer panel
package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.ui.panel.domains.Domain;
import Kirrkirr.ui.panel.domains.DomainTree;
import Kirrkirr.ui.panel.domains.Position;
import Kirrkirr.ui.panel.domains.PositionTracker;
import Kirrkirr.ui.panel.optionPanel.DomainsOptionPanel;
import Kirrkirr.ui.panel.optionPanel.KirrkirrOptionPanel;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/** NewSemanticPanel is the KirrkirrPanel that contains the semantic domain
 *  display with ovals inside ovals. It adds some
 *  other controls for zooming and displaying the current vertical
 *  position in the tree.  Also implements the necessary methods to be a
 *  valid panel within Kirrkirr.  The main content of the semantic panel
 *  is under the DomainTree.
 *
 *  Here's the organization we have here:
 *    NewSemanticPanel (KirrkirrPanel, BoxLayout X_AXIS)
 *      SemSplitPane (JSplitPane, horizontal split (left and right)
 *        leftPanel (JPanel, BorderLayout)
 *          zOut (Zoom out KirrkirrButton) in NORTH
 *          tracker (PositionTracker, BoxLayout Y_AXIS) in CENTER
 *            hierarchyScroller (JScrollPane, 6 visible rows)
 *            strut
 *            glue
 *            sensePanel (JPanel, Boxlayout Y_AXIS)
 *              senseLabel (JLabel)
 *              senseScroller (JScrollPane)
 *                senseList(JList)
 *            strut
 *            glue
 *            childrenScroller
 *          zIn (Zoom in KirrkirrButton) in SOUTH
 *        dt (DomainTree (JComponent), painted by Kirrkirr code)
 *
 *  @author Steven Miller
 *  @author Ian Spiro
 */

public class NewSemanticPanel extends KirrkirrPanel implements
                                        ActionListener, ListSelectionListener {

    //string constants
    private static final String SC_DESC = "Explore_a_graphical_view_of_semantic_domains";
    private static final String SC_NAME = "Domains";
    private static final String SC_ZIN_TEXT = "Zoom_In";
    private static final String SC_ZOUT_TEXT = "Zoom_Out";
    private static final String SC_ZOUT_TIP = "Zoom_out_one_level";
    private static final String SC_ZIN_TIP = "Zoom_in_on_a_random_domain";
    private static final String SC_SENSE_LIST = "Senses";


    //the main part of the display
    private final DomainTree dt;  //the domain tree itself - null if none loaded

    //buttons to facilitate zooming in/out
    private JButton zIn;
    private final JButton zOut;

    //for gloss mode.  That is, for when in English -> L1 mode
    private String lastWord;  // = null;
    private boolean glossMode;  // = false;

    //for sense panel
    private DefaultListModel senseModel;
    private JList senseList;
    private Vector<Vector<String>> domainsEntries;


    public NewSemanticPanel(Kirrkirr kkparent, JFrame window, final int size,
                            String domainFile) {
        super(kkparent, window);
        // setLayout(null);
        setName(Helper.getTranslation(SC_NAME));

        // if ( ! isOpaque()) {
        //   Dbg.print("New semantic panel isn't opaque!!!");
        // }
        if (domainFile == null) {
            // do nothing
        } else if ("".equals(domainFile)) {
            domainFile = null;
        } else {
            domainFile = RelFile.dictionaryDir + RelFile.fileSeparator() +
                         domainFile;
        }
        dt = new DomainTree(this, domainFile);
        // for buttons
        JPanel leftPanel = new JPanel();
        int bdr = (size >= KirrkirrPanel.NORMAL) ? 3: 2;
        leftPanel.setLayout(new BorderLayout(bdr, bdr)); // space round components

        // Zoom In and Zoom Out buttons at top/bottom
        if (size > KirrkirrPanel.SMALL) {
            zIn = new KirrkirrButton(Helper.getTranslation(SC_ZIN_TEXT), "ZoomIn16.gif", this);
            zIn.setToolTipText(Helper.getTranslation(SC_ZIN_TIP));
            leftPanel.add(zIn, BorderLayout.SOUTH);
        }

        zOut = new KirrkirrButton(Helper.getTranslation(SC_ZOUT_TEXT), "ZoomOut16.gif", this);
        zOut.setToolTipText(Helper.getTranslation(SC_ZOUT_TIP));
        leftPanel.add(zOut, BorderLayout.NORTH);

        //create the sense navigation panel
        JPanel sensePanel = createSensePanel();
        domainsEntries = new Vector();

        // The ancestors and children part of the left column
        //vertical location in tree - glorified JList on left side of panel
        PositionTracker tracker = new PositionTracker(dt, sensePanel, size);
        leftPanel.add(tracker, BorderLayout.CENTER);
        // try to make bigger on macosx (seemed to have no effect!)
        // if (Helper.onMacOSX()) {
        //  leftPanel.setPreferredSize(new Dimension(180, 400));
        // } else {
        // CDM 2008: maybe this is just a bad idea?  Try deleting it.
        // leftPanel.setPreferredSize(new Dimension(125, 400));
        // }

        final JSplitPane semSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, dt);
        if (size > KirrkirrPanel.SMALL) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    float split = 0.2F;
                    semSplit.setDividerLocation(split);
                }
            });
        }

        // this makes it fit in and display okay, unlike BorderLayout
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        // add(semSplit, BorderLayout.CENTER);
        add(semSplit);
    }


    /** Returns true if there is no semantic domain information available.
     *  @return true if there is no semantic domain information available.
     */
    public boolean hasNoContent() {
        return dt.noContent;
    }


    private JPanel createSensePanel() {
        senseModel = new DefaultListModel();
        senseList = new JList(senseModel);
        senseList.clearSelection();
        senseList.setVisibleRowCount(5);
        senseList.setBackground(Color.lightGray);
        senseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        senseList.addListSelectionListener(this);
        senseList.setCellRenderer(new SenseRenderer());

        JScrollPane senseScroller = new JScrollPane(senseList);

        JLabel senseLabel = new JLabel(SC_SENSE_LIST, SwingConstants.CENTER);
        senseLabel.setLabelFor(senseScroller);
        JPanel sensePanel = new JPanel();
        sensePanel.setLayout(new BoxLayout(sensePanel, BoxLayout.Y_AXIS));
        sensePanel.add(Box.createVerticalGlue());
        sensePanel.add(senseLabel);
        sensePanel.add(senseScroller);
        sensePanel.add(Box.createVerticalGlue());
        sensePanel.setPreferredSize(new Dimension(100, 100));
        return sensePanel;
    }


    /** Implement listener for KKButtons in this pane.
     *  Fixed so that random child domains are chosen.
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if (src == zIn) {
            Domain currDomain;
            if (dt.getCurrentWord() != null) {
                currDomain = dt.getCurrentDomain();
            } else {
                currDomain = dt.getRoot();
            }
            if (currDomain != null) {
                Domain randChild = currDomain.randomChildDomainPri();
                if (randChild != null) {
                    dt.zoomTo(randChild, true);
                }
            }
        } else if (src == zOut) {
            dt.zoomOut();
        } else {
            System.err.println("NewSemanticPanel: Shouldn't happen: unhandled: " + e);
        }
    }


    //    public void setBounds(int x, int y, int width, int height) {
    //  super.setBounds(x,y,width,height);
        //always have the panel take up as much space as it can
        //      dt.setSize(mainPane.getWidth(), mainPane.getHeight());
    //    }


    //Fairly standard JPanel drawing, with the addition of a resize
    //to maximize use of the parent container.
    // public void paintComponent(Graphics g) {
    //  super.paintComponent(g);
    // }


    /** If notified by DomainTree that the selected word was changed, pass
     *  this information along to the rest of Kirrkirr.
     *
     *  @param newWord The uniqueKey for the word that the domain browser
     *      is moving too.
     */
    public void selectedWordChanged(String newWord) {
        updateSenseList(newWord);
        parent.setCurrentWord(newWord, false, this, Kirrkirr.EXPLORER, 0);
    }


    /** Probably should change this to just replace the ListModel with
     *  new one!
     *
     *  @param word Word to show the senses of.
     */
    private void updateSenseList(String word) {
        if (Dbg.DOMAINS) Dbg.print("updateSenseList for " + word);
        domainsEntries = parent.cache.getAllDomains(word);
        if (Dbg.DOMAINS) Dbg.print("domainsEntries is" + domainsEntries);

        //update the senseList
        senseList.clearSelection();
        senseModel.removeAllElements();
        // does nothing if null
        if (domainsEntries != null) {
            for (int i = 0, domainsSize = domainsEntries.size(); i < domainsSize; i++) {
                Domain curSenseDom = dt.getDomain(word, (Vector) domainsEntries.elementAt(i), false);
                if (curSenseDom != dt.getRoot()) {
                    senseModel.addElement(new Sense(curSenseDom, i+1));
                } else {
                    if (Dbg.DOMAINS) {
                        Dbg.print("Error? curSenseDom is " + curSenseDom);
                        Vector fields = (Vector) domainsEntries.elementAt(i);
                        for (int n = 0; n < fields.size(); n++) {
                            Dbg.print((String) fields.elementAt(n));
                        }
                    }
                }
            }
        }
        //we don't want notifications from list (it is passive, not active
        //during word changes), so remove then re-add self as listener
        senseList.removeListSelectionListener(this);
        senseList.setSelectedIndex(0);
        senseList.addListSelectionListener(this);
    }


    public void valueChanged(ListSelectionEvent e) {
        int sense = senseList.getSelectedIndex();
        if (sense == -1) return;
        Vector domains = (Vector) domainsEntries.elementAt(sense);
        if (domains != null) { //otherwise should display error
            String curWord = dt.getCurrentWord();
            if (curWord !=null) {
                dt.setCurrentWord(curWord, domains);
            }
        }
    }


    //necessary for filling in KirrkirrPanel interface methods
    public boolean isResizable() {
        return true;
    }

    // Maybe: fill in method copying text to clipboard
    // Not really necessary if only thing we can select is the current selected word; we get that anyway
    // public int copyText(boolean isCut) {
    //    return 0;
    // return dt.getCurrentWord()
    // }


    /** Called by Kirrkirr to set the current word in this component.
     *  All KirrkirrPanels must implement this method. They will often want to
     *  do nothing if it was themselves that changed the word (tested by
     *  checking the signaller)
     */
    public void setCurrentWord(String uniqueKey,
                               boolean gloss,
                               final JComponent signaller, final int signallerType,
                               final int arg) {
        //check 2 things:
        //    -we didn't generate this signal
        //    -not a gloss coming in
        //if both satisfied, continue
        if (signaller != this) {
            if (! glossMode || arg == 1) { //if last of gloss group, (or not in gloss
                                         //mode), set current word
                if (glossMode && gloss) uniqueKey = lastWord;  //set last valid headword
                updateSenseList(uniqueKey);
                if (Dbg.DOMAINS) {
                    Dbg.print("New semantic panel.setCurrentWord: " + uniqueKey);
                }
                dt.setCurrentWord(uniqueKey, parent.cache.getDomainsEntry(uniqueKey));
            } else if (arg == 0) // && glossMode (implied by above test)
                lastWord = uniqueKey; //remember so that when actual
                                     //gloss comes through we can set
                                     //to last headword
        }
    }


    public void setSense(Domain word) {
        //try matching against various senses
        for (int i = 0, sz = senseModel.size(); i < sz; i++) {
            if (word == ((Position) senseModel.elementAt(i)).getDomain()) {
                //remove and re-add so we don't get notified of change
                senseList.removeListSelectionListener(this);
                senseList.setSelectedIndex(i);
                senseList.addListSelectionListener(this);
                break;
            }
        }
    }

    /** Called by Kirrkirr when the user clicks "switch to headword"
     *  or "switch to gloss." Most panels will probably implement this
     *  to disable or enable certain features.
     *
     *  @param toGloss true when the scroll list was switched to gloss
     */
    public void scrollPanelChanged(boolean toGloss) {
        glossMode = toGloss;
    }



    /** Returns the String that is suitable rollover text for a tabbed
     *  pane containing this panel. Panels must implement this.
     *  The panel should return localized text.
     *
     *  @return The string to be used as rollover text
     */
    public String getTabRollover() {
        return Helper.getTranslation(SC_DESC);
    }


    /** When we are selected, we should start up the appropriate
     *  background threads (painter and animation).
     */
    public void tabSelected() {
        dt.start();
    }


    /** When the domain panel is deselected in the tabbed view, we should
     *  kill off any background threads that are still running.
     */
    public void tabDeselected() {
        dt.halt();
    }


    /** When Kirrkirr is activated/deactivated (due to window
     * restoration/minimization), we start/stop our threads, respectively.
     */
    public void start() {
        dt.start();
    }


    public void stop() {
        dt.halt();
    }


    public int getMaxChildren() {
        return dt.getMaxChildren();
    }


    public void setMaxChildren(int newMax) {
        dt.setMaxChildren(newMax);
    }


    public boolean getLimitChildren() {
        return dt.childrenDrawingIsLimited();
    }


    public void setLimitChildren(boolean limit) {
        dt.setChildrenDrawingIsLimited(limit);
    }


    public KirrkirrOptionPanel getOptionPanel() {
        return new DomainsOptionPanel(this);
    }

    public void showPics(boolean show, boolean showOnly) {
        dt.showPics(show, showOnly);
    }


    static class Sense extends Position {

        private static final String SC_SENSE = "Sense ";

        public Sense(Domain d, int num) {
            super(d, Helper.getTranslation(SC_SENSE) + ' ' + num);
        }

    } // end class Sense


    //For the JList - tells the list just to ask the item (which in this
    //case is the Sense object) to render itself. Since the Sense is
    //set up as a JLabel with the color and text of its corresponding
    //domain,the position then displays in the list consistent with the domain.

    static class SenseRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            return (Component) value;
        }

    } // end class SenseRender

}

