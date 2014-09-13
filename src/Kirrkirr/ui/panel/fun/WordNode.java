package Kirrkirr.ui.panel.fun;

import Kirrkirr.ui.panel.GraphPanel;
import Kirrkirr.Kirrkirr;
//import Kirrkirr.dictionary.DictFields;
//import Kirrkirr.dictionary.DictField;
//import Kirrkirr.dictionary.DictionaryInfo;
//import Kirrkirr.dictionary.DictionaryCache;
import Kirrkirr.util.Dbg;
import Kirrkirr.util.Helper;
//import Kirrkirr.util.RelFile;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
// import com.sun.java.accessibility.*;
import javax.swing.event.*;
//import java.io.Serializable;

/** WordNode.java - Represents a node in the FunPanel network
 * visualization.
 *
 * @author Conrad Wai 2002
 */
public class WordNode extends JComponent implements WordNodeModelListener {

    private WordNodeModel model;
    private FFFPanel parent;

    // for mouse listening - also stored in parent coord. sys.
    private int prevX;
    private int prevY;


    public WordNode(WordNodeModel m, FFFPanel p) {
	model = m;
	parent = p;

	// determine size/dimensions of model if first associated WordNode
	// (generally the case)
	if (!model.hasSize()) {
	    determineSize();
	}

	model.addNodeListener(this);
	synchToModel(model);

	// mouse listeners

	// MouseInputListener/Adapter instead?
	addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    updateMousePressed(e);

		}

		public void mouseReleased(MouseEvent e) {
		    maybeShowPopup(e);
		    if(model.beingDragged())
			moveNode(e, true);
		    model.setDrag(false); //reset so spring can take
					  //effect

		}
	    });

	addMouseMotionListener(new MouseMotionAdapter() {
		public void mouseDragged(MouseEvent e) {
		    if(model.beingDragged())
			moveNode(e, false);
		}
	    });
   }

    public WordNodeModel getModel() { return (model); }

    // implements WordNodeModelListener

    // maybe should create an adapter for this listener!!
    public void nodeColorChanged(WordNodeModel changed) { }
    public void nodeLocationChanged(WordNodeModel changed) { }
    public void nodeSizeChanged(WordNodeModel changed) { }
    public void nodeBoundsChanged(WordNodeModel changed) { }
    // improve (esp. when optimize painting)!!
    public void nodeChanged(WordNodeModel changed, int changeType) {
	switch (changeType) {
	    case WordNodeModel.NODE_COLOR_CHANGED:
		synchToModel(changed);
		break;
	    case WordNodeModel.NODE_LOCATION_CHANGED:
		synchToModel(changed);
		break;
	    case WordNodeModel.NODE_SIZE_CHANGED:
		synchToModel(changed);
		break;
	    case WordNodeModel.NODE_BOUNDS_CHANGED:
		synchToModel(changed);
		break;
	    default:
		synchToModel(changed);
		break;
	}
    }

    // manually control size, etc.
    private void synchToModel(WordNodeModel changed) {
	//	Dbg.print("got synchToModel");
	// do something with this.model??
	int x = changed.getX();
	int y = changed.getY();
	int w = changed.getWidth();
	int h = changed.getHeight();

	// is this setBounds call ok (in terms of Swing style)?
	// (sometimes this is called within Swing thread (when dragging) - right?)
	// (but sometimes we're not in Swing thread (when animating) - right?)
	//  - maybe use SwingUtilities.invokeLater() when calling
	//  model.translate() in this case...
	setBounds(x, y, w, h);  // will repaint only if bounds changed (right?)
	repaint();  // this repaint will take care of color change
		    // (necess. if above bounds comment accurate)
    }

    // mouse listening...

    // setSelected, maybeShowPopup, and setup prevX, prevY for possible drag
    private void updateMousePressed(MouseEvent e) {
	if(!e.isPopupTrigger()) {
	    prevX = getX() + e.getX();
	    prevY = getY() + e.getY();
	    model.setDrag(true);
	}
	else {
	    model.setDrag(false);
	}

	// request to be the selected shape
	//	parent.getModel().setSelected(this.getModel());  // is this unclean?

	//go directly to graph and have it tell its parent that the
	//selected node was changed.  then the notification can proceed
	//entirely top-down, avoiding looping notifications and properly
	//updating when node is selected in graph or externally in word list.
	parent.getModel().getGraph().selectedNodeChanged(getModel().getUniqueKey());


	//ALL of this is handled in the GraphPanel's setCurrentWord and
	//its sub-calls - don't need to repeat.

	//update tooltip for layered pane
	//	parent.setToolTipText(Helper.uniqueKeyToPrintableString(getModel().getUniqueKey()));

	// clicking on a node causes a graph.reshuffle() (bring this panel
	// to front if necessary - just return if already on top)
	//	parent.getModel().getGraph().reshuffle(parent);

	maybeShowPopup(e);
	// move to when a mouseClicked, or mouseReleased, event occurs??
	int clickCount = e.getClickCount();
	if (clickCount > 1) {
	    if (Dbg.NEWFUN) Dbg.print("Should be sprouting...");
 	    getModel().sproutLinks();  // toggle??  if not, then sproutLinks should check for already sprouted...
	}
    }

    private void maybeShowPopup(MouseEvent e) {
	if (e.isPopupTrigger()) {
	    parent.showPopup(getX() + e.getX(), getY() + e.getY());
	}

    }

    // maybe shouldn't allow drag if isPopupTrigger() or something??
    private void moveNode(MouseEvent e, boolean checkBounds) {
	int dx, dy;
	int currX = getX() + e.getX();
	int currY = getY() + e.getY();

	dx = currX - prevX;
	dy = currY - prevY;
	if(checkBounds)
	    model.translate(dx,dy, parent);
	else
	    model.translate(dx, dy, null);

	prevX = currX;  // update coords to present ones
	prevY = currY;
    }

    // paint methods

    // calculate the size, to be stored in the model
    // the model can't do this correctly itself, since it should not "know"
    // about the view...
    //
    // call JComponent.getGraphics() here so I can find out node
    // width/height at an arbitrary time (i.e., when I'm constructing the
    // node) - will it be null, or hack??  if so, could wait till
    // paintComponent called for first time...
    private void determineSize() {
	// is this an acceptable use of getGraphics()?? if not, can just
	// wait until paintComponent()...
	Graphics g = parent.getGraphics();  // or just this.getGraphics()??

	if (g == null)  // not displayable at this point (try again
	    return;     // when first paint)

// 	if (Dbg.NEWFUN) Dbg.print("determining size (no args)");

	// can we store some of this stuff for perf. (cf. paintComponent())
	Font funFont = parent.getFunFont();
	g.setFont(funFont);
	FontMetrics funFM = parent.getFunFontMetrics(g, funFont);

	// get display value rather than the unique key
	// (put in ivar for perf.??)
	// could alternatively use Helper.uniqueKeyToPrintableString() - option??
	String displayValue = Helper.getWord(model.getUniqueKey());

	determineSize(funFM, displayValue);
    }

    private void determineSize(FontMetrics funFM, String displayValue) {
	int w = 10 + funFM.stringWidth(displayValue);
	int h = funFM.getHeight() + 4;

	model.setSize(w, h);

// 	if (Dbg.NEWFUN) Dbg.print("determining size (two args) for "+this);
// 	if (Dbg.NEWFUN) Dbg.print("\tsize: "+w+", "+h);
    }

    // should make constants out of some of these "magic numbers"
    //
    // should do as little as poss. in here...
    /**
     * @todo: may be able to optimize node painting by drawing String,
     * Rect. onto a BufferedImage and caching this image (which rarely
     * changes), and paintComponent could then simply copy or "blit" the
     * image onto the screen
     */
    public void paintComponent(Graphics g) {
	// change to local coords to draw - always 0, 0??
        int x = model.getX() - getX();
        int y = model.getY() - getY();

	//if (Dbg.NEWFUN) Dbg.print("painting local " + x + ", " + y);

	// adapted from old FunPanel:

	Font funFont = parent.getFunFont();
        g.setFont(funFont);
	FontMetrics funFM = parent.getFunFontMetrics(g, funFont);

	g.setColor(model.getColor());

	// get display value rather than the unique key
	// (put in ivar for perf.??)
	// could alternatively use Helper.uniqueKeyToPrintableString() - option??
	String displayValue = Helper.getWord(model.getUniqueKey());

	// model should always store non-gloss val. w/o uniquifier stuff,
	// and tack that stuff on locally if necessary!!
	int w = 0;
	int h = 0;
	if (!model.hasSize()) {
// 	    w = 10;
// 	    h = funFM.getHeight() + 4;

// //         if (gloss) {
// //             h = (h * 2);
// //             w += Math.max(funfm.stringWidth(trimLbl),
// //                           funfm.stringWidth(n.getGlossLbl()));
// //             h += 2;     //2 above, 2 between and 2 pixel spaces below
// //         } else {
//             w += funFM.stringWidth(displayValue);
// //         }
// 	    model.setSize(w, h);
	    determineSize(funFM, displayValue);
	}
	else {
	    w = model.getWidth();
	    h = model.getHeight();
	}

      // not necess. w/ current listening scheme, etc., right??
// 	// this is a lot to be doing in paintComponent.  any way to
// 	// improve - should a new thread be forked??
// 	if (isMoving && linkEdges != null) {
// 	    int numLinks = linkEdges.size();
// 	    for (int i = 0; i < numLinks; i++) {
// 		LinkEdge linkEdge = (LinkEdge)linkEdges.elementAt(i);
// 		linkEdge.updateCoords();
// 	    }
// 	}

//      boolean put_in_poly = graph.parent.showPoly() && (n.hnum > 0);
// 	if (put_in_poly) {
// 	    w += 10;
// 	}

        g.fill3DRect(x, y, w, h, true);

	//changed so unfound words just have different text, not diff
	//block color
	//	g.setColor(Color.black);
	g.setColor(model.getNodeTextColor());

	g.drawString(displayValue, x+5, y + funFM.getAscent());  // improve coords??
//         if (!gloss) {
//             if (n.exact == null) {
//                 g.setColor(Color.gray);
//             }
//             g.drawString(trimLbl, x - (w-10)/2, (y - (h-4)/2) + funfm.getAscent());       //x,y is middle
//         } else {
//             if (n.exact == null) {
//                 g.setColor(Color.gray);
//             }
//             g.drawString(trimLbl, x - (w-10)/2, (y - (h-6)/2) + funfm.getAscent());
//             g.setColor(Color.gray);
//             g.drawString(n.getGlossLbl(), x - (w-10)/2, (y + 2) + funfm.getAscent());
//         }

//         if (put_in_poly) {
//             g.setFont(superscript);
//             g.setColor(Color.black);
//             g.drawString(String.valueOf(n.hnum), x + (w/2) - 10,
// 			 (y - (h-4)/2) + 8);
//             // g.setFont(funfont);
//         }

    }

}

