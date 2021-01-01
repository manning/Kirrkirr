package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.FunListener;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.ui.dialog.HtmlDialog;
import Kirrkirr.ui.panel.optionPanel.KirrkirrOptionPanel;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.dictionary.DictFields;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.event.*;

/** GraphPanel a KirrkirrPanel for the Graphical layout of the dictionary
 *  - using FunPanel.java
 */
public class OldGraphPanel extends KirrkirrPanel implements
                        ActionListener, ItemListener, FunListener {

    public OldFunPanel funPanel;

    // parent and window defined in KirrkirrPanel
    public JScrollPane funScrollPane;

    private JButton scramble;
    private JButton shake;
    private JButton clear;
    private JButton pick;
    private JCheckBox stop;
    private JCheckBox gloss;
    JCheckBox showlegend;     // used in FunOptionPanel

    /** String constants, needing internationalization */
    public static final String SC_GRAPH_NAME="Network";
    private static final String SC_STOP_MOVING="Stop_Moving";
    private static final String SC_SHOW_ENGLISH="Show_Gloss";
    private static final String SC_LEGEND ="Legend";
    private static final String SC_SCRAMBLE="Scramble";
    private static final String SC_SHAKE="Shake";
    private static final String SC_CLEAR="Clear";
    private static final String SC_RANDOM="Random_Pick";
    private static final String SC_RANDOM_SHORT="Random";
    private static final String SC_GRAPH_ROLLOVER="Graphical_network_layout_of_words";
    private static final String SC_NO_ENTRY="No_entry_for_clicked_word";
    private static final String SC_LIST_EMPTY="No_words_in_list_to_pick_from";
    private static final String SC_GP1="gp1"; // these two need MessageFormat
    private static final String SC_GP2="gp2";


    public OldGraphPanel(Kirrkirr kparent, JFrame window, boolean istoppane,
                         int kirrkirrSize) {
        super(kparent, window);
        this.window = window;
        setName(Helper.getTranslation(SC_GRAPH_NAME));

        addComponentListener(new ResizeListener());

        funPanel = new OldFunPanel(this, kirrkirrSize <= KirrkirrPanel.TINY);

        funScrollPane = new JScrollPane(funPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //funScrollPane = new JScrollPane(funPanel);

        //funScrollPane.setMinimumSize(new Dimension(300, 200));
        funScrollPane.setDoubleBuffered(true);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        int wid = Helper.onMacOSX() ? 650: (kirrkirrSize <= KirrkirrPanel.TINY) ? 600: 625;
        p.setMaximumSize(new Dimension(wid, 20));
        // the above line is vital to layout, though I don't understand it
        p.add(Box.createHorizontalStrut(1));

        stop = new JCheckBox(Helper.getTranslation(SC_STOP_MOVING));
        stop.addItemListener(this);
        stop.setForeground(Color.red.darker());
        p.add(stop);

        gloss = new JCheckBox(Helper.getTranslation(SC_SHOW_ENGLISH));
        gloss.addItemListener(this);
        gloss.setForeground(Color.red.darker());
        p.add(gloss);

        showlegend = new JCheckBox(Helper.getTranslation(SC_LEGEND));
        showlegend.addItemListener(this);
        showlegend.setForeground(Color.red.darker());
        showlegend.setSelected(true);
        p.add(showlegend);
        //      p.add(Box.createHorizontalGlue());

        p.add(Box.createHorizontalStrut(4));   // add a bit of space
        p.add(Box.createHorizontalGlue());
        scramble = new KirrkirrButton(SC_SCRAMBLE, this);
        // scramble.setBackground(Color.red);
        // scramble.setForeground(Color.white);
        p.add(scramble);
        p.add(Box.createHorizontalStrut(1));

        shake = new KirrkirrButton(SC_SHAKE, this);
        // shake.setBackground(Color.yellow);
        p.add(shake);
        p.add(Box.createHorizontalStrut(1));

        clear = new KirrkirrButton(SC_CLEAR, this);
        // clear.setBackground(Color.black);
        // clear.setForeground(Color.white);
        p.add(clear);
        p.add(Box.createHorizontalStrut(1));

        if (kirrkirrSize <= KirrkirrPanel.TINY || Helper.onMacOSX()) {
            pick = new KirrkirrButton(SC_RANDOM_SHORT, this);
        } else {
            pick = new KirrkirrButton(SC_RANDOM, this);
        }
        // pick.setBackground(Color.green);
        p.add(pick);
        p.add(Box.createHorizontalGlue());

        if ( ! Helper.onMacOSX()) {
            Color lightGray = Color.gray.brighter();
            scramble.setBackground(lightGray);
            shake.setBackground(lightGray);
            clear.setBackground(lightGray);
            pick.setBackground(lightGray);
        }

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentY(Component.CENTER_ALIGNMENT);
        add(funScrollPane);
        add(p);
    }


    @Override
    public String getTabRollover() {
        return Helper.getTranslation(SC_GRAPH_ROLLOVER);
    }


    /** This method is called when the Network Pane is selected.
     *  It fills in either the current gloss equivalents or
     *  current words from the history.
     */
    @Override
    public void tabSelected() {
        // String tailWord = parent.scrollPanel.getSelectedWord();
        // this tailWord could be either Gloss or Headword...
        //Vector backwardList = parent.history.getBackwardList();
        // boolean changed[] = parent.history.getChanged();
        // if something has changed, we have work to do; reset changed
        // if (changed[0] && toppane) {
        //     parent.history.setChanged(0,false);
        // } else if (changed[1] && ! toppane) {
        //     parent.history.setChanged(1,false);
        // } else {
        //    if (! parent.headwordsShowing()) {
        //        // Gloss glosses are showing!
        //        parent.setCurrentWord(tailWord, true, null, 0, 0);
        //    }
        //    return;
        // }
        // replay all the History into the GraphPanel
        //funPanel.clearFunPanel();
        //      for (int i=0; i<backwardList.size(); i++) {
            // cdm backwardlist is now a hnum padded String
        //    String entry = (String) backwardList.elementAt(i);
        //  setCurrentWord(entry, false, parent.history, 0, 0);
        //}
    }


    // cdm separate method to start spring alg so can delay
    @Override
    public void start () {
        funPanel.start();
    }

    @Override
    public void stop () {
        funPanel.stop();
    }

    @Override
    public KirrkirrOptionPanel getOptionPanel() {
        return new FunOptionPanel(funPanel, this);
    }

    /** Overrides KirrkirrPanel.  We use the arg to control sprouting.
     *  If called by ourselves with arg == 1: don't sprout
     *  otherwise do sprout
     * The internal boolean argument sendLinks indicates whether
     * sprouting should occur (double click or signal from outside)
     */
    @Override
    public void setCurrentWord(String uniqueKey, boolean gloss,
                               final JComponent signaller, final int signallerType,
                               final int arg)
    {
        long starttime;

        if (gloss)
            return;
        if (Dbg.TIMING) {
            Dbg.print("OldGraphPane.setCurrentWord(" + uniqueKey + ")");
            starttime= System.currentTimeMillis();
        }
        boolean sendLinks = ! (signallerType == parent.GRAPH && arg == 1);
        // System.err.println("signallerType is "+signallerType+" arg is "+ arg +
        //      " sendLinks is " + sendLinks);
        if (Dbg.TIMING) Dbg.print("XXXX before getDictEntryLinks");
        DictFields found = parent.cache.getDictEntryLinks(uniqueKey);
        if (Dbg.TIMING) Dbg.print("XXXX after getDictEntryLinks");
        if (found == null)
            return;    // should only happen if dict is defective ...
        if (sendLinks) funPanel.addFocusNode(uniqueKey, found);
        if (Dbg.TIMING) Dbg.print("XXXX after addFocusNode");
        funPanel.setSelected(uniqueKey);
        int neighbors = funPanel.numLinks(uniqueKey);
        int size = found.size();
        if (size >  neighbors) neighbors = size;
        //funPanel.printAllEdges();
        //compound sentence construction - use MessageFormat
        //http://java.sun.com/docs/books/tutorial/i18n/format/messageFormat.html
        // tell about update of the Fun panel in status bar
        if (Dbg.TIMING) Dbg.print("XXXX before message format");
        Object[] args = { Helper.getWord(uniqueKey),
                Integer.valueOf(neighbors)};
        String form;
        if (sendLinks) {
            form = SC_GP1;
        } else {
            form = SC_GP2;
        }
        String text = MessageFormat.format(Helper.getTranslation(form), args);
        parent.setStatusBar(text);
        if (Dbg.TIMING) {
            long endtime = System.currentTimeMillis();
            endtime = endtime - starttime;
            Dbg.print("GraphPanel.setCurrentWord() took " + endtime + "ms");
        }
    }


    /* Copies the selected word label to the system
     *  clipboard and returns the length of the selection. If no
     *  text is selected, does nothing and returns 0.
     *  Overrides KirrkirrPanel.
     *  At present this does a copy even if you ask for a cut.  Should beep?
     *  @param isCut true if this should be a cut operation
     *  @return how many characters were copy (0 if no selection)
     *  Currently disabled -- would prefer copy from Text pane
    public int copyText() {
        String selected = funPanel.getSelectedText();
        if (Dbg.CUTPASTE)
            if (selected != null)
                Dbg.print("GraphPanel cut/copy; selected is " +
                          selected.length() + " chars\n  " + selected);
            else
                Dbg.print("GraphPanel cut/copy; selected is null");
        if (selected != null) {
            parent.putStringInClipboard(selected);
            return(selected.length());
        } else {
            return(0);
        }
    }
    */


    // === FunListener interface methods ===

    @Override
    public void funGetLinks(String uniqueKey) {
        if (uniqueKey == null) {
            parent.setStatusBar(Helper.getTranslation(SC_NO_ENTRY));
            return;
        }
        parent.setCurrentWord(uniqueKey, false,
                        this, parent.GRAPH, 0);
    }

    @Override
    public void funFindWord(String clicked) {
        if (clicked == null) {
            parent.setStatusBar(Helper.getTranslation(SC_NO_ENTRY));
            return;
        }
        parent.setCurrentWord(clicked, false,
                        this, parent.GRAPH, 1);
    }

    @Override
    public void funSetRandom() {
        if (Dbg.TWO) Dbg.print("Setting random");
        int size =  parent.headwordsListSize();
        if (size == 0) {
            parent.setStatusBar(Helper.getTranslation(SC_LIST_EMPTY));
            return;
        }
        // in random, don't need to subtract one, since result < size
        int pick = (int)(Math.random() * size);
        String rand = parent.scrollPanel.headwordAt(pick);
        parent.setCurrentWord(rand,false, this, parent.GRAPH, 0);
    }


    void openGlossDialog (String uniqueKey) {
         HtmlDialog hnd = new HtmlDialog(parent, Kirrkirr.htmlFolder,
                                      uniqueKey);
    }


    public void actionPerformed(ActionEvent e) {
        Object obj = e.getSource();

        if (obj == scramble) {
            funPanel.scrambleShake(true);
        } else if (obj == shake ) {
            funPanel.scrambleShake(false);
        } else if (obj == clear ) {
            funPanel.clearFunPanel();
        } else if (obj == pick ) {
            funSetRandom();
        }
    }

    /** overides KirrkirrPanel
     */
    public void saveState(ObjectOutputStream oos) throws IOException {
        //funPanel.saveState(oos);
    }

    /** overides KirrkirrPanel
     *   @throws ClassNotFoundException
     *   @throws IOException
     */
    @Override
    public void loadState(ObjectInputStream ois) throws
                                IOException, ClassNotFoundException {
        //funPanel.loadState(ois);
    }


    public void itemStateChanged(ItemEvent e)
    {
        Object obj = e.getSource();

        if (obj == stop) {
            funPanel.stop = stop.isSelected();
        }
        else if (obj == gloss) {
            funPanel.gloss = gloss.isSelected();
        }
        else if (obj == showlegend) {
            funPanel.legend = showlegend.isSelected();
        }
    }


    static class ResizeListener extends ComponentAdapter {
        public void componentResized(ComponentEvent e) {
        }
    }

} //end class GraphPanel


final class FunOptionPanel extends KirrkirrOptionPanel
            implements Serializable,ChangeListener, ItemListener {

    private static final String SC_GRAPH_OPTIONS_TITLE="Network_constants";
    private static final String SC_WHAT_LINKS="Choose_what_links_to_see";
    private static final String SC_RANDOM_MOVEMENT="Random_movement";
    private static final String SC_SHOW_LEGEND="Show_legend";
    private static final String SC_OTHER_OPTIONS="Other_options";

    private static final String SC_STANDARD_REP1="Standard";
    private static final String SC_STANDARD_REP2="Repulsion";
    private static final String SC_EXTRA_REP1="Extra";
    private static final String SC_EXTRA_REP2="Repulsion";
    private static final String SC_RELAXED_SPR1="Relaxed";
    private static final String SC_RELAXED_SPR2="spring";
    private static final String SC_ANIM_SPEED1="Animation";
    private static final String SC_ANIM_SPEED2="speed";
    private static final String SC_EDGE_WIDTH1="Edge";
    private static final String SC_EDGE_WIDTH2="width";
    private static final String SC_MAX_FNODES1="Maximum";
    private static final String SC_MAX_FNODES2="focus_nodes";
    private static final String SC_DESC = "Adjust_network_parameters_and_visible_links";

    private final static int OPTIONS = 4;
    private final static int K2 = 5; //standard repulsion
    private final static int K3 = 4; //extra repulsion
    private final static int Luv = 3; //relaxed spring
    private final static int C = 0; //animation speed
    private final static int EW = 1; //edge width
    private final static int Q_SIZE = 2; //max focus nodes

    private OldFunPanel funPanel;
    private OldGraphPanel graphPanel;

    private JCheckBox[] links;

    // private JCheckBox stress;
    private JCheckBox random;
    private JCheckBox legend;

    private JPanel[] column = new JPanel[OPTIONS];
    private JSlider[] scroll = new JSlider[OPTIONS];
    private JLabel[] value = new JLabel[OPTIONS];
    private int[] defaultVals = new int[OPTIONS];

    private int[] newValsScroll = new int[OPTIONS];
    private boolean[] newValsLinks = new boolean[OPTIONS];

    private final Dimension minimumSize = new Dimension(580, 250);

    public Dimension getMinimumSize() {
        return minimumSize;
    }

    public Dimension getPreferredSize() {
        return minimumSize;
    }

    FunOptionPanel(OldFunPanel funPanel, OldGraphPanel graphPanel) {
        setName(Helper.getTranslation(OldGraphPanel.SC_GRAPH_NAME));
        this.funPanel = funPanel;
        this.graphPanel = graphPanel;
        JPanel scroll_p = new JPanel();
        scroll_p.setLayout(new BoxLayout(scroll_p, BoxLayout.X_AXIS));

        for (int i=0 ; i<OPTIONS ; i++) {
            column[i] = new JPanel();
            column[i].setLayout(new BoxLayout(column[i], BoxLayout.Y_AXIS));
            JLabel temp;

            // chris: I deleted the center alignment for labels, since
            // it wasn't working for sliders!
            switch (i) {
                case K2:
                    defaultVals[i] = Double.valueOf(funPanel.K2).intValue();
                    scroll[i] = new JSlider(JSlider.VERTICAL, 3, 500, defaultVals[i]);
                    temp=new JLabel(Helper.getTranslation(SC_STANDARD_REP1));
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    column[i].add(temp);
                    temp=new JLabel(Helper.getTranslation(SC_STANDARD_REP2));
                    column[i].add(temp);
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    break;
                case K3:
                    defaultVals[i] = Double.valueOf(funPanel.K3).intValue();
                    scroll[i] = new JSlider(JSlider.VERTICAL, 6, 400, defaultVals[i]);
                    temp=new JLabel(Helper.getTranslation(SC_EXTRA_REP1));
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    column[i].add(temp);
                    temp=new JLabel(Helper.getTranslation(SC_EXTRA_REP2));
                    column[i].add(temp);
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    break;
                case Luv:
                    defaultVals[i] = Double.valueOf(funPanel.Luv).intValue();
                    scroll[i] = new JSlider(JSlider.VERTICAL, 40, 200, defaultVals[i]);
                    temp=new JLabel(Helper.getTranslation(SC_RELAXED_SPR1));
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    column[i].add(temp);
                    temp=new JLabel(Helper.getTranslation(SC_RELAXED_SPR2));
                    column[i].add(temp);
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    break;
                case C:
                    defaultVals[i] = Double.valueOf(300.0 - 1/funPanel.C).intValue();
                    //"300 - " so that a big value gives a faster speed
                    scroll[i] = new JSlider(JSlider.VERTICAL, 1, 300, defaultVals[i]);
                    temp=new JLabel(Helper.getTranslation(SC_ANIM_SPEED1));
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    column[i].add(temp);
                    temp=new JLabel(Helper.getTranslation(SC_ANIM_SPEED2));
                    column[i].add(temp);
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    break;
                case EW:
                    defaultVals[i] = funPanel.EW;
                    scroll[i] = new JSlider(JSlider.VERTICAL, 1, 10, defaultVals[i]);
                    temp=new JLabel(Helper.getTranslation(SC_EDGE_WIDTH1));
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    column[i].add(temp);
                    temp=new JLabel(Helper.getTranslation(SC_EDGE_WIDTH2));
                    column[i].add(temp);
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    break;
                case Q_SIZE:
                    defaultVals[i] = funPanel.Q_SIZE;
                    scroll[i] = new JSlider(JSlider.VERTICAL, 2, 15, defaultVals[i]);
                    temp=new JLabel(Helper.getTranslation(SC_MAX_FNODES1));
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    column[i].add(temp);
                    temp=new JLabel(Helper.getTranslation(SC_MAX_FNODES2));
                    column[i].add(temp);
                    // temp.setAlignmentX(Component.CENTER_ALIGNMENT);
                    break;
            }
            value[i] = new JLabel(padString(defaultVals[i]));
            column[i].add(scroll[i]);
            column[i].add(value[i]);
            scroll[i].addChangeListener(this);
            // value[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            // scroll[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            scroll[i].setMaximumSize(new Dimension(100,100));
            scroll_p.add(column[i]);
            scroll_p.add(Box.createHorizontalGlue());
        }
        scroll_p.setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(SC_GRAPH_OPTIONS_TITLE)));
        // scroll_p.setMaximumSize(new Dimension(390,130));
        scroll_p.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel edge_p = new JPanel();
        edge_p.setLayout(new BoxLayout(edge_p,BoxLayout.Y_AXIS));

        int numLinks = (Kirrkirr.dictInfo != null) ? Kirrkirr.dictInfo.getNumLinks(): 0;
        links = new JCheckBox[numLinks];
        for (int i = 0 ; i < links.length ; i++) {
            links[i] = new JCheckBox(Helper.getTranslation(Kirrkirr.dictInfo.getLinkName(i)));
            links[i].setSelected(true);
            links[i].setForeground(Kirrkirr.dictInfo.getLinkColor(i));
            links[i].addItemListener(this);
            links[i].setMaximumSize(new Dimension(180,40));
            edge_p.add(links[i]);
        }

        edge_p.setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(SC_WHAT_LINKS)));
        edge_p.setMaximumSize(new Dimension(200,250));
        edge_p.setPreferredSize(new Dimension(200,250));

        JPanel oth_p = new JPanel();
        oth_p.setLayout(new BoxLayout(oth_p, BoxLayout.X_AXIS));

        random = new JCheckBox(Helper.getTranslation(SC_RANDOM_MOVEMENT));
        random.addItemListener(this);
        oth_p.add(random);
        legend = new JCheckBox(Helper.getTranslation(SC_SHOW_LEGEND), funPanel.legend);
        legend.addItemListener(this);
        oth_p.add(legend);
        oth_p.setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(SC_OTHER_OPTIONS)));
        oth_p.setAlignmentX(Component.CENTER_ALIGNMENT);
        oth_p.setAlignmentY(Component.CENTER_ALIGNMENT);

        JPanel tempPanel=new JPanel();
        tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.Y_AXIS));
        tempPanel.add(scroll_p);
        tempPanel.add(oth_p);
        // the size below is limiting the size of the right half of the (old) Network options panel
        tempPanel.setMaximumSize(new Dimension(300,350));
        tempPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(edge_p);
        add(tempPanel);
    }

    public String getToolTip() {
        return SC_DESC;
    }


    // KirrkirrOptionPanel interface
    public void defaults() {
        for (int i=0 ; i<OPTIONS ; i++) {
            scroll[i].setValue(defaultVals[i]);
        }
    }

    // KirrkirrOptionPanel interface
    public void apply() {
        /*        funPanel.K2 = (new Integer(scroll[K2].getValue())).doubleValue();
                  funPanel.K3 = (new Integer(scroll[K3].getValue())).doubleValue();*/
        funPanel.Luv = Integer.valueOf(scroll[Luv].getValue()).doubleValue();
        funPanel.C = 1.0/(301.0 - Integer.valueOf(scroll[C].getValue()).doubleValue());
        funPanel.EW = scroll[EW].getValue();
        funPanel.Q_SIZE = scroll[Q_SIZE].getValue();
    }

public void setup() {
    // fiddle the value of the legend variable in the GraphPanel, since
    // it needs to echo the one there
    legend.setSelected(funPanel.legend);
}


public void itemStateChanged(ItemEvent e)
{
    Object obj = e.getSource();

    for (short i = 0 ; i < links.length ; i++) {
        if (obj==links[i]) {
            if (Kirrkirr.profileManager!=null)
                Kirrkirr.profileManager.setUnsaved();
            if(! links[i].isSelected())
                funPanel.hideEdge.put(Short.valueOf(i), "");
            else
                funPanel.hideEdge.remove(Short.valueOf(i));
            return;
        }
    }

    // if (obj == stress) {
    //  funPanel.stress = stress.isSelected();
    // } else
    if (obj == random) {
        if (Kirrkirr.profileManager!=null)
            Kirrkirr.profileManager.setUnsaved();
        funPanel.random = random.isSelected();
    } else if (obj == legend) {
        //this extra line prevents it from setting unsaved
        //every time the options panel opens, due to the
        //setup() function
        if (funPanel.legend!=legend.isSelected() &&
            Kirrkirr.profileManager!=null)
            Kirrkirr.profileManager.setUnsaved();
        // keep the GraphPanel button synchronized
        funPanel.legend = legend.isSelected();
        graphPanel.showlegend.setSelected(funPanel.legend);
    }
}

public void stateChanged(ChangeEvent e)
{
    int chosen = 0;
    JSlider source = (JSlider) e.getSource();

    //if (!source.getValueIsAdjusting()) {
    for(int i=0; i<OPTIONS ; i++) {
        if (scroll[i].equals(source)){
            chosen = i;
            if (Kirrkirr.profileManager!=null)
                Kirrkirr.profileManager.setUnsaved();
        }
    }
    value[chosen].setText(padString(scroll[chosen].getValue()));
}


  private static String padString(int i) {
    if (i < 10) {
        return " " + String.valueOf(i) + " ";
    } else if (i < 100) {
        return " " + String.valueOf(i);
    } else {
        return String.valueOf(i);
    }
  }

public void saveState(ObjectOutputStream oos) throws IOException{
    for (int i=0; i<OPTIONS; i++) {
        oos.writeInt(scroll[i].getValue());
    }
    for (int i = 0 ; i<OPTIONS; i++) {
        oos.writeBoolean(links[i].isSelected());
    }
    // oos.writeBoolean(stress.isSelected());
    oos.writeBoolean(random.isSelected());
    oos.writeBoolean(legend.isSelected());

}

public void changeScroll() {
    for(int i=0;i<OPTIONS;i++){
        scroll[i].setValue(newValsScroll[i]);
    }
    apply();
}

public void changeLinks() {
    for(short i=0;i<OPTIONS;i++){
        links[i].setSelected(newValsLinks[i]);
    }
}

public void loadState(ObjectInputStream ois) throws IOException,
                                                 ClassNotFoundException {
    boolean trap;
    for(int i=0; i < OPTIONS; i++) {
        newValsScroll[i]=ois.readInt();
    }
    changeScroll();

    for (int i = 0 ; i < OPTIONS ; i++) {
        newValsLinks[i]=ois.readBoolean();
    }
    changeLinks();

    // trap = ois.readBoolean();
    // stress.setSelected(trap);

    trap = ois.readBoolean();
    random.setSelected(trap);

    trap = ois.readBoolean();
    legend.setSelected(trap);

    this.repaint();
}

} //FunOptionPanel

