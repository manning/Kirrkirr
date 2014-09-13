package Kirrkirr.ui.panel.domains;

import Kirrkirr.ui.panel.KirrkirrPanel;
import Kirrkirr.util.Helper;
import Kirrkirr.util.Dbg;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;


/** PositionTracker
 *  creates a sort of domain derivation display to show all the ancestors
 *  and children
 *  of the current domain.  Displays them top-down from left to right,
 *  where each position is shown as a circle of the same color as the
 *  corresponding domain in the domain tree.  The name of the domain is
 *  given by a tool tip.  If clicked, the domain tree will zoom to that
 *  level, updating both itself and the tracker.  It also includes a
 *  SensePanel displayed within it.
 *
 *  @author Ian Spiro
 *  @author Steven Miller
 */

public class PositionTracker extends JPanel implements ListSelectionListener {

    private DomainTree dtree;
    private JList hierarchyList;
    private JList childrenList;

    private static final String SC_CHILDREN = "Children";

    public PositionTracker(DomainTree dt, JPanel sensePanel, int size) {
	super();  //set ourself up as a scroll panel
	// setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());
        JPanel upperJPanel = new JPanel();
        upperJPanel.setLayout(new BoxLayout(upperJPanel, BoxLayout.Y_AXIS));

        dt.registerTracker(this);
	dtree = dt;

	PositionRenderer renderer = new PositionRenderer();

        int verticalScrollPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;
        if (Helper.onAMac()) {
            verticalScrollPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS;
        }
        int horizScrollPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        if (size > KirrkirrPanel.TINY && Helper.onAMac()) {
            horizScrollPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS;
        }

        if (size > KirrkirrPanel.TINY) {
	    hierarchyList = new JList(dt.hierarchy);
	    JScrollPane hierarchyScroller = new JScrollPane(hierarchyList,
				    verticalScrollPolicy,
				    horizScrollPolicy);
	    hierarchyList.setCellRenderer(renderer);
	    hierarchyList.setFixedCellHeight(20);
	    hierarchyList.setVisibleRowCount(6);
	    hierarchyList.setBackground(Color.lightGray);
	    hierarchyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    hierarchyList.addListSelectionListener(this);

	    //add the hierarchy list
	    upperJPanel.add(hierarchyScroller);
            upperJPanel.add(Box.createVerticalStrut(4));
	}
	upperJPanel.add(Box.createVerticalGlue());

	if (sensePanel != null) {
	    upperJPanel.add(sensePanel);
            if (size > KirrkirrPanel.TINY) {
	      upperJPanel.add(Box.createVerticalStrut(4));
            }
	    upperJPanel.add(Box.createVerticalGlue());
	}

	childrenList = new JList(dt.children);
	JScrollPane childrenScroller = new JScrollPane(childrenList,
				   verticalScrollPolicy,
				   horizScrollPolicy);
	childrenList.setCellRenderer(renderer);
	childrenList.setFixedCellHeight(20);
	// childrenList.setVisibleRowCount(6);
	childrenList.setBackground(Color.lightGray);
	childrenList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	childrenList.addListSelectionListener(this);
	// JPanel childrenBox = new JPanel();
        // childrenBox.setLayout(new BorderLayout());
        if (Dbg.DOMAINS) Dbg.print("PositionTracker: size is " + size);
        // if (size >= KirrkirrPanel.LARGE) {
          // childrenBox.setMaximumSize(new Dimension(400, 50));
        //   childrenBox.setMinimumSize(new Dimension(80, 20));
        // } else {
        //   childrenBox.setMaximumSize(new Dimension(300, 25));
        //   childrenBox.setMinimumSize(new Dimension(80, 20));
        // }
        JLabel childrenLabel = new JLabel(Helper.getTranslation(SC_CHILDREN),
                                          SwingConstants.CENTER);
	// childrenBox.add(childrenLabel, BorderLayout.NORTH);
	// add(childrenBox);
        upperJPanel.add(childrenLabel);
        add(upperJPanel, BorderLayout.NORTH);
        add(childrenScroller, BorderLayout.CENTER);
    }


    /** Implementation of the ListSelectionListener interface - when a user
     *  clicks on an item in the position list, we'd like to zoom to that
     *  level.
     */
    public void valueChanged(ListSelectionEvent e) {
	if (e.getValueIsAdjusting()) return; //only take last of rapid-fire selections

	JList source = (JList) e.getSource();
        Domain newGoal = null;
	if (source == hierarchyList)
	    newGoal = (Domain) hierarchyList.getSelectedValue();
	else if (source == childrenList)
	    newGoal = (Domain) childrenList.getSelectedValue();

	if (newGoal == null) //if no selection, return
	    return;
	dtree.zoomTo(newGoal, true);
    }


    /** Method called by domain tree to add/remove items from the current
     *  domain's derivation.
     */
    public void updateTracker() {
      if (hierarchyList != null) hierarchyList.clearSelection();
      childrenList.clearSelection();
    }



    /** For the JList - tells the list just to ask the item (which in this
     *  case is the Position object) to render itself. Since the Position is
     *  set up as a JLabel with the color and text of its corresponding
     *  domain,the position then displays in the list consistent with the
     *  domain.
     */
    static class PositionRenderer extends DefaultListCellRenderer {

	public Component getListCellRendererComponent(JList list,
						      Object value,
						      int index,
						      boolean isSelected,
						      boolean cellHasFocus) {
	    return new Position((Domain)value);
	}

    } // end class PositionRenderer

} // end class PositionTracker
