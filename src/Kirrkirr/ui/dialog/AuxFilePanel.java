package Kirrkirr.ui.dialog;

import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.util.RelFile;
import Kirrkirr.util.KirrkirrFileFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.FileFilter;
import java.io.File;

import javax.swing.*;

 /** Class to help unify code and behavior for a file input field
  * in any file-related dialog box.  This encapsulates a text field, its
  * label, and an accompanying browse button that enables searching for
  * an appropriate file.
  */
public class AuxFilePanel extends JPanel implements ActionListener {

    private static final String SC_BROWSE = "Browse...";
    private final JLabel fileTypeLabel; //what file to enter
    private final JTextField fileTextBox; //where to enter files
    private final KirrkirrButton browse; //a button to find files
    private final short extType; //type of file extension - see KirrkirrFileFilter
    private String innerFolder;
    private final boolean shortForm;
    private final FileFilter fFilter;
    
    public AuxFilePanel(String fileType, short fileExtType) {
    	this(fileType, fileExtType, false);
    }
    	
    public AuxFilePanel(String fileType, short fileExtType, boolean shortForm) {
        this(fileType, fileExtType, shortForm, null);
    }

    /** @param handler The handler is called on a file accepted in the
     *            FileChooser dialog.  We're really misusing the interface,
     *            since it doesn't filter but just processes.
     */
    public AuxFilePanel(String fileType, short fileExtType, boolean shortForm,
                        FileFilter handler) {
        super();
        extType = fileExtType;
        this.shortForm = shortForm;
        fFilter = handler;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        //set up the label
        fileTypeLabel = new JLabel(fileType);
        fileTypeLabel.setMaximumSize(new Dimension(160,30));
        fileTypeLabel.setPreferredSize(new Dimension(160,30));

        add(Box.createHorizontalStrut(15));
        add(fileTypeLabel);
        add(Box.createHorizontalStrut(5));

        //set up the text field
        fileTextBox = new JTextField(20);
        fileTextBox.setMaximumSize(new Dimension(75,30));
        add(fileTextBox);
        add(Box.createHorizontalStrut(5));

        //set up the browse button
        browse = new KirrkirrButton(SC_BROWSE, null, this);
        browse.setMaximumSize(new Dimension(60, 30));
        add(browse);
        add(Box.createHorizontalGlue());
        
        innerFolder = "";
    }

    //set all contained components to our enabled status
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        fileTypeLabel.setEnabled(enabled);
        fileTextBox.setEnabled(enabled);
        browse.setEnabled(enabled);
    }
    
    public void setInnerFolder(String path) {
    	innerFolder = path;
    }

    /** Respond to an action - in this case, only browse button click
     * is relevant.  We pop up a file chooser with the appropriate
     * file filter to help the user find a suitable file.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == browse) {
            //open up a file chooser dialog
            JFileChooser chooser;
            if (innerFolder.isEmpty()) {
                chooser = new JFileChooser(RelFile.dictionaryDir);
            } else {
                chooser = new JFileChooser(RelFile.dictionaryDir +
                                           RelFile.fileSeparator() +
                                           innerFolder);
            }
            chooser.setMultiSelectionEnabled(false);
            KirrkirrFileFilter filter = new KirrkirrFileFilter(extType);
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(this);

            // if a file is chosen
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                //fill in text field with chosen file name
                File fi = chooser.getSelectedFile();
                if (shortForm) {
            	    fileTextBox.setText(fi.getName());
                } else {
            	    fileTextBox.setText(fi.getAbsolutePath());
                }
                if (fFilter != null) {
                    fFilter.accept(fi);
                }
            }
        }
    }

    // since we encapsulate a text field, provide an accessor so that
    // the dialog can run tests using our input
    public String getText() { return fileTextBox.getText(); }
    
    public void setText(String text) { fileTextBox.setText(text); }
     
}

