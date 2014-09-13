package Kirrkirr.ui.panel.domains;

import javax.swing.*;
import java.awt.*;

import Kirrkirr.util.FontProvider;


/**  Helper class that encapsulates the behavior of one of the levels
 *   in the current domain derivation display.  This draws a word/domain
 *   as a rect/oval via paintComponent when it is just interior decoration
 *   not actually a Domain itself.
 */
public class Position extends JLabel {

    private Domain d;

    public Position(Domain dom) {
        this(dom, dom.getText());
    }

    public Position(Domain dom, String text) {
        super(text, SwingConstants.CENTER);
        setFont(FontProvider.WORD_LIST_FONT);
        d = dom;
    }

    public Domain getDomain() { return d; }
    public String toString() { return super.getText(); }


    /** Fill the cell in to resemble domains in DomainTree. */
    public void paintComponent(Graphics g) {
        boolean selected = d.isSelected();
        if (selected) {
            g.setColor(Color.yellow);
        } else {
            g.setColor(d.getFinalColor());
        }
        if (d.isWord()) {
            g.fillRect(0,0,getWidth()-1, getHeight()-1);
            if (selected) {
                g.setColor(Color.black);
                g.drawRect(0,0, getWidth()-1, getHeight()-1);
            }
        } else {
            g.fillOval(0,0, getWidth()-1, getHeight()-1);
        }
        super.paintComponent(g);
    }

}

