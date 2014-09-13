package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.FontProvider;
import Kirrkirr.ui.panel.optionPanel.KirrkirrOptionPanel;

import javax.swing.*;
import javax.swing.event.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** NotesPanel: an editable panel for writing and saving
 *  notes to the dictionary.
 */
public class NotesPanel extends KirrkirrPanel implements ActionListener,
                                                         FocusListener {

    /** Hashtable for notes made.  This is a class variable, so that all
     *  NotesPanel's share the same notes.  Hence we initialize it here.
     */
    private static Hashtable notesMade = new Hashtable();

    public JTextArea textNotes; // = null;
    private String currentUniqueKey;
    /** true if the current word is actually an Gloss gloss */
    private boolean currentGloss;
    private JLabel currentTitle;
    private String origNotes = "";

    private static final Dimension minimumSize = new Dimension(50, 50);

    private static final Color default_yellow = new Color(255, 255, 187);
    private static Color new_bg = default_yellow;

    /** String constants (SC) that need to be translated for NotesPanel.java
     */
    protected static final String SC_NOTES_NAME     = "Notes";
    private static final String SC_NOTES_ROLLOVER = "Make_notes_about_words";
    // private static final String SC_COLOR_CHOOSER  = "Color_Chooser";

   /** Another string constant that doesn't need translation */
    private static final String SC_GLOSS_PREFIX = "ENG: ";


    public Dimension getMinimumSize() {
        return minimumSize;
    }

    public Dimension getPreferredSize() {
        return minimumSize;
    }


    public NotesPanel(Kirrkirr kparent) {
        super(kparent);
        setName(Helper.getTranslation(SC_NOTES_NAME));
        setLayout(new BorderLayout());
        textNotes = new JTextArea();
        textNotes.setBackground(new_bg);
        // textNotes.setEditable(true); // This is the default
        textNotes.setLineWrap(true);
        textNotes.setWrapStyleWord(true);
        // Make font bigger
        textNotes.setFont(FontProvider.VERY_LARGE_TEXT_FONT);
        textNotes.addFocusListener(this);
        //textNotes.addActionListener(this);
        JScrollPane scrollPane = new JScrollPane(textNotes,
                                      JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        currentTitle =  new JLabel(Helper.getTranslation(SC_NOTES_NAME));
        currentTitle.setFont(FontProvider.VERY_LARGE_ITALIC_TEXT_FONT);
        add(currentTitle, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        if (Dbg.NOTES) {
            Dbg.print("Made NotesPanel; color " + textNotes.getBackground());
        }
    }


    public String getTabRollover() {
        return Helper.getTranslation(SC_NOTES_ROLLOVER);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == textNotes) {
            saveNotesMade();                    //ie the user hit <_|Enter
        }
    }


    public void focusGained(FocusEvent e) {
        if (Dbg.NOTES) {
            Dbg.print("Notes focus gained color " + textNotes.getBackground());
        }
    }


    public void focusLost(FocusEvent e) {
        //ie if the user moves to another component
        saveNotesMade();
    }


    /**
     * When the user presses enter or moves to another component,
     * this function is called, and it saves any changes in the notes
     * the user has made into the profile.
     */
    public void saveNotesMade() {
        // save notes only if they are different
        String newNotes = textNotes.getText();
        // we might want to trim whitespace!
        if ( ! origNotes.equals(newNotes)) {
            if (currentGloss) {
                notesMade.put(SC_GLOSS_PREFIX + currentUniqueKey, newNotes);
            } else {
                notesMade.put(currentUniqueKey, newNotes);
            }
            // a word has a note if the text is not null.
            boolean hasNote = ( ! newNotes.equals(""));
            if (Dbg.NOTES && hasNote) Dbg.print("hasnote=true! " +
                    currentUniqueKey +"| eng:" +currentGloss);
            if (! currentGloss) {
                parent.cache.addEntryUpdateNote(currentUniqueKey, hasNote);
            }
            if (parent.profileManager!=null)
                parent.profileManager.setUnsaved();
        }
    }


    /**
     * Called by Kirrkirr when the current word selected is changed.
     * Clears the notes for the old word and loads the notes for the
     * new word (if there are any).
     */
    public void setCurrentWord(final String tailWord, boolean gloss,
                        final JComponent signaller, final int signallerType,
                        final int arg)
    {
        setCurrentWord(tailWord, gloss);
    }

    /**
     * Clears the notes for the old word and loads the notes for the
     * new word (if there are any).
     */
    private void setCurrentWord(String uniqueKey, boolean gloss) {
        if (Dbg.NOTES) Dbg.print("Notes.setCurrentWord: |" + uniqueKey +
                                   "| gloss: " + gloss);
        currentTitle.setText(Helper.getTranslation(SC_NOTES_NAME) + ": " +
                                 Helper.uniqueKeyToPrintableString(uniqueKey));
        currentUniqueKey = uniqueKey;
        currentGloss = gloss;
        // currentPaddedWord used as the key for the notes table
        if (gloss) {
            origNotes = (String) notesMade.get(SC_GLOSS_PREFIX +
                    currentUniqueKey);
        } else {
            origNotes = (String) notesMade.get(currentUniqueKey);
        }
        if (Dbg.NOTES) Dbg.print("origNotes for |" + currentUniqueKey +
                                 "|: |" + origNotes + "|");

        if (origNotes == null) {
            origNotes = "";
        }
        textNotes.setText(origNotes);
        repaint();
    }


    /**
     * Returns the color chooser option panel to change the color
     * of the notes. Overrides KirrkirrPanel.
     */
    public KirrkirrOptionPanel getOptionPanel () {
        return(new NotesOptionPanel(this));
    }

    public void setBackgroundColor(Color color) {
        if (color == null) {
            new_bg = default_yellow;
        } else {
            new_bg = color;
        }
        textNotes.setBackground(new_bg);
    }

    public Color getBackgroundColor(){
        return new_bg;
    }

    /** Gets selected text from the notes panel (if any)
     *  and copies it to the system clipboard, returning the
     *  length of the text copied, or 0 if there was none selected.
     *  Overrides KirrkirrPanel.
     *  @param isCut true if this should be a cut operation
     *  @return how many characters were copied (0 if no selection)
     */
    public int copyText(boolean isCut) {
        String selected = textNotes.getSelectedText();
        if (selected != null) {
            if (isCut) {
                textNotes.cut();
            } else {
                textNotes.copy();
            }
            return(selected.length());
        } else {
            return(0);
        }
    }
    /** Called by the profile manager. For many panels,
     *  this is a no-op, but any that wish to save/load state
     *  may override it and should also implement loadState.
     *  @see #loadState
     */
    public void saveState(ObjectOutputStream oos) throws IOException {
        oos.writeObject(notesMade);
    }


    /** Called by the profile manager. For many panels,
     *  this is a no-op, but any that wish to save/load state
     *  may override it and should also implement saveState.
     *  This reloads saved notes. If a note that was saved can't
     *  be found, it pops up an error message to the user instead
     *  of failing silently, but then removes the notes which fail
     *  from the current (but not the saved) profile.
     *  Except, if they are Gloss notes, and then we just
     *  keep on trucking.
     *  @see #saveState
     */
    public void loadState(ObjectInputStream ois) throws IOException,
                                                ClassNotFoundException {
        notesMade = (Hashtable)ois.readObject();
        boolean bad = false;
        for (Enumeration e = notesMade.keys(); e.hasMoreElements(); ){
            String cur=(String)e.nextElement();
            if ( ! cur.startsWith(SC_GLOSS_PREFIX)) {
                // it's a note about a Wrlp word
                if ( ! parent.cache.addEntryUpdateNote(cur,true)) {
                    if ( !bad) {
                        JOptionPane.showMessageDialog(Kirrkirr.kk.window,
                               "Error loading note from profile. " +
                               "Warning: your notes may not load successfully",
                                                  "Note load error",
                                                  JOptionPane.ERROR_MESSAGE);
                        bad=true;
                    }
                    notesMade.remove(cur);
                }
            }
        }
    }


    /** Class for displaying the color chooser
     *  to change the color of the notes.
     *  This assumes that the color is global to all NotesPanels,
     *  so we make the main class color a class variable.
     */
    static class NotesOptionPanel extends KirrkirrOptionPanel implements ChangeListener {

        private static final String SC_BACKGROUND     = "Background_Color";
        private static final String SC_DESC = "Change_the_color_of_the_notes_pane";

        private JColorChooser color;
        private NotesPanel notesp;

        NotesOptionPanel(NotesPanel notesp) {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setName(Helper.getTranslation(NotesPanel.SC_NOTES_NAME));
            this.notesp = notesp;
            setBorder(BorderFactory.createTitledBorder(Helper.getTranslation(NotesOptionPanel.SC_BACKGROUND)));

            if (Dbg.NOTES) {
                Dbg.print("Creating color chooser of color " +
                          notesp.getBackgroundColor());
            }
            color = new JColorChooser(notesp.getBackgroundColor());
            color.setPreferredSize(new Dimension(450, 318));
            color.getSelectionModel().addChangeListener(this);
            add(color);
        }


        public String getToolTip(){
            return SC_DESC;
        }


        public void stateChanged(ChangeEvent e) {
            // do nothing here.  Wait till they apply change.
            // setNotesColor(color.getColor());
        }

        /** Called when "apply" is pressed in KirrkirrOptionsDialog
         */
        public void apply() {
            setNotesColor(color.getColor());
        }

        /** Called when "defaults" is pressed in KirrkirrOptionsDialog
         */
        public void defaults() {
            setNotesColor(null);
        }

        /** Called by profile manager, in case the options
         *  panel wants to save any state. If so, it should also
         *  implement <code>loadState</code>.
         *  This saves the background color of the JTextArea.
         *  @see #loadState
         */
        public void saveState(ObjectOutputStream oos) throws IOException {
            oos.writeObject(notesp.getBackgroundColor());
        }

        /** Changes the color of <i>both</i> NotesPanel's at once by accessing
         *  them via the parent.
         *  Really there should be a better more general implementation of this:
         *   maybe the KirrkirrPanel class could keep
         *  a list of instantiations of it, and apply profile settings to each.
         */
        public void setNotesColor(Color c) {
            if (Dbg.NOTES) {
                Dbg.print("Notes setNotesColor " + c);
            }
            NotesPanel currentPanel = (NotesPanel) notesp.parent.KKTabs[Kirrkirr.TOPPANE][Kirrkirr.NOTES];
            currentPanel.setBackgroundColor(c);
            if (!notesp.parent.isOneRHSPane()){
                    currentPanel = (NotesPanel) notesp.parent.KKTabs[Kirrkirr.BOTPANE][Kirrkirr.NOTES];
            currentPanel.setBackgroundColor(c);
            }
        }


        /** Called by profile manager, in case the options
         *  panel wants to save/load state. If so, it should also
         *  implement <code>saveState</code>.
         *  @see #saveState
         */
        public void loadState(ObjectInputStream ois) throws IOException, ClassNotFoundException {
            setNotesColor((Color) ois.readObject());
        }

    } // end class NotesOptionPanel


} // end class NotesPanel


