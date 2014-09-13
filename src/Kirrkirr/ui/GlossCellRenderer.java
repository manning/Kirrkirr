package Kirrkirr.ui;

import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.GlossDictEntry;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.RelFile;
import Kirrkirr.util.FontProvider;
import Kirrkirr.ui.panel.ScrollPanel;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;


/** GlossCellRenderer is similar to HWCellRenderer, but currently
 *  GlossDictEntries have limited support for notes/pictures/sounds,
 *  (and they don't have homonym) so it doesn't require as much functionality.
 *  Since they share so much functionality, it might make sense to subclass
 *  or make an abstract superclass, but there is no need to right now.
 *  This is a nice abstraction from whats in the datamodel being reflected and
 *  what appears in the list.
 *
 *  @author kparton
 */
public class GlossCellRenderer extends JLabel implements ListCellRenderer,
                                                TableCellRenderer {

    /** Kirrkirr parent */
    private static Kirrkirr kirr;

    //image files for the icons that popup on the JLabel. Unfortunately
    //JLabel can only take one icon, so we need to cover
    //every combination possible.
    private static final String pictureIcon = "picture.gif";

    private StringBuffer word = new StringBuffer(30);


    public GlossCellRenderer(Kirrkirr kirrkirr) {
        kirr = kirrkirr;
        setOpaque(true);
        setHorizontalTextPosition(SwingConstants.LEFT);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int col){
        return getListCellRendererComponent(null,value,-1,isSelected,hasFocus);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus){
        GlossDictEntry de = null;

        // the cache can be null when Kirrkirr is loading up
        if (kirr.cache != null) {
            de = kirr.cache.getGlossIndexEntry(value.toString());
        }
        setBackground(isSelected ? Color.yellow : Color.white);
        if (de != null && de.numMatches() == 0) {
            setForeground(FontProvider.GLOSS_LIST_LIGHTER_COLOR);
        } else {
            setForeground(FontProvider.GLOSS_LIST_COLOR);
        }
        boolean markedsubw = (kirr.seeSubwords() &&
                              ((de != null) && de.isSubword));
        word.setLength(0); // empty out the StringBuffer
        if (markedsubw) {
            setFont(FontProvider.GLOSS_LIST_SUBWORD_FONT);
            word.append("  ");
        } else {
            setFont(FontProvider.GLOSS_LIST_FONT);
        }
        word.append(value.toString());
        if (de != null &&
            kirr.scrollPanel.getGlossAttribute(ScrollPanel.SEE_FREQ)) {
            word.append(" [").append(de.freq).append(']');
        }
        String str = word.toString();
        setText(str);

        setIcon(null); // clear out any icon
        if (de != null) {
            if (de.hasPics) {
                setIcon(RelFile.makeImageIcon(pictureIcon,false));
            }
        }
        if (Dbg.CELLRENDERING) {
            Dbg.print("Rendering |" + str + '|');
        }
        return this;
     }

 }

