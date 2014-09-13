package Kirrkirr.ui.dialog;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.ui.panel.optionPanel.KirrkirrOptionPanel;
import Kirrkirr.ui.panel.KirrkirrPanel;
import Kirrkirr.util.Helper;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/** The <code>KirrkirrOptionsDialog</code> object provides a dialog with tabbed
 *  panes from which a tabbed pane's options can be accessed from, if
 *  the tabbed pane has an options dialog.
 *  @author      Kevin Jansz
 *  @see KirrkirrPanel#getOptionPanel
 *  (Madhu:'00 : minor modifications)
 */
public class KirrkirrOptionsDialog extends JDialog implements ActionListener {

    //static string constants that need to be translated
    private static final String SC_OPTIONS_DESC = "Edit_options_for_Kirrkirr";
    private static final String SC_OPTIONS_MAC = "Preferences_for_Kirrkirr";
    private static final String SC_APPLY = "Apply";
    private static final String SC_OK = "OK";
    // private static final String SC_CANCEL="Cancel";
    private static final String SC_DEFAULTS="Defaults";
    private static final String SC_CLOSE="Close";

    /** tabbed pane "parent" */
    private JTabbedPane tabbedPane;
    private JButton close, defaults, ok, apply;

    // this is sized to about the min. currently possible (Nov 2001): 580, 425
    private static final Dimension minimumSize = new Dimension(580, 425);


    /** Create a new KirrkirrOptionsDialog window.
     *
     *  @param window The frame with respect to which this dialog is positioned
     */
    public KirrkirrOptionsDialog(JFrame window) {
        super(window, Helper.getTranslation(Helper.onAMac() ? SC_OPTIONS_MAC : SC_OPTIONS_DESC), false);
        //false = non-modal; i.e., other windows can be active

        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(null);

        apply = new KirrkirrButton(SC_APPLY, "ok.gif", this);
        defaults = new KirrkirrButton(SC_DEFAULTS, "undo.gif", this);
        ok = new KirrkirrButton(SC_OK, "save.gif", this);
        close = new KirrkirrButton(SC_CLOSE, "cancel.gif", this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(apply);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(defaults);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(ok);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(close);
        buttonPanel.add(Box.createHorizontalGlue());

        // getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        setLocation(50, 50);
    }

    // cw 02: doesn't seem to "obey" this, though...
    public Dimension getMinimumSize() {
        return minimumSize;
    }

    public Dimension getPreferredSize() {
        return minimumSize;
    }

    public int numOptionPanels(){
        return tabbedPane.getTabCount();
    }


    /** To obtain the Option Panel from the tabbed Pane of the given index
     *  Madhu:'00
     *
     *  @param myOption Index of option panel
     *  @return The KirrkirrOptionPanel or null if index is too big
     */
    public KirrkirrOptionPanel optionPanelAt(int myOption) {
        if (tabbedPane.getTabCount() > myOption) {
            return (KirrkirrOptionPanel) tabbedPane.getComponentAt(myOption);
        }
        return null;
    }


    /** Adds a KirrkirrOptionPanel to the range of preferences panels
     *  @param optPanel The panel to be added
     */
    public void addOptionPanel(KirrkirrOptionPanel optPanel) {
        Dimension ds = new Dimension(500, 320);
        addOptionPanel(optPanel, ds);
    }


    /** Add an option panel.
     *
     *  @param optPanel The option panel
     *  @param ds It's size.
     */
    private void addOptionPanel(KirrkirrOptionPanel optPanel, Dimension ds) {
        optPanel.setMinimumSize(ds);
        optPanel.setPreferredSize(ds);
        optPanel.setMaximumSize(ds);

        tabbedPane.addTab(optPanel.getName(), null, optPanel,
                          Helper.getTranslation(optPanel.getToolTip()));
        // For some reason, this line of code causes a crash when starting up
        // Kirrkirr.
        // If you don't "pack()" or "setSize" your JFrame or Dialog, it will
        // not display when you say "show()" or "setVisible(true)"
        // Solution: have Kirrkirr call "pack()" immediately before calling
        // setVisible()
        // pack();
    }


    public void setup() {
        KirrkirrOptionPanel ctop;
        for (int i = 0, tabcnt = tabbedPane.getTabCount(); i < tabcnt; i++) {
            ctop = (KirrkirrOptionPanel)tabbedPane.getComponent(i);
            ctop.setup();
        }
        tabbedPane.setSelectedIndex(Kirrkirr.HTML);
    }


    public void actionPerformed(ActionEvent e) {
        KirrkirrOptionPanel kkoptionpanel;
        Object src = e.getSource();

        if (src == defaults) {
            kkoptionpanel = (KirrkirrOptionPanel)tabbedPane.getSelectedComponent();
            kkoptionpanel.defaults();
        } else if (src == apply) {
             kkoptionpanel = (KirrkirrOptionPanel)tabbedPane.getSelectedComponent();
             kkoptionpanel.apply();
        } else if (src == ok) {
            for( int i = 0 ; i < tabbedPane.getTabCount() ; i++) {
                kkoptionpanel = (KirrkirrOptionPanel)tabbedPane.getComponent(i);
                kkoptionpanel.apply();
            }
            dispose();
        } else if (src == close) {
            for( int i = 0 ; i < tabbedPane.getTabCount() ; i++) {
                kkoptionpanel = (KirrkirrOptionPanel) tabbedPane.getComponent(i);
                kkoptionpanel.cancel();
            }
            dispose();
        }
    }

}

