package Kirrkirr.ui;

import Kirrkirr.Kirrkirr;
import Kirrkirr.dictionary.DictEntry;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;
import Kirrkirr.util.RelFile;
import Kirrkirr.util.FontProvider;
import Kirrkirr.ui.panel.ScrollPanel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.Vector;

/** <code>HWCellRenderer</code> is used to render native language headwords in
 *  both the ScrollPanel and the Advanced Search Search Results.
 *  It had to be written because there
 *  is such extensive use of the datamodel headWords that putting
 *  bracketted polysemy tails in it will mess things up.
 *  This is a nice abstraction from what's in the datamodel being reflected
 *  and what appears in the list.
 *  This needs to be written to be as fast as possible, as scroll list
 *  redrawing happens all the time (especially in JDK1.1.8!).
 *
 *  @author kjansz
 *  @author Christopher Manning
 */
public class HWCellRenderer extends JLabel implements ListCellRenderer,
                                                TableCellRenderer {

    private final Kirrkirr kirr;
    // extra space for long ones
    private final StringBuffer sb = new StringBuffer(30);

    private static final String pictureIcon="picture.gif";
    private static final String picturesoundIcon="picturesound.gif";
    private static final String picturesoundnoteIcon="picturenotesound.gif";
    private static final String picturenoteIcon="picturenote.gif";
    private static final String notesoundIcon="notesound.gif";
    private static final String noteIcon="note.gif";
    private static final String soundIcon="sound.gif";


    public HWCellRenderer(Kirrkirr kirr) {
        this.kirr = kirr;
        setOpaque(true);
        setForeground(FontProvider.WORD_LIST_COLOR);
        setHorizontalTextPosition(SwingConstants.LEFT);
    }

    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row,
                                                   int col) {
        return getListCellRendererComponent(null,value,-1,isSelected,hasFocus);
    }

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus)
    {
        final String origword = (String) value; // .toString();
        String word = Helper.getWord(origword);  // remove uniquifier
        DictEntry de = null;
        // the cache can be null when Kirrkirr is loading up
        if (kirr.cache != null) {
            de = kirr.cache.getIndexEntry(origword);
        }
        setBackground(isSelected ? Color.yellow : Color.white);
        boolean markedsubw = ((kirr.seeSubwords())
            && ((de != null) && de.isSubword));
        sb.setLength(0); // empty out the StringBuffer
        if (markedsubw) {
            setFont(FontProvider.WORD_LIST_SUBWORD_FONT);
            sb.append("  ");
        } else {
            setFont(FontProvider.WORD_LIST_FONT);
        }
        sb.append(word);
        if (kirr.scrollPanel != null) {
            String poly = Helper.getUniquifier(origword);
            boolean showpoly = poly != null &&
                kirr.scrollPanel.getAttribute(ScrollPanel.SEE_POLY);
            boolean showFreq = de != null &&
                kirr.scrollPanel.getAttribute(ScrollPanel.SEE_FREQ);
            boolean showPartOfSpeech = de != null &&
                kirr.scrollPanel.getAttribute(ScrollPanel.SEE_POS);
            boolean showDialect = de != null &&
                kirr.scrollPanel.getAttribute(ScrollPanel.SEE_DIALECT);

            if (showpoly) {
                sb.append(" (").append(poly).append(')');
            }

            if (showPartOfSpeech) {
                Vector v = kirr.cache.getPOS(de);
                if (v != null) {
                    sb.append(" <").append(v.elementAt(0).toString());
                    //can there be >1 POS?
                    for (int i = 1, size = v.size(); i < size; i++) {
                        sb.append(", ").append(v.elementAt(i).toString());
                    }
                    sb.append('>');
                }
            }
            if (showDialect) {
                Vector v = kirr.cache.getDialect(de);
                if (v != null) {
                    sb.append(" {").append(v.elementAt(0));
                    //can there be >1 dialect? yes.
                    for (int i=1;i<v.size();i++){
                        sb.append(", ").append(v.elementAt(i));
                    }
                    sb.append('}');
                }
            }
            if (showFreq) {
                sb.append(" [").append(de.freq).append(']');
            }
        }

        setText(sb.toString());
        if (de != null) {
            // it may be null at initial startup time
            if (de.hasPics) {
                if (de.hasSounds) {
                    if (de.hasNote){
                        setIcon(RelFile.makeImageIcon(picturesoundnoteIcon,false));
                    }  else{
                        setIcon(RelFile.makeImageIcon(picturesoundIcon,false));
                    }
                } else if (de.hasNote){
                    setIcon(RelFile.makeImageIcon(picturenoteIcon,false));
                } else {
                    setIcon(RelFile.makeImageIcon(pictureIcon,false));
                }
            } else if (de.hasSounds) {
                if (de.hasNote) {
                    setIcon(RelFile.makeImageIcon(notesoundIcon,false));
                }  else {
                    setIcon(RelFile.makeImageIcon(soundIcon,false));
                }
            } else if (de.hasNote) {
                setIcon(RelFile.makeImageIcon(noteIcon,false));
            } else {
                setIcon(null); // clear out any icon
            }
        } else {
            setIcon(null); // clear out any icon
        }
        if (Dbg.CELLRENDERING) {
            Dbg.print("Rendering |" + origword + "| as |" + word + '|');
        }
        return this;
    }

}

