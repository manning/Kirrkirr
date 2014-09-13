package Kirrkirr.ui.panel.optionPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import Kirrkirr.Kirrkirr;
import Kirrkirr.ui.panel.GraphPanel;
import Kirrkirr.util.FontProvider;
import Kirrkirr.util.Helper;


/** Options panel to change the color of the nodes in all
 *  of the fun panels in Kirrkirr (in GraphPanel, games, etc).
 */
public class FunPanelOptionPanel extends KirrkirrOptionPanel implements
                                        ChangeListener, MouseListener {

    private static final int /* NOTFOUND=0, */ NODE=1,CLICKED=2;
    private static Kirrkirr parent;

    private GraphPanel gpanel;
    private JColorChooser color;
    // private JTextArea flabel;
    private JTextArea nlabel, clabel;
    private int curmode;

    /* Note: need to change these for translation, to get rid of \n's */
    private static final String SC_DESC="Change_the_colors_of_the_words_floating_in_the_network";
    private static final String SC_NAME="Network";
    private static final String SC_INSTRUCTIONS="Click_on_which_type_\nof_word_to_change_\nthe_color_of";
    // private static final String SC_NOT_FOUND_DESC="'Not found'_Nodes\nThe_node_words_which_\nhave_no_dictionary_entry";
    private static final String SC_CLICKED_DESC="Clicked_Node\nThe_word_you_have_\njust_clicked_on";
    private static final String SC_NODE_DESC="Regular_Nodes\nWords_which_are_related_\nto_the_main_words";


    public FunPanelOptionPanel(Kirrkirr kparent, GraphPanel gpanel) {
        this.parent = kparent;
        this.gpanel = gpanel;
        setName(Helper.getTranslation(SC_NAME));
        JPanel colors=new JPanel();
        colors.setLayout(new BoxLayout(colors,BoxLayout.Y_AXIS));
        Dimension d=new Dimension(200,130);

        JTextArea label=new JTextArea(Helper.getTranslation(SC_INSTRUCTIONS)+": ");
        label.setEditable(false);
        label.setForeground(Color.blue);
        label.setFont(FontProvider.PROMINENT_VERY_LARGE_INTERFACE_FONT);
        label.setMaximumSize(d);
        colors.add(label);
        colors.add(Box.createVerticalStrut(5));

        // flabel=new JTextArea(Helper.getTranslation(SC_NOT_FOUND_DESC));
        // flabel.setEditable(false);
        // flabel.setMaximumSize(d);
        // flabel.addMouseListener(this);
        // flabel.setBackground(gpanel.getNotFoundNodeColor());
        // colors.add(flabel);
        // colors.add(Box.createVerticalStrut(5));

        clabel=new JTextArea(Helper.getTranslation(SC_CLICKED_DESC));
        clabel.setMaximumSize(d);
        clabel.addMouseListener(this);
        clabel.setEditable(false);
        clabel.setBackground(gpanel.getSelectedNodeColor());
        colors.add(clabel);
        colors.add(Box.createVerticalStrut(5));

        nlabel=new JTextArea(Helper.getTranslation(SC_NODE_DESC));
        nlabel.setMaximumSize(d);
        nlabel.setEditable(false);
        nlabel.addMouseListener(this);
        nlabel.setBackground(gpanel.getDefaultNodeColor());

        colors.add(nlabel);
        colors.setMaximumSize(new Dimension(200,400));

        JPanel p=new JPanel();
        p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));

        curmode=NODE;
        color = new JColorChooser(gpanel.getDefaultNodeColor());
        color.getSelectionModel().addChangeListener(this);

        p.add(colors);
        p.add(color);

        setLayout(new BorderLayout());
        add(p, BorderLayout.CENTER);
    }


    public void mouseClicked(MouseEvent e) {
        Object src = e.getSource();

        // if (src.equals(flabel)) {
        //     curmode=NOTFOUND;
        //     color.setColor(gpanel.getNotFoundNodeColor());
        // } else
        if (src.equals(nlabel)) {
            curmode=NODE;
            color.setColor(gpanel.getDefaultNodeColor());
        } else if (src.equals(clabel)) {
            curmode=CLICKED;
            color.setColor(gpanel.getSelectedNodeColor());
        }
    }


    public String getToolTip() {
        return SC_DESC;
    }


    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}


    public void stateChanged(ChangeEvent e) {
        if (Kirrkirr.profileManager != null)
            Kirrkirr.profileManager.setUnsaved();
        Color newColor = color.getColor();
        switch (curmode) {
        // case NOTFOUND:
        //     flabel.setBackground(newColor);
        //     break;
        case CLICKED:
            clabel.setBackground(newColor);
            break;
        case NODE:
            nlabel.setBackground(newColor);
            break;
        }
        parent.repaint();
    }

    public void apply(){
        // gpanel.setNotFoundNodeColor(flabel.getBackground());
        gpanel.setSelectedNodeColor(clabel.getBackground());
        gpanel.setDefaultNodeColor(nlabel.getBackground());
        parent.repaint();
    }


    /** Reset the colors to their defaults.  They are updated in the parent
     *  display and by resetting the background of the labels
     */
    /*   public void defaults() {
        FunPanel.focusColor=FunPanel.defaultFocusColor;
        flabel.setBackground(FunPanel.focusColor);
        FunPanel.selectColor=FunPanel.defaultSelectColor;
        clabel.setBackground(FunPanel.selectColor);
        FunPanel.nodeColor=FunPanel.defaultNodeColor;
        nlabel.setBackground(FunPanel.nodeColor);
        parent.repaint();
        }*/


    /** Called by profile manager, in case the options
     *  panel wants to save any state. If so, it should also
     *  implement <code>loadState</code>.
     *  @see #loadState
     */
    public void saveState(ObjectOutputStream oos) throws IOException{
        // oos.writeObject(gpanel.getNotFoundNodeColor());
        oos.writeObject(gpanel.getSelectedNodeColor());
        oos.writeObject(gpanel.getDefaultNodeColor());
    }

    /** Called by profile manager, in case the options
     *  panel wants to save/load state. If so, it should also
     *  implement <code>saveState</code>.
     *  @see #saveState
     */
    public void loadState(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // gpanel.setNotFoundNodeColor((Color)ois.readObject());
        gpanel.setSelectedNodeColor((Color)ois.readObject());
        gpanel.setDefaultNodeColor((Color)ois.readObject());
        parent.repaint();
    }

}

