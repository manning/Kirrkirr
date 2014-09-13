package Kirrkirr.ui.panel.optionPanel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.FontProvider;

import javax.swing.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;

public class LookAndFeelOptionPanel extends KirrkirrOptionPanel implements ActionListener {

    private JRadioButton[] landfs;
    private ButtonGroup group;
    private String[] lf;
    private static Kirrkirr parent;
    private int defaultLfIndex = -1;
    private String selected;
    private int selectedIndex=-1;
    // private JComboBox langOption;
    // private JLabel langLabel;

    private static final String SC_DESC1="This_changes_the_way_the_buttons,_menus_and_things_look_in_Kirrkirr.";
    private static final String SC_DESC2="It's_hard_to_describe_how_each_Look_will_be_on_your_computer_until_you_try_it.";
    private static final String SC_DESC3="You_can_always_change_back_to_normal_by_clicking_on_the_\"Defaults\"_button.";
    private static final String SC_LOOK_AND_FEEL="Look_and_Feel";
    // private static final String SC_AENG = "Australian_English";
    // private static final String SC_WARL = "Warlpiri";
    // private static final String SC_PICK_A_LANGUAGE = "Pick_a_language";


    // cw 02: cosmetic changes
    public LookAndFeelOptionPanel(Kirrkirr kparent) {
        // Vector languages = new Vector(2);
        // languages.addElement(Helper.getTranslation(SC_AENG));
        // languages.addElement(Helper.getTranslation(SC_WARL));

        parent = kparent;
        setLayout(new FlowLayout(FlowLayout.LEFT));

        // todo: textarea/scrollpane not resizing properly...
        JTextArea desc=new JTextArea(Helper.getTranslation(SC_DESC1 + " " + SC_DESC2 + " " + SC_DESC3));
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
        desc.setForeground(Color.blue);
        desc.setPreferredSize(new Dimension(425, 130));
        desc.setMaximumSize(new Dimension(500, 130));
        JScrollPane scroller = new JScrollPane(desc);
        scroller.setMaximumSize(new Dimension(550, 140));
        add(scroller);
        add(Box.createHorizontalStrut(5));  // in addition to hgap (so
                                            // when wrap, e.g.)

        JPanel radioPanel = new JPanel();
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.Y_AXIS));

        // LOOK AND FEEL STUFF
        LookAndFeel defaultlf = UIManager.getLookAndFeel();
        String defaultLfName = defaultlf.getName();
        UIManager.LookAndFeelInfo[] lafi = UIManager.getInstalledLookAndFeels();
        int newLeng = lafi.length + 1;
        boolean classicMac = Helper.onClassicMac();
        if (classicMac) {
            newLeng++;
        }
        lf = new String[newLeng];
        for (int i = 0; i < lafi.length; i++) {
            lf[i] = lafi[i].getClassName();
        }
        lf[lafi.length] = defaultlf.getClass().getName();
        if (classicMac) {
            lf[lafi.length+1] = Kirrkirr.MACINTOSH_LOOK_AND_FEEL;
        }
        landfs=new JRadioButton[newLeng];
        group=new ButtonGroup();
        for (int i=0; i < lf.length; i++) {
            String thisLf;
            if (i < lafi.length) {
                thisLf = lafi[i].getName();
            } else if (i == lafi.length) {
                thisLf = "Platform default";
            } else {
                thisLf = "Extended Mac (Luca)";
            }
            landfs[i] = new JRadioButton(thisLf);
            if (thisLf.equals(defaultLfName)) {
                defaultLfIndex = i;
                landfs[defaultLfIndex].setSelected(true);
            }
            landfs[i].addActionListener(this);
            group.add(landfs[i]);
            radioPanel.add(landfs[i]);
        }

        add(radioPanel);
        setName(Helper.getTranslation(SC_LOOK_AND_FEEL));

        // ap 02: language picker for ui (fw and back buttons right now)
        // langLabel = new JLabel(Helper.getTranslation(SC_PICK_A_LANGUAGE));
        // langOption = new JComboBox(languages);
        // langOption.setMaximumSize(new Dimension(100, 30));
        // langOption.setSelectedIndex(0);
        /*
        langOption.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox) e.getSource();
                    int langId = cb.getSelectedIndex();
                    if (langId == 0) {
                        try {
               String fname = "lang_en_AU.properties";
               if (Dbg.PROGRESS) Dbg.print("Looking for file " + fname);
               Kirrkirr.lang = new
               PropertyResourceBundle(RelFile.makeURL(fname).openConnection().getInputStream());
                        } catch(IOException ioe) {}
                    } else {
                        try{
               String fname = "lang_wp_AU.properties";
               if (Dbg.PROGRESS) Dbg.print("Looking for file " + fname);
               Kirrkirr.lang = new PropertyResourceBundle(RelFile.makeURL(fname).openConnection().getInputStream());
                        } catch(IOException ioe) {}
                    }
                }
            });
        add(langLabel);
        add(langOption);
        */
    }


    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        selected = null;
        int index = -1;
        for (int i=0; i < landfs.length; i++) {
            if (src.equals(landfs[i])) {
                selected = lf[i];
                index = i;
            }
        }
        if (index == -1) return;
        selectedIndex = index;
    }

    public String getToolTip() {
        return Helper.getTranslation(SC_DESC1);
    }

    public void apply() {
        if (selected == null || selectedIndex == -1) return;
        if (Kirrkirr.profileManager!=null)
            Kirrkirr.profileManager.setUnsaved();
        try {
            UIManager.setLookAndFeel(selected);
        } catch(Exception ee) {
            ee.printStackTrace();
            landfs[selectedIndex].setEnabled(false);
            return;
        }
        SwingUtilities.updateComponentTreeUI(parent);
        SwingUtilities.updateComponentTreeUI(Kirrkirr.kirrkirrOptions);
        parent.kirrkirrOptions.invalidate();
        parent.kirrkirrOptions.repaint();
        parent.invalidate();
        parent.repaint();
        // SwingUtilities.updateComponentTreeUI(this.getParent());
        // this.getParent().invalidate();
        // this.getParent().repaint();
    }

    public void defaults() {
        selectedIndex = defaultLfIndex;
        selected = lf[defaultLfIndex];
        landfs[defaultLfIndex].setSelected(true);
    }

    /** Called by profile manager, in case the options
     *  panel wants to save any state. If so, it should also
     *  implement <code>loadState</code>.
     *  @see #loadState
     */
    public void saveState(ObjectOutputStream oos) throws IOException {
        if (selected == null || selectedIndex==-1) {
            String def = UIManager.getSystemLookAndFeelClassName();
            oos.writeObject(def);
        } else {
            oos.writeObject(selected);
        }
    }

    /** Called by profile manager, in case the options
     *  panel wants to save/load state. If so, it should also
     *  implement <code>saveState</code>.
     *  @see #saveState
     */
    public void loadState(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        selected = (String) ois.readObject();
        try {
            UIManager.setLookAndFeel(selected);
        } catch(Exception e) {
            if (Dbg.ERROR) {
                e.printStackTrace();
            }
        }
//          /*    if (Kirrkirr.window==null) return;
//        SwingUtilities.updateComponentTreeUI(parent);
//        SwingUtilities.updateComponentTreeUI(Kirrkirr.kirrkirrOptions);
//        parent.kirrkirrOptions.invalidate();
//        parent.kirrkirrOptions.repaint();
//        parent.invalidate();
//        parent.repaint(); */
//          SwingUtilities.updateComponentTreeUI(Kirrkirr.window);
//          SwingUtilities.updateComponentTreeUI(Kirrkirr.menubar);
//          SwingUtilities.updateComponentTreeUI(Kirrkirr.kirrkirrOptions);
//          Kirrkirr.kirrkirrOptions.validate();
//          Kirrkirr.kirrkirrOptions.repaint();
//          Kirrkirr.window.validate();
//          Kirrkirr.window.repaint();
//          Kirrkirr.kk.repaint();
    }

}


