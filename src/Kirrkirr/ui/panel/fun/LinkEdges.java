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

/** LinkEdges.java - Central edge view in charge of drawing all edges in
 * the FunPanel network visualization.
 *
 * @author Conrad Wai 2002
 */
public class LinkEdges implements LinkEdgeModelListener {

    public static final int EDGE_WIDTH = 3;

    private FFFPanel canvas;

    // the view shouldn't be relied upon to be the one to provide this:
    // moved to FunModel...
//     private Vector edgeModels;

    public LinkEdges(FFFPanel canvas) {
	this.canvas = canvas;

// 	edgeModels = new Vector();
    }

    public void addEdge(LinkEdgeModel edgeModel) {
	edgeModel.addEdgeListener(this);

// 	edgeModels.addElement(edgeModel);

	redrawEdge(edgeModel);
    }

    public void removeEdge(LinkEdgeModel edgeModel) {
	edgeModel.removeEdgeListener(this);

	redrawEdge(edgeModel);  // repaint: when run through models,
				// hopefully this one won't be there!
    }

    // implement LinkEdgeModelListener

    public void edgeColorChanged(LinkEdgeModel changed) {
	redrawEdge(changed);
    }
    public void edgePointsChanged(LinkEdgeModel changed) {
	redrawEdge(changed);
    }
    public void edgeChanged(LinkEdgeModel changed, int changeType) {
    }

    private void redrawEdge(LinkEdgeModel edgeModel) {
// 	if (Dbg.NEWFUN) Dbg.print("Redrawing edge: " + edgeModel);

	Rectangle lastDrawnRect = edgeModel.getLastDrawnRect();

	Point[] endpoints = edgeModel.computeEndpoints();

	// this may not be the best way to compute this, but it works ok...

	Rectangle newBounds = new Rectangle(endpoints[0]);
	newBounds.add(endpoints[1]);  // add other point
	// the following line makes it so that width and height - even when
	// the two endpoints are collinear - are never 0, so repaint clip
	// is never "empty" for next redraw...
	newBounds.setSize(newBounds.width+1, newBounds.height+1);

	Rectangle repaintBounds = new Rectangle(lastDrawnRect);
	repaintBounds.add(newBounds);  // union...

	edgeModel.setLastDrawnRect(newBounds);  // update lastDrawnRect

	canvas.repaint(repaintBounds);
    }

    // this will be called whenever *anything* on the canvas is redrawn
    public void redrawRegion(Graphics g, Rectangle clip) {
	if (clip == null) {
	    // what should I do - ignore, or draw whole screen??
	    if (Dbg.NEWFUN) Dbg.print("clip null!");
	} else {
// 	    if (Dbg.NEWFUN) Dbg.print("clip: " + clip);

	    // we need to run through all the edges, since more than just
	    // the changed edge (if any) may need to be repainted - e.g.,
	    // crossed edges, dragging a node over an edge...
	    // actually, not so bad - this is analogous to what Swing must
	    // be doing anyways...
	    Vector edgeModels = canvas.getModel().getEdgeModels();
	    if (edgeModels == null) return;
	    int numEdges = edgeModels.size();
	    for (int i = 0; i < numEdges; i++) {
		LinkEdgeModel edgeModel = (LinkEdgeModel)edgeModels.elementAt(i);
		// now all edge models have up-to-date lastDrawnRects...
		if (clip.intersects(edgeModel.getLastDrawnRect())) {
		    // should really be drawing all these to an offscreen
		    // buffer, at least!!  (but doesn't swing double
		    // buffer automatically?  in which case, since image /
		    // buffer always changing, wouldn't improve perf. ...)
		    drawEdge(g, edgeModel);
		}
	    }
	}
    }

    // this is all being done within canvas' paintComponent, so try to
    // keep things at a minimum
    public void drawEdge(Graphics g, LinkEdgeModel edgeModel) {
// 	if (Dbg.NEWFUN) Dbg.print("drawing edge: " + this);

	g.setColor(edgeModel.getColor());

	// shouldn't have to compute this again!!
	Point[] endpoints = edgeModel.computeEndpoints();

	int x1 = endpoints[0].x;
	int x2 = endpoints[1].x;
	int y1 = endpoints[0].y;
	int y2 = endpoints[1].y;

	// adapted from old FunPanel:

	// note: can't just call drawLine since want to control thickness

        int dx = x2 - x1;
        int dy = y2 - y1;
	
        if (dx == 0) {
	    final int x[] = { x1+EDGE_WIDTH, x1-EDGE_WIDTH, x2-EDGE_WIDTH, x2+EDGE_WIDTH};
	    final int y[] = { y1, y1, y2, y2};
	    //Dbg.print("==: "+"x1 "+x1+" y1 "+y1+" x2 "+x2+" y2 "+y2+" dx "+dx+" dy" +dy);
	    g.fillPolygon(x, y, 4);
        } else {
	    double m = -1.0 * dy/dx;
	    double theta = Math.atan(m);
	    dx = (int) Math.abs(Math.round(EDGE_WIDTH * Math.sin(theta)));
	    dy = (int) Math.abs(Math.round(EDGE_WIDTH * Math.cos(theta)));
	    if (m < 0) {
		final int x[] = { x1+dx, x1-dx, x2-dx, x2+dx};
		final int y[] = { y1-dy, y1+dy, y2+dy, y2-dy};
		//Dbg.print("-ve: "+"x1 "+x1+" y1 "+y1+" x2 "+x2+" y2 "+y2+" dx "+dx+" dy" +dy);
		g.fillPolygon(x, y, 4);
	    } else {
		final int x[] = { x1-dx, x1+dx, x2+dx, x2-dx};
		final int y[] = { y1-dy, y1+dy, y2+dy, y2-dy};
		//Dbg.print("+ve: "+"x1 "+x1+" y1 "+y1+" x2 "+x2+" y2 "+y2+" dx "+dx+" dy" +dy);
		g.fillPolygon(x, y, 4);
	    }
        }
    }

}

