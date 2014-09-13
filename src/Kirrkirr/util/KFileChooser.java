package Kirrkirr.util;

import java.io.File;
import java.awt.*;

import javax.swing.*;


/** Kirrkirr wrapper code for selecting a file.
 *  This can do either a JFileChooser or a awt.FileDialog,
 *  depending on a property.
 *  The code does localization of Strings internal to its processing.
 *
 *  @author Christopher Manning
 */
public class KFileChooser {

    private File currentDir;
    private int fileSelectionMode;
    private String dialogTitle;
    private JFileChooser jFileChooser;

    public KFileChooser(File currentDirectory) {
        currentDir = currentDirectory;
    }

    public KFileChooser(String currentDirectory) {
        currentDir = new File(currentDirectory);
    }

    /** Set what kind of files to allow.
     *
     * @param fsm This uses the constants defined by JFileChooser.
     */
    public void setFileSelectionMode(int fsm) {
        fileSelectionMode = fsm;
    }

    public void setDialogTitle(String title) {
        dialogTitle = title;
    }

    /**
     *
     * @param parent
     * @param approveButtonText
     * @return A JFileChooser option (APPROVE_OPTION, CANCEL_OPTION or ERROR_OPTION
     */
    public int showDialog(Component parent, String approveButtonText) {
        jFileChooser = new JFileChooser(currentDir);
        jFileChooser.setFileSelectionMode(fileSelectionMode);
        jFileChooser.setDialogTitle(Helper.getTranslation(dialogTitle));
        return jFileChooser.showDialog(parent, approveButtonText);
    }

    File getSelectedFile() {
        return jFileChooser.getSelectedFile();
    }
    
}
