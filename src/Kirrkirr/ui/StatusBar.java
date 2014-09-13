package Kirrkirr.ui;

import Kirrkirr.util.RelFile;
import Kirrkirr.util.Helper;
import Kirrkirr.util.FontProvider;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** A Status Bar - Just like the pro's have.
 *  It will not only set the text in a nicely sunken in pane, but will
 *  clear the text after the 'DELAY' - just like in a real program!
 */
public class StatusBar extends JPanel implements ActionListener {
    /** Used for setting the delay */
    private Timer timer;
    /** The text displayed on the status bar */
    private JLabel status;
    /** The corner icon, currently with a picture of a dictionary.
     *  If icon is not available, says "Kirrkirr."
     *  cw 2002: now changed to display "dictLang->glossLang" or v.v. after
     *  switching lists, etc.: see the new setIconText() method, called
     *  indirectly by ScrollPanel.switchLists() through
     *  Kirrkirr.setStatusBarIconText()
     */
    private JLabel icon;
    /** How long to wait before clearing the message */
    private static final int DELAY = 6000;             //approx. 6 seconds
    private final Dimension preferredSize = new Dimension(480, 25);

    public Dimension getMinimumSize() {
        return preferredSize;
    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public StatusBar() {
        this(null);
    }

    /** Sets up the status bar with an empty message.
     *  Tries to put the image in <code>iconfile</code>
     *  in the lower left corner of the status bar.
     *  If it can't, just prints "Kirrkirr" instead.
     *  @param iconfile a file in the default images
     *    directory referring to the image to put in the
     *    lower left corner of the status bar. (Currently,
     *    a dictionary.) If null, the corner says "Kirrkirr"
     *    instead.
     */
    public StatusBar(String iconfile) {
        super();
        if(iconfile != null) {
            icon = new JLabel(RelFile.makeImageIcon(iconfile,false));
        } else {
            icon = new JLabel("Kirrkirr");
        }
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel ip = new JPanel();
        ip.setLayout(new BoxLayout(ip, BoxLayout.X_AXIS));
        ip.setBorder(BorderFactory.createLoweredBevelBorder());
        ip.add(icon);

        status = new JLabel("");
        status.setFont(FontProvider.PROMINENT_LARGE_INTERFACE_FONT);

        Dimension ss = new Dimension(450, 22);
        status.setMinimumSize(ss);
        status.setMaximumSize(ss);
        status.setPreferredSize(ss);
        status.setForeground(Color.black);
        JPanel sp = new JPanel();
        sp.setLayout(new BoxLayout(sp, BoxLayout.X_AXIS));
        sp.setBorder(BorderFactory.createLoweredBevelBorder());
        sp.add(Box.createHorizontalStrut(8));

        sp.add(status);
        sp.add(Box.createHorizontalGlue());

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createEmptyBorder(3, 1, 1, 1));        //top, left, bott, right
        add(ip);
        add(Box.createHorizontalStrut(5));
        add(sp);
        add(Box.createHorizontalStrut(3));

        timer = new Timer(DELAY, this);
        timer.setRepeats(false);
    }

    /** Sets the icon text to <code>newText</code>
     *  Whereas setText has a timer/delay associated
     *  with it, this does not.
     *  @param newText the new string to display
     */
    public void setIconText(String newText) {
        icon.setText(Helper.getTranslation(newText));
    }

    /** Sets the icon picture to <code>newIconFile</code>
     *  Whereas setText has a timer/delay associated
     *  with it, this does not.
     *  @param newIcon the new string to display
     */
    public void setIcon(String newIconFile) {
        icon.setIcon(RelFile.makeImageIcon(newIconFile, false));
    }

    /** Called when timer finishes. Clears the
     *  text.
     */
    public void actionPerformed(ActionEvent e) {
        timer.stop();
        clear();
    }

    /** Resets the timer and sets the status bar
     *  message to <code>txt</code>
     *  @param txt what to set the status bar to.
     */
    public synchronized void setText(String txt) {
        timer.restart();
        status.setText(txt);
    }

    /** Sets status bar message to the empty string.
     */
    public synchronized void clear() {
        status.setText("");
    }

} //end class StatusBar

