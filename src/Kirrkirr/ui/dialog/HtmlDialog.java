package Kirrkirr.ui.dialog;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.HtmlListener;
import Kirrkirr.ui.KirrkirrButton;
import Kirrkirr.ui.panel.HtmlPanel;
import Kirrkirr.ui.panel.NotesPanel;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.*;

/** HtmlDialog - displays the formatted html + notes for the current word in
 *  a separate JFrame.
 */
public class HtmlDialog extends JFrame
        implements HtmlListener, ActionListener, HyperlinkListener {

    private static final String SC_NOTES_TITLE="Dictionary_Entry_And_Notes";
    private static final String SC_DEFAULT_TITLE="Dictionary_Entry";
    private static final String SC_CLOSE="Close";
    private static final String SC_ENTRY_NOTES="Entry_and_Notes";
    private static final String SC_FORWARD="Forward";
    private static final String SC_BACK="Back";
    private static final String SC_KEEP="Keep";
    private static final String SC_COPY="Copy";
    private static final String SC_CLIPBOARD_UNAVAIL="Can't_Access_Clipboard";

    public static final int NO_NOTES = 1;
    public static final int NOTES = 2;

    private final HtmlPanel    htmlGloss; // = null;
    private NotesPanel   textNotes;
    private final String current;
    private final boolean notes;
    private static Kirrkirr parent;
    private final JButton close;
    private final JButton b_forward;
    private final JButton b_back;
    private final JButton b_copy;
    private final JButton b_keep;
    private final Vector<String> history;
    private int currentPlace;

    private static final Dimension minimumSize = new Dimension(500, 380);

    @Override
    public Dimension getMinimumSize() {
        return minimumSize;
    }

    @Override
    public Dimension getPreferredSize() {
        return minimumSize;
    }


    public HtmlDialog(Kirrkirr p, String htmlFolder) {
        this(p, htmlFolder, NOTES, null);
    }

    public HtmlDialog(Kirrkirr p, String htmlFolder, String uniqueKey) {
        this(p, htmlFolder, NOTES, uniqueKey);
    }

    public HtmlDialog(Kirrkirr p, String htmlFolder, int arg,
                      String uniqueKey) {
	this(p, htmlFolder, arg, uniqueKey, false);
    }

    /** If arg != NOTES, then it is a helpfile, and uniqueKey is the help file
     */
    public HtmlDialog(Kirrkirr p, String htmlFolder, int arg,
                      String uniqueKey, boolean gloss) {
        super();
        parent = p;
        notes = (arg == NOTES);
        if (notes) {
	    setTitle(Helper.getTranslation(SC_NOTES_TITLE));
	    // This is a word.  Make sure word is available first...
	    parent.cache.ensureHtmlAvailable(uniqueKey);
        } else {
	    setTitle(Helper.getTranslation(SC_DEFAULT_TITLE));
        }

	current = uniqueKey;
        currentPlace = 0;
        history = new Vector<>();
        history.addElement(current);

        // show the frame; assume a screen size of at least 600x400
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width/2 - 300 + (int) (Math.random()*40),
                    screenSize.height/2 - 200 + (int) (Math.random()*40));

        WindowListener winlin = new WindowAdapter() {
            public void windowClosing(WindowEvent e) { dispose(); }
        };
        addWindowListener(winlin);

        JPanel pane = new JPanel();

        if (notes) {
            pane.setLayout(new BoxLayout(pane,BoxLayout.Y_AXIS));//new GridLayout(2,1));   //2 down, 1 across
            setSize(380, 300);
        }
        else {
            pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
            setSize(480, 350);     //ie for Html Help page
        }

        JPanel butt_p = new JPanel();
        close = new KirrkirrButton(SC_CLOSE, this);
        butt_p.add(close);

	// Because of the replication-of-file bug in JEditor, you must set
	// the word's html file now (hard to explain, a bit more detail
	// in HtmlPanel)
	    // For the help files, use the setCurrentWord method which
	    // understands this is not a word. For some reason these files
	    // have no problems in JEditor??
	    // added by Jessica Halida
	    // currently the file about.html sometimes displayed
	    // but sometimes it is attached to start.html which is undesirable
	    // htmlGloss = new HtmlPanel(parent, this, htmlFolder);
	htmlGloss = new HtmlPanel(parent, this, htmlFolder, uniqueKey);

        if (notes) {
            textNotes = new NotesPanel(parent);
            textNotes.setCurrentWord(current, gloss,
				     null, 0, 0);
	    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				       htmlGloss, textNotes);
	    //            pane.add(textNotes);
	    pane.add(splitPane);
	    splitPane.setOneTouchExpandable(true);
            setTitle(Helper.getWord(uniqueKey) + " : " +
		     Helper.getTranslation(SC_ENTRY_NOTES));
        } else {
	    pane.add(htmlGloss);
            setTitle(Helper.getWord(uniqueKey));
        }

        // create buttons and set up listeners for them
        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new BoxLayout(buttonpanel, BoxLayout.X_AXIS));

        // added by Jessica Halida
	// displaying additional button "Keep" besides "Copy"
        //if (notes) {

            // added by Andrei Pop - "Forward" and "Back" buttons
            b_back = new KirrkirrButton(SC_BACK, "back.gif", this);
            b_back.setPressedIcon(RelFile.makeImageIcon("backdown.gif",false));
            b_back.setRolloverIcon(RelFile.makeImageIcon("backRollover.gif", false));
            b_back.setRolloverEnabled(true);
            b_back.setName(Helper.getTranslation(SC_BACK));
            b_back.setToolTipText(Helper.getTranslation(SC_BACK));
            b_back.setEnabled(false);
            buttonpanel.add(b_back);

            b_forward = new KirrkirrButton(SC_FORWARD, "forward.gif", this);
            b_forward.setPressedIcon(RelFile.makeImageIcon("forwarddown.gif",false));
            b_forward.setRolloverIcon(RelFile.makeImageIcon("forwardRollover.gif", false));
            b_forward.setRolloverEnabled(true);
            b_forward.setName(Helper.getTranslation(SC_FORWARD));
            b_forward.setToolTipText(Helper.getTranslation(SC_FORWARD));
            b_forward.setEnabled(false);
            buttonpanel.add(b_forward);

            b_keep = new KirrkirrButton(SC_KEEP, "keep.gif", this);
            b_keep.setPressedIcon(RelFile.makeImageIcon("keepDown.gif",false));
            b_keep.setRolloverIcon(RelFile.makeImageIcon("keepRollover.gif",false));
            b_keep.setRolloverEnabled(true);
            b_keep.setName(Helper.getTranslation(SC_KEEP));
            b_keep.setToolTipText(Helper.getTranslation(SC_KEEP));
            b_keep.setEnabled(true);
            buttonpanel.add(b_keep);
        //}

        b_copy = new KirrkirrButton(SC_COPY, "copy.gif", this);
        b_copy.setPressedIcon(RelFile.makeImageIcon("copyDown.gif",false));
        b_copy.setRolloverIcon(RelFile.makeImageIcon("copyRollover.gif",false));
        b_copy.setRolloverEnabled(true);
        // b_copy.setName(Helper.getTranslation(SC_COPY)); // not used
        b_copy.setToolTipText(Helper.getTranslation(SC_COPY));
        // b_copy.setEnabled(true);  // this is the default
        buttonpanel.add(b_copy);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buttonpanel, BorderLayout.NORTH);
        getContentPane().add(pane, BorderLayout.CENTER);
        getContentPane().add(butt_p, BorderLayout.SOUTH);
        setVisible(true);
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
	Object obj = e.getSource();

        if (obj == close) {
            dispose();
        } else if (obj == b_forward) {

	    if ((currentPlace+1) < history.size()){
                currentPlace++;
                setCurrentWord((String)history.elementAt(currentPlace));
	    }
            b_forward.setEnabled((currentPlace + 1) < history.size());

        } else if (obj == b_back) {

            if (currentPlace > 0){
                currentPlace--;
                setCurrentWord((String)history.elementAt(currentPlace));
	    }
            b_back.setEnabled(currentPlace > 0);

        } else if (obj == b_copy) {
            copyText();
        } else if (obj == b_keep) {
            openHtmlDialog();
        }
    }


    /** A version of setCurrentWord that takes a uniqueKey (word+uniquifier).
     *  Also implements history.
     */
    public void setCurrentWord(String uniqueKey)
    {
        if (uniqueKey == null) {
            return;
        }

	// have to call via 'new' setCurrentWord now to make sure that the
	// needed stuff is in the cache.

        updateHistory(uniqueKey);
        htmlGloss.setCurrentWord(uniqueKey, false,
				 null, 0, 0);
        if (notes) {
            textNotes.setCurrentWord(uniqueKey, false,
				     null, 0, 0);
            setTitle(Helper.uniqueKeyToPrintableString(uniqueKey) +
		     " : " + Helper.getTranslation(SC_ENTRY_NOTES));
        } else {
            setTitle(uniqueKey);
        }
    }


    private void updateHistory(/*padded*/ String word){
        if(currentPlace == history.size()){
            history.addElement(word);
            currentPlace++;
	}
        if (!((currentPlace+1) < history.size())) {
            b_forward.setEnabled(false);
        } else {
            b_forward.setEnabled(true);
        }
        if (currentPlace > 0) {
            b_back.setEnabled(true);
        } else {
            b_back.setEnabled(false);
        }
   }


    /** implemented as part of the HtmlInterface */
    @Override
    public void wordClicked(String uniqueKey, JComponent signaller) {
        currentPlace++;
        history.setSize(currentPlace); // trimming vector
	history.addElement(uniqueKey);     // adding new place
	setCurrentWord(uniqueKey);
    }


    public void copyText() {
        int copied = htmlGloss.copyText(false);
        if ((copied == 0) && (notes)) {
            copied = textNotes.copyText(false);
        }

        if (copied == 0) {
            try {
                // get the system clipboard
                Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
                // copy the word to the transferable object
                StringSelection s = new StringSelection(Helper.getWord(current));
                // put text in clipboard
                c.setContents(s,s);
            } catch(SecurityException se) {
                parent.setStatusBar("Applet "+Helper.getTranslation(SC_CLIPBOARD_UNAVAIL));
                getToolkit().beep();
            }
        }
    }


    public void openHtmlDialog() {
        if (notes) {
            new HtmlDialog(parent, Kirrkirr.htmlFolder, current);
        } else {
            new HtmlDialog(parent, Kirrkirr.htmlFolder, NO_NOTES, current);
        }
    }

    /** Handle hyperlink's being clicked. */
    @Override
    public void hyperlinkUpdate(HyperlinkEvent event) {
        HyperlinkEvent.EventType eventType = event.getEventType();
        if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
            if (event instanceof HTMLFrameHyperlinkEvent) {
                HTMLFrameHyperlinkEvent linkEvent =
                        (HTMLFrameHyperlinkEvent) event;
                HTMLDocument document =
                        (HTMLDocument) displayEditorPane.getDocument();
                document.processHTMLFrameHyperlinkEvent(linkEvent);
            } else {
                showPage(event.getURL(), true);
            }
        }
    }

}

