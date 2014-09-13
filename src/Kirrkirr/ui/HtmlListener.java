package Kirrkirr.ui;

import javax.swing.JComponent;

/** HtmlListener.java - allows HTML Panel to notify it of Hyperlink updates.
 */
public interface HtmlListener {

    public abstract void wordClicked(String uniqueKey, JComponent signaller);

}

