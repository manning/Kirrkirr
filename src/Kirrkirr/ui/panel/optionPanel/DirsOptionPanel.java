package Kirrkirr.ui.panel.optionPanel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Formerly TempDirOptionPanel.  cw 2002: added ability to change default
 * dictionary (and some cosmetic changes).  right now just records the
 * dictionary to start with next time the program is run.
 */
public class DirsOptionPanel extends KirrkirrOptionPanel implements
                                                        ActionListener {

    private static final String SC_DIRECTORIES = "Configure";
    private static final String SC_BROWSE = "Browse";
    private static final String SC_DESC = "Change_the_configuration_that_Kirrkirr_uses";
    private static final String SC_OLD_DOMAINS = "Old_(tree_view)_domain_display_(after_restart)";
    private static final String SC_OLD_NETWORK = "Old_network_display_(after_restart)";
    private static final String SC_LANG_CODE = "Language_Code";
    private static final String SC_COUNTRY_CODE = "Country_Code";
    private static final String SC_ONE_RHS_PANE = "One_explorer_pane_on_the_right_hand_side_(after_restart)";
    private static final String SC_DIVIDER_LOC = "Have_one_of_the_two_panes_initially_minimized_(after_restart)";

    public static final String PROP_OLDNETWORK = "kirrkirr.oldNetwork";
    public static final String PROP_OLDDOMAINS = "kirrkirr.oldDomains";
    public static final String PROP_LANG_CODE = "kirrkirr.langCode";
    public static final String PROP_COUNTRY_CODE = "kirrkirr.countryCode";
    public static final String PROP_ONERHSPANE = "kirrkirr.oneRHSPane";
    public static final String PROP_SINGLEPANEL_STARTUP = "kirrkirr.singlePanelStartup";

    // put in RelFile, as w/ SELECT_TEMP_DIR?
    private static final String SC_SELECT_DICT_DIR = "Select_Default_Dictionary_Directory_(requires_restarting_Kirrkirr)";
    // right now change will only really take effect next time Kirrkirr is
    // started (could be extended to be more dynamic)

    private final JTextField liveDictField;
    private final JTextField liveTempDirField;
    private final JTextField langField;
    private final JTextField countryField;

    private final JCheckBox oldNetwork;
    private final JCheckBox oldDomains;
    private final JCheckBox oneRHS;
    private final JCheckBox panelMinimized;

    public DirsOptionPanel() {
        setName(Helper.getTranslation(SC_DIRECTORIES));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //////////// Dictionary Directory /////////////
        add(Box.createVerticalStrut(18));
        JLabel dictLabel = new JLabel(Helper.getTranslation(SC_SELECT_DICT_DIR));
        // dictLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(dictLabel);
        add(Box.createVerticalStrut(8));

        JPanel dictPanel = new JPanel(); // FlowLayout by default
        ((FlowLayout)dictPanel.getLayout()).setAlignment(FlowLayout.LEFT);
        dictPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        liveDictField = new JTextField(RelFile.dictionaryDir, 20);
        liveDictField.setMaximumSize(new Dimension(500, 16));
        liveDictField.setEditable(false);
        dictPanel.add(liveDictField);
        dictPanel.add(Box.createHorizontalStrut(2));  // (plus hgap, for when wrapping, e.g.)
        dictPanel.add(new KirrkirrButton(SC_BROWSE, new PickDictDir()));
        add(dictPanel);
        //add(Box.createGlue());

        //////////// Temp Directory /////////////
        add(Box.createVerticalStrut(10));
        JLabel tempLabel = new JLabel(Helper.getTranslation(RelFile.SELECT_TEMP_DIR));
        tempLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(tempLabel);
        add(Box.createVerticalStrut(6));

        JPanel tempDirPanel = new JPanel(); // FlowLayout by default
        ((FlowLayout)tempDirPanel.getLayout()).setAlignment(FlowLayout.LEFT);
        tempDirPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        liveTempDirField = new JTextField(RelFile.WRITE_DIRECTORY, 20);
        liveTempDirField.setMaximumSize(new Dimension(500, 16));
        liveTempDirField.setEditable(false);
        tempDirPanel.add(liveTempDirField);
        tempDirPanel.add(Box.createHorizontalStrut(2));  // (plus hgap, for when wrapping, e.g.)
        tempDirPanel.add(new KirrkirrButton(SC_BROWSE, this));
        add(tempDirPanel);
        add(Box.createVerticalStrut(10));

        JPanel langPanel = new JPanel(); // FlowLayout by default
        ((FlowLayout)langPanel.getLayout()).setAlignment(FlowLayout.LEFT);
        langPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel langLabel = new JLabel(Helper.getTranslation(SC_LANG_CODE));
        langLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        langPanel.add(langLabel);
        langPanel.add(Box.createHorizontalStrut(2));

        langField = new JTextField(Kirrkirr.langCode, 3);
        langField.setMaximumSize(new Dimension(50, 16));
        langPanel.add(langField);
        langPanel.add(Box.createHorizontalStrut(10));  // (plus hgap, for when wrapping, e.g.)

        JLabel countryLabel = new JLabel(Helper.getTranslation(SC_COUNTRY_CODE));
        countryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        langPanel.add(countryLabel);
        langPanel.add(Box.createHorizontalStrut(2));

        countryField = new JTextField(Kirrkirr.countryCode, 3);
        countryField.setMaximumSize(new Dimension(50, 16));
        langPanel.add(countryField);
        langPanel.add(Box.createHorizontalStrut(2));  // (plus hgap, for when wrapping, e.g.)

        add(langPanel);
        add(Box.createVerticalStrut(10));

        oldNetwork = new JCheckBox(Helper.getTranslation(SC_OLD_NETWORK), false);
        oldNetwork.addActionListener(this);
        add(oldNetwork);
        // JPanel oldNetworkPanel = new JPanel();
        // oldNetworkPanel.setLayout(new BoxLayout(oldNetworkPanel, BoxLayout.X_AXIS));
        // oldNetworkPanel.add(oldNetwork);
        // oldNetworkPanel.add(Box.createHorizontalGlue());
        // oldNetworkPanel.setMaximumSize(new Dimension(800,35));
        //add(oldNetworkPanel);
        // add(Box.createVerticalStrut(12));

        oldDomains = new JCheckBox(Helper.getTranslation(SC_OLD_DOMAINS), false);
        oldDomains.addActionListener(this);
        add(oldDomains);
        // JPanel oldDomainsPanel = new JPanel();
        // oldDomainsPanel.setLayout(new BoxLayout(oldDomainsPanel, BoxLayout.X_AXIS));
        // oldDomainsPanel.add(oldDomains);
        // oldDomainsPanel.add(Box.createHorizontalGlue());
        // oldDomainsPanel.setMaximumSize(new Dimension(800,35));
        //add(oldDomainsPanel);
        add(Box.createVerticalStrut(12));

        oneRHS = new JCheckBox(Helper.getTranslation(SC_ONE_RHS_PANE), false);
        oneRHS.addActionListener(this);
        add(oneRHS);


        panelMinimized = new JCheckBox(Helper.getTranslation(SC_DIVIDER_LOC));
        panelMinimized.addActionListener(this);
        add(panelMinimized);

        add(Box.createGlue());
    }


    @Override
    public void setup() {
        String val;
	boolean boolval;

        val = Kirrkirr.kk.getProperty(PROP_OLDNETWORK);
        boolval = "true".equalsIgnoreCase(val);
        oldNetwork.setSelected(boolval);

	// This code was failing if PROP_OLDDOMAINS isn't defined.
        // I can't see why and it seems to work now....  CDM Sep 2004.
        val = Kirrkirr.kk.getProperty(PROP_OLDDOMAINS);
	boolval = "true".equalsIgnoreCase(val);
        oldDomains.setSelected(boolval);

        val = Kirrkirr.kk.getProperty(PROP_ONERHSPANE);
        boolval = "true".equalsIgnoreCase(val);
        oneRHS.setSelected(boolval);
        if (boolval) panelMinimized.setEnabled(false);

        val = Kirrkirr.kk.getProperty(PROP_SINGLEPANEL_STARTUP);
        boolval = "true".equalsIgnoreCase(val);
        panelMinimized.setSelected(boolval);
     }

    /** Called when "defaults" is pressed in KirrkirrOptionsDialog
     */
    @Override
    public void defaults() {
        oldNetwork.setSelected(true);
        oldDomains.setSelected(false);
        oneRHS.setSelected(false);
        panelMinimized.setSelected(false);
        panelMinimized.setEnabled(true);
        langField.setText("en");
        countryField.setText("AU");
        revalidate();  // to get text fields displaying correctly
    }


    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == oldDomains) {
        } else if (src == oldNetwork) {
        } else if (src == oneRHS) {
            panelMinimized.setEnabled( ! oneRHS.isSelected());
        } else if (src == panelMinimized) {
        } else {
            RelFile.editWriteDirectory();
            liveTempDirField.setText(RelFile.WRITE_DIRECTORY);
            if (Kirrkirr.profileManager!=null) {
                Kirrkirr.profileManager.setUnsaved();
            }
        }
    }


    @Override
    public void apply() {
        Kirrkirr.kk.changeProperty(PROP_OLDDOMAINS, oldDomains.isSelected());
        Kirrkirr.kk.changeProperty(PROP_OLDNETWORK, oldNetwork.isSelected());

        Kirrkirr.kk.changeProperty(PROP_LANG_CODE, langField.getText());
        Kirrkirr.kk.changeProperty(PROP_COUNTRY_CODE, countryField.getText());

        Kirrkirr.kk.changeProperty(PROP_ONERHSPANE, oneRHS.isSelected());
        Kirrkirr.kk.changeProperty(PROP_SINGLEPANEL_STARTUP, panelMinimized.isSelected());
    }


    @Override
    public String getToolTip() {
        return SC_DESC;
    }

    /** Called by profile manager, in case the options
     *  panel wants to save any state. If so, it should also
     *  implement {@code loadState}.
     *  @see #loadState
     */
    @Override
    public void saveState(ObjectOutputStream oos) throws IOException{
        //is this right? what if they open it on a different computer?
        //but it would be good to save for applet folks...
        oos.writeObject(RelFile.WRITE_DIRECTORY);
    }

    /** Called by profile manager, in case the options
     *  panel wants to save/load state. If so, it should also
     *  implement {@code saveState}.
     *  @see #saveState
     */
    @Override
    public void loadState(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        RelFile.WRITE_DIRECTORY = (String)ois.readObject();
    }

    /** cw 2002: inner class to listen to dictDirButton (perhaps should
     * move tempDirButton's listener to an inner class as well).
     *
     * @see DirsOptionPanel#actionPerformed(java.awt.event.ActionEvent)
     * @see Kirrkirr.util.RelFile#editWriteDirectory()
     * @see Kirrkirr#promptForNewDictionary()
     * @see Kirrkirr#changeProperty(String, String)
     */
    public class PickDictDir implements ActionListener {

        // todo [cdm 2020]: Unify with 2 other ways of changing dictionary directory:
        // namely, promptForNewDictionary in Kirrkirr.java and Kirrkirr.LoadDictDir class actionPerformed()
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(new File(RelFile.WRITE_DIRECTORY)); // change? base dir (RelFile.codeBase)? - they actually start off the same (RelFile.Init)
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setMultiSelectionEnabled(false);
            fileChooser.setDialogTitle(Helper.getTranslation(SC_SELECT_DICT_DIR));
            int userChoice = fileChooser.showDialog(Kirrkirr.window, Helper.getTranslation(RelFile.SELECT));
            if (userChoice == JFileChooser.APPROVE_OPTION) {
                // this is how promptForNewDictionary() does it, but
                // it doesn't seem to be the best way...  Only works if subdir
                String dictDir = fileChooser.getSelectedFile().getName();
                // todo [FILE_REDO]: String dictDir = fileChooser.getSelectedFile().getPath();
                Kirrkirr.kk.changeProperty("kirrkirr.dictionaryDirectory",
                                           dictDir);  // RelFile.MakeFileName??
                // changeProperty() already saves, so unlike in
                //DirsOptionPanel, the following is unnecessary:
                //if (Kirrkirr.profileManager!=null)
                //Kirrkirr.profileManager.setUnsaved();

                liveDictField.setText(dictDir);

                // RelFile.setDictionaryDir()?? - but only want for next time at this point??
            }
        }
    } // end class PickDictDir

} // end class DirsOptionPanel

