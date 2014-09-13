package Kirrkirr.ui.dialog;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.util.*;
import Kirrkirr.IndexMaker;
import Kirrkirr.IndexMakerTracker;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.util.*;
import java.io.*;

/** The <code>ToolsDialog</code> object provides a dialog for various
 *  Kirrkirr tools that deal with external files and other functionality
 *  that is not part of the dictionary and ui itself but is critical to
 *  its operation.  The initial motivation for this dialog is a dialog
 *  to run IndexMaker (i.e., to create an
 *  integrated tool for making .clk and domain files).
 *
 *  @author      Steve Miller
 */
public class ToolsDialog extends JDialog implements ActionListener, Runnable, FileFilter {

    //static string constants that need to be translated
    static final String SC_TOOLS_DESC = "Kirrkirr_File_Tools";
    static final String SC_OK = "OK";
    static final String SC_CANCEL ="Cancel";

    private static final String SC_DICT_FILE = "dictionary_file";
    private static final String SC_SPEC_FILE = "dictionary_specification_file";
    private static final String SC_FWD_FILE = "forward_index_file";
    private static final String SC_REV_FILE = "reverse_index_file";
    private static final String SC_DOM_FILE = "domain_file";
    private static final String SC_CONV_FILE = "domain_conversion_file";
    private static final String SC_ERROR_MAKING = "Error_making_indices";

    private static final String SC_FILL_PROP =
    	"Fill_Fields_Using_Existing_Properties_File";

    //buttons allowing for closing of dialog or creation of aux files
    private KirrkirrButton cancel, run;

    //main controls (checkboxes, text fields)
    private JPanel mainPanel; //container

    private ToolsDialogCheckBox makeForwardIndex; //toggle on/off
    private ToolsDialogCheckBox makeReverseIndex;
    private ToolsDialogCheckBox makeDomainFile;
    private ToolsDialogCheckBox useDomainConvFile;

    private AuxFilePanel dictFilePanel;   //fields for required files
    private AuxFilePanel specFilePanel;

    private AuxFilePanel forwardIndexPanel; //fields dependent upon which
    private AuxFilePanel reverseIndexPanel; //files are to be made
    private AuxFilePanel domainFilePanel;

    private AuxFilePanel domainConvFilePanel;

    private String propFile;

    //String form of various controls - obtained at time aux files are to
    //be made
    private String forwardIndex, reverseIndex, domFile, dictFile, specFile, domConvFile;

    //sizing info
    private static final Dimension minimumSize = new Dimension(500, 500);


    /** Create a new ToolsDialog window.
     *
     *  @param modal Here, false means non-modal. I.e., other windows can be
     *     active, and true means that this is a requestor that must be dealt
     *     with.
     */
    public ToolsDialog(Kirrkirr parent, boolean modal) {

        super(parent.window, Helper.getTranslation(SC_TOOLS_DESC), modal);

        // place and size the dialog
        // setLocationRelativeTo(parent);
        int xLoc = parent.getWidth()/2 - 250;
        int yLoc = parent.getHeight()/2 - 200;
        if (xLoc >= 0 && yLoc >= 0) {
            setLocation(xLoc, yLoc);
        }
        setSize(minimumSize);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        //set up the main controls
        setupMainPanel();

        //set up the buttons
        setupButtonPanel();

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                dispose();
            }
        });

        setVisible(true);
    }

    /** Sets up the buttons at the bottom of the dialog
     * OK will run the tool(s) selected, after which the
     * dialog will close.  Cancel will close the dialog.
     */

    private void setupButtonPanel() {
        //button to run a particular tool
        run = new KirrkirrButton(SC_OK, null, this);

        //button to close the dialog
        cancel = new KirrkirrButton(SC_CANCEL, null, this);

        //layout and add buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(run);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancel);
        buttonPanel.add(Box.createHorizontalGlue());
        getContentPane().add(buttonPanel);
    }

    /** Sets up the main panel in the tools dialog.  This includes
     * controls for the required input fields (dictionary file and
     * spec file), as well as those for tool-dependent inputs, such as
     * L1->L2 clk file, L2->L1 clk file, and domain XML file.
     */

    private void setupMainPanel() {
        //prepare the panel itself
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        //set up sub-panel where a properties file can be selected to fill in the data
        setupPropFilePanel();

        //separate panels
        mainPanel.add(Box.createVerticalStrut(10));

        //set up sub-panel containing fields for dictionary and spec file fields
        setupRequiredFields();

        //set up the index maker tools
        setupIndexMakerFields();

        //set up the domainBuilder tools
        setupDomainBuilderFields();

        //shove extra space to the bottom
        mainPanel.add(Box.createVerticalGlue());

        //add the pane to the dialog
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }


    /**
     * Sets up the sub-panel for using a dictionary properties file to fill in
     * the field names
     */
    private void setupPropFilePanel() {
        //set up the panel
        JPanel propFilePanel = new JPanel();
    	propFilePanel.setLayout(new BoxLayout(propFilePanel, BoxLayout.Y_AXIS));
    	propFilePanel.setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(SC_FILL_PROP)));

        // set up the properties file control
        JPanel propFilePanel2 = new AuxFilePanel("Properties file: ",
                KirrkirrFileFilter.PROP_ENTRY, false, this);
        propFilePanel.add(propFilePanel2);

        mainPanel.add(propFilePanel);
    }


    /** Sets up the controls for the required (common) fields for
     * all tests.  This includes the dictionary file and the spec file.
     */
    private void setupRequiredFields() {
        //set up the panel
        JPanel requiredFilesPanel = new JPanel();
        requiredFilesPanel.setLayout(new BoxLayout(requiredFilesPanel, BoxLayout.Y_AXIS));
        requiredFilesPanel.setBorder(BorderFactory.createTitledBorder("Required Input Files"));

        //set up the dictionary file control
        dictFilePanel = new AuxFilePanel("Dictionary file: ", KirrkirrFileFilter.XML_ENTRY);
        requiredFilesPanel.add(dictFilePanel);

        //set up the specification file control
        specFilePanel = new AuxFilePanel("Dictionary specification file:",
                                                KirrkirrFileFilter.XML_ENTRY);
        requiredFilesPanel.add(specFilePanel);

        //add this panel to the main panel.
        mainPanel.add(requiredFilesPanel);
    }

    /** Sets up the controls for the tool-dependent fields, including
     * forward and reverse index files, as well as the XML domain file.
     */

    private void setupIndexMakerFields() {
        //set up enclosing panel
        JPanel indexMakerPanel = new JPanel();
        indexMakerPanel.setLayout(new BoxLayout(indexMakerPanel, BoxLayout.Y_AXIS));
        indexMakerPanel.setBorder(BorderFactory.createTitledBorder("Index Files"));

        //set up forward index controls
        forwardIndexPanel = new AuxFilePanel("Forward index file:",
                                                          KirrkirrFileFilter.CLK_ENTRY);
        makeForwardIndex = new ToolsDialogCheckBox("Make forward index file", forwardIndexPanel);
        forwardIndexPanel.setEnabled(false);

        //set up reverse index controls
        reverseIndexPanel = new AuxFilePanel("Reverse index file:",
                                                          KirrkirrFileFilter.CLK_ENTRY);
        makeReverseIndex = new ToolsDialogCheckBox("Make reverse index file", reverseIndexPanel);
        reverseIndexPanel.setEnabled(false);

        //lay out the controls in the enclosing panel
        indexMakerPanel.add(makeForwardIndex);
        indexMakerPanel.add(forwardIndexPanel);
        indexMakerPanel.add(Box.createVerticalStrut(5));
        indexMakerPanel.add(makeReverseIndex);
        indexMakerPanel.add(reverseIndexPanel);

        //add panel to main panel
        mainPanel.add(indexMakerPanel);
    }

    /** Sets up the controls for the domain builder tool and places
     *  these controls in the main panel.
     */

    private void setupDomainBuilderFields() {
        //set up enclosing panel
        JPanel domainBuilderPanel = new JPanel();
        domainBuilderPanel.setLayout(new BoxLayout(domainBuilderPanel, BoxLayout.Y_AXIS));
        domainBuilderPanel.setBorder(BorderFactory.createTitledBorder("Semantic Domain File"));

        //set up controls
        domainFilePanel = new AuxFilePanel("XML domain file:", KirrkirrFileFilter.XML_ENTRY);
        makeDomainFile = new ToolsDialogCheckBox("Make domain file", domainFilePanel);
        domainBuilderPanel.add(makeDomainFile);

        //add controls to panel
        domainBuilderPanel.add(domainFilePanel);

        domainBuilderPanel.add(Box.createVerticalStrut(5));

        domainConvFilePanel = new AuxFilePanel("XML domain conversion file:", KirrkirrFileFilter.XML_ENTRY);
        useDomainConvFile = new ToolsDialogCheckBox("Use domain conversion file", domainConvFilePanel);

        domainBuilderPanel.add(useDomainConvFile);
        domainBuilderPanel.add(domainConvFilePanel);

        domainBuilderPanel.setEnabled(false);
        //add panel to main panel
        mainPanel.add(domainBuilderPanel);
    }

    //getters for sizing info
    public Dimension getMinimumSize() { return minimumSize; }
    public Dimension getPreferredSize() { return minimumSize; }

    /** How to respond to button clicks - if run, then run.  If
     * input was valid and tests started, close on completion.
     * If cancel is clicked, just close.
     */
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == run) {
            if (makeFiles()) {
                dispose();
            }
        } else if (src == cancel) {
            dispose();
        }
    }

    /** This is the callback for the AuxFilePanel that selects a properties
     *  file from which to fill in all the fields.
     *
     * @param file The file containing the properties
     * @return The return value is always true and has no meaning
     */
    public boolean accept(File file) {
        propFile = file.getName();
        if (propFile != null && ! propFile.equalsIgnoreCase("")) {
            File parent = file.getParentFile();
            if (parent != null) {
                fillInFields(parent.getName());
            } else {
                fillInFields(null);
            }
        }
        return true;
    }

    public void setDictFile(String folder, String dictFile) {
    	if (folder == null || dictFile == null) return;
    	File dict = new File(folder+RelFile.fileSeparator()+dictFile);
    	dictFilePanel.setText(dict.getAbsolutePath());
    }

    public void setSpecFile(String specFile) {
    	if (specFile == null) return;
    	specFilePanel.setText(specFile);
    }

    /**
     * Fills in the fields of the dialog according to a dictionary prop. file
     * specified through the class variable propFile (ouch!).
     */
    private void fillInFields(String parent) {
    	if (propFile == null) return;
    	Properties selectedProps = new Properties();

    	try {
            BufferedInputStream bis = new BufferedInputStream(RelFile.makeURL(parent, propFile).openConnection().getInputStream());
            PropertiesUtils.load(selectedProps, bis);
            bis.close();

            dictFile = selectedProps.getProperty("dictionary.dictionary");
            if (dictFile != null) {
            	dictFile = RelFile.MakeFileName(parent, dictFile);
            	dictFilePanel.setText(dictFile);
            } else {
            	dictFilePanel.setText("");
            }
            specFile = selectedProps.getProperty("dictionary.dictSpecFile");
            if (specFile != null) {
            	specFile = RelFile.MakeFileName(parent, specFile);
            	specFilePanel.setText(specFile);
            } else {
            	specFilePanel.setText("");
            }
            forwardIndex = selectedProps.getProperty("dictionary.index");
            if (forwardIndex != null) {
            	makeForwardIndex.setSelected(true);
            	forwardIndex = RelFile.MakeFileName(parent, forwardIndex);
            	forwardIndexPanel.setText(forwardIndex);
            } else {
            	makeForwardIndex.setSelected(false);
            	forwardIndexPanel.setText("");
            }
            reverseIndex = selectedProps.getProperty("dictionary.reverseIndex");
            if (reverseIndex != null) {
            	makeReverseIndex.setSelected(true);
            	reverseIndex = RelFile.MakeFileName(parent, reverseIndex);
            	reverseIndexPanel.setText(reverseIndex);
            } else {
            	makeReverseIndex.setSelected(false);
            	reverseIndexPanel.setText("");
            }
            domFile = selectedProps.getProperty("dictionary.domainFile");
            if (domFile != null) {
            	makeDomainFile.setSelected(true);
            	domFile = RelFile.MakeFileName(parent, domFile);
            	domainFilePanel.setText(domFile);
            } else {
            	makeDomainFile.setSelected(false);
            	domainFilePanel.setText("");
            }
            domConvFile = selectedProps.getProperty("dictionary.domainConverter");
            if (domConvFile != null) {
            	useDomainConvFile.setSelected(true);
            	domConvFile = RelFile.MakeFileName(parent, domConvFile);
            	domainConvFilePanel.setText(domConvFile);
            } else {
            	useDomainConvFile.setSelected(false);
            	domainConvFilePanel.setText("");
            }

        } catch (Exception e) {
            // Probably file not found. Do nothing.
            Dbg.print("Couldn't open propFile in ToolsDialog: " + e.toString());
        }
    }

    //returns: false if no tests were run due to insufficient files being
    //specified.  returns true if files were made, regardless of success.

    private boolean makeFiles() {
        //if nothing is to be created, just return;
        if(!makeDomainFile.isSelected() && !makeForwardIndex.isSelected()
           && !makeReverseIndex.isSelected())
            return true;

        //obtain and check input
        if(!inputIsComplete())
            return false;

        //make requested files
        Thread maker = new Thread(this);
        maker.start();

        return true;  //tests were attempted
    }

    /** Actually make auxiliary files based on user's requests.
     *  Note that this is done on a background thread.
     */
    public void run() {
        IndexProgressDialog tracker = new IndexProgressDialog(Kirrkirr.window,
                forwardIndex != null, reverseIndex != null, domFile != null);
        //forward and/or reverse index, etc.
        //this is a no-op if all index filenames are null
        try {
            if ( ! IndexMaker.makeIndexFiles(specFile, dictFile, forwardIndex,
                                      reverseIndex, domFile, domConvFile, tracker)) {
                // error! couldn't parse dict file on first pass
                JOptionPane.showMessageDialog(null,
                    Helper.getTranslation(SC_ERROR_MAKING) + ":\n" +
                      IndexMaker.lastError,
                    Helper.getTranslation(SC_ERROR_MAKING),
                    JOptionPane.ERROR_MESSAGE);
                tracker.dispose();
            }
        } catch (Throwable exception) {
            exception.printStackTrace();
            tracker.dispose();
        }
    }


    /** Predicate helper method to check the input in the fields for
     * consistency before running tests.  This does not check validity
     * of input, only if something has been supplied where necessary.
     */

    private boolean inputIsComplete() {
        forwardIndex = null;
        reverseIndex = null;
        domFile = null;

        //check dictionary file
        dictFile = dictFilePanel.getText();
        if (dictFile == null || dictFile.length() == 0) {
            //error - no dictionary file specified - pop up dialog saying
            //so
            popupNeedInputDialog(SC_DICT_FILE);
            return false;
        }

        //check specification file
        specFile = specFilePanel.getText();
        if (specFile == null || specFile.length() == 0) {
            //error - no spec file specified - pop up dialog saying so
            popupNeedInputDialog(SC_SPEC_FILE);
            return false;
        }

        //check forward index
        if (makeForwardIndex.isSelected()) {
            forwardIndex = forwardIndexPanel.getText();
            if(forwardIndex == null ||forwardIndex.length() == 0) {
                //error - no fwd index file specified = pop up dialog
                popupNeedInputDialog(SC_FWD_FILE);
                return false;
            }
        }

        //check reverse index
        if (makeReverseIndex.isSelected()) {
            reverseIndex = reverseIndexPanel.getText();
            if(reverseIndex == null || reverseIndex.length() == 0) {
                //error - no forward index filename specified - pop up
                //dialog saying so
                popupNeedInputDialog(SC_REV_FILE);
                return false;
            }
        }

        //domain file
        if (makeDomainFile.isSelected()) {
            domFile = domainFilePanel.getText();
            if(domFile == null || domFile.length() == 0) {
                //error - no domain file specified - pop up dialog saying so
                popupNeedInputDialog(SC_DOM_FILE);
                return false;
            }
        }

        //domain conversion file
        if (useDomainConvFile.isSelected()) {
        	domConvFile = domainConvFilePanel.getText();
        	if (domConvFile == null || domConvFile.length() == 0) {
        		popupNeedInputDialog(SC_CONV_FILE);
        		return false;
        	}
        }
        return true;
    }

    /** Bring up a dialog box explaining the input that must be entered.
     */
    private void popupNeedInputDialog(String inputName) {
        new NeedInputDialog(Kirrkirr.window, inputName);
    }

    /** Inner class creating a simple dialog to inform the user of
     *  additional input needed to run tools.
     */
    static class NeedInputDialog extends JDialog implements ActionListener {
        private static final String SC_SPECIFY = "You_must_specify_a";
        private static final String SC_NEED_INPUT = "Need_Input";

        public NeedInputDialog(JFrame owner, String inputName) {
            //create the dialog
            super(owner, Helper.getTranslation(SC_NEED_INPUT), true);


            //add the error message
            JLabel message = new JLabel(Helper.getTranslation(SC_SPECIFY)
                                        + " " +
                                        Helper.getTranslation(inputName) +
                                        ".", SwingConstants.CENTER);
            getContentPane().add(message, BorderLayout.CENTER);

            //add ok button for closing
            JPanel okPanel = new JPanel();
            KirrkirrButton ok = new KirrkirrButton(Helper.getTranslation(SC_OK), null, this);
            okPanel.add(ok);
            getContentPane().add(okPanel, BorderLayout.SOUTH);


            Dimension d = message.getPreferredSize();
            setSize(new Dimension(d.width + 20, 70));

            Rectangle bnds = owner.getBounds();
            //place it
            setLocation(bnds.x + bnds.width/2-d.width/2 - 10,
                        bnds.y + bnds.height/2-35);

            //pop up
            setVisible(true);
        }

        //when user clicks ok, just close
        public void actionPerformed(ActionEvent e) {
            dispose();
        }

    }


    /** Inner class that represents a glorified check box - behavior is simply
     * to associate itself with an AuxFilePanel, and set that panel's
     * enabled bit correspondingly whenever it is checked/un-checked.
     */
    static class ToolsDialogCheckBox extends JComponent implements ActionListener {

        private JCheckBox checkBox; //the actual control
        private AuxFilePanel associatedPanel; //panel to enable/disable on select/deselect

        public ToolsDialogCheckBox(String text, AuxFilePanel assocPanel) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            //add the control
            checkBox = new JCheckBox(text, false);
            checkBox.addActionListener(this);
            add(checkBox);
            add(Box.createHorizontalGlue());

            //set up association with our Aux panel
            associatedPanel = assocPanel;
            associatedPanel.setEnabled(checkBox.isSelected());
        }

        /** When toggled on and off, we simply set our associated
         * AuxFilePanel's enabled bit to our selected status
         */
        public void actionPerformed(ActionEvent e) {
            associatedPanel.setEnabled(checkBox.isSelected());
        }

        // accessor so that ToolsDialog may query our checkbox's status
        // when determining what tests to run
        public boolean isSelected() { return checkBox.isSelected(); }
        public void setSelected(boolean selected) {
        	checkBox.setSelected(selected);
        	associatedPanel.setEnabled(selected);
        }

    }


    /** Helper dialog that will stick around with a progress bar to track
     *  the IndexMaker's progress.
     *
     */
    public static class IndexProgressDialog extends JDialog implements IndexMakerTracker
    {
        private boolean doFwd, doRev, doDom; //whether each type of file
                                             //is being built
        private boolean doneDispose; //=false;

        private int stages, curStage;  //number of passes to complete

        private JLabel stageName;
        private JProgressBar progressBar;

        private static final String SC_INDEX_PROG = "Index Maker progress";

        private static final String SC_GET_FPOS = "Getting file positions";
        private static final String SC_FWD = "Making forward index";
        private static final String SC_REV = "Making reverse index";
        private static final String SC_DOM = "Making domain index";

        public IndexProgressDialog(JFrame owner, boolean forward,
                                   boolean reverse, boolean domains) {
            super(owner, Helper.getTranslation(SC_INDEX_PROG), false);

            doFwd = forward;
            doRev = reverse;
            doDom = domains;

            stages = 1; //for gathering fpos indices
            if(doFwd)
                ++stages;
            if(doRev)
                ++stages;
            if(doDom)
                ++stages;

            curStage = 1;

            JPanel progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel,
                                                  BoxLayout.Y_AXIS));
            progressPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            stageName = new JLabel("Step " + curStage + " of " + stages +
                                  ": "  + SC_GET_FPOS);
            stageName.setAlignmentX(CENTER_ALIGNMENT);

            //set up progress bar - max is irrelevant until we get
            //nextPassTotal notification
            progressBar = new JProgressBar(0, 100);  // initialized to min value
            stageName.setLabelFor(progressBar);
            progressBar.setAlignmentX(CENTER_ALIGNMENT);

            progressPanel.add(stageName);
            progressPanel.add(Box.createVerticalStrut(12));
            progressPanel.add(progressBar);
            getContentPane().add(progressPanel);
            Rectangle bnds = owner.getBounds();

            setLocation(bnds.x + bnds.width/2 - 125,
                        bnds.y + bnds.height/2 - 50);
            setSize(new Dimension(250,100));
            setVisible(true);
        }


        /* For the IndexMakerTracker interface
         *
         */
        public void totalStepsForPass(int nSteps) {
            progressBar.setMaximum(nSteps);
        }

        public void stepsDone(int nStepsDone) {
            progressBar.setValue(nStepsDone);
        }

        public void passDone() {
            ++curStage;  //move to next Stage

            //set up name for next stage
            if (doFwd) {
                stageName.setText("Step " + curStage + " of " + stages +
                                  ": " + SC_FWD);
                doFwd = false;
            } else if(doRev) {
                stageName.setText("Step " + curStage + " of " + stages +
                                  ": " + SC_REV);
                doRev = false;
            } else if(doDom) {
                stageName.setText("Step " + curStage + " of " + stages +
                                  ": " + SC_DOM);
                doDom = false;
            } else {
                maybeDispose();
            }
        }

        public void maybeDispose() {
            if (!doneDispose) {
                dispose(); //all done
                doneDispose = true;
            }
        }
    } // end class IndexProgressDialog

} // end class ToolsDialog

