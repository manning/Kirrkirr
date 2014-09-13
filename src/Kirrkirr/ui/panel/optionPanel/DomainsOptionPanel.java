package Kirrkirr.ui.panel.optionPanel;

import Kirrkirr.ui.panel.domains.Domain;
import Kirrkirr.ui.panel.NewSemanticPanel;
import Kirrkirr.util.Helper;

import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*; 

/** SuperClass for all option panels - defines methods required for notifying them of close
 *  events, etc. in the main KirrkirrOptionsDialog.
 *  (Madhu:'00 added the change and the setxslfilename methods)
 */
public class DomainsOptionPanel extends KirrkirrOptionPanel implements ActionListener{
    private JTextField drawLimit;
    private JLabel drawLimitLabel;
    private NewSemanticPanel nsp;
    private JCheckBox setLimit;
    private JCheckBox showPics;
    private JCheckBox showPicsOnly;

    private static final String SC_CHILD_CUTOFF = "Max_number_of_words_shown_per_domain";
    private static final String SC_NAME = "Domains";
    private static final String SC_SET_LIMITS = "Limit_number_of_words_shown_per_domain";
    private static final String SC_SHOW_PICS = "Show_thumbnail_images";
    private static final String SC_SHOW_PICS_ONLY = "Don't_show_text_if_there_is_a_picture";

    public DomainsOptionPanel(NewSemanticPanel semPanel) {	
	super();
	setName(Helper.getTranslation(SC_NAME));
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

	nsp = semPanel;

	JPanel checkPanel = new JPanel();
	setLimit = new JCheckBox(Helper.getTranslation(SC_SET_LIMITS), 
				 nsp.getLimitChildren());
	setLimit.addActionListener(this);
	checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.X_AXIS));
	checkPanel.add(setLimit);
	checkPanel.add(Box.createHorizontalGlue());
	checkPanel.setMaximumSize(new Dimension(800,35));
	add(checkPanel);

	String text = String.valueOf(Domain.getMaxChildren());
	drawLimit = new JTextField(text, 4);
	drawLimit.setMinimumSize(new Dimension(60, 30));
	drawLimit.setMaximumSize(new Dimension(60,30));
	drawLimitLabel = new JLabel(Helper.getTranslation(SC_CHILD_CUTOFF));
	drawLimitLabel.setLabelFor(drawLimit);		
	JPanel drawLimitPanel = new JPanel();
	drawLimitPanel.setLayout(new BoxLayout(drawLimitPanel, BoxLayout.X_AXIS));
	drawLimitPanel.add(Box.createHorizontalStrut(20));
	drawLimitPanel.add(drawLimitLabel);
	drawLimitPanel.add(Box.createHorizontalStrut(5));
	drawLimitPanel.add(drawLimit);
	drawLimitPanel.add(Box.createHorizontalGlue());
	drawLimitPanel.setMaximumSize(new Dimension(800,35));
	add(drawLimitPanel);
	
	add(Box.createVerticalStrut(15));

	showPics = new JCheckBox(Helper.getTranslation(SC_SHOW_PICS), false);
	showPics.addActionListener(this);
	JPanel showPicsPanel = new JPanel();
	showPicsPanel.setLayout(new BoxLayout(showPicsPanel, BoxLayout.X_AXIS));
	showPicsPanel.add(showPics);
	showPicsPanel.add(Box.createHorizontalGlue());
	showPicsPanel.setMaximumSize(new Dimension(800,35));
	add(showPicsPanel);

	showPicsOnly = new JCheckBox(Helper.getTranslation(SC_SHOW_PICS_ONLY),
				     false);
	showPics.addActionListener(this);
	JPanel showPicsOnlyPanel = new JPanel();
	showPicsOnlyPanel.setLayout(new BoxLayout(showPicsOnlyPanel,
						  BoxLayout.X_AXIS));
	showPicsOnlyPanel.add(Box.createHorizontalStrut(20));
	showPicsOnlyPanel.add(showPicsOnly);
	showPicsOnlyPanel.add(Box.createHorizontalGlue());
	showPicsOnlyPanel.setMaximumSize(new Dimension(800,35));
	add(showPicsOnlyPanel);

	add(Box.createVerticalGlue());
    }


    public void actionPerformed(ActionEvent e) {
	Object src = e.getSource();
	boolean selected;
	if (src == setLimit) {
	    selected = setLimit.isSelected();
	    //  enable/disable controls based on check box
	    drawLimitLabel.setEnabled(selected);
	    drawLimit.setEnabled(selected);
	} else if (src == showPics) {
	    selected = showPics.isSelected();
	    //  enable/disable controls based on check box
	    showPicsOnly.setEnabled(selected);
	}
	//for showPics, showPicsOnly toggles, wait until apply() to act
    }
    

    /** Called when "cancel" is pressed in KirrkirrOptionsDialog
     */
    public void cancel() {
    }

    /** Called when "apply" is pressed in KirrkirrOptionsDialog
     */
    public void apply() {
	boolean applyLimits = setLimit.isSelected();
	nsp.setLimitChildren(applyLimits);
	nsp.setMaxChildren(Integer.parseInt(drawLimit.getText()));

	nsp.showPics(showPics.isSelected(), showPicsOnly.isSelected());
    }    

    /** Called when "defaults" is pressed in KirrkirrOptionsDialog
     */
    public void defaults() {
    }    
    
    /** Return a string that describes the options panel.  This string will
     *  be internationalized by the caller.
     *  @return Description of option panel
     */
    public String getToolTip() {
	return null;
    }

    /** Do any necessary initialization before displaying each time.
     */
    public void setup() {
    }

    /** Called by profile manager, in case the options
     *  panel wants to save any state. If so, it should also
     *  implement <code>loadState</code>. 
     *  @see #loadState
     */    
    public void saveState(ObjectOutputStream oos) throws IOException {
    }    

    /** Called by profile manager, in case the options
     *  panel wants to save/load state. If so, it should also
     *  implement <code>saveState</code>. 
     *  @see #saveState
     */   
    public void loadState(ObjectInputStream ois) throws IOException, 
						ClassNotFoundException {
    } 

}

