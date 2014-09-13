package Kirrkirr.ui.panel;

import Kirrkirr.Kirrkirr;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.FontProvider;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.ui.JTextFieldLimiter;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/** OneBoxPanel -- a small continuously visible panel that lets the
 *  user search for words.
 */
public class OneBoxPanel extends JPanel
    implements ActionListener, DocumentListener {

    private static final String SC_FIND="Find!";
    private static final String SC_CLEAR="Clear";
    private static final String SC_FILTER="Filter_Word_List";
    // private static final String SC_RESET="Reset Word List";
    private static final String SC_SEARCH="Search";

    private JTextField searchBox;
    private JButton    filter;
    // private JButton    reset;
    private JButton    clear;
    private JButton    find;
    private static Kirrkirr  parent;
    private static final Color boxColor=Color.green.darker().darker();


    public OneBoxPanel(Kirrkirr p, int kirrkirrSize) {
        super();
        parent = p;
        setBackground(boxColor);

        int len = 20;
        if (kirrkirrSize <= KirrkirrPanel.TINY) {
            len = 15;
        }
        searchBox = new JTextField(len);
        searchBox.setDocument(new JTextFieldLimiter());
        if (kirrkirrSize <= KirrkirrPanel.SMALL) {
            searchBox.setFont(FontProvider.PROMINENT_LARGE_WORD_FONT);
        } else {
            searchBox.setFont(FontProvider.PROMINENT_VERY_LARGE_WORD_FONT);
        }
        // stop search box growing vertically.  should choose size better.
        // box seemed too small on Mac, but I think must have been < 20?
        searchBox.setMaximumSize(new Dimension(300, 24));
        searchBox.getDocument().addDocumentListener(this);
        searchBox.addActionListener(this);
        searchBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        find = new KirrkirrButton(Helper.getTranslation(SC_FIND), this);
        find.setBackground(Color.green);
        find.setForeground(Color.black);
        find.setAlignmentY(Component.CENTER_ALIGNMENT);

        clear = new KirrkirrButton(SC_CLEAR, this);
        clear.setBackground(Color.red);
        clear.setAlignmentY(Component.CENTER_ALIGNMENT);

        filter = new KirrkirrButton(SC_FILTER, this);
        filter.setAlignmentY(Component.CENTER_ALIGNMENT);

        if ( ! Helper.onMacOSX()) {
            // can't do white foreground text on OS X
            clear.setForeground(Color.white);
            filter.setBackground(Color.black);
            filter.setForeground(Color.white);
        }

        // reset = new KirrkirrButton(SC_RESET, this);
        // reset.setBackground(Color.blue);
        // reset.setForeground(Color.white);
        // reset.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel sl = new JLabel(Helper.getTranslation(SC_SEARCH) + ":");
        sl.setOpaque(true);
        sl.setBackground(boxColor);//new Color(212, 164, 74));
        sl.setForeground(Color.white);
        if (kirrkirrSize <= KirrkirrPanel.SMALL) {
            sl.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);
        } else {
            sl.setFont(FontProvider.PROMINENT_VERY_LARGE_INTERFACE_FONT);
        }

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(Box.createHorizontalStrut(2));
        add(sl);
        add(Box.createHorizontalStrut(4));
        add(searchBox);
        add(Box.createHorizontalStrut(4));
        add(find);
        add(Box.createHorizontalStrut(3));
        add(clear);
        int boxSize = 8;
        if (kirrkirrSize <= KirrkirrPanel.SMALL) {
            boxSize = 5;
        }
        add(Box.createHorizontalStrut(boxSize));
        add(filter);
        add(Box.createGlue());//HorizontalStrut(3));
        //  add(reset);
        //add(Box.createHorizontalStrut(1));
        setMinimumSize(new Dimension(430, 30));
        setMaximumSize(new Dimension(4000, 30));
    }


    public void insertUpdate(DocumentEvent e) {
        parent.scrollPanel.scrollToWord(searchBox.getText());
    }


    public void removeUpdate(DocumentEvent e) {
        parent.scrollPanel.scrollToWord(searchBox.getText());
    }

    public void changedUpdate(DocumentEvent e) {
    }

    /** Gets selected text from the searchBox (if any)
     *  and copies it to the system clipboard, returning the
     *  length of the text copied, or 0 if there was none selected.
     *  @param isCut true if this should be a cut operation
     *  @return how many characters were copy (0 if no selection)
     */
    public int copyText(boolean isCut) {
        String selected = searchBox.getSelectedText();
        if (Dbg.CUTPASTE) {
            if (selected != null)
                Dbg.print("OneBoxPanel cut/copy; selected is " +
                          selected.length() + " chars\n  " + selected);
            else
                Dbg.print("OneBoxPanel cut/copy; selected is null");
        }
        if (selected != null) {
            if (isCut) {
                searchBox.cut();
            } else {
                searchBox.copy();
            }
            return(selected.length());
        } else {
            return(0);
        }
    }

    public void actionPerformed(ActionEvent e){
        Object obj = e.getSource();

        if (obj == searchBox || obj == find)
            parent.scrollPanel.updateSelection(searchBox.getText(), false);
        else if (obj == clear)
            searchBox.setText("");
        else if (obj == filter)
            parent.scrollPanel.updateSelection(searchBox.getText(), true);
        // else if (obj == reset)
        //    parent.resetHeadWordsGUI();
    }

} // end class OneBoxPanel

