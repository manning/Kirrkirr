package Kirrkirr.ui.data;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.panel.optionPanel.KirrkirrOptionPanel;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.util.KirrkirrFileFilter;

import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/** The <code>ProfileManager</code> controls the loading/saving
 *  of default and user-defined profiles. It asks
 *  each KirrkirrPanel and each KirrkirrOptionPanel to load
 *  and store any information they might think useful in a profile.
 *  Currently, only the preferences set in the Options panels
 *  (node color, xsl file type, etc) are saved. Maybe should
 *  save History too?
 *
 *  @author Kevin Jansz
 *
 * (Madhu:'00	major modifications made to get profiles working)
 * (cw 2002: minor ui changes (e.g., added many missing
 * Helper.getTranslation() calls) - still a little sloppy...)
 */
public class ProfileManager { //implements ActionListener {

    //static string constants that need to be translated
    private static final String SC_CURRENT_PROFILE="The_current_profile_is";
    private static final String SC_CURRENT_DEFAULT="The_default_profile_is";
    private static final String SC_OLD_DICT="Warning:_this_profile_was_saved_with_an_old_dictionary";
    private static final String SC_UNSAVED="(not_saved)";
    private static final String SC_PROFILE="Profile";
    private static final String SC_DESC="Save_your_personalized_options_in_a_user_profile";
    private static final String SC_CHOOSE_PROFILE="Choose_a_Profile";
    private static final String SC_SAVE_SUCCESS="Successfully_saved_profile";
    private static final String SC_SAVE_ERROR="Kirrkirr_couldn't_save_the_profile";
    private static final String SC_LOAD_SUCCESS="Successfully_loaded_profile";
    private static final String SC_LOAD_ERROR="Kirrkirr_couldn't_load_profile";
    private static final String SC_CHANGE_DEFAULT="Change_Default_Profile_To";
    private static final String SC_AUTOLOAD="Autoload_default_profile_on_start_up?";
    private static final String SC_LOAD="Load_Profile";
    private static final String SC_SAVE_AS="Save_Profile";
    private static final String SC_DEFAULT = "Kirrkirr_Default";

    private boolean possiblyUnsaved; // = false;

    private File currentProfile, defaultProfile;
    private boolean autoload;

    /*private JFrame profileSettings;
      private JComboBox profilesMenu;
      //private JCheckBox autoload;
      private JButton OK, Cancel;
      private String myProfile;
      private String myAutoLoad;
    public static final String endMarker = "**"; */

    private Kirrkirr   parent;

    public ProfileManager (Kirrkirr parent, boolean autoloadDefault,
			   String defaultP) {
        this.parent = parent;
	if (defaultP==null || defaultP.equals(""))
	    defaultProfile=null;
	else
	    defaultProfile=new File(defaultP);
	currentProfile=null;
	autoload=autoloadDefault;
	parent.window.setTitle("Kirrkirr");
	if (autoload && defaultProfile!=null && loadProfile(defaultProfile)){
	    parent.window.setTitle("Kirrkirr: " + currentProfile.getName() + " " + SC_PROFILE);
	}
    }


    public KirrkirrOptionPanel getOptionPanel(){
	return new ProfileOptionPanel();
    }


    /** Open an InputStream on the requested file
     *  @param filename A complete filename path
     *  @param haveWriteAccess This is currently unused...
     *  @return an InputStream for this file
     *  @throws IOException if the file cannot be opened, etc.
     */
    private InputStream getInputStream(String filename,
			       boolean haveWriteAccess) throws IOException {
	URL url=new URL("file","",filename);
	if (Dbg.VERBOSE) Dbg.print("Loading profile URL: " + url);
	URLConnection uc;
	/*if (haveWriteAccess) {
	  url = RelFile.MakeWriteURL(Kirrkirr.usersFolder, filename);
	  } else {
	  url = RelFile.makeURL(Kirrkirr.usersFolder, filename);
	  }*/
	uc = url.openConnection();
	return(uc.getInputStream());
    }


    /** Load a stored profile.
     *  This routine handles difficulties in opening the profile.
     *  @param fullFilename a fully qualified filename
     *  @return true if profile was successfully loaded; false otherwise
     */
    private boolean loadProfile(File fullFilename) {
	if (Dbg.VERBOSE) Dbg.print("loadProfile "+fullFilename);
        try {
	    ObjectInputStream ois=new ObjectInputStream(getInputStream(fullFilename.toString(),true));
	    if (ois==null){
		parent.setStatusBar("trouble opening file "+fullFilename);
		return false;
	    }

            String ind = (String)ois.readObject();
	    String engIndex=(String)ois.readObject();
	    String xml = (String)ois.readObject();

	    for (int i=0;i<parent.kirrkirrOptions.numOptionPanels();i++){
		parent.kirrkirrOptions.optionPanelAt(i).loadState(ois);
	    }

	    for (int j = parent.TOPPANE; j < parent.NUMPANES; j++) {
		for(int i=0; i < parent.KKPANES ; i++) {
		    if(parent.KKTabs[j][i]!=null)
			parent.KKTabs[j][i].loadState(ois);
		}
	    }

            ois.close();

            boolean old_dictionary;
            if (parent.APPLET) {
                old_dictionary = ((! ind.equals((new URL(Kirrkirr.indexFile)).getFile()))
				  ||  (!engIndex.equals((new URL(Kirrkirr.engIndexFile)).getFile()))
				  ||  (! xml.equals((new URL(Kirrkirr.xmlFile)).getFile())));
            } else {
                old_dictionary = ((! ind.equals(Kirrkirr.indexFile)) ||
				  (! engIndex.equals(Kirrkirr.engIndexFile)) ||
				   (! xml.equals(Kirrkirr.xmlFile)));
            }

            if (old_dictionary) {
                parent.setStatusBar(SC_OLD_DICT+": "+xml);
                if (Dbg.K) System.out.println("old index: |"+ind+"| index: |"+
					      Kirrkirr.indexFile+"| old xml: |"+
					      xml+"| xmlFile: |"+Kirrkirr.xmlFile+"|");
            }
	    currentProfile=fullFilename;
	    parent.window.setTitle("Kirrkirr: " + currentProfile.getName() + " "+SC_PROFILE);
	    possiblyUnsaved=false;
	    return true;
        } catch(Exception e) {
	    if (Dbg.ERROR)
		e.printStackTrace();
	    JOptionPane.showMessageDialog(Kirrkirr.kk.window,
			  "Error loading profile from \n" + fullFilename,
					  "Profile load error",
					  JOptionPane.ERROR_MESSAGE);
	    return false;
        }
    }

    public boolean saveProfile(){
	File filename=getFilenameChoice(SC_SAVE_AS);
	if (filename==null) return false;
	if (saveProfile(filename)){
	    parent.setStatusBar(Helper.getTranslation(SC_SAVE_SUCCESS) +
				": " + filename.getName());
	    currentProfile=filename;
	    parent.window.setTitle("Kirrkirr: " + currentProfile.getName() + " "+SC_PROFILE);
	    parent.window.setTitle("Kirrkirr: " + filename.getName() + " "+SC_PROFILE);
	    return true;
	} else {
	    parent.setStatusBar(Helper.getTranslation(SC_SAVE_ERROR+": "+filename)); //cw 02: just added missing getTranslation call
	    return false;
	}
    }

    //fully qualified filename
    private boolean saveProfile(File fullFilename){
	if (Dbg.VERBOSE) Dbg.print("saveProfile "+fullFilename);
        try {
            //first read the current profiles
            ObjectOutputStream oos;
            //String wrfile = RelFile.MakeFileName(Kirrkirr.usersFolder, profile);
	    String wrfile=fullFilename.toString();

            oos = new ObjectOutputStream(new FileOutputStream(wrfile));
            oos.writeObject(Kirrkirr.indexFile);
	    oos.writeObject(Kirrkirr.engIndexFile);
            oos.writeObject(Kirrkirr.xmlFile);

	    for (int i=0;i<parent.kirrkirrOptions.numOptionPanels();i++){
		parent.kirrkirrOptions.optionPanelAt(i).saveState(oos);
	    }

      	    for (int j = parent.TOPPANE; j < parent.NUMPANES; j++) {
		for(int i=0; i< parent.KKPANES ; i++) {
		    if(parent.KKTabs[j][i]!=null)
			parent.KKTabs[j][i].saveState(oos);
		}
	    }

	    oos.close();
	    currentProfile=fullFilename;
	    possiblyUnsaved=false;
	    parent.window.setTitle("Kirrkirr: " + currentProfile.getName() + " "+SC_PROFILE);
        } catch(Exception e) {
	    if (Dbg.ERROR)
		e.printStackTrace();
	    JOptionPane.showMessageDialog(Kirrkirr.kk.window,
					  "Error saving profile to \n"+fullFilename, "Profile save error",
					  JOptionPane.ERROR_MESSAGE);
	    return false;
        }
	return true;
    }

    /** Returns true if kirrkirr should prompt
     *  user to save the profile before exiting.
     *  @return true if there may be unsaved profile information
     */
    public boolean isUnsaved() {
	return possiblyUnsaved;
    }

    /** Should be called every time something that
     *  would be saved in a profile is changed,
     *  so that Kirrkirr can prompt for saving
     *  when user exits. (Unsaved is reset if
     *  user stores profile.) Because multiple
     *  objects may alter this state, none can
     *  set it to false (except profile manager).
     */
    public void setUnsaved() {
	if (Dbg.VERBOSE) Dbg.print("Profile possibly unsaved");
        possiblyUnsaved = true;
    }

    /** Choose a file
     *  @param prompt The string that will appear in the Save/Open box.
     *  	It will be internationalized within this routine
     *  @return the chosen filename or null if the user cancels
     */
    private static File getFilenameChoice(String prompt) {
	JFileChooser fileChooser = new JFileChooser(new File(Kirrkirr.usersFolder));
	fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	fileChooser.setDialogTitle(Helper.getTranslation(SC_CHOOSE_PROFILE));
	fileChooser.setFileFilter(new KirrkirrFileFilter(KirrkirrFileFilter.PLAIN_ENTRY));
	int userChoice = fileChooser.showDialog(Kirrkirr.window,
						Helper.getTranslation(prompt));
	if (userChoice == JFileChooser.APPROVE_OPTION) {
	    return fileChooser.getSelectedFile();
	} else return null;
    }

    class ProfileOptionPanel extends KirrkirrOptionPanel
						 implements ActionListener{

	//our own copy of autoload and default profile,
	//in case user chooses "cancel"

	private boolean curAutoLoad;
	private File curDefaultProfile; // this can stay null if no profile set

	private JButton save, load, changeDefault;
	private JCheckBox autoloadCheck;
	private JLabel currentLabel, defaultLabel;

	ProfileOptionPanel() {
	    curAutoLoad = autoload;
	    curDefaultProfile = defaultProfile;

	    setLayout(new GridLayout(5,1));
	    setName(ProfileManager.SC_PROFILE);

	    //displays current profile
	    JLabel tempLabel=new JLabel(Helper.getTranslation(SC_CURRENT_PROFILE) + "  ");
	    currentLabel=new JLabel();
	    if (currentProfile==null)
		currentLabel.setText(Helper.getTranslation(SC_UNSAVED));
	    else
		currentLabel.setText(currentProfile.getName());
	    JPanel tempPanel=new JPanel();
	    tempPanel.setLayout(new BoxLayout(tempPanel,BoxLayout.X_AXIS));
	    tempPanel.add(tempLabel);
	    tempPanel.add(currentLabel);
	    add(tempPanel);

	    //save and load buttons
	    save=new KirrkirrButton(SC_SAVE_AS, this);
	    load=new KirrkirrButton(SC_LOAD, this);
	    tempPanel=new JPanel();
	    tempPanel.setLayout(new BoxLayout(tempPanel,BoxLayout.X_AXIS));
	    tempPanel.add(save);
	    tempPanel.add(Box.createHorizontalStrut(2));
	    tempPanel.add(load);
	    add(tempPanel);

	    //display default profile
	    tempLabel=new JLabel(Helper.getTranslation(SC_CURRENT_DEFAULT) +
				 "  ");
	    defaultLabel=new JLabel();
	    if (Dbg.VERBOSE)
		Dbg.print("def profile: "+defaultProfile);
	    if (defaultProfile==null)
		defaultLabel.setText(Helper.getTranslation(SC_DEFAULT));
	    else
		defaultLabel.setText(defaultProfile.getName());
	    tempPanel=new JPanel();
	    tempPanel.setLayout(new BoxLayout(tempPanel,BoxLayout.X_AXIS));
	    tempPanel.add(tempLabel);
	    tempPanel.add(defaultLabel);
	    add(tempPanel);

	    //change default button
	    changeDefault=new KirrkirrButton(SC_CHANGE_DEFAULT+"...",this);
	    tempPanel=new JPanel();
	    tempPanel.setLayout(new BoxLayout(tempPanel,BoxLayout.X_AXIS));
	    tempPanel.add(changeDefault);
	    add(tempPanel);

	    // autoload checkbox
	    // tempLabel=new JLabel(Helper.getTranslation(SC_AUTOLOAD));
	    autoloadCheck = new JCheckBox(Helper.getTranslation(SC_AUTOLOAD),
					  autoload);
	    autoloadCheck.addActionListener(this);
	    add(autoloadCheck);
	    // tempPanel=new JPanel();
	    // tempPanel.setLayout(new BoxLayout(tempPanel,BoxLayout.X_AXIS));
	    // tempPanel.add(tempLabel);
	    // tempPanel.add(autoloadCheck);
	    // add(tempPanel);
	}


	public void actionPerformed(ActionEvent e) {
	    Object o = e.getSource();
	    if (o==autoloadCheck){
		curAutoLoad=autoloadCheck.isSelected();
	    } else if (o==load) {
		File filename=getFilenameChoice(SC_LOAD);
		if (filename==null) return;
		if (loadProfile(filename)){
		    parent.setStatusBar(Helper.getTranslation(SC_LOAD_SUCCESS+": "+filename.getName()));
		    currentProfile=filename;
		    parent.window.setTitle(Helper.getTranslation("Kirrkirr: " + currentProfile.getName() + " "+SC_PROFILE));
		    currentLabel.setText(filename.getName());
		    repaint();
		} else {
		    parent.setStatusBar(Helper.getTranslation(SC_LOAD_ERROR+": "+filename));
		}
	    } else if (o==save) {
		File filename=getFilenameChoice(SC_SAVE_AS);
		if (filename==null) return;
		if (saveProfile(filename)){
		    parent.setStatusBar(Helper.getTranslation(SC_SAVE_SUCCESS+": "+filename.getName()));
		    currentProfile=filename;
		    parent.window.setTitle(Helper.getTranslation("Kirrkirr: " + currentProfile.getName() + " "+SC_PROFILE));
		    currentLabel.setText(filename.getName());
		    repaint();
		} else {
		    parent.setStatusBar(Helper.getTranslation(SC_SAVE_ERROR+": "+filename));
		}
	    } else if (o==changeDefault) {
		File filename=getFilenameChoice(SC_CHANGE_DEFAULT);
		if (filename==null) return;
		curDefaultProfile=filename;
		defaultProfile=curDefaultProfile;
		defaultLabel.setText(curDefaultProfile.getName());
		repaint();
	    }
	}


	/** Called when "cancel" is pressed in KirrkirrOptionsDialog
	 */
	public void cancel() {
	    curAutoLoad=autoload;
	    autoloadCheck.setSelected(curAutoLoad);
	    curDefaultProfile=defaultProfile;
	    // XXXX fix: curDefaultProfile can be null!
	    if (curDefaultProfile == null) {
		defaultLabel.setText(Helper.getTranslation(SC_DEFAULT));
	    } else {
		defaultLabel.setText(curDefaultProfile.getName());
	    }
	    if (currentProfile==null)
		currentLabel.setText(Helper.getTranslation(SC_UNSAVED));
	    else
		currentLabel.setText(currentProfile.getName());
	    repaint();
	}

	/** Called when "apply" is pressed in KirrkirrOptionsDialog
	 */
	public void apply() {
	    autoload=curAutoLoad;
	    defaultProfile=curDefaultProfile;
	    if (defaultProfile!=null){
		parent.changeProperty("kirrkirr.defaultProfile",defaultProfile.toString());
	    } else {
		parent.changeProperty("kirrkirr.defaultProfile"," ");
	    }

	    parent.changeProperty("kirrkirr.AutoLoadDefault", curAutoLoad);
	}

	/** Called when "defaults" is pressed in KirrkirrOptionsDialog
	 */
	public void defaults() {
	    curDefaultProfile=null;
	    defaultLabel.setText(Helper.getTranslation(SC_DEFAULT));
	    curAutoLoad=false;
	    autoloadCheck.setSelected(curAutoLoad);
	}


	public String getToolTip() {
	    return ProfileManager.SC_DESC;
	}

    } // class ProfileOptionPanel

}

