package Kirrkirr.ui.panel.optionPanel;

import Kirrkirr.util.*;
import Kirrkirr.ui.panel.HtmlPanel;
import Kirrkirr.Kirrkirr;

import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


/** The {@code XslOptionPanel} object allows the user the option to choose
 *  a different Xsl file to be used in the
 *  generation of the HTML dictionary entries
 *
 *  @version     1.0, 20/08/99
 *  @author      Kevin Jansz
 *
 *  @see Kirrkirr.dictionary.DictionaryCache#xslChanged
 *
 *	(additions by Madhu '00
 *	User can also choose any other Xsl file from the system
 *	@version 2.1.5 2/12/00
 *
 *  Rewritten by cw 2002 in order to be less dictionary-specific by
 *  incorporating tags in the xml dictionary conversion spec file, rather
 *  than just using hard-coded values.
 *
 *  @version     3.0, 26/07/2002
 *  @author      Conrad Wai
 */
public class XslOptionPanel extends KirrkirrOptionPanel
	    				implements ActionListener {

    //static string constants that need to be translated
    private static final String SC_BROWSE = "Browse";
    // private static final String SC_CHOOSE_FORMAT="Format_of_Entries";
    private static final String SC_FORMAT_OPTIONS = "Format_options";
    private static final String SC_CHOOSE_XSL =
	"Choose a formatting style (XSL) file";
    private static final String SC_DESC = "Change_the_amount_of_detail_shown_in_formatted_entries";
    /*
    private static final String options[] = {
        "Just_meaning",
        "Basic_details",
        "Full_entry",
	"All_details",
	"In_Headword",
	"Other"};

    private static final String descriptions[] = {
        "Only_show_the_meaning_of_the_word_in_Gloss",
        "Display_just_the_definitions_and_the_links_to_other_words",
        "Display_the_standard_dictionary_entry",
	"Display_everything_in_the_dictionary_database",
	"Display_stuff_in_Headword_as_much_as_possible",
	"Select_any_desired_XSL_file" };

    public static final String fileNames[] = {
	"meaning.xsl",
	"basic.xsl",
	"full.xsl",
	"everything.xsl",
	"inheadword.xsl",
	""};
    */
    // cw 2002: keeping these static is somewhat questionable, but
    // DictionaryCache wants certain methods (e.g.,
    // getDefaultStyleSheet()) and variables (e.g., fileNames[]) to be
    // static
    private static String[] options;
    private static String[] descriptions;
    public static String[] fileNames;  // change approach: make symmetrical w/
				// other 2 (w/ "other" option at elem. 0)

    private final Kirrkirr kirr;

    // cw 2002: change numbering scheme
//     private static final int XSLFILES = 6;
//     private static final int defaultOption = 0;
//     private static final int OtherFileOption = 5;
//     private int previousOption = 0;
    private int numOptions;  // includes "other file" option
    public static int defaultOption;
    private static final int otherFileOption = 0;
    private int previousOption = 1;

    private static String xslFolder = "xsl";
    private static String defaultFolder = "xsl";

//     private JRadioButton xslFiles[] = new JRadioButton[XSLFILES];
    private final JRadioButton[] xslFiles;

    private final JButton browse;
    private final JTextField txtFile;
    private File fileBrowsed;
    //private static String fileBrowsedName;

    public XslOptionPanel(Kirrkirr kirr) {
	this.kirr = kirr;

	// cw 2002: init. what used to be hardcoded in

	numOptions = (kirr.dictInfo != null) ? kirr.dictInfo.getNumXslFiles(): 0;
        numOptions++;  // +1 for "other"
        defaultOption = (numOptions > 1) ? 1: 0;


        xslFiles = new JRadioButton[numOptions];

	options = new String[numOptions];
	descriptions = new String[numOptions];
	fileNames = new String[numOptions];

	for (int i = 0; i < numOptions; i++) {
	    if (i == otherFileOption) {
		options[i] = "Other";
		descriptions[i] = "Select_any_desired_XSL_file";
		fileNames[i] = "";
	    } else {
		options[i] = kirr.dictInfo.getXslShortname(i-1);
		descriptions[i] = kirr.dictInfo.getXslDescription(i-1);
		fileNames[i] = kirr.dictInfo.getXslFilename(i-1);
	    }
	}

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        ButtonGroup bg = new ButtonGroup();

       	//Madhu:'00 radio contains the radio buttons
        JPanel radio = new JPanel();
        radio.setLayout(new BoxLayout(radio, BoxLayout.Y_AXIS));
        radio.setAlignmentX(Component.LEFT_ALIGNMENT);
	//Madhu:'00 choice contains the Browse button and the textField
	JPanel choice = new JPanel();
	choice.setLayout(new BoxLayout(choice, BoxLayout.X_AXIS));
	choice.setAlignmentX(Component.LEFT_ALIGNMENT);
	choice.setAlignmentY(Component.CENTER_ALIGNMENT);
	add(radio);
	add(choice);

	browse = new JButton(Helper.getTranslation(SC_BROWSE));
	browse.setAlignmentY(Component.CENTER_ALIGNMENT);
	browse.setEnabled(false);
	txtFile = new JTextField(5);
	txtFile.setMaximumSize(new Dimension(250,30));
	txtFile.setAlignmentY(Component.CENTER_ALIGNMENT);
	txtFile.setEnabled(false);
	choice.add(browse);
	choice.add(Box.createHorizontalStrut(5));
	choice.add(txtFile);
	choice.add(Box.createHorizontalStrut(25));

	for (int i = 0; i < numOptions; i++) {
            xslFiles[i] = new JRadioButton(getOption(i));
            xslFiles[i].setSelected(false);
	    xslFiles[i].addActionListener(this);
	    if (i != otherFileOption) {  // add this last, for "visual accord"
		radio.add(xslFiles[i]);
		radio.add(new JLabel(this.getDescription(i)));
	    }
            bg.add(xslFiles[i]);
	}
	radio.add(xslFiles[otherFileOption]);  // add "other" option (last)
	radio.add(new JLabel(getDescription(otherFileOption)));

// 	xslFiles[0].setSelected(true);
	xslFiles[defaultOption].setSelected(true);
	browse.addActionListener(this);
	setName(Helper.getTranslation(HtmlPanel.SC_HTML_TITLE));
        setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(SC_FORMAT_OPTIONS)));
    }

    /** Returns the default style sheet, given a folder.
     *  Now gets passed a full folder path (dictionaryDir/xsl).
     *  @param folder The folder that has XSL stuff
     *  @return The default stylesheet path as a URL
     */
    public static String getDefaultStyleSheet(String folder) {
        xslFolder = folder;
	defaultFolder = folder;
        return RelFile.MakeURLString(xslFolder, fileNames[defaultOption]);
    }

    public static String getDescription(int i) {
    	return Helper.getTranslation(descriptions[i]);
    }

    public static String getOption(int i) {
    	return Helper.getTranslation(options[i]);
    }

    public void apply()  {
//         for(int i = 0 ; i < XSLFILES ; i++) {
	for (int i = 0; i < numOptions; i++) {
            if (xslFiles[i].isSelected() && ( i != previousOption) ) {
		if (Dbg.PROGRESS) Dbg.print("XSL: "+i +" was "+previousOption);
		if (Kirrkirr.profileManager!=null)
		    Kirrkirr.profileManager.setUnsaved();
                // int confirm = JOptionPane.showConfirmDialog(parent.window,
                //        "This will require the dictionary entries to be regenerated. Is this OK?"
                //        , "HTML regenerate", JOptionPane.YES_NO_OPTION);
                // if (confirm == JOptionPane.YES_OPTION) {
		// if i is one of the user's own xsl files then, MakeWriteURLString
		Helper.setCursor(kirr, true);
		//Madhu:'00, any xsl file can be chosen by the user
		if (i == otherFileOption  && fileBrowsed != null) {
		    xslFolder = "file:" + fileBrowsed.getAbsolutePath();
		    fileNames[otherFileOption] = fileBrowsed.getName();
		    //parent.cache.xslChanged("","");//error checking!
		    kirr.cache.xslChanged(xslFolder);
		    //not RelFile.MakeURLString(xslFolder,fileNames[i]));
		} else {
		    xslFolder=defaultFolder;
		    //parent.cache.xslChanged("","");
		    kirr.cache.xslChanged(RelFile.MakeURLString(xslFolder, fileNames[i]));
		}
		previousOption = i;
		Helper.setCursor(kirr, false);
                // } else {
                //    xslFiles[i].setSelected(false);
                //    xslFiles[previousOption].setSelected(true);
                // }
            }
        }
    }

    @Override
    public String getToolTip() {
	return SC_DESC;
    }

    @Override
    public void cancel() {
        setOption(previousOption);
    }

    @Override
    public void defaults() {
        setOption(defaultOption);
    }

    private void setOption(int opt) {
	for (int i = 0; i < numOptions; i++) {
            if ( i != opt ) {
                xslFiles[i].setSelected(false);
            } else {
                xslFiles[opt].setSelected(true);
            }
        }
    }

   //Madhu:'00 for selection of any other XSL file from the user's system
   @Override
   public void actionPerformed(ActionEvent e){
       Object src = e.getSource();

       if (src == browse) {
	   // the Browse button is enabled only when option OtherFileOption
	   // is selected
	   JFileChooser chooser = new JFileChooser();
	   chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	   chooser.setDialogTitle(Helper.getTranslation(SC_CHOOSE_XSL));
	   chooser.setFileFilter(new KirrkirrFileFilter(KirrkirrFileFilter.XSL_ENTRY));
	   //to set the current directory to kirrkirr..cannot set it to the xsl subfolder!
	   if (Dbg.ERROR) Dbg.print("Setting default file to " +
				RelFile.MakeURLString(xslFolder,fileNames[1]));
	   //chooser.setCurrentDirectory(new File(RelFile.MakeURLString(xslFolder,fileNames[1])));
	   chooser.setCurrentDirectory(new File(RelFile.MakeURLString(xslFolder)));
	   int returnVal = chooser.showOpenDialog(kirr);
	   if (returnVal == JFileChooser.APPROVE_OPTION){
	       fileBrowsed = chooser.getSelectedFile();
	       txtFile.setText(fileBrowsed.getAbsolutePath());
	   }
       } else if (src == xslFiles[otherFileOption]) {
           browse.setEnabled(true);
       } else {
           browse.setEnabled(false);
           txtFile.setText("");
       }
   }


    private void setXslFilename(String optionChosen)
    {
	int myChoice = otherFileOption; //a number that will not exist in the list;temporary soln
// 	for(int i=0;i<XSLFILES;i++){
	for (int i = 0; i < numOptions; i++) {
	    if(fileNames[i].equals(RelFile.getFile(optionChosen))){
		myChoice=i;
		break;
	    }
	}

	if(myChoice!= otherFileOption){
	    xslFiles[myChoice].setSelected(true);
	    txtFile.setText("");
	    this.repaint();
	    previousOption=myChoice;
	} else {
	    xslFiles[otherFileOption].setSelected(true);
	    txtFile.setText(RelFile.getFile(optionChosen));
	    this.repaint();
	    previousOption= otherFileOption;
	}
    }

    /** Called by profile manager, in case the options
     *  panel wants to save any state. If so, it should also
     *  implement <code>loadState</code>.
     *  @see #loadState
     */
    public void saveState(ObjectOutputStream oos) throws IOException{
	oos.writeObject(kirr.cache.xslfile);
    }

    /** Called by profile manager, in case the options
     *  panel wants to save/load state. If so, it should also
     *  implement <code>saveState</code>.
     *  @see #saveState
     */
    public void loadState(ObjectInputStream ois) throws IOException, ClassNotFoundException {
	String xslfile = (String)ois.readObject();
	kirr.cache.xslChanged(xslfile);
	setXslFilename(xslfile);
    }

}

